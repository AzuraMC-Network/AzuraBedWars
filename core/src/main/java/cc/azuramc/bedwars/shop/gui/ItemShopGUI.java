package cc.azuramc.bedwars.shop.gui;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.item.armor.ArmorType;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.gui.base.action.NewGUIAction;
import cc.azuramc.bedwars.shop.ColorType;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.ShopItemType;
import cc.azuramc.bedwars.shop.ShopManager;
import cc.azuramc.bedwars.shop.helper.ShopItemGiver;
import cc.azuramc.bedwars.shop.helper.ShopPaymentHandler;
import cc.azuramc.bedwars.shop.helper.ToolDisplayHelper;
import cc.azuramc.bedwars.shop.page.DefaultShopPage;
import cc.azuramc.bedwars.util.MessageUtil;
import cc.azuramc.bedwars.util.ShopUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

/**
 * 道具商店GUI类
 * 用于展示和处理各种可购买道具
 *
 * @author an5w1r@163.com
 */
public class ItemShopGUI extends CustomGUI {

    /**
     * 资源名称缓存
     */
    private static final Map<Material, String> RESOURCE_NAMES = new HashMap<>();

    static {
        RESOURCE_NAMES.put(XMaterial.IRON_INGOT.get(), "铁");
        RESOURCE_NAMES.put(XMaterial.GOLD_INGOT.get(), "金");
        RESOURCE_NAMES.put(XMaterial.EMERALD.get(), "绿宝石");
        RESOURCE_NAMES.put(XMaterial.DIAMOND.get(), "钻石");
    }

    /**
     * 创建道具商店GUI
     *
     * @param gamePlayer  游戏玩家
     * @param slot        商店槽位
     * @param gameManager 游戏实例
     */
    public ItemShopGUI(GamePlayer gamePlayer, int slot, GameManager gameManager) {
        super(gamePlayer, "§8道具商店 - " + ChatColor.stripColor(ShopManager.getSHOPS().get(slot).getMainShopItem().getDisplayName()), 54);

        initializeShopNavbar(gamePlayer, slot, gameManager);
        initializeShopSeparator(slot);

        ShopData shopData = ShopManager.getSHOPS().get(slot);
        if (shopData instanceof DefaultShopPage) {
            initializeQuickBuyShop(gamePlayer, slot, gameManager);
        } else {
            initializeRegularShop(gamePlayer, shopData, slot, gameManager);
        }
    }

    /**
     * 初始化商店导航栏
     */
    private void initializeShopNavbar(GamePlayer gamePlayer, int slot, GameManager gameManager) {
        int i = 0;
        for (ShopData shopData : ShopManager.getSHOPS()) {
            if (i > 9) {
                continue;
            }

            int finalI = i;
            setItem(i, new ItemBuilder()
                            .setItemStack(shopData.getMainShopItem().getItemStack().clone())
                            .setDisplayName(shopData.getMainShopItem().getDisplayName())
                            .getItem(),
                    new GUIAction(0, () -> {
                        if (finalI != slot) {
                            new ItemShopGUI(gamePlayer, finalI, gameManager).open();
                        }
                    }, false));
            ++i;
        }
    }

