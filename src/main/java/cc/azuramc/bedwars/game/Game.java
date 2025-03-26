package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.map.MapData;
import cc.azuramc.bedwars.events.BedwarsGameStartEvent;
import cc.azuramc.bedwars.game.event.EventManager;
import cc.azuramc.bedwars.scoreboards.GameBoard;
import cc.azuramc.bedwars.scoreboards.LobbyBoard;
import cc.azuramc.bedwars.shop.ItemShopManager;
import cc.azuramc.bedwars.specials.SpecialItem;
import cc.azuramc.bedwars.utils.Util;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

import java.util.*;

@Data
public class Game {
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

    private HashMap<ArmorStand, String> armorSande;
    private HashMap<ArmorStand, String> armorStand;

    private List<SpecialItem> specialItems;

    public Game(AzuraBedWars main, Location waitingLocation) {
        this.main = main;
        this.forceStart = false;
        this.waitingLocation = waitingLocation;
        this.gameTeams = new ArrayList<>();
        this.gameParties = new ArrayList<>();

        this.armorSande = new HashMap<>();
        this.armorStand = new HashMap<>();

        this.specialItems = new ArrayList<>();

        ItemShopManager.init(this);
        this.eventManager = new EventManager(this);
    }

    public void loadGame(MapData mapData) {
        this.mapData = mapData;
        this.blocks = mapData.loadMap();
        this.respawnLocation = mapData.getReSpawn().toLocation();

        Util.spawnALL(main);

        for (int i = 0; i < mapData.getBases().size(); i++) {
            gameTeams.add(new GameTeam(TeamColor.values()[i], mapData.getBases().get(i).toLocation(), mapData.getPlayers().getTeam()));
        }

        this.gameState = GameState.WAITING;
    }

    public void addPlayer(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        if (gameState == GameState.RUNNING) {
            GameBoard.show(player);
            GameBoard.updateBoard();

            if (gamePlayer.getGameTeam() != null) {
                if (!gamePlayer.getGameTeam().isDead()) {
                    Util.setPlayerTeamTab();
                    try {
                        // 尝试使用新版本API (1.16+)
                        Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, respawnLocation, false, false, PlayerRespawnEvent.RespawnReason.PLUGIN));
                    } catch (Throwable e) {
                        // 如果新版本API不可用，回退到旧版本API (1.8)
                        Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, respawnLocation, false));
                    }
                    broadcastMessage("§7" + gamePlayer.getDisplayname() + "§a重连上线");
                    return;
                }

