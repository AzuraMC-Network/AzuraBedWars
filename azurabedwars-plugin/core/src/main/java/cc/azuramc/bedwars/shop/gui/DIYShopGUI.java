package cc.azuramc.bedwars.shop.gui;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.shop.*;
import cc.azuramc.bedwars.util.ShopUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class DIYShopGUI extends CustomGUI {


    /**
     * 灰色
     */
    private static final int BORDER_GLASS_COLOR = 7;
    /**
     * 红色
     */
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
                new GUIAction(0, () -> {
                }, false));

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
                setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
                }, false));
            }
        }

        // 设置左右边框
        for (int row = 1; row < 6; row++) {
            int leftBorder = row * 9;
            int rightBorder = row * 9 + 8;
            setItem(leftBorder, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
            }, false));
            setItem(rightBorder, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
            }, false));
        }

        // 设置底部边框
        for (int i = 45; i < 54; i++) {
            setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
            }, false));
        }
    }

    /**
     * 设置商店槽位
     */
    private void setupShopSlots(GameManager gameManager, GamePlayer gamePlayer, String className) {
        PlayerData playerData = gamePlayer.getPlayerData();
        // 从数据库加载JSON格式的快捷商店配置
        Map<Integer, String> shopDataMap = ShopUtil.loadShopDataFromJson(playerData);

        // 获取DefaultShopPage作为默认物品来源
        ShopData defaultShopData = ShopManager.getSHOPS().get(0);
        List<ShopItemType> defaultItems = defaultShopData.getShopItems();

        // 遍历所有可用槽位
        for (int slotIndex = 0; slotIndex < ShopUtil.SHOP_SLOTS.length; slotIndex++) {
            int actualSlotPosition = ShopUtil.SHOP_SLOTS[slotIndex];

            // 检查该槽位是否有用户配置的物品
            String itemData = shopDataMap.get(slotIndex);

            if (itemData != null && !"AIR".equals(itemData)) {
                // 用户有自定义物品配置
                String[] itemInfo = itemData.split("#");
                ShopItemType shopItemType = ShopUtil.findItemType(itemInfo);

                if (shopItemType != null) {
                    setupOccupiedSlot(gameManager, gamePlayer, actualSlotPosition, shopItemType, className, slotIndex);
                    continue;
                }
                // 如果自定义物品数据有误，继续使用默认物品逻辑
            }

            // 用户没有配置或配置无效 使用默认物品
            if (slotIndex < defaultItems.size()) {
                ShopItemType defaultItemType = defaultItems.get(slotIndex);
                setupDefaultSlot(gameManager, gamePlayer, actualSlotPosition, defaultItemType, className, slotIndex, playerData);
            } else {
                // 超出默认物品数量 设置真正的空槽位
                setupEmptySlot(gameManager, gamePlayer, actualSlotPosition, slotIndex, className, playerData);
            }
        }
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
                    Map<Integer, String> shopDataMap = ShopUtil.loadShopDataFromJson(playerData);

                    // 更新指定槽位的物品
                    shopDataMap.put(slotIndex, className);

                    // 保存回数据库
                    ShopUtil.saveShopDataToJson(playerData, shopDataMap);

                    // 播放确认音效
                    gamePlayer.playSound(XSound.UI_BUTTON_CLICK.get(), 1, 10F);

                    // 返回物品商店
                    new ItemShopGUI(gamePlayer, 0, gameManager).open();
                }, false));
    }

    /**
     * 设置默认物品槽位
     * 显示DefaultShopPage的默认物品 但允许用户替换
     */
    private void setupDefaultSlot(GameManager gameManager, GamePlayer gamePlayer, int slotPosition,
                                  ShopItemType defaultItemType, String className, int slotIndex, PlayerData playerData) {
        // 准备物品显示
        ItemBuilder itemBuilder = prepareItemDisplay(gamePlayer, defaultItemType);

        // 设置GUI项
        super.setItem(slotPosition,
                itemBuilder
                        .setDisplayName("§a" + defaultItemType.getDisplayName() + " §7(默认)")
                        .setLores("§7这是默认快捷购买物品", "§e点击替换为当前选择的物品")
                        .getItem(),
                new GUIAction(0, () -> {
                    // 加载当前快捷商店配置
                    Map<Integer, String> shopDataMap = ShopUtil.loadShopDataFromJson(playerData);

                    // 更新指定槽位的物品
                    shopDataMap.put(slotIndex, className);

                    // 保存回数据库
                    ShopUtil.saveShopDataToJson(playerData, shopDataMap);

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
                    Map<Integer, String> shopDataMap = ShopUtil.loadShopDataFromJson(playerData);

                    // 更新指定槽位的物品
                    shopDataMap.put(slotIndex, className);

                    // 保存回数据库
                    ShopUtil.saveShopDataToJson(playerData, shopDataMap);

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
