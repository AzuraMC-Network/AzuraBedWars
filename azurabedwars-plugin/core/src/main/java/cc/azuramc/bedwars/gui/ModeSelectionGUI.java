package cc.azuramc.bedwars.gui;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;

/**
 * 模式选择GUI
 * 允许玩家选择普通模式或经验模式
 *
 * @author an5w1r@163.com
 */
public class ModeSelectionGUI extends CustomGUI {
    /**
     * 灰色
     */
    private static final int BORDER_GLASS_COLOR = 7;
    /**
     * 绿色
     */
    private static final int HIGHLIGHT_GLASS_COLOR = 5;

    /**
     * 创建模式选择GUI
     *
     * @param gamePlayer 游戏玩家
     */
    public ModeSelectionGUI(GamePlayer gamePlayer) {
        super(gamePlayer, "§8资源类型选择", 27);

        // 设置界面边框
        setupBorders();

        // 设置模式选项
        setupDefaultModeItem(gamePlayer);
        setupExperienceModeItem(gamePlayer);
    }

    /**
     * 设置界面边框
     */
    private void setupBorders() {
        // 设置顶部边框
        for (int i = 0; i < 9; i++) {
            setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
            }, false));
        }

        // 设置左右边框
        setItem(9, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));
        setItem(17, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));

        // 设置底部边框
        for (int i = 18; i < 27; i++) {
            setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
            }, false));
        }

        // 设置选择区域装饰
        setItem(10, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + HIGHLIGHT_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));
        setItem(12, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + HIGHLIGHT_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));
        setItem(14, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + HIGHLIGHT_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));
        setItem(16, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + HIGHLIGHT_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));
    }

    /**
     * 设置普通模式选项
     */
    private void setupDefaultModeItem(GamePlayer gamePlayer) {
        boolean isSelected = gamePlayer.getPlayerData().getMode() == GameModeType.DEFAULT;
        Player player = gamePlayer.getPlayer();

        setItem(11, new ItemBuilder()
                        .setType(XMaterial.RED_BED.get())
                        .setDisplayName("§a普通模式")
                        .setLores(
                                "§7使用资源购买物品的模式",
                                "§7需要收集§f铁锭§7、§6金锭§7、§b钻石§7和§2绿宝石",
                                " ",
                                isSelected ? "§a§l✓ 已选择" : "§7点击选择此模式"
                        )
                        .getItem(),
                new GUIAction(0, () -> {
                    if (isSelected) {
                        player.sendMessage("§a你已经选择了普通模式!");
                        return;
                    }

                    gamePlayer.getPlayerData().setMode(GameModeType.DEFAULT);
                    GamePlayer.get(player.getUniqueId()).setGameModeType(GameModeType.DEFAULT);

                    player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 10F, 1F);
                    AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
                    player.sendMessage("§a已选择普通模式!");
                }, true));
    }

    /**
     * 设置经验模式选项
     */
    private void setupExperienceModeItem(GamePlayer gamePlayer) {
        boolean isSelected = gamePlayer.getPlayerData().getMode() == GameModeType.EXPERIENCE;
        Player player = gamePlayer.getPlayer();

        setItem(15, new ItemBuilder()
                        .setType(XMaterial.EXPERIENCE_BOTTLE.get())
                        .setDisplayName("§a经验模式")
                        .setLores(
                                "§7使用经验等级购买物品的模式",
                                "§7无需收集资源，直接使用§e经验等级§7购买",
                                " ",
                                isSelected ? "§a§l✓ 已选择" : "§7点击选择此模式"
                        )
                        .getItem(),
                new GUIAction(0, () -> {
                    if (isSelected) {
                        player.sendMessage("§a你已经选择了经验模式!");
                        return;
                    }

                    gamePlayer.getPlayerData().setMode(GameModeType.EXPERIENCE);
                    GamePlayer.get(player.getUniqueId()).setGameModeType(GameModeType.EXPERIENCE);

                    player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 10F, 1F);
                    AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
                    player.sendMessage("§a已选择经验模式!");
                }, true));
    }
}
