package cc.azuramc.bedwars.database.entity;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.service.PlayerDataService;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.orm.manager.ChangeManager;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
@Getter
public class PlayerData implements ChangeManager.DirtyTracker {
    
    private PlayerDataService playerDataService = AzuraBedWars.getInstance().getPlayerDataService();

    private int id;
    private String name;
    private UUID uuid;
    private GameModeType mode;
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

    private boolean isDirty;

    public void setId(int id) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.id = id;
    }
    public void setName(String name) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.name = name;
    }
    public void setUuid(UUID uuid) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.uuid = uuid;
    }
    public void setMode(GameModeType mode) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.mode = mode;
    }
    public void setKills(int kills) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.kills = kills;
    }
    public void setDeaths(int deaths) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.deaths = deaths;
    }
    public void setAssists(int assists) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.assists = assists;
    }
    public void setFinalKills(int finalKills) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.finalKills = finalKills;
    }
    public void setDestroyedBeds(int destroyedBeds) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.destroyedBeds = destroyedBeds;
    }
    public void setWins(int wins) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.wins = wins;
    }
    public void setLosses(int losses) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.losses = losses;
    }
    public void setGames(int games) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.games = games;
    }

    public void addKills(int kills) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.kills = this.kills + kills;
    }
    public void addDeaths(int deaths) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.deaths = this.deaths + deaths;
    }
    public void addAssists(int assists) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.assists = this.assists + assists;
    }
    public void addFinalKills(int finalKills) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.finalKills = this.finalKills + finalKills;
    }
    public void addDestroyedBeds(int destroyedBeds) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.destroyedBeds = this.destroyedBeds + destroyedBeds;
    }
    public void addWins(int wins) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.wins = this.wins + wins;
    }
    public void addLosses(int losses) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.losses = this.losses + losses;
    }
    public void addGames(int games) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.games = this.games + games;
    }

    public void addKills() {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.kills++;
    }
    public void addDeaths() {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.deaths++;
    }
    public void addAssists() {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.assists++;
    }
    public void addFinalKills() {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.finalKills++;
    }
    public void addDestroyedBeds() {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.destroyedBeds++;
    }
    public void addWins() {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.wins++;
    }
    public void addLosses() {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.losses++;
    }
    public void addGames() {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.games++;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(Timestamp updatedAt) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.updatedAt = updatedAt;
    }
    public void setShopDataJson(String shopDataJson) {
        this.isDirty = true;
        playerDataService.changeManager.registerDirty(this);
        this.shopDataJson = shopDataJson;
    }

    @Override
    public boolean isDirty() {
        return this.isDirty;
    }

    @Override
    public void cleanDirty() {
        this.isDirty =  false;
    }
}
