package cc.azuramc.bedwars.spectator;

import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

/**
 * 旁观目标管理类
 * <p>
 * 管理旁观者的目标玩家，包括目标跟踪、传送和状态显示
 * </p>
 */
public class SpectatorTarget {
    // 常量定义
    private static final String TARGET_LOST_MESSAGE = "§c§l目标已丢失或不在同一个世界";
    private static final String FIRST_PERSON_TITLE = "§a正在旁观§7%s";
    private static final String FIRST_PERSON_SUBTITLE = "§a点击左键打开菜单  §c按Shift键退出";
    private static final String FIRST_PERSON_ACTION_BAR = "§f目标: §a§l%s  §f生命值: §a§l%d §c§l❤";
    private static final String THIRD_PERSON_ACTION_BAR = "§f目标: §a§l%s  §f生命值: §a§l%d  §f距离: §a§l%s米";
    private static final String MENU_HINT = "  §a点击左键打开菜单  §c按Shift退出";
    private static final double AUTO_TP_DISTANCE = 20.0D;
    private static final int TITLE_FADE_IN = 0;
    private static final int TITLE_DURATION = 20;
    private static final int TITLE_FADE_OUT = 0;
    
    // 实例变量
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
            String actionBarText = String.format(FIRST_PERSON_ACTION_BAR, 
                target.getName(), (int)target.getHealth()) + MENU_HINT;
            gamePlayer.sendActionBar(actionBarText);
            return;
        }

        // 检查是否在同一世界
        if (!isSameWorld()) {
            gamePlayer.sendActionBar(TARGET_LOST_MESSAGE);
            return;
        }

        // 第三人称旁观模式
        String actionBarText = String.format(THIRD_PERSON_ACTION_BAR, 
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
        if (settings.getOption(SpectatorSettings.Option.AUTOTP)) {
            if (!isSameWorld() || getDistance() >= AUTO_TP_DISTANCE) {
                teleportToTarget();

                if (settings.getOption(SpectatorSettings.Option.FIRSTPERSON)) {
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

        if (SpectatorSettings.get(gamePlayer).getOption(SpectatorSettings.Option.FIRSTPERSON)) {
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
            TITLE_FADE_IN, 
            TITLE_DURATION, 
            TITLE_FADE_OUT, 
            String.format(FIRST_PERSON_TITLE, target.getName()), 
            FIRST_PERSON_SUBTITLE
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