    /**
     * 初始化商店分隔条
     */
    private void initializeShopSeparator(int currentShopSlot) {
        for (int i = 9; i < 18; i++) {
            if (i == (currentShopSlot + 9)) {
                setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:5").orElse(XMaterial.GLASS_PANE).parseItem(),
                        new GUIAction(0, () -> {
                        }, false));
            } else {
                setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:7").orElse(XMaterial.GLASS_PANE).parseItem(),
                        new GUIAction(0, () -> {
                        }, false));
            }
        }
    }

    /**
     * 初始化快捷购买商店
     */
    private void initializeQuickBuyShop(GamePlayer gamePlayer, int slot, GameManager gameManager) {
        Map<Integer, String> shopDataMap = ShopUtil.loadShopDataFromJson(gamePlayer.getPlayerData());
        ShopData defaultShopData = ShopManager.getSHOPS().get(slot);
        List<ShopItemType> defaultItems = defaultShopData.getShopItems();

        for (int slotIndex = 0; slotIndex < ShopUtil.SHOP_SLOTS.length; slotIndex++) {
            int actualSlotPosition = ShopUtil.SHOP_SLOTS[slotIndex];
            String itemData = shopDataMap.get(slotIndex);

            if (itemData != null && !"AIR".equals(itemData)) {
                String[] itemInfo = itemData.split("#");
                ShopItemType shopItemType = ShopUtil.findItemType(itemInfo);

                if (shopItemType != null) {
                    setItem(gamePlayer, slot, actualSlotPosition, gameManager, shopItemType, -1,
                            Arrays.asList("§7Shift+左键从快捷购买中移除", " "));
                    continue;
                }
            }

            if (slotIndex < defaultItems.size()) {
                ShopItemType defaultItemType = defaultItems.get(slotIndex);
                setItem(gamePlayer, slot, actualSlotPosition, gameManager, defaultItemType, slotIndex,
                        Arrays.asList("§7Shift+左键添加到快捷购买", " "));
            } else {
                setEmptySlot(gamePlayer.getPlayer(), actualSlotPosition, slot, gameManager);
            }
        }
    }

    /**
     * 初始化常规商店
     */
    private void initializeRegularShop(GamePlayer gamePlayer, ShopData shopData, int shopSlot, GameManager gameManager) {
        int itemIndex = -1;
        for (ShopItemType shopItemType : shopData.getShopItems()) {
            itemIndex++;
            setItem(gamePlayer, shopSlot, ShopUtil.SHOP_SLOTS[itemIndex], gameManager, shopItemType, itemIndex, null);
        }
    }

    /**
     * 设置空槽位
     */
    private void setEmptySlot(Player player, int slotPosition, int shopSlot, GameManager gameManager) {
        setItem(slotPosition,
                new ItemBuilder()
                        .setItemStack(Objects.requireNonNull(XMaterial.matchXMaterial("STAINED_GLASS_PANE:14").orElse(XMaterial.GLASS_PANE).parseItem()))
                        .setDisplayName("§c空闲的槽位")
                        .setLores("§7这是一个快捷购买槽位!§bShift+左键", "§7将任意物品放到这里~")
                        .getItem(),
                new NewGUIAction(0, event -> {
                    if (!event.getClick().isShiftClick()) {
                        return;
                    }
                    player.sendMessage("§c这是个空的槽位!请使用Shift+左键添加物品到这里~");
                }, false));
    }

    /**
     * 设置商店物品
     */
    public void setItem(GamePlayer gamePlayer, int shopSlot, int displaySlot, GameManager gameManager,
                        ShopItemType shopItemType, int itemSlot, List<String> moreLore) {
        PlayerData playerData = gamePlayer.getPlayerData();
        ItemBuilder itemBuilder = prepareItemDisplay(gamePlayer, shopItemType);
        List<String> lore = createItemLore(shopItemType, playerData.getMode(), moreLore);

        super.setItem(displaySlot,
                itemBuilder.setDisplayName("§c" + shopItemType.getDisplayName())
                        .setLores(lore)
                        .getItem(),
                new NewGUIAction(0, event -> handleItemClick(event, gamePlayer, shopSlot, displaySlot,
                        shopItemType, itemBuilder, itemSlot, playerData, gameManager), false));
    }

    /**
     * 准备物品显示
     */
    private ItemBuilder prepareItemDisplay(GamePlayer gamePlayer, ShopItemType shopItemType) {
        ItemBuilder itemBuilder = new ItemBuilder();
        itemBuilder.setItemStack(shopItemType.getItemStack().clone());

        if (shopItemType.getColorType() == ColorType.PICKAXE) {
            ToolDisplayHelper.updatePickaxeDisplay(gamePlayer, itemBuilder, shopItemType);
        } else if (shopItemType.getColorType() == ColorType.AXE) {
            ToolDisplayHelper.updateAxeDisplay(gamePlayer, itemBuilder, shopItemType);
        }

        return itemBuilder;
    }

    /**
     * 创建物品说明
     */
    private List<String> createItemLore(ShopItemType shopItemType, GameModeType gameModeType, List<String> moreLore) {
        List<String> lore = new ArrayList<>();
        lore.add("§7物品:");
        lore.add("§8•" + shopItemType.getDisplayName());
        lore.add(" ");

        if (moreLore != null && !moreLore.isEmpty()) {
            lore.addAll(moreLore);
        }

        Material priceMaterial = shopItemType.getPriceCost().material();
        String resourceName = RESOURCE_NAMES.getOrDefault(priceMaterial, "资源");

        if (gameModeType == GameModeType.EXPERIENCE) {
            lore.add("§7花费: §3§l" + shopItemType.getPriceCost().xp() + "级");
        } else {
            lore.add("§7花费: §3§l" + shopItemType.getPriceCost().amount() + " " + resourceName);
        }

        return lore;
    }

    /**
     * 处理物品点击
     */
    private void handleItemClick(InventoryClickEvent event, GamePlayer gamePlayer,
                                 int shopSlot, int displaySlot, ShopItemType shopItemType, ItemBuilder itemBuilder,
                                 int itemSlot, PlayerData playerData, GameManager gameManager) {
        if (event.isShiftClick() || event.getClick().isShiftClick()) {
            handleShiftClick(gamePlayer, shopSlot, displaySlot, itemBuilder, itemSlot, playerData, gameManager);
            return;
        }

        if (!canPurchaseItem(gamePlayer, shopItemType, itemBuilder.getItem().getType())) {
            return;
        }

        if (!ShopPaymentHandler.processPayment(gamePlayer, shopItemType, playerData.getMode())) {
            return;
        }

        ShopItemGiver.handleItemGiving(gamePlayer, shopSlot, shopItemType, itemBuilder, gameManager);
    }

    /**
     * 处理Shift+点击
     */
    private void handleShiftClick(GamePlayer gamePlayer, int shopSlot, int displaySlot,
                                  ItemBuilder itemBuilder, int itemSlot, PlayerData playerData, GameManager gameManager) {
        if (shopSlot == 0) {
            int slotIndex = Arrays.asList(ShopUtil.SHOP_SLOTS).indexOf(displaySlot);
            if (slotIndex == -1) {
                return;
            }

            Map<Integer, String> shopDataMap = ShopUtil.loadShopDataFromJson(playerData);
            shopDataMap.remove(slotIndex);
            ShopUtil.saveShopDataToJson(playerData, shopDataMap);

            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
        } else {
            new DIYShopGUI(gamePlayer, gameManager, itemBuilder.getItem().clone(),
                    ShopManager.getSHOPS().get(shopSlot).getClass().getSimpleName()
                            + "#" + (itemSlot + 1)).open();
        }
    }

    /**
     * 检查是否可以购买物品
     */
    private boolean canPurchaseItem(GamePlayer gamePlayer, ShopItemType shopItemType, Material itemMaterial) {
        // 工具已满级检查
        if (shopItemType.getColorType() == ColorType.PICKAXE && gamePlayer.getPickaxeType() == ToolType.DIAMOND) {
            return false;
        }
        if (shopItemType.getColorType() == ColorType.AXE && gamePlayer.getAxeType() == ToolType.DIAMOND) {
            return false;
        }

        // 剪刀检查
        if (XMaterial.SHEARS.get() != null && XMaterial.SHEARS.get().equals(itemMaterial) && gamePlayer.isShear()) {
            return false;
        }

        // 护甲检查
        if (itemMaterial == XMaterial.CHAINMAIL_BOOTS.get() && gamePlayer.getArmorType() != ArmorType.DEFAULT) {
            return false;
        }
        if (itemMaterial == XMaterial.IRON_BOOTS.get() &&
                (gamePlayer.getArmorType() == ArmorType.IRON || gamePlayer.getArmorType() == ArmorType.DIAMOND)) {
            return false;
        }
        if (itemMaterial == XMaterial.DIAMOND_BOOTS.get() && gamePlayer.getArmorType() == ArmorType.DIAMOND) {
            return false;
        }

        // 首次购买工具检查
        if ((itemMaterial == XMaterial.WOODEN_PICKAXE.get() && gamePlayer.getPickaxeType() == ToolType.NONE) && hasEmptySlot(gamePlayer)) {
            return true;
        }
        if ((itemMaterial == XMaterial.WOODEN_AXE.get() && gamePlayer.getAxeType() == ToolType.NONE) && hasEmptySlot(gamePlayer)) {
            return true;
        }

        // 背包空间检查
        if (!hasEmptySlot(gamePlayer)) {
            gamePlayer.sendMessage(MessageUtil.color("&c背包已满！"));
            return false;
        }

        return true;
    }

    private boolean hasEmptySlot(GamePlayer gamePlayer) {
        return gamePlayer.getPlayer().getInventory().firstEmpty() != -1;
    }
}
