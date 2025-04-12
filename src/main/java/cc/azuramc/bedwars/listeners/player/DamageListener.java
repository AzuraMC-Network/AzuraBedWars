package cc.azuramc.bedwars.listeners.player;

import cc.azuramc.bedwars.compat.util.ActionBarUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.events.BedwarsPlayerKilledEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.enums.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 伤害监听器
 * <p>
 * 负责处理玩家伤害和死亡事件，包括虚空伤害、玩家击杀、团队伤害检测等
 * </p>
 */
public class DamageListener implements Listener {
    // 常量定义
    private static final String METADATA_VOID_PLAYER = "voidPlayer";
    private static final String METADATA_FIREBALL_NOFALL = "FIREBALL PLAYER NOFALL";
    private static final String METADATA_SHOP = "Shop";
    private static final String METADATA_SHOP2 = "Shop2";
    private static final String COINS_ACTION_BAR = "§6+1个金币";
    private static final String COINS_MESSAGE = "§6+1个金币 (最终击杀)";
    private static final int COINS_ACTIONBAR_TIMES = 5;
    private static final int ACTIONBAR_PERIOD = 10;
    private static final int RESPAWN_DELAY = 10;
    private static final double VOID_DAMAGE = 100.0D;
    private static final int COINS_REWARD = 1;

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();
    private final AzuraBedWars plugin = AzuraBedWars.getInstance();

    /**
     * 处理实体伤害事件
     *
     * @param event 实体伤害事件
     */
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        // 检查商店NPC伤害
        if (event.getEntity().hasMetadata(METADATA_SHOP) || event.getEntity().hasMetadata(METADATA_SHOP2)) {
            event.setCancelled(true);
            return;
        }

        // 只处理玩家伤害
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        // 处理等待阶段的伤害
        if (gameManager.getGameState() == GameState.WAITING) {
            handleWaitingStateDamage(event, player);
            return;
        }

