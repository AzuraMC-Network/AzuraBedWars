package cc.azuramc.bedwars.database.entity;

import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.level.PlayerLevelManager;
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
    private int level;
    private double experience;
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
        this.setDestroyedBeds(0);
        this.setWins(0);
        this.setLosses(0);
        this.setGames(0);
        this.setShopDataJson("{}");
        this.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        this.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        this.gamePlayer = gamePlayer;
    }

    public void addLevel(int level) {
        this.level = this.level + level;
    }
    public void addExperience(double experience) {
        this.experience = this.experience + experience;
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

    public void addLevel() {
        this.level++;
    }
    public void addKills() {
        PlayerLevelManager.addExperience(this.gamePlayer, 1);
        this.kills++;
    }
    public void addDeaths() {
        this.deaths++;
    }
    public void addAssists() {
        PlayerLevelManager.addExperience(this.gamePlayer, 0.5);
        this.assists++;
    }
    public void addFinalKills() {
        PlayerLevelManager.addExperience(this.gamePlayer, 2);
        this.finalKills++;
    }
    public void addDestroyedBeds() {
        PlayerLevelManager.addExperience(this.gamePlayer, 3);
        this.destroyedBeds++;
    }
    public void addWins() {
        PlayerLevelManager.addExperience(this.gamePlayer, 5);
        this.wins++;
    }
    public void addLosses() {
        this.losses++;
    }
    public void addGames() {
        PlayerLevelManager.addExperience(this.gamePlayer, 0.1);
        this.games++;
    }
}
