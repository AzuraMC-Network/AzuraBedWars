package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.player.BedwarsPlayerKillEvent;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.PlayerConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.listener.projectile.FireballHandler;
import cc.azuramc.bedwars.util.DamageUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.VaultUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 伤害监听器
 * <p>
 * 负责处理玩家伤害和死亡事件，包括虚空伤害、玩家击杀、团队伤害检测等
 * </p>
 *
 * @author an5w1r@163.com
 */
public class PlayerDamageListener implements Listener {

    private static final MessageConfig messageConfig = AzuraBedWars.getInstance().getMessageConfig();
    private final static PlayerConfig.PlayerDeath CONFIG = AzuraBedWars.getInstance().getPlayerConfig().getPlayerDeath();

    private static final String METADATA_SHOP = "Shop";
    private static final String METADATA_SHOP2 = "Shop2";
    private static final int COINS_ACTIONBAR_TIMES = CONFIG.getCoinsActionBarTimes();
    private static final int ACTIONBAR_PERIOD = CONFIG.getActionBarPeriod();
    private static final int RESPAWN_DELAY = 10;
    private static final double VOID_DAMAGE = 100.0D;
    private static final double COINS_REWARD = CONFIG.getCoinsReward();
    private static final int ATTACK_DISPLAY_TITLE_TICKS = 10;
    private final boolean ARROW_DISPLAY_ENABLED = AzuraBedWars.getInstance().getGameManager().isArrowDisplayEnabled();
    private final boolean ATTACK_DISPLAY_ENABLED = AzuraBedWars.getInstance().getGameManager().isAttackDisplayEnabled();
    private final double FIREBALL_FALLEN_DAMAGE_RATE = CONFIG.getFireballFallenDamageRate();
    private final double NORMAL_FALLEN_DAMAGE_RATE = CONFIG.getNormalFallenDamageRate();
    private final double EXPLOSION_DAMAGE_RATE = CONFIG.getExplosionDamageRate();

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
        GamePlayer gamePlayer = GamePlayer.get(player);

        // 处理等待阶段的伤害
        if (gameManager.getGameState() == GameState.WAITING) {
            handleWaitingStateDamage(event, player);
            return;
        }

        // 处理游戏阶段的伤害
        if (gameManager.getGameState() == GameState.RUNNING) {
            handleRunningStateDamage(event, gamePlayer);
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
        Player killer = DamageUtil.findKiller(player);

        GamePlayer gamePlayer = GamePlayer.get(player);
        GameTeam gameTeam = gamePlayer != null ? gamePlayer.getGameTeam() : null;

        // 如果没有击杀者，直接清理掉落物并继续处理
        if (killer == null) {
            LoggerUtil.debug("Triggered PlayerDamageListener$onDeath | killer is null");
        } else {
            LoggerUtil.debug("Triggered PlayerDamageListener$onDeath | killer isn't null");
            // 处理击杀奖励
            KillRewardHandler.processKillReward(event, gamePlayer, GamePlayer.get(killer));
        }

        // 清理掉落物
        cleanDeathDrops(event);

        // 处理死亡后的游戏逻辑
        processDeathGameLogic(gamePlayer, gameTeam);
        handlePlayerRespawn(gamePlayer);
    }

    /**
     * 处理玩家死亡后的游戏逻辑
     *
     * @param gamePlayer 游戏玩家
     * @param gameTeam   玩家所在队伍
     */
    private void processDeathGameLogic(GamePlayer gamePlayer, GameTeam gameTeam) {

        if (gamePlayer == null) {
            return;
        }

        // 游戏未开始
        if (gameManager.getGameState() == GameState.WAITING) {
            return;
        }

        // 玩家是观察者
        if (gamePlayer.isSpectator()) {
            return;
        }

        // 处理死亡
        handleDeath(gamePlayer, gameTeam);

        // 如果存在隐藏盔甲效果则移除
        if (gamePlayer.isInvisible()) {
            gamePlayer.endInvisibility();
        }

    }

    /**
     * 处理实体间伤害事件（PVP等）
     *
     * @param event 实体间伤害事件
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        // 检查是否游戏中
        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        Entity entity = event.getEntity();
        Entity attacker = event.getDamager();

        // 只处理受击者为玩家的情况
        if (!(entity instanceof Player)) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(entity.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        // 攻击者为玩家
        if (attacker instanceof Player) {
            // 玩家VS玩家处理
            handlePlayerVsPlayerDamage(event, gamePlayer, attacker);
        }
        // 攻击者为投掷物
        else if (attacker instanceof Projectile) {
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
     * @param gamePlayer 游戏玩家
     */
    private void handleRunningStateDamage(EntityDamageEvent event, GamePlayer gamePlayer) {
        // 游戏结束时取消所有伤害
        if (gameManager.getGameEventManager().isOver()) {
            event.setCancelled(true);
            return;
        }

        if (gamePlayer == null) {
            return;
        }

        // 观察者不受伤害
        if (gamePlayer.isSpectator()) {
            event.setCancelled(true);
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                gamePlayer.getSpectatorTarget().tp();
            }
            return;
        }

