package cc.azuramc.bedwars.devtools;

import cc.azuramc.bedwars.util.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MCP over Streamable HTTP, answering every request with a single JSON body and never opening an SSE stream
 *
 * @author an5w1r@163.com
 */
public final class McpServer {

    /** What the endpoint needs from the plugin */
    public interface Host {
        /** Throws {@link ToolError} when writes are not allowed right now */
        void checkWriteAllowed() throws Exception;

        void audit(String tool, JsonObject arguments, String outcome);
    }

    public static final String PATH = "/mcp";
    /** Newest first, the first one is offered to clients asking for an unknown version */
    private static final List<String> PROTOCOL_VERSIONS = Arrays.asList("2025-11-25", "2025-06-18", "2025-03-26", "2024-11-05");
    private static final int MAX_BODY = 4 * 1024 * 1024;

    private static final int PARSE_ERROR = -32700;
    private static final int INVALID_REQUEST = -32600;
    private static final int METHOD_NOT_FOUND = -32601;
    private static final int INVALID_PARAMS = -32602;
    private static final int INTERNAL_ERROR = -32603;
    private static final int RESOURCE_NOT_FOUND = -32002;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
    private final Host host;
    private final McpContent content;
    private final Map<String, McpTool> tools = new LinkedHashMap<>();
    private final byte[] expectedAuthorization;
    private final String version;
    private HttpServer server;
    private ExecutorService executor;
    @Getter
    private InetSocketAddress address;

    public McpServer(Host host, McpContent content, List<McpTool> tools, String token, String version) {
        this.host = host;
        this.content = content;
        for (McpTool tool : tools) {
            this.tools.put(tool.getName(), tool);
        }
        this.expectedAuthorization = ("Bearer " + token).getBytes(StandardCharsets.UTF_8);
        this.version = version;
    }

