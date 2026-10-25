package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.game.BedwarsGameLoadEvent;
import cc.azuramc.bedwars.api.event.game.BedwarsGameStartEvent;
import cc.azuramc.bedwars.api.event.player.BedwarsPlayerKillEvent;
import cc.azuramc.bedwars.api.event.player.BedwarsPlayerReconnectEvent;
import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.IGameTeam;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.config.object.GameplayConfig;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.service.PlayerDataService;
import cc.azuramc.bedwars.game.economy.EconomyMode;
import cc.azuramc.bedwars.game.economy.EconomyType;
import cc.azuramc.bedwars.game.item.special.AbstractSpecialItem;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.spectator.SpectatorManager;
import cc.azuramc.bedwars.game.task.GameStartTask;
import cc.azuramc.bedwars.game.task.RespawnTask;
import cc.azuramc.bedwars.game.task.generator.GeneratorManager;
import cc.azuramc.bedwars.game.timeline.GameTimeline;
import cc.azuramc.bedwars.listener.player.PlayerAFKListener;
import cc.azuramc.bedwars.shop.ShopCatalog;
import cc.azuramc.bedwars.tablist.TabListListener;
import cc.azuramc.bedwars.tablist.TabListManager;
import cc.azuramc.bedwars.util.DamageUtil;
import cc.azuramc.bedwars.util.LoadGameUtil;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.ServerMOTD;
import cc.azuramc.bedwars.util.hook.VaultUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author an5w1r@163.com
 */
@Data
public class GameManager implements IGameManager {

    private static final long COUNTDOWN_TICK_PERIOD = 20L;
    private static final int RECONNECT_RESPAWN_SECONDS = 10;

    private Material teamSelectorMaterial;
    private Material leaveGameMaterial;

    private AzuraBedWars plugin;
    private EconomyType economyType;
    private GeneratorManager generatorManager;
    private GameTimeline timeline;

    private MapData mapData;
    private double buildLimitHeight;

    private GameState gameState;

    private Location waitingLocation;
    private Location respawnLocation;

    private Set<Location> blocksLocation;
    private GameStartTask gameStartTask = null;
    private List<GameTeam> gameTeams;

    private Set<ArmorStand> armorSande;
    private Set<ArmorStand> armorStand;

    private List<AbstractSpecialItem> abstractSpecialItems;

    private TabListManager tabListManager;

    private final Map<UUID, ReconnectState> reconnectStates = new ConcurrentHashMap<>();
    private List<GameTeam> aliveTeamsAtEnd;

    public GameManager(AzuraBedWars plugin) {
        this.plugin = plugin;
        this.gameTeams = new ArrayList<>();
        this.armorSande = new HashSet<>();
        this.armorStand = new HashSet<>();
        this.abstractSpecialItems = new ArrayList<>();
        this.economyType = plugin.getSettingsConfig().getEconomyMode();
        ShopCatalog.init(economyType);
        this.generatorManager = new GeneratorManager(this);
        this.timeline = new GameTimeline(this);
        this.teamSelectorMaterial = XMaterial.WHITE_WOOL.get();
        this.leaveGameMaterial = XMaterial.SLIME_BALL.get();

        this.tabListManager = new TabListManager(this);
        Bukkit.getPluginManager().registerEvents(new TabListListener(tabListManager), plugin.getPlugin());
    }

    public EconomyMode getEconomy() {
        return economyType.getMode();
    }

    public boolean isArrowDisplayEnabled() {
        return plugin.getDisplayConfig().getDisplayDamage().isArrowDisplayEnabled();
    }

    public boolean isAttackDisplayEnabled() {
        return plugin.getDisplayConfig().getDisplayDamage().isAttackDisplayEnabled();
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
     * @param mapData a map that passed MapCheck, with its world loaded
     */
    public void loadGame(MapData mapData) {
        // 触发游戏加载前置事件（可取消以阻止加载）
        BedwarsGameLoadEvent.Pre preEvent = new BedwarsGameLoadEvent.Pre(this, mapData);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }

        // 新一局加载时清空上一局遗留的重连快照，避免累积与跨局错误恢复
        reconnectStates.clear();

        this.mapData = mapData;
        this.buildLimitHeight = mapData.getHigherY();
        this.blocksLocation = mapData.scanMapBlocks();
        this.respawnLocation = mapData.getRespawnLocation().toLocation();
        this.waitingLocation = mapData.getWaitingLocation().toLocation();

        LoadGameUtil.spawnAll(plugin);

        List<TeamColor> colors = mapData.getTeamColors(plugin.getGameplayConfig().getTeamBlockSearchRadius());
        for (int i = 0; i < mapData.getBases().size(); i++) {
            Location base = mapData.getBases().get(i).toLocation();
            gameTeams.add(new GameTeam(this, colors.get(i), base, mapData.getClosestDrop(base, MapData.DropType.BASE), mapData.getPlayers().getTeam()));
        }

        this.gameState = GameState.WAITING;
        // 更新MOTD
        updateServerMOTD();

        // 触发游戏加载完成后置事件
        Bukkit.getPluginManager().callEvent(new BedwarsGameLoadEvent.Post(this, mapData));
    }

