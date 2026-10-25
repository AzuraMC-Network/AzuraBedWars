package cc.azuramc.bedwars.database.entity;

import cc.azuramc.bedwars.database.annotation.Column;
import cc.azuramc.bedwars.database.annotation.QueryField;
import cc.azuramc.bedwars.database.annotation.Table;
import cc.azuramc.bedwars.game.GamePlayer;
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

    public PlayerData(@NotNull GamePlayer gamePlayer) {
        this.setName(gamePlayer.getName());
        this.setUuid(gamePlayer.getUuid());
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
    }

    public void addExperience(double experience) {
        this.experience += experience;
        touch();
    }

    public void addKills() {
        this.kills++;
        touch();
    }

    public void addDeaths() {
        this.deaths++;
        touch();
    }

    public void addAssists() {
        this.assists++;
        touch();
    }

    public void addFinalKills() {
        this.finalKills++;
        touch();
    }

    public void addFinalDeaths() {
        this.finalDeaths++;
        touch();
    }

    public void addDestroyedBeds() {
        this.destroyedBeds++;
        touch();
    }

    public void addWins() {
        this.wins++;
        touch();
    }

    public void addTies() {
        this.ties++;
        touch();
    }

    public void addLosses() {
        this.losses++;
        touch();
    }

    public void addGames() {
        this.games++;
        touch();
    }

    private void touch() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
