package cc.azuramc.bedwars.database;

/**
 * 数据库常量类
 * 存储所有与数据库相关的常量，避免魔法值
 * 
 * @author an5w1r@163.com
 */
public class DatabaseConstants {
    
    /** 数据库连接池配置 */
    public static final int POOL_MAX_SIZE = 10;
    public static final int POOL_MIN_IDLE = 5;
    public static final int POOL_IDLE_TIMEOUT = 300000;
    public static final int POOL_CONNECTION_TIMEOUT = 20000;
    public static final int POOL_MAX_LIFETIME = 1200000;
    
    /** SQL数据类型 */
    public static final String TYPE_VARCHAR_36 = "VARCHAR(36)";
    public static final String TYPE_VARCHAR_32 = "VARCHAR(32)";
    public static final String TYPE_VARCHAR_64 = "VARCHAR(64)";
    public static final String TYPE_VARCHAR_20 = "VARCHAR(20)";
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_INT = "INT";
    public static final String TYPE_BOOLEAN = "BOOLEAN";
    public static final String TYPE_TIMESTAMP = "TIMESTAMP";
    public static final String TYPE_AUTO_INCREMENT = "AUTO_INCREMENT";
    
    /** 玩家统计表 */
    public static final String PLAYER_STATS_COL_NAME = "name";
    public static final String PLAYER_STATS_COL_MODE = "mode";
    public static final String PLAYER_STATS_COL_KILLS = "kills";
    public static final String PLAYER_STATS_COL_DEATHS = "deaths";
    public static final String PLAYER_STATS_COL_DESTROYED_BEDS = "destroyedBeds";
    public static final String PLAYER_STATS_COL_WINS = "wins";
    public static final String PLAYER_STATS_COL_LOSES = "loses";
    public static final String PLAYER_STATS_COL_GAMES = "games";
    
    /** 玩家商店表 */
    public static final String PLAYER_SHOP_COL_NAME = "name";
    public static final String PLAYER_SHOP_COL_DATA = "data";
    
    /** 观战者设置表 */
    public static final String SPECTATOR_COL_NAME = "name";
    public static final String SPECTATOR_COL_SPEED = "speed";
    public static final String SPECTATOR_COL_AUTO_TP = "autoTp";
    public static final String SPECTATOR_COL_NIGHT_VISION = "nightVision";
    public static final String SPECTATOR_COL_FIRST_PERSON = "firstPerson";
    public static final String SPECTATOR_COL_HIDE_OTHER = "hideOther";
    public static final String SPECTATOR_COL_FLY = "fly";
    
    /** 地图表 */
    public static final String MAP_COL_ID = "id";
    public static final String MAP_COL_NAME = "name";
    public static final String MAP_COL_DISPLAY_NAME = "display_name";
    public static final String MAP_COL_MIN_PLAYERS = "min_players";
    public static final String MAP_COL_MAX_PLAYERS = "max_players";
    public static final String MAP_COL_TEAMS = "teams";
    public static final String MAP_COL_DATA = "data";
    public static final String MAP_COL_AUTHOR = "author";
    
    /** 通用列 */
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";
    
    /** 数据库表结构定义 */
    public static final String PLAYER_STATS_TABLE_DEFINITION = 
        PLAYER_STATS_COL_NAME + " " + TYPE_VARCHAR_36 + " PRIMARY KEY, " +
        PLAYER_STATS_COL_MODE + " " + TYPE_VARCHAR_20 + " NOT NULL, " +
        PLAYER_STATS_COL_KILLS + " " + TYPE_INT + " DEFAULT 0, " +
        PLAYER_STATS_COL_DEATHS + " " + TYPE_INT + " DEFAULT 0, " +
        PLAYER_STATS_COL_DESTROYED_BEDS + " " + TYPE_INT + " DEFAULT 0, " +
        PLAYER_STATS_COL_WINS + " " + TYPE_INT + " DEFAULT 0, " +
        PLAYER_STATS_COL_LOSES + " " + TYPE_INT + " DEFAULT 0, " +
        PLAYER_STATS_COL_GAMES + " " + TYPE_INT + " DEFAULT 0, " +
        COL_CREATED_AT + " " + TYPE_TIMESTAMP + " DEFAULT CURRENT_TIMESTAMP, " +
        COL_UPDATED_AT + " " + TYPE_TIMESTAMP + " DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
    
