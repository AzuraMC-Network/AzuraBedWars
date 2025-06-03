package cc.azuramc.bedwars.database.builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 建表语句构建器，用于构建CREATE TABLE语句
 * 示例: new CreateTableBuilder(conn).createTable("users")
 *      .column("id", "INT", "AUTO_INCREMENT", "PRIMARY KEY")
 *      .column("name", "VARCHAR(100)", "NOT NULL")
 *      .column("age", "INT")
 *      .prepare();
 * <p>
 * 或者使用DataType工具类:
 * 示例: new CreateTableBuilder(conn).createTable("users")
 *      .column("id", DataType.PK_INT())
 *      .column("name", DataType.VARCHAR(100), DataType.NOT_NULL())
 *      .column("age", DataType.INT())
 *      .prepare();
 * 
 * @author an5w1r@163.com
 */
public class CreateTableBuilder {
    private Connection connection;
    private String tableName;
    private Map<String, List<String>> columns = new LinkedHashMap<>();
    private List<String> primaryKeys = new ArrayList<>();
    private List<String> foreignKeys = new ArrayList<>();
    private List<String> uniqueKeys = new ArrayList<>();
    private List<String> indexes = new ArrayList<>();
    private boolean ifNotExists = false;
    private String engine = "InnoDB";
    private String charset = "utf8mb4";
    private String collate = "utf8mb4_general_ci";

