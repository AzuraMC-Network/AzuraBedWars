package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.game.BedwarsGameLoadEvent;
import cc.azuramc.bedwars.api.event.game.BedwarsGameStartEvent;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.compat.util.WoolUtil;
import cc.azuramc.bedwars.config.object.ItemConfig;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.event.GameEventManager;
import cc.azuramc.bedwars.game.item.special.AbstractSpecialItem;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.spectator.SpectatorManager;
import cc.azuramc.bedwars.game.task.GameStartTask;
import cc.azuramc.bedwars.game.task.generator.GeneratorManager;
import cc.azuramc.bedwars.jedis.JedisManager;
import cc.azuramc.bedwars.jedis.event.JedisGameLoadingEvent;
import cc.azuramc.bedwars.jedis.event.JedisGameStartEvent;
import cc.azuramc.bedwars.listener.player.PlayerAFKListener;
import cc.azuramc.bedwars.shop.ShopManager;
import cc.azuramc.bedwars.tablist.TabListManager;
import cc.azuramc.bedwars.upgrade.task.TeamUpgradeCheckTask;
import cc.azuramc.bedwars.util.LoadGameUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.ServerMOTD;
import com.cryptomorin.xseries.XMaterial;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.block.Block;
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
 *
 * @author an5w1r@163.com
 */
@Data
public class GameManager {

    private static final MessageConfig messageConfig = AzuraBedWars.getInstance().getMessageConfig();
    private ItemConfig.GameManager itemConfig;
    private SettingsConfig settingsConfig;

    private static final long COUNTDOWN_TICK_PERIOD = 20L;
    private static final int ASSIST_TIME_WINDOW_MS = 10000;

    private String msgPlayerReconnect;
    private String msgPlayerLeave;

    private Material resourceSelectorMaterial;
    private String resourceSelectorName;
    private Material teamSelectorMaterial;
    private String teamSelectorName;
    private Material leaveGameMaterial;
    private String leaveGameName;

    private boolean arrowDisplayEnabled;
    private boolean attackDisplayEnabled;

    private AzuraBedWars plugin;
    private GeneratorManager generatorManager;
    private GameEventManager gameEventManager;

    private MapData mapData;
    private double buildLimitHeight;

    private GameState gameState;
    private boolean isForceStarted;

    private Location waitingLocation;
    private Location respawnLocation;

    private List<Location> blocksLocation;
    private GameStartTask gameStartTask = null;
    private List<GameTeam> gameTeams;
    private List<GameParty> gameParties;

    private Map<ArmorStand, String> armorSande;
    private Map<ArmorStand, String> armorStand;

    private List<AbstractSpecialItem> abstractSpecialItems;

    private int teamBlockSearchRadius;

    private TabListManager tabListManager;

    private List<GamePlayer> allInGamePlayers;

    /**
     * 创建一个新的游戏实例
     *
     * @param plugin 插件主类实例
     */
    public GameManager(AzuraBedWars plugin) {
        this.plugin = plugin;
        this.isForceStarted = false;
        this.gameTeams = new ArrayList<>();
        this.gameParties = new ArrayList<>();
        this.armorSande = new HashMap<>();
        this.armorStand = new HashMap<>();
        this.abstractSpecialItems = new ArrayList<>();
        ShopManager.init(this);
        this.generatorManager = new GeneratorManager(this);
        this.gameEventManager = new GameEventManager(this);
        initializeConfigs();

        this.tabListManager = new TabListManager(this);
        tabListManager.startAutoUpdate(plugin);
        this.allInGamePlayers = new ArrayList<>();
    }

    private void initializeConfigs() {
        this.itemConfig = plugin.getItemConfig().getGameManager();
        this.settingsConfig = plugin.getSettingsConfig();

        this.msgPlayerReconnect = messageConfig.getPlayerReconnectMessage();
        this.msgPlayerLeave = messageConfig.getPlayerLeaveMessage();

        this.resourceSelectorMaterial = XMaterial.PAPER.get();
        this.resourceSelectorName = itemConfig.getResourceSelectorName();
        this.teamSelectorMaterial = XMaterial.WHITE_WOOL.get();
        this.teamSelectorName = itemConfig.getTeamSelectorName();
        this.leaveGameMaterial = XMaterial.SLIME_BALL.get();
        this.leaveGameName = itemConfig.getLeaveGameName();

        this.arrowDisplayEnabled = settingsConfig.getDisplayDamage().isArrowDisplayEnabled();
        this.attackDisplayEnabled = settingsConfig.getDisplayDamage().isAttackDisplayEnabled();
        this.teamBlockSearchRadius = settingsConfig.getTeamBlockSearchRadius();
    }

