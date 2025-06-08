package cc.azuramc.bedwars.database.entity;

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
    private String shopData;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private boolean isDirty;

    public void setId(int id) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.id = id;
    }
    public void setName(String name) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.name = name;
    }
    public void setUuid(UUID uuid) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.uuid = uuid;
    }
    public void setMode(GameModeType mode) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.mode = mode;
    }
    public void setKills(int kills) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.kills = kills;
    }
    public void setDeaths(int deaths) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.deaths = deaths;
    }
    public void setAssists(int assists) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.assists = assists;
    }
    public void setFinalKills(int finalKills) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.finalKills = finalKills;
    }
    public void setDestroyedBeds(int destroyedBeds) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.destroyedBeds = destroyedBeds;
    }
    public void setWins(int wins) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.wins = wins;
    }
    public void setLosses(int losses) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.losses = losses;
    }
    public void setGames(int games) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.games = games;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(Timestamp updatedAt) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.updatedAt = updatedAt;
    }
    public void setShopData(String shopData) {
        this.isDirty = true;
        PlayerDataService.changeManager.registerDirty(this);
        this.shopData = shopData;
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
