package cc.azuramc.bedwars.database.builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 插入语句构建器，用于构建INSERT语句
 * 示例: new InsertBuilder(conn).insertInto("users").values("name", "张三").values("age", 25).prepare();
 * @author an5w1r@163.com
 */
public class InsertBuilder {
    private Connection connection;
    private String tableName;
    private Map<String, Object> columnValues = new LinkedHashMap<>();
    private boolean returnGeneratedKeys = false;
    private List<List<Object>> batchValues = new ArrayList<>();
    private List<String> columns = new ArrayList<>();

    public InsertBuilder(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接 (Connection) 不能为空。");
        }
        this.connection = connection;
    }

    public InsertBuilder insertInto(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表名不能为空。");
        }
        this.tableName = tableName;
        return this;
    }

    public InsertBuilder values(String column, Object value) {
        if (column == null || column.trim().isEmpty()) {
            throw new IllegalArgumentException("列名不能为空。");
        }
        this.columnValues.put(column, value);
        return this;
    }

    public InsertBuilder returnGeneratedKeys() {
        this.returnGeneratedKeys = true;
        return this;
    }

    /**
     * 添加一批要插入的值，用于批量插入
     * 注意：必须先调用columns()方法设置列名
     * @param values 要插入的值，顺序必须与columns()方法中设置的列名顺序一致
     * @return InsertBuilder实例
     */
    public InsertBuilder addBatch(Object... values) {
        if (this.columns.isEmpty()) {
            throw new IllegalStateException("必须先调用columns()方法设置列名");
        }
        if (values.length != this.columns.size()) {
            throw new IllegalArgumentException("值的数量必须与列的数量相同");
        }
        List<Object> batch = new ArrayList<>();
        Collections.addAll(batch, values);
        this.batchValues.add(batch);
        return this;
    }

    /**
     * 设置要插入的列名，用于批量插入
     * @param columns 要插入的列名
     * @return InsertBuilder实例
     */
    public InsertBuilder columns(String... columns) {
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("列名不能为空。");
        }
        this.columns.clear();
        for (String column : columns) {
            if (column != null && !column.trim().isEmpty()) {
                this.columns.add(column);
            }
        }
        return this;
    }

    public PreparedStatement prepare() throws SQLException {
        if (this.tableName == null) {
            throw new IllegalStateException("必须先调用 insertInto() 指定表名。");
        }

        PreparedStatement pstmt;
        
        // 批量插入
        if (!this.batchValues.isEmpty()) {
            if (this.columns.isEmpty()) {
                throw new IllegalStateException("必须先调用columns()方法设置列名。");
            }
            
            StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
            sqlBuilder.append(this.tableName).append(" (");
            sqlBuilder.append(String.join(", ", this.columns));
            sqlBuilder.append(") VALUES (");
            
            String placeholders = this.columns.stream()
                    .map(c -> "?")
                    .collect(Collectors.joining(", "));
            sqlBuilder.append(placeholders).append(")");
            
            String finalSql = sqlBuilder.toString();
            
            pstmt = this.returnGeneratedKeys ? 
                    this.connection.prepareStatement(finalSql, PreparedStatement.RETURN_GENERATED_KEYS) : 
                    this.connection.prepareStatement(finalSql);
            
            for (List<Object> batch : this.batchValues) {
                for (int i = 0; i < batch.size(); i++) {
                    pstmt.setObject(i + 1, batch.get(i));
                }
                pstmt.addBatch();
            }
        } 
        // 单条插入
        else {
            if (this.columnValues.isEmpty()) {
                throw new IllegalStateException("必须至少调用一次 values() 方法来指定要插入的值");
            }
            
            StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
            sqlBuilder.append(this.tableName).append(" (");
            
            String columnNames = String.join(", ", this.columnValues.keySet());
            sqlBuilder.append(columnNames).append(") VALUES (");
            
            String placeholders = this.columnValues.values().stream()
                    .map(v -> "?")
                    .collect(Collectors.joining(", "));
            sqlBuilder.append(placeholders).append(")");
            
            String finalSql = sqlBuilder.toString();
            
            pstmt = this.returnGeneratedKeys ? 
                    this.connection.prepareStatement(finalSql, PreparedStatement.RETURN_GENERATED_KEYS) : 
                    this.connection.prepareStatement(finalSql);
            
            List<Object> parameters = new ArrayList<>(this.columnValues.values());
            try {
                for (int i = 0; i < parameters.size(); i++) {
                    pstmt.setObject(i + 1, parameters.get(i));
                }
            } catch (SQLException e) {
                try { pstmt.close(); } catch (SQLException closeEx) { e.addSuppressed(closeEx); }
                throw e;
            }
        }
        
        return pstmt;
    }
} 