    public CreateTableBuilder(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("数据库连接 (Connection) 不能为空。");
        }
        this.connection = connection;
    }

    public CreateTableBuilder createTable(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表名不能为空。");
        }
        this.tableName = tableName;
        return this;
    }

    public CreateTableBuilder ifNotExists() {
        this.ifNotExists = true;
        return this;
    }

    /**
     * 添加列定义
     * @param name 列名
     * @param type 列类型
     * @param attributes 列属性，如 NOT NULL, DEFAULT 等
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder column(String name, String type, String... attributes) {
        if (name == null || name.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("列名和类型不能为空。");
        }
        
        List<String> columnDefinition = new ArrayList<>();
        columnDefinition.add(type);
        
        if (attributes != null) {
            for (String attr : attributes) {
                if (attr != null && !attr.trim().isEmpty()) {
                    columnDefinition.add(attr);
                }
            }
        }
        
        this.columns.put(name, columnDefinition);
        return this;
    }
    
    /**
     * 快速添加主键自增整数ID列
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder addIdColumn() {
        return column("id", "INT", "AUTO_INCREMENT", "PRIMARY KEY");
    }
    
    /**
     * 快速添加创建时间列
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder addCreatedAtColumn() {
        return column("created_at", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP");
    }
    
    /**
     * 快速添加更新时间列
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder addUpdatedAtColumn() {
        return column("updated_at", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP", "ON UPDATE CURRENT_TIMESTAMP");
    }
    
    /**
     * 快速添加时间戳列（创建时间和更新时间）
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder addTimestamps() {
        addCreatedAtColumn();
        addUpdatedAtColumn();
        return this;
    }

    /**
     * 添加主键
     * @param columns 主键列名
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder primaryKey(String... columns) {
        if (columns != null) {
            StringBuilder pk = new StringBuilder("PRIMARY KEY (");
            boolean first = true;
            for (String column : columns) {
                if (column != null && !column.trim().isEmpty()) {
                    if (!first) {
                        pk.append(", ");
                    }
                    pk.append(column);
                    first = false;
                }
            }
            pk.append(")");
            if (!first) { // 确保至少有一列被添加
                this.primaryKeys.add(pk.toString());
            }
        }
        return this;
    }

    /**
     * 添加外键
     * @param column 当前表的列名
     * @param refTable 引用的表名
     * @param refColumn 引用的列名
     * @param onDelete ON DELETE行为
     * @param onUpdate ON UPDATE行为
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder foreignKey(String column, String refTable, String refColumn, 
                                         String onDelete, String onUpdate) {
        if (column == null || column.trim().isEmpty() || 
            refTable == null || refTable.trim().isEmpty() || 
            refColumn == null || refColumn.trim().isEmpty()) {
            throw new IllegalArgumentException("列名、引用表名和引用列名不能为空。");
        }
        
        StringBuilder fk = new StringBuilder("FOREIGN KEY (")
                .append(column)
                .append(") REFERENCES ")
                .append(refTable)
                .append(" (")
                .append(refColumn)
                .append(")");
        
        if (onDelete != null && !onDelete.trim().isEmpty()) {
            fk.append(" ON DELETE ").append(onDelete);
        }
        
        if (onUpdate != null && !onUpdate.trim().isEmpty()) {
            fk.append(" ON UPDATE ").append(onUpdate);
        }
        
        this.foreignKeys.add(fk.toString());
        return this;
    }

    /**
     * 添加唯一键
     * @param name 唯一键名称
     * @param columns 唯一键列名
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder uniqueKey(String name, String... columns) {
        if (columns != null && columns.length > 0) {
            StringBuilder uk = new StringBuilder("UNIQUE KEY ");
            uk.append(name).append(" (");
            
            boolean first = true;
            for (String column : columns) {
                if (column != null && !column.trim().isEmpty()) {
                    if (!first) {
                        uk.append(", ");
                    }
                    uk.append(column);
                    first = false;
                }
            }
            uk.append(")");
            
            if (!first) { // 确保至少有一列被添加
                this.uniqueKeys.add(uk.toString());
            }
        }
        return this;
    }

    /**
     * 添加索引
     * @param name 索引名称
     * @param columns 索引列名
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder index(String name, String... columns) {
        if (columns != null && columns.length > 0) {
            StringBuilder idx = new StringBuilder("INDEX ");
            idx.append(name).append(" (");
            
            boolean first = true;
            for (String column : columns) {
                if (column != null && !column.trim().isEmpty()) {
                    if (!first) {
                        idx.append(", ");
                    }
                    idx.append(column);
                    first = false;
                }
            }
            idx.append(")");
            
            if (!first) { // 确保至少有一列被添加
                this.indexes.add(idx.toString());
            }
        }
        return this;
    }

    /**
     * 设置表引擎
     * @param engine 引擎名称，如 InnoDB, MyISAM 等
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder engine(String engine) {
        if (engine != null && !engine.trim().isEmpty()) {
            this.engine = engine;
        }
        return this;
    }

    /**
     * 设置表字符集
     * @param charset 字符集，如 utf8, utf8mb4 等
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder charset(String charset) {
        if (charset != null && !charset.trim().isEmpty()) {
            this.charset = charset;
        }
        return this;
    }

    /**
     * 设置表排序规则
     * @param collate 排序规则，如 utf8_general_ci, utf8mb4_unicode_ci 等
     * @return CreateTableBuilder实例
     */
    public CreateTableBuilder collate(String collate) {
        if (collate != null && !collate.trim().isEmpty()) {
            this.collate = collate;
        }
        return this;
    }

    public PreparedStatement prepare() throws SQLException {
        if (this.tableName == null) {
            throw new IllegalStateException("必须先调用 createTable() 指定表名。");
        }
        if (this.columns.isEmpty()) {
            throw new IllegalStateException("必须至少添加一列。");
        }

        StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE ");
        
        if (this.ifNotExists) {
            sqlBuilder.append("IF NOT EXISTS ");
        }
        
        sqlBuilder.append(this.tableName).append(" (\n");
        
        // 添加列定义
        List<String> columnDefinitions = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : this.columns.entrySet()) {
            StringBuilder colDef = new StringBuilder("  ").append(entry.getKey()).append(" ");
            colDef.append(String.join(" ", entry.getValue()));
            columnDefinitions.add(colDef.toString());
        }
        
        // 添加主键、外键、唯一键和索引
        columnDefinitions.addAll(this.primaryKeys.stream().map(pk -> "  " + pk).toList());
        columnDefinitions.addAll(this.foreignKeys.stream().map(fk -> "  " + fk).toList());
        columnDefinitions.addAll(this.uniqueKeys.stream().map(uk -> "  " + uk).toList());
        columnDefinitions.addAll(this.indexes.stream().map(idx -> "  " + idx).toList());
        
        sqlBuilder.append(String.join(",\n", columnDefinitions));
        sqlBuilder.append("\n) ENGINE=").append(this.engine);
        
        if (this.charset != null) {
            sqlBuilder.append(" DEFAULT CHARSET=").append(this.charset);
        }
        
        if (this.collate != null) {
            sqlBuilder.append(" COLLATE=").append(this.collate);
        }
        
        String finalSql = sqlBuilder.toString();
        
        return this.connection.prepareStatement(finalSql);
    }
} 