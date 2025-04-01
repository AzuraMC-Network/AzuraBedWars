package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.compat.util.ItemBuilderUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.map.data.MapData;
import cc.azuramc.bedwars.events.BedwarsGameStartEvent;
import cc.azuramc.bedwars.game.event.impl.EventManager;
import cc.azuramc.bedwars.scoreboards.GameBoard;
import cc.azuramc.bedwars.scoreboards.LobbyBoard;
import cc.azuramc.bedwars.shop.ItemShopManager;
import cc.azuramc.bedwars.specials.SpecialItem;
import cc.azuramc.bedwars.utils.Util;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * 游戏管理核心类
 * <p>
 * 负责管理整个起床战争游戏的生命周期、玩家、团队以及游戏状态。
 * 包含游戏开始、运行和结束的逻辑，以及玩家加入、离开和重新连接的处理。
 * </p>
 */
@Data
public class Game {
    // 时间常量
    private static final long COUNTDOWN_TICK_PERIOD = 20L;
    private static final int ASSIST_TIME_WINDOW_MS = 10000;
    
    // 消息常量
    private static final String MSG_PLAYER_RECONNECT = "§7%s§a重连上线";
    private static final String MSG_PLAYER_LEAVE = "§7%s§e离开游戏";
    
    // 物品常量
    private static final Material RESOURCE_SELECTOR_MATERIAL = Material.PAPER;
    private static final String RESOURCE_SELECTOR_NAME = "§a资源类型选择§7(右键选择)";
    private static final Material LEAVE_GAME_MATERIAL = Material.SLIME_BALL;
    private static final String LEAVE_GAME_NAME = "§c离开游戏§7(右键离开)";
    
    private AzuraBedWars main;
    private EventManager eventManager;
    private MapData mapData;
    private GameState gameState;
    private boolean forceStart;

    private Location waitingLocation;
    private Location respawnLocation;

    private List<Location> blocks;
    private GameLobbyCountdown gameLobbyCountdown = null;
    private List<GameTeam> gameTeams;
    private List<GameParty> gameParties;

    private Map<ArmorStand, String> armorSande;
    private Map<ArmorStand, String> armorStand;

    private List<SpecialItem> specialItems;

    /**
     * 创建一个新的游戏实例
     *
     * @param main 插件主类实例
     */
    public Game(AzuraBedWars main) {
        this.main = main;
        this.forceStart = false;
        this.gameTeams = new ArrayList<>();
        this.gameParties = new ArrayList<>();
        this.armorSande = new HashMap<>();
        this.armorStand = new HashMap<>();
        this.specialItems = new ArrayList<>();
        ItemShopManager.init(this);
        this.eventManager = new EventManager(this);
    }

    /**
     * 加载游戏地图和相关设置
     *
     * @param mapData 地图数据
     */
    public void loadGame(MapData mapData) {
        if (mapData == null) {
            return;
        }
        
        this.mapData = mapData;
        this.blocks = mapData.loadMap();
        this.respawnLocation = mapData.getReSpawn().toLocation();
        this.waitingLocation = mapData.getWaitingLocation().toLocation();

        Util.spawnALL(main);

        initializeTeams(mapData);
        this.gameState = GameState.WAITING;
    }

    /**
     * 初始化游戏团队
     *
     * @param mapData 地图数据
     */
    private void initializeTeams(MapData mapData) {
        for (int i = 0; i < mapData.getBases().size(); i++) {
            gameTeams.add(new GameTeam(
                TeamColor.values()[i], 
                mapData.getBases().get(i).toLocation(), 
                mapData.getPlayers().getTeam()
            ));
        }
    }

    /**
     * 玩家加入游戏处理
     *
     * @param gamePlayer 游戏玩家
     */
    public void addPlayer(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        if (gameState == GameState.RUNNING) {
            handlePlayerJoinRunningGame(gamePlayer, player);
            return;
        }

        handlePlayerJoinWaitingGame(gamePlayer, player);
    }

