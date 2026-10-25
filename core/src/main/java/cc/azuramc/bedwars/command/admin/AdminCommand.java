package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.WorldUtil;
import cc.azuramc.bedwars.util.CommandUtil;
import cc.azuramc.bedwars.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.File;
import java.util.Arrays;
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
                "&7 • &f/bw reload &7重新加载配置文件",
                "&7 • &f/map &7查看地图相关指令帮助 &3&o[仅EditMode]",
                "&7 • &f/setup &7查看快速配置相关指令 &3&o[仅EditMode]",
                "&7 • &f/bw editorMode true/false &7开关编辑模式(重启后生效)",
                "&7 • &f/bw toWorld <worldName> &7前往世界",
                "&7 • &f/bw loadWorld <worldName> &7加载世界",
                "&7 • &f/bw start &7立即开始游戏 &3&o[仅非EditMode]",
                MessageUtil.CHAT_BAR
        ));

        CommandUtil.sendLayout(actor, helpMessages);
    }

    @Subcommand("reload")
    public void reloadConfigFiles(BukkitCommandActor actor) {
        plugin.getConfigManager().reloadAll();
        CommandUtil.sendLayout(actor, "&a成功重载配置文件");
    }

    @Subcommand("editorMode")
    @AutoComplete("@booleanValues")
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
    @AutoComplete("@booleanValues")
    public void debugMode(BukkitCommandActor actor, boolean value) {
        if (plugin.getSettingsConfig().isDebugMode() == value) {
            CommandUtil.sendLayout(actor, "&cdebugMode值已经是 " + value);
            return;
        }
        plugin.getSettingsConfig().setDebugMode(value);
        CommandUtil.sendLayout(actor, "&a成功设置值为 " + value);
        plugin.getConfigManager().saveConfig("settings");
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
