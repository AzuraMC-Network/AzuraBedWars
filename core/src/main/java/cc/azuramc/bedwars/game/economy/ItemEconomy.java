package cc.azuramc.bedwars.game.economy;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public class ItemEconomy implements EconomyMode {

    private static final Map<Material, String> RESOURCE_NAMES = new LinkedHashMap<>();

    static {
        RESOURCE_NAMES.put(XMaterial.IRON_INGOT.get(), "铁");
        RESOURCE_NAMES.put(XMaterial.GOLD_INGOT.get(), "金");
        RESOURCE_NAMES.put(XMaterial.DIAMOND.get(), "钻石");
        RESOURCE_NAMES.put(XMaterial.EMERALD.get(), "绿宝石");
    }

    @Override
    public String getDisplayName() {
        return AzuraBedWars.getInstance().getSettingsConfig().getLobbyScoreboard().getDefaultMode();
    }

    @Override
    public boolean canAfford(Player player, PriceCost price) {
        return countItems(player.getInventory(), price.material()) >= price.amount();
    }

    @Override
    public void withdraw(Player player, PriceCost price) {
        ItemStack[] contents = player.getInventory().getContents();
        int remaining = price.amount();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != price.material()) {
                continue;
            }
            if (item.getAmount() > remaining) {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            } else {
                remaining -= item.getAmount();
                item.setAmount(0);
            }
            player.getInventory().setItem(i, item);
        }
    }

    @Override
    public String formatPrice(PriceCost price) {
        return price.amount() + " " + RESOURCE_NAMES.getOrDefault(price.material(), "资源");
    }

    @Override
    public boolean deposit(GamePlayer gamePlayer, ItemStack resource) {
        Material type = resource.getType();
        if (type != XMaterial.IRON_INGOT.get() && type != XMaterial.GOLD_INGOT.get()) {
            return false;
        }

        gamePlayer.getPlayer().getInventory().addItem(new ItemStack(type, resource.getAmount()));
        if (XSound.ENTITY_PLAYER_LEVELUP.get() != null) {
            gamePlayer.playSound(XSound.ENTITY_PLAYER_LEVELUP.get(), 10, 15F);
        }
        return true;
    }

    @Override
    public void rewardKill(GamePlayer victim, GamePlayer killer) {
        Inventory victimInventory = victim.getPlayer().getInventory();
        Player killerPlayer = killer.getPlayer();

        for (Map.Entry<Material, String> resource : RESOURCE_NAMES.entrySet()) {
            int amount = countItems(victimInventory, resource.getKey());
            if (amount == 0) {
                continue;
            }

            for (ItemStack leftover : killerPlayer.getInventory().addItem(new ItemStack(resource.getKey(), amount)).values()) {
                killerPlayer.getWorld().dropItemNaturally(killerPlayer.getLocation(), leftover);
            }
            killer.sendMessage("§e+" + amount + " " + resource.getValue());
        }
    }

    private static int countItems(Inventory inventory, Material material) {
        int total = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        return total;
    }
}
