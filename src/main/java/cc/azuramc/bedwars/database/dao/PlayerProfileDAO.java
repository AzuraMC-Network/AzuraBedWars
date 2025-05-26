package cc.azuramc.bedwars.database.dao;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.DatabaseConstants;
import cc.azuramc.bedwars.database.profile.PlayerProfile;
import cc.azuramc.bedwars.database.query.QueryBuilder;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 玩家数据DAO类
 * 负责PlayerProfile的数据库操作
 * 
 * @author an5w1r@163.com
 */
public class PlayerProfileDAO {
    private static final Logger LOGGER = Logger.getLogger(PlayerProfileDAO.class.getName());
    private static final String PLAYER_DATA_TABLE = AzuraBedWars.PLAYER_DATA_TABLE;
    private static final String PLAYER_SHOP_TABLE = AzuraBedWars.PLAYER_SHOP_TABLE;
    private static final String SPECTATOR_SETTINGS_TABLE = AzuraBedWars.SPECTATOR_SETTINGS_TABLE;
    
    private final DatabaseManager databaseManager;

    private static PlayerProfileDAO instance;

    public static synchronized PlayerProfileDAO getInstance() {
        if (instance == null) {
            instance = new PlayerProfileDAO();
        }
        return instance;
    }
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private PlayerProfileDAO() {
        this.databaseManager = DatabaseManager.getInstance();
        
        // 确保表存在
        ensureTablesExist();
    }
    
    /**
     * 确保所有必要的表都存在
     */
    private void ensureTablesExist() {
        try {
            // 创建表
            QueryBuilder queryBuilder = new QueryBuilder()
                .table(PLAYER_DATA_TABLE);
            
            databaseManager.executeUpdate(queryBuilder.buildCreateTableQuery(
                DatabaseConstants.PLAYER_STATS_TABLE_DEFINITION), null);
            
            queryBuilder = new QueryBuilder()
                .table(PLAYER_SHOP_TABLE);
            
            databaseManager.executeUpdate(queryBuilder.buildCreateTableQuery(
                DatabaseConstants.PLAYER_SHOP_TABLE_DEFINITION), null);
            
            queryBuilder = new QueryBuilder()
                .table(SPECTATOR_SETTINGS_TABLE);
            
            databaseManager.executeUpdate(queryBuilder.buildCreateTableQuery(
                DatabaseConstants.SPECTATOR_TABLE_DEFINITION), null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "创建数据库表时出错", e);
        }
    }
    
