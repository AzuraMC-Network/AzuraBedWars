package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.economy.PriceCost;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public final class ShopPurchase {

    private ShopPurchase() {
    }

    /**
     * @return true when the item was paid for and given
     */
    public static boolean buy(GamePlayer gamePlayer, ShopItem item) {
        if (!canBuy(gamePlayer, item)) {
            return false;
        }
        if (!AzuraBedWars.getInstance().getGameManager().getEconomy().charge(gamePlayer, priceFor(gamePlayer, item))) {
            return false;
        }

        switch (item.getKind()) {
            case ARMOR:
                gamePlayer.setArmorType(item.getArmor());
                gamePlayer.giveArmor();
                break;
            case PICKAXE:
                ToolType pickaxe = nextTier(gamePlayer.getPickaxeType());
                gamePlayer.setPickaxeType(pickaxe);
                gamePlayer.givePickaxe(pickaxe != ToolType.WOOD);
                break;
            case AXE:
                ToolType axe = nextTier(gamePlayer.getAxeType());
                gamePlayer.setAxeType(axe);
                gamePlayer.giveAxe(axe != ToolType.WOOD);
                break;
            case SHEARS:
                gamePlayer.setShear(true);
                gamePlayer.giveShear();
                break;
            default:
                gamePlayer.getPlayer().getInventory().addItem(createItem(gamePlayer, item));
                break;
        }
        return true;
    }

    public static PriceCost priceFor(GamePlayer gamePlayer, ShopItem item) {
        switch (item.getKind()) {
            case PICKAXE:
                return toolPrice(nextTier(gamePlayer.getPickaxeType()));
            case AXE:
                return toolPrice(nextTier(gamePlayer.getAxeType()));
            default:
                return Objects.requireNonNull(item.getPrice());
        }
    }

    public static ItemStack displayIcon(GamePlayer gamePlayer, ShopItem item) {
        ItemStack icon = item.getIcon().clone();
        if (item.getKind() == ShopItemKind.PICKAXE) {
            icon.setType(pickaxe(nextTier(gamePlayer.getPickaxeType())));
        } else if (item.getKind() == ShopItemKind.AXE) {
            icon.setType(axe(nextTier(gamePlayer.getAxeType())));
        }
        return icon;
    }

    private static boolean canBuy(GamePlayer gamePlayer, ShopItem item) {
        switch (item.getKind()) {
            case ARMOR:
                return gamePlayer.getArmorType().ordinal() < Objects.requireNonNull(item.getArmor()).ordinal();
            case PICKAXE:
                if (gamePlayer.getPickaxeType() == ToolType.DIAMOND) {
                    return false;
                }
                break;
            case AXE:
                if (gamePlayer.getAxeType() == ToolType.DIAMOND) {
                    return false;
                }
                break;
            case SHEARS:
                if (gamePlayer.isShear()) {
                    return false;
                }
                break;
            default:
                break;
        }

        if (gamePlayer.getPlayer().getInventory().firstEmpty() == -1) {
            gamePlayer.sendMessage("§c背包已满！");
            return false;
        }
        return true;
    }

    private static ItemStack createItem(GamePlayer gamePlayer, ShopItem item) {
        ItemStack icon = item.getIcon();
        ItemBuilder builder = new ItemBuilder().setItemStack(icon.clone());
        if (!icon.getType().isBlock()) {
            builder.setDisplayName(item.getName());
        }

        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return builder.getItem();
        }

        if (item.getKind() == ShopItemKind.SWORD) {
            Player player = gamePlayer.getPlayer();
            player.getInventory().remove(Objects.requireNonNull(XMaterial.WOODEN_SWORD.get()));
            Enchantment sharpness = XEnchantment.SHARPNESS.get();
            if (gameTeam.getUpgradeManager().hasSharpnessUpgrade() && sharpness != null) {
                builder.addEnchant(sharpness, 1);
            }
        } else if (item.getKind() == ShopItemKind.TEAM_COLORED) {
            String type = icon.getType().name();
            if (type.contains("WOOL")) {
                builder.setWoolColor(gameTeam.getDyeColor()).setAmount(icon.getAmount());
                for (Map.Entry<Enchantment, Integer> enchant : icon.getEnchantments().entrySet()) {
                    builder.addEnchant(enchant.getKey(), enchant.getValue());
                }
            } else if (type.contains("GLASS")) {
                builder.setGlassColor(gameTeam.getDyeColor()).setAmount(icon.getAmount());
            } else {
                builder.setDurability(gameTeam.getDyeColor().getDyeData());
            }
        }
        return builder.getItem();
    }

    private static ToolType nextTier(ToolType current) {
        switch (current) {
            case NONE:
                return ToolType.WOOD;
            case WOOD:
                return ToolType.STONE;
            case STONE:
                return ToolType.IRON;
            default:
                return ToolType.DIAMOND;
        }
    }

    private static PriceCost toolPrice(ToolType tier) {
        switch (tier) {
            case WOOD:
                return new PriceCost(XMaterial.IRON_INGOT.get(), 10, 10);
            case STONE:
                return new PriceCost(XMaterial.IRON_INGOT.get(), 20, 20);
            case IRON:
                return new PriceCost(XMaterial.GOLD_INGOT.get(), 8, 24);
            default:
                return new PriceCost(XMaterial.GOLD_INGOT.get(), 12, 36);
        }
    }

    private static Material pickaxe(ToolType tier) {
        switch (tier) {
            case WOOD:
                return XMaterial.WOODEN_PICKAXE.get();
            case STONE:
                return XMaterial.STONE_PICKAXE.get();
            case IRON:
                return XMaterial.IRON_PICKAXE.get();
            default:
                return XMaterial.DIAMOND_PICKAXE.get();
        }
    }

    private static Material axe(ToolType tier) {
        switch (tier) {
            case WOOD:
                return XMaterial.WOODEN_AXE.get();
            case STONE:
                return XMaterial.STONE_AXE.get();
            case IRON:
                return XMaterial.IRON_AXE.get();
            default:
                return XMaterial.DIAMOND_AXE.get();
        }
    }
}
