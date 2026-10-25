package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.shop.ItemSpec;
import cc.azuramc.bedwars.game.GameTeam;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XItemFlag;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Turns {@link ItemSpec} into items and checks its names against the running server
 *
 * @author an5w1r@163.com
 */
public final class ItemSpecs {

    public static final SpecChecks.Names NAMES = new SpecChecks.Names() {
        @Override
        public boolean material(String name) {
            return ItemSpecs.material(name) != null;
        }

        @Override
        public boolean enchantment(String name) {
            return XEnchantment.of(name).map(XEnchantment::get).isPresent();
        }

        @Override
        public boolean effect(String name) {
            return XPotion.of(name).map(XPotion::get).isPresent();
        }

        @Override
        public boolean flag(String name) {
            return XItemFlag.of(name).map(XItemFlag::get).isPresent();
        }

        @Override
        public boolean entity(String name) {
            return XEntityType.of(name).map(XEntityType::get).isPresent();
        }

        @Override
        public boolean sound(String name) {
            return XSound.of(name).map(XSound::get).isPresent();
        }
    };

    private ItemSpecs() {
    }

    @Nullable
    public static Material material(String name) {
        return XMaterial.matchXMaterial(name).map(XMaterial::get).orElse(null);
    }

    /**
     * Names this server does not know are left out, the validator already reported them
     *
     * @param shopName display name of items that are not blocks, unless the spec has its own
     * @return null when the material does not exist on this server
     */
    @Nullable
    public static ItemStack build(ItemSpec spec, @Nullable String shopName) {
        ItemStack base = XMaterial.matchXMaterial(spec.getMaterial()).map(XMaterial::parseItem).orElse(null);
        if (base == null) {
            return null;
        }
        ItemBuilder builder = new ItemBuilder().setItemStack(base).setAmount(spec.getAmount());
        if (spec.getSpawnEgg() != null) {
            EntityType type = XEntityType.of(spec.getSpawnEgg()).map(XEntityType::get).orElse(null);
            if (type != null) {
                builder.setSpawnEggType(type);
            }
        }
        if (spec.getPotion() != null) {
            ItemSpec.Potion potion = spec.getPotion();
            PotionEffectType effect = XPotion.of(potion.getEffect()).map(XPotion::get).orElse(null);
            if (effect != null) {
                builder.setPotionData(new PotionEffect(effect, (int) Math.round(potion.getSeconds() * 20), potion.getLevel() - 1));
            }
        }
        String name = spec.getName() != null ? spec.getName() : base.getType().isBlock() ? null : shopName;
        if (name != null) {
            builder.setDisplayName(name);
        }
        if (!spec.getLore().isEmpty()) {
            builder.setLores(spec.getLore());
        }
        for (Map.Entry<String, Integer> enchant : spec.getEnchants().entrySet()) {
            Enchantment enchantment = XEnchantment.of(enchant.getKey()).map(XEnchantment::get).orElse(null);
            if (enchantment != null) {
                builder.addEnchant(enchantment, enchant.getValue());
            }
        }
        for (String flag : spec.getFlags()) {
            ItemFlag itemFlag = XItemFlag.of(flag).map(XItemFlag::get).orElse(null);
            if (itemFlag != null) {
                builder.addItemFlag(itemFlag);
            }
        }
        if (spec.isUnbreakable()) {
            builder.setUnbreakable(true, true);
        }
        return builder.getItem();
    }

    /**
     * Swaps a colorable material for the team's color, keeping amount, meta and tags
     */
    public static ItemStack teamColored(ItemStack item, GameTeam gameTeam) {
        if (item.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(gameTeam.getColor());
            ItemStack colored = item.clone();
            colored.setItemMeta(meta);
            return colored;
        }
        String base = XMaterial.matchXMaterial(item).name();
        for (DyeColor color : DyeColor.values()) {
            if (base.startsWith(color.name() + "_")) {
                base = base.substring(color.name().length() + 1);
                break;
            }
        }
        if (base.startsWith("GLASS")) {
            base = "STAINED_" + base;
        }
        XMaterial target = XMaterial.matchXMaterial(gameTeam.getDyeColor().name() + "_" + base).orElse(null);
        ItemStack colored = target != null ? target.parseItem() : null;
        if (colored == null) {
            return item.clone();
        }
        colored.setAmount(item.getAmount());
        if (item.hasItemMeta()) {
            colored.setItemMeta(item.getItemMeta());
        }
        return colored;
    }
}
