package cc.azuramc.bedwars.database.builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询语句构建器，用于构建SELECT语句
 * 示例: new SelectBuilder(conn).select("id", "name").from("users").where("age", ">", 18).orderBy("id", "DESC").limit(10).prepare();
 * @author an5w1r@163.com
 */
public class SelectBuilder implements QueryBuilder {
    private Connection connection;
    private String[] columns;
    private String tableName;
    private List<WhereCondition> whereConditions = new ArrayList<>();
    private List<String> groupByColumns = new ArrayList<>();
    private List<String> havingConditions = new ArrayList<>();
    private List<Object> havingValues = new ArrayList<>();
    private List<String> orderByColumns = new ArrayList<>();
    private List<String> orderByDirections = new ArrayList<>();
    private Integer limit;
    private Integer offset;
    private List<JoinClause> joins = new ArrayList<>();

    /** 内部类用于表示JOIN子句 */
    private static class JoinClause {
        String type;
        String table;
        String onCondition;

        JoinClause(String type, String table, String onCondition) {
            this.type = type;
            this.table = table;
            this.onCondition = onCondition;
        }

        String getSqlFragment() {
            return type + " JOIN " + table + " ON " + onCondition;
        }
    }

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

        String getLogicalOperator() {
            return logicalOperator;
        }

        String getColumn() {
            return column;
        }

        String getOperator() {
            return operator;
        }

