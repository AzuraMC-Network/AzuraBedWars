package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.EventConfig;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.event.AbstractGameEvent;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.spectator.task.SpectatorCompassTask;
import cc.azuramc.bedwars.game.task.GeneratorTask;
import cc.azuramc.bedwars.game.trap.TrapManager;
import cc.azuramc.bedwars.game.trap.TrapType;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * 游戏开始事件
 * 负责处理游戏开始时的初始化和团队升级效果
 *
 * @author an5w1r@163.com
 */
public class GameStartEvent extends AbstractGameEvent {

    private static final AzuraBedWars PLUGIN = AzuraBedWars.getInstance();
    private static final EventConfig.StartEvent CONFIG = PLUGIN.getEventConfig().getStartEvent();
    private static final MessageConfig.Start MESSAGE_EVENT = PLUGIN.getMessageConfig().getStart();

    /**
     * 创建游戏开始事件
     */
    public GameStartEvent() {
        super(MESSAGE_EVENT.getEventName(), CONFIG.getCountDown(), CONFIG.getEventPriority());
    }

    /**
     * 处理游戏开始倒计时
     *
     * @param gameManager 游戏实例
     * @param seconds 剩余秒数
     */
    @Override
    public void executeRunnable(GameManager gameManager, int seconds) {
        gameManager.broadcastSound(XSound.UI_BUTTON_CLICK.get(), 1f, 1f);
        gameManager.broadcastTitleToAll(
                MESSAGE_EVENT.getTitle().getTitleString(), MESSAGE_EVENT.getTitle().getSubtitle() + seconds, CONFIG.getTitle().getFadeIn(),
            CONFIG.getTitle().getTitleStay(),
            CONFIG.getTitle().getFadeOut()
        );
    }

    /**
     * 执行游戏开始事件
     * 注册团队升级任务、启动资源生成和指南针追踪
     *
     * @param gameManager 游戏实例
     */
    @Override
    public void execute(GameManager gameManager) {
        registerTeamUpgradeTask(gameManager);
        startResourceGenerators(gameManager);
        startCompassTracking();
    }

    /**
     * 注册团队升级任务，处理团队效果和陷阱
     *
     * @param gameManager 游戏实例
     */
    private void registerTeamUpgradeTask(GameManager gameManager) {
        gameManager.getGameEventManager().registerRunnable(MESSAGE_EVENT.getTeamUpgradeTaskName(), (s, c) ->
            GamePlayer.getOnlinePlayers().forEach(player -> {
                if (player.isSpectator()) {
                    return;
                }

                for (GameTeam gameTeam : gameManager.getGameTeams()) {
                    // 跳过不同世界的团队
                    if (!Objects.equals(player.getPlayer().getLocation().getWorld(), gameTeam.getSpawnLocation().getWorld())) {
                        continue;
                    }

                    // 处理疯狂矿工效果
                    applyManicMinerEffect(gameTeam);

                    // 如果是团队成员，检查治愈池效果
                    if (gameTeam.isInTeam(player)) {
                        applyHealingPoolEffect(player, gameTeam);
                        continue;
                    }

                    // 处理敌方玩家进入团队领地的陷阱触发
                    handleEnemyInTeamTerritory(player, gameTeam);
                }
            })
        );
    }

    /**
     * 应用疯狂矿工效果给团队成员
     *
     * @param gameTeam 游戏团队
     */
    private void applyManicMinerEffect(GameTeam gameTeam) {
        if (gameTeam.getMagicMinerUpgrade() <= 0) {
            return;
        }

        AzuraBedWars.getInstance().mainThreadRunnable(() -> gameTeam.getAlivePlayers().forEach((player -> {
            PotionEffectType fastDigging = XPotion.HASTE.get();
            if (fastDigging != null) {
                // 将等级减1 因为Minecraft药水效果等级从0开始 而我们的等级从1开始
                int effectLevel = gameTeam.getMagicMinerUpgrade() - 1;
                player.getPlayer().addPotionEffect(new PotionEffect(fastDigging,
                    CONFIG.getUpgrade().getHasteEffectDuration(),
                        effectLevel));
            }
        })));
    }

