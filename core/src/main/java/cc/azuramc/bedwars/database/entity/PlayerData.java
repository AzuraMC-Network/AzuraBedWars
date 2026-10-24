package cc.azuramc.bedwars.database.entity;

import cc.azuramc.bedwars.database.annotation.Column;
import cc.azuramc.bedwars.database.annotation.QueryField;
import cc.azuramc.bedwars.database.annotation.Table;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.level.PlayerLevelManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
@Getter
@Setter
@Table("player_data")
public class PlayerData {

    /**
     * 查询键枚举
     */
    public enum Query {
        BY_UUID
    }

    @Column(value = "id", primaryKey = true, autoIncrement = true)
    private String id;

    @Column(value = "name", size = 16, nullable = false)
    private String name;

    @QueryField("BY_UUID")
    @Column(value = "uuid", size = 36, nullable = false)
    private UUID uuid;

    @Column(value = "mode", size = 16, nullable = false)
    private GameModeType mode;

    @Column(value = "level", defaultValue = "1")
    private int level;

    @Column(value = "experience", defaultValue = "0.0")
    private double experience;

    @Column(value = "kills", defaultValue = "0")
    private int kills;

    @Column(value = "deaths", defaultValue = "0")
    private int deaths;

    @Column(value = "assists", defaultValue = "0")
    private int assists;

    @Column(value = "final_kills", defaultValue = "0")
    private int finalKills;

    @Column(value = "final_deaths", defaultValue = "0")
    private int finalDeaths;

    @Column(value = "destroyed_beds", defaultValue = "0")
    private int destroyedBeds;

    @Column(value = "wins", defaultValue = "0")
    private int wins;

    @Column(value = "ties", defaultValue = "0")
    private int ties;

    @Column(value = "losses", defaultValue = "0")
    private int losses;

    @Column(value = "games", defaultValue = "0")
    private int games;

    @Column(value = "shop_data_json", type = "TEXT")
    private String shopDataJson;

    @Column(value = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(value = "updated_at")
    private Timestamp updatedAt;

    @NotNull
    private transient GamePlayer gamePlayer;

    public PlayerData(@NotNull GamePlayer gamePlayer) {
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