        Object getValue() {
            return value;
        }
    }

    public SelectBuilder(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接 (Connection) 不能为空。");
        }
        this.connection = connection;
    }

    public SelectBuilder select(String... columns) {
        if (columns == null || columns.length == 0) {
            this.columns = new String[]{"*"};
        } else {
            this.columns = columns;
        }
        return this;
    }

    public SelectBuilder from(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表名不能为空。");
        }
        this.tableName = tableName;
        return this;
    }

    public SelectBuilder where(String column, String operator, Object value) {
        return where(column, operator, value, "AND");
    }

    public SelectBuilder where(String column, String operator, Object value, String logicalOperator) {
        if (column == null || column.trim().isEmpty() || operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("WHERE 子句的列名和操作符不能为空。");
        }
        this.whereConditions.add(new WhereCondition(column, operator, value, logicalOperator));
        return this;
    }

    public SelectBuilder whereEquals(String column, Object value) {
        return where(column, "=", value);
    }

    public SelectBuilder orWhere(String column, String operator, Object value) {
        return where(column, operator, value, "OR");
    }

    public SelectBuilder orWhereEquals(String column, Object value) {
        return orWhere(column, "=", value);
    }

    public SelectBuilder groupBy(String... columns) {
        if (columns != null) {
            for (String column : columns) {
                if (column != null && !column.trim().isEmpty()) {
                    this.groupByColumns.add(column);
                }
            }
        }
        return this;
    }
    
    public SelectBuilder having(String condition, String operator, Object value) {
        if (condition == null || condition.trim().isEmpty() || operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("HAVING 子句的条件和操作符不能为空。");
        }
        this.havingConditions.add(condition + " " + operator + " ?");
        this.havingValues.add(value);
        return this;
    }

    public SelectBuilder orderBy(String column, String direction) {
        if (column != null && !column.trim().isEmpty()) {
            this.orderByColumns.add(column);
            this.orderByDirections.add(direction != null && 
                    (direction.equalsIgnoreCase("DESC") || 
                     direction.equalsIgnoreCase("DESCENDING")) ? "DESC" : "ASC");
        }
        return this;
    }

    public SelectBuilder orderBy(String column) {
        return orderBy(column, "ASC");
    }

    public SelectBuilder limit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("LIMIT 值不能为负数。");
        }
        this.limit = limit;
        return this;
    }

    public SelectBuilder offset(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("OFFSET 值不能为负数。");
        }
        this.offset = offset;
        return this;
    }

    public SelectBuilder join(String table, String onCondition) {
        return join("INNER", table, onCondition);
    }

    public SelectBuilder leftJoin(String table, String onCondition) {
        return join("LEFT", table, onCondition);
    }

    public SelectBuilder rightJoin(String table, String onCondition) {
        return join("RIGHT", table, onCondition);
    }

    private SelectBuilder join(String type, String table, String onCondition) {
        if (table == null || table.trim().isEmpty() || onCondition == null || onCondition.trim().isEmpty()) {
            throw new IllegalArgumentException("JOIN 子句的表名和条件不能为空。");
        }
        this.joins.add(new JoinClause(type, table, onCondition));
        return this;
    }

    @Override
    public PreparedStatement prepare() throws SQLException {
        if (this.columns == null) {
            throw new IllegalStateException("必须先调用 select() 指定要查询的列。");
        }
        if (this.tableName == null) {
            throw new IllegalStateException("必须调用 from() 指定表名。");
        }

        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
        
        // SELECT 子句
        sqlBuilder.append(String.join(", ", this.columns));
        
        // FROM 子句
        sqlBuilder.append(" FROM ").append(this.tableName);
        
        // JOIN 子句
        if (!this.joins.isEmpty()) {
            for (JoinClause join : this.joins) {
                sqlBuilder.append(" ").append(join.getSqlFragment());
            }
        }

        List<Object> parameters = new ArrayList<>();
        
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
        
        // GROUP BY 子句
        if (!this.groupByColumns.isEmpty()) {
            sqlBuilder.append(" GROUP BY ").append(String.join(", ", this.groupByColumns));
            
            // HAVING 子句
            if (!this.havingConditions.isEmpty()) {
                sqlBuilder.append(" HAVING ").append(String.join(" AND ", this.havingConditions));
                parameters.addAll(this.havingValues);
            }
        }
        
        // ORDER BY 子句
        if (!this.orderByColumns.isEmpty()) {
            sqlBuilder.append(" ORDER BY ");
            List<String> orderClauses = new ArrayList<>();
            for (int i = 0; i < this.orderByColumns.size(); i++) {
                orderClauses.add(this.orderByColumns.get(i) + " " + this.orderByDirections.get(i));
            }
            sqlBuilder.append(String.join(", ", orderClauses));
        }
        
        // LIMIT 和 OFFSET 子句
        if (this.limit != null) {
            sqlBuilder.append(" LIMIT ?");
            parameters.add(this.limit);
            
            if (this.offset != null) {
                sqlBuilder.append(" OFFSET ?");
                parameters.add(this.offset);
            }
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
    
    @Override
    public String toSql() {
        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
        
        // SELECT 子句
        sqlBuilder.append(String.join(", ", this.columns));
        
        // FROM 子句
        sqlBuilder.append(" FROM ").append(this.tableName);
        
        // JOIN 子句
        if (!this.joins.isEmpty()) {
            for (JoinClause join : this.joins) {
                sqlBuilder.append(" ").append(join.getSqlFragment());
            }
        }
        
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
        
        // GROUP BY 子句
        if (!this.groupByColumns.isEmpty()) {
            sqlBuilder.append(" GROUP BY ").append(String.join(", ", this.groupByColumns));
            
            // HAVING 子句
            if (!this.havingConditions.isEmpty()) {
                sqlBuilder.append(" HAVING ").append(String.join(" AND ", this.havingConditions));
            }
        }
        
        // ORDER BY 子句
        if (!this.orderByColumns.isEmpty()) {
            sqlBuilder.append(" ORDER BY ");
            List<String> orderClauses = new ArrayList<>();
            for (int i = 0; i < this.orderByColumns.size(); i++) {
                orderClauses.add(this.orderByColumns.get(i) + " " + this.orderByDirections.get(i));
            }
            sqlBuilder.append(String.join(", ", orderClauses));
        }
        
        // LIMIT 和 OFFSET 子句
        if (this.limit != null) {
            sqlBuilder.append(" LIMIT ").append(this.limit);
            
            if (this.offset != null) {
                sqlBuilder.append(" OFFSET ").append(this.offset);
            }
        }
        
        return sqlBuilder.toString();
    }
} 