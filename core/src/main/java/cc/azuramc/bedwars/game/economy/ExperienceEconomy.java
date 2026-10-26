package cc.azuramc.bedwars.game.economy;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.task.generator.GeneratorManager;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author an5w1r@163.com
 */
public class ExperienceEconomy implements EconomyMode {

    @Override
    public String getDisplayName() {
        return AzuraBedWars.getInstance().getDisplayConfig().getLobbyScoreboard().getExpMode();
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
        GeneratorConfig.Resource config = GeneratorManager.resourceOf(resource.getType());
        if (config == null) {
            return false;
        }

        Player player = gamePlayer.getPlayer();
        player.setLevel(player.getLevel() + config.getXpValue() * resource.getAmount());
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
            GeneratorConfig.Resource resource = item != null ? GeneratorManager.resourceOf(item.getType()) : null;
            if (resource != null) {
                levels += resource.getXpValue() * item.getAmount();
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
