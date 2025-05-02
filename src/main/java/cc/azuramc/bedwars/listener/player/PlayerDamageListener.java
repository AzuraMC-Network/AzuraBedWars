package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.BedwarsPlayerKilleEvent;
import cc.azuramc.bedwars.compat.util.ActionBarUtil;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.PlayerConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.team.GameTeam;
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
public class PlayerDamageListener implements Listener {

    private final static MessageConfig.PlayerDeath messageConfig = AzuraBedWars.getInstance().getMessageConfig().getPlayerDeath();
    private final static PlayerConfig.PlayerDeath config = AzuraBedWars.getInstance().getPlayerConfig().getPlayerDeath();

    // 常量定义
    private static final String METADATA_VOID_PLAYER = "VOID_PLAYER";
    private static final String METADATA_FIREBALL_NOFALL = "FIREBALL_PLAYER_NOFALL";
    private static final String METADATA_SHOP = "Shop";
    private static final String METADATA_SHOP2 = "Shop2";
    private static final String COINS_ACTION_BAR = messageConfig.getCoinsActionBar();
    private static final String COINS_MESSAGE = messageConfig.getCoinsMessage();
    private static final int COINS_ACTIONBAR_TIMES = config.getCoinsActionBarTimes();
    private static final int ACTIONBAR_PERIOD = config.getActionBarPeriod();
    private static final int RESPAWN_DELAY = 10;
    private static final double VOID_DAMAGE = 100.0D;
    private static final int COINS_REWARD = config.getCoinsReward();
    private static final int ATTACK_DISPLAY_TITLE_TICKS = 10;
    private final boolean ARROW_DISPLAY_ENABLED = AzuraBedWars.getInstance().getGameManager().isArrowDisplayEnabled();
    private final boolean ATTACK_DISPLAY_ENABLED = AzuraBedWars.getInstance().getGameManager().isAttackDisplayEnabled();

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
            killer.giveExpLevels(getPlayerRewardExp(player));
        }

        // 清理死亡信息和物品
        cleanDeathDrops(event);

        // 游戏未开始
        if (gameManager.getGameState() == GameState.WAITING) {
            return;
        }

        // 玩家是观察者
        if (gamePlayer != null && gamePlayer.isSpectator()) {
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
    private int getPlayerRewardExp(Player player) {
        // 处理物品类型资源
        Map<Material, Integer> items = new HashMap<>();
        items.put(Material.IRON_INGOT, 0);
        items.put(Material.GOLD_INGOT, 0);
        items.put(Material.DIAMOND, 0);
        items.put(Material.EMERALD, 0);
        // 遍历背包 得到各类物品资源总数 存在 items 中
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                continue;
            }

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

        // 检查是否游戏中
        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        Entity entity = event.getEntity();
        Entity attacker = event.getDamager();

        GamePlayer gamePlayer = entity instanceof Player ? GamePlayer.get(entity.getUniqueId()) : null;

        // 检查受击者是否为玩家
        if (gamePlayer == null) {
            return;
        }

        // 攻击者不为玩家
        if (attacker instanceof Player) {
            // 玩家VS玩家处理
            handlePlayerVsPlayerDamage(event, gamePlayer, attacker);
        }

        // 攻击者不为投掷物
        if (attacker instanceof Projectile) {
            // 投掷物攻击玩家处理
            handleProjectileDamage(event, gamePlayer, (Projectile) attacker);
        }
    }

    /**
     * 处理等待状态的伤害
     *
     * @param event  伤害事件
     * @param player 玩家
     */
    private void handleWaitingStateDamage(EntityDamageEvent event, Player player) {
        event.setCancelled(true);
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            // 倒计时时间小于3秒时不处理
            if (gameManager.getGameStartTask() != null && gameManager.getGameStartTask().getCountdown() < 3) {
                return;
            }
            player.teleport(gameManager.getWaitingLocation());
        }
    }

    /**
     * 处理游戏进行中的伤害
     *
     * @param event      伤害事件
     * @param player     玩家
     * @param gamePlayer 游戏玩家
     */
    private void handleRunningStateDamage(EntityDamageEvent event, Player player, GamePlayer gamePlayer) {
        // 游戏结束时取消所有伤害
        if (gameManager.getGameEventManager().isOver()) {
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
     * @param event      伤害事件
     * @param player     玩家
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
            processKill(gamePlayer, gameTeam, killerPlayer, killerTeam, killer, player, gameTeam.isDestroyed());
            broadcastVoidKillMessage(gamePlayer, gameTeam, killerPlayer, killerTeam, gameTeam.isDestroyed());
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

        BedwarsPlayerKilleEvent event = new BedwarsPlayerKilleEvent(player, killer, isFinalKill);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (isFinalKill) {
            // 最终击杀给金币奖励
            showCoinsReward(killer);
            AzuraBedWars.getInstance().getEcon().depositPlayer(player, COINS_REWARD);
            killerPlayer.addFinalKills();
        } else {
            killerPlayer.addKills();
        }

        killerPlayer.getPlayerProfile().addKills();
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
     * @param player     死亡玩家
     * @param gamePlayer 游戏玩家
     * @param gameTeam   玩家队伍
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
        boolean isFinalKill = gameTeam != null && gameTeam.isDestroyed();

        BedwarsPlayerKilleEvent event = new BedwarsPlayerKilleEvent(player, killer, isFinalKill);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

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
            Bukkit.getPluginManager().callEvent(new BedwarsPlayerKilleEvent(player, killer, isFinalKill));
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
     * @param player     死亡玩家
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
     * @param event      伤害事件
     * @param gamePlayer 受伤玩家
     * @param attacker   攻击者
     */
    private void handlePlayerVsPlayerDamage(EntityDamageByEntityEvent event, GamePlayer gamePlayer, Entity attacker) {

        if (gamePlayer == null
                || gamePlayer.isSpectator()
                || gameManager.getGameState() != GameState.RUNNING
                || !(attacker instanceof Player)) {
            return;
        }

        GamePlayer attackPlayer = GamePlayer.get(attacker.getUniqueId());

        // 观察者不能造成伤害
        if (attackPlayer.isSpectator()) {
            event.setCancelled(true);
            return;
        }

        // 阻止队友伤害
        if (gamePlayer.getGameTeam().isInTeam(attackPlayer)) {
            event.setCancelled(true);
            return;
        }

        // 普通攻击伤害显示
        if (ATTACK_DISPLAY_ENABLED && attackPlayer.isViewingArrowDamage()) {
            attackPlayer.sendTitle(1, ATTACK_DISPLAY_TITLE_TICKS, 5, "&r ", "&e伤害 " + String.format("%.1f", event.getFinalDamage()));
        }
    }

    /**
     * 处理投射物伤害
     *
     * @param event      伤害事件
     * @param gamePlayer 受伤玩家
     * @param projectile 投射物
     */
    private void handleProjectileDamage(EntityDamageByEntityEvent event, GamePlayer gamePlayer, Projectile projectile) {

        if (gamePlayer == null
                || gamePlayer.isSpectator()
                || gameManager.getGameState() != GameState.RUNNING
                || !(projectile.getShooter() instanceof Player)) {
            return;
        }

        GamePlayer attackerPlayer = GamePlayer.get(((Player) projectile.getShooter()).getUniqueId());

        // 火球伤害特殊处理
        if (projectile.getType() == EntityType.FIREBALL) {
            event.setCancelled(true);
            return;
        }

        // 阻止队友的投掷物伤害
        if (gamePlayer.getGameTeam().isInTeam(attackerPlayer)) {
            event.setCancelled(true);
            return;
        }

        // 检查投掷物是否为箭矢
        if (projectile.getType() == EntityType.ARROW) {

            if (!ARROW_DISPLAY_ENABLED) {
                return;
            }

            // 检查攻击者是否开启弓箭伤害显示
            if (!attackerPlayer.isViewingArrowDamage()) {
                return;
            }

            attackerPlayer.sendMessage("&e&l目标玩家 &c&l" + gamePlayer.getPlayer().getName() + " &e&l剩余血量: &c&l" + String.format("%.1f", gamePlayer.getPlayer().getHealth()) + " &c&l❤");
        }

    }
}