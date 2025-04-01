package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.utils.TitleUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.types.ToolType;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
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
 */
public class RespawnListener implements Listener {
    // 常量定义
    private static final int RESPAWN_COUNTDOWN_SECONDS = 5;
    private static final int RESPAWN_PROTECTION_TICKS = 60;
    private static final int TITLE_FADE_IN = 1;
    private static final int TITLE_STAY = 20;
    private static final int TITLE_FADE_OUT = 1;
    private static final long RESPAWN_DELAY_TICKS = 1L;
    private static final long RESPAWN_TIMER_PERIOD = 20L;
    
    // 消息常量
    private static final String RESPAWN_COUNTDOWN_TITLE = "§e§l%d";
    private static final String RESPAWN_COUNTDOWN_SUBTITLE = "§7你死了,将在稍后重生";
    private static final String RESPAWN_COMPLETE_TITLE = "§a已复活！";
    private static final String RESPAWN_COMPLETE_SUBTITLE = "§7因为你的床还在所以你复活了";
    private static final String DEATH_PERMANENT_TITLE = "§c你凉了！";
    private static final String DEATH_PERMANENT_SUBTITLE = "§7你没床了";
    private static final String TEAM_ELIMINATED_FORMAT = "§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃";
    private static final String TEAM_ELIMINATED_MSG = "%s §c凉了! §e挖床者: %s";
    private static final String REJOIN_MESSAGE = "§c你凉了!想再来一局嘛? ";
    private static final String REJOIN_BUTTON = "§b§l点击这里!";
    private static final String REJOIN_COMMAND = "/queue join qc x";
    
    private final List<UUID> noDamage = new ArrayList<>();
    private final Game game = AzuraBedWars.getInstance().getGame();

    /**
     * 处理玩家重生事件
     *
     * @param event 玩家重生事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

        GameTeam gameTeam = null;
        if (gamePlayer != null) {
            gameTeam = gamePlayer.getGameTeam();
        }

        PlayerData playerData = null;
        if (gamePlayer != null) {
            playerData = gamePlayer.getPlayerData();
        }

        // 游戏未运行时不处理
        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        if (gamePlayer != null) {
            gamePlayer.clean();
        }

        // 如果玩家的床已经被摧毁，处理永久死亡
        if (gameTeam != null && gameTeam.isBedDestroy()) {
            handlePermanentDeath(event, player, gamePlayer, gameTeam, playerData);
            return;
        }

        // 处理临时死亡（床还在）
        handleTemporaryDeath(event, player, gamePlayer, gameTeam);
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
     * @param player 玩家
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在队伍
     * @param playerData 玩家数据
     */
    private void handlePermanentDeath(PlayerRespawnEvent event, Player player, GamePlayer gamePlayer, GameTeam gameTeam, PlayerData playerData) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 显示重新加入游戏的提示
                sendRejoinMessage(player);

                // 设置重生位置和移动状态
                teleportToRespawnLocation(event, player);
                
                // 隐藏死亡玩家
                GamePlayer.getOnlinePlayers().forEach(otherPlayer -> 
                    PlayerUtil.hidePlayer(otherPlayer.getPlayer(), player));

                // 设置为观察者
                gamePlayer.toSpectator(DEATH_PERMANENT_TITLE, DEATH_PERMANENT_SUBTITLE);
            }
        }.runTaskLater(AzuraBedWars.getInstance(), RESPAWN_DELAY_TICKS);
        
        playerData.addLoses();

        // 如果整个队伍都被消灭，广播消息
        if (gameTeam.isDead()) {
            announceTeamElimination(gameTeam);
        }
    }

    /**
     * 发送重新加入游戏的消息
     *
     * @param player 玩家
     */
    private void sendRejoinMessage(Player player) {
        TextComponent textComponent = new TextComponent(REJOIN_MESSAGE);
        textComponent.addExtra(REJOIN_BUTTON);
        textComponent.getExtra().getFirst().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, REJOIN_COMMAND));
        player.spigot().sendMessage(textComponent);
    }

    /**
     * 广播队伍被消灭的消息
     *
     * @param gameTeam 被消灭的队伍
     */
    private void announceTeamElimination(GameTeam gameTeam) {
        String destroyerName = gameTeam.getDestroyPlayer() != null ? gameTeam.getDestroyPlayer().getDisplayname() : "null";
        
        game.broadcastSound(SoundUtil.ENDERDRAGON_HIT(), 10, 10);
        game.broadcastMessage(TEAM_ELIMINATED_FORMAT);
        game.broadcastMessage(" ");
        game.broadcastMessage(String.format(TEAM_ELIMINATED_MSG, gameTeam.getChatColor() + gameTeam.getName(), destroyerName));
        game.broadcastMessage(" ");
        game.broadcastMessage(TEAM_ELIMINATED_FORMAT);
    }

    /**
     * 处理床存在时的临时死亡
     *
     * @param event 玩家重生事件
     * @param player 玩家
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在队伍
     */
    private void handleTemporaryDeath(PlayerRespawnEvent event, Player player, GamePlayer gamePlayer, GameTeam gameTeam) {
        // 设置重生位置和玩家状态
        teleportToRespawnLocation(event, player);
        
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
                    TitleUtil.sendTitle(player, TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT, 
                        String.format(RESPAWN_COUNTDOWN_TITLE, delay), RESPAWN_COUNTDOWN_SUBTITLE);
                    this.delay -= 1;
                    return;
                }

                // 重置玩家状态
                resetPlayerState(player, gamePlayer, gameTeam);
                cancel();
            }
        }.runTaskTimer(AzuraBedWars.getInstance(), RESPAWN_TIMER_PERIOD, RESPAWN_TIMER_PERIOD);
    }

    /**
     * 重置玩家状态，完成重生
     *
     * @param player 玩家
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在队伍
     */
    private void resetPlayerState(Player player, GamePlayer gamePlayer, GameTeam gameTeam) {
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
        player.teleport(gameTeam.getSpawn());
        player.setGameMode(GameMode.SURVIVAL);
        
        // 添加临时伤害保护
        applyDamageProtection(player);

        // 显示重生成功标题
        TitleUtil.sendTitle(player, TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT,
            RESPAWN_COMPLETE_TITLE, RESPAWN_COMPLETE_SUBTITLE);
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
     * @param player 玩家
     */
    private void applyDamageProtection(Player player) {
        noDamage.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(
            AzuraBedWars.getInstance(), 
            () -> noDamage.remove(player.getUniqueId()), 
            RESPAWN_PROTECTION_TICKS
        );
    }

    /**
     * 传送到重生位置并重置移动状态
     *
     * @param event 玩家重生事件
     * @param player 玩家
     */
    private void teleportToRespawnLocation(PlayerRespawnEvent event, Player player) {
        event.setRespawnLocation(game.getRespawnLocation());
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0.0F);
        player.teleport(game.getRespawnLocation());
    }
}
