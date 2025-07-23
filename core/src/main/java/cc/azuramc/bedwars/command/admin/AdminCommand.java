package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.WorldUtil;
import cc.azuramc.bedwars.util.CommandUtil;
import cc.azuramc.bedwars.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
@Command({"bedwars", "bw", "azurabedwars"})
@CommandPermission("azurabedwars.admin")
public class AdminCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();

    @DefaultFor("bw")
    public void getHelpCommand(BukkitCommandActor actor) {
        CommandUtil.sendLayout(actor, MessageUtil.CHAT_BAR);
        CommandUtil.sendLayout(actor, MessageUtil.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 指令帮助"));
        CommandUtil.sendLayout(actor, "");
        CommandUtil.sendLayout(actor, MessageUtil.color("&7 • &f/bw reload &7重新加载配置文件"));
        CommandUtil.sendLayout(actor, MessageUtil.color("&7 • &f/map &7查看地图相关指令帮助 &3&o[仅EditMode]"));
        CommandUtil.sendLayout(actor, MessageUtil.color("&7 • &f/setup &7查看快速配置相关指令 &3&o[仅EditMode]"));
        CommandUtil.sendLayout(actor, MessageUtil.color("&7 • &f/bw editorMode true/false &7开关编辑模式(重启后生效)"));
        CommandUtil.sendLayout(actor, MessageUtil.color("&7 • &f/bw toWorld <worldName> &7前往世界"));
        CommandUtil.sendLayout(actor, MessageUtil.color("&7 • &f/bw loadWorld <worldName> &7加载世界"));
        CommandUtil.sendLayout(actor, MessageUtil.color("&7 • &f/bw start &7立即开始游戏 &3&o[仅非EditMode]"));
        CommandUtil.sendLayout(actor, MessageUtil.CHAT_BAR);
    }

    @Subcommand("reload")
    public void reloadConfigFiles(BukkitCommandActor actor) {
        plugin.getConfigManager().reloadAll();
        CommandUtil.sendLayout(actor, "&a成功重载配置文件");
    }

    @Subcommand("editorMode")
    public void editorMode(BukkitCommandActor actor, boolean value) {
        if (plugin.getSettingsConfig().isEditorMode() == value) {
            CommandUtil.sendLayout(actor, "&ceditorMode值已经是 " + value);
            return;
        }
        plugin.getSettingsConfig().setEditorMode(value);
        CommandUtil.sendLayout(actor, "&a成功设置值为 " + value);
        CommandUtil.sendLayout(actor, "&a&l重启后&a令此设置生效.");
        plugin.getConfigManager().saveConfig("settings");
    }

    @Subcommand("debugMode")
    public void debugMode(BukkitCommandActor actor, boolean value) {
        if (plugin.getSettingsConfig().isEditorMode() == value) {
            CommandUtil.sendLayout(actor, "&cdebugMode值已经是 " + value);
            return;
        }
        plugin.getSettingsConfig().setDebugMode(value);
        CommandUtil.sendLayout(actor, "&a成功设置值为 " + value);
        plugin.getConfigManager().saveConfig("settings");
    }


    @Subcommand("toWorld")
    public void toWorld(Player player, String worldName) {
        player.teleport(Objects.requireNonNull(Bukkit.getWorld(worldName)).getSpawnLocation());
    }

    @Subcommand("loadWorld")
    public void loadWorld(BukkitCommandActor actor, String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        World mapWorld = Bukkit.createWorld(creator);

        if (mapWorld != null) {
            WorldUtil.setWorldRules(mapWorld);
        }
    }
}
