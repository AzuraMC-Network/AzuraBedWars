package cc.azuramc.bedwars.guis;

import cc.azuramc.bedwars.utils.gui.CustomGUI;
import cc.azuramc.bedwars.utils.gui.GUIAction;
import cc.azuramc.bedwars.utils.gui.NewGUIAction;
import cc.azuramc.bedwars.compat.util.ItemBuilderUtil;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.shop.ItemShopManager;
import cc.azuramc.bedwars.shop.ShopData;
import cc.azuramc.bedwars.shop.data.DefaultShop;
import cc.azuramc.bedwars.shop.type.ColorType;
import cc.azuramc.bedwars.shop.type.ItemType;
import cc.azuramc.bedwars.shop.type.PriceCost;
import cc.azuramc.bedwars.types.ArmorType;
import cc.azuramc.bedwars.types.ModeType;
import cc.azuramc.bedwars.types.ToolType;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import cc.azuramc.bedwars.compat.enchantment.EnchantmentUtil;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 道具商店GUI类
 * 用于展示和处理各种可购买道具
 */
public class ItemShopGUI extends CustomGUI {
    // 商店展示槽位
    private static final Integer[] SHOP_SLOTS = new Integer[]{
            19, 20, 21, 22, 23, 24, 25, 
            28, 29, 30, 31, 32, 33, 34, 
            37, 38, 39, 40, 41, 42, 43
    };
    
    // 资源名称缓存
    private static final Map<Material, String> RESOURCE_NAMES = new HashMap<>();
    
    // 静态初始化资源名称
    static {
        RESOURCE_NAMES.put(MaterialUtil.IRON_INGOT(), "铁");
        RESOURCE_NAMES.put(MaterialUtil.GOLD_INGOT(), "金");
        RESOURCE_NAMES.put(MaterialUtil.EMERALD(), "绿宝石");
        RESOURCE_NAMES.put(MaterialUtil.DIAMOND(), "钻石");
    }

    /**
     * 创建道具商店GUI
     * @param player 玩家
     * @param slot 商店槽位
     * @param game 游戏实例
     */
    public ItemShopGUI(Player player, int slot, Game game) {
        super(player, "§8道具商店 - " + ChatColor.stripColor(ItemShopManager.getShops().get(slot).getMainShopItem().getDisplayName()), 54);
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        PlayerData playerData = gamePlayer.getPlayerData();

        // 初始化商店导航栏
        initializeShopNavbar(player, slot, game);
        
        // 初始化商店分隔条
        initializeShopSeparator(slot);

        // 初始化商店内容
        ShopData shopData = ItemShopManager.getShops().get(slot);
        if (shopData instanceof DefaultShop) {
            initializeDefaultShop(gamePlayer, playerData, slot, game);
        } else {
            initializeRegularShop(gamePlayer, shopData, slot, game);
        }
    }

