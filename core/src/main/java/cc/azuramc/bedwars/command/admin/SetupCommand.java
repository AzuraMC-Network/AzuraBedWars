package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.listener.setup.SetupItemListener;
import cc.azuramc.bedwars.util.MessageUtil;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Arrays;

/**
 * @author an5w1r@163.com
 */
@Command("setup")
@CommandPermission("azurabedwars.admin")
public class SetupCommand {

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();
    private final SetupItemListener setup;

    public SetupCommand(SetupItemListener setup) {
        this.setup = setup;
    }

    @DefaultFor("setup")
    public void help(Player player) {
        MessageUtil.color(Arrays.asList(
                MessageUtil.CHAT_BAR,
                "&b&lAzuraBedWars &8- &7v" + plugin.getPlugin().getDescription().getVersion() + " &8- &b起床战争 - 设置工具",
                "",
                "&7 • &f/setup start <地图> &7领取设置工具 右键在脚下设置",
                "&7 • &f/setup stop &7收回设置工具",
                MessageUtil.CHAT_BAR
        )).forEach(player::sendMessage);
    }

    @Subcommand("start")
    @AutoComplete("@mapNames")
    public void start(Player player, String mapName) {
        if (plugin.getMapManager().getMap(mapName) == null) {
            player.sendMessage(MessageUtil.color("&c找不到地图 " + mapName + " 先用 /map create " + mapName + " 创建"));
            return;
        }
        setup.start(player, mapName);
        player.sendMessage(MessageUtil.color("&a正在设置地图 " + mapName + " 右键工具在脚下设置 每次修改自动保存"));
    }

    @Subcommand("stop")
    public void stop(Player player) {
        player.sendMessage(MessageUtil.color(setup.stop(player) ? "&a已退出地图设置" : "&c你没有在设置地图"));
    }
}