//                Bukkit.getPluginManager().callEvent(new RejoinGameDeathEvent(gamePlayer.getPlayer()));
            }

            gamePlayer.toSpectator(null, null);
            return;
        }

        GamePlayer.getOnlinePlayers().forEach((gamePlayer1 -> {
            hidePlayer(gamePlayer1.getPlayer(), player);
            showPlayer(gamePlayer1.getPlayer(), player);
            hidePlayer(player, player);
            showPlayer(player, player);
        }));

        player.spigot().respawn();
        player.setGameMode(GameMode.ADVENTURE);
        player.getEnderChest().clear();
        gamePlayer.clean();

        player.teleport(waitingLocation);

        LobbyBoard.show(player);
        LobbyBoard.updateBoard();

        player.getInventory().addItem(new ItemBuilderUtil().setType(Material.PAPER).setDisplayName("§a资源类型选择§7(右键选择)").getItem());
        player.getInventory().setItem(8, new ItemBuilderUtil().setType(Material.SLIME_BALL).setDisplayName("§c离开游戏§7(右键离开)").getItem());

        if (isStartable()) {
            if (gameState == GameState.WAITING && getGameLobbyCountdown() == null) {
                GameLobbyCountdown lobbyCountdown = new GameLobbyCountdown(this);
                lobbyCountdown.runTaskTimer(main, 20L, 20L);
                setGameLobbyCountdown(lobbyCountdown);
            }
        }
    }

    public void removePlayers(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        if (gameState == GameState.WAITING) {
            broadcastMessage("§7" + gamePlayer.getDisplayname() + "§e离开游戏");
        }

        if (gameState == GameState.RUNNING && gamePlayer.isSpectator()) {
            return;
        }

        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return;
        }

        if (gameTeam.isBedDestroy()) {
            gamePlayer.setGameTeam(null);
//            Bukkit.getPluginManager().callEvent(new RejoinGameDeathEvent(player));
        }

        if (gameTeam.getAlivePlayers().isEmpty()) {
            if (!gameTeam.isBedDestroy()) {
                gameTeam.setBedDestroy(true);
            }
        }
    }

    public GameParty getPlayerParty(GamePlayer gamePlayer) {
        for (GameParty gameParty : gameParties) {
            if (gameParty.isInTeam(gamePlayer)) {
                return gameParty;
            }
        }
        return null;
    }

    public void addParty(GameParty gameParty) {
        gameParties.add(gameParty);
    }

    public void removeParty(GameParty gameParty) {
        gameParties.remove(gameParty);
    }

    public int getMaxPlayers() {
        return mapData.getBases().size() * mapData.getPlayers().getTeam();
    }

    boolean hasEnoughPlayers() {
        return GamePlayer.getOnlinePlayers().size() >= mapData.getPlayers().getMin();
    }

    public GameTeam getLowestTeam() {
        GameTeam lowest = null;
        for (GameTeam gameTeam : gameTeams) {
            if (lowest == null) {
                lowest = gameTeam;
                continue;
            }

            if (!gameTeam.isFull() && gameTeam.getGamePlayers().size() < lowest.getGamePlayers().size()) {
                lowest = gameTeam;
            }
        }

        return lowest;
    }


    public void moveFreePlayersToTeam() {
        for (GameParty gameParty : gameParties) {
            GameTeam lowest = getLowestTeam();
            for (GamePlayer gamePlayer : gameParty.getPlayers()) {
                if (gamePlayer.getGameTeam() == null) {
                    if (!lowest.addPlayer(gamePlayer)) {
                        lowest = getLowestTeam();
                    }
                }
            }
        }

        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            if (gamePlayer.getGameTeam() == null) {
                GameTeam lowest = getLowestTeam();
                lowest.addPlayer(gamePlayer);
            }
        }
    }

    public void teleportPlayersToTeamSpawn() {
        for (GameTeam gameTeam : this.gameTeams) {
            for (GamePlayer gamePlayer : gameTeam.getAlivePlayers()) {
                Player player = gamePlayer.getPlayer();

                player.setVelocity(new Vector(0, 0, 0));
                player.setFallDistance(0.0F);
                player.teleport(gameTeam.getSpawn());
            }
        }
    }

    public GameTeam getTeam(TeamColor teamColor) {
        for (GameTeam gameTeam : gameTeams) {
            if (gameTeam.getTeamColor() == teamColor) {
                return gameTeam;
            }
        }
        return null;
    }

    public void addSpecialItem(SpecialItem specialItem) {
        this.specialItems.add(specialItem);
    }

    public void removeSpecialItem(SpecialItem specialItem) {
        this.specialItems.remove(specialItem);
    }

    public void broadcastTitle(Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> gamePlayer.sendTitle(fadeIn, stay, fadeOut, title, subTitle));
    }

    public void broadcastTeamTitle(GameTeam gameTeam, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> gamePlayer.sendTitle(fadeIn, stay, fadeOut, title, subTitle));
    }

    public void broadcastTeamMessage(GameTeam gameTeam, String... texts) {
        gameTeam.getAlivePlayers().forEach(player -> Arrays.asList(texts).forEach(player::sendMessage));
    }

    public void broadcastTeamSound(GameTeam gameTeam, Sound sound, float v, float v1) {
        gameTeam.getAlivePlayers().forEach(gamePlayer -> gamePlayer.playSound(sound, v, v1));
    }

    public void broadcastSpectatorMessage(String... texts) {
        GamePlayer.getSpectators().forEach(gamePlayer -> Arrays.asList(texts).forEach(gamePlayer::sendMessage));
    }

    public void broadcastMessage(String... texts) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> Arrays.asList(texts).forEach(gamePlayer::sendMessage));
    }

    public void broadcastSound(Sound sound, float v, float v1) {
        GamePlayer.getOnlinePlayers().forEach(gamePlayer -> gamePlayer.playSound(sound, v, v1));
    }

    public boolean isStartable() {
        return (this.hasEnoughPlayers() && this.hasEnoughTeams());
    }

    public boolean hasEnoughTeams() {
        int teamsWithPlayers = 0;
        for (GameTeam gameTeam : gameTeams) {
            if (!gameTeam.getGamePlayers().isEmpty()) {
                teamsWithPlayers++;
            }
        }

        List<GamePlayer> freePlayers = GamePlayer.getGamePlayers();
        freePlayers.removeAll(GamePlayer.getTeamPlayers());

        return (teamsWithPlayers > 1 || (teamsWithPlayers == 1 && !freePlayers.isEmpty()) || (teamsWithPlayers == 0 && freePlayers.size() >= 2));
    }

    public String getFormattedTime(int time) {
        String minStr;
        String secStr;
        int min = (int) (double) (time / 60);
        int sec = time % 60;
        minStr = min < 10 ? "0" + min : String.valueOf(min);
        secStr = sec < 10 ? "0" + sec : String.valueOf(sec);
        return minStr + ":" + secStr;
    }


    public void start() {
        gameState = GameState.RUNNING;

        moveFreePlayersToTeam();
        eventManager.start();

        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Player player = gamePlayer.getPlayer();

            if (gamePlayer.getGameTeam() == null) {
                player.kickPlayer("");
                continue;
            }

//            Bukkit.getPluginManager().callEvent(new RejoinPlayerJoinEvent(player));
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setGameMode(GameMode.SURVIVAL);
            gamePlayer.clean();
            gamePlayer.getPlayerData().addGames();
        }

        teleportPlayersToTeamSpawn();

        getGameTeams().forEach(team -> {
            if (team.getGamePlayers().isEmpty()) {
                team.setBedDestroy(true);
            }
        });

        GamePlayer.getOnlinePlayers().forEach(GamePlayer::giveInventory);
        Bukkit.getPluginManager().callEvent(new BedwarsGameStartEvent());
    }


    public boolean isOver() {
        int alives = 0;
        for (GameTeam gameTeam : gameTeams) {
            if (!gameTeam.isDead()) {
                alives += 1;
            }
        }
        return alives <= 1;
    }

    public GameTeam getWinner() {
        for (GameTeam team : gameTeams) {
            if (!team.isDead()) {
                return team;
            }
        }
        return null;
    }

    public GamePlayer findTargetPlayer(GamePlayer gamePlayer) {
        GamePlayer foundPlayer = null;
        double distance = Double.MAX_VALUE;

        ArrayList<GamePlayer> possibleTargets = new ArrayList<>(GamePlayer.getOnlinePlayers());
        possibleTargets.removeAll(gamePlayer.getGameTeam().getGamePlayers());
        possibleTargets.removeIf(GamePlayer::isSpectator);


        for (GamePlayer player1 : possibleTargets) {
            if (gamePlayer.getPlayer().getWorld() != player1.getPlayer().getWorld()) {
                continue;
            }

            double dist = gamePlayer.getPlayer().getLocation().distance(player1.getPlayer().getLocation());
            if (dist < distance) {
                foundPlayer = player1;
                distance = dist;
            }
        }

        return foundPlayer;
    }

    private void hidePlayer(Player player, Player target) {
        try {
            // 尝试使用新版本API (1.12.2+)
            player.hidePlayer(main, target);
        } catch (Throwable e) {
            // 如果新版本API不可用，回退到旧版本API
            player.hidePlayer(target);
        }
    }

    private void showPlayer(Player player, Player target) {
        try {
            // 尝试使用新版本API (1.12.2+)
            player.showPlayer(main, target);
        } catch (Throwable e) {
            // 如果新版本API不可用，回退到旧版本API
            player.showPlayer(target);
        }
    }
}
