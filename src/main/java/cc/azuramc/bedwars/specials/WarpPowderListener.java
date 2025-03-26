package cc.azuramc.bedwars.specials;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class WarpPowderListener implements Listener {
    private final Game game = AzuraBedWars.getInstance().getGame();

    private WarpPowder getActiveWarpPowder(Game game, GamePlayer gamePlayer) {
        for (SpecialItem item : game.getSpecialItems()) {
            if (item instanceof WarpPowder) {
                WarpPowder powder = (WarpPowder) item;
                if (powder.getPlayer().equals(gamePlayer)) {
                    return powder;
                }
            }
        }

        return null;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (gamePlayer.isSpectator()) {
            return;
        }

        WarpPowder powder = null;
        for (SpecialItem item : game.getSpecialItems()) {
            if (!(item instanceof WarpPowder)) {
                continue;
            }

            powder = (WarpPowder) item;
            if (!powder.getPlayer().equals(gamePlayer)) {
                powder = null;
                continue;
            }
            break;
        }

        if (powder != null) {
            powder.cancelTeleport(true, true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (game.getGameState() == GameState.RUNNING) {
            event.getItemDrop().getItemStack().getItemMeta().getDisplayName();
            if (event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equals("§4取消传送")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        WarpPowder warpPowder = new WarpPowder();
        if (!event.getMaterial().equals(warpPowder.getItemMaterial()) && !event.getMaterial().equals(warpPowder.getActivatedMaterial())) {
            return;
        }

        WarpPowder powder = this.getActiveWarpPowder(game, gamePlayer);

        if (event.getMaterial().equals(warpPowder.getActivatedMaterial())) {
            event.getItem().getItemMeta().getDisplayName();
            if (!event.getItem().getItemMeta().getDisplayName().equals("§4取消传送")) {
                return;
            }

            if (powder != null) {
                powder.setStackAmount(powder.getStack().getAmount() + 1);

                player.updateInventory();
                powder.cancelTeleport(true, true);
                event.setCancelled(true);
            }

            return;
        }

        if (powder != null) {
            player.sendMessage("§c你已经开始了一个传送!");
            return;
        }

        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            return;
        }

        warpPowder.setPlayer(gamePlayer);
        warpPowder.runTask();
        event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        WarpPowder powder = null;
        for (SpecialItem item : game.getSpecialItems()) {
            if (!(item instanceof WarpPowder)) {
                continue;
            }

            powder = (WarpPowder) item;
            if (powder.getPlayer().equals(gamePlayer)) {
                break;
            }

            powder = null;
        }

        if (powder != null) {
            powder.setStackAmount(powder.getStack().getAmount() + 1);
            player.updateInventory();
            powder.cancelTeleport(true, true);
        }
    }

}
