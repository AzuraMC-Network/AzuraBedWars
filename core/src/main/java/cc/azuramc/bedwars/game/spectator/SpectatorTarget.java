package cc.azuramc.bedwars.game.spectator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.PlayerConfig;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

/**
 * 旁观目标管理类
 * 管理旁观者的目标玩家，包括目标跟踪、传送和状态显示
 *
 * @author an5w1r@163.com
 */
public class SpectatorTarget {

    private static final MessageConfig.Spectator spectatorConfig = AzuraBedWars.getInstance().getMessageConfig().getSpectator();
    private static final PlayerConfig.Spectator.Target CONFIG = AzuraBedWars.getInstance().getPlayerConfig().getSpectator().getTarget();

    private static final double AUTO_TP_DISTANCE = CONFIG.getAutoTPDistance();
    private static final int TITLE_FADE_IN = 0;
    private static final int TITLE_DURATION = CONFIG.getTitleDuration();
    private static final int TITLE_FADE_OUT = 0;

    private final DecimalFormat df = new DecimalFormat("0.0");
    private final GamePlayer gamePlayer;
    private GamePlayer gameTarget;
    private final Player player;
    private Player target;

    /**
     * 构造函数
     *
     * @param gamePlayer 旁观者
     * @param gameTarget 目标玩家
     */
    public SpectatorTarget(GamePlayer gamePlayer, GamePlayer gameTarget) {
        this.gamePlayer = gamePlayer;
        this.player = gamePlayer.getPlayer();
        this.gameTarget = gameTarget;
        this.target = gameTarget != null ? gameTarget.getPlayer() : null;
    }

    /**
     * 获取旁观者
     *
     * @return 旁观者
     */
    public GamePlayer getPlayer() {
        return gamePlayer;
    }

    /**
     * 获取目标玩家
     *
     * @return 目标玩家
     */
    public GamePlayer getTarget() {
        return gameTarget;
    }

    /**
     * 设置目标玩家
     *
     * @param gameTarget 新的目标玩家
     */
    public void setTarget(GamePlayer gameTarget) {
        this.gameTarget = gameTarget;
        this.target = gameTarget != null ? gameTarget.getPlayer() : null;
    }

    /**
     * 发送目标状态提示
     */
    public void sendTip() {
        if (isTargetInvalid()) {
            return;
        }

        // 第一人称旁观模式
        if (isFirstPersonViewActive()) {
            String actionBarText = String.format(spectatorConfig.getFirstPersonActionBar(),
                    target.getName(), (int) target.getHealth()) + spectatorConfig.getMenuHint();
            gamePlayer.sendActionBar(actionBarText);
            return;
        }

        // 检查是否在同一世界
        if (!isSameWorld()) {
            gamePlayer.sendActionBar(spectatorConfig.getTargetLostMessage());
            return;
        }

        // 第三人称旁观模式
        String actionBarText = String.format(spectatorConfig.getThirdPersonActionBar(),
            target.getName(), (int)target.getHealth(), df.format(getDistance()));
        gamePlayer.sendActionBar(actionBarText);
    }

    /**
     * 自动传送到目标玩家
     */
    public void autoTp() {
        if (isTargetInvalid()) {
            return;
        }

        SpectatorSettings settings = SpectatorSettings.get(gamePlayer);
        if (settings.getOption(SpectatorSettings.Option.AUTO_TP)) {
            if (!isSameWorld() || getDistance() >= AUTO_TP_DISTANCE) {
                teleportToTarget();

                if (settings.getOption(SpectatorSettings.Option.FIRST_PERSON)) {
                    activateFirstPersonView();
                }
            }
        }
    }

    /**
     * 传送到目标玩家
     */
    public void tp() {
        if (isTargetInvalid()) {
            return;
        }

        if (SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRST_PERSON)) {
            teleportToTarget();
            activateFirstPersonView();
            return;
        }

        teleportToTarget();
    }

    /**
     * 检查目标是否有效
     *
     * @return 目标是否无效
     */
    public boolean isTargetInvalid() {
        if (gameTarget == null || target == null) {
            return true;
        }

        if (gameTarget.isSpectator() || !target.isOnline()) {
            clearTarget();
            return true;
        }

        return false;
    }

    /**
     * 清除目标
     */
    private void clearTarget() {
        gameTarget = null;
        target = null;
    }

    /**
     * 检查是否在同一世界
     *
     * @return 是否在同一世界
     */
    private boolean isSameWorld() {
        return player.getWorld().equals(target.getWorld());
    }

    /**
     * 获取与目标的距离
     *
     * @return 距离
     */
    private double getDistance() {
        if (isSameWorld()) {
            return player.getLocation().distance(target.getLocation());
        }
        return Double.MAX_VALUE;
    }

    /**
     * 传送到目标位置
     */
    private void teleportToTarget() {
        if (target != null) {
            player.teleport(target);
        }
    }

    /**
     * 激活第一人称视角
     */
    private void activateFirstPersonView() {
        gamePlayer.sendTitle(
                String.format(spectatorConfig.getFirstPersonTitle(), target.getName()), spectatorConfig.getFirstPersonSubTitle(), TITLE_FADE_IN,
                TITLE_DURATION,
            TITLE_FADE_OUT
        );
        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(target);
    }

    /**
     * 检查是否处于第一人称视角
     *
     * @return 是否处于第一人称视角
     */
    private boolean isFirstPersonViewActive() {
        return player.getSpectatorTarget() != null && player.getSpectatorTarget().equals(target);
    }
}
