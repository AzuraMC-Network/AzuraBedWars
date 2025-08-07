package cc.azuramc.bedwars.game.spectator.gui;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 旁观者指南针GUI
 * 允许旁观者选择要观察的玩家并传送到其位置
 *
 * @author an5w1r@163.com
 */
public class SpectatorCompassGUI extends CustomGUI {

    private static final MessageConfig.Spectator spectatorConfig = AzuraBedWars.getInstance().getMessageConfig().getSpectator();

    private static final int SMALL_INVENTORY_SIZE = 27;
    private static final int LARGE_INVENTORY_SIZE = 54;

    /**
     * 构造函数
     *
     * @param gamePlayer 打开GUI的玩家
     */
    public SpectatorCompassGUI(GamePlayer gamePlayer) {
        super(gamePlayer, spectatorConfig.getSpectatorCompassGuiTitle(), calculateInventorySize());
        initializeItems();
    }

    /**
     * 计算库存大小
     *
     * @return 适当的库存大小
     */
    private static int calculateInventorySize() {
        return GamePlayer.getOnlinePlayers().size() <= SMALL_INVENTORY_SIZE ?
                SMALL_INVENTORY_SIZE : LARGE_INVENTORY_SIZE;
    }

    /**
     * 初始化GUI物品
     */
    private void initializeItems() {
        int slot = 0;
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            if (!isValidTarget(gamePlayer)) {
                continue;
            }

            setItem(slot, createPlayerItem(gamePlayer), createTeleportAction(gamePlayer));
            slot++;
        }
    }

    /**
     * 检查玩家是否可以作为观察目标
     *
     * @param gamePlayer 要检查的玩家
     * @return 是否有效
     */
    private boolean isValidTarget(GamePlayer gamePlayer) {
        return gamePlayer != null && gamePlayer.isOnline() && !gamePlayer.isSpectator();
    }

    /**
     * 创建玩家物品
     *
     * @param gamePlayer 目标玩家
     * @return 物品栈
     */
    private ItemStack createPlayerItem(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        Player targetPlayer = gamePlayer.getPlayer();

        return new ItemBuilder()
                .setOwner(gamePlayer.getName())
                .setDisplayName(gameTeam.getName() + " " + gamePlayer.getNickName())
                .setLores(
                        " ",
                        String.format(spectatorConfig.getHealthFormat(), (int) targetPlayer.getHealth()),
                        String.format(spectatorConfig.getFoodFormat(), targetPlayer.getFoodLevel()),
                        String.format(spectatorConfig.getLevelFormat(), targetPlayer.getLevel()),
                        String.format(spectatorConfig.getDistanceFormat(), (int) targetPlayer.getLocation().distance(gamePlayer.getPlayer().getLocation()))
                )
                .getItem();
    }

    /**
     * 创建传送动作
     *
     * @param gamePlayer 目标玩家
     * @return GUI动作
     */
    private GUIAction createTeleportAction(GamePlayer gamePlayer) {
        return new GUIAction(0, () -> {
            if (gamePlayer != null && gamePlayer.isOnline()) {
                gamePlayer.getPlayer().teleport(gamePlayer.getPlayer());
            }
        }, true);
    }
}
