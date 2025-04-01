package cc.azuramc.bedwars.guis;

import cc.azuramc.bedwars.scoreboards.LobbyBoard;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import cc.azuramc.bedwars.utils.gui.CustomGUI;
import cc.azuramc.bedwars.utils.gui.GUIAction;
import cc.azuramc.bedwars.compat.util.ItemBuilderUtil;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.types.ModeType;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * 模式选择GUI
 * 允许玩家选择普通模式或经验模式
 */
public class ModeSelectionGUI extends CustomGUI {
    
    // 边框装饰物品颜色
    private static final int BORDER_GLASS_COLOR = 7; // 灰色
    private static final int HIGHLIGHT_GLASS_COLOR = 5; // 绿色

    /**
     * 创建模式选择GUI
     * @param player 玩家
     */
    public ModeSelectionGUI(Player player) {
        this(player, null);
    }
    
    /**
     * 创建模式选择GUI
     * @param player 玩家
     * @param game 游戏实例（可为空）
     */
    public ModeSelectionGUI(Player player, Game game) {
        super(player, "§8资源类型选择", 27);
        
        PlayerData playerData = Objects.requireNonNull(GamePlayer.get(player.getUniqueId())).getPlayerData();
        
        // 设置界面边框
        setupBorders();
        
        // 设置模式选项
        setupDefaultModeItem(player, playerData);
        setupExperienceModeItem(player, playerData);
    }
    
    /**
     * 设置界面边框
     */
    private void setupBorders() {
        // 设置顶部边框
        for (int i = 0; i < 9; i++) {
            setItem(i, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        }
        
        // 设置左右边框
        setItem(9, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        setItem(17, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        
        // 设置底部边框
        for (int i = 18; i < 27; i++) {
            setItem(i, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        }
        
        // 设置选择区域装饰
        setItem(10, MaterialUtil.getStainedGlassPane(HIGHLIGHT_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        setItem(12, MaterialUtil.getStainedGlassPane(HIGHLIGHT_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        setItem(14, MaterialUtil.getStainedGlassPane(HIGHLIGHT_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        setItem(16, MaterialUtil.getStainedGlassPane(HIGHLIGHT_GLASS_COLOR), new GUIAction(0, () -> {}, false));
    }
    
    /**
     * 设置普通模式选项
     */
    private void setupDefaultModeItem(Player player, PlayerData playerData) {
        boolean isSelected = playerData.getModeType() == ModeType.DEFAULT;
        
        setItem(11, new ItemBuilderUtil()
                .setType(MaterialUtil.BED())
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
                    
                    playerData.setModeType(ModeType.DEFAULT);
                    SoundUtil.playOrbPickupSound(player);
                    LobbyBoard.updateBoard();
                    player.sendMessage("§a已选择普通模式!");
                }, true));
    }
    
    /**
     * 设置经验模式选项
     */
    private void setupExperienceModeItem(Player player, PlayerData playerData) {
        boolean isSelected = playerData.getModeType() == ModeType.EXPERIENCE;
        
        setItem(15, new ItemBuilderUtil()
                .setType(MaterialUtil.EXPERIENCE_BOTTLE())
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
                    
                    playerData.setModeType(ModeType.EXPERIENCE);
                    SoundUtil.playOrbPickupSound(player);
                    LobbyBoard.updateBoard();
                    player.sendMessage("§a已选择经验模式!");
                }, true));
    }
}
