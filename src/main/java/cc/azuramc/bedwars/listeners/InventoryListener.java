package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final Game game = AzuraBedWars.getInstance().getGame();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (gamePlayer == null) {
            return;
        }

        if (!isToolOrSword(getUIItem(event))) {
            return;
        }

        if (gamePlayer.isChestOpening()) {
            event.setCancelled(true);
            return;
        }

        if (gamePlayer.isEnderChestOpening()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        InventoryType inventoryType = event.getInventory().getType();
        switch (inventoryType) {
            case PLAYER:
                gamePlayer.setViewingInventory(true);
                break;
            case CHEST:
                gamePlayer.setViewingChest(true);
                break;
            case ENDER_CHEST:
                gamePlayer.setViewingEnderChest(true);
            default:
                break;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        InventoryType inventoryType = event.getInventory().getType();
        switch (inventoryType) {
            case PLAYER:
                gamePlayer.setViewingInventory(false);
                break;
            case CHEST:
                gamePlayer.setViewingChest(false);
                break;
            case ENDER_CHEST:
                gamePlayer.setViewingEnderChest(false);
            default:
                break;
        }
    }

    private ItemStack getUIItem(InventoryClickEvent event) {
        if (event.getCurrentItem() != null) return event.getCurrentItem();
        if (event.getCursor() != null) return event.getCursor();
        return null;
    }

    public static boolean isToolOrSword(ItemStack item) {
        return item != null
                && (item.getType().name().endsWith("_PICKAXE")
                || item.getType().name().endsWith("_AXE")
                || item.getType().name().endsWith("_SWORD")
                || item.getType() == Material.SHEARS);
    }
}
