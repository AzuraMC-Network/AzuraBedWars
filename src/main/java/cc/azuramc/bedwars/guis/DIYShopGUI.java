package cc.azuramc.bedwars.guis;

import cc.azuramc.bedwars.utils.gui.CustomGUI;
import cc.azuramc.bedwars.utils.gui.GUIAction;
import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.shop.ItemShopManager;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * 自定义商店GUI
 * 用于将物品添加到快捷购买菜单
 */
public class DIYShopGUI extends CustomGUI {
    
    // 商店可选择的槽位
    private static final Integer[] SHOP_SLOTS = new Integer[]{
            19, 20, 21, 22, 23, 24, 25, 
            28, 29, 30, 31, 32, 33, 34, 
            37, 38, 39, 40, 41, 42, 43
    };
    
    // 商店边框装饰物品类型
    private static final int BORDER_GLASS_COLOR = 7; // 灰色
    private static final int EMPTY_SLOT_GLASS_COLOR = 14; // 红色

    /**
     * 创建自定义商店GUI
     *
     * @param game 游戏实例
     * @param gamePlayer 游戏玩家
     * @param itemStack 要添加的物品
     * @param className 物品分类名称
     */
    public DIYShopGUI(Game game, GamePlayer gamePlayer, ItemStack itemStack, String className) {
        super(gamePlayer.getPlayer(), "§8添加物品到快捷购买", 54);
        Player player = gamePlayer.getPlayer();
        PlayerData playerData = gamePlayer.getPlayerData();

        // 初始化界面
        initializeUI(game, gamePlayer, itemStack, className, playerData);
    }
    
    /**
     * 初始化用户界面
     */
    private void initializeUI(Game game, GamePlayer gamePlayer, ItemStack itemStack, String className, PlayerData playerData) {
        Player player = gamePlayer.getPlayer();
        
        // 设置顶部展示物品
        setItem(4, new ItemBuilderUtil()
                .setItemStack(itemStack)
                .setLores(" ", "§e正在添加物品到快捷购买菜单..")
                .getItem(), 
                new GUIAction(0, () -> {}, false));
                
        // 设置GUI边框装饰
        setupBorders();
        
        // 设置快捷购买槽位
        setupShopSlots(game, gamePlayer, className, playerData);
    }
    
