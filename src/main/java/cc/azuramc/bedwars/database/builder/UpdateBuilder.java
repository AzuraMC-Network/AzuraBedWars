package cc.azuramc.bedwars.database.builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 更新语句构建器，用于构建UPDATE语句
 * 示例: new UpdateBuilder(conn).update("users").set("email", "new@example.com").where("id", "=", 1).prepare();
 *
 * @author an5w1r@163.com
 */
public class UpdateBuilder implements QueryBuilder {
    private String tableName;
    private Map<String, Object> setClauses = new LinkedHashMap<>();
    /** 存储WHERE条件 */
    private List<WhereCondition> whereConditions = new ArrayList<>();
    private Connection connection;

    public UpdateBuilder(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接 (Connection) 不能为空。");
        }
        this.connection = connection;
    }

    public UpdateBuilder update(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表名不能为空。");
        }
        this.tableName = tableName;
        return this;
    }

    public UpdateBuilder set(String column, Object value) {
        if (column == null || column.trim().isEmpty()) {
            throw new IllegalArgumentException("SET 子句中的列名不能为空。");
        }
        this.setClauses.put(column, value);
        return this;
    }

    /**
     * 添加一个 WHERE 条件。可以多次调用以添加多个条件 (默认用 AND 连接)。
     * @param column 列名
     * @param operator 操作符，例如 "=", ">", "<", "LIKE" 等
     * @param value  条件的值
     * @return Builder 自身，用于链式调用
     */
    public UpdateBuilder where(String column, String operator, Object value) {
        return where(column, operator, value, "AND");
    }

    /**
     * 添加一个 WHERE 条件，并指定逻辑操作符。
     * @param column 列名
     * @param operator 操作符，例如 "=", ">", "<", "LIKE" 等
     * @param value 条件的值
     * @param logicalOperator 逻辑操作符，例如 "AND", "OR"
     * @return Builder 自身，用于链式调用
     */
    public UpdateBuilder where(String column, String operator, Object value, String logicalOperator) {
        if (column == null || column.trim().isEmpty() || operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("WHERE 子句的列名和操作符不能为空。");
        }
        this.whereConditions.add(new WhereCondition(column, operator, value, logicalOperator));
        return this;
    }

    // 为了方便，可以为常用的 "=" 操作符提供一个重载方法
    public UpdateBuilder whereEquals(String column, Object value) {
        return where(column, "=", value);
    }

    /**
     * 添加一个使用 OR 连接的 WHERE 条件。
     * @param column 列名
     * @param operator 操作符
     * @param value 值
     * @return Builder 自身，用于链式调用
     */
    public UpdateBuilder orWhere(String column, String operator, Object value) {
        return where(column, operator, value, "OR");
    }

    /**
     * 添加一个使用 OR 连接的 WHERE 条件，使用 = 操作符。
     * @param column 列名
     * @param value 值
     * @return Builder 自身，用于链式调用
     */
    public UpdateBuilder orWhereEquals(String column, Object value) {
        return orWhere(column, "=", value);
    }

    @Override
    public PreparedStatement prepare() throws SQLException {
        if (this.tableName == null) {
            throw new IllegalStateException("必须先调用 update(tableName) 指定表名。");
        }
        if (this.setClauses.isEmpty()) {
            throw new IllegalStateException("必须至少调用一次 set() 方法来指定要更新的列。");
        }
        // 警告: 如果没有 WHERE 条件，将会更新所有行，这通常是危险的。
        // 实际应用中可能需要强制 WHERE 条件或有明确的配置允许无条件更新。
        if (this.whereConditions.isEmpty()) {
            System.err.println("警告 (UpdateBuilder): WHERE 子句为空，这将更新表中的所有行！");
            // throw new IllegalStateException("必须指定 WHERE 条件以防止更新所有行。"); // 或者更严格地抛出异常
        }

        StringBuilder sqlBuilder = new StringBuilder("UPDATE ");
        sqlBuilder.append(this.tableName).append(" SET ");

        List<Object> parameters = new ArrayList<>();

        // SET 子句
        String setAssignments = this.setClauses.entrySet().stream()
                .map(entry -> {
                    parameters.add(entry.getValue());
                    return entry.getKey() + " = ?";
                })
                .collect(Collectors.joining(", "));
        sqlBuilder.append(setAssignments);

        // WHERE 子句
        if (!this.whereConditions.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            boolean isFirst = true;
            for (WhereCondition condition : this.whereConditions) {
                if (!isFirst) {
                    sqlBuilder.append(" ").append(condition.getLogicalOperator()).append(" ");
                }
                sqlBuilder.append(condition.getSqlFragment());
                parameters.add(condition.getValue());
                isFirst = false;
            }
        }

        String finalSql = sqlBuilder.toString();
        // System.out.println("UpdateBuilder SQL: " + finalSql);
        // System.out.println("UpdateBuilder Params: " + parameters);

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

    @Override
    public String toSql() {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE ");
        sqlBuilder.append(this.tableName).append(" SET ");

        // SET 子句
        String setAssignments = this.setClauses.keySet().stream()
                .map(key -> key + " = ?")
                .collect(Collectors.joining(", "));
        sqlBuilder.append(setAssignments);

        // WHERE 子句
        if (!this.whereConditions.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            boolean isFirst = true;
            for (WhereCondition condition : this.whereConditions) {
                if (!isFirst) {
                    sqlBuilder.append(" ").append(condition.getLogicalOperator()).append(" ");
                }
                sqlBuilder.append(condition.getColumn()).append(" ").append(condition.getOperator()).append(" ?");
                isFirst = false;
            }
        }

        return sqlBuilder.toString();
    }
}