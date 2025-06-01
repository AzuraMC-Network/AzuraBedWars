package cc.azuramc.bedwars.database.builder;

import lombok.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static cc.azuramc.bedwars.database.builder.DataType.*;
import static cc.azuramc.bedwars.database.builder.DataType.Type.*;
import static cc.azuramc.bedwars.database.builder.DataType.Constraint.*;

/**
 * SQL语句构建管理器，用于创建各种SQL语句的构建器
 * @author an5w1r@163.com
 */
@Data
public class PreparedStatementBuildManager {

    private Connection connection;
    private UpdateBuilder updateBuilder;
    private SelectBuilder selectBuilder;
    private InsertBuilder insertBuilder;
    private DeleteBuilder deleteBuilder;
    private CreateTableBuilder createTableBuilder;
    private boolean isDebug;

    public PreparedStatementBuildManager(Connection connection, boolean isDebug) {
        this.connection = connection;
        this.updateBuilder = new UpdateBuilder(connection);
        this.selectBuilder = new SelectBuilder(connection);
        this.insertBuilder = new InsertBuilder(connection);
        this.deleteBuilder = new DeleteBuilder(connection);
        this.createTableBuilder = new CreateTableBuilder(connection);
        this.isDebug = isDebug;
    }
    
    /**
     * 创建更新语句构建器
     * @param tableName 表名
     * @return 更新语句构建器
     */
    public UpdateBuilder update(String tableName) {
        return updateBuilder.update(tableName);
    }
    
    /**
     * 创建查询语句构建器
     * @param columns 要查询的列
     * @return 查询语句构建器
     */
    public SelectBuilder select(String... columns) {
        return selectBuilder.select(columns);
    }
    
    /**
     * 创建插入语句构建器
     * @param tableName 表名
     * @return 插入语句构建器
     */
    public InsertBuilder insertInto(String tableName) {
        return insertBuilder.insertInto(tableName);
    }
    
    /**
     * 创建删除语句构建器
     * @param tableName 表名
     * @return 删除语句构建器
     */
    public DeleteBuilder deleteFrom(String tableName) {
        return deleteBuilder.deleteFrom(tableName);
    }
    
    /**
     * 创建建表语句构建器
     * @param tableName 表名
     * @return 建表语句构建器
     */
    public CreateTableBuilder createTable(String tableName) {
        return createTableBuilder.createTable(tableName);
    }
    
    /**
     * 快速创建一个带有ID和时间戳的表
     * @param tableName 表名
     * @return 建表语句构建器
     */
    public CreateTableBuilder createStandardTable(String tableName) {
        return createTableBuilder.createTable(tableName)
                .ifNotExists()
                .column("id", PK_INT())
                .addCreatedAtColumn()
                .addUpdatedAtColumn();
    }
    
    /**
     * 快速创建一个用户表
     * @param tableName 表名
     * @return 建表语句构建器
     */
    public CreateTableBuilder createUserTable(String tableName) {
        return createTableBuilder.createTable(tableName)
                .ifNotExists()
                .column("id", PK_INT())
                .column("username", VARCHAR.size(50), NOT_NULL.getSql(), UNIQUE.getSql())
                .column("password", VARCHAR.size(255), NOT_NULL.getSql())
                .column("email", VARCHAR.size(100), NOT_NULL.getSql(), UNIQUE.getSql())
                .column("status", TINYINT.size(1), NOT_NULL.getSql(), DEFAULT(1))
                .column("created_at", TIMESTAMP_DEFAULT_CURRENT())
                .column("updated_at", TIMESTAMP_DEFAULT_CURRENT_ON_UPDATE());
    }
    
    /**
     * 快速创建一个带有外键关联的子表
     * @param tableName 子表名
     * @param parentTable 父表名
     * @param foreignKeyColumn 外键列名
     * @return 建表语句构建器
     */
    public CreateTableBuilder createChildTable(String tableName, String parentTable, String foreignKeyColumn) {
        return createTableBuilder.createTable(tableName)
                .ifNotExists()
                .column("id", PK_INT())
                .column(foreignKeyColumn, INT_NOT_NULL())
                .foreignKey(foreignKeyColumn, parentTable, "id", CASCADE.getSql(), CASCADE.getSql())
                .index("idx_" + foreignKeyColumn, foreignKeyColumn)
                .addCreatedAtColumn()
                .addUpdatedAtColumn();
    }
    
    /**
     * 执行PreparedStatement并关闭它
     * @param pstmt PreparedStatement对象
     * @return 受影响的行数
     * @throws SQLException 如果发生SQL异常
     */
    public int execute(PreparedStatement pstmt) throws SQLException {
        try {
            return pstmt.executeUpdate();
        } finally {
            pstmt.close();
        }
    }
    
    /**
     * 执行PreparedStatement并关闭它，忽略异常
     * @param pstmt PreparedStatement对象
     * @return 受影响的行数，如果发生异常则返回-1
     */
    public int executeQuietly(PreparedStatement pstmt) {
        try {
            int result = pstmt.executeUpdate();
            pstmt.close();
            return result;
        } catch (SQLException e) {
            if (isDebug) {
                e.printStackTrace();
            }
            try {
                pstmt.close();
            } catch (SQLException ex) {
                // 忽略关闭异常
            }
            return -1;
        }
    }
}