    /**
     * 更新服务器MOTD
     */
    private void updateServerMOTD() {
        ServerMOTD.updateMOTD(this.gameState);
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

        // call event
        BedwarsGameLoadEvent event = new BedwarsGameLoadEvent(this, mapData);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        this.mapData = mapData;
        this.buildLimitHeight = mapData.getHigherY();
        this.blocksLocation = mapData.loadMap();
        this.respawnLocation = mapData.getRespawnLocation().toLocation();
        this.waitingLocation = mapData.getWaitingLocation().toLocation();

        LoadGameUtil.spawnAll(plugin);

        // 在初始化团队前检查必要的队伍配置
        if (mapData.getPlayers().getTeam() == null) {
            plugin.getLogger().warning("地图 " + mapData.getName() + " 的每个队伍玩家数未设置，使用默认值1");
            mapData.getPlayers().setTeam(1);
        }

        initializeTeams(mapData);
        this.gameState = GameState.WAITING;
        // 更新MOTD
        updateServerMOTD();

        // 添加JedisManager的空值检查
        if (JedisManager.getInstance() != null) {
            JedisManager.getInstance().getExpand().put("map", mapData.getName());
            Bukkit.getPluginManager().callEvent(new JedisGameLoadingEvent(getMaxPlayers()));
        } else {
            plugin.getLogger().warning("JedisManager实例为null，跳过Jedis相关操作");
        }
    }

    /**
     * 初始化游戏团队
     *
     * @param mapData 地图数据
     */
    private void initializeTeams(MapData mapData) {
        // 旧的方法是按顺序分配颜色
        // for (int i = 0; i < mapData.getBases().size(); i++) {
        //     gameTeams.add(new GameTeam(
        //         TeamColor.values()[i],
        //         mapData.getBases().get(i).toLocation(),
        //         mapData.getPlayers().getTeam()
        //     ));
        // }

        // 新 检测基地附近的羊毛颜色来确定队伍颜色
        for (int i = 0; i < mapData.getBases().size(); i++) {
            Location baseLocation = mapData.getBases().get(i).toLocation();
            Location resourceDropLocation = getClosestBaseLocationDropLocation(baseLocation);
            LoggerUtil.debug("GameManager$initializeTeams | baseLocation is " + baseLocation +
                    ", resourceDropLocation is " + resourceDropLocation);
            TeamColor teamColor = detectTeamColorFromWool(baseLocation);
            LoggerUtil.debug("GameManager$initializeTeams | detectTeamColorFromWool is " + teamColor);

            // 如果无法检测到羊毛颜色，则使用默认顺序
            if (teamColor == null) {
                teamColor = TeamColor.values()[i % TeamColor.values().length];
                LoggerUtil.debug("GameManager$initializeTeams | team color is null so we used the order " + teamColor);
            }

            // 获取每个队伍的玩家数量，添加空值检查防止NPE
            Integer teamSize = mapData.getPlayers().getTeam();
            if (teamSize == null) {
                // 设置默认值为1
                teamSize = 1;
            }

            gameTeams.add(new GameTeam(
                    this,
                    teamColor,
                    baseLocation,
                    resourceDropLocation,
                    teamSize
            ));
        }
    }

    /**
     * 根据BaseLocation获得最近的BaseType DropLocation
     *
     * @param baseLocation base location
     * @return 最近的BaseType DropLocation
     */
    private Location getClosestBaseLocationDropLocation(Location baseLocation) {
        List<Location> baseDropLocations = mapData.getDropLocations(MapData.DropType.BASE);

        Location resourceDropLocation = null;
        double minDistanceSquared = Double.MAX_VALUE;

        for (Location dropLocation : baseDropLocations) {
            double distanceSquared = baseLocation.distanceSquared(dropLocation);
            if (distanceSquared < minDistanceSquared) {
                minDistanceSquared = distanceSquared;
                resourceDropLocation = dropLocation;
            }
        }

        if (resourceDropLocation == null) {
            LoggerUtil.debug("No BASE drop location found for base at " + baseLocation);
        }

        return resourceDropLocation;
    }