        // 处理游戏阶段的伤害
        if (gameManager.getGameState() == GameState.RUNNING) {
            handleRunningStateDamage(event, player, gamePlayer);
        }
    }

    /**
     * 处理玩家死亡事件
     *
     * @param event 玩家死亡事件
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        GameTeam gameTeam = gamePlayer != null ? gamePlayer.getGameTeam() : null;

        // 给予被击杀者身上的资源至击杀者（全部转换为经验）
        if (player.getKiller() != null && player.getKiller() instanceof Player killer) {
            killer.giveExpLevels(getPlayerRewardEXP(player));
        }

        // 清理死亡信息和物品
        cleanDeathDrops(event);

        // 游戏未开始或玩家是观察者时不处理
        if (gameManager.getGameState() == GameState.WAITING || (gamePlayer != null && gamePlayer.isSpectator())) {
            return;
        }

        // 处理非虚空死亡
        if (!player.hasMetadata(METADATA_VOID_PLAYER)) {
            handleNormalDeath(player, gamePlayer, gameTeam);
        }

        // 移除虚空标记并处理重生
        player.removeMetadata(METADATA_VOID_PLAYER, plugin);
        handlePlayerRespawn(player);
    }

    /**
     * 获取玩家死亡时身上的资源（经典模式）
     *
     * @param player 要获取的目标玩家（死亡玩家）
     */
    private int getPlayerRewardEXP(Player player) {
        // 处理物品类型资源
        Map<Material, Integer> items = new HashMap<>();
        items.put(Material.IRON_INGOT, 0);
        items.put(Material.GOLD_INGOT, 0);
        items.put(Material.DIAMOND, 0);
        items.put(Material.EMERALD, 0);
          // 遍历背包 得到各类物品资源总数 存在 items 中
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;

            Material itemType = item.getType();
            if (items.containsKey(itemType)) {
                items.put(itemType, items.get(itemType) + item.getAmount());
            }
        }

        // 处理经验资源
        int oldExp = player.getLevel();
        int ironExp = items.get(Material.IRON_INGOT);
        int goldExp = items.get(Material.GOLD_INGOT) * 3;
        int diamondExp = items.get(Material.DIAMOND) * 40;
        int emeraldExp = items.get(Material.EMERALD) * 80;

        // 目标玩家的全部身家 :w:
        return oldExp + ironExp + goldExp + diamondExp + emeraldExp;
    }

    /**
     * 处理实体间伤害事件（PVP等）
     *
     * @param event 实体间伤害事件
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();

        // 只处理玩家相关伤害
        if (entity instanceof Player || damager instanceof Player || damager instanceof Projectile) {
            if (gameManager.getGameState() != GameState.RUNNING) {
                return;
            }

            GamePlayer gamePlayer = entity instanceof Player ? GamePlayer.get(entity.getUniqueId()) : null;
            
            if (damager instanceof Player && entity instanceof Player) {
                handlePlayerVsPlayerDamage(event, gamePlayer, damager);
            } else if (entity instanceof Player && damager instanceof Projectile projectile) {
                handleProjectileDamage(event, gamePlayer, projectile);
            }
        }
    }

    /**
     * 处理等待状态的伤害
     *
     * @param event 伤害事件
     * @param player 玩家
     */
    private void handleWaitingStateDamage(EntityDamageEvent event, Player player) {
        event.setCancelled(true);
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            // 倒计时时间小于3秒时不处理
            if (gameManager.getGameStartRunnable() != null && gameManager.getGameStartRunnable().getCountdown() < 3) {
                return;
            }
            player.teleport(gameManager.getWaitingLocation());
        }
    }

    /**
     * 处理游戏进行中的伤害
     *
     * @param event 伤害事件
     * @param player 玩家
     * @param gamePlayer 游戏玩家
     */
    private void handleRunningStateDamage(EntityDamageEvent event, Player player, GamePlayer gamePlayer) {
        // 游戏结束时取消所有伤害
        if (gameManager.getEventManager().isOver()) {
            event.setCancelled(true);
            return;
        }

        // 观察者不受伤害
        if (gamePlayer != null && gamePlayer.isSpectator()) {
            event.setCancelled(true);
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                gamePlayer.getSpectatorTarget().tp();
            }
            return;
        }

        // 处理虚空伤害
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && gamePlayer != null) {
            handleVoidDamage(event, player, gamePlayer);
            return;
        }

        // 处理火球落地不受伤
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && player.hasMetadata(METADATA_FIREBALL_NOFALL)) {
            event.setCancelled(true);
            player.removeMetadata(METADATA_FIREBALL_NOFALL, plugin);
        }
    }

    /**
     * 处理虚空伤害
     *
     * @param event 伤害事件
     * @param player 玩家
     * @param gamePlayer 游戏玩家
     */
    private void handleVoidDamage(EntityDamageEvent event, Player player, GamePlayer gamePlayer) {
        event.setDamage(VOID_DAMAGE);
        Player killer = player.getKiller();
        GameTeam gameTeam = gamePlayer.getGameTeam();

        if (killer != null) {
            GamePlayer killerPlayer = GamePlayer.get(killer.getUniqueId());
            if (killerPlayer == null) {
                return;
            }
            
            GameTeam killerTeam = killerPlayer.getGameTeam();
            processKill(gamePlayer, gameTeam, killerPlayer, killerTeam, killer, player, gameTeam.isBedDestroy());
            broadcastVoidKillMessage(gamePlayer, gameTeam, killerPlayer, killerTeam, gameTeam.isBedDestroy());
        } else {
            // 自杀消息
            gameManager.broadcastMessage(gameTeam.getChatColor() + gamePlayer.getNickName() + "(" + gameTeam.getName() + ")§e掉下了虚空");
            gamePlayer.getPlayerProfile().addDeaths();
        }
        
        player.setMetadata(METADATA_VOID_PLAYER, new FixedMetadataValue(plugin, ""));
    }

    /**
     * 发送虚空击杀消息
     */
    private void broadcastVoidKillMessage(GamePlayer gamePlayer, GameTeam gameTeam, GamePlayer killerPlayer, GameTeam killerTeam, boolean isFinalKill) {
        if (isFinalKill) {
            gameManager.broadcastMessage(gameTeam.getChatColor() + gamePlayer.getNickName() + "(" + gameTeam.getName() + ") [最终击杀]§e被" + killerTeam.getChatColor() + "(" + killerTeam.getName() + ")§e狠狠滴丢下虚空");
        } else {
            gameManager.broadcastMessage(gameTeam.getChatColor() + gamePlayer.getNickName() + "(" + gameTeam.getName() + ")§e被" + killerTeam.getChatColor() + "(" + killerTeam.getName() + ")§e狠狠滴丢下虚空");
        }
    }

    /**
     * 处理普通击杀奖励和消息
     */
    private void processKill(GamePlayer gamePlayer, GameTeam gameTeam, GamePlayer killerPlayer, GameTeam killerTeam, Player killer, Player player, boolean isFinalKill) {
        if (isFinalKill) {
            // 最终击杀给金币奖励
            showCoinsReward(killer);
            AzuraBedWars.getInstance().getEcon().depositPlayer(player, COINS_REWARD);
            killerPlayer.addFinalKills();
        } else {
            killerPlayer.addKills();
        }
        
        killerPlayer.getPlayerProfile().addKills();
        Bukkit.getPluginManager().callEvent(new BedwarsPlayerKilledEvent(player, killer, isFinalKill));
    }

    /**
     * 显示金币奖励
     *
     * @param player 获得奖励的玩家
     */
    private void showCoinsReward(Player player) {
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i == COINS_ACTIONBAR_TIMES) {
                    cancel();
                    return;
                }
                ActionBarUtil.sendBar(player, COINS_ACTION_BAR);
                i++;
            }
        }.runTaskTimerAsynchronously(plugin, 0, ACTIONBAR_PERIOD);
        player.sendMessage(COINS_MESSAGE);
    }

    /**
     * 清理死亡掉落物和消息
     *
     * @param event 死亡事件
     */
    private void cleanDeathDrops(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.getEntity().getInventory().clear();
        event.setDroppedExp(0);
    }

    /**
     * 处理普通（非虚空）死亡
     *
     * @param player 死亡玩家
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家队伍
     */
    private void handleNormalDeath(Player player, GamePlayer gamePlayer, GameTeam gameTeam) {
        // 获取击杀者
        Player killer = findKiller(player, gamePlayer);
        if (killer == null) {
            return;
        }

        GamePlayer killerPlayer = GamePlayer.get(killer.getUniqueId());
        if (killerPlayer == null) {
            return;
        }

        GameTeam killerTeam = killerPlayer.getGameTeam();
        boolean isFinalKill = gameTeam != null && gameTeam.isBedDestroy();

        // 处理最终击杀
        if (isFinalKill) {
            showCoinsReward(killer);
            AzuraBedWars.getInstance().getEcon().depositPlayer(player, COINS_REWARD);
            killerPlayer.addFinalKills();
            
            // 广播击杀消息
            gameManager.broadcastMessage(gameTeam.getChatColor() + gamePlayer.getNickName() + "(" + gameTeam.getName() + "♛)[最终击杀]§e被" +
                                 killerTeam.getChatColor() + killerPlayer.getNickName() + "(" + killerTeam.getName() + "♛)§e狠狠滴推倒");
        }

        // 触发击杀事件
        if (gameTeam != null) {
            Bukkit.getPluginManager().callEvent(new BedwarsPlayerKilledEvent(player, killer, isFinalKill));
        }

        // 更新玩家数据
        killerPlayer.getPlayerProfile().addKills();
        if (gamePlayer != null) {
            gamePlayer.getPlayerProfile().addDeaths();
        }
    }

    /**
     * 寻找真正的击杀者（包括辅助击杀）
     *
     * @param player 死亡玩家
     * @param gamePlayer 游戏玩家
     * @return 击杀者
     */
    private Player findKiller(Player player, GamePlayer gamePlayer) {
        Player killer = player.getKiller();
        
        // 如果没有直接击杀者，尝试从辅助中获取
        if (killer == null && gamePlayer != null) {
            List<GamePlayer> killers = gamePlayer.getAssistsManager().getAssists(System.currentTimeMillis());
            if (killers != null && !killers.isEmpty()) {
                killer = killers.getFirst().getPlayer();
            }
        }
        
        return killer;
    }

    /**
     * 处理玩家重生
     *
     * @param player 重生玩家
     */
    private void handlePlayerRespawn(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.spigot().respawn();
            
            // 使用PlayerUtil隐藏玩家
            for (GamePlayer otherPlayer : GamePlayer.getOnlinePlayers()) {
                PlayerUtil.hidePlayer(otherPlayer.getPlayer(), player);
            }
            
            // 玩家自己也看不到自己
            PlayerUtil.hidePlayer(player, player);
        }, RESPAWN_DELAY);
    }

    /**
     * 处理玩家VS玩家伤害
     *
     * @param event 伤害事件
     * @param gamePlayer 受伤玩家
     * @param damager 攻击者
     */
    private void handlePlayerVsPlayerDamage(EntityDamageByEntityEvent event, GamePlayer gamePlayer, Entity damager) {
        GamePlayer damagerPlayer = GamePlayer.get(damager.getUniqueId());

        // 观察者不能造成伤害
        if (damagerPlayer != null && damagerPlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        // 阻止队友伤害
        if (gamePlayer != null && gamePlayer.getGameTeam().isInTeam(damagerPlayer)) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理投射物伤害
     *
     * @param event 伤害事件
     * @param gamePlayer 受伤玩家
     * @param projectile 投射物
     */
    private void handleProjectileDamage(EntityDamageByEntityEvent event, GamePlayer gamePlayer, Projectile projectile) {
        // 火球伤害特殊处理
        if (projectile.getType() == EntityType.FIREBALL) {
            event.setCancelled(true);
            return;
        }

        // 检查射击者是否为玩家
        if (projectile.getShooter() instanceof Player) {
            GamePlayer attackerPlayer = GamePlayer.get(((Player) projectile.getShooter()).getUniqueId());

            // 阻止队友的弓箭伤害
            if (gamePlayer != null && gamePlayer.getGameTeam().isInTeam(attackerPlayer)) {
                event.setCancelled(true);
            }
        }
    }
}