    /**
     * 设置GUI边框装饰
     */
    private void setupBorders() {
        // 设置顶部边框
        for (int i = 0; i < 9; i++) {
            if (i != 4) { // 跳过中间物品展示位置
                setItem(i, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
            }
        }
        
        // 设置左右边框
        for (int row = 1; row < 6; row++) {
            int leftBorder = row * 9;
            int rightBorder = row * 9 + 8;
            setItem(leftBorder, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
            setItem(rightBorder, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        }
        
        // 设置底部边框
        for (int i = 45; i < 54; i++) {
            setItem(i, MaterialUtil.getStainedGlassPane(BORDER_GLASS_COLOR), new GUIAction(0, () -> {}, false));
        }
    }
    
    /**
     * 设置商店槽位
     */
    private void setupShopSlots(Game game, GamePlayer gamePlayer, String className, PlayerData playerData) {
        Player player = gamePlayer.getPlayer();
        String[] shopSort = playerData.getShopSort();
        
        for (int i = 0; i < SHOP_SLOTS.length; i++) {
            String slotData = shopSort[i];
            int slotPosition = SHOP_SLOTS[i];
            
            // 解析槽位数据
            String[] itemInfo = !slotData.equals("AIR") ? slotData.split("#") : null;
            ItemType itemType = findItemType(itemInfo);
            
            if (itemInfo == null || itemType == null) {
                // 空槽位
                setupEmptySlot(game, player, slotPosition, i, className, playerData);
            } else {
                // 已有物品的槽位
                setupOccupiedSlot(game, gamePlayer, slotPosition, itemType, className);
            }
        }
    }
    
    /**
     * 查找物品类型
     */
    private ItemType findItemType(String[] itemInfo) {
        if (itemInfo == null || itemInfo.length != 2) {
            return null;
        }
        
        for (ShopData shopData : ItemShopManager.getShops()) {
            if (shopData.getClass().getSimpleName().equals(itemInfo[0])) {
                return shopData.getShopItems().get(Integer.parseInt(itemInfo[1]) - 1);
            }
        }
        
        return null;
    }
    
    /**
     * 设置空槽位
     */
    private void setupEmptySlot(Game game, Player player, int slotPosition, int slotIndex, String className, PlayerData playerData) {
        setItem(slotPosition, 
                new ItemBuilderUtil()
                    .setItemStack(MaterialUtil.getStainedGlassPane(EMPTY_SLOT_GLASS_COLOR))
                    .setDisplayName("§c空闲的槽位")
                    .setLores("§e点击设置该位置为当前物品")
                    .getItem(), 
                new GUIAction(0, () -> {
                    // 更新玩家数据
                    playerData.getShopSort()[slotIndex] = className;
                    playerData.saveShops();
                    
                    // 播放确认音效
                    SoundUtil.playClickSound(player);
                    
                    // 返回物品商店
                    new ItemShopGUI(player, 0, game).open();
                }, false));
    }
    
    /**
     * 设置已占用槽位
     */
    private void setupOccupiedSlot(Game game, GamePlayer gamePlayer, int slotPosition, ItemType itemType, String className) {
        // 准备物品显示
        ItemBuilderUtil itemBuilder = prepareItemDisplay(gamePlayer, itemType);
        
        // 设置GUI项
        super.setItem(slotPosition, 
                itemBuilder
                    .setDisplayName("§c" + itemType.getDisplayName())
                    .setLores("§e点击替换为当前物品")
                    .getItem(), 
                new GUIAction(0, () -> {
                    Player player = gamePlayer.getPlayer();
                    PlayerData playerData = gamePlayer.getPlayerData();
                    
                    // 获取槽位索引
                    int slotIndex = Arrays.asList(SHOP_SLOTS).indexOf(slotPosition);
                    
                    // 更新玩家数据
                    playerData.getShopSort()[slotIndex] = className;
                    playerData.saveShops();
                    
                    // 播放确认音效
                    SoundUtil.playClickSound(player);
                    
                    // 返回物品商店
                    new ItemShopGUI(player, 0, game).open();
                }, false));
    }

    /**
     * 准备物品显示
     */
    private ItemBuilderUtil prepareItemDisplay(GamePlayer gamePlayer, ItemType itemType) {
        ItemBuilderUtil itemBuilder = new ItemBuilderUtil();
        itemBuilder.setItemStack(itemType.getItemStack().clone());
        
        // 根据物品颜色类型进行特殊处理
        if (itemType.getColorType() == ColorType.PICKAXE) {
            updatePickaxeDisplay(gamePlayer, itemBuilder, itemType);
        } else if (itemType.getColorType() == ColorType.AXE) {
            updateAxeDisplay(gamePlayer, itemBuilder, itemType);
        }
        
        return itemBuilder;
    }
    
    /**
     * 更新稿子显示
     */
    private void updatePickaxeDisplay(GamePlayer gamePlayer, ItemBuilderUtil itemBuilder, ItemType itemType) {
        switch (gamePlayer.getPickaxeType()) {
            case STONE:
                itemBuilder.setType(MaterialUtil.STONE_PICKAXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.IRON_INGOT(), 20, 20));
                break;
            case IRON:
                itemBuilder.setType(MaterialUtil.IRON_PICKAXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.GOLD_INGOT(), 8, 24));
                break;
            case DIAMOND:
                itemBuilder.setType(MaterialUtil.DIAMOND_PICKAXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.GOLD_INGOT(), 12, 36));
                break;
            default:
                itemBuilder.setType(MaterialUtil.WOODEN_PICKAXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.IRON_INGOT(), 10, 10));
                break;
        }
    }
    
    /**
     * 更新斧头显示
     */
    private void updateAxeDisplay(GamePlayer gamePlayer, ItemBuilderUtil itemBuilder, ItemType itemType) {
        switch (gamePlayer.getAxeType()) {
            case STONE:
                itemBuilder.setType(MaterialUtil.STONE_AXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.IRON_INGOT(), 20, 20));
                break;
            case IRON:
                itemBuilder.setType(MaterialUtil.IRON_AXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.GOLD_INGOT(), 8, 24));
                break;
            case DIAMOND:
                itemBuilder.setType(MaterialUtil.DIAMOND_AXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.GOLD_INGOT(), 12, 36));
                break;
            default:
                itemBuilder.setType(MaterialUtil.WOODEN_AXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.IRON_INGOT(), 10, 10));
                break;
        }
    }
}