    public static final String PLAYER_SHOP_TABLE_DEFINITION = 
        PLAYER_SHOP_COL_NAME + " " + TYPE_VARCHAR_36 + " PRIMARY KEY, " +
        PLAYER_SHOP_COL_DATA + " " + TYPE_TEXT + " NOT NULL, " +
        COL_CREATED_AT + " " + TYPE_TIMESTAMP + " DEFAULT CURRENT_TIMESTAMP, " +
        COL_UPDATED_AT + " " + TYPE_TIMESTAMP + " DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
    
    public static final String SPECTATOR_TABLE_DEFINITION = 
        SPECTATOR_COL_NAME + " " + TYPE_VARCHAR_36 + " PRIMARY KEY, " +
        SPECTATOR_COL_SPEED + " " + TYPE_INT + " DEFAULT 0, " +
        SPECTATOR_COL_AUTO_TP + " " + TYPE_BOOLEAN + " DEFAULT false, " +
        SPECTATOR_COL_NIGHT_VISION + " " + TYPE_BOOLEAN + " DEFAULT false, " +
        SPECTATOR_COL_FIRST_PERSON + " " + TYPE_BOOLEAN + " DEFAULT true, " +
        SPECTATOR_COL_HIDE_OTHER + " " + TYPE_BOOLEAN + " DEFAULT false, " +
        SPECTATOR_COL_FLY + " " + TYPE_BOOLEAN + " DEFAULT false, " +
        COL_CREATED_AT + " " + TYPE_TIMESTAMP + " DEFAULT CURRENT_TIMESTAMP, " +
        COL_UPDATED_AT + " " + TYPE_TIMESTAMP + " DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
    
    public static final String MAP_TABLE_DEFINITION = 
        MAP_COL_ID + " " + TYPE_INT + " " + TYPE_AUTO_INCREMENT + " PRIMARY KEY, " +
        MAP_COL_NAME + " " + TYPE_VARCHAR_32 + " NOT NULL UNIQUE, " +
        MAP_COL_DISPLAY_NAME + " " + TYPE_VARCHAR_64 + " NOT NULL, " +
        MAP_COL_MIN_PLAYERS + " " + TYPE_INT + " NOT NULL, " +
        MAP_COL_MAX_PLAYERS + " " + TYPE_INT + " NOT NULL, " +
        MAP_COL_TEAMS + " " + TYPE_INT + " NOT NULL, " +
        MAP_COL_DATA + " " + TYPE_TEXT + " NOT NULL, " +
        MAP_COL_AUTHOR + " " + TYPE_VARCHAR_64 + ", " +
        COL_CREATED_AT + " " + TYPE_TIMESTAMP + " DEFAULT CURRENT_TIMESTAMP, " +
        COL_UPDATED_AT + " " + TYPE_TIMESTAMP + " DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
        
    /** 默认商店项目 */
    public static final String[] DEFAULT_SHOP_ITEMS = new String[]{
        "BlockShop#1", "SwordShop#1", "ArmorShop#1", "FoodShop#1", 
        "BowShop#2", "PotionShop#1", "UtilityShop#2", "BlockShop#8", 
        "SwordShop#2", "ArmorShop#2", "UtilityShop#1", "BowShop#1", 
        "PotionShop#2", "UtilityShop#4", "AIR", "AIR", "AIR", "AIR", 
        "AIR", "AIR", "AIR"
    };
    
    /** SQL关键词 */
    public static final String SQL_PRIMARY_KEY = "PRIMARY KEY";
    public static final String SQL_NOT_NULL = "NOT NULL";
    public static final String SQL_UNIQUE = "UNIQUE";
    public static final String SQL_DEFAULT = "DEFAULT";
    public static final String SQL_DEFAULT_CURRENT_TIMESTAMP = "DEFAULT CURRENT_TIMESTAMP";
    public static final String SQL_ON_UPDATE_CURRENT_TIMESTAMP = "ON UPDATE CURRENT_TIMESTAMP";
    
    /** 查询语句常用部分 */
    public static final String QUERY_WHERE_NAME_EQUALS = "name = ?";
} 