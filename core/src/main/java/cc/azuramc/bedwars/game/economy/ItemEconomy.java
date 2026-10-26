package cc.azuramc.bedwars.game.economy;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.task.generator.GeneratorManager;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * @author an5w1r@163.com
 */
public class ItemEconomy implements EconomyMode {

    @Override
    public String getDisplayName() {
        return AzuraBedWars.getInstance().getDisplayConfig().getLobbyScoreboard().getDefaultMode();
    }

    @Override
    public boolean canAfford(Player player, PriceCost price) {
        Material material = material(price);
        return price.amount() <= 0 || material != null && countItems(player.getInventory(), material) >= price.amount();
    }

    @Override
    public void withdraw(Player player, PriceCost price) {
        Material material = material(price);
        ItemStack[] contents = player.getInventory().getContents();
        int remaining = price.amount();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != material) {
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
        GeneratorConfig.Resource resource = AzuraBedWars.getInstance().getGeneratorConfig().resource(price.currency());
        return price.amount() + " " + (resource != null ? resource.getName() : price.currency());
    }

    @Override
    public boolean deposit(GamePlayer gamePlayer, ItemStack resource) {
        GeneratorConfig.Resource config = GeneratorManager.resourceOf(resource.getType());
        if (config == null || config.getAt() != GeneratorConfig.Spawn.BASE) {
            return false;
        }

        gamePlayer.getPlayer().getInventory().addItem(new ItemStack(resource.getType(), resource.getAmount()));
        if (XSound.ENTITY_PLAYER_LEVELUP.get() != null) {
            gamePlayer.playSound(XSound.ENTITY_PLAYER_LEVELUP.get(), 10, 15F);
        }
        return true;
    }

    @Override
    public void rewardKill(GamePlayer victim, GamePlayer killer) {
        Inventory victimInventory = victim.getPlayer().getInventory();
        Player killerPlayer = killer.getPlayer();

        for (GeneratorConfig.Resource resource : AzuraBedWars.getInstance().getGeneratorConfig().getResources().values()) {
            Material material = GeneratorManager.material(resource);
            int amount = material != null ? countItems(victimInventory, material) : 0;
            if (amount == 0) {
                continue;
            }

            for (ItemStack leftover : killerPlayer.getInventory().addItem(new ItemStack(material, amount)).values()) {
                killerPlayer.getWorld().dropItemNaturally(killerPlayer.getLocation(), leftover);
            }
            killer.sendMessage("§e+" + amount + " " + resource.getName());
        }
    }

    @Nullable
    private static Material material(PriceCost price) {
        GeneratorConfig.Resource resource = AzuraBedWars.getInstance().getGeneratorConfig().resource(price.currency());
        return resource != null ? GeneratorManager.material(resource) : null;
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