    public void start(String hostName, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(hostName, port), 0);
        server.createContext(PATH, this::handle);
        AtomicInteger threads = new AtomicInteger();
        executor = Executors.newFixedThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable, "AzuraBedWars-MCP-" + threads.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        server.setExecutor(executor);
        server.start();
        address = server.getAddress();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            executor.shutdownNow();
            server = null;
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            if (!isAllowedOrigin(exchange.getRequestHeaders().getFirst("Origin"))) {
                respond(exchange, 403, null);
                return;
            }
            String authorization = exchange.getRequestHeaders().getFirst("Authorization");
            if (authorization == null || !MessageDigest.isEqual(expectedAuthorization, authorization.getBytes(StandardCharsets.UTF_8))) {
                exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
                respond(exchange, 401, error(JsonNull.INSTANCE, INVALID_REQUEST, "Missing or wrong bearer token, see plugins/AzuraBedWars/devtools/token"));
                return;
            }
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Allow", "POST");
                respond(exchange, 405, null);
                return;
            }
            JsonElement body;
            try {
                body = parse(readBody(exchange.getRequestBody()));
            } catch (JsonParseException | IOException e) {
                respond(exchange, 400, error(JsonNull.INSTANCE, PARSE_ERROR, "Body is not JSON: " + e.getMessage()));
                return;
            }
            JsonElement response;
            if (body.isJsonArray()) {
                JsonArray responses = new JsonArray();
                for (JsonElement message : body.getAsJsonArray()) {
                    JsonObject reply = handleMessage(message, exchange);
                    if (reply != null) {
                        responses.add(reply);
                    }
                }
                response = responses.size() == 0 ? null : responses;
            } else {
                response = handleMessage(body, exchange);
            }
            respond(exchange, response == null ? 202 : 200, response);
        } catch (Exception e) {
            LoggerUtil.error("MCP request failed", e);
            respond(exchange, 500, error(JsonNull.INSTANCE, INTERNAL_ERROR, e.toString()));
        } finally {
            exchange.close();
        }
    }

    /**
     * @return null for notifications and responses, which get no reply
     */
    private JsonObject handleMessage(JsonElement element, HttpExchange exchange) {
        if (!element.isJsonObject()) {
            return error(JsonNull.INSTANCE, INVALID_REQUEST, "A JSON-RPC message must be an object");
        }
        JsonObject message = element.getAsJsonObject();
        JsonElement id = message.get("id");
        if (!message.has("method") || id == null || id.isJsonNull()) {
            return null;
        }
        String method = message.get("method").getAsString();
        JsonObject params = message.has("params") && message.get("params").isJsonObject()
                ? message.getAsJsonObject("params") : new JsonObject();
        try {
            if ("initialize".equals(method)) {
                exchange.getResponseHeaders().set("Mcp-Session-Id", UUID.randomUUID().toString());
            }
            return success(id, dispatch(method, params));
        } catch (RpcError e) {
            return error(id, e.code, e.getMessage());
        } catch (Exception e) {
            LoggerUtil.error("MCP " + method + " failed", e);
            return error(id, INTERNAL_ERROR, e.toString());
        }
    }

    private JsonElement dispatch(String method, JsonObject params) throws Exception {
        switch (method) {
            case "initialize":
                return initialize(params);
            case "ping":
                return new JsonObject();
            case "tools/list":
                JsonArray list = new JsonArray();
                for (McpTool tool : tools.values()) {
                    list.add(tool.describe());
                }
                return wrap("tools", list);
            case "tools/call":
                return callTool(params);
            case "resources/list":
                return wrap("resources", content.resources());
            case "resources/templates/list":
                return wrap("resourceTemplates", content.resourceTemplates());
            case "resources/read":
                return readResource(params);
            case "prompts/list":
                return wrap("prompts", content.prompts());
            case "prompts/get":
                return getPrompt(params);
            default:
                throw new RpcError(METHOD_NOT_FOUND, "Unknown method " + method);
        }
    }

    private JsonObject initialize(JsonObject params) {
        String requested = params.has("protocolVersion") ? params.get("protocolVersion").getAsString() : "";
        JsonObject result = new JsonObject();
        result.addProperty("protocolVersion", PROTOCOL_VERSIONS.contains(requested) ? requested : PROTOCOL_VERSIONS.get(0));
        JsonObject capabilities = new JsonObject();
        capabilities.add("tools", new JsonObject());
        capabilities.add("resources", new JsonObject());
        capabilities.add("prompts", new JsonObject());
        result.add("capabilities", capabilities);
        JsonObject serverInfo = new JsonObject();
        serverInfo.addProperty("name", "AzuraBedWars");
        serverInfo.addProperty("version", version);
        result.add("serverInfo", serverInfo);
        result.addProperty("instructions", content.instructions());
        return result;
    }

    private JsonObject callTool(JsonObject params) {
        String name = params.has("name") ? params.get("name").getAsString() : "";
        McpTool tool = tools.get(name);
        if (tool == null) {
            throw new RpcError(INVALID_PARAMS, "Unknown tool " + name);
        }
        JsonObject arguments = params.has("arguments") && params.get("arguments").isJsonObject()
                ? params.getAsJsonObject("arguments") : new JsonObject();
        boolean failed = false;
        String text;
        try {
            if (!tool.isReadOnly()) {
                host.checkWriteAllowed();
            }
            JsonElement result = tool.getHandler().call(new Args(arguments));
            text = result.isJsonPrimitive() ? result.getAsString() : gson.toJson(result);
        } catch (ToolError e) {
            failed = true;
            text = e.getMessage();
        } catch (Exception e) {
            failed = true;
            text = "Internal error: " + e;
            LoggerUtil.error("MCP tool " + name + " failed", e);
        }
        host.audit(name, arguments, failed ? "error: " + text : "ok");

        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", text);
        JsonArray contents = new JsonArray();
        contents.add(textContent);
        JsonObject result = wrap("content", contents);
        result.addProperty("isError", failed);
        return result;
    }

    private JsonObject readResource(JsonObject params) throws Exception {
        String uri = params.has("uri") ? params.get("uri").getAsString() : "";
        String text;
        try {
            text = content.read(uri);
        } catch (ToolError e) {
            throw new RpcError(RESOURCE_NOT_FOUND, e.getMessage());
        }
        if (text == null) {
            throw new RpcError(RESOURCE_NOT_FOUND, "Unknown resource " + uri);
        }
        JsonObject resource = new JsonObject();
        resource.addProperty("uri", uri);
        resource.addProperty("mimeType", McpContent.mimeType(uri));
        resource.addProperty("text", text);
        JsonArray contents = new JsonArray();
        contents.add(resource);
        return wrap("contents", contents);
    }

    private JsonObject getPrompt(JsonObject params) {
        String name = params.has("name") ? params.get("name").getAsString() : "";
        JsonObject arguments = params.has("arguments") && params.get("arguments").isJsonObject()
                ? params.getAsJsonObject("arguments") : new JsonObject();
        JsonObject prompt = content.prompt(name, arguments);
        if (prompt == null) {
            throw new RpcError(INVALID_PARAMS, "Unknown prompt " + name);
        }
        return prompt;
    }

    /** Browsers send Origin, local CLI clients usually do not */
    static boolean isAllowedOrigin(String origin) {
        if (origin == null || origin.isEmpty()) {
            return true;
        }
        try {
            String host = new URI(origin).getHost();
            return host != null && ("localhost".equals(host) || "127.0.0.1".equals(host) || "[::1]".equals(host) || "::1".equals(host));
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private static JsonElement parse(String text) {
        // Gson on 1.8 servers predates JsonParser.parseString
        return new JsonParser().parse(text);
    }

    private static String readBody(InputStream input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            bytes.write(buffer, 0, read);
            if (bytes.size() > MAX_BODY) {
                throw new IOException("Body is larger than " + MAX_BODY + " bytes");
            }
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

    private void respond(HttpExchange exchange, int status, JsonElement body) throws IOException {
        if (body == null) {
            exchange.sendResponseHeaders(status, -1);
            return;
        }
        byte[] bytes = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static JsonObject wrap(String key, JsonElement value) {
        JsonObject object = new JsonObject();
        object.add(key, value);
        return object;
    }

    private static JsonObject success(JsonElement id, JsonElement result) {
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", id);
        response.add("result", result);
        return response;
    }

    private static JsonObject error(JsonElement id, int code, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", id);
        response.add("error", error);
        return response;
    }

    private static final class RpcError extends RuntimeException {
        private final int code;

        RpcError(int code, String message) {
            super(message);
            this.code = code;
        }
    }
}
