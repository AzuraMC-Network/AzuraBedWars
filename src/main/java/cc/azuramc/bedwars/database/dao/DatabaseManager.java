package cc.azuramc.bedwars.database.dao;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.connection.ConnectionPoolHandler;
import cc.azuramc.bedwars.database.query.QueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据库操作管理器
 * 提供通用的数据库操作实现
 * 
 * @author an5w1r@163.com
 */
public class DatabaseManager implements DatabaseOperations {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    
    /**
     * 单例实例
     */
    private static DatabaseManager instance;
    
    /**
     * 获取DatabaseManager单例实例
     * 
     * @return DatabaseManager实例
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private DatabaseManager() {
    }
    
    @Override
    public <T> T executeQuery(String sql, Object[] params, Function<ResultSet, T> resultMapper) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = prepareStatement(connection, sql, params)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultMapper.apply(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "执行查询时出错: " + sql, e);
            throw e;
        }
    }
    
    @Override
    public int executeUpdate(String sql, Object[] params) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = prepareStatement(connection, sql, params)) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "执行更新时出错: " + sql, e);
            throw e;
        }
    }
    
    @Override
    public int[] executeBatch(String sql, List<Object[]> paramsList) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (Object[] params : paramsList) {
                setParameters(statement, params);
                statement.addBatch();
            }
            
            return statement.executeBatch();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "执行批处理时出错: " + sql, e);
            throw e;
        }
    }
    
    @Override
    public boolean exists(String sql, Object[] params) throws SQLException {
        return executeQuery(sql, params, resultSet -> {
            try {
                return resultSet.next();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "检查记录是否存在时出错", e);
                return false;
            }
        });
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        AzuraBedWars instance = AzuraBedWars.getInstance();
        if (instance == null) {
            throw new SQLException("无法获取AzuraBedWars实例");
        }
        
        ConnectionPoolHandler connectionPoolHandler = instance.getConnectionPoolHandler();
        if (connectionPoolHandler == null) {
            throw new SQLException("连接池处理器尚未初始化");
        }
        
        return connectionPoolHandler.getConnection();
    }
    
    /**
     * 使用QueryBuilder执行查询操作
     * 
     * @param queryBuilder 查询构建器
     * @param resultMapper 结果映射函数
     * @param <T> 返回类型
     * @return 查询结果
     * @throws SQLException SQL异常
     */
    public <T> T executeQuery(QueryBuilder queryBuilder, Function<ResultSet, T> resultMapper) throws SQLException {
        return executeQuery(queryBuilder.buildSelectQuery(), queryBuilder.getParametersArray(), resultMapper);
    }
    
    /**
     * 使用QueryBuilder执行插入操作
     * 
     * @param queryBuilder 查询构建器
     * @return 受影响的行数
     * @throws SQLException SQL异常
     */
    public int executeInsert(QueryBuilder queryBuilder) throws SQLException {
        return executeUpdate(queryBuilder.buildInsertQuery(), queryBuilder.getParametersArray());
    }
    
    /**
     * 使用QueryBuilder执行更新操作
     * 
     * @param queryBuilder 查询构建器
     * @return 受影响的行数
     * @throws SQLException SQL异常
     */
    public int executeUpdate(QueryBuilder queryBuilder) throws SQLException {
        return executeUpdate(queryBuilder.buildUpdateQuery(), queryBuilder.getParametersArray());
    }
    
    /**
     * 使用QueryBuilder执行删除操作
     * 
     * @param queryBuilder 查询构建器
     * @return 受影响的行数
     * @throws SQLException SQL异常
     */
    public int executeDelete(QueryBuilder queryBuilder) throws SQLException {
        return executeUpdate(queryBuilder.buildDeleteQuery(), queryBuilder.getParametersArray());
    }
    
    /**
     * 使用QueryBuilder检查记录是否存在
     * 
     * @param queryBuilder 查询构建器
     * @return 是否存在
     * @throws SQLException SQL异常
     */
    public boolean exists(QueryBuilder queryBuilder) throws SQLException {
        return exists(queryBuilder.buildSelectQuery(), queryBuilder.getParametersArray());
    }
    
    /**
     * 准备SQL语句并设置参数
     * 
     * @param connection 数据库连接
     * @param sql SQL语句
     * @param params 参数
     * @return 准备好的语句
     * @throws SQLException SQL异常
     */
    private PreparedStatement prepareStatement(Connection connection, String sql, Object[] params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        setParameters(statement, params);
        return statement;
    }
    
    /**
     * 设置预处理语句的参数
     * 
     * @param statement 预处理语句
     * @param params 参数
     * @throws SQLException SQL异常
     */
    private void setParameters(PreparedStatement statement, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
        }
    }
} 