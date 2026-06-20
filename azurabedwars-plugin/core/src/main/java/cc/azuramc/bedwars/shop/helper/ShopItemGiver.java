package cc.azuramc.bedwars.shop.helper;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.item.armor.ArmorType;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import cc.azuramc.bedwars.shop.ColorType;
import cc.azuramc.bedwars.shop.ShopItemType;
import cc.azuramc.bedwars.shop.gui.ItemShopGUI;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 商店物品给予处理类
 * 负责处理物品购买后的给予逻辑
 *
 * @author an5w1r@163.com
 */
public final class ShopItemGiver {

    /**
     * 工具给予处理器映射
     */
    private static final Map<Material, ToolGiveAction> TOOL_HANDLERS = new EnumMap<>(Material.class);

    static {
        // 镐子处理器
        registerToolHandler(XMaterial.WOODEN_PICKAXE, (gp, replace) -> {
            gp.setPickaxeType(ToolType.WOOD);
            gp.givePickaxe(replace);
        }, false);
        registerToolHandler(XMaterial.STONE_PICKAXE, (gp, replace) -> {
            gp.setPickaxeType(ToolType.STONE);
            gp.givePickaxe(replace);
        }, true);
        registerToolHandler(XMaterial.IRON_PICKAXE, (gp, replace) -> {
            gp.setPickaxeType(ToolType.IRON);
            gp.givePickaxe(replace);
        }, true);
        registerToolHandler(XMaterial.DIAMOND_PICKAXE, (gp, replace) -> {
            gp.setPickaxeType(ToolType.DIAMOND);
            gp.givePickaxe(replace);
        }, true);

        // 斧子处理器
        registerToolHandler(XMaterial.WOODEN_AXE, (gp, replace) -> {
            gp.setAxeType(ToolType.WOOD);
            gp.giveAxe(replace);
        }, false);
        registerToolHandler(XMaterial.STONE_AXE, (gp, replace) -> {
            gp.setAxeType(ToolType.STONE);
            gp.giveAxe(replace);
        }, true);
        registerToolHandler(XMaterial.IRON_AXE, (gp, replace) -> {
            gp.setAxeType(ToolType.IRON);
            gp.giveAxe(replace);
        }, true);
        registerToolHandler(XMaterial.DIAMOND_AXE, (gp, replace) -> {
            gp.setAxeType(ToolType.DIAMOND);
            gp.giveAxe(replace);
        }, true);

        // 剪刀处理器
        registerToolHandler(XMaterial.SHEARS, (gp, replace) -> {
            gp.setShear(true);
            gp.giveShear();
        }, false);
    }

    private ShopItemGiver() {
        // 工具类不允许实例化
    }

    /**
     * 注册工具处理器
     */
    private static void registerToolHandler(XMaterial material, BiConsumer<GamePlayer, Boolean> action, boolean replace) {
        Material mat = material.get();
        if (mat != null) {
            TOOL_HANDLERS.put(mat, new ToolGiveAction(action, replace));
        }
    }

