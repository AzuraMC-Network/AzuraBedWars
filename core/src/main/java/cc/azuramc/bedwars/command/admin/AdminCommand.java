package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.WorldUtil;
import cc.azuramc.bedwars.config.ApplyResult;
import cc.azuramc.bedwars.config.ConfigDocument;
import cc.azuramc.bedwars.config.ConfigProblem;
import cc.azuramc.bedwars.config.ConfigService;
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
import java.util.Arrays;
import java.util.Collections;
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
        List<String> helpMessages = MessageUtil.color(Arrays.asList(
                MessageUtil.CHAT_BAR,
                "&b&lAzuraBedWars &8- &7v" + plugin.getPlugin().getDescription().getVersion() + " &8- &b起床战争 - 指令帮助",
                "",
                "&7 • &f/bw reload [配置名] &7校验并重新加载配置文件",
                "&7 • &f/bw config check &7检查所有配置文件的错误",
                "&7 • &f/bw config schema &7重新生成编辑器补全用的 JSON Schema",
                "&7 • &f/map &7查看地图相关指令帮助 &3&o[仅EditMode]",
                "&7 • &f/setup &7查看快速配置相关指令 &3&o[仅EditMode]",
                "&7 • &f/bw editorMode true/false &7开关编辑模式(重启后生效)",
                "&7 • &f/bw debugMode true/false &7开关调试日志",
                "&7 • &f/bw devtools on/off/status/restart/rotate &7开发服的 AI 配置助手(MCP)",
                "&7 • &f/bw toWorld <worldName> &7前往世界",
                "&7 • &f/bw loadWorld <worldName> &7加载世界",
                "&7 • &f/bw start &7立即开始游戏 &3&o[仅非EditMode]",
                MessageUtil.CHAT_BAR
        ));

        CommandUtil.sendLayout(actor, helpMessages);
    }

    @Subcommand("reload")
    @AutoComplete("@configDocuments")
    public void reloadConfigFiles(BukkitCommandActor actor, @Optional String document) {
        ConfigService configService = plugin.getConfigService();
        if (document != null && configService.document(document) == null) {
            CommandUtil.sendLayout(actor, "&c没有名为 " + document + " 的配置，可选: " + documentIds());
            return;
        }
        List<ApplyResult> results = document == null
                ? configService.reloadAll()
                : Collections.singletonList(configService.reload(document));
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
        lines.add(problems.isEmpty() ? "&a配置文件没有发现问题" : "&e配置文件有 " + problems.size() + " 个问题:");
        addProblems(lines, problems);
        CommandUtil.sendLayout(actor, lines);
    }

    @Subcommand("config schema")
    public void exportSchema(BukkitCommandActor actor) {
        plugin.getConfigService().exportSchemas();
        CommandUtil.sendLayout(actor, "&a已重新生成 " + ConfigService.SCHEMA_DIR + "/ 下的 JSON Schema");
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
        startDevtools(actor, plugin.getConfigService().reload(Devtools.DOCUMENT_ID));
    }

    @Subcommand("devtools off")
    public void devtoolsOff(BukkitCommandActor actor) {
        plugin.getDevtools().stop();
        plugin.getConfigService().setValue(Devtools.DOCUMENT_ID, "enabled", false);
        CommandUtil.sendLayout(actor, "&aMCP 已关闭 端口已释放");
    }

    @Subcommand("devtools rotate")
    public void devtoolsRotate(BukkitCommandActor actor) {
        try {
            String error = plugin.getDevtools().rotateToken();
            if (error != null) {
                CommandUtil.sendLayout(actor, "&c令牌已更换 但 MCP 未能重新启动: " + error);
                return;
            }
        } catch (IOException e) {
            CommandUtil.sendLayout(actor, "&c无法写入新令牌: " + e.getMessage());
            return;
        }
        CommandUtil.sendLayout(actor, "&a令牌已更换 旧令牌立即失效 AI 客户端需要用新命令重新添加");
        devtoolsStatus(actor);
    }

    @Subcommand("devtools status")
    public void devtoolsStatus(BukkitCommandActor actor) {
        Devtools devtools = plugin.getDevtools();
        if (!devtools.isRunning()) {
            CommandUtil.sendLayout(actor, "&eMCP 未开启 /bw devtools on 开启");
            return;
        }
        CommandUtil.sendLayout(actor, Arrays.asList(
                "&aMCP 运行中 &f" + devtools.endpoint(),
                "&7在 AI 客户端所在的电脑上执行:",
                "&f" + devtools.connectCommand(),
                "&7令牌保存在 plugins/AzuraBedWars/devtools/token 审计日志在 devtools/audit.log"
        ));
    }

    private void startDevtools(BukkitCommandActor actor, ApplyResult configResult) {
        if (configResult.getStatus() == ApplyResult.Status.REJECTED) {
            report(actor, configResult);
            return;
        }
        String error = plugin.getDevtools().start();
        if (error != null) {
            CommandUtil.sendLayout(actor, "&cMCP 未能启动: " + error);
            return;
        }
        devtoolsStatus(actor);
    }

    private static String describe(ApplyResult result) {
        String name = "&f" + result.getDocumentId() + " ";
        switch (result.getStatus()) {
            case APPLIED:
                return name + "&a已生效";
            case NEXT_GAME:
                return name + "&e已保存，下一局开始时生效";
            case RESTART_REQUIRED:
                return name + "&e已生效，以下项重启后生效: &f" + String.join(", ", result.getRestartPaths());
            default:
                return name + "&c有错误，未应用，仍使用原配置";
        }
    }

    private static void addProblems(List<String> lines, List<ConfigProblem> problems) {
        int shown = Math.min(problems.size(), 10);
        for (int i = 0; i < shown; i++) {
            ConfigProblem problem = problems.get(i);
            lines.add((problem.isError() ? "&c ✖ " : "&e ! ") + "&7" + problem);
        }
        if (problems.size() > shown) {
            lines.add("&7 ……还有 " + (problems.size() - shown) + " 个，完整列表见控制台");
            AzuraBedWars.logProblems(problems);
        }
    }

    private String documentIds() {
        List<String> ids = new ArrayList<>();
        for (ConfigDocument<?> document : plugin.getConfigService().documents()) {
            ids.add(document.getId());
        }
        return String.join(", ", ids);
    }

    @Subcommand("toWorld")
    public void toWorld(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(MessageUtil.color("&c世界 " + worldName + " 未加载 先用 /bw loadWorld " + worldName));
            return;
        }
        player.teleport(world.getSpawnLocation());
    }

    @Subcommand("loadWorld")
    public void loadWorld(BukkitCommandActor actor, String worldName) {
        if (!new File(Bukkit.getWorldContainer(), worldName + "/level.dat").isFile()) {
            CommandUtil.sendLayout(actor, "&c服务器目录下没有世界存档 " + worldName);
            return;
        }
        World world = Bukkit.createWorld(new WorldCreator(worldName).environment(World.Environment.NORMAL));
        if (world == null) {
            CommandUtil.sendLayout(actor, "&c无法加载世界 " + worldName);
            return;
        }
        WorldUtil.setWorldRules(world);
        CommandUtil.sendLayout(actor, "&a世界 " + worldName + " 已加载 /bw toWorld " + worldName + " 前往");
    }
}
