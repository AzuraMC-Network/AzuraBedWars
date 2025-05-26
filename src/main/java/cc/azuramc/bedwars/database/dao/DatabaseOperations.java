package cc.azuramc.bedwars.database.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

/**
 * 数据库操作接口
 * 提供通用的数据库操作方法
 * 
 * @author an5w1r@163.com
 */
public interface DatabaseOperations {
    
    /**
     * 执行查询操作
     * 
     * @param sql SQL查询语句
     * @param params 查询参数
     * @param resultMapper 结果映射函数
     * @param <T> 返回类型
     * @return 查询结果
     * @throws SQLException SQL异常
     */
    <T> T executeQuery(String sql, Object[] params, Function<ResultSet, T> resultMapper) throws SQLException;
    
    /**
     * 执行更新操作
     * 
     * @param sql SQL更新语句
     * @param params 更新参数
     * @return 受影响的行数
     * @throws SQLException SQL异常
     */
    int executeUpdate(String sql, Object[] params) throws SQLException;
    
    /**
     * 执行批量更新操作
     * 
     * @param sql SQL更新语句
     * @param paramsList 批量参数列表
     * @return 受影响的行数数组
     * @throws SQLException SQL异常
     */
    int[] executeBatch(String sql, List<Object[]> paramsList) throws SQLException;
    
    /**
     * 检查记录是否存在
     * 
     * @param sql SQL查询语句
     * @param params 查询参数
     * @return 是否存在
     * @throws SQLException SQL异常
     */
    boolean exists(String sql, Object[] params) throws SQLException;
    
    /**
     * 获取数据库连接
     * 
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    Connection getConnection() throws SQLException;
} 