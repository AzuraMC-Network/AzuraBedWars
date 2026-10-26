package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.WorldUtil;
import cc.azuramc.bedwars.config.ApplyResult;
import cc.azuramc.bedwars.config.ConfigProblem;
import cc.azuramc.bedwars.config.ConfigService;
import cc.azuramc.bedwars.config.object.LangConfig;
import cc.azuramc.bedwars.devtools.Devtools;
import cc.azuramc.bedwars.util.CommandUtil;
import cc.azuramc.bedwars.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Command({"azurabedwars", "bedwars", "abw", "bw"})
@CommandPermission("azurabedwars.admin")
public class AdminCommand {

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();

    @DefaultFor({"azurabedwars", "bedwars", "abw", "bw"})
    public void getHelpCommand(BukkitCommandActor actor) {
        List<String> helpMessages = new ArrayList<>();
        helpMessages.add(MessageUtil.CHAT_BAR);
        for (String line : lang().getHelp()) {
            helpMessages.add(MessageUtil.fill(line, "version", plugin.getPlugin().getDescription().getVersion()));
        }
        helpMessages.add(MessageUtil.CHAT_BAR);

        CommandUtil.sendLayout(actor, helpMessages);
    }

    @Subcommand("reload")
    @AutoComplete("@configDocuments")
    public void reloadConfigFiles(BukkitCommandActor actor, @Optional String document) {
        ConfigService configService = plugin.getConfigService();
        if (document != null && !configService.isReloadable(document)) {
            CommandUtil.sendLayout(actor, MessageUtil.fill(lang().getUnknownConfig(), "config", document, "available", documentIds()));
            return;
        }
        List<ApplyResult> results = document == null
                ? configService.reloadAll()
                : configService.reload(document);
        List<String> lines = new ArrayList<>();
        for (ApplyResult result : results) {
            lines.add(describe(result));
            addProblems(lines, result.getProblems());
        }
        CommandUtil.sendLayout(actor, lines);
    }

    @Subcommand("config check")
    public void checkConfig(BukkitCommandActor actor) {
        List<ConfigProblem> problems = plugin.getConfigService().check();
        List<String> lines = new ArrayList<>();
        lines.add(problems.isEmpty() ? lang().getConfigClean() : MessageUtil.fill(lang().getConfigProblems(), "count", problems.size()));
        addProblems(lines, problems);
        CommandUtil.sendLayout(actor, lines);
    }

    @Subcommand("config schema")
    public void exportSchema(BukkitCommandActor actor) {
        plugin.getConfigService().exportSchemas();
        CommandUtil.sendLayout(actor, MessageUtil.fill(lang().getSchemaWritten(), "folder", ConfigService.SCHEMA_DIR));
    }

    @Subcommand("editorMode")
    @AutoComplete("@booleanValues")
    public void editorMode(BukkitCommandActor actor, boolean value) {
        setSetting(actor, "editor-mode", value);
    }

    @Subcommand("debugMode")
    @AutoComplete("@booleanValues")
    public void debugMode(BukkitCommandActor actor, boolean value) {
        setSetting(actor, "debug-mode", value);
    }

    private void setSetting(BukkitCommandActor actor, String key, boolean value) {
        report(actor, plugin.getConfigService().setValue("settings", key, value));
    }

    private static void report(BukkitCommandActor actor, ApplyResult result) {
        List<String> lines = new ArrayList<>();
        lines.add(describe(result));
        addProblems(lines, result.getProblems());
        CommandUtil.sendLayout(actor, lines);
    }

    @Subcommand("devtools on")
    public void devtoolsOn(BukkitCommandActor actor) {
        startDevtools(actor, plugin.getConfigService().setValue(Devtools.DOCUMENT_ID, "enabled", true));
    }

    @Subcommand("devtools restart")
    public void devtoolsRestart(BukkitCommandActor actor) {
        startDevtools(actor, plugin.getConfigService().reload(Devtools.DOCUMENT_ID).get(0));
    }

    @Subcommand("devtools off")
    public void devtoolsOff(BukkitCommandActor actor) {
        plugin.getDevtools().stop();
        ApplyResult result = plugin.getConfigService().setValue(Devtools.DOCUMENT_ID, "enabled", false);
        if (result.getStatus() == ApplyResult.Status.REJECTED) {
            List<String> lines = new ArrayList<>();
            lines.add(lang().getDevtoolsOffUnsaved());
            addProblems(lines, result.getProblems());
            CommandUtil.sendLayout(actor, lines);
            return;
        }
        CommandUtil.sendLayout(actor, lang().getDevtoolsOff());
    }

