package cc.azuramc.bedwars.devtools;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.DevtoolsConfig;
import cc.azuramc.bedwars.devtools.tool.ConfigTools;
import cc.azuramc.bedwars.devtools.tool.ExtensionTools;
import cc.azuramc.bedwars.devtools.tool.GameTools;
import cc.azuramc.bedwars.devtools.tool.MapTools;
import cc.azuramc.bedwars.devtools.tool.ServerTools;
import cc.azuramc.bedwars.devtools.tool.ShopTools;
import cc.azuramc.bedwars.extension.Extensions;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Dev-mode MCP endpoint that lets an AI client read and edit config files and maps
 *
 * @author an5w1r@163.com
 */
public final class Devtools implements McpServer.Host {

    public static final String DOCUMENT_ID = "devtools";
    private static final String DIRECTORY = "devtools";
    private static final long MAIN_THREAD_TIMEOUT_SECONDS = 5;
    private static final int AUDIT_VALUE_LIMIT = 300;

    private final AzuraBedWars plugin;
    @Getter
    private final LogBuffer logs = new LogBuffer(500);
    @Getter
    private final Markers markers;
    @Nullable
    private McpServer server;
    @Getter
    @Nullable
    private String token;
    private Set<String> builtIn = Collections.emptySet();

    public Devtools(AzuraBedWars plugin) {
        this.plugin = plugin;
        this.markers = new Markers(plugin);
    }

    public DevtoolsConfig config() {
        return plugin.getConfigService().get(DevtoolsConfig.class);
    }

    public boolean isRunning() {
        return server != null;
    }

    /**
     * @return why the endpoint could not start, null when it runs
     */
    @Nullable
    public String start() {
        stop();
        DevtoolsConfig config = config();
        try {
            if (!config.isAllowRemote() && !InetAddress.getByName(config.getHost()).isLoopbackAddress()) {
                return "host " + config.getHost() + " is not a loopback address, set allow-remote: true in devtools.yml to listen on it";
            }
            token = loadOrCreateToken(false);
            List<McpTool> tools = new ArrayList<>();
            tools.addAll(new ServerTools(plugin, this).tools());
            tools.addAll(new ConfigTools(plugin, this).tools());
            tools.addAll(new MapTools(plugin, this).tools());
            tools.addAll(new ShopTools(plugin, this).tools());
            tools.addAll(new GameTools(plugin, this).tools());
            tools.addAll(new ExtensionTools(plugin, this).tools());
            builtIn = new HashSet<>();
            for (McpTool tool : tools) {
                builtIn.add(tool.getName());
            }
            McpServer created = new McpServer(this, new McpContent(plugin, this), () -> withDevTools(tools), token,
                    plugin.getPlugin().getDescription().getVersion());
            created.start(config.getHost(), config.getPort());
            server = created;
        } catch (IOException e) {
            return "cannot listen on " + config.getHost() + ":" + config.getPort() + ": " + e.getMessage();
        }
        logs.install();
        LoggerUtil.warn("MCP devtools are listening on " + endpoint() + ", turn them off on production servers");
        if (config.isAllowRemote()) {
            LoggerUtil.warn("MCP devtools accept remote connections, anyone with the token can change this server");
        }
        return null;
    }

    private List<McpTool> withDevTools(List<McpTool> builtInTools) {
        List<McpTool> tools = new ArrayList<>(builtInTools);
        for (Extensions.Tool registered : plugin.getExtensions().getDevTools()) {
            if (builtIn.contains(registered.getName())) {
                continue;
            }
            tools.add(McpTool.external(registered.getName(), "From the plugin " + registered.getPlugin().getName() + ". " + registered.getDescription(),
                    registered.getInputSchema(), registered.isReadOnly(), args -> sync(() -> {
                        try {
                            return registered.getTool().call(args.json());
                        } catch (IllegalArgumentException e) {
                            throw new ToolError(e.getMessage());
                        }
                    })));
        }
        return tools;
    }

    public boolean isBuiltIn(String name) {
        return builtIn.contains(name);
    }

