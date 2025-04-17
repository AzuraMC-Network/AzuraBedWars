package cc.azuramc.bedwars.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.EventConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.event.GameEvent;
import cc.azuramc.bedwars.spectator.task.SpectatorCompassTask;
import cc.azuramc.bedwars.game.task.GeneratorTask;
import cc.azuramc.bedwars.compat.wrapper.SoundWrapper;
import cc.azuramc.bedwars.compat.wrapper.PotionEffectWrapper;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * 游戏开始事件
 * 负责处理游戏开始时的初始化和团队升级效果
 */
public class GameStartEvent extends GameEvent {

    private static final AzuraBedWars plugin = AzuraBedWars.getInstance();
    private static final EventConfig.StartEvent config = plugin.getEventConfig().getStartEvent();

    /**
     * 创建游戏开始事件
     */
    public GameStartEvent() {
        super(config.getEventName(), config.getCountDown(), config.getEventPriority());
    }

    /**
     * 处理游戏开始倒计时
     *
     * @param gameManager 游戏实例
     * @param seconds 剩余秒数
     */
    @Override
    public void executeRunnable(GameManager gameManager, int seconds) {
        gameManager.broadcastSound(SoundWrapper.CLICK(), 1f, 1f);
        gameManager.broadcastTitle(
            config.getTitle().getFadeIn(),
            config.getTitle().getTitleStay(),
            config.getTitle().getFadeOut(),
            config.getTitle().getTitleString(),
            config.getTitle().getSubtitle() + seconds
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
        gameManager.getGameEventManager().registerRunnable(config.getTeamUpgradeTaskName(), (s, c) ->
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
            PotionEffectType fastDigging = PotionEffectWrapper.HASTE();
            if (fastDigging != null) {
                player.getPlayer().addPotionEffect(new PotionEffect(fastDigging, 
                    config.getUpgrade().getHasteEffectDuration(), 
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
        
        if (distance <= config.getUpgrade().getHealingPoolRange() && gameTeam.isHealPool()) {
            AzuraBedWars.getInstance().mainThreadRunnable(() -> {
                PotionEffectType regeneration = PotionEffectWrapper.REGENERATION();
                if (regeneration != null) {
                    player.getPlayer().addPotionEffect(new PotionEffect(regeneration, 
                        config.getUpgrade().getRegenerationEffectDuration(),
                        config.getUpgrade().getRegenerationEffectAmplifier()));
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
        
        if (distance <= config.getUpgrade().getTrapTriggerRange() && !gameTeam.isDead()) {
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
            PotionEffectType blindness = PotionEffectWrapper.BLINDNESS();
            if (blindness != null) {
                player.getPlayer().addPotionEffect(new PotionEffect(blindness, 
                    config.getUpgrade().getTrapEffectDuration(),
                    config.getUpgrade().getTrapEffectAmplifier()));
            }
        });

        // 给敌方玩家添加缓慢效果
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            PotionEffectType slowness = PotionEffectWrapper.SLOWNESS();
            if (slowness != null) {
                player.getPlayer().addPotionEffect(new PotionEffect(slowness, 
                    config.getUpgrade().getTrapEffectDuration(),
                    config.getUpgrade().getTrapEffectAmplifier()));
            }
        });

        // 通知团队成员陷阱被触发
        AzuraBedWars.getInstance().mainThreadRunnable(() -> gameTeam.getAlivePlayers().forEach((player1 -> {
            player1.sendTitle(0, 20, 0, "§c§l陷阱触发！", null);
            SoundWrapper.playEndermanTeleportSound(player1);
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
            PotionEffectType miningFatigue = PotionEffectWrapper.MINING_FATIGUE();
            if (miningFatigue != null) {
                player.getPlayer().addPotionEffect(new PotionEffect(miningFatigue, 
                    config.getUpgrade().getMiningFatigueEffectDuration(),
                    config.getUpgrade().getMiningFatigueEffectAmplifier()));
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
        new GeneratorTask(gameManager).start();
    }
    
    /**
     * 启动指南针追踪系统
     */
    private void startCompassTracking() {
        new SpectatorCompassTask().start();
    }
}
