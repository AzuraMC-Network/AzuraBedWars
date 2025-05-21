package cc.azuramc.bedwars.shop.gui;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.database.profile.PlayerProfile;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.shop.*;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
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
        PlayerProfile playerProfile = gamePlayer.getPlayerProfile();

        // 初始化界面
        initializeUI(gameManager, gamePlayer, itemStack, className, playerProfile);
    }
    
    /**
     * 初始化用户界面
     */
    private void initializeUI(GameManager gameManager, GamePlayer gamePlayer, ItemStack itemStack, String className, PlayerProfile playerProfile) {
        
        // 设置顶部展示物品
        setItem(4, new ItemBuilder()
                .setItemStack(itemStack)
                .setLores(" ", "§e正在添加物品到快捷购买菜单..")
                .getItem(), 
                new GUIAction(0, () -> {}, false));
                
        // 设置GUI边框装饰
        setupBorders();
        
        // 设置快捷购买槽位
        setupShopSlots(gameManager, gamePlayer, className, playerProfile);
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
     * 设置商店槽位
     */
    private void setupShopSlots(GameManager gameManager, GamePlayer gamePlayer, String className, PlayerProfile playerProfile) {
        Player player = gamePlayer.getPlayer();
        String[] shopSort = playerProfile.getShopSort();
        
        for (int i = 0; i < SHOP_SLOTS.length; i++) {
            String slotData = shopSort[i];
            int slotPosition = SHOP_SLOTS[i];
            
            // 解析槽位数据
            String[] itemInfo = !"AIR".equals(slotData) ? slotData.split("#") : null;
            ShopItemType shopItemType = findItemType(itemInfo);
            
            if (itemInfo == null || shopItemType == null) {
                // 空槽位
                setupEmptySlot(gameManager, gamePlayer, slotPosition, i, className, playerProfile);
            } else {
                // 已有物品的槽位
                setupOccupiedSlot(gameManager, gamePlayer, slotPosition, shopItemType, className);
            }
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
    private void setupEmptySlot(GameManager gameManager, GamePlayer gamePlayer, int slotPosition, int slotIndex, String className, PlayerProfile playerProfile) {
        setItem(slotPosition, 
                new ItemBuilder()
                    .setItemStack(Objects.requireNonNull(XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + EMPTY_SLOT_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem()))
                    .setDisplayName("§c空闲的槽位")
                    .setLores("§e点击设置该位置为当前物品")
                    .getItem(), 
                new GUIAction(0, () -> {
                    // 更新玩家数据
                    playerProfile.getShopSort()[slotIndex] = className;
                    playerProfile.saveShops();
                    
                    // 播放确认音效
                    gamePlayer.playSound(XSound.UI_BUTTON_CLICK.get(), 1, 10F);
                    
                    // 返回物品商店
                    new ItemShopGUI(gamePlayer, 0, gameManager).open();
                }, false));
    }
    
    /**
     * 设置已占用槽位
     */
    private void setupOccupiedSlot(GameManager gameManager, GamePlayer gamePlayer, int slotPosition, ShopItemType shopItemType, String className) {
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
                    PlayerProfile playerProfile = gamePlayer.getPlayerProfile();
                    
                    // 获取槽位索引
                    int slotIndex = Arrays.asList(SHOP_SLOTS).indexOf(slotPosition);
                    
                    // 更新玩家数据
                    playerProfile.getShopSort()[slotIndex] = className;
                    playerProfile.saveShops();
                    
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