    /**
     * 为团队基地内的玩家应用治愈池效果
     *
     * @param gamePlayer 游戏玩家
     * @param gameTeam 游戏团队
     */
    private void applyHealingPoolEffect(GamePlayer gamePlayer, GameTeam gameTeam) {
        double distance = gamePlayer.getPlayer().getLocation().distance(gameTeam.getSpawnLocation());

        if (distance <= CONFIG.getUpgrade().getHealingPoolRange() && gameTeam.isHasHealPoolUpgrade()) {
            AzuraBedWars.getInstance().mainThreadRunnable(() -> {
                PotionEffectType regeneration = XPotion.REGENERATION.get();
                if (regeneration != null) {
                    gamePlayer.getPlayer().addPotionEffect(new PotionEffect(regeneration,
                        CONFIG.getUpgrade().getRegenerationEffectDuration(),
                        CONFIG.getUpgrade().getRegenerationEffectAmplifier()));
                }
            });
        }
    }

    /**
     * 处理敌方玩家进入团队领地触发的陷阱
     *
     * @param gamePlayer 游戏玩家
     * @param gameTeam 游戏团队
     */
    private void handleEnemyInTeamTerritory(GamePlayer gamePlayer, GameTeam gameTeam) {
        double distance = gamePlayer.getPlayer().getLocation().distance(gameTeam.getSpawnLocation());
        TrapManager trapManager = gameTeam.getTrapManager();

        if (distance <= CONFIG.getUpgrade().getTrapTriggerRange() && !gameTeam.isDead()) {
            // 触发致盲陷阱
            if (trapManager.isTrapActive(TrapType.BLINDNESS)) {
                LoggerUtil.debug("GameStartEvent$handleEnemyInTeamTerritory | trigger blindness trap, gamePlayer: " + gamePlayer.getName());
                triggerBlindnessTrap(gamePlayer, gameTeam);
            }

            // 触发反击陷阱
            if (trapManager.isTrapActive(TrapType.FIGHT_BACK)) {
                LoggerUtil.debug("GameStartEvent$handleEnemyInTeamTerritory | trigger fightback trap, gamePlayer: " + gamePlayer.getName());
                triggerFightbackTrap(gamePlayer, gameTeam);
            }

            // 触发反击陷阱
            if (trapManager.isTrapActive(TrapType.ALARM)) {
                LoggerUtil.debug("GameStartEvent$handleEnemyInTeamTerritory | trigger alarm trap, gamePlayer: " + gamePlayer.getName());
                triggerAlarmTrap(gamePlayer, gameTeam);
            }

            // 触发挖掘疲劳陷阱
            if (trapManager.isTrapActive(TrapType.MINER)) {
                LoggerUtil.debug("GameStartEvent$handleEnemyInTeamTerritory | trigger mining fatigue trap, gamePlayer: " + gamePlayer.getName());
                triggerMinerTrap(gamePlayer, gameTeam);
            }
        }
    }

