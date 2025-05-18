package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.util.ChatColorUtil;
import cc.azuramc.bedwars.util.SetupItemManager;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

/**
 * 地图设置命令
 * @author an5w1r@163.com
 */
@Command("setup")
@CommandPermission("azurabedwars.admin")
public class SetupCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    private final AzuraBedWars plugin;
    private final SetupItemManager setupItemManager;

    public SetupCommand(AzuraBedWars plugin) {
        this.plugin = plugin;
        this.setupItemManager = plugin.getSetupItemManager();
    }

    /**
     * 检查是否处于编辑模式，如果不是则向用户发送消息并返回true
     */
    private boolean checkEditorMode(Player player) {
        if (!plugin.getSettingsConfig().isEditorMode()) {
            player.sendMessage(ChatColorUtil.color("&c错误: 该命令只能在编辑模式下使用"));
            player.sendMessage(ChatColorUtil.color("&c请在配置文件中设置 editorMode: true 并重启服务器"));
            return true;
        }
        return false;
    }

    @DefaultFor("setup")
    public void getSetupCommand(Player player) {
        if (checkEditorMode(player)) {
            return;
        }

        player.sendMessage(ChatColorUtil.CHAT_BAR);
        player.sendMessage(ChatColorUtil.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 设置工具"));
        player.sendMessage("");
        player.sendMessage(ChatColorUtil.color("&7 • &f/setup start <mapName> &7开始配置地图"));
        player.sendMessage(ChatColorUtil.color("&7 • &f/setup stop <mapName> &7结束配置地图"));
        player.sendMessage("");
        player.sendMessage(ChatColorUtil.CHAT_BAR);
    }

    @Subcommand("start")
    public void startSetup(Player player, String mapName) {
        if (checkEditorMode(player)) {
            return;
        }
        AzuraBedWars.getInstance().getMapManager().getAndLoadMapData(mapName);

        setupItemManager.giveSetupItems(player, mapName);
        setupItemManager.setPlayerMapContext(player.getName(), mapName);
        player.sendMessage(ChatColorUtil.color("&a已进入地图「" + mapName + "」的设置模式！"));
    }


    @Subcommand("stop")
    public void stopSetup(Player player) {
        if (checkEditorMode(player)) {
            return;
        }

        setupItemManager.removePlayerMapContext(player.getName());
        player.getInventory().clear();
        player.sendMessage(ChatColorUtil.color("&a已退出地图设置模式！"));
    }
} 