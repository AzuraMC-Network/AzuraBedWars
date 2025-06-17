package cc.azuramc.bedwars.listener.player;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.PlayerConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import cc.azuramc.bedwars.game.team.GameTeam;
import com.cryptomorin.xseries.XSound;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 玩家重生事件监听器
 * <p>
 * 负责处理玩家死亡后的重生逻辑，包括床存在和床被摧毁的两种情况。
 * 同时也处理玩家重生后的无敌时间和工具等级降级。
 * </p>
 * @author an5w1r@163.com
 */
public class PlayerRespawnListener implements Listener {

    private final static PlayerConfig.PlayerRespawn CONFIG = AzuraBedWars.getInstance().getPlayerConfig().getPlayerRespawn();
    private final static MessageConfig.PlayerRespawn MESSAGE_CONFIG = AzuraBedWars.getInstance().getMessageConfig().getPlayerRespawn();

    private static final int RESPAWN_COUNTDOWN_SECONDS = CONFIG.getRespawnCountdownSeconds();
    private static final int RESPAWN_PROTECTION_TICKS = CONFIG.getRespawnProtectionTicks();
    private static final int TITLE_FADE_IN = 1;
    private static final int TITLE_STAY = CONFIG.getTitleStay();
    private static final int TITLE_FADE_OUT = 1;
    private static final long RESPAWN_DELAY_TICKS = 1L;
    private static final long RESPAWN_TIMER_PERIOD = 20L;

    private static final String RESPAWN_COUNTDOWN_TITLE = MESSAGE_CONFIG.getRespawnCountdownTitle();
    private static final String RESPAWN_COUNTDOWN_SUBTITLE = MESSAGE_CONFIG.getRespawnCountdownSubTitle();
    private static final String RESPAWN_COMPLETE_TITLE = MESSAGE_CONFIG.getRespawnCompleteTitle();
    private static final String RESPAWN_COMPLETE_SUBTITLE = MESSAGE_CONFIG.getRespawnCompleteSubTitle();
    private static final String DEATH_PERMANENT_TITLE = MESSAGE_CONFIG.getDeathPermanentTitle();
    private static final String DEATH_PERMANENT_SUBTITLE = MESSAGE_CONFIG.getDeathPermanentSubTitle();
    private static final String TEAM_ELIMINATED_FORMAT = MESSAGE_CONFIG.getTeamEliminatedFormat();
    private static final String TEAM_ELIMINATED_MSG = MESSAGE_CONFIG.getTeamEliminatedMessage();
    private static final String REJOIN_MESSAGE = MESSAGE_CONFIG.getRejoinMessage();
    private static final String REJOIN_BUTTON = MESSAGE_CONFIG.getRejoinButton();
    private static final String REJOIN_COMMAND = MESSAGE_CONFIG.getRejoinCommand();
    
    private final List<UUID> noDamage = new ArrayList<>();
    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    /**
     * 处理玩家重生事件
     *
     * @param event 玩家重生事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player);

        GameTeam gameTeam = null;
        if (gamePlayer != null) {
            gameTeam = gamePlayer.getGameTeam();
        }

        // 游戏未运行时不处理
        if (gameManager.getGameState() != GameState.RUNNING) {
            return;
        }

        if (gamePlayer != null) {
            gamePlayer.cleanState();
        }

        // 如果玩家的床已经被摧毁，处理永久死亡
        if (gameTeam != null && gameTeam.isDestroyed()) {
            handlePermanentDeath(event, gamePlayer, gameTeam);
            return;
        }

        // 处理临时死亡（床还在）
        handleTemporaryDeath(event, gamePlayer, gameTeam);
    }

    /**
     * 处理玩家受伤事件，提供重生保护
     *
     * @param evt 实体受伤事件
     */
    @EventHandler
    public void onDamage(EntityDamageEvent evt) {
        if (noDamage.contains(evt.getEntity().getUniqueId())) {
            evt.setCancelled(true);
        }
    }

    /**
     * 处理床被摧毁后的永久死亡
     *
     * @param event 玩家重生事件
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在队伍
     */
    private void handlePermanentDeath(PlayerRespawnEvent event, GamePlayer gamePlayer, GameTeam gameTeam) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 显示重新加入游戏的提示
                sendRejoinMessage(gamePlayer);

                // 设置重生位置和移动状态
                teleportToRespawnLocation(event);
                
                // 隐藏死亡玩家
                GamePlayer.getOnlinePlayers().forEach(otherPlayer -> 
                    PlayerUtil.hidePlayer(otherPlayer.getPlayer(), gamePlayer.getPlayer()));