    /**
     * 处理物品给予
     *
     * @param gamePlayer   游戏玩家
     * @param shopSlot     商店槽位
     * @param shopItemType 商店物品类型
     * @param itemBuilder  物品构建器
     * @param gameManager  游戏管理器
     */
    public static void handleItemGiving(GamePlayer gamePlayer, int shopSlot, ShopItemType shopItemType,
                                        ItemBuilder itemBuilder, GameManager gameManager) {
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
    public static boolean handleArmorGiving(GamePlayer gamePlayer, int shopSlot, Material material, GameManager gameManager) {
        ArmorType armorType = null;

        if (XMaterial.CHAINMAIL_BOOTS.get() == material) {
            armorType = ArmorType.CHAINMAIL;
        } else if (XMaterial.IRON_BOOTS.get() == material) {
            armorType = ArmorType.IRON;
        } else if (XMaterial.DIAMOND_BOOTS.get() == material) {
            armorType = ArmorType.DIAMOND;
        }

        if (armorType != null) {
            gamePlayer.setArmorType(armorType);
            gamePlayer.giveArmor();
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        }

        return false;
    }

    /**
     * 处理工具给予（使用 Map 替代 if-else 链）
     */
    public static boolean handleToolGiving(GamePlayer gamePlayer, int shopSlot, Material material, GameManager gameManager) {
        ToolGiveAction action = TOOL_HANDLERS.get(material);
        if (action != null) {
            action.action.accept(gamePlayer, action.replace);
            new ItemShopGUI(gamePlayer, shopSlot, gameManager).open();
            return true;
        }
        return false;
    }

    /**
     * 处理普通物品给予
     */
    public static void handleRegularItemGiving(GamePlayer gamePlayer, ShopItemType shopItemType) {
        ItemBuilder itemBuilder = new ItemBuilder().setItemStack(shopItemType.getItemStack().clone());
        // 如果物品不可放置则为其命名
        if (!shopItemType.getItemStack().getType().isBlock()) {
            itemBuilder.setDisplayName(shopItemType.getDisplayName());
        }
        Player player = gamePlayer.getPlayer();

        // 处理剑特殊情况
        handleSwordSpecialCase(gamePlayer, shopItemType, itemBuilder, player);

        // 处理有颜色的方块
        handleColoredBlock(gamePlayer, shopItemType, itemBuilder);

        // 将物品添加到玩家库存
        player.getInventory().addItem(itemBuilder.getItem());
        LoggerUtil.debug("item: " + itemBuilder.getItem().getType().name());
    }

    /**
     * 处理剑的特殊情况
     */
    private static void handleSwordSpecialCase(GamePlayer gamePlayer, ShopItemType shopItemType,
                                               ItemBuilder itemBuilder, Player player) {
        String itemTypeName = shopItemType.getItemStack().getType().name();
        if (itemTypeName.endsWith("_SWORD") || itemTypeName.endsWith("SWORD")) {
            if (XMaterial.WOODEN_SWORD.get() != null) {
                player.getInventory().remove(XMaterial.WOODEN_SWORD.get());
            }

            // 添加锋利附魔
            GameTeam gameTeam = gamePlayer.getGameTeam();
            if (gameTeam == null) {
                return;
            }
            if (gameTeam.getUpgradeManager().hasSharpnessUpgrade()) {
                Enchantment sharpness = XEnchantment.SHARPNESS.get();
                if (sharpness != null) {
                    itemBuilder.addEnchant(sharpness, 1);
                }
            }
        }
    }

    /**
     * 处理有颜色的方块
     */
    private static void handleColoredBlock(GamePlayer gamePlayer, ShopItemType shopItemType, ItemBuilder itemBuilder) {
        if (shopItemType.getColorType() != ColorType.COLOR) {
            return;
        }

        String typeName = shopItemType.getItemStack().getType().name();

        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }

        if (typeName.contains("WOOL")) {
            // 处理羊毛
            itemBuilder.setWoolColor(gameTeam.getDyeColor());
            itemBuilder.setAmount(shopItemType.getItemStack().getAmount());

            // 保留原始附魔（如果有）
            for (Map.Entry<Enchantment, Integer> entry : shopItemType.getItemStack().getEnchantments().entrySet()) {
                itemBuilder.addEnchant(entry.getKey(), entry.getValue());
            }
        } else if (typeName.contains("GLASS")) {
            // 处理玻璃
            itemBuilder.setGlassColor(gameTeam.getDyeColor());
            itemBuilder.setAmount(shopItemType.getItemStack().getAmount());
        } else {
            // 对于其他颜色方块，使用旧方法
            itemBuilder.setDurability(gameTeam.getDyeColor().getDyeData());
        }
    }

    /**
     * 工具给予动作封装
     */
    private record ToolGiveAction(BiConsumer<GamePlayer, Boolean> action, boolean replace) {
    }
}
