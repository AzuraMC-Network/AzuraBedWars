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
import cc.azuramc.bedwars.shop.*;
import cc.azuramc.bedwars.shop.page.DefaultShopPage;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.MessageUtil;
import cc.azuramc.bedwars.util.ShopUtil;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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


    /* 静态初始化资源名称 */
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

        // 初始化商店导航栏
        initializeShopNavbar(gamePlayer, slot, gameManager);

        // 初始化商店分隔条
        initializeShopSeparator(slot);

        // 初始化商店内容
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
                // 高亮当前选中的商店分类
                setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:5").orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
                }, false));
            } else {
                setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:7").orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
                }, false));
            }
        }
    }

    /**
     * 初始化快捷购买商店
     * 优先显示用户自定义配置，空槽位显示DefaultShopPage的默认物品
     */
    private void initializeQuickBuyShop(GamePlayer gamePlayer, int slot, GameManager gameManager) {
        // 从数据库加载JSON格式的快捷商店配置
        Map<Integer, String> shopDataMap = ShopUtil.loadShopDataFromJson(gamePlayer.getPlayerData());

        // 获取DefaultShopPage作为默认物品来源
        ShopData defaultShopData = ShopManager.getSHOPS().get(slot);
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
                    // 设置用户自定义的物品
                    setItem(gamePlayer, slot, actualSlotPosition, gameManager, shopItemType, -1,
                            Arrays.asList("§7Shift+左键从快捷购买中移除", " "));
                    continue;
                }
                // 如果自定义物品数据有误 继续使用默认物品逻辑
            }

            // 用户没有配置或配置无效 使用默认物品
            if (slotIndex < defaultItems.size()) {
                ShopItemType defaultItemType = defaultItems.get(slotIndex);
                setItem(gamePlayer, slot, actualSlotPosition, gameManager, defaultItemType, slotIndex,
                        Arrays.asList("§7Shift+左键添加到快捷购买", " "));
            } else {
                // 超出默认物品数量 设置空槽位
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
                        .setItemStack(XMaterial.matchXMaterial("STAINED_GLASS_PANE:14").orElse(XMaterial.GLASS_PANE).parseItem())
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
    public void setItem(GamePlayer gamePlayer, int shopSlot, int displaySlot, GameManager gameManager, ShopItemType shopItemType, int itemSlot, List<String> moreLore) {
        Player player = gamePlayer.getPlayer();
        PlayerData playerData = gamePlayer.getPlayerData();

        // 准备物品显示
        ItemBuilder itemBuilder = prepareItemDisplay(gamePlayer, shopItemType);

        // 创建物品说明
        List<String> lore = createItemLore(shopItemType, gamePlayer.getGameModeType(), moreLore);

        // 设置商店项
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
            case WOOD:
                itemBuilder.setType(XMaterial.STONE_PICKAXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.IRON_INGOT.get(), 20, 20));
                break;
            case STONE:
                itemBuilder.setType(XMaterial.IRON_PICKAXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.GOLD_INGOT.get(), 8, 24));
                break;
            case IRON:
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
            case WOOD:
                itemBuilder.setType(XMaterial.STONE_AXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.IRON_INGOT.get(), 20, 20));
                break;
            case STONE:
                itemBuilder.setType(XMaterial.IRON_AXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.GOLD_INGOT.get(), 8, 24));
                break;
            case IRON:
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

    /**
     * 创建物品说明
     */
    private List<String> createItemLore(ShopItemType shopItemType, GameModeType gameModeType, List<String> moreLore) {
        List<String> lore = new ArrayList<>();
        lore.add("§7物品:");
        lore.add("§8•" + shopItemType.getDisplayName());
        lore.add(" ");

        // 添加额外说明
        if (moreLore != null && !moreLore.isEmpty()) {
            lore.addAll(moreLore);
        }

        // 添加价格说明
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
        // 处理Shift+点击 (快捷购买相关操作)
        if (event.isShiftClick() || event.getClick().isShiftClick()) {
            handleShiftClick(gamePlayer, shopSlot, displaySlot, itemBuilder, itemSlot, playerData, gameManager);
            return;
        }

        // 检查是否可以购买（工具已达最高级或已拥有）
        if (!canPurchaseItem(gamePlayer, shopItemType, itemBuilder.getItem().getType())) {
            return;
        }

        // 处理支付
        if (!processPayment(gamePlayer, shopItemType, playerData.getMode())) {
            return;
        }

        // 处理物品给予
        handleItemGiving(gamePlayer, shopSlot, shopItemType, itemBuilder, gameManager);
    }

    /**
     * 处理Shift+点击 - 使用JSON格式
     */
    private void handleShiftClick(GamePlayer gamePlayer, int shopSlot, int displaySlot,
                                  ItemBuilder itemBuilder, int itemSlot, PlayerData playerData, GameManager gameManager) {
        if (shopSlot == 0) {
            // 从快捷购买移除
            int slotIndex = Arrays.asList(ShopUtil.SHOP_SLOTS).indexOf(displaySlot);
            if (slotIndex == -1) {
                return;
            }

            // 加载当前快捷商店配置
            Map<Integer, String> shopDataMap = ShopUtil.loadShopDataFromJson(playerData);

            // 移除指定槽位的物品
            shopDataMap.remove(slotIndex);

            // 保存回数据库
            ShopUtil.saveShopDataToJson(playerData, shopDataMap);

            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
        } else {
            // 添加到快捷购买
            new DIYShopGUI(gamePlayer, gameManager, itemBuilder.getItem().clone(),
                    ShopManager.getSHOPS().get(shopSlot).getClass().getSimpleName()
                            + "#" + (itemSlot + 1)).open();
        }
    }


    /**
     * 检查是否可以购买物品
     */
    private boolean canPurchaseItem(GamePlayer gamePlayer, ShopItemType shopItemType, Material itemMaterial) {
        // 如果镐子已满级则不卖
        if (shopItemType.getColorType() == ColorType.PICKAXE && gamePlayer.getPickaxeType() == ToolType.DIAMOND) {
            return false;
        }

        // 如果斧子已满级则不卖
        if (shopItemType.getColorType() == ColorType.AXE && gamePlayer.getAxeType() == ToolType.DIAMOND) {
            return false;
        }

        // 如果已经有剪刀则不卖
        if (XMaterial.SHEARS.get().equals(itemMaterial) && gamePlayer.isShear()) {
            return false;
        }

        // 如果买锁链且当前不是皮革则不卖
        if (itemMaterial == XMaterial.CHAINMAIL_BOOTS.get() && gamePlayer.getArmorType() != ArmorType.DEFAULT) {
            return false;
        }

        // 如果买铁套且当前是铁套/钻套则不卖
        if (itemMaterial == XMaterial.IRON_BOOTS.get() && (gamePlayer.getArmorType() == ArmorType.IRON || gamePlayer.getArmorType() == ArmorType.DIAMOND)) {
            return false;
        }

        // 如果买钻套且当前是钻套则不卖
        if (itemMaterial == XMaterial.DIAMOND_BOOTS.get() && gamePlayer.getArmorType() == ArmorType.DIAMOND) {
            return false;
        }

        // 如果买木镐且背包有空格子则卖
        if ((itemMaterial == XMaterial.WOODEN_PICKAXE.get() && gamePlayer.getPickaxeType() == ToolType.NONE) && hasEmptySlot(gamePlayer)) {
            return true;
        }

        // 如果买木斧且背包有空格子则卖
        if ((itemMaterial == XMaterial.WOODEN_AXE.get() && gamePlayer.getAxeType() == ToolType.NONE) && hasEmptySlot(gamePlayer)) {
            return true;
        }

        // 如果背包已满则不卖
        if (!hasEmptySlot(gamePlayer)) {
            gamePlayer.sendMessage(MessageUtil.color("&c背包已满！"));
            return false;
        }

        return true;
    }

    private boolean hasEmptySlot(GamePlayer gamePlayer) {
        return gamePlayer.getPlayer().getInventory().firstEmpty() != -1;
    }

    /**
     * 处理支付
     */
    private boolean processPayment(GamePlayer gamePlayer, ShopItemType shopItemType, GameModeType gameModeType) {
        if (gameModeType == GameModeType.DEFAULT) {
            // 默认模式：支付物品
            return processItemPayment(gamePlayer, shopItemType);
        } else {
            // 经验模式：支付经验
            return processExperiencePayment(gamePlayer, shopItemType);
        }
    }

    /**
     * 处理物品支付
     */
    private boolean processItemPayment(GamePlayer gamePlayer, ShopItemType shopItemType) {
        Material paymentMaterial = shopItemType.getPriceCost().material();
        int requiredAmount = shopItemType.getPriceCost().amount();

        // 计算玩家拥有的资源总数
        int playerTotal = 0;
        ItemStack[] inventory = gamePlayer.getPlayer().getInventory().getContents();

        for (ItemStack item : inventory) {
            if (item != null && item.getType().equals(paymentMaterial)) {
                playerTotal += item.getAmount();
            }
        }

        // 检查是否有足够资源
        if (playerTotal < requiredAmount) {
            gamePlayer.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
            gamePlayer.sendMessage("§c没有足够资源购买！");
            return false;
        }

        // 扣除资源
        int remainingToDeduct = requiredAmount;
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType().equals(paymentMaterial) && remainingToDeduct > 0) {
                if (item.getAmount() > remainingToDeduct) {
                    item.setAmount(item.getAmount() - remainingToDeduct);
                    remainingToDeduct = 0;
                } else {
                    remainingToDeduct -= item.getAmount();
                    item.setAmount(0);
                }
                gamePlayer.getPlayer().getInventory().setItem(i, item);
            }
        }

        gamePlayer.playSound(XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
        return true;
    }

    /**
     * 处理经验支付
     * 优先从当前资源扣除，不足时：
     * - 如果是IRON/GOLD/DIAMOND不足，向上递增(IRON→GOLD→DIAMOND→EMERALD)
     * - 如果是EMERALD不足，向下递减(EMERALD→DIAMOND→GOLD→IRON)
     */
    private boolean processExperiencePayment(GamePlayer gamePlayer, ShopItemType shopItemType) {
        int requiredXp = shopItemType.getPriceCost().xp();

        if (gamePlayer.getPlayer().getLevel() < requiredXp) {
            gamePlayer.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
            gamePlayer.sendMessage("§c没有足够资源购买！");
            return false;
        }

        gamePlayer.getPlayer().setLevel(gamePlayer.getPlayer().getLevel() - requiredXp);
        gamePlayer.playSound(XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
        return true;
//        Player player = gamePlayer.getPlayer();
//
//        int requiredXp = shopItemType.getPriceCost().xp();
//        String requiredResourceType = shopItemType.getPriceCost().material().toString().toUpperCase();
//
//
//        // 检查玩家是否有足够的经验等级
//        if (player.getLevel() < requiredXp) {
//            gamePlayer.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
//            gamePlayer.sendMessage("§c没有足够资源购买！");
//            return false;
//        }
//
//        // 定义资源类型顺序（按价值递增）
//        final String[] resources = {"IRON", "GOLD", "DIAMOND", "EMERALD"};
//
//        // 查找当前资源类型在列表中的位置
//        int resourceIndex = -1;
//        for (int i = 0; i < resources.length; i++) {
//            if (resources[i].equals(requiredResourceType)) {
//                resourceIndex = i;
//                break;
//            }
//        }
//
//        // 如果不是标准资源类型，直接从经验中扣除
//        if (resourceIndex == -1) {
//            player.setLevel(player.getLevel() - requiredXp);
//            gamePlayer.playSound(XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
//            return true;
//        }
//
//        // 首先尝试从指定资源类型中扣除
//        int remainingXp = requiredXp;
//        int available = gamePlayer.getExperience(requiredResourceType);
//
//        if (available > 0) {
//            int toDeduct = Math.min(available, remainingXp);
//            gamePlayer.spendResourceExperience(requiredResourceType, toDeduct);
//            remainingXp -= toDeduct;
//        }
//
//        // 如果仍需扣除，根据资源类型选择向上递增或向下递减
//        if (remainingXp > 0) {
//            if (requiredResourceType.equals("EMERALD")) {
//                // 创建向下递减的资源列表 (DIAMOND → GOLD → IRON)
//                String[] lowerResources = {"DIAMOND", "GOLD", "IRON"};
//
//                // 用forEach遍历低价值资源进行扣除
//                remainingXp = getRemainingXp(gamePlayer, remainingXp, lowerResources);
//            } else {
//                // 确定需要向上递增的资源列表
//                String[] higherResources = switch (requiredResourceType) {
//                    case "IRON" -> new String[]{"GOLD", "DIAMOND", "EMERALD"};
//                    case "GOLD" -> new String[]{"DIAMOND", "EMERALD"};
//                    case "DIAMOND" -> new String[]{"EMERALD"};
//                    default -> new String[0];
//                };
//
//                // 用forEach遍历高价值资源进行扣除
//                remainingXp = getRemainingXp(gamePlayer, remainingXp, higherResources);
//            }
//        }
//
//        // 如果所有资源尝试后仍需扣除，从玩家经验等级中扣除
//        if (remainingXp > 0) {
//            player.setLevel(player.getLevel() - remainingXp);
//        }
//
//        gamePlayer.playSound(XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
//        return true;
    }

    private int getRemainingXp(GamePlayer gamePlayer, int remainingXp, String[] lowerResources) {
        int available;
        for (String resource : lowerResources) {
            if (remainingXp <= 0) {
                break;
            }

            available = gamePlayer.getExperience(resource);
            if (available > 0) {
                int toDeduct = Math.min(available, remainingXp);
                gamePlayer.spendResourceExperience(resource, toDeduct);
                remainingXp -= toDeduct;
            }
        }
        return remainingXp;
    }

    /**
     * 处理物品给予
     */
    private void handleItemGiving(GamePlayer gamePlayer, int shopSlot, ShopItemType shopItemType, ItemBuilder itemBuilder, GameManager gameManager) {
        Material material = itemBuilder.getItem().getType();

        // 处理护甲
        if (handleArmorGiving(gamePlayer, shopSlot, material, gameManager)) {
            return;
        }

        // 处理工具
        if (handleToolGiving(gamePlayer, shopSlot, material, gameManager)) {
            return;
        }

        // 处理普通物品
        handleRegularItemGiving(gamePlayer, shopItemType);
    }

    /**
     * 处理护甲给予
     */
    private boolean handleArmorGiving(GamePlayer gamePlayer, int shopSlot, Material material, GameManager gameManager) {

        if (XMaterial.CHAINMAIL_BOOTS.get() == material) {
            gamePlayer.setArmorType(ArmorType.CHAINMAIL);
            gamePlayer.giveArmor();
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        } else if (XMaterial.IRON_BOOTS.get() == material) {
            gamePlayer.setArmorType(ArmorType.IRON);
            gamePlayer.giveArmor();
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        } else if (XMaterial.DIAMOND_BOOTS.get() == material) {
            gamePlayer.setArmorType(ArmorType.DIAMOND);
            gamePlayer.giveArmor();
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        }
        return false;
    }

    /**
     * 处理工具给予
     */
    private boolean handleToolGiving(GamePlayer gamePlayer, int shopSlot, Material material, GameManager gameManager) {

        // 镐
        if (XMaterial.WOODEN_PICKAXE.get() == material) {
            gamePlayer.setPickaxeType(ToolType.WOOD);
            gamePlayer.givePickaxe(false);
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        } else if (XMaterial.STONE_PICKAXE.get() == material) {
            gamePlayer.setPickaxeType(ToolType.STONE);
            gamePlayer.givePickaxe(true);
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        } else if (XMaterial.IRON_PICKAXE.get() == material) {
            gamePlayer.setPickaxeType(ToolType.IRON);
            gamePlayer.givePickaxe(true);
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        } else if (XMaterial.DIAMOND_PICKAXE.get() == material) {
            gamePlayer.setPickaxeType(ToolType.DIAMOND);
            gamePlayer.givePickaxe(true);
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        }

        // 斧
        else if (XMaterial.WOODEN_AXE.get() == material) {
            gamePlayer.setAxeType(ToolType.WOOD);
            gamePlayer.giveAxe(false);
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        } else if (XMaterial.STONE_AXE.get() == material) {
            gamePlayer.setAxeType(ToolType.STONE);
            gamePlayer.giveAxe(true);
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        } else if (XMaterial.IRON_AXE.get() == material) {
            gamePlayer.setAxeType(ToolType.IRON);
            gamePlayer.giveAxe(true);
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        } else if (XMaterial.DIAMOND_AXE.get() == material) {
            gamePlayer.setAxeType(ToolType.DIAMOND);
            gamePlayer.giveAxe(true);
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        }

        // 剪刀
        else if (XMaterial.SHEARS.get() == material) {
            gamePlayer.setShear(true);
            gamePlayer.giveShear();
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        }

        return false;
    }

    /**
     * 处理普通物品给予
     */
    private void handleRegularItemGiving(GamePlayer gamePlayer, ShopItemType shopItemType) {
        ItemBuilder itemBuilder = new ItemBuilder().setItemStack(shopItemType.getItemStack().clone());
        // 如果物品不可放置则为其命名
        if (!shopItemType.getItemStack().getType().isBlock())
            itemBuilder.setDisplayName(shopItemType.getDisplayName());
        Player player = gamePlayer.getPlayer();

        // 处理剑特殊情况
        String itemTypeName = shopItemType.getItemStack().getType().name();
        if (itemTypeName.endsWith("_SWORD") || itemTypeName.endsWith("SWORD")) {
            player.getInventory().remove(XMaterial.WOODEN_SWORD.get());

            // 添加锋利附魔
            if (gamePlayer.getGameTeam().getUpgradeManager().hasSharpnessUpgrade()) {
                Enchantment sharpness = XEnchantment.SHARPNESS.get();
                if (sharpness != null) {
                    itemBuilder.addEnchant(sharpness, 1);
                }
            }
        }

        // 处理有颜色的方块
        if (shopItemType.getColorType() == ColorType.COLOR) {
            // 处理羊毛
            if (shopItemType.getItemStack().getType().name().contains("WOOL")) {
                // 创建带颜色的羊毛
                itemBuilder.setWoolColor(gamePlayer.getGameTeam().getDyeColor());
                itemBuilder.setAmount(shopItemType.getItemStack().getAmount());

                // 保留原始附魔（如果有）
                for (Map.Entry<Enchantment, Integer> entry : shopItemType.getItemStack().getEnchantments().entrySet()) {
                    itemBuilder.addEnchant(entry.getKey(), entry.getValue());
                }

            } else if (shopItemType.getItemStack().getType().name().contains("GLASS")) {
                itemBuilder.setGlassColor(gamePlayer.getGameTeam().getDyeColor());
                itemBuilder.setAmount(shopItemType.getItemStack().getAmount());
            } else {
                // 对于非羊毛的颜色方块，使用旧方法
                itemBuilder.setDurability(gamePlayer.getGameTeam().getDyeColor().getDyeData());
            }
        }

        // 将物品添加到玩家库存
        player.getInventory().addItem(itemBuilder.getItem());
        LoggerUtil.debug("item: " + itemBuilder.getItem().getType().name());
    }
}
