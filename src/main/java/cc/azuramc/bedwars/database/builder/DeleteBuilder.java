package cc.azuramc.bedwars.database.builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 删除语句构建器，用于构建DELETE语句
 * 示例: new DeleteBuilder(conn).deleteFrom("users").where("id", "=", 1).prepare();
 * @author an5w1r@163.com
 */
public class DeleteBuilder {
    private Connection connection;
    private String tableName;
    private List<WhereCondition> whereConditions = new ArrayList<>();
    private Integer limit;

    /** 内部类用于表示WHERE条件 */
    private static class WhereCondition {
        String column;
        String operator;
        Object value;
        String logicalOperator;

        WhereCondition(String column, String operator, Object value, String logicalOperator) {
            this.column = column;
            this.operator = operator;
            this.value = value;
            this.logicalOperator = logicalOperator;
        }

        String getSqlFragment() {
            return column + " " + operator + " ?";
        }
    }

    public DeleteBuilder(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接 (Connection) 不能为空。");
        }
        this.connection = connection;
    }

    public DeleteBuilder deleteFrom(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表名不能为空。");
        }
        this.tableName = tableName;
        return this;
    }

    public DeleteBuilder where(String column, String operator, Object value) {
        return where(column, operator, value, "AND");
    }

    public DeleteBuilder where(String column, String operator, Object value, String logicalOperator) {
        if (column == null || column.trim().isEmpty() || operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("WHERE 子句的列名和操作符不能为空。");
        }
        this.whereConditions.add(new WhereCondition(column, operator, value, logicalOperator));
        return this;
    }

    public DeleteBuilder whereEquals(String column, Object value) {
        return where(column, "=", value);
    }

    public DeleteBuilder orWhere(String column, String operator, Object value) {
        return where(column, operator, value, "OR");
    }

    public DeleteBuilder orWhereEquals(String column, Object value) {
        return orWhere(column, "=", value);
    }

    public DeleteBuilder limit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("LIMIT 值不能为负数。");
        }
        this.limit = limit;
        return this;
    }

    public PreparedStatement prepare() throws SQLException {
        if (this.tableName == null) {
            throw new IllegalStateException("必须先调用 deleteFrom() 指定表名。");
        }

        // 警告: 如果没有 WHERE 条件，将会删除所有行，这通常是危险的。
        if (this.whereConditions.isEmpty()) {
            System.err.println("警告 (DeleteBuilder): WHERE 子句为空，这将删除表中的所有行！");
        }

        StringBuilder sqlBuilder = new StringBuilder("DELETE FROM ");
        sqlBuilder.append(this.tableName);

        List<Object> parameters = new ArrayList<>();

        // WHERE 子句
        if (!this.whereConditions.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            boolean isFirst = true;
            for (WhereCondition condition : this.whereConditions) {
                if (!isFirst) {
                    sqlBuilder.append(" ").append(condition.logicalOperator).append(" ");
                }
                sqlBuilder.append(condition.getSqlFragment());
                parameters.add(condition.value);
                isFirst = false;
            }
        }

        // LIMIT 子句
        if (this.limit != null) {
            sqlBuilder.append(" LIMIT ?");
            parameters.add(this.limit);
        }

        String finalSql = sqlBuilder.toString();

        PreparedStatement pstmt = this.connection.prepareStatement(finalSql);
        try {
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
        } catch (SQLException e) {
            try { pstmt.close(); } catch (SQLException closeEx) { e.addSuppressed(closeEx); }
            throw e;
        }
        return pstmt;
    }
} 