    public void addPlayer(GamePlayer gamePlayer) {
        plugin.getScoreboardManager().showBoard(gamePlayer);
        plugin.getScoreboardManager().updateAllBoards();

        if (gameState == GameState.WAITING) {
            joinWaitingGame(gamePlayer);
        } else {
            joinRunningGame(gamePlayer);
        }
        tabListManager.addToTab(gamePlayer);
    }

    private void joinWaitingGame(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        PlayerUtil.showPlayer(gamePlayer, GamePlayer.getOnlinePlayers());
        PlayerUtil.showPlayer(GamePlayer.getOnlinePlayers(), gamePlayer);

        player.spigot().respawn();
        player.setGameMode(GameMode.ADVENTURE);
        player.getEnderChest().clear();
        gamePlayer.cleanState();
        player.teleport(waitingLocation);

        if (plugin.getSettingsConfig().isEnableTeamSelection()) {
            player.getInventory().setItem(2, new ItemBuilder().setType(teamSelectorMaterial).setDisplayName(plugin.getItemConfig().getGameManager().getTeamSelectorName()).getItem());
        }
        player.getInventory().setItem(8, new ItemBuilder().setType(leaveGameMaterial).setDisplayName(plugin.getItemConfig().getGameManager().getLeaveGameName()).getItem());

        if (gameStartTask == null && canStart()) {
            gameStartTask = new GameStartTask(this);
            gameStartTask.runTaskTimer(plugin.getPlugin(), COUNTDOWN_TICK_PERIOD, COUNTDOWN_TICK_PERIOD);
        }
    }

    private void joinRunningGame(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameState != GameState.RUNNING || gameTeam == null || gameTeam.isDestroyed()) {
            SpectatorManager.toSpectator(gamePlayer);
            return;
        }