    /**
     * 初始化商店导航栏
     */
    private void initializeShopNavbar(Player player, int slot, Game game) {
        int i = 0;
        for (ShopData shopData : ItemShopManager.getShops()) {
            if (i > 9) {
                continue;
            }

            int finalI = i;
            setItem(i, new ItemBuilderUtil()
                    .setItemStack(shopData.getMainShopItem().getItemStack().clone())
                    .setDisplayName(shopData.getMainShopItem().getDisplayName())
                    .getItem(), 
                    new GUIAction(0, () -> {
                        if (finalI != slot) new ItemShopGUI(player, finalI, game).open();
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
                setItem(i, MaterialUtil.getStainedGlassPane(5), new GUIAction(0, () -> {}, false));
            } else {
                setItem(i, MaterialUtil.getStainedGlassPane(7), new GUIAction(0, () -> {}, false));
            }
        }
    }
    
    /**
     * 初始化默认商店(快捷购买)
     */
    private void initializeDefaultShop(GamePlayer gamePlayer, PlayerData playerData, int slot, Game game) {
        int itemIndex = -1;
        for (String shopItemCode : playerData.getShopSort()) {
            itemIndex++;
            
            // 解析商店物品代码
            String[] itemInfo = !shopItemCode.equals("AIR") ? shopItemCode.split("#") : null;
            ItemType itemType = findItemType(itemInfo);
            
            if (itemInfo == null || itemType == null) {
                // 设置空槽位
                setEmptySlot(gamePlayer.getPlayer(), SHOP_SLOTS[itemIndex], slot, game);
                continue;
            }
            
            // 设置有物品的槽位
            setItem(gamePlayer, slot, SHOP_SLOTS[itemIndex], game, itemType, -1, 
                    Arrays.asList("§7Shift+左键从快捷购买中移除", " "));
        }
    }
    
    /**
     * 设置空槽位
     */
    private void setEmptySlot(Player player, int slotPosition, int shopSlot, Game game) {
        setItem(slotPosition, 
                new ItemBuilderUtil()
                    .setItemStack(MaterialUtil.getStainedGlassPane(14))
                    .setDisplayName("§c空闲的槽位")
                    .setLores("§7这是一个快捷购买槽位!§bShift+左键", "§7将任意物品放到这里~")
                    .getItem(), 
                new NewGUIAction(0, event -> {
                    if (!event.getClick().isShiftClick()) return;
                    player.sendMessage("§c这是个空的槽位!请使用Shift+左键添加物品到这里~");
                }, false));
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
     * 初始化常规商店
     */
    private void initializeRegularShop(GamePlayer gamePlayer, ShopData shopData, int shopSlot, Game game) {
        int itemIndex = -1;
        for (ItemType itemType : shopData.getShopItems()) {
            itemIndex++;
            setItem(gamePlayer, shopSlot, SHOP_SLOTS[itemIndex], game, itemType, itemIndex, null);
        }
    }

    /**
     * 设置商店物品
     */
    public void setItem(GamePlayer gamePlayer, int shopSlot, int displaySlot, Game game, ItemType itemType, int itemSlot, List<String> moreLore) {
        Player player = gamePlayer.getPlayer();
        PlayerData playerData = gamePlayer.getPlayerData();
        ModeType modeType = playerData.getModeType();

        // 准备物品显示
        ItemBuilderUtil itemBuilder = prepareItemDisplay(gamePlayer, itemType);
        
        // 创建物品说明
        List<String> lore = createItemLore(itemType, modeType, moreLore);

        // 设置商店项
        super.setItem(displaySlot, 
            itemBuilder.setDisplayName("§c" + itemType.getDisplayName())
                      .setLores(lore)
                      .getItem(), 
            new NewGUIAction(0, event -> handleItemClick(event, gamePlayer, player, shopSlot, displaySlot, 
                                                       itemType, itemBuilder, itemSlot, playerData, game), false));
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
            case WOOD:
                itemBuilder.setType(MaterialUtil.STONE_PICKAXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.IRON_INGOT(), 20, 20));
                break;
            case STONE:
                itemBuilder.setType(MaterialUtil.IRON_PICKAXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.GOLD_INGOT(), 8, 24));
                break;
            case IRON:
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
            case WOOD:
                itemBuilder.setType(MaterialUtil.STONE_AXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.IRON_INGOT(), 20, 20));
                break;
            case STONE:
                itemBuilder.setType(MaterialUtil.IRON_AXE());
                itemType.setPriceCost(new PriceCost(MaterialUtil.GOLD_INGOT(), 8, 24));
                break;
            case IRON:
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
    
    /**
     * 创建物品说明
     */
    private List<String> createItemLore(ItemType itemType, ModeType modeType, List<String> moreLore) {
        List<String> lore = new ArrayList<>();
        lore.add("§7物品:");
        lore.add("§8•" + itemType.getDisplayName());
        lore.add(" ");
        
        // 添加额外说明
        if (moreLore != null && !moreLore.isEmpty()) {
            lore.addAll(moreLore);
        }
        
        // 添加价格说明
        Material priceMaterial = itemType.getPriceCost().getMaterial();
        String resourceName = RESOURCE_NAMES.getOrDefault(priceMaterial, "资源");
        
        if (modeType == ModeType.EXPERIENCE) {
            lore.add("§7花费: §3§l" + itemType.getPriceCost().getXp() + "级");
        } else {
            lore.add("§7花费: §3§l" + itemType.getPriceCost().getAmount() + " " + resourceName);
        }
        
        return lore;
    }
    
    /**
     * 处理物品点击
     */
    private void handleItemClick(org.bukkit.event.inventory.InventoryClickEvent event, GamePlayer gamePlayer, Player player, 
                                int shopSlot, int displaySlot, ItemType itemType, ItemBuilderUtil itemBuilder, 
                                int itemSlot, PlayerData playerData, Game game) {
        // 处理Shift+点击 (快捷购买相关操作)
        if (event.isShiftClick() || event.getClick().isShiftClick()) {
            handleShiftClick(player, gamePlayer, shopSlot, displaySlot, itemBuilder, itemSlot, playerData, game);
            return;
        }
        
        // 检查是否可以购买（工具已达最高级或已拥有）
        if (!canPurchaseItem(gamePlayer, itemType, itemBuilder.getItem().getType())) {
            return;
        }
        
        // 处理支付
        if (!processPayment(player, itemType, playerData.getModeType())) {
            return;
        }
        
        // 处理物品给予
        handleItemGiving(player, gamePlayer, shopSlot, itemType, itemBuilder, game);
    }
    
    /**
     * 处理Shift+点击
     */
    private void handleShiftClick(Player player, GamePlayer gamePlayer, int shopSlot, int displaySlot, 
                                  ItemBuilderUtil itemBuilder, int itemSlot, PlayerData playerData, Game game) {
        if (shopSlot == 0) {
            // 从快捷购买移除
            int slotIndex = Arrays.asList(SHOP_SLOTS).indexOf(displaySlot);
            if (slotIndex == -1) return;
            
            playerData.getShopSort()[slotIndex] = "AIR";
            playerData.saveShops();
            new ItemShopGUI(player, shopSlot, game).open();
        } else {
            // 添加到快捷购买
            new DIYShopGUI(game, gamePlayer, itemBuilder.getItem().clone(), 
                          ItemShopManager.getShops().get(shopSlot).getClass().getSimpleName() 
                          + "#" + (itemSlot + 1)).open();
        }
    }
    
    /**
     * 检查是否可以购买物品
     */
    private boolean canPurchaseItem(GamePlayer gamePlayer, ItemType itemType, Material itemMaterial) {
        // 检查工具类型和等级
        if (itemType.getColorType() == ColorType.PICKAXE && gamePlayer.getPickaxeType() == ToolType.DIAMOND) {
            return false;
        }
        
        if (itemType.getColorType() == ColorType.AXE && gamePlayer.getAxeType() == ToolType.DIAMOND) {
            return false;
        }
        
        // 检查剪刀
        if (MaterialUtil.SHEARS().equals(itemMaterial) && gamePlayer.isShear()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 处理支付
     */
    private boolean processPayment(Player player, ItemType itemType, ModeType modeType) {
        if (modeType == ModeType.DEFAULT) {
            // 默认模式：支付物品
            return processItemPayment(player, itemType);
        } else {
            // 经验模式：支付经验
            return processExperiencePayment(player, itemType);
        }
    }
    
    /**
     * 处理物品支付
     */
    private boolean processItemPayment(Player player, ItemType itemType) {
        Material paymentMaterial = itemType.getPriceCost().getMaterial();
        int requiredAmount = itemType.getPriceCost().getAmount();
        
        // 计算玩家拥有的资源总数
        int playerTotal = 0;
        ItemStack[] inventory = player.getInventory().getContents();
        
        for (ItemStack item : inventory) {
            if (item != null && item.getType().equals(paymentMaterial)) {
                playerTotal += item.getAmount();
            }
        }
        
        // 检查是否有足够资源
        if (playerTotal < requiredAmount) {
            SoundUtil.playEndermanTeleportSound(player);
            player.sendMessage("§c没有足够资源购买！");
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
                player.getInventory().setItem(i, item);
            }
        }
        
        SoundUtil.playItemPickupSound(player);
        return true;
    }
    
    /**
     * 处理经验支付
     */
    private boolean processExperiencePayment(Player player, ItemType itemType) {
        int requiredXp = itemType.getPriceCost().getXp();
        
        if (player.getLevel() < requiredXp) {
            SoundUtil.playEndermanTeleportSound(player);
            player.sendMessage("§c没有足够资源购买！");
            return false;
        }
        
        player.setLevel(player.getLevel() - requiredXp);
        SoundUtil.playItemPickupSound(player);
        return true;
    }
    
    /**
     * 处理物品给予
     */
    private void handleItemGiving(Player player, GamePlayer gamePlayer, int shopSlot, 
                                 ItemType itemType, ItemBuilderUtil itemBuilder, Game game) {
        Material material = itemBuilder.getItem().getType();
        
        // 处理护甲
        if (handleArmorGiving(player, gamePlayer, shopSlot, material, game)) {
            return;
        }
        
        // 处理工具
        if (handleToolGiving(player, gamePlayer, shopSlot, material, game)) {
            return;
        }
        
        // 处理普通物品
        handleRegularItemGiving(player, gamePlayer, itemType);
    }
    
    /**
     * 处理护甲给予
     */
    private boolean handleArmorGiving(Player player, GamePlayer gamePlayer, int shopSlot, Material material, Game game) {
        if (MaterialUtil.CHAINMAIL_BOOTS().equals(material)) {
            gamePlayer.setArmorType(ArmorType.CHAINMAIL);
            gamePlayer.giveArmor();
            player.updateInventory();
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        } else if (MaterialUtil.IRON_BOOTS().equals(material)) {
            gamePlayer.setArmorType(ArmorType.IRON);
            gamePlayer.giveArmor();
            player.updateInventory();
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        } else if (MaterialUtil.DIAMOND_BOOTS().equals(material)) {
            gamePlayer.setArmorType(ArmorType.DIAMOND);
            gamePlayer.giveArmor();
            player.updateInventory();
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        }
        return false;
    }
    
    /**
     * 处理工具给予
     */
    private boolean handleToolGiving(Player player, GamePlayer gamePlayer, int shopSlot, Material material, Game game) {
        // 镐
        if (MaterialUtil.WOODEN_PICKAXE().equals(material)) {
            gamePlayer.setPickaxeType(ToolType.WOOD);
            gamePlayer.givePickaxe(false);
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        } else if (MaterialUtil.STONE_PICKAXE().equals(material)) {
            gamePlayer.setPickaxeType(ToolType.STONE);
            gamePlayer.givePickaxe(true);
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        } else if (MaterialUtil.IRON_PICKAXE().equals(material)) {
            gamePlayer.setPickaxeType(ToolType.IRON);
            gamePlayer.givePickaxe(true);
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        } else if (MaterialUtil.DIAMOND_PICKAXE().equals(material)) {
            gamePlayer.setPickaxeType(ToolType.DIAMOND);
            gamePlayer.givePickaxe(true);
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        }
        
        // 斧
        else if (MaterialUtil.WOODEN_AXE().equals(material)) {
            gamePlayer.setAxeType(ToolType.WOOD);
            gamePlayer.giveAxe(false);
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        } else if (MaterialUtil.STONE_AXE().equals(material)) {
            gamePlayer.setAxeType(ToolType.STONE);
            gamePlayer.giveAxe(true);
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        } else if (MaterialUtil.IRON_AXE().equals(material)) {
            gamePlayer.setAxeType(ToolType.IRON);
            gamePlayer.giveAxe(true);
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        } else if (MaterialUtil.DIAMOND_AXE().equals(material)) {
            gamePlayer.setAxeType(ToolType.DIAMOND);
            gamePlayer.giveAxe(true);
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        }
        
        // 剪刀
        else if (MaterialUtil.SHEARS().equals(material)) {
            gamePlayer.setShear(true);
            gamePlayer.giveShear();
            new ItemShopGUI(player, shopSlot, game).open();
            return true;
        }
        
        return false;
    }
    
    /**
     * 处理普通物品给予
     */
    private void handleRegularItemGiving(Player player, GamePlayer gamePlayer, ItemType itemType) {
        ItemBuilderUtil itemBuilder = new ItemBuilderUtil().setItemStack(itemType.getItemStack().clone());
        
        // 处理剑特殊情况
        String itemTypeName = itemType.getItemStack().getType().toString();
        if (itemTypeName.endsWith("_SWORD") || itemTypeName.endsWith("SWORD")) {
            player.getInventory().remove(MaterialUtil.WOODEN_SWORD());
            
            // 添加锋利附魔
            if (gamePlayer.getGameTeam().isSharpenedSwords()) {
                Enchantment sharpness = EnchantmentUtil.DAMAGE_ALL();
                if (sharpness != null) {
                    itemBuilder.addEnchant(sharpness, 1);
                }
            }
        }
        
        // 处理有颜色的方块
        if (itemType.getColorType() == ColorType.COLOR) {
            // 处理羊毛
            if (MaterialUtil.isWool(itemType.getItemStack().getType())) {
                // 创建带颜色的羊毛
                ItemBuilderUtil woolBuilder = new ItemBuilderUtil();
                woolBuilder.setWoolColor(gamePlayer.getGameTeam().getDyeColor());
                woolBuilder.setAmount(itemType.getItemStack().getAmount());
                
                // 保留原始附魔（如果有）
                for (Map.Entry<Enchantment, Integer> entry : itemType.getItemStack().getEnchantments().entrySet()) {
                    woolBuilder.addEnchant(entry.getKey(), entry.getValue());
                }
                
                // 添加到玩家物品栏
                player.getInventory().addItem(woolBuilder.getItem());
                return; // 已经添加到物品栏，直接返回
            } else {
                // 对于非羊毛的颜色方块，使用旧方法
                itemBuilder.setDurability(MaterialUtil.getWoolData(gamePlayer.getGameTeam().getDyeColor()));
            }
        }
        
        // 将物品添加到玩家库存
        player.getInventory().addItem(itemBuilder.getItem());
    }
}
