package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.BedwarsPlayerKillEvent;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.PlayerConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.listener.projectile.FireballHandler;
import cc.azuramc.bedwars.util.CustomEntityRemoverUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.VaultUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 伤害监听器
 * <p>
 * 负责处理玩家伤害和死亡事件，包括虚空伤害、玩家击杀、团队伤害检测等
 * </p>
 *
 * @author an5w1r@163.com
 */
public class PlayerDamageListener implements Listener {

    private final static MessageConfig.PlayerDeath MESSAGE_CONFIG = AzuraBedWars.getInstance().getMessageConfig().getPlayerDeath();
    private final static PlayerConfig.PlayerDeath CONFIG = AzuraBedWars.getInstance().getPlayerConfig().getPlayerDeath();

    private static final String METADATA_SHOP = "Shop";
    private static final String METADATA_SHOP2 = "Shop2";
    private static final String COINS_ACTION_BAR = MESSAGE_CONFIG.getCoinsActionBar();
    private static final String COINS_MESSAGE = MESSAGE_CONFIG.getCoinsMessage();
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
        Player killer = player.getKiller();

        GamePlayer gamePlayer = GamePlayer.get(player);
        GameTeam gameTeam = gamePlayer != null ? gamePlayer.getGameTeam() : null;

        // 如果没有击杀者，直接清理掉落物并继续处理
        if (killer == null) {
            LoggerUtil.debug("Triggered PlayerDamageListener$onDeath | killer is null");
        }
        else {
            LoggerUtil.debug("Triggered PlayerDamageListener$onDeath | killer isn't null");
            // 处理击杀奖励
            processKillReward(event, gamePlayer, GamePlayer.get(killer));
        }

        // 清理掉落物
        cleanDeathDrops(event);

