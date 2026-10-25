package cc.azuramc.bedwars.game.economy;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.shop.PriceCost;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
public class ExperienceEconomy implements EconomyMode {

    private static final Map<Material, Integer> RESOURCE_LEVELS = new HashMap<>();

    static {
        RESOURCE_LEVELS.put(XMaterial.IRON_INGOT.get(), 1);
        RESOURCE_LEVELS.put(XMaterial.GOLD_INGOT.get(), 3);
        RESOURCE_LEVELS.put(XMaterial.DIAMOND.get(), 40);
        RESOURCE_LEVELS.put(XMaterial.EMERALD.get(), 80);
    }

    @Override
    public String getDisplayName() {
        return AzuraBedWars.getInstance().getSettingsConfig().getLobbyScoreboard().getExpMode();
    }

    @Override
    public boolean canAfford(Player player, PriceCost price) {
        return player.getLevel() >= price.xp();
    }

    @Override
    public void withdraw(Player player, PriceCost price) {
        player.setLevel(player.getLevel() - price.xp());
    }

    @Override
    public String formatPrice(PriceCost price) {
        return price.xp() + "级";
    }

    @Override
    public boolean deposit(GamePlayer gamePlayer, ItemStack resource) {
        Integer levels = RESOURCE_LEVELS.get(resource.getType());
        if (levels == null) {
            return false;
        }

        Player player = gamePlayer.getPlayer();
        player.setLevel(player.getLevel() + levels * resource.getAmount());
        if (XSound.ENTITY_PLAYER_LEVELUP.get() != null) {
            gamePlayer.playSound(XSound.ENTITY_PLAYER_LEVELUP.get(), 10, 15F);
        }
        return true;
    }

    @Override
    public void rewardKill(GamePlayer victim, GamePlayer killer) {
        Player victimPlayer = victim.getPlayer();
        int levels = victimPlayer.getLevel();
        // resources bought back from the convert shop
        for (ItemStack item : victimPlayer.getInventory().getContents()) {
            if (item != null && RESOURCE_LEVELS.containsKey(item.getType())) {
                levels += RESOURCE_LEVELS.get(item.getType()) * item.getAmount();
            }
        }
        if (levels == 0) {
            return;
        }

        Player killerPlayer = killer.getPlayer();
        killerPlayer.setLevel(killerPlayer.getLevel() + levels);
        victimPlayer.setLevel(0);
        killer.sendMessage("§e+" + levels + "级");
    }
}