    @Subcommand("devtools rotate")
    public void devtoolsRotate(BukkitCommandActor actor) {
        try {
            String error = plugin.getDevtools().rotateToken();
            if (error != null) {
                CommandUtil.sendLayout(actor, MessageUtil.fill(lang().getDevtoolsRotateRestartFailed(), "error", error));
                return;
            }
        } catch (IOException e) {
            CommandUtil.sendLayout(actor, MessageUtil.fill(lang().getDevtoolsTokenWriteFailed(), "error", e.getMessage()));
            return;
        }
        CommandUtil.sendLayout(actor, lang().getDevtoolsRotated());
        devtoolsStatus(actor);
    }

    @Subcommand("devtools status")
    public void devtoolsStatus(BukkitCommandActor actor) {
        Devtools devtools = plugin.getDevtools();
        if (!devtools.isRunning()) {
            CommandUtil.sendLayout(actor, lang().getDevtoolsNotRunning());
            return;
        }
        List<String> lines = new ArrayList<>();
        for (String line : lang().getDevtoolsRunning()) {
            lines.add(MessageUtil.fill(line, "endpoint", devtools.endpoint(), "command", devtools.connectCommand()));
        }
        CommandUtil.sendLayout(actor, lines);
    }

    private void startDevtools(BukkitCommandActor actor, ApplyResult configResult) {
        if (configResult.getStatus() == ApplyResult.Status.REJECTED) {
            report(actor, configResult);
            return;
        }
        String error = plugin.getDevtools().start();
        if (error != null) {
            CommandUtil.sendLayout(actor, MessageUtil.fill(lang().getDevtoolsStartFailed(), "error", error));
            return;
        }
        devtoolsStatus(actor);
    }

    private static String describe(ApplyResult result) {
        String name = result.getDocumentId();
        switch (result.getStatus()) {
            case APPLIED:
                return MessageUtil.fill(lang().getApplied(), "config", name);
            case NEXT_GAME:
                return MessageUtil.fill(lang().getAppliedNextGame(), "config", name);
            case RESTART_REQUIRED:
                return MessageUtil.fill(lang().getAppliedRestart(), "config", name, "paths", String.join(", ", result.getRestartPaths()));
            default:
                return MessageUtil.fill(lang().getRejected(), "config", name);
        }
    }

    private static void addProblems(List<String> lines, List<ConfigProblem> problems) {
        int shown = Math.min(problems.size(), 10);
        for (int i = 0; i < shown; i++) {
            ConfigProblem problem = problems.get(i);
            lines.add((problem.isError() ? "&c ✖ " : "&e ! ") + "&7" + problem);
        }
        if (problems.size() > shown) {
            lines.add(MessageUtil.fill(lang().getMoreProblems(), "count", problems.size() - shown));
            AzuraBedWars.logProblems(problems);
        }
    }

    private static LangConfig.Admin lang() {
        return AzuraBedWars.getInstance().getLang().getAdmin();
    }

    private String documentIds() {
        return String.join(", ", plugin.getConfigService().reloadableIds());
    }

    @Subcommand("toWorld")
    public void toWorld(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(MessageUtil.fill(lang().getWorldNotLoaded(), "world", worldName));
            return;
        }
        player.teleport(world.getSpawnLocation());
    }

    @Subcommand("loadWorld")
    public void loadWorld(BukkitCommandActor actor, String worldName) {
        if (!WorldUtil.isWorldSave(new File(Bukkit.getWorldContainer(), worldName)) && !WorldUtil.isWorldSave(WorldUtil.getDimensionFolder(worldName))) {
            CommandUtil.sendLayout(actor, MessageUtil.fill(lang().getWorldMissing(), "world", worldName));
            return;
        }
        World world = Bukkit.createWorld(new WorldCreator(worldName).environment(World.Environment.NORMAL));
        if (world == null) {
            CommandUtil.sendLayout(actor, MessageUtil.fill(lang().getWorldLoadFailed(), "world", worldName));
            return;
        }
        WorldUtil.setWorldRules(world);
        CommandUtil.sendLayout(actor, MessageUtil.fill(lang().getWorldLoaded(), "world", worldName));
    }
}
