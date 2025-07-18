package cc.azuramc.bedwars.shop.gui;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.shop.*;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义商店GUI
 * 用于将物品添加到快捷购买菜单
 *
 * @author an5w1r@163.com
 */
public class DIYShopGUI extends CustomGUI {

    /**
     * 商店可选择的槽位
     */
    private static final Integer[] SHOP_SLOTS = new Integer[]{
            19, 20, 21, 22, 23, 24, 25, 
            28, 29, 30, 31, 32, 33, 34, 
            37, 38, 39, 40, 41, 42, 43
    };

    /** 灰色 */
    private static final int BORDER_GLASS_COLOR = 7;
    /** 红色 */
    private static final int EMPTY_SLOT_GLASS_COLOR = 14;
    
    /** JSON处理器 */
    private static final Gson GSON = new Gson();
    private static final Type SHOP_DATA_TYPE = new TypeToken<Map<Integer, String>>(){}.getType();

    /**
     * 创建自定义商店GUI
     *
     * @param gamePlayer  游戏玩家
     * @param gameManager 游戏实例
     * @param itemStack   要添加的物品
     * @param className   物品分类名称
     */
    public DIYShopGUI(GamePlayer gamePlayer, GameManager gameManager, ItemStack itemStack, String className) {
        super(gamePlayer, "§8添加物品到快捷购买", 54);

        // 初始化界面
        initializeUI(gameManager, gamePlayer, itemStack, className);
    }
    
    /**
     * 初始化用户界面
     */
    private void initializeUI(GameManager gameManager, GamePlayer gamePlayer, ItemStack itemStack, String className) {
        
        // 设置顶部展示物品
        setItem(4, new ItemBuilder()
                .setItemStack(itemStack)
                .setLores(" ", "§e正在添加物品到快捷购买菜单..")
                .getItem(), 
                new GUIAction(0, () -> {}, false));
                
        // 设置GUI边框装饰
        setupBorders();
        
        // 设置快捷购买槽位
        setupShopSlots(gameManager, gamePlayer, className);
    }
    
