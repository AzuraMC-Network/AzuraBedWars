package cc.azuramc.bedwars.database.entity;

import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
@Data
public class PlayerData {

    private int id;
    private String name;
    private UUID uuid;
    private GameModeType mode;
    private double level;
    private int kills;
    private int deaths;
    private int assists;
    private int finalKills;
    private int destroyedBeds;
    private int wins;
    private int losses;
    private int games;
    private String shopDataJson;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public PlayerData(GamePlayer gamePlayer) {
        this.setName(gamePlayer.getName());
        this.setUuid(gamePlayer.getUuid());
        this.setMode(GameModeType.DEFAULT);
        this.setLevel(0.0);
        this.setKills(0);
        this.setDeaths(0);
        this.setAssists(0);
        this.setFinalKills(0);
        this.setDestroyedBeds(0);
        this.setWins(0);
        this.setLosses(0);
        this.setGames(0);
        this.setShopDataJson("{}");
        this.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        this.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    }

    public void addLevel(double level) {
        this.level = this.level + level;
    }
    public void addKills(int kills) {
        this.kills = this.kills + kills;
    }
    public void addDeaths(int deaths) {
        this.deaths = this.deaths + deaths;
    }
    public void addAssists(int assists) {
        this.assists = this.assists + assists;
    }
    public void addFinalKills(int finalKills) {
        this.finalKills = this.finalKills + finalKills;
    }
    public void addDestroyedBeds(int destroyedBeds) {
        this.destroyedBeds = this.destroyedBeds + destroyedBeds;
    }
    public void addWins(int wins) {
        this.wins = this.wins + wins;
    }
    public void addLosses(int losses) {
        this.losses = this.losses + losses;
    }
    public void addGames(int games) {
        this.games = this.games + games;
    }

    public void addKills() {
        this.kills++;
    }
    public void addDeaths() {
        this.deaths++;
    }
    public void addAssists() {
        this.assists++;
    }
    public void addFinalKills() {
        this.finalKills++;
    }
    public void addDestroyedBeds() {
        this.destroyedBeds++;
    }
    public void addWins() {
        this.wins++;
    }
    public void addLosses() {
        this.losses++;
    }
    public void addGames() {
        this.games++;
    }
}
