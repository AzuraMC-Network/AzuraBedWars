package cc.azuramc.bedwars.upgrade.task;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.EventConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.upgrade.trap.TrapManager;
import cc.azuramc.bedwars.upgrade.trap.TrapType;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XPotion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * 团队升级任务类
 * 负责处理团队升级效果的持续检测和应用
 *
 * @author an5w1r@163.com
 */
public class TeamUpgradeCheckTask {

    private static final EventConfig.StartEvent CONFIG = AzuraBedWars.getInstance().getEventConfig().getStartEvent();

    private final GameManager gameManager;

    public TeamUpgradeCheckTask(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * 执行团队升级任务
     * 处理所有在线玩家的升级效果和陷阱检测
     */
    public void execute() {
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
        });
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
                // Subtract 1 from the level because Minecraft potion effect levels start at 0
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
     * @param gameTeam   游戏团队
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
     * @param gameTeam   游戏团队
     */
    private void handleEnemyInTeamTerritory(GamePlayer gamePlayer, GameTeam gameTeam) {
        double distance = gamePlayer.getPlayer().getLocation().distance(gameTeam.getSpawnLocation());
        TrapManager trapManager = gameTeam.getTrapManager();

        if (distance <= CONFIG.getUpgrade().getTrapTriggerRange() && !gameTeam.isDead()) {
            // 检查陷阱触发冷却
            if (gamePlayer.isTrapTriggerCooldown()) {
                return;
            }

            // 队列式触发 只触发第一个陷阱
            TrapType firstTrap = trapManager.getFirstActiveTrap();
            if (firstTrap != null) {
                LoggerUtil.debug("TeamUpgradeTask$handleEnemyInTeamTerritory | trigger " + firstTrap.name() + " trap, gamePlayer: " + gamePlayer.getName());

                // 更新玩家陷阱触发时间
                gamePlayer.startTrapTriggerCooldownTask();

                // 触发陷阱
                trapManager.getTrapStrategy(firstTrap).triggerTrap(gamePlayer, gameTeam);
            }
        }
    }
}
