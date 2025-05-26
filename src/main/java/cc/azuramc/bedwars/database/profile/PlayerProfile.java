package cc.azuramc.bedwars.database.profile;

import cc.azuramc.bedwars.database.dao.PlayerProfileDAO;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
public class PlayerProfile {
    private GamePlayer gamePlayer;
    private GameModeType gameModeType;
    private int kills;
    private int deaths;
    private int destroyedBeds;
    private int wins;
    private int loses;
    private int games;
    private String[] shopSort;

    /**
     * 创建一个新的玩家数据对象
     * 
     * @param gamePlayer 游戏玩家对象
     */
    public PlayerProfile(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
        
        // 使用DAO加载玩家数据
        PlayerProfile loadedProfile = PlayerProfileDAO.getInstance().loadPlayerProfile(gamePlayer);
        
        if (loadedProfile != null) {
            // 将加载的数据复制到当前对象
            this.gameModeType = loadedProfile.getGameModeType();
            this.kills = loadedProfile.getKills();
            this.deaths = loadedProfile.getDeaths();
            this.destroyedBeds = loadedProfile.getDestroyedBeds();
            this.wins = loadedProfile.getWins();
            this.loses = loadedProfile.getLoses();
            this.games = loadedProfile.getGames();
        } else {
            // 创建默认数据
            this.gameModeType = GameModeType.DEFAULT;
            this.kills = 0;
            this.deaths = 0;
            this.destroyedBeds = 0;
            this.wins = 0;
            this.loses = 0;
            this.games = 0;
        }
    }

    /**
     * 异步加载商店数据
     */
    public void asyncLoadShop() {
        PlayerProfileDAO.getInstance().loadPlayerShopAsync(this)
            .thenAccept(shopData -> {
                if (shopData != null) {
                    this.shopSort = shopData;
                }
            });
    }

    /**
     * 保存商店数据
     */
    public void saveShops() {
        PlayerProfileDAO.getInstance().savePlayerShopAsync(this);
    }

    /**
     * 设置游戏模式类型
     * 
     * @param gameModeType 游戏模式类型
     */
    public void setGameModeType(GameModeType gameModeType) {
        if (this.gameModeType == gameModeType) {
            return;
        }

        PlayerProfileDAO.getInstance().updateGameModeAsync(this, gameModeType)
            .thenAccept(success -> {
                if (success) {
                    this.gameModeType = gameModeType;
                }
            });
    }

    /**
     * 增加击杀数
     */
    public void addKills() {
        PlayerProfileDAO.getInstance().incrementKillsAsync(this);
    }

    /**
     * 增加最终击杀数
     */
    public void addFinalKills() {
        //finalKills += 1;
    }

    /**
     * 增加死亡数
     */
    public void addDeaths() {
        PlayerProfileDAO.getInstance().incrementDeathsAsync(this);
    }

    /**
     * 增加摧毁床数
     */
    public void addDestroyedBeds() {
        PlayerProfileDAO.getInstance().incrementDestroyedBedsAsync(this);
    }

    /**
     * 增加胜利数
     */
    public void addWins() {
        PlayerProfileDAO.getInstance().incrementWinsAsync(this);
    }

    /**
     * 增加失败数
     */
    public void addLoses() {
        PlayerProfileDAO.getInstance().incrementLosesAsync(this);
    }

    /**
     * 增加游戏数
     */
    public void addGames() {
        PlayerProfileDAO.getInstance().incrementGamesAsync(this);
    }
}