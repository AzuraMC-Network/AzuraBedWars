package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.wrapper.EnchantmentWrapper;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDropListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onGameDrop(PlayerDropItemEvent event) {
        if (gameManager.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (gameManager.getGameState() == GameState.RUNNING) {
            Player player = event.getPlayer();
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            ItemStack itemStack = event.getItemDrop().getItemStack();

            if (gamePlayer.isSpectator()) {
                event.setCancelled(true);
                return;
            }

            if (itemStack.getType().toString().endsWith("_HELMET") || itemStack.getType().toString().endsWith("_CHESTPLATE") || itemStack.getType().toString().endsWith("_LEGGINGS") || itemStack.getType().toString().endsWith("_BOOTS")) {
                event.setCancelled(true);
                return;
            }

            if (itemStack.getType().toString().endsWith("_AXE") || itemStack.getType().toString().endsWith("PICKAXE") || itemStack.getType() == Material.SHEARS) {
                event.setCancelled(true);
                return;
            }

            if (itemStack.getType().toString().endsWith("_SWORD")) {
                if (itemStack.getType() == MaterialWrapper.WOODEN_SWORD()) {
                    event.getItemDrop().remove();
                }

                itemStack.removeEnchantment(EnchantmentWrapper.DAMAGE_ALL());
                int size = 0;
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack itemStack1 = player.getInventory().getItem(i);
                    if (itemStack1 != null && itemStack1.getType().toString().endsWith("_SWORD")) {
                        size++;
                    }
                }

                if (size == 0) {
                    Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance(), () -> gamePlayer.giveSword(false), 8);
                }
            }
        }
    }
}