    /**
     * 从基地位置附近检测羊毛颜色来确定队伍颜色
     *
     * @param location 基地位置
     * @return 检测到的队伍颜色，如果没有检测到则返回null
     */
    private TeamColor detectTeamColorFromWool(Location location) {
        // 搜索范围，可以根据实际情况调整
        int radius = teamBlockSearchRadius;
        World world = location.getWorld();
        LoggerUtil.debug("GameManager$detectTeamColorFromWool | world is " + world);
        LoggerUtil.debug("GameManager$detectTeamColorFromWool | location is " + location);
        LoggerUtil.debug("GameManager$detectTeamColorFromWool | radius is " + radius);

        // 检查基础条件
        if (world == null) {
            LoggerUtil.debug("GameManager$detectTeamColorFromWool | world is null, returning null");
            return null;
        }

        if (radius <= 0) {
            LoggerUtil.debug("GameManager$detectTeamColorFromWool | radius is <= 0, returning null");
            return null;
        }

        LoggerUtil.debug("GameManager$detectTeamColorFromWool | starting search in radius " + radius);

        // 遍历位置周围的方块
        int checkedBlocks = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    checkedBlocks++;
                    Block block = world.getBlockAt(
                            location.getBlockX() + x,
                            location.getBlockY() + y,
                            location.getBlockZ() + z
                    );

                    // 检查方块是否为羊毛
                    if (block.getType().name().contains("WOOL")) {
                        // 根据羊毛颜色确定队伍颜色
                        LoggerUtil.debug("GameManager$detectTeamColorFromWool | Found WOOL block! Type: " + block.getType().name());
                        return WoolUtil.getTeamColorFromWoolBlock(block);
                    }
                }
            }
        }

        LoggerUtil.debug("GameManager$detectTeamColorFromWool | Checked " + checkedBlocks + " blocks, no wool found");
        return null;
    }

    /**
     * 玩家加入游戏处理
     *
     * @param gamePlayer 游戏玩家
     */
    public void addPlayer(GamePlayer gamePlayer) {

        if (gameState == GameState.RUNNING) {
            handlePlayerJoinRunningGame(gamePlayer);
            tabListManager.addToTab(gamePlayer);
            return;
        }

        handlePlayerJoinWaitingGame(gamePlayer);

        tabListManager.addToTab(gamePlayer);
        allInGamePlayers.add(gamePlayer);
    }

    /**
     * 处理玩家加入等待状态的游戏
     *
     * @param gamePlayer 游戏玩家
     */
    private void handlePlayerJoinWaitingGame(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        // 处理玩家可见性
        showAllPlayers(gamePlayer);

        // 设置玩家基本状态
        player.spigot().respawn();
        player.setGameMode(GameMode.ADVENTURE);
        player.getEnderChest().clear();
        gamePlayer.cleanState();

        // 传送到等待区域
        player.teleport(waitingLocation);

        // 设置记分板
        plugin.getScoreboardManager().showBoard(gamePlayer);
        plugin.getScoreboardManager().updateAllBoards();

        // 给予物品
        giveWaitingItems(gamePlayer);

        // 检查是否可以开始游戏
        checkGameStart();
    }

    /**
     * 更新玩家可见性设置
     *
     * @param gamePlayer 游戏玩家
     */
    private void showAllPlayers(GamePlayer gamePlayer) {
        for (GamePlayer onlinePlayer : GamePlayer.getOnlinePlayers()) {
            PlayerUtil.showPlayer(onlinePlayer, GamePlayer.getOnlinePlayers());
        }
    }

    /**
     * 给予玩家等待时的物品
     *
     * @param gamePlayer 玩家
     */
    private void giveWaitingItems(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        // Game Mode Selection
        player.getInventory().addItem(
                new ItemBuilder()
                        .setType(resourceSelectorMaterial)
                        .setDisplayName(resourceSelectorName)
                        .getItem()
        );

        // Team Selection
//        player.getInventory().setItem(2,
//                new ItemBuilder()
//                        .setType(teamSelectorMaterial)
//                        .setDisplayName(teamSelectorName)
//                        .getItem()
//        );

        // Leave Game
        player.getInventory().setItem(8,
                new ItemBuilder()
                        .setType(leaveGameMaterial)
                        .setDisplayName(leaveGameName)
                        .getItem()
        );
    }

    /**
     * 处理玩家加入正在运行的游戏
     *
     * @param gamePlayer 游戏玩家
     */
    private void handlePlayerJoinRunningGame(GamePlayer gamePlayer) {
        // 设置记分板
        plugin.getScoreboardManager().showBoard(gamePlayer);
        plugin.getScoreboardManager().updateAllBoards();

        // 检查玩家团队状态
        if (gamePlayer.getGameTeam() != null) {
            if (gamePlayer.getGameTeam().isHasBed()) {
                gamePlayer.setReconnect(true);
                PlayerUtil.callPlayerRespawnEvent(gamePlayer.getPlayer(), respawnLocation);
                broadcastMessage(String.format(msgPlayerReconnect, gamePlayer.getNickName()));
                return;
            }
        }

        // 如果没有有效团队，设为观察者
        SpectatorManager.toSpectator(gamePlayer);
    }

    /**
     * 检查是否可以开始游戏倒计时
     */
    private void checkGameStart() {
        if (canStart() && gameState == GameState.WAITING && getGameStartTask() == null) {
            GameStartTask lobbyCountdown = new GameStartTask(this);
            lobbyCountdown.runTaskTimer(plugin, COUNTDOWN_TICK_PERIOD, COUNTDOWN_TICK_PERIOD);
            setGameStartTask(lobbyCountdown);
        }
    }

    /**
     * 处理玩家离开游戏
     *
     * @param gamePlayer 游戏玩家
     */
    public void removePlayers(GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            return;
        }

        if (gameState == GameState.WAITING) {
            broadcastMessage(String.format(msgPlayerLeave, gamePlayer.getNickName()));
        }

        if (gameState == GameState.RUNNING && gamePlayer.isSpectator()) {
            return;
        }

        AzuraBedWars.getInstance().getPlayerDataService().updatePlayerData(gamePlayer);

        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }

        // 处理玩家离开对团队的影响
        handleTeamPlayerLeave(gamePlayer, gameTeam);
        allInGamePlayers.remove(gamePlayer);
    }

    /**
     * 处理玩家离开对团队的影响
     *
     * @param gamePlayer 游戏玩家
     * @param gameTeam   玩家所在团队
     */
    private void handleTeamPlayerLeave(GamePlayer gamePlayer, GameTeam gameTeam) {
        if (gameTeam.isDestroyed()) {
            gamePlayer.setGameTeam(null);
        }

        if (gameTeam.getAlivePlayers().isEmpty() && !gameTeam.isDestroyed()) {
            gameTeam.setDestroyed(true);
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
        return this.mapData.getMaxPlayers();
    }

    /**
     * 检查是否有足够的玩家开始游戏
     *
     * @return 是否有足够玩家
     */
    public boolean hasEnoughPlayers() {
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
                .orElse(gameTeams.get(0));
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
                teleportPlayerSafely(player, gameTeam.getSpawnLocation());
            }
        }
    }

    /**
     * 安全地传送玩家到指定位置
     *
     * @param player   玩家
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
     * @param abstractSpecialItem 特殊物品
     */
    public void addSpecialItem(AbstractSpecialItem abstractSpecialItem) {
        this.abstractSpecialItems.add(abstractSpecialItem);
    }

    /**
     * 从游戏中移除特殊物品
     *
     * @param abstractSpecialItem 特殊物品
     */
    public void removeSpecialItem(AbstractSpecialItem abstractSpecialItem) {
        this.abstractSpecialItems.remove(abstractSpecialItem);
    }

    /**
     * 向所有玩家广播标题
     *
     * @param title    主标题
     * @param subTitle 副标题
     * @param fadeIn   淡入时间
     * @param stay     停留时间
     * @param fadeOut  淡出时间
     */
    public void broadcastTitleToAll(String title, String subTitle, Integer fadeIn, Integer stay, Integer fadeOut) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer ->
                gamePlayer.sendTitle(title, subTitle, fadeIn, stay, fadeOut));
    }

    /**
     * 向特定团队广播标题
     *
     * @param gameTeam 目标团队
     * @param title    主标题
     * @param subTitle 副标题
     * @param fadeIn   淡入时间
     * @param stay     停留时间
     * @param fadeOut  淡出时间
     */
    public void broadcastTeamTitle(GameTeam gameTeam, String title, String subTitle, Integer fadeIn, Integer stay, Integer fadeOut) {
        gameTeam.getAlivePlayers().forEach(gamePlayer ->
                gamePlayer.sendTitle(title, subTitle, fadeIn, stay, fadeOut));
    }

    /**
     * 向特定团队广播消息
     *
     * @param gameTeam 目标团队
     * @param texts    消息文本
     */
    public void broadcastTeamMessage(GameTeam gameTeam, String... texts) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> Arrays.stream(texts).forEach(gamePlayer::sendMessage));
        LoggerUtil.printChat(texts);
    }

    /**
     * 向特定团队广播消息
     *
     * @param gameTeam 目标团队
     * @param textList 消息文本
     */
    public void broadcastTeamMessage(GameTeam gameTeam, List<String> textList) {
        gameTeam.getAlivePlayers().forEach(player -> textList.forEach(player::sendMessage));
        LoggerUtil.printChat(textList);
    }

    /**
     * 向特定团队播放声音
     *
     * @param gameTeam 目标团队
     * @param sound    声音类型
     * @param volume   音量
     * @param pitch    音调
     */
    public void broadcastTeamSound(GameTeam gameTeam, Sound sound, float volume, float pitch) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> gamePlayer.playSound(sound, volume, pitch));
    }

    /**
     * 向所有观察者广播消息
     *
     * @param texts 消息文本
     */
    public void broadcastSpectatorMessage(String... texts) {
        GamePlayer.getSpectators().forEach(gamePlayer -> Arrays.stream(texts).forEach(gamePlayer::sendMessage));
        LoggerUtil.printChat(texts);
    }

    /**
     * 向所有观察者广播消息
     *
     * @param textList 消息文本
     */
    public void broadcastSpectatorMessage(List<String> textList) {
        GamePlayer.getSpectators().forEach(gamePlayer -> textList.forEach(gamePlayer::sendMessage));
        LoggerUtil.printChat(textList);
    }

    /**
     * 向所有玩家广播消息
     *
     * @param texts 消息文本
     */
    public void broadcastMessage(String... texts) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> Arrays.stream(texts).forEach(gamePlayer::sendMessage));
        LoggerUtil.printChat(texts);
    }

    /**
     * 向所有玩家广播消息
     *
     * @param textList 消息文本
     */
    public void broadcastMessage(List<String> textList) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer ->
                textList.forEach(gamePlayer::sendMessage)
        );
        LoggerUtil.printChat(textList);
    }

    /**
     * 向所有玩家播放声音
     *
     * @param sound  声音类型
     * @param volume 音量
     * @param pitch  音调
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
    public boolean canStart() {
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

        BedwarsGameStartEvent bedwarsGameStartEvent = new BedwarsGameStartEvent(this);
        Bukkit.getPluginManager().callEvent(bedwarsGameStartEvent);
        if (bedwarsGameStartEvent.isCancelled()) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new JedisGameStartEvent());

        gameState = GameState.RUNNING;
        // 更新MOTD
        updateServerMOTD();

        moveFreePlayersToTeam();
        gameEventManager.start();

        preparePlayersForGame();
        teleportPlayersToTeamSpawn();
        markEmptyTeams();

        // 更新所有玩家的TabList显示名称
        tabListManager.updateAllTabListNames();

        GamePlayer.getOnlinePlayers().forEach(GamePlayer::giveInventory);

        // 开始挂机状态检测
        PlayerAFKListener.startCheckAFKTask();

        // 注册团队升级任务
        registerTeamUpgradeCheckTask();
    }

    /**
     * 注册团队升级任务，处理团队效果和陷阱
     */
    private void registerTeamUpgradeCheckTask() {
        TeamUpgradeCheckTask teamUpgradeCheckTask = new TeamUpgradeCheckTask(this);
        gameEventManager.registerRunnable("团队升级", (s, c) -> teamUpgradeCheckTask.execute());
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

            // 重置本局游戏数据
            gamePlayer.setCurrentGameKills(0);
            gamePlayer.setCurrentGameFinalKills(0);
            gamePlayer.setCurrentGameAssists(0);
            gamePlayer.setCurrentGameDeaths(0);
            gamePlayer.setCurrentGameDestroyedBeds(0);
        }
    }

    /**
     * 标记没有玩家的队伍为床已被摧毁
     */
    private void markEmptyTeams() {
        getGameTeams().forEach(team -> {
            if (team.getGamePlayers().isEmpty()) {
                team.setDestroyed(true);
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
        List<GameTeam> aliveTeams = gameTeams.stream()
                .filter(team -> !team.isDead())
                .toList();

        if (aliveTeams.size() == 1) {
            return aliveTeams.get(0);
        }

        return null;
    }

    public List<GameTeam> getAliveTeams() {
        return gameTeams.stream()
                .filter(team -> !team.isDead())
                .toList();
    }

    public boolean isTie() {
        List<GameTeam> aliveTeams = getAliveTeams();
        return aliveTeams.size() > 1;
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