                // 设置为观察者
                gamePlayer.toSpectator(DEATH_PERMANENT_TITLE, DEATH_PERMANENT_SUBTITLE);
            }
        }.runTaskLater(AzuraBedWars.getInstance(), RESPAWN_DELAY_TICKS);
        
        gamePlayer.getPlayerData().addLosses();

        // 如果整个队伍都被消灭，广播消息
        if (gameTeam.isDead()) {
            announceTeamElimination(gameTeam);
        }
    }

    /**
     * 发送重新加入游戏的消息
     *
     * @param gamePlayer 游戏玩家
     */
    private void sendRejoinMessage(GamePlayer gamePlayer) {
        TextComponent textComponent = new TextComponent(REJOIN_MESSAGE);
        textComponent.addExtra(REJOIN_BUTTON);
        textComponent.getExtra().getFirst().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, REJOIN_COMMAND));
        gamePlayer.getPlayer().spigot().sendMessage(textComponent);
    }

    /**
     * 广播队伍被消灭的消息
     *
     * @param gameTeam 被消灭的队伍
     */
    private void announceTeamElimination(GameTeam gameTeam) {
        String destroyerName = gameTeam.getDestroyPlayer() != null ? gameTeam.getDestroyPlayer().getNickName() : "null";
        
        gameManager.broadcastSound(XSound.ENTITY_ENDER_DRAGON_HURT.get(), 10, 10);
        gameManager.broadcastMessage(TEAM_ELIMINATED_FORMAT);
        gameManager.broadcastMessage(" ");
        gameManager.broadcastMessage(String.format(TEAM_ELIMINATED_MSG, gameTeam.getChatColor() + gameTeam.getName(), destroyerName));
        gameManager.broadcastMessage(" ");
        gameManager.broadcastMessage(TEAM_ELIMINATED_FORMAT);
    }

    /**
     * 处理床存在时的临时死亡
     *
     * @param event 玩家重生事件
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在队伍
     */
    private void handleTemporaryDeath(PlayerRespawnEvent event, GamePlayer gamePlayer, GameTeam gameTeam) {
        Player player = event.getPlayer();
        // 设置重生位置和玩家状态
        teleportToRespawnLocation(event);
        
        player.setGameMode(GameMode.SPECTATOR);
        player.setFlying(true);

        // 启动重生倒计时
        new BukkitRunnable() {
            int delay = RESPAWN_COUNTDOWN_SECONDS;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                if (this.delay > 0) {
                    // 显示倒计时
                    gamePlayer.sendTitle(String.format(RESPAWN_COUNTDOWN_TITLE, delay), RESPAWN_COUNTDOWN_SUBTITLE,
                            TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT);
                    this.delay -= 1;
                    return;
                }

                // 重置玩家状态
                resetPlayerState(gamePlayer, gameTeam);
                cancel();
            }
        }.runTaskTimer(AzuraBedWars.getInstance(), RESPAWN_TIMER_PERIOD, RESPAWN_TIMER_PERIOD);
    }

    /**
     * 重置玩家状态，完成重生
     *
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在队伍
     */
    private void resetPlayerState(GamePlayer gamePlayer, GameTeam gameTeam) {
        Player player = gamePlayer.getPlayer();
        // 重置经验和等级
        player.setExp(0f);
        player.setLevel(0);

        // 降级工具等级
        degradeTools(gamePlayer);

        // 恢复玩家物品栏
        gamePlayer.giveInventory();
        
        // 使玩家对所有人可见
        PlayerUtil.showPlayer(player, player);
        GamePlayer.getOnlinePlayers().forEach(otherPlayer -> 
            PlayerUtil.showPlayer(otherPlayer.getPlayer(), player));
        
        // 传送到队伍出生点
        player.teleport(gameTeam.getSpawnLocation());
        player.setGameMode(GameMode.SURVIVAL);
        
        // 添加临时伤害保护
        applyDamageProtection(gamePlayer);

        // 显示重生成功标题
        gamePlayer.sendTitle(RESPAWN_COMPLETE_TITLE, RESPAWN_COMPLETE_SUBTITLE, TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT);
    }

    /**
     * 降级玩家的工具等级
     *
     * @param gamePlayer 游戏玩家
     */
    private void degradeTools(GamePlayer gamePlayer) {
        // 降级稿子
        if (gamePlayer.getPickaxeType() != ToolType.NONE) {
            gamePlayer.setPickaxeType(degradeToolLevel(gamePlayer.getPickaxeType()));
        }

        // 降级斧头
        if (gamePlayer.getAxeType() != ToolType.NONE) {
            gamePlayer.setAxeType(degradeToolLevel(gamePlayer.getAxeType()));
        }
    }

    /**
     * 降低工具等级
     *
     * @param currentType 当前工具等级
     * @return 降级后的工具等级
     */
    private ToolType degradeToolLevel(ToolType currentType) {
        return switch (currentType) {
            case DIAMOND -> ToolType.IRON;
            case IRON -> ToolType.STONE;
            case STONE -> ToolType.WOOD;
            default -> currentType;
        };
    }

    /**
     * 应用伤害保护效果
     *
     * @param gamePlayer 游戏玩家玩家
     */
    private void applyDamageProtection(GamePlayer gamePlayer) {
        noDamage.add(gamePlayer.getUuid());
        Bukkit.getScheduler().runTaskLater(
            AzuraBedWars.getInstance(), 
            () -> noDamage.remove(gamePlayer.getUuid()),
            RESPAWN_PROTECTION_TICKS
        );
    }

    /**
     * 传送到重生位置并重置移动状态
     *
     * @param event 玩家重生事件
     */
    private void teleportToRespawnLocation(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        event.setRespawnLocation(gameManager.getRespawnLocation());
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0.0F);
        player.teleport(gameManager.getRespawnLocation());
    }
}