        // 处理虚空伤害
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setDamage(VOID_DAMAGE);
            return;
        }

        // 处理火球落地伤害修改
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && gamePlayer.getPlayer().hasMetadata(FireballHandler.NO_FALL_DAMAGE_METADATA)) {
            event.setDamage(event.getFinalDamage() * FIREBALL_FALLEN_DAMAGE_RATE);
            gamePlayer.getPlayer().removeMetadata(FireballHandler.NO_FALL_DAMAGE_METADATA, plugin);
            return;
        }

        // 处理普通摔落伤害修改
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setDamage(event.getFinalDamage() * NORMAL_FALLEN_DAMAGE_RATE);
            return;
        }

        // 处理爆炸伤害修改
        if ((event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            event.setDamage(event.getFinalDamage() * EXPLOSION_DAMAGE_RATE);
        }

        // 玩家攻击移除隐身
        if (gamePlayer.isInvisible()) {
            gamePlayer.endInvisibility();
        }
    }

    /**
     * 显示金币奖励
     *
     * @param gamePlayer 获得奖励的玩家
     */
    private void showCoinsReward(GamePlayer gamePlayer) {
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i == COINS_ACTIONBAR_TIMES) {
                    cancel();
                    return;
                }
                gamePlayer.sendActionBar(messageConfig.getNormalKillRewardsMessage());
                i++;
            }
        }.runTaskTimerAsynchronously(plugin, 0, ACTIONBAR_PERIOD);
        gamePlayer.sendMessage(messageConfig.getFinalKillRewardsMessage());
    }

    /**
     * 清理死亡掉落物和消息
     *
     * @param event 死亡事件
     */
    private void cleanDeathDrops(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.getEntity().getInventory().clear();
        event.setDroppedExp(0);
        LoggerUtil.debug("playerDeath -> cleanDeathDrops$Method");
    }

    /**
     * 修正：处理所有死亡
     *
     * @param gamePlayer 游戏玩家
     * @param gameTeam   玩家队伍
     * @author ImCur_
     */
    private void handleDeath(GamePlayer gamePlayer, GameTeam gameTeam) {

        // 获取击杀者
        GamePlayer gameKiller = DamageUtil.findKiller(gamePlayer);

        GameTeam killerTeam = gameKiller == null ? null : gameKiller.getGameTeam();
        boolean isFinalKill = gameTeam != null && gameTeam.isDestroyed();

        BedwarsPlayerKillEvent event = new BedwarsPlayerKillEvent(gamePlayer, gameKiller, isFinalKill);
        Bukkit.getPluginManager().callEvent(event);

        if (gameKiller == null) {
            return;
        }

        // 处理最终击杀
        if (isFinalKill) {
            PlayerDeathReasonListener.setFinalKill(true);
            if (!VaultUtil.ecoIsNull) {
                showCoinsReward(gameKiller);
                VaultUtil.depositPlayer(gameKiller, COINS_REWARD);
            }
            gameKiller.getPlayerData().addFinalKills();
            gamePlayer.getPlayerData().addFinalDeaths();
        } else {
            gameKiller.getPlayerData().addKills();
        }

        gamePlayer.getPlayerData().addDeaths();
    }

    /**
     * 处理玩家重生
     *
     * @param gamePlayer 重生玩家
     */
    private void handlePlayerRespawn(GamePlayer gamePlayer) {
        if (gamePlayer == null) return;
        Player player = gamePlayer.getPlayer();
        // PlayerRespawnListener处理复活后的相关事宜 (清除不可见等等)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            gamePlayer.getPlayer().spigot().respawn();

            // 使用PlayerUtil隐藏玩家
            PlayerUtil.hidePlayer(GamePlayer.getOnlinePlayers(), gamePlayer);

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

        // 处理玩家复活保护
        if (PlayerRespawnListener.RESPAWN_PROTECT.contains(attackPlayer)) {
            PlayerRespawnListener.RESPAWN_PROTECT.remove(gamePlayer);
        }

        // 如果玩家在保护状态下 不显示攻击伤害信息
        if (PlayerRespawnListener.RESPAWN_PROTECT.contains(gamePlayer)) {
            return;
        }

        gamePlayer.setLastDamage(attackPlayer);

        // 普通攻击伤害显示
        if (ATTACK_DISPLAY_ENABLED && attackPlayer.isViewingArrowDamage()) {
            int finalDamage = Math.round((float) event.getFinalDamage());
            int currentHealth = Math.round((float) gamePlayer.getPlayer().getHealth());
            int remainingHealth = Math.max(currentHealth - finalDamage, 0);
            attackPlayer.sendTitle(
                    "&r ",
                    "&b伤害 &f- &e" + finalDamage + "  &b血量 &f- &e" + remainingHealth +
                            "&f/&e" + Math.round((float) PlayerUtil.getMaxHealth(gamePlayer.getPlayer())),
                    1, ATTACK_DISPLAY_TITLE_TICKS,
                    5);
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

        // 火球伤害特殊处理 (注意 火球伤害是在其他的类有处理 所以这里取消掉不管即可)
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

            gamePlayer.setLastDamage(attackerPlayer);

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
