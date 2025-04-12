package cc.azuramc.bedwars.game.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.event.GameEvent;
import cc.azuramc.bedwars.game.timer.CompassRunnable;
import cc.azuramc.bedwars.game.timer.GeneratorRunnable;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
import cc.azuramc.bedwars.compat.potioneffect.PotionEffectUtil;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * 游戏开始事件
 * 负责处理游戏开始时的初始化和团队升级效果
 */
public class StartEvent extends GameEvent {
    // 游戏开始倒计时时间（秒）
    private static final int START_COUNTDOWN_SECONDS = 5;
    
    // 开始事件优先级
    private static final int START_EVENT_PRIORITY = 0;
    
    // 团队升级任务名称
    private static final String TEAM_UPGRADE_TASK_NAME = "团队升级";
    
    // 药水效果相关常量
    private static final int HASTE_EFFECT_DURATION = 40;
    private static final int REGENERATION_EFFECT_DURATION = 60;
    private static final int REGENERATION_EFFECT_AMPLIFIER = 1;
    private static final int TRAP_EFFECT_DURATION = 200;
    private static final int TRAP_EFFECT_AMPLIFIER = 1;
    private static final int MINING_FATIGUE_EFFECT_DURATION = 200;
    private static final int MINING_FATIGUE_EFFECT_AMPLIFIER = 0;
    
    // 距离阈值常量
    private static final double HEALING_POOL_RANGE = 7.0;
    private static final double TRAP_TRIGGER_RANGE = 20.0;
    
    // 标题显示常量
    private static final int TITLE_FADE_IN = 1;
    private static final int TITLE_DURATION = 20;
    private static final int TITLE_FADE_OUT = 1;
    
    /**
     * 创建游戏开始事件
     */
    public StartEvent() {
        super("开始游戏", START_COUNTDOWN_SECONDS, START_EVENT_PRIORITY);
    }

    /**
     * 处理游戏开始倒计时
     *
     * @param gameManager 游戏实例
     * @param seconds 剩余秒数
     */
    @Override
    public void executeRunnable(GameManager gameManager, int seconds) {
        gameManager.broadcastSound(SoundUtil.CLICK(), 1f, 1f);
        gameManager.broadcastTitle(TITLE_FADE_IN, TITLE_DURATION, TITLE_FADE_OUT, "§c§l游戏即将开始", "§e§l" + seconds);
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
        gameManager.getEventManager().registerRunnable(TEAM_UPGRADE_TASK_NAME, (s, c) ->
            GamePlayer.getOnlinePlayers().forEach(player -> {
                if (player.isSpectator()) {
                    return;
                }
                
                for (GameTeam gameTeam : gameManager.getGameTeams()) {
                    // 跳过不同世界的团队
                    if (!Objects.equals(player.getPlayer().getLocation().getWorld(), gameTeam.getSpawn().getWorld())) {
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
        if (gameTeam.getManicMiner() <= 0) {
            return;
        }
        
        AzuraBedWars.getInstance().mainThreadRunnable(() -> gameTeam.getAlivePlayers().forEach((player -> {
            PotionEffectType fastDigging = PotionEffectUtil.HASTE();
            if (fastDigging != null) {
                player.getPlayer().addPotionEffect(new PotionEffect(fastDigging, HASTE_EFFECT_DURATION, 
                        gameTeam.getManicMiner()));
            }
        })));
    }
    
    /**
     * 为团队基地内的玩家应用治愈池效果
     *
     * @param player 游戏玩家
     * @param gameTeam 游戏团队
     */
    private void applyHealingPoolEffect(GamePlayer player, GameTeam gameTeam) {
        double distance = player.getPlayer().getLocation().distance(gameTeam.getSpawn());
        
        if (distance <= HEALING_POOL_RANGE && gameTeam.isHealPool()) {
            AzuraBedWars.getInstance().mainThreadRunnable(() -> {
                PotionEffectType regeneration = PotionEffectUtil.REGENERATION();
                if (regeneration != null) {
                    player.getPlayer().addPotionEffect(new PotionEffect(regeneration, 
                            REGENERATION_EFFECT_DURATION, REGENERATION_EFFECT_AMPLIFIER));
                }
            });
        }
    }
    
    /**
     * 处理敌方玩家进入团队领地触发的陷阱
     *
     * @param player 游戏玩家
     * @param gameTeam 游戏团队
     */
    private void handleEnemyInTeamTerritory(GamePlayer player, GameTeam gameTeam) {
        double distance = player.getPlayer().getLocation().distance(gameTeam.getSpawn());
        
        if (distance <= TRAP_TRIGGER_RANGE && !gameTeam.isDead()) {
            // 触发普通陷阱
            if (gameTeam.isTrap()) {
                triggerTrap(player, gameTeam);
            }
            
            // 触发挖掘疲劳陷阱
            if (gameTeam.isMiner()) {
                triggerMiningFatigueTrap(player, gameTeam);
            }
        }
    }
    
    /**
     * 触发团队陷阱效果
     *
     * @param player 触发陷阱的玩家
     * @param gameTeam 拥有陷阱的团队
     */
    private void triggerTrap(GamePlayer player, GameTeam gameTeam) {
        gameTeam.setTrap(false);

        // 给敌方玩家添加失明效果
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            PotionEffectType blindness = PotionEffectUtil.BLINDNESS();
            if (blindness != null) {
                player.getPlayer().addPotionEffect(new PotionEffect(blindness, 
                        TRAP_EFFECT_DURATION, TRAP_EFFECT_AMPLIFIER));
            }
        });

        // 给敌方玩家添加缓慢效果
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            PotionEffectType slowness = PotionEffectUtil.SLOWNESS();
            if (slowness != null) {
                player.getPlayer().addPotionEffect(new PotionEffect(slowness, 
                        TRAP_EFFECT_DURATION, TRAP_EFFECT_AMPLIFIER));
            }
        });

        // 通知团队成员陷阱被触发
        AzuraBedWars.getInstance().mainThreadRunnable(() -> gameTeam.getAlivePlayers().forEach((player1 -> {
            player1.sendTitle(0, 20, 0, "§c§l陷阱触发！", null);
            SoundUtil.playEndermanTeleportSound(player1);
        })));
    }
    
    /**
     * 触发挖掘疲劳陷阱
     *
     * @param player 触发陷阱的玩家
     * @param gameTeam 拥有陷阱的团队
     */
    private void triggerMiningFatigueTrap(GamePlayer player, GameTeam gameTeam) {
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            PotionEffectType miningFatigue = PotionEffectUtil.MINING_FATIGUE();
            if (miningFatigue != null) {
                player.getPlayer().addPotionEffect(new PotionEffect(miningFatigue, 
                        MINING_FATIGUE_EFFECT_DURATION, MINING_FATIGUE_EFFECT_AMPLIFIER));
            }
        });
        gameTeam.setMiner(false);
    }
    
    /**
     * 启动资源生成器
     *
     * @param gameManager 游戏实例
     */
    private void startResourceGenerators(GameManager gameManager) {
        new GeneratorRunnable(gameManager).start();
    }
    
    /**
     * 启动指南针追踪系统
     */
    private void startCompassTracking() {
        new CompassRunnable().start();
    }
}
