package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.economy.EconomyType;
import cc.azuramc.bedwars.game.economy.PriceCost;
import cc.azuramc.bedwars.game.item.armor.ArmorType;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public final class ShopCatalog {

    private static final List<ShopCategory> CATEGORIES = new ArrayList<>();

    private ShopCatalog() {
    }

    public static void init(EconomyType economyType) {
        CATEGORIES.clear();

        CATEGORIES.add(new ShopCategory("DefaultShopPage", icon(XMaterial.NETHER_STAR), "§b快速购买", Arrays.asList(
                colored(new ItemBuilder().setType(XMaterial.WHITE_WOOL.get()).setAmount(16), "羊毛", iron(4, 4)),
                sword(XMaterial.STONE_SWORD, "石剑", iron(10, 10)),
                armor(ArmorType.CHAINMAIL, XMaterial.CHAINMAIL_BOOTS, "锁链装备§7（死亡不掉落）", iron(40, 40)),
                item(new ItemBuilder().setType(XMaterial.GOLDEN_APPLE.get()), "金苹果", gold(3, 15)),
                item(equipment(XMaterial.BOW), "弓", gold(12, 60)),
                item(potion(XPotion.SPEED.get(), 600, 1), "速度药水§7（30秒）", emerald(1, 100)),
                item(new ItemBuilder().setType(XMaterial.TNT.get()), "TNT", gold(4, 40)),
                item(new ItemBuilder().setType(XMaterial.OAK_PLANKS.get()).setAmount(16), "橡木板", gold(4, 20)),
                sword(XMaterial.IRON_SWORD, "铁剑", gold(7, 35)),
                armor(ArmorType.IRON, XMaterial.IRON_BOOTS, "铁装备§7（死亡不掉落）", gold(12, 60)),
                item(new ItemBuilder().setType(XMaterial.FIRE_CHARGE.get()), "火球", iron(50, 50)),
                item(new ItemBuilder().setType(XMaterial.ARROW.get()).setAmount(8), "箭", iron(8, 10)),
                item(potion(XPotion.JUMP_BOOST.get(), 600, 1), "跳跃药水§7（30秒）", emerald(1, 100)),
                item(new ItemBuilder().setType(XMaterial.WATER_BUCKET.get()), "水桶", emerald(1, 100))
        )));

        CATEGORIES.add(new ShopCategory("BlockShopPage", icon(XMaterial.TERRACOTTA), "§a方块", Arrays.asList(
                colored(new ItemBuilder().setType(XMaterial.WHITE_WOOL.get()).setAmount(16), "羊毛", iron(4, 4)),
                colored(new ItemBuilder().setType(XMaterial.WHITE_WOOL.get()).setAmount(4).addEnchant(XEnchantment.EFFICIENCY.get(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS), "火速羊毛", iron(4, 4)),
                colored(new ItemBuilder().setType(XMaterial.TERRACOTTA.get()).setAmount(16), "硬化粘土", iron(12, 12)),
                colored(new ItemBuilder().setType(XMaterial.GLASS.get()).setAmount(4), "防爆玻璃", iron(12, 12)),
                item(new ItemBuilder().setType(XMaterial.END_STONE.get()).setAmount(12), "末地石", iron(24, 24)),
                item(new ItemBuilder().setType(XMaterial.LADDER.get()).setAmount(16), "梯子", iron(4, 4)),
                item(new ItemBuilder().setType(XMaterial.OAK_PLANKS.get()).setAmount(16), "橡木板", gold(4, 20)),
                item(new ItemBuilder().setType(XMaterial.SLIME_BLOCK.get()).setAmount(4), "史莱姆块", gold(2, 10)),
                item(new ItemBuilder().setType(XMaterial.COBWEB.get()).setAmount(2), "蜘蛛网", gold(4, 20)),
                item(new ItemBuilder().setType(XMaterial.OBSIDIAN.get()).setAmount(4), "黑曜石", emerald(4, 400))
        )));

        CATEGORIES.add(new ShopCategory("SwordShopPage", icon(XMaterial.GOLDEN_SWORD), "§a近战武器", Arrays.asList(
                sword(XMaterial.STONE_SWORD, "石剑", iron(10, 10)),
                sword(XMaterial.IRON_SWORD, "铁剑", gold(7, 35)),
                sword(XMaterial.DIAMOND_SWORD, "钻石剑", emerald(4, 400)),
                item(new ItemBuilder().setType(XMaterial.STICK.get()).addEnchant(XEnchantment.KNOCKBACK.get(), 1), "击退棒", gold(10, 150))
        )));

        CATEGORIES.add(new ShopCategory("ArmorShopPage", icon(XMaterial.CHAINMAIL_BOOTS), "§a盔甲", Arrays.asList(
                armor(ArmorType.CHAINMAIL, XMaterial.CHAINMAIL_BOOTS, "锁链装备§7（死亡不掉落）", iron(40, 40)),
                armor(ArmorType.IRON, XMaterial.IRON_BOOTS, "铁装备§7（死亡不掉落）", gold(12, 60)),
                armor(ArmorType.DIAMOND, XMaterial.DIAMOND_BOOTS, "钻石装备§7（死亡不掉落）", emerald(6, 600))
        )));

        CATEGORIES.add(new ShopCategory("ToolShopPage", icon(XMaterial.STONE_PICKAXE), "§a工具", Arrays.asList(
                new ShopItem(equipment(XMaterial.WOODEN_PICKAXE).addEnchant(XEnchantment.EFFICIENCY.get(), 1).getItem(), "镐子", ShopItemKind.PICKAXE, null, null),
                new ShopItem(equipment(XMaterial.WOODEN_AXE).addEnchant(XEnchantment.EFFICIENCY.get(), 1).getItem(), "斧子", ShopItemKind.AXE, null, null),
                new ShopItem(equipment(XMaterial.SHEARS).getItem(), "剪刀", ShopItemKind.SHEARS, null, iron(30, 30))
        )));

        CATEGORIES.add(new ShopCategory("BowShopPage", icon(XMaterial.BOW), "§a远程武器", Arrays.asList(
                item(new ItemBuilder().setType(XMaterial.ARROW.get()).setAmount(8), "箭 x8", gold(2, 10)),
                item(equipment(XMaterial.BOW), "弓", gold(12, 30)),
                item(equipment(XMaterial.BOW).addEnchant(XEnchantment.POWER.get(), 1), "弓（力量I）", gold(24, 60)),
                item(equipment(XMaterial.BOW).addEnchant(XEnchantment.POWER.get(), 1).addEnchant(XEnchantment.KNOCKBACK.get(), 1), "弓（力量I,冲击I）", emerald(6, 600))
        )));

        CATEGORIES.add(new ShopCategory("PotionShopPage", icon(XMaterial.BREWING_STAND), "§a药水", Arrays.asList(
                item(potion(XPotion.SPEED.get(), 900, 2), "速度药水§7（45秒）", emerald(1, 100)),
                item(potion(XPotion.JUMP_BOOST.get(), 900, 5), "跳跃药水§7（45秒）", emerald(1, 100)),
                item(potion(XPotion.INVISIBILITY.get(), 600, 1), "隐身药水§7（30秒）", emerald(2, 200))
        )));

        CATEGORIES.add(new ShopCategory("UtilityShopPage", icon(XMaterial.TNT), "§a实用道具", Arrays.asList(
                item(new ItemBuilder().setType(XMaterial.FIRE_CHARGE.get()), "火球", iron(50, 50)),
                item(new ItemBuilder().setType(XMaterial.TNT.get()), "TNT", gold(4, 40)),
                item(new ItemBuilder().setType(XMaterial.ENDER_PEARL.get()), "末影珍珠", emerald(4, 400)),
                item(new ItemBuilder().setType(XMaterial.WATER_BUCKET.get()), "水桶", emerald(1, 100)),
                item(new ItemBuilder().setType(XMaterial.BLAZE_ROD.get()).setLores(" ", "§f在空中使用该平台", "§f将在脚下生成一块临时史莱姆方块平台救生!"), "救援平台", emerald(1, 80)),
                item(new ItemBuilder().setType(XMaterial.GOLDEN_APPLE.get()), "金苹果", gold(3, 15)),
                item(new ItemBuilder().setType(XMaterial.EGG.get()), "搭桥蛋", emerald(1, 100)),
                item(new ItemBuilder().setType(XMaterial.SNOWBALL.get()), "蠹虫", iron(40, 40)),
                item(new ItemBuilder().setType(XMaterial.WOLF_SPAWN_EGG.get()).setSpawnEggType(EntityType.WOLF), "梦幻守护者", iron(128, 128)),
                item(new ItemBuilder().setType(XMaterial.CHEST.get()), "速建防御塔", iron(64, 64)),
                item(new ItemBuilder().setType(XMaterial.MILK_BUCKET.get()), "魔法牛奶", gold(8, 80))
        )));

        if (economyType == EconomyType.EXPERIENCE) {
            CATEGORIES.add(new ShopCategory("ConvertShopPage", icon(XMaterial.EXPERIENCE_BOTTLE), "§a经验转换", Arrays.asList(
                    item(new ItemBuilder().setType(XMaterial.IRON_INGOT.get()), "铁锭", levels(1)),
                    item(new ItemBuilder().setType(XMaterial.GOLD_INGOT.get()), "金锭", levels(5)),
                    item(new ItemBuilder().setType(XMaterial.DIAMOND.get()), "钻石", levels(50)),
                    item(new ItemBuilder().setType(XMaterial.EMERALD.get()), "绿宝石", levels(100))
            )));
        }
    }

    @NotNull
    public static List<ShopCategory> getCategories() {
        return Collections.unmodifiableList(CATEGORIES);
    }

    @NotNull
    public static ShopCategory getQuickBuy() {
        return CATEGORIES.get(0);
    }

    @Nullable
    public static ShopItem find(String categoryId, int index) {
        for (ShopCategory category : CATEGORIES) {
            if (category.getId().equals(categoryId)) {
                return index >= 0 && index < category.getItems().size() ? category.getItems().get(index) : null;
            }
        }
        return null;
    }

    private static ItemStack icon(XMaterial material) {
        return new ItemBuilder().setType(material.get()).setLores("§e点击查看！").addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem();
    }

    private static ItemBuilder equipment(XMaterial material) {
        return new ItemBuilder().setType(material.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
    }

    private static ItemBuilder potion(PotionEffectType type, int ticks, int amplifier) {
        return new ItemBuilder().setType(XMaterial.POTION.get()).setPotionData(new PotionEffect(Objects.requireNonNull(type), ticks, amplifier));
    }

    private static ShopItem item(ItemBuilder icon, String name, PriceCost price) {
        return new ShopItem(icon.getItem(), name, ShopItemKind.ITEM, null, price);
    }

    private static ShopItem colored(ItemBuilder icon, String name, PriceCost price) {
        return new ShopItem(icon.getItem(), name, ShopItemKind.TEAM_COLORED, null, price);
    }

    private static ShopItem sword(XMaterial material, String name, PriceCost price) {
        return new ShopItem(equipment(material).getItem(), name, ShopItemKind.SWORD, null, price);
    }

    private static ShopItem armor(ArmorType armor, XMaterial boots, String name, PriceCost price) {
        return new ShopItem(equipment(boots).getItem(), name, ShopItemKind.ARMOR, armor, price);
    }

    private static PriceCost iron(int amount, int xp) {
        return new PriceCost(XMaterial.IRON_INGOT.get(), amount, xp);
    }

    private static PriceCost gold(int amount, int xp) {
        return new PriceCost(XMaterial.GOLD_INGOT.get(), amount, xp);
    }

    private static PriceCost emerald(int amount, int xp) {
        return new PriceCost(XMaterial.EMERALD.get(), amount, xp);
    }

    /** Material unused since this page exists only in experience mode */
    private static PriceCost levels(int xp) {
        return new PriceCost(XMaterial.OAK_BUTTON.get(), 1, xp);
    }
}