    /**
     * 处理玩家加入等待状态的游戏
     *
     * @param gamePlayer 游戏玩家
     * @param player 原始玩家对象
     */
    private void handlePlayerJoinWaitingGame(GamePlayer gamePlayer, Player player) {
        // 处理玩家可见性
        updatePlayerVisibility(gamePlayer, player);

        // 设置玩家基本状态
        player.spigot().respawn();
        player.setGameMode(GameMode.ADVENTURE);
        player.getEnderChest().clear();
        gamePlayer.cleanState();

        // 传送到等待区域
        player.teleport(waitingLocation);

        // 设置记分板
        LobbyBoard.show(player);
        LobbyBoard.updateBoard();

        // 给予物品
        giveWaitingItems(player);

        // 检查是否可以开始游戏
        checkGameStart();
    }

    /**
     * 更新玩家可见性设置
     * 
     * @param gamePlayer 游戏玩家
     * @param player 原始玩家对象
     */
    private void updatePlayerVisibility(GamePlayer gamePlayer, Player player) {
        // 使当前玩家可见
        for (GamePlayer otherPlayer : GamePlayer.getOnlinePlayers()) {
            Player otherPlayerObj = otherPlayer.getPlayer();
            
            // 跳过已经是观察者的玩家
            if (otherPlayer.isSpectator()) {
                PlayerUtil.hidePlayer(player, otherPlayerObj);
                continue;
            }
            
            // 让所有玩家看到新玩家
            PlayerUtil.showPlayer(otherPlayerObj, player);
            // 让新玩家看到所有玩家
            PlayerUtil.showPlayer(player, otherPlayerObj);
        }
    }

    /**
     * 重置所有玩家可见性
     * 在游戏状态变化时调用此方法
     */
    public void resetAllPlayersVisibility() {
        List<GamePlayer> onlinePlayers = GamePlayer.getOnlinePlayers();
        
        for (GamePlayer player1 : onlinePlayers) {
            Player p1 = player1.getPlayer();
            
            for (GamePlayer player2 : onlinePlayers) {
                Player p2 = player2.getPlayer();
                
                // 跳过自己
                if (player1.equals(player2)) continue;
                
                // 如果是观察者，对其他玩家不可见
                if (player1.isSpectator()) {
                    PlayerUtil.hidePlayer(p2, p1);
                } else if (player2.isSpectator()) {
                    PlayerUtil.hidePlayer(p1, p2);
                } else {
                    // 正常玩家互相可见
                    PlayerUtil.showPlayer(p1, p2);
                    PlayerUtil.showPlayer(p2, p1);
                }
            }
        }
    }

    /**
     * 给予玩家等待时的物品
     *
     * @param player 玩家
     */
    private void giveWaitingItems(Player player) {
        player.getInventory().addItem(
            new ItemBuilderUtil()
                .setType(RESOURCE_SELECTOR_MATERIAL)
                .setDisplayName(RESOURCE_SELECTOR_NAME)
                .getItem()
        );
        
        player.getInventory().setItem(8, 
            new ItemBuilderUtil()
                .setType(LEAVE_GAME_MATERIAL)
                .setDisplayName(LEAVE_GAME_NAME)
                .getItem()
        );
    }

    /**
     * 处理玩家加入正在运行的游戏
     *
     * @param gamePlayer 游戏玩家
     * @param player 原始玩家对象
     */
    private void handlePlayerJoinRunningGame(GamePlayer gamePlayer, Player player) {
        // 设置记分板
        GameBoard.show(player);
        GameBoard.updateBoard();

        // 检查玩家团队状态
        if (gamePlayer.getGameTeam() != null) {
            if (!gamePlayer.getGameTeam().isDead()) {
                Util.setPlayerTeamTab();
                PlayerUtil.callPlayerRespawnEvent(player, respawnLocation);
                broadcastMessage(String.format(MSG_PLAYER_RECONNECT, gamePlayer.getNickName()));
                return;
            }
        }

        // 如果没有有效团队，设为观察者
        gamePlayer.toSpectator(null, null);
    }

    /**
     * 检查是否可以开始游戏倒计时
     */
    private void checkGameStart() {
        if (isStartable() && gameState == GameState.WAITING && getGameLobbyCountdown() == null) {
            GameLobbyCountdown lobbyCountdown = new GameLobbyCountdown(this);
            lobbyCountdown.runTaskTimer(main, COUNTDOWN_TICK_PERIOD, COUNTDOWN_TICK_PERIOD);
            setGameLobbyCountdown(lobbyCountdown);
        }
    }