    /**
     * Starts keeping log lines before the endpoint runs, so startup problems show in logs_tail
     */
    public void captureLogs() {
        logs.install();
    }

    public void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
        logs.uninstall();
        markers.clear();
    }

    /**
     * @return why the endpoint could not restart with the new token, null when it is fine
     */
    @Nullable
    public String rotateToken() throws IOException {
        token = loadOrCreateToken(true);
        return isRunning() ? start() : null;
    }

    public String endpoint() {
        DevtoolsConfig config = config();
        String host = config.getHost();
        if ("0.0.0.0".equals(host) || "::".equals(host)) {
            host = "127.0.0.1";
        }
        if (host.contains(":")) {
            host = "[" + host + "]";
        }
        return "http://" + host + ":" + config.getPort() + McpServer.PATH;
    }

    public String connectCommand() {
        return "claude mcp add --transport http azurabedwars " + endpoint() + " --header \"Authorization: Bearer " + token + "\"";
    }

    public File directory() {
        return new File(plugin.getPlugin().getDataFolder(), DIRECTORY);
    }

    private String loadOrCreateToken(boolean rotate) throws IOException {
        File file = new File(directory(), "token");
        if (!rotate && file.isFile()) {
            String existing = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
            if (existing.length() >= 32) {
                return existing;
            }
        }
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        StringBuilder hex = new StringBuilder();
        for (byte b : random) {
            hex.append(String.format("%02x", b));
        }
        Files.createDirectories(directory().toPath());
        Files.write(file.toPath(), hex.toString().getBytes(StandardCharsets.UTF_8));
        return hex.toString();
    }

    /**
     * Runs a task on the server thread, Bukkit state must not be touched from the HTTP workers
     */
    public <T> T sync(Callable<T> task) throws Exception {
        if (Bukkit.isPrimaryThread()) {
            return task.call();
        }
        Future<T> future = Bukkit.getScheduler().callSyncMethod(plugin.getPlugin(), task);
        try {
            return future.get(MAIN_THREAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            if (future.cancel(false)) {
                throw new ToolError("The server did not get to the request within " + MAIN_THREAD_TIMEOUT_SECONDS + " seconds, nothing was changed, try again");
            }
            // already running on the main thread, it finishes as one step
            return unwrap(future, 60);
        } catch (ExecutionException e) {
            throw rethrow(e);
        }
    }

    private static <T> T unwrap(Future<T> future, long seconds) throws Exception {
        try {
            return future.get(seconds, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw rethrow(e);
        }
    }

    private static Exception rethrow(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof Exception) {
            return (Exception) cause;
        }
        return e;
    }

    @Override
    public void checkWriteAllowed() throws Exception {
        String blocked = sync(this::writeBlocker);
        if (blocked != null) {
            throw new ToolError(blocked);
        }
    }

    /**
     * Must run on the main thread
     *
     * @return why writes are refused right now, null when they are allowed
     */
    @Nullable
    public String writeBlocker() {
        if (config().isAllowWithPlayers()) {
            return null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(LoggerUtil.ADMIN_PERMISSION)) {
                return "Player " + player.getName() + " is online and is not an admin, writes are refused."
                        + " The server owner can set allow-with-players: true in devtools.yml";
            }
        }
        return null;
    }

    @Override
    public synchronized void audit(String tool, JsonObject arguments, String outcome) {
        JsonObject shown = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : arguments.entrySet()) {
            JsonElement value = entry.getValue();
            String text = value.isJsonPrimitive() ? value.getAsString() : value.toString();
            shown.add(entry.getKey(), text.length() > AUDIT_VALUE_LIMIT
                    ? new JsonPrimitive(text.substring(0, AUDIT_VALUE_LIMIT) + "...(" + text.length() + " chars)") : value);
        }
        String line = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + tool + " " + shown + " -> "
                + outcome.replace('\n', ' ') + "\n";
        try {
            Files.createDirectories(directory().toPath());
            Files.write(new File(directory(), "audit.log").toPath(), line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            LoggerUtil.warn("Cannot write the MCP audit log: " + e.getMessage());
        }
    }
}
