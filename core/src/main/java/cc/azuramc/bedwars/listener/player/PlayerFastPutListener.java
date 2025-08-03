package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.ToolSetUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * @author ant1aura@qq.com
 */
public class PlayerFastPutListener implements Listener {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);
        Action action = event.getAction();
        ItemStack item = event.getItem();

        if (gamePlayer == null
                || gameManager.getGameState() != GameState.RUNNING
                || item == null
                || action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        Inventory targetInventory = null;
        Material blockType = block.getType();

        switch (blockType) {
            case CHEST:
            case TRAPPED_CHEST:
                if (block.getState() instanceof Chest) {
                    targetInventory = ((Chest) block.getState()).getInventory();
                }
                break;
            case ENDER_CHEST:
                targetInventory = player.getEnderChest();
                break;
            default:
                return;
        }

        if (!isOtherTeamChest(gamePlayer, block)) {
            return;
        }

        if (targetInventory == null) return;

        Inventory inventory = player.getInventory();
        boolean hasFailures = false;
        int transferredCount = 0;
        int totalItems = 0;

        if (ToolSetUtil.isOnlyOneTool(player, item)) {
            gamePlayer.sendMessage("&c你不能将仅有的一件该物品放入箱子！");
            return;
        }

        if (ToolSetUtil.PROTECTED_TOOLS.contains(item.getType())) {
            HashMap<Integer, ItemStack> left = targetInventory.addItem(item.clone());
            if (left.isEmpty()) {
                gamePlayer.sendMessage("&a成功转移了 1 个物品！");
                PlayerUtil.setItemInHand(player, null);
            } else {
                gamePlayer.sendMessage("&c物品添加失败！箱子已满或空间不足。");
            }
            return;
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item1 = inventory.getItem(i);
            if (item1 != null && item1.getType() == item.getType()) {
                totalItems += item1.getAmount();
                HashMap<Integer, ItemStack> left = targetInventory.addItem(item1.clone());

                if (left.isEmpty()) {
                    // 完全添加成功
                    transferredCount += item1.getAmount();
                    inventory.setItem(i, null);
                } else {
                    // 部分或完全失败
                    ItemStack remaining = left.get(0);
                    transferredCount += (item1.getAmount() - remaining.getAmount());
                    inventory.setItem(i, remaining);
                    hasFailures = true;
                }
            }
        }

        if (hasFailures) {
            if (transferredCount > 0) {
                // 部分成功
                gamePlayer.sendMessage("&e部分物品添加失败！已转移 " + transferredCount + "/" + totalItems + " 个物品，箱子空间不足。");
            } else {
                gamePlayer.sendMessage("&c物品添加失败！箱子已满或空间不足。");
            }
        } else if (transferredCount > 0) {
            gamePlayer.sendMessage("&a成功转移了 " + transferredCount + " 个物品！");
        }
    }

    private boolean isOtherTeamChest(GamePlayer gamePlayer, Block block) {
        for (GameTeam team : AzuraBedWars.getInstance().getGameManager().getGameTeams()) {
            if (team.getSpawnLocation().distance(block.getLocation()) <= 18) {
                if (!team.getAlivePlayers().isEmpty() && !team.isInTeam(gamePlayer)) {
                    gamePlayer.sendMessage("&c只有该队伍的玩家才可以使用这个箱子");
                    return false;
                }
                break;
            }
        }

        return true;
    }
}