        BedwarsPlayerReconnectEvent.Pre preEvent = new BedwarsPlayerReconnectEvent.Pre(gamePlayer, gameTeam, this);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            gamePlayer.getPlayer().kickPlayer("canceled reconnect by event");
            return;
        }

        gamePlayer.setReconnect(true);
        gamePlayer.cleanState();
        new RespawnTask(this, gamePlayer, RECONNECT_RESPAWN_SECONDS).start();
        broadcastMessage(String.format(plugin.getMessageConfig().getPlayerReconnectMessage(), gamePlayer.getNickName()));
        Bukkit.getPluginManager().callEvent(new BedwarsPlayerReconnectEvent.Post(gamePlayer, gameTeam, this));
    }

    /**
     * Must run while the player is still online, since quitting counts as a final death once the bed is gone
     */
    public void removePlayer(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameState == GameState.WAITING) {
            broadcastMessage(String.format(plugin.getMessageConfig().getPlayerLeaveMessage(), gamePlayer.getNickName()));
        } else if (gameState == GameState.RUNNING && gameTeam != null) {
            if (gameTeam.isDestroyed() && !gamePlayer.isSpectator()) {
                handlePlayerDeath(gamePlayer, null);
            }
            reconnectStates.put(gamePlayer.getUuid(), new ReconnectState(gamePlayer));
        }
        plugin.getDatabaseProviderFactory().getPlayerDataService().updatePlayerData(gamePlayer);
    }

    public boolean hasReconnectState(UUID uuid) {
        return reconnectStates.containsKey(uuid);
    }

    @Nullable
    public ReconnectState takeReconnectState(UUID uuid) {
        return reconnectStates.remove(uuid);
    }

    /**
     * @param cause null when the player quit
     */
    public void handlePlayerDeath(GamePlayer victim, @Nullable DamageCause cause) {
        GameTeam team = victim.getGameTeam();
        boolean finalKill = team.isDestroyed();
        GamePlayer killer = DamageUtil.findKiller(victim);
        List<GamePlayer> assists = victim.getAssistsManager().getAssists(System.currentTimeMillis());
        victim.getAssistsManager().clear();

        victim.recordDeath(finalKill);
        if (killer != null) {
            if (!killer.isSpectator() && !killer.isRespawning()) {
                getEconomy().rewardKill(victim, killer);
            }
            killer.recordKill(finalKill);
            if (finalKill && !VaultUtil.ecoIsNull) {
                rewardFinalKillCoins(killer);
            }
            for (GamePlayer assist : assists) {
                GamePlayer online = GamePlayer.get(assist.getUuid());
                if (online != null && !online.equals(killer)) {
                    online.recordAssist();
                }
            }
            Bukkit.getPluginManager().callEvent(new BedwarsPlayerKillEvent(victim, killer, finalKill));
        }

        DeathMessages.broadcast(this, victim, killer, finalKill, cause);
        if (victim.isInvisible()) {
            victim.endInvisibility();
        }
        if (finalKill && team.getAlivePlayers().size() == 1) {
            eliminateTeam(team);
        }
    }

    private void rewardFinalKillCoins(GamePlayer killer) {
        GameplayConfig.PlayerDeath config = plugin.getGameplayConfig().getPlayerDeath();
        VaultUtil.depositPlayer(killer, config.getCoinsReward());
        killer.sendMessage(plugin.getMessageConfig().getFinalKillRewardsMessage());
        new BukkitRunnable() {
            int shown = 0;

            @Override
            public void run() {
                if (shown++ == config.getCoinsActionBarTimes()) {
                    cancel();
                    return;
                }
                killer.sendActionBar(plugin.getMessageConfig().getNormalKillRewardsMessage());
            }
        }.runTaskTimer(plugin.getPlugin(), 0, config.getActionBarPeriod());
    }

    private void eliminateTeam(GameTeam gameTeam) {
        GamePlayer destroyer = gameTeam.getDestroyPlayer();
        broadcastSound(XSound.ENTITY_ENDER_DRAGON_HURT.get(), 10, 10);
        broadcastMessage(plugin.getMessageConfig().getTeamEliminatedFormat(), " ",
                String.format(plugin.getMessageConfig().getTeamEliminatedMessage(), gameTeam.getChatColor() + gameTeam.getName(),
                        destroyer != null ? destroyer.getNickName() : "null"),
                " ", plugin.getMessageConfig().getTeamEliminatedFormat());
    }

    public int getMaxPlayers() {
        return mapData.getMaxPlayers();
    }

    public boolean hasEnoughPlayers() {
        return GamePlayer.getOnlinePlayers().size() >= mapData.getPlayers().getMin();
    }

    public boolean canStart() {
        if (!hasEnoughPlayers()) {
            return false;
        }
        long teamsWithPlayers = gameTeams.stream().filter(team -> !team.getGamePlayers().isEmpty()).count();
        long freePlayers = GamePlayer.getOnlinePlayers().stream().filter(player -> player.getGameTeam() == null).count();
        return teamsWithPlayers + freePlayers >= 2;
    }

    public GameTeam getLowestTeam() {
        return gameTeams.stream()
                .filter(team -> !team.isFull())
                .min(Comparator.comparingInt(team -> team.getGamePlayers().size()))
                .orElse(gameTeams.get(0));
    }

    @Nullable
    public GameTeam getTeam(TeamColor teamColor) {
        return gameTeams.stream()
                .filter(team -> team.getTeamColor() == teamColor)
                .findFirst()
                .orElse(null);
    }

    public void addSpecialItem(AbstractSpecialItem abstractSpecialItem) {
        abstractSpecialItems.add(abstractSpecialItem);
    }

    public void removeSpecialItem(AbstractSpecialItem abstractSpecialItem) {
        abstractSpecialItems.remove(abstractSpecialItem);
    }

    public void broadcastTitleToAll(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> gamePlayer.sendTitle(title, subTitle, fadeIn, stay, fadeOut));
    }

    public void broadcastTeamTitle(IGameTeam gameTeam, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> gamePlayer.sendTitle(title, subTitle, fadeIn, stay, fadeOut));
    }

    public void broadcastTeamMessage(IGameTeam gameTeam, String... texts) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> Arrays.stream(texts).forEach(gamePlayer::sendMessage));
        LoggerUtil.printChat(texts);
    }

    public void broadcastTeamMessage(IGameTeam gameTeam, List<String> textList) {
        gameTeam.getAlivePlayers().forEach(player -> textList.forEach(player::sendMessage));
        LoggerUtil.printChat(textList);
    }

    public void broadcastTeamSound(IGameTeam gameTeam, Sound sound, float volume, float pitch) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> gamePlayer.playSound(sound, volume, pitch));
    }

    public void broadcastSpectatorMessage(String... texts) {
        GamePlayer.getSpectators().forEach(gamePlayer -> Arrays.stream(texts).forEach(gamePlayer::sendMessage));
        LoggerUtil.printChat(texts);
    }

    public void broadcastMessage(String... texts) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> Arrays.stream(texts).forEach(gamePlayer::sendMessage));
        LoggerUtil.printChat(texts);
    }

    public void broadcastMessage(List<String> textList) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> textList.forEach(gamePlayer::sendMessage));
        LoggerUtil.printChat(textList);
    }

    public void broadcastSound(Sound sound, float volume, float pitch) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> gamePlayer.playSound(sound, volume, pitch));
    }

    public String getFormattedTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public void start() {
        BedwarsGameStartEvent.Pre preEvent = new BedwarsGameStartEvent.Pre(this);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }

        if (gameStartTask != null) {
            gameStartTask.cancel();
            gameStartTask = null;
        }
        plugin.getConfigService().applyPending();
        gameState = GameState.RUNNING;
        updateServerMOTD();

        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            if (gamePlayer.getGameTeam() == null) {
                getLowestTeam().addPlayer(gamePlayer);
            }
        }
        timeline.start();

        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Player player = gamePlayer.getPlayer();
            GameTeam gameTeam = gamePlayer.getGameTeam();
            if (gameTeam == null) {
                player.kickPlayer("");
                continue;
            }
            player.setGameMode(GameMode.SURVIVAL);
            gamePlayer.cleanState();
            gamePlayer.recordGamePlayed();
            player.setVelocity(new Vector(0, 0, 0));
            player.setFallDistance(0.0F);
            player.teleport(gameTeam.getSpawnLocation());
            gamePlayer.giveInventory();
        }
        for (GameTeam gameTeam : gameTeams) {
            if (gameTeam.getGamePlayers().isEmpty()) {
                gameTeam.setDestroyed(true);
            }
        }

        tabListManager.updateAllTabListNames();
        PlayerAFKListener.startCheckAFKTask();
        Bukkit.getPluginManager().callEvent(new BedwarsGameStartEvent.Post(this));
    }

    public void end() {
        aliveTeamsAtEnd = getAliveTeams();
        gameState = GameState.ENDING;
        updateServerMOTD();
    }

    /**
     * Teams whose players all left wait while their bed stands, except when only two teams remain
     */
    public boolean isOver() {
        int alive = getAliveTeams().size();
        long waiting = gameTeams.stream().filter(this::isWaitingForReconnect).count();
        return alive <= 1 && alive + waiting <= 2;
    }

    public boolean isWaitingForReconnect(GameTeam gameTeam) {
        return gameTeam.isDead() && !gameTeam.isDestroyed()
                && reconnectStates.values().stream().anyMatch(state -> state.getTeamColor() == gameTeam.getTeamColor());
    }

    /**
     * The bed breaker takes a final kill for every player of a team that left
     */
    public void eliminateOfflineTeam(GameTeam gameTeam, GamePlayer bedBreaker) {
        PlayerDataService playerDataService = plugin.getDatabaseProviderFactory().getPlayerDataService();
        for (ReconnectState state : reconnectStates.values()) {
            if (state.getTeamColor() != gameTeam.getTeamColor()) {
                continue;
            }
            PlayerData playerData = playerDataService.getCachedPlayerData(state.getUuid());
            if (playerData != null) {
                playerData.addFinalDeaths();
            }
            bedBreaker.recordKill(true);
            if (!VaultUtil.ecoIsNull) {
                rewardFinalKillCoins(bedBreaker);
            }
        }
        eliminateTeam(gameTeam);
    }

    /**
     * Frozen when the game ends, so players leaving afterwards do not change the result
     */
    @NotNull
    public List<GameTeam> getAliveTeams() {
        if (aliveTeamsAtEnd != null) {
            return aliveTeamsAtEnd;
        }
        return gameTeams.stream()
                .filter(team -> !team.isDead())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Nullable
    public GameTeam getWinner() {
        List<GameTeam> aliveTeams = getAliveTeams();
        return aliveTeams.size() == 1 ? aliveTeams.get(0) : null;
    }

    public boolean isTie() {
        return getAliveTeams().size() > 1;
    }

    @NotNull
    public List<GameTeam> getGameTeams() {
        return Collections.unmodifiableList(gameTeams);
    }

    @Nullable
    public GamePlayer findTargetPlayer(GamePlayer gamePlayer) {
        Location location = gamePlayer.getPlayer().getLocation();
        GamePlayer closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (GamePlayer target : GamePlayer.getOnlinePlayers()) {
            if (target.getGameTeam() == null || target.getGameTeam() == gamePlayer.getGameTeam()
                    || target.isSpectator() || target.isRespawning()
                    || target.getPlayer().getWorld() != location.getWorld()) {
                continue;
            }
            double distance = target.getPlayer().getLocation().distanceSquared(location);
            if (distance < closestDistance) {
                closestPlayer = target;
                closestDistance = distance;
            }
        }
        return closestPlayer;
    }
}
