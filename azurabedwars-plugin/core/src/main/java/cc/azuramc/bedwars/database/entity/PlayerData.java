package cc.azuramc.bedwars.database.entity;

import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.level.PlayerLevelManager;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
@Getter
@Setter
public class PlayerData {

    private String id;
    private String name;
    private UUID uuid;
    private GameModeType mode;
    private int level;
    private double experience;
    private int kills;
    private int deaths;
    private int assists;
    private int finalKills;
    private int finalDeaths;
    private int destroyedBeds;
    private int wins;
    private int ties;
    private int losses;
    private int games;
    private String shopDataJson;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private GamePlayer gamePlayer;

    public PlayerData(GamePlayer gamePlayer) {
        this.setName(gamePlayer.getName());
        this.setUuid(gamePlayer.getUuid());
        this.setMode(GameModeType.DEFAULT);
        this.setLevel(1);
        this.setExperience(0.0);
        this.setKills(0);
        this.setDeaths(0);
        this.setAssists(0);
        this.setFinalKills(0);
        this.setFinalDeaths(0);
        this.setDestroyedBeds(0);
        this.setWins(0);
        this.setLosses(0);
        this.setTies(0);
        this.setGames(0);
        this.setShopDataJson("{}");
        this.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        this.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        this.gamePlayer = gamePlayer;
    }

    public void addLevel(int level) {
        this.level += level;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addExperience(double experience) {
        this.experience += experience;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addKills(int kills) {
        gamePlayer.addCurrentGameKills(kills);
        this.kills += kills;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addDeaths(int deaths) {
        gamePlayer.addCurrentGameDeaths(deaths);
        this.deaths += deaths;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addAssists(int assists) {
        gamePlayer.addCurrentGameAssists(assists);
        this.assists += assists;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addFinalKills(int finalKills) {
        gamePlayer.addCurrentGameFinalKills(finalKills);
        this.finalKills += finalKills;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addFinalDeaths(int finalDeaths) {
        this.finalDeaths += finalDeaths;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addDestroyedBeds(int destroyedBeds) {
        gamePlayer.addCurrentGameDestroyedBeds(destroyedBeds);
        this.destroyedBeds += destroyedBeds;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addWins(int wins) {
        this.wins += wins;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addTies(int ties) {
        this.ties += ties;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addLosses(int losses) {
        this.losses += losses;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addGames(int games) {
        this.games += games;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addLevel() {
        this.level++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addKills() {
        PlayerLevelManager.addExperience(this.gamePlayer, 1);
        gamePlayer.addCurrentGameKills();
        this.kills++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addDeaths() {
        gamePlayer.addCurrentGameDeaths();
        this.deaths++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addAssists() {
        PlayerLevelManager.addExperience(this.gamePlayer, 0.5);
        gamePlayer.addCurrentGameAssists();
        this.assists++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addFinalKills() {
        PlayerLevelManager.addExperience(this.gamePlayer, 2);
        gamePlayer.addCurrentGameFinalKills();
        this.finalKills++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addFinalDeaths() {
        this.finalDeaths++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addDestroyedBeds() {
        PlayerLevelManager.addExperience(this.gamePlayer, 3);
        gamePlayer.addCurrentGameDestroyedBeds();
        this.destroyedBeds++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addWins() {
        PlayerLevelManager.addExperience(this.gamePlayer, 5);
        this.wins++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addTies() {
        this.ties++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addLosses() {
        this.losses++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public void addGames() {
        PlayerLevelManager.addExperience(this.gamePlayer, 0.1);
        this.games++;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