    /**
     * 处理玩家离开游戏
     *
     * @param gamePlayer 游戏玩家
     */
    public void removePlayers(GamePlayer gamePlayer) {
        if (gameState == GameState.WAITING) {
            broadcastMessage(String.format(MSG_PLAYER_LEAVE, gamePlayer.getNickName()));
        }

        if (gameState == GameState.RUNNING && gamePlayer.isSpectator()) {
            return;
        }

        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }

        // 处理玩家离开对团队的影响
        handleTeamPlayerLeave(gamePlayer, gameTeam);
    }

    /**
     * 处理玩家离开对团队的影响
     *
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在团队
     */
    private void handleTeamPlayerLeave(GamePlayer gamePlayer, GameTeam gameTeam) {
        if (gameTeam.isBedDestroy()) {
            gamePlayer.setGameTeam(null);
        }

        if (gameTeam.getAlivePlayers().isEmpty() && !gameTeam.isBedDestroy()) {
            gameTeam.setBedDestroy(true);
        }
    }

    /**
     * 获取玩家所在的队伍
     *
     * @param gamePlayer 游戏玩家
     * @return 玩家所在队伍，如果不在队伍中则返回null
     */
    public GameParty getPlayerParty(GamePlayer gamePlayer) {
        return gameParties.stream()
                .filter(party -> party.isInTeam(gamePlayer))
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加一个队伍到游戏中
     *
     * @param gameParty 游戏队伍
     */
    public void addParty(GameParty gameParty) {
        gameParties.add(gameParty);
    }

    /**
     * 从游戏中移除一个队伍
     *
     * @param gameParty 游戏队伍
     */
    public void removeParty(GameParty gameParty) {
        gameParties.remove(gameParty);
    }

    /**
     * 获取游戏最大玩家数量
     *
     * @return 最大玩家数量
     */
    public int getMaxPlayers() {
        return mapData.getBases().size() * mapData.getPlayers().getTeam();
    }

    /**
     * 检查是否有足够的玩家开始游戏
     *
     * @return 是否有足够玩家
     */
    boolean hasEnoughPlayers() {
        return GamePlayer.getOnlinePlayers().size() >= mapData.getPlayers().getMin();
    }

    /**
     * 获取人数最少的团队
     *
     * @return 人数最少的团队
     */
    public GameTeam getLowestTeam() {
        return gameTeams.stream()
                .filter(team -> !team.isFull())
                .min(Comparator.comparingInt(team -> team.getGamePlayers().size()))
                .orElse(gameTeams.getFirst());
    }

    /**
     * 将无团队玩家分配到团队中
     */
    public void moveFreePlayersToTeam() {
        // 先处理队伍内的玩家
        assignPartyPlayersToTeams();
        
        // 再处理独立玩家
        assignIndividualPlayersToTeams();
    }

    /**
     * 将队伍玩家分配到游戏团队中
     */
    private void assignPartyPlayersToTeams() {
        for (GameParty gameParty : gameParties) {
            GameTeam lowest = getLowestTeam();
            
            // 尝试将整个队伍放入同一游戏团队
            List<GamePlayer> unassignedPlayers = gameParty.getPlayers().stream()
                    .filter(player -> player.getGameTeam() == null)
                    .toList();
                    
            for (GamePlayer gamePlayer : unassignedPlayers) {
                if (!lowest.addPlayer(gamePlayer)) {
                    lowest = getLowestTeam();
                    lowest.addPlayer(gamePlayer);
                }
            }
        }
    }

    /**
     * 将独立玩家分配到游戏团队中
     */
    private void assignIndividualPlayersToTeams() {
        List<GamePlayer> freePlayers = GamePlayer.getOnlinePlayers().stream()
                .filter(player -> player.getGameTeam() == null)
                .toList();
                
        for (GamePlayer gamePlayer : freePlayers) {
            GameTeam lowest = getLowestTeam();
            lowest.addPlayer(gamePlayer);
        }
    }

    /**
     * 将所有玩家传送到各自团队的出生点
     */
    public void teleportPlayersToTeamSpawn() {
        for (GameTeam gameTeam : this.gameTeams) {
            for (GamePlayer gamePlayer : gameTeam.getAlivePlayers()) {
                Player player = gamePlayer.getPlayer();
                teleportPlayerSafely(player, gameTeam.getSpawn());
            }
        }
    }

    /**
     * 安全地传送玩家到指定位置
     *
     * @param player 玩家
     * @param location 目标位置
     */
    private void teleportPlayerSafely(Player player, Location location) {
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0.0F);
        player.teleport(location);
    }

    /**
     * 根据团队颜色获取团队
     *
     * @param teamColor 团队颜色
     * @return 对应的团队，如果不存在则返回null
     */
    public GameTeam getTeam(TeamColor teamColor) {
        return gameTeams.stream()
                .filter(team -> team.getTeamColor() == teamColor)
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加特殊物品到游戏中
     *
     * @param specialItem 特殊物品
     */
    public void addSpecialItem(SpecialItem specialItem) {
        this.specialItems.add(specialItem);
    }

    /**
     * 从游戏中移除特殊物品
     *
     * @param specialItem 特殊物品
     */
    public void removeSpecialItem(SpecialItem specialItem) {
        this.specialItems.remove(specialItem);
    }

    /**
     * 向所有玩家广播标题
     *
     * @param fadeIn 淡入时间
     * @param stay 停留时间
     * @param fadeOut 淡出时间
     * @param title 主标题
     * @param subTitle 副标题
     */
    public void broadcastTitle(Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> 
            gamePlayer.sendTitle(fadeIn, stay, fadeOut, title, subTitle));
    }

    /**
     * 向特定团队广播标题
     *
     * @param gameTeam 目标团队
     * @param fadeIn 淡入时间
     * @param stay 停留时间
     * @param fadeOut 淡出时间
     * @param title 主标题
     * @param subTitle 副标题
     */
    public void broadcastTeamTitle(GameTeam gameTeam, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> 
            gamePlayer.sendTitle(fadeIn, stay, fadeOut, title, subTitle));
    }

    /**
     * 向特定团队广播消息
     *
     * @param gameTeam 目标团队
     * @param texts 消息文本
     */
    public void broadcastTeamMessage(GameTeam gameTeam, String... texts) {
        gameTeam.getAlivePlayers().forEach(player -> 
            Arrays.asList(texts).forEach(player::sendMessage));
    }

    /**
     * 向特定团队播放声音
     *
     * @param gameTeam 目标团队
     * @param sound 声音类型
     * @param volume 音量
     * @param pitch 音调
     */
    public void broadcastTeamSound(GameTeam gameTeam, Sound sound, float volume, float pitch) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> 
            gamePlayer.playSound(sound, volume, pitch));
    }

    /**
     * 向所有观察者广播消息
     *
     * @param texts 消息文本
     */
    public void broadcastSpectatorMessage(String... texts) {
        GamePlayer.getSpectators().forEach(gamePlayer -> 
            Arrays.asList(texts).forEach(gamePlayer::sendMessage));
    }

    /**
     * 向所有玩家广播消息
     *
     * @param texts 消息文本
     */
    public void broadcastMessage(String... texts) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> 
            Arrays.asList(texts).forEach(gamePlayer::sendMessage));
    }

    /**
     * 向所有玩家播放声音
     *
     * @param sound 声音类型
     * @param volume 音量
     * @param pitch 音调
     */
    public void broadcastSound(Sound sound, float volume, float pitch) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> 
            gamePlayer.playSound(sound, volume, pitch));
    }

    /**
     * 检查游戏是否可以开始
     *
     * @return 是否可以开始
     */
    public boolean isStartable() {
        return (this.hasEnoughPlayers() && this.hasEnoughTeams());
    }

    /**
     * 检查是否有足够的团队开始游戏
     *
     * @return 是否有足够的团队
     */
    public boolean hasEnoughTeams() {
        int teamsWithPlayers = countTeamsWithPlayers();
        List<GamePlayer> freePlayers = getFreePlayers();

        return (teamsWithPlayers > 1 || 
                (teamsWithPlayers == 1 && !freePlayers.isEmpty()) || 
                (teamsWithPlayers == 0 && freePlayers.size() >= 2));
    }

    /**
     * 获取没有加入团队的玩家列表
     *
     * @return 无团队玩家列表
     */
    private List<GamePlayer> getFreePlayers() {
        List<GamePlayer> freePlayers = new ArrayList<>(GamePlayer.getGamePlayers());
        freePlayers.removeAll(GamePlayer.getTeamPlayers());
        return freePlayers;
    }

    /**
     * 计算有玩家的团队数量
     *
     * @return 有玩家的团队数量
     */
    private int countTeamsWithPlayers() {
        return (int) gameTeams.stream()
                .filter(team -> !team.getGamePlayers().isEmpty())
                .count();
    }

    /**
     * 获取格式化的时间字符串
     *
     * @param seconds 总秒数
     * @return 格式化的时间字符串 (mm:ss)
     */
    public String getFormattedTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        
        String minutesStr = (minutes < 10 ? "0" : "") + minutes;
        String secondsStr = (remainingSeconds < 10 ? "0" : "") + remainingSeconds;
        
        return minutesStr + ":" + secondsStr;
    }

    /**
     * 开始游戏
     */
    public void start() {
        gameState = GameState.RUNNING;

        moveFreePlayersToTeam();
        eventManager.start();

        preparePlayersForGame();
        teleportPlayersToTeamSpawn();
        markEmptyTeams();

        GamePlayer.getOnlinePlayers().forEach(GamePlayer::giveInventory);
        Bukkit.getPluginManager().callEvent(new BedwarsGameStartEvent());
    }

    /**
     * 准备玩家开始游戏
     */
    private void preparePlayersForGame() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Player player = gamePlayer.getPlayer();

            if (gamePlayer.getGameTeam() == null) {
                player.kickPlayer("");
                continue;
            }

            player.setAllowFlight(false);
            player.setFlying(false);
            player.setGameMode(GameMode.SURVIVAL);
            gamePlayer.cleanState();
            gamePlayer.getPlayerData().addGames();
        }
    }

    /**
     * 标记没有玩家的队伍为床已被摧毁
     */
    private void markEmptyTeams() {
        getGameTeams().forEach(team -> {
            if (team.getGamePlayers().isEmpty()) {
                team.setBedDestroy(true);
            }
        });
    }

    /**
     * 检查游戏是否结束
     *
     * @return 游戏是否结束
     */
    public boolean isOver() {
        long aliveTeams = gameTeams.stream()
                .filter(team -> !team.isDead())
                .count();
        return aliveTeams <= 1;
    }

    /**
     * 获取获胜的团队
     *
     * @return 获胜团队，如果没有则返回null
     */
    public GameTeam getWinner() {
        return gameTeams.stream()
                .filter(team -> !team.isDead())
                .findFirst()
                .orElse(null);
    }

    /**
     * 寻找最近的敌方玩家
     *
     * @param gamePlayer 当前玩家
     * @return 最近的敌方玩家，如果没有则返回null
     */
    public GamePlayer findTargetPlayer(GamePlayer gamePlayer) {
        List<GamePlayer> possibleTargets = getPossibleTargets(gamePlayer);
        
        GamePlayer closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (GamePlayer target : possibleTargets) {
            if (gamePlayer.getPlayer().getWorld() != target.getPlayer().getWorld()) {
                continue;
            }

            double distance = gamePlayer.getPlayer().getLocation().distance(target.getPlayer().getLocation());
            if (distance < closestDistance) {
                closestPlayer = target;
                closestDistance = distance;
            }
        }

        return closestPlayer;
    }

    /**
     * 获取可能的目标玩家列表
     *
     * @param gamePlayer 当前玩家
     * @return 可能的目标玩家列表
     */
    private List<GamePlayer> getPossibleTargets(GamePlayer gamePlayer) {
        List<GamePlayer> targets = new ArrayList<>(GamePlayer.getOnlinePlayers());
        
        // 移除同队玩家
        if (gamePlayer.getGameTeam() != null) {
            targets.removeAll(gamePlayer.getGameTeam().getGamePlayers());
        }
        
        // 移除观察者
        targets.removeIf(GamePlayer::isSpectator);
        
        return targets;
    }
}