    /**
     * 加载玩家数据
     * 
     * @param gamePlayer 游戏玩家
     * @return 玩家数据
     */
    public PlayerProfile loadPlayerProfile(GamePlayer gamePlayer) {
        try {
            QueryBuilder queryBuilder = new QueryBuilder()
                .table(PLAYER_DATA_TABLE)
                .select("*")
                .where(DatabaseConstants.QUERY_WHERE_NAME_EQUALS, gamePlayer.getName());
            
            return databaseManager.executeQuery(queryBuilder, resultSet -> {
                try {
                    if (resultSet.next()) {
                        PlayerProfile profile = new PlayerProfile(gamePlayer);
                        profile.setGameModeType(GameModeType.valueOf(resultSet.getString(DatabaseConstants.PLAYER_STATS_COL_MODE)));
                        profile.setKills(resultSet.getInt(DatabaseConstants.PLAYER_STATS_COL_KILLS));
                        profile.setDeaths(resultSet.getInt(DatabaseConstants.PLAYER_STATS_COL_DEATHS));
                        profile.setDestroyedBeds(resultSet.getInt(DatabaseConstants.PLAYER_STATS_COL_DESTROYED_BEDS));
                        profile.setWins(resultSet.getInt(DatabaseConstants.PLAYER_STATS_COL_WINS));
                        profile.setLoses(resultSet.getInt(DatabaseConstants.PLAYER_STATS_COL_LOSES));
                        profile.setGames(resultSet.getInt(DatabaseConstants.PLAYER_STATS_COL_GAMES));
                        return profile;
                    } else {
                        // 创建新玩家数据
                        PlayerProfile profile = new PlayerProfile(gamePlayer);
                        profile.setGameModeType(GameModeType.DEFAULT);
                        
                        QueryBuilder insertBuilder = new QueryBuilder()
                            .table(PLAYER_DATA_TABLE)
                            .insert(DatabaseConstants.PLAYER_STATS_COL_NAME, gamePlayer.getName())
                            .insert(DatabaseConstants.PLAYER_STATS_COL_MODE, GameModeType.DEFAULT.toString())
                            .insert(DatabaseConstants.PLAYER_STATS_COL_KILLS, 0)
                            .insert(DatabaseConstants.PLAYER_STATS_COL_DEATHS, 0)
                            .insert(DatabaseConstants.PLAYER_STATS_COL_DESTROYED_BEDS, 0)
                            .insert(DatabaseConstants.PLAYER_STATS_COL_WINS, 0)
                            .insert(DatabaseConstants.PLAYER_STATS_COL_LOSES, 0)
                            .insert(DatabaseConstants.PLAYER_STATS_COL_GAMES, 0);
                        
                        databaseManager.executeInsert(insertBuilder);
                        
                        return profile;
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "加载玩家数据时出错", e);
                    return null;
                }
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "加载玩家数据时出错", e);
            return null;
        }
    }
    
    /**
     * 异步加载玩家商店数据
     * 
     * @param playerProfile 玩家数据
     * @return 异步任务
     */
    public CompletableFuture<String[]> loadPlayerShopAsync(PlayerProfile playerProfile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(PLAYER_SHOP_TABLE)
                    .select(DatabaseConstants.PLAYER_SHOP_COL_DATA)
                    .where(DatabaseConstants.QUERY_WHERE_NAME_EQUALS, playerProfile.getGamePlayer().getName());
                
                return databaseManager.executeQuery(queryBuilder, resultSet -> {
                    try {
                        if (resultSet.next()) {
                            return resultSet.getString(DatabaseConstants.PLAYER_SHOP_COL_DATA).split(", ");
                        } else {
                            String[] shopSort = DatabaseConstants.DEFAULT_SHOP_ITEMS;
                            
                            StringBuilder dataBuilder = new StringBuilder();
                            for (String item : shopSort) {
                                if (!dataBuilder.isEmpty()) {
                                    dataBuilder.append(", ");
                                }
                                dataBuilder.append(item);
                            }
                            
                            QueryBuilder insertBuilder = new QueryBuilder()
                                .table(PLAYER_SHOP_TABLE)
                                .insert(DatabaseConstants.PLAYER_SHOP_COL_NAME, playerProfile.getGamePlayer().getName())
                                .insert(DatabaseConstants.PLAYER_SHOP_COL_DATA, dataBuilder.toString());
                            
                            databaseManager.executeInsert(insertBuilder);
                            
                            return shopSort;
                        }
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "加载玩家商店数据时出错", e);
                        return null;
                    }
                });
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "加载玩家商店数据时出错", e);
                return null;
            }
        });
    }
    
    /**
     * 保存玩家商店数据
     * 
     * @param playerProfile 玩家数据
     * @return 异步任务
     */
    public CompletableFuture<Boolean> savePlayerShopAsync(PlayerProfile playerProfile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String[] shopSort = playerProfile.getShopSort();
                if (shopSort == null) {
                    return false;
                }
                
                StringBuilder dataBuilder = new StringBuilder();
                for (String item : shopSort) {
                    if (dataBuilder.length() > 0) {
                        dataBuilder.append(", ");
                    }
                    dataBuilder.append(item);
                }
                
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(PLAYER_SHOP_TABLE)
                    .update(DatabaseConstants.PLAYER_SHOP_COL_DATA, dataBuilder.toString())
                    .where(DatabaseConstants.QUERY_WHERE_NAME_EQUALS, playerProfile.getGamePlayer().getName());
                
                int affected = databaseManager.executeUpdate(queryBuilder);
                return affected > 0;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "保存玩家商店数据时出错", e);
                return false;
            }
        });
    }
    
    /**
     * 更新玩家游戏模式
     * 
     * @param playerProfile 玩家数据
     * @param gameModeType 游戏模式
     * @return 异步任务
     */
    public CompletableFuture<Boolean> updateGameModeAsync(PlayerProfile playerProfile, GameModeType gameModeType) {
        if (playerProfile.getGameModeType() == gameModeType) {
            return CompletableFuture.completedFuture(true);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(PLAYER_DATA_TABLE)
                    .update(DatabaseConstants.PLAYER_STATS_COL_MODE, gameModeType.toString())
                    .where(DatabaseConstants.QUERY_WHERE_NAME_EQUALS, playerProfile.getGamePlayer().getName());
                
                int affected = databaseManager.executeUpdate(queryBuilder);
                if (affected > 0) {
                    playerProfile.setGameModeType(gameModeType);
                    return true;
                }
                return false;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "更新玩家游戏模式时出错", e);
                return false;
            }
        });
    }
    
    /**
     * 增加玩家击杀数
     * 
     * @param playerProfile 玩家数据
     * @return 异步任务
     */
    public CompletableFuture<Boolean> incrementKillsAsync(PlayerProfile playerProfile) {
        playerProfile.setKills(playerProfile.getKills() + 1);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(PLAYER_DATA_TABLE)
                    .update(DatabaseConstants.PLAYER_STATS_COL_KILLS, playerProfile.getKills())
                    .where(DatabaseConstants.QUERY_WHERE_NAME_EQUALS, playerProfile.getGamePlayer().getName());
                
                int affected = databaseManager.executeUpdate(queryBuilder);
                return affected > 0;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "增加玩家击杀数时出错", e);
                return false;
            }
        });
    }
    
    /**
     * 增加玩家死亡数
     * 
     * @param playerProfile 玩家数据
     * @return 异步任务
     */
    public CompletableFuture<Boolean> incrementDeathsAsync(PlayerProfile playerProfile) {
        playerProfile.setDeaths(playerProfile.getDeaths() + 1);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(PLAYER_DATA_TABLE)
                    .update(DatabaseConstants.PLAYER_STATS_COL_DEATHS, playerProfile.getDeaths())
                    .where(DatabaseConstants.QUERY_WHERE_NAME_EQUALS, playerProfile.getGamePlayer().getName());
                
                int affected = databaseManager.executeUpdate(queryBuilder);
                return affected > 0;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "增加玩家死亡数时出错", e);
                return false;
            }
        });
    }
    
    /**
     * 增加玩家摧毁床数
     * 
     * @param playerProfile 玩家数据
     * @return 异步任务
     */
    public CompletableFuture<Boolean> incrementDestroyedBedsAsync(PlayerProfile playerProfile) {
        playerProfile.setDestroyedBeds(playerProfile.getDestroyedBeds() + 1);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(PLAYER_DATA_TABLE)
                    .update(DatabaseConstants.PLAYER_STATS_COL_DESTROYED_BEDS, playerProfile.getDestroyedBeds())
                    .where(DatabaseConstants.QUERY_WHERE_NAME_EQUALS, playerProfile.getGamePlayer().getName());
                
                int affected = databaseManager.executeUpdate(queryBuilder);
                return affected > 0;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "增加玩家摧毁床数时出错", e);
                return false;
            }
        });
    }
    
    /**
     * 增加玩家胜利数
     * 
     * @param playerProfile 玩家数据
     * @return 异步任务
     */
    public CompletableFuture<Boolean> incrementWinsAsync(PlayerProfile playerProfile) {
        playerProfile.setWins(playerProfile.getWins() + 1);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(PLAYER_DATA_TABLE)
                    .update(DatabaseConstants.PLAYER_STATS_COL_WINS, playerProfile.getWins())
                    .where(DatabaseConstants.QUERY_WHERE_NAME_EQUALS, playerProfile.getGamePlayer().getName());
                
                int affected = databaseManager.executeUpdate(queryBuilder);
                return affected > 0;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "增加玩家胜利数时出错", e);
                return false;
            }
        });
    }
    
    /**
     * 增加玩家失败数
     * 
     * @param playerProfile 玩家数据
     * @return 异步任务
     */
    public CompletableFuture<Boolean> incrementLosesAsync(PlayerProfile playerProfile) {
        playerProfile.setLoses(playerProfile.getLoses() + 1);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(PLAYER_DATA_TABLE)
                    .update(DatabaseConstants.PLAYER_STATS_COL_LOSES, playerProfile.getLoses())
                    .where(DatabaseConstants.QUERY_WHERE_NAME_EQUALS, playerProfile.getGamePlayer().getName());
                
                int affected = databaseManager.executeUpdate(queryBuilder);
                return affected > 0;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "增加玩家失败数时出错", e);
                return false;
            }
        });
    }
    
    /**
     * 增加玩家游戏数
     * 
     * @param playerProfile 玩家数据
     * @return 异步任务
     */
    public CompletableFuture<Boolean> incrementGamesAsync(PlayerProfile playerProfile) {
        playerProfile.setGames(playerProfile.getGames() + 1);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .table(PLAYER_DATA_TABLE)
                    .update(DatabaseConstants.PLAYER_STATS_COL_GAMES, playerProfile.getGames())
                    .where(DatabaseConstants.QUERY_WHERE_NAME_EQUALS, playerProfile.getGamePlayer().getName());
                
                int affected = databaseManager.executeUpdate(queryBuilder);
                return affected > 0;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "增加玩家游戏数时出错", e);
                return false;
            }
        });
    }
    
    /**
     * 获取所有玩家的名称列表
     * 
     * @return 玩家名称列表
     */
    public List<String> getAllPlayerNames() {
        try {
            QueryBuilder queryBuilder = new QueryBuilder()
                .table(PLAYER_DATA_TABLE)
                .select(DatabaseConstants.PLAYER_STATS_COL_NAME);
            
            return databaseManager.executeQuery(queryBuilder, resultSet -> {
                List<String> names = new ArrayList<>();
                try {
                    while (resultSet.next()) {
                        names.add(resultSet.getString(DatabaseConstants.PLAYER_STATS_COL_NAME));
                    }
                    return names;
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "获取所有玩家名称时出错", e);
                    return names;
                }
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "获取所有玩家名称时出错", e);
            return new ArrayList<>();
        }
    }
} 