    /**
     * 触发失明陷阱
     *
     * @param gamePlayer 触发陷阱的玩家
     * @param gameTeam 拥有陷阱的团队
     */
    private void triggerBlindnessTrap(GamePlayer gamePlayer, GameTeam gameTeam) {
        TrapManager trapManager = gameTeam.getTrapManager();
        trapManager.deactivateTrap(TrapType.BLINDNESS);

        // 给敌方玩家添加 失明 缓慢 效果
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            PotionEffectType blindness = XPotion.BLINDNESS.get();
            gamePlayer.getPlayer().addPotionEffect(new PotionEffect(blindness,
                    CONFIG.getUpgrade().getTrapEffectDuration(),
                    CONFIG.getUpgrade().getTrapEffectAmplifier()));

            PotionEffectType slowness = XPotion.SLOWNESS.get();
            gamePlayer.getPlayer().addPotionEffect(new PotionEffect(slowness,
                    CONFIG.getUpgrade().getTrapEffectDuration(),
                    CONFIG.getUpgrade().getTrapEffectAmplifier()));
        });

        // 通知团队成员陷阱被触发
        announceTrapTrigger(gameTeam);
    }

    /**
     * 触发反击陷阱
     *
     * @param gamePlayer 触发陷阱的玩家
     * @param gameTeam 拥有陷阱的团队
     */
    private void triggerFightbackTrap(GamePlayer gamePlayer, GameTeam gameTeam) {
        TrapManager trapManager = gameTeam.getTrapManager();
        trapManager.deactivateTrap(TrapType.FIGHT_BACK);

        // 给己方玩家添加速度和跳跃提升效果
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            PotionEffectType blindness = XPotion.SPEED.get();
            PotionEffectType jumpBoost = XPotion.JUMP_BOOST.get();
            gameTeam.getAlivePlayers().forEach(player -> {
                player.getPlayer().addPotionEffect(new PotionEffect(blindness,
                        15,
                        1));

                player.getPlayer().addPotionEffect(new PotionEffect(jumpBoost,
                        15,
                        1));
            });
        });

        // 通知团队成员陷阱被触发
        announceTrapTrigger(gameTeam);
    }

    /**
     * 触发警报陷阱
     *
     * @param gamePlayer 触发陷阱的玩家
     * @param gameTeam 拥有陷阱的团队
     */
    private void triggerAlarmTrap(GamePlayer gamePlayer, GameTeam gameTeam) {
        TrapManager trapManager = gameTeam.getTrapManager();
        trapManager.deactivateTrap(TrapType.ALARM);

        if (gamePlayer.isInvisible()) {
            gamePlayer.endInvisibility();
        }

        // 通知团队成员陷阱被触发
        AzuraBedWars.getInstance().mainThreadRunnable(() -> gameTeam.getAlivePlayers().forEach((player1 -> {
            player1.sendTitle("§c§l陷阱触发！", "&e触发者 " + gamePlayer.getGameTeam().getName() + gamePlayer.getName(), 0, 40, 0);
            player1.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
        })));
    }

    /**
     * 触发挖掘疲劳陷阱
     *
     * @param player 触发陷阱的玩家
     * @param gameTeam 拥有陷阱的团队
     */
    private void triggerMinerTrap(GamePlayer player, GameTeam gameTeam) {
        TrapManager trapManager = gameTeam.getTrapManager();
        trapManager.deactivateTrap(TrapType.MINER);

        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            PotionEffectType miningFatigue = XPotion.MINING_FATIGUE.get();
            if (miningFatigue != null) {
                player.getPlayer().addPotionEffect(new PotionEffect(miningFatigue,
                    CONFIG.getUpgrade().getMiningFatigueEffectDuration(),
                    CONFIG.getUpgrade().getMiningFatigueEffectAmplifier()));
            }
        });

        // 通知团队成员陷阱被触发
        announceTrapTrigger(gameTeam);
    }

    /**
     * 通知团队成员陷阱被触发
     *
     * @param gameTeam 游戏团队
     */
    private void announceTrapTrigger(GameTeam gameTeam) {
        AzuraBedWars.getInstance().mainThreadRunnable(() -> gameTeam.getAlivePlayers().forEach((player1 -> {
            player1.sendTitle("§c§l陷阱触发！", null, 0, 40, 0);
            player1.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
        })));
    }

    /**
     * 启动资源生成器
     *
     * @param gameManager 游戏实例
     */
    private void startResourceGenerators(GameManager gameManager) {
        new GeneratorTask(gameManager).start();
    }

    /**
     * 启动指南针追踪系统
     */
    private void startCompassTracking() {
        new SpectatorCompassTask().start();
    }
}
