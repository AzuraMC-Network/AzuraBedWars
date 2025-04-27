package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.WorldUtil;
import cc.azuramc.bedwars.util.ChatColorUtil;
import cc.azuramc.bedwars.util.CommandUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Objects;

@Command({"bedwars", "bw", "azurabedwars"})
@CommandPermission("azurabedwars.admin")
public class AdminCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();

    @DefaultFor("bw")
    public void getHelpCommand(BukkitCommandActor actor) {
        CommandUtil.sendLayout(actor, ChatColorUtil.CHAT_BAR);
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 指令帮助"));
        CommandUtil.sendLayout(actor, "");
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map &7查看地图相关指令帮助"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/bw editorMode true/false &7开关编辑模式"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/bw toWorld <worldName> &7前往世界"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/bw loadWorld <worldName> &7加载世界"));
        CommandUtil.sendLayout(actor, ChatColorUtil.CHAT_BAR);
    }

    @Subcommand("map")
    public void getMapCommandHelp(BukkitCommandActor actor) {
        CommandUtil.sendLayout(actor, ChatColorUtil.CHAT_BAR);
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 地图设置"));
        CommandUtil.sendLayout(actor, "");
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map create <mapName> &7创建新的地图"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setWaiting <mapName> &7设置等待大厅位置"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setAuthor <mapName> <authorName> &7设置地图作者名"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setTeamPlayers <mapName> <number> &7设置队伍最大人数"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setMinPlayers <mapName> <number> &7设置地图最小需要人数"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setRespawn <mapName> &7设置地图重生点"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map addBase <mapName> &7增加基地出生点"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map addDrop <mapName> <type> &7增加资源点 (类型: BASE/DIAMOND/EMERALD)"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map addShop <mapName> <type> &7增加商店 (类型: ITEM/UPGRADE)"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setPos1 <mapName> &7设置地图边界1"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setPos2 <mapName> &7设置地图边界2"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map setUrl <url> &7设置地图文件物理路径"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map save <mapName> &7保存地图"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map load <mapName> &7加载地图配置"));
        CommandUtil.sendLayout(actor, "");
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map list <type> &7查看指定存储方式地图列表"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map migrate &7迁移所有地图数据存储方式 (类型: JSON/MYSQL)"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map migrate <源类型> <目标类型> [mapName] &7迁移地图存储方式"));
        CommandUtil.sendLayout(actor, ChatColorUtil.color("&7 • &f/map info <mapName> &7查看地图信息"));
        CommandUtil.sendLayout(actor, ChatColorUtil.CHAT_BAR);
    }

    @Subcommand("editorMode")
    public void editorMode(BukkitCommandActor actor, boolean value) {
        if (plugin.getSettingsConfig().isEditorMode() == value) {
            CommandUtil.sendLayout(actor, "&ceditorMode值已经是 " + value + "了");
            return;
        }
        plugin.getSettingsConfig().setEditorMode(value);
        CommandUtil.sendLayout(actor, "成功设置值为 " + value);
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
