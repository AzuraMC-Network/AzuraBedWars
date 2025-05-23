package cc.azuramc.bedwars.spectator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 旁观者设置管理类
 * 管理旁观者的各种设置，包括速度、自动传送、夜视等
 *
 * @author an5w1r@163.com
 */
public class SpectatorSettings {

    private static final int THREAD_POOL_SIZE = 5;
    private static final String[] COLUMNS = {"Name", "speed", "autoTp", "nightVision", "firstPerson", "hideOther", "fly"};

    private static final int CORE_POOL_SIZE = THREAD_POOL_SIZE;
    private static final int MAX_POOL_SIZE = THREAD_POOL_SIZE;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 100;

    /**
     * 线程池和缓存
     */
    private static final ExecutorService FIXED_THREAD_POOL = new ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAX_POOL_SIZE,
        KEEP_ALIVE_TIME,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(QUEUE_CAPACITY),
        Executors.defaultThreadFactory(),
        new ThreadPoolExecutor.CallerRunsPolicy()
    );
    private static final Map<GamePlayer, SpectatorSettings> SPECTATOR_SETTINGS_HASH_MAP = new HashMap<>();

    private final GamePlayer gamePlayer;
    @Getter private int speed;
    private boolean autoTp;
    private boolean nightVision;
    private boolean firstPerson;
    private boolean hideOther;
    private boolean fly;

    /**
     * 构造函数
     * 
     * @param gamePlayer 游戏玩家
     */
    public SpectatorSettings(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
        loadSettings();
    }

    /**
     * 从数据库加载设置
     */
    private void loadSettings() {
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection()) {
            // 查询现有设置
            String selectQuery = String.format("SELECT * FROM %s WHERE %s=?", AzuraBedWars.SPECTATOR_SETTINGS_TABLE, COLUMNS[0]);
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                selectStmt.setString(1, gamePlayer.getName());
                try (ResultSet resultSet = selectStmt.executeQuery()) {
                    if (resultSet.next()) {
                        loadFromResultSet(resultSet);
                    } else {
                        createDefaultSettings(connection);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从结果集加载设置
     * 
     * @param resultSet 数据库结果集
     * @throws SQLException SQL异常
     */
    private void loadFromResultSet(ResultSet resultSet) throws SQLException {
        this.speed = resultSet.getInt(COLUMNS[1]);
        this.autoTp = resultSet.getBoolean(COLUMNS[2]);
        this.nightVision = resultSet.getBoolean(COLUMNS[3]);
        this.firstPerson = resultSet.getBoolean(COLUMNS[4]);
        this.hideOther = resultSet.getBoolean(COLUMNS[5]);
        this.fly = resultSet.getBoolean(COLUMNS[6]);
    }

    /**
     * 创建默认设置
     * 
     * @param connection 数据库连接
     * @throws SQLException SQL异常
     */
    private void createDefaultSettings(Connection connection) throws SQLException {
        String insertQuery = String.format(
            "INSERT INTO %s (%s,%s,%s,%s,%s,%s,%s) VALUES (?,0,0,0,1,0,0)",
                AzuraBedWars.SPECTATOR_SETTINGS_TABLE, COLUMNS[0], COLUMNS[1], COLUMNS[2], COLUMNS[3], COLUMNS[4], COLUMNS[5], COLUMNS[6]
        );
        
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setString(1, gamePlayer.getName());
            insertStmt.executeUpdate();
        }
        
        // 设置默认值
        this.speed = 0;
        this.autoTp = false;
        this.nightVision = false;
        this.firstPerson = true;
        this.hideOther = false;
        this.fly = false;
    }

    /**
     * 获取玩家的旁观者设置
     * 
     * @param player 游戏玩家
     * @return 旁观者设置
     */
    public static SpectatorSettings get(GamePlayer player) {
        return SPECTATOR_SETTINGS_HASH_MAP.computeIfAbsent(player, SpectatorSettings::new);
    }

    /**
     * 设置速度等级
     * 
     * @param level 速度等级
     */
    public void setSpeed(int level) {
        if (level < 0 || level > 4) {
            return;
        }
        speed = level;
        updateSetting(COLUMNS[1], level);
    }

    /**
     * 获取选项状态
     * 
     * @param option 选项
     * @return 是否启用
     */
    public boolean getOption(Option option) {
        return switch (option) {
            case AUTO_TP -> autoTp;
            case NIGHT_VISION -> nightVision;
            case FIRST_PERSON -> firstPerson;
            case HIDE_OTHER -> hideOther;
            case FLY -> fly;
        };
    }

    /**
     * 设置选项状态
     * 
     * @param option 选项
     * @param value 新值
     */
    public void setOption(Option option, boolean value) {
        if (getOption(option) == value) {
            return;
        }

        switch (option) {
            case AUTO_TP:
                autoTp = value;
                updateSetting(COLUMNS[2], value);
                break;
            case NIGHT_VISION:
                nightVision = value;
                updateSetting(COLUMNS[3], value);
                break;
            case FIRST_PERSON:
                firstPerson = value;
                updateSetting(COLUMNS[4], value);
                break;
            case HIDE_OTHER:
                hideOther = value;
                updateSetting(COLUMNS[5], value);
                break;
            case FLY:
                fly = value;
                updateSetting(COLUMNS[6], value);
                break;
            default: break;
        }
    }

    /**
     * 更新数据库中的设置
     * 
     * @param column 列名
     * @param value 新值
     */
    private void updateSetting(String column, Object value) {
        FIXED_THREAD_POOL.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection()) {
                String updateQuery = String.format("UPDATE %s SET %s=? WHERE %s=?", AzuraBedWars.SPECTATOR_SETTINGS_TABLE, column, COLUMNS[0]);
                try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                    if (value instanceof Boolean) {
                        stmt.setBoolean(1, (Boolean) value);
                    } else if (value instanceof Integer) {
                        stmt.setInt(1, (Integer) value);
                    }
                    stmt.setString(2, gamePlayer.getName());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 获取游戏玩家
     * 
     * @return 游戏玩家
     */
    public GamePlayer getPlayer() {
        return gamePlayer;
    }

    /**
     * 旁观者选项枚举
     */
    public enum Option {
        /** 自动传送至跟随的玩家 */
        AUTO_TP,
        NIGHT_VISION,
        FIRST_PERSON,
        HIDE_OTHER,
        FLY
    }
}