    /**
     * 设置GUI边框装饰
     */
    private void setupBorders() {
        // 设置顶部边框
        for (int i = 0; i < 9; i++) {
            // 跳过中间物品展示位置
            if (i != 4) {
                setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
            }
        }
        
        // 设置左右边框
        for (int row = 1; row < 6; row++) {
            int leftBorder = row * 9;
            int rightBorder = row * 9 + 8;
            setItem(leftBorder, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
            setItem(rightBorder, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
        }
        
        // 设置底部边框
        for (int i = 45; i < 54; i++) {
            setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
        }
    }
    
    /**
     * 设置商店槽位 - 新的JSON方式
     */
    private void setupShopSlots(GameManager gameManager, GamePlayer gamePlayer, String className) {
        PlayerData playerData = gamePlayer.getPlayerData();
        // 从数据库加载JSON格式的快捷商店配置
        Map<Integer, String> shopDataMap = loadShopDataFromJson(playerData);
        
        // 遍历所有可用槽位
        for (int slotIndex = 0; slotIndex < SHOP_SLOTS.length; slotIndex++) {
            int actualSlotPosition = SHOP_SLOTS[slotIndex];
            
            // 检查该槽位是否有配置的物品
            String itemData = shopDataMap.get(slotIndex);
            
            if (itemData == null || "AIR".equals(itemData)) {
                // 空槽位
                setupEmptySlot(gameManager, gamePlayer, actualSlotPosition, slotIndex, className, playerData);
            } else {
                // 已有物品的槽位
                String[] itemInfo = itemData.split("#");
                ShopItemType shopItemType = findItemType(itemInfo);
                
                if (shopItemType != null) {
                    setupOccupiedSlot(gameManager, gamePlayer, actualSlotPosition, shopItemType, className, slotIndex);
                } else {
                    // 数据错误，当作空槽位处理
                    setupEmptySlot(gameManager, gamePlayer, actualSlotPosition, slotIndex, className, playerData);
                }
            }
        }
    }
    
    /**
     * 从数据库加载JSON格式的快捷商店配置
     * 解析JSON为Map<Integer, String>
     */
    private Map<Integer, String> loadShopDataFromJson(PlayerData playerData) {
        try {
            String shopDataJson = playerData.getShopDataJson();
            if (shopDataJson != null && !shopDataJson.trim().isEmpty()) {
                Map<Integer, String> shopData = GSON.fromJson(shopDataJson, SHOP_DATA_TYPE);
                return shopData != null ? shopData : new HashMap<>();
            }
        } catch (Exception e) {
            // JSON解析失败，返回空Map
            e.printStackTrace();
        }
        
        // 返回默认空配置
        return new HashMap<>();
    }
    
    /**
     * 将Map<Integer, String>保存为JSON格式到数据库
     */
    private void saveShopDataToJson(PlayerData playerData, Map<Integer, String> shopDataMap) {
        try {
            String jsonData = GSON.toJson(shopDataMap);
            playerData.setShopDataJson(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 查找物品类型
     */
    private ShopItemType findItemType(String[] itemInfo) {
        if (itemInfo == null || itemInfo.length != 2) {
            return null;
        }
        
        for (ShopData shopData : ShopManager.getSHOPS()) {
            if (shopData.getClass().getSimpleName().equals(itemInfo[0])) {
                return shopData.getShopItems().get(Integer.parseInt(itemInfo[1]) - 1);
            }
        }
        
        return null;
    }
    
    /**
     * 设置空槽位
     */
    private void setupEmptySlot(GameManager gameManager, GamePlayer gamePlayer, int slotPosition, int slotIndex, String className, PlayerData playerData) {
        setItem(slotPosition, 
                new ItemBuilder()
                    .setItemStack(Objects.requireNonNull(XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + EMPTY_SLOT_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem()))
                    .setDisplayName("§c空闲的槽位")
                    .setLores("§e点击设置该位置为当前物品")
                    .getItem(), 
                new GUIAction(0, () -> {
                    // 加载当前快捷商店配置
                    Map<Integer, String> shopDataMap = loadShopDataFromJson(playerData);
                    
                    // 更新指定槽位的物品
                    shopDataMap.put(slotIndex, className);
                    
                    // 保存回数据库
                    saveShopDataToJson(playerData, shopDataMap);
                    
                    // 播放确认音效
                    gamePlayer.playSound(XSound.UI_BUTTON_CLICK.get(), 1, 10F);
                    
                    // 返回物品商店
                    new ItemShopGUI(gamePlayer, 0, gameManager).open();
                }, false));
    }
    
    /**
     * 设置已占用槽位
     */
    private void setupOccupiedSlot(GameManager gameManager, GamePlayer gamePlayer, int slotPosition, ShopItemType shopItemType, String className, int slotIndex) {
        // 准备物品显示
        ItemBuilder itemBuilder = prepareItemDisplay(gamePlayer, shopItemType);
        
        // 设置GUI项
        super.setItem(slotPosition, 
                itemBuilder
                    .setDisplayName("§c" + shopItemType.getDisplayName())
                    .setLores("§e点击替换为当前物品")
                    .getItem(), 
                new GUIAction(0, () -> {
                    Player player = gamePlayer.getPlayer();
                    PlayerData playerData = gamePlayer.getPlayerData();
                    
                    // 加载当前快捷商店配置
                    Map<Integer, String> shopDataMap = loadShopDataFromJson(playerData);
                    
                    // 更新指定槽位的物品
                    shopDataMap.put(slotIndex, className);
                    
                    // 保存回数据库
                    saveShopDataToJson(playerData, shopDataMap);
                    
                    // 播放确认音效
                    player.playSound(player.getLocation(), XSound.UI_BUTTON_CLICK.get(), 1, 10F);
                    
                    // 返回物品商店
                    new ItemShopGUI(gamePlayer, 0, gameManager).open();
                }, false));
    }

    /**
     * 准备物品显示
     */
    private ItemBuilder prepareItemDisplay(GamePlayer gamePlayer, ShopItemType shopItemType) {
        ItemBuilder itemBuilder = new ItemBuilder();
        itemBuilder.setItemStack(shopItemType.getItemStack().clone());
        
        // 根据物品颜色类型进行特殊处理
        if (shopItemType.getColorType() == ColorType.PICKAXE) {
            updatePickaxeDisplay(gamePlayer, itemBuilder, shopItemType);
        } else if (shopItemType.getColorType() == ColorType.AXE) {
            updateAxeDisplay(gamePlayer, itemBuilder, shopItemType);
        }
        
        return itemBuilder;
    }
    
    /**
     * 更新稿子显示
     */
    private void updatePickaxeDisplay(GamePlayer gamePlayer, ItemBuilder itemBuilder, ShopItemType shopItemType) {
        switch (gamePlayer.getPickaxeType()) {
            case STONE:
                itemBuilder.setType(XMaterial.STONE_PICKAXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.IRON_INGOT.get(), 20, 20));
                break;
            case IRON:
                itemBuilder.setType(XMaterial.IRON_PICKAXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.GOLD_INGOT.get(), 8, 24));
                break;
            case DIAMOND:
                itemBuilder.setType(XMaterial.DIAMOND_PICKAXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.GOLD_INGOT.get(), 12, 36));
                break;
            default:
                itemBuilder.setType(XMaterial.WOODEN_PICKAXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.IRON_INGOT.get(), 10, 10));
                break;
        }
    }
    
    /**
     * 更新斧头显示
     */
    private void updateAxeDisplay(GamePlayer gamePlayer, ItemBuilder itemBuilder, ShopItemType shopItemType) {
        switch (gamePlayer.getAxeType()) {
            case STONE:
                itemBuilder.setType(XMaterial.STONE_AXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.IRON_INGOT.get(), 20, 20));
                break;
            case IRON:
                itemBuilder.setType(XMaterial.IRON_AXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.GOLD_INGOT.get(), 8, 24));
                break;
            case DIAMOND:
                itemBuilder.setType(XMaterial.DIAMOND_AXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.GOLD_INGOT.get(), 12, 36));
                break;
            default:
                itemBuilder.setType(XMaterial.WOODEN_AXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.IRON_INGOT.get(), 10, 10));
                break;
        }
    }
}