        // 处理死亡后的游戏逻辑
        processDeathGameLogic(gamePlayer, gameTeam);
        handlePlayerRespawn(gamePlayer);
    }

    private void processKillReward(PlayerDeathEvent event, GamePlayer gamePlayer, GamePlayer gameKiller) {
        if (gamePlayer == null) {
            return;
        }

        if (gameKiller.getGameModeType() == GameModeType.EXPERIENCE) {
            // 1. 击杀者是经验模式
            if (gamePlayer.getGameModeType() == GameModeType.EXPERIENCE) {
                LoggerUtil.debug("Triggered PlayerDamageListener$processKillReward | player and killer all the 'EXPERIENCE' mode'");
                // 1.1 被击杀者也是经验模式，直接给经验，无需转换
                // 从experienceSources直接给予经验
                convertExperienceSourcesToExp(gamePlayer, gameKiller);
            } else {
                // 1.2 被击杀者是default模式，需要将物品转换为经验
                LoggerUtil.debug("Triggered PlayerDamageListener$processKillReward | player is 'EXPERIENCE' mode , killer is 'DEFAULT' mode");
                gameKiller.getPlayer().giveExpLevels(getPlayerRewardExp(gamePlayer.getPlayer()));
            }
        } else {
            // 2. 击杀者是default模式
            if (gamePlayer.getGameModeType() == GameModeType.EXPERIENCE) {
                // 2.1 被击杀者是经验模式，需要将经验转换为物品
                LoggerUtil.debug("Triggered PlayerDamageListener$processKillReward | player is 'DEFAULT' mode , killer is 'EXPERIENCE' mode");
                convertExperienceSourcesToItems(gamePlayer, gameKiller, event);
            } else {
                // 2.2 被击杀者是default模式，直接转移物品
                LoggerUtil.debug("Triggered PlayerDamageListener$processKillReward | player and killer all the 'DEFAULT' mode'");
                transferItemsToKiller(gamePlayer.getPlayer(), gameKiller.getPlayer(), event);
            }
        }

        // 清除经验来源map
        gamePlayer.getExperienceSources().clear();
        LoggerUtil.debug("PlayerDamageListener$convertExperienceSourcesToExp | expMapCleared");
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
        if (PlayerInvisibilityListener.isPlayerInvisible(gamePlayer)) {
            PlayerInvisibilityListener.forceEndInvisibility(gamePlayer);
        }

    }

    /**
     * 将死亡玩家的物品转移给击杀者
     *
     * @param player 死亡玩家
     * @param killer 击杀者
     * @param event  死亡事件
     */
    private void transferItemsToKiller(Player player, Player killer, PlayerDeathEvent event) {
        Inventory playerInventory = player.getInventory();
        Inventory killerInventory = killer.getInventory();

        // 定义需要转移的资源类型
        Material[] resourceTypes = {
                XMaterial.IRON_INGOT.get(),
                XMaterial.GOLD_INGOT.get(),
                XMaterial.DIAMOND.get(),
                XMaterial.EMERALD.get()
        };

        // 先把所有物品添加到掉落列表中
        List<ItemStack> drops = new ArrayList<>();

        // 收集所有死亡玩家的资源物品
        for (ItemStack item : playerInventory.getContents()) {
            if (item == null) {
                continue;
            }

            Material itemType = item.getType();
            boolean isResource = false;

            // 检查是否为资源物品
            for (Material resourceType : resourceTypes) {
                if (resourceType.equals(itemType)) {
                    isResource = true;
                    break;
                }
            }

            // 如果是资源物品，加入掉落列表
            if (isResource) {
                drops.add(item.clone());
            }
        }

        // 尝试将资源给击杀者
        for (ItemStack item : drops) {
            // 尝试添加到击杀者背包
            HashMap<Integer, ItemStack> leftover = killerInventory.addItem(item);

            // 如果有剩余，掉落在死亡玩家位置
            if (!leftover.isEmpty()) {
                for (ItemStack leftItem : leftover.values()) {
                    player.getWorld().dropItem(player.getLocation(), leftItem);
                }
            }
        }

        // 清空死亡玩家的物品栏
        playerInventory.clear();

        // 更新两个玩家的背包显示
        player.updateInventory();
        killer.updateInventory();
    }

    /**
     * 获取玩家死亡时身上的资源（经典模式）
     *
     * @param player 要获取的目标玩家（死亡玩家）
     */
    private int getPlayerRewardExp(Player player) {
        // 处理物品类型资源
        Map<Material, Integer> items = new HashMap<>();
        items.put(XMaterial.IRON_INGOT.get(), 0);
        items.put(XMaterial.GOLD_INGOT.get(), 0);
        items.put(XMaterial.DIAMOND.get(), 0);
        items.put(XMaterial.EMERALD.get(), 0);
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
        int ironExp = items.get(XMaterial.IRON_INGOT.get());
        int goldExp = items.get(XMaterial.GOLD_INGOT.get()) * 3;
        int diamondExp = items.get(XMaterial.DIAMOND.get()) * 40;
        int emeraldExp = items.get(XMaterial.EMERALD.get()) * 80;

        // 目标玩家的全部身家 :w:
        return oldExp + ironExp + goldExp + diamondExp + emeraldExp;
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

        GamePlayer gamePlayer = entity instanceof Player ? GamePlayer.get(entity.getUniqueId()) : null;

        // 处理玩家和召唤物间的攻击
        handlePlayerVsCreature(event, entity, attacker);

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

    private void handlePlayerVsCreature(EntityDamageByEntityEvent event, Entity entity, Entity attacker) {

        CustomEntityRemoverUtil entityo = CustomEntityRemoverUtil.getDespawnables().get(entity.getUniqueId());
        if (entityo == null) {
            return;
        }

        GameTeam entityTeam = entityo.getGameTeam();

        GamePlayer attackerPlayer = GamePlayer.get(attacker.getUniqueId());
        GameTeam attackerTeam = attackerPlayer.getGameTeam();
        if (attackerTeam == null) {
            return;
        }

        if (attackerTeam != entityTeam) {
            return;
        }

        event.setCancelled(true);
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

        // 处理玩家复活保护
        if (PlayerRespawnListener.RESPAWN_PROTECT.get(gamePlayer) != null) {
            PlayerRespawnListener.RESPAWN_PROTECT.remove(gamePlayer);
        }

        // 玩家攻击移除隐身
        if (PlayerInvisibilityListener.isPlayerInvisible(gamePlayer)) {
            PlayerInvisibilityListener.forceEndInvisibility(gamePlayer);
        }
    }

//    /**
//     * 处理虚空伤害
//     *
//     * @param event      伤害事件
//     * @param gamePlayer 游戏玩家
//     */
//    private void handleVoidDamage(EntityDamageEvent event, GamePlayer gamePlayer) {
//        event.setDamage(VOID_DAMAGE);
//        GamePlayer gameKiller = GamePlayer.get(gamePlayer.getPlayer().getKiller());
//        GameTeam gameTeam = gamePlayer.getGameTeam();
//
//        if (gameKiller != null) {
//
//            GameTeam killerTeam = gameKiller.getGameTeam();
//            processKill(gamePlayer, gameTeam, gameKiller, killerTeam, gameTeam.isDestroyed());
//        } else {
//            // 自杀消息
//            gamePlayer.getPlayerData().addDeaths();
//        }
//
//        gamePlayer.getPlayer().setMetadata(METADATA_VOID_PLAYER, new FixedMetadataValue(plugin, ""));
//    }

//    /**
//     * 处理普通击杀奖励和消息
//     */
//    private void processKill(GamePlayer gamePlayer, GameTeam gameTeam, GamePlayer gameKiller, GameTeam killerTeam, boolean isFinalKill) {
//
//        BedwarsPlayerKillEvent event = new BedwarsPlayerKillEvent(gamePlayer, gameKiller, isFinalKill);
//        Bukkit.getPluginManager().callEvent(event);
//        if (event.isCancelled()) {
//            return;
//        }
//
//        if (isFinalKill) {
//            // 最终击杀给金币奖励
//            if (!VaultUtil.ecoIsNull) {
//                showCoinsReward(gameKiller);
//                VaultUtil.depositPlayer(gameKiller, COINS_REWARD);
//            }
//            gameKiller.getPlayerData().addFinalKills();
//        } else {
//            gameKiller.getPlayerData().addKills();
//        }
//
//        gameKiller.getPlayerData().addKills();
//    }

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
                gamePlayer.sendActionBar(COINS_ACTION_BAR);
                i++;
            }
        }.runTaskTimerAsynchronously(plugin, 0, ACTIONBAR_PERIOD);
        gamePlayer.sendMessage(COINS_MESSAGE);
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
     *
     * @author ImCur_
     */
    private void handleDeath(GamePlayer gamePlayer, GameTeam gameTeam) {

        // 获取击杀者
        GamePlayer gameKiller = findKiller(gamePlayer);

        GameTeam killerTeam = gameKiller == null ? null : gameKiller.getGameTeam();
        boolean isFinalKill = gameTeam != null && gameTeam.isDestroyed();

        BedwarsPlayerKillEvent event = new BedwarsPlayerKillEvent(gamePlayer, gameKiller, isFinalKill);
        Bukkit.getPluginManager().callEvent(event);

        // 处理最终击杀
        if (isFinalKill) {
            PlayerDeathReasonListener.setFinalKill(true);
            if (gameKiller != null) {
                if (!VaultUtil.ecoIsNull) {
                    showCoinsReward(gameKiller);
                    VaultUtil.depositPlayer(gameKiller, COINS_REWARD);
                }
                gameKiller.getPlayerData().addFinalKills();
            }
        }

        // 更新玩家数据
        if (gameKiller != null)
            gameKiller.getPlayerData().addKills();
        gamePlayer.getPlayerData().addDeaths();
    }

    /**
     * 寻找真正的击杀者（包括辅助击杀）
     *
     * @param gamePlayer 游戏玩家
     * @return 击杀者
     */
    private GamePlayer findKiller(GamePlayer gamePlayer) {
        Player killer = gamePlayer.getPlayer().getKiller();

        // 如果没有直接击杀者，尝试从辅助中获取
        if (killer == null) {
            List<GamePlayer> killers = gamePlayer.getAssistsManager().getAssists(System.currentTimeMillis());
            if (killers != null && !killers.isEmpty()) {
                return killers.get(0);
            }
        }

        return GamePlayer.get(killer);
    }

    /**
     * 处理玩家重生
     *
     * @param gamePlayer 重生玩家
     */
    private void handlePlayerRespawn(GamePlayer gamePlayer) {
        if (gamePlayer == null) return;
        Player player = gamePlayer.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            gamePlayer.getPlayer().spigot().respawn();

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
            int finalDamage = (int) (event.getFinalDamage() + 0.5);
            attackPlayer.sendTitle(
                    "&r ",
                    "&b伤害 &f- &e" + finalDamage + "  &b血量 &f- &e" + ((int) gamePlayer.getPlayer().getHealth() - finalDamage) +
                            "&f/&e" + (int) PlayerUtil.getMaxHealth(gamePlayer.getPlayer()),
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

    /**
     * 将被击杀者的经验来源转换为经验值给予击杀者
     *
     * @param gamePlayer 被击杀的游戏玩家
     * @param gameKiller 击杀者
     */
    private void convertExperienceSourcesToExp(GamePlayer gamePlayer, GamePlayer gameKiller) {
        int totalExp = 0;
        Map<String, Integer> expSources = gamePlayer.getExperienceSources();

        // 按照不同资源类型计算总经验值
        for (Map.Entry<String, Integer> entry : expSources.entrySet()) {
            String resourceType = entry.getKey();
            int amount = entry.getValue();
            LoggerUtil.debug("PlayerDamageListener$convertExperienceSourcesToExp | in for amount: " + amount);

            // 不同资源类型的经验转换倍率
            switch (resourceType) {
                case "IRON":
                    // IRON 不用转换
                    totalExp += amount;
                    break;
                case "GOLD":
                    // GOLD 除以3
                    totalExp += amount * 3;
                    break;
                case "DIAMOND":
                    // DIAMOND 除以40
                    totalExp += amount * 40;
                    break;
                case "EMERALD":
                    // EMERALD 除以80
                    totalExp += amount * 80;
                    break;
                default:
                    // 其他资源类型直接加上原值
                    totalExp += amount;
                    break;
            }
        }

        // 加上被击杀者的经验等级
        int playerLevel = gamePlayer.getPlayer().getLevel();
        LoggerUtil.debug("PlayerDamageListener$convertExperienceSourcesToExp | playerLevel: " + playerLevel);
        totalExp += playerLevel;
        LoggerUtil.debug("PlayerDamageListener$convertExperienceSourcesToExp | totalExp: " + totalExp);

        // 给予击杀者经验
        gameKiller.getPlayer().giveExpLevels(totalExp);
        gameKiller.sendMessage("&a击杀 &e" + gamePlayer.getName() + " &a掠夺了 &e" + totalExp + " &a经验");
    }

    /**
     * 将被击杀者的经验来源转换为物品给予击杀者
     *
     * @param gamePlayer 被击杀的游戏玩家
     * @param gameKiller 击杀者
     * @param event      死亡事件
     */
    private void convertExperienceSourcesToItems(GamePlayer gamePlayer, GamePlayer gameKiller, PlayerDeathEvent event) {
        Player killer = gameKiller.getPlayer();
        Inventory killerInventory = killer.getInventory();
        Map<String, Integer> expSources = gamePlayer.getExperienceSources();
        List<ItemStack> drops = new ArrayList<>();

        // 从经验源转换为相应的物品数量
        for (Map.Entry<String, Integer> entry : expSources.entrySet()) {
            String resourceType = entry.getKey();
            int expAmount = entry.getValue();
            int itemAmount;
            Material material;

            // 根据资源类型进行转换
            switch (resourceType) {
                case "IRON":
                    // IRON 不用转换
                    itemAmount = expAmount;
                    material = XMaterial.IRON_INGOT.get();
                    break;
                case "GOLD":
                    // GOLD 除以3
                    itemAmount = (int) Math.floor(expAmount / 3.0);
                    material = XMaterial.GOLD_INGOT.get();
                    break;
                case "DIAMOND":
                    // DIAMOND 除以40
                    itemAmount = (int) Math.floor(expAmount / 40.0);
                    material = XMaterial.DIAMOND.get();
                    break;
                case "EMERALD":
                    // EMERALD 除以80
                    itemAmount = (int) Math.floor(expAmount / 80.0);
                    material = XMaterial.EMERALD.get();
                    break;
                default:
                    continue;
            }

            // 如果转换后数量大于0，创建物品堆并添加到掉落列表
            if (itemAmount > 0) {
                // 创建物品 - 每个物品栈最多64个
                while (itemAmount > 0) {
                    int stackSize = Math.min(itemAmount, 64);
                    ItemStack item = null;
                    if (material != null) {
                        item = new ItemStack(material, stackSize);
                    }
                    drops.add(item);
                    itemAmount -= stackSize;
                }
            }
        }

        // 给予击杀者物品或掉落在地上
        for (ItemStack item : drops) {
            // 尝试添加到击杀者背包
            HashMap<Integer, ItemStack> leftover = killerInventory.addItem(item);

            // 如果有剩余，掉落在死亡玩家位置
            if (!leftover.isEmpty()) {
                for (ItemStack leftItem : leftover.values()) {
                    gamePlayer.getPlayer().getWorld().dropItem(gamePlayer.getPlayer().getLocation(), leftItem);
                }
            }
        }

        // 清空死亡玩家的物品栏
        gamePlayer.getPlayer().getInventory().clear();

        // 更新两个玩家的背包显示
        gamePlayer.getPlayer().updateInventory();
        killer.updateInventory();
    }
}