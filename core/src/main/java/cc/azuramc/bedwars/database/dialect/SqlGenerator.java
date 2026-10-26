package cc.azuramc.bedwars.database.dialect;

import cc.azuramc.bedwars.database.mapper.ColumnMeta;
import cc.azuramc.bedwars.database.mapper.EntityMapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author an5w1r@163.com
 */
public class SqlGenerator {

    private final SqlDialect dialect;

    public SqlGenerator(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public static SqlGenerator mysql() {
        return new SqlGenerator(MySqlDialect.INSTANCE);
    }

    /**
     * 生成 CREATE TABLE SQL
     *
     * @param clazz   实体类
     * @param indexes 索引定义（可选），格式如 "INDEX idx_name (column_name)"
     */
    public String generateCreateTableSql(Class<?> clazz, String... indexes) {
        String tableName = dialect.wrapIdentifier(EntityMapper.getTableName(clazz));
        List<ColumnMeta> columns = EntityMapper.getColumnMetas(clazz);

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");

        if (dialect.supportsIfNotExists()) {
            sql.append("IF NOT EXISTS ");
        }

        sql.append(tableName).append(" (\n");

        // 列定义
        List<String> columnDefs = new ArrayList<>();
        for (ColumnMeta meta : columns) {
            columnDefs.add("    " + generateColumnDefinition(meta));
        }

        // 索引定义
        for (String index : indexes) {
            columnDefs.add("    " + index);
        }

        sql.append(String.join(",\n", columnDefs));
        sql.append("\n)");

        // 表选项
        String tableOptions = dialect.getTableOptions();
        if (!tableOptions.isEmpty()) {
            sql.append(" ").append(tableOptions);
        }

        return sql.toString();
    }

    /**
     * 生成列定义
     */
    private String generateColumnDefinition(ColumnMeta meta) {
        StringBuilder def = new StringBuilder();
        def.append(dialect.wrapIdentifier(meta.columnName())).append(" ");

        // 主键自增
        if (meta.primaryKey() && meta.autoIncrement()) {
            def.append(dialect.getPrimaryKeyAutoIncrement());
            return def.toString();
        }

        // 类型
        String sqlType = getSqlType(meta);
        def.append(sqlType);

        // 主键（非自增）
        if (meta.primaryKey()) {
            def.append(" PRIMARY KEY");
            return def.toString();
        }

        // NOT NULL
        if (!meta.nullable()) {
            def.append(" ").append(dialect.getNotNull());
        }

        // 默认值
        if (!meta.defaultValue().isEmpty()) {
            def.append(" DEFAULT ").append(meta.defaultValue());
        } else if (isTimestampType(meta) && "created_at".equals(meta.columnName())) {
            def.append(" ").append(dialect.getDefaultCurrentTimestamp());
        } else if (isTimestampType(meta) && "updated_at".equals(meta.columnName())) {
            def.append(" ").append(dialect.getDefaultCurrentTimestamp());
            String onUpdate = dialect.getOnUpdateCurrentTimestamp();
            if (!onUpdate.isEmpty()) {
                def.append(" ").append(onUpdate);
            }
        }

        return def.toString();
    }

    /**
     * 获取 SQL 类型
     */
    private String getSqlType(ColumnMeta meta) {
        // 优先使用注解指定的类型
        if (!meta.type().isEmpty()) {
            return meta.type();
        }

        Class<?> fieldType = meta.getFieldType();

        // 字符串类型
        if (fieldType == String.class) {
            if (meta.size() > 0) {
                return dialect.getVarcharType(meta.size());
            }
            return dialect.getTextType();
        }

        // 数值类型
        if (fieldType == int.class || fieldType == Integer.class) {
            return dialect.getIntType();
        }
        if (fieldType == long.class || fieldType == Long.class) {
            return "BIGINT";
        }
        if (fieldType == double.class || fieldType == Double.class) {
            return dialect.getDoubleType();
        }
        if (fieldType == float.class || fieldType == Float.class) {
            return "FLOAT";
        }
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            return dialect.getBooleanType();
        }

        // 时间类型
        if (fieldType == Timestamp.class || fieldType == java.util.Date.class) {
            return dialect.getTimestampType();
        }

        // UUID 存为字符串
        if (fieldType == UUID.class) {
            return dialect.getVarcharType(36);
        }

        // 枚举存为字符串
        if (fieldType.isEnum()) {
            int size = meta.size() > 0 ? meta.size() : 32;
            return dialect.getVarcharType(size);
        }

        // 默认
        return dialect.getTextType();
    }

    private boolean isTimestampType(ColumnMeta meta) {
        Class<?> fieldType = meta.getFieldType();
        return fieldType == Timestamp.class || fieldType == java.util.Date.class;
    }

    /**
     * 生成 INSERT SQL（排除主键自增列）
     */
    public String generateInsertSql(Class<?> clazz) {
        String tableName = dialect.wrapIdentifier(EntityMapper.getTableName(clazz));
        List<ColumnMeta> columns = EntityMapper.getNonPrimaryKeyColumns(clazz);

        String columnNames = columns.stream()
                .map(meta -> dialect.wrapIdentifier(meta.columnName()))
                .collect(Collectors.joining(", "));

        String placeholders = columns.stream()
                .map(m -> "?")
                .collect(Collectors.joining(", "));

        return "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";
    }

    /**
     * 生成 UPDATE SQL（仅包含可更新字段）
     */
    public String generateUpdateSql(Class<?> clazz) {
        String tableName = dialect.wrapIdentifier(EntityMapper.getTableName(clazz));
        List<ColumnMeta> updatableColumns = EntityMapper.getUpdatableColumns(clazz);
        ColumnMeta pkColumn = EntityMapper.getPrimaryKeyColumn(clazz);

        String setClause = updatableColumns.stream()
                .map(meta -> dialect.wrapIdentifier(meta.columnName()) + " = ?")
                .collect(Collectors.joining(", "));

        return "UPDATE " + tableName + " SET " + setClause +
                " WHERE " + dialect.wrapIdentifier(pkColumn.columnName()) + " = ?";
    }

    /**
     * 生成 SELECT ALL SQL
     */
    public String generateSelectAllSql(Class<?> clazz) {
        String tableName = dialect.wrapIdentifier(EntityMapper.getTableName(clazz));
        List<ColumnMeta> columns = EntityMapper.getColumnMetas(clazz);

        String columnNames = columns.stream()
                .map(meta -> dialect.wrapIdentifier(meta.columnName()))
                .collect(Collectors.joining(", "));

        return "SELECT " + columnNames + " FROM " + tableName;
    }

    /**
     * 生成带 WHERE 条件的 SELECT SQL（通过 @QueryField 的 key，单条件）
     */
    public String generateSelectByQueryKeySql(Class<?> clazz, String queryKey) {
        String columnName = EntityMapper.getQueryColumn(clazz, queryKey);
        return generateSelectAllSql(clazz) + " WHERE " + dialect.wrapIdentifier(columnName) + " = ?";
    }

    /**
     * 生成带 WHERE 条件的 SELECT SQL（通过 @QueryField 的 key，多条件 AND）
     */
    public String generateSelectByQueryKeysSql(Class<?> clazz, String... queryKeys) {
        if (queryKeys.length == 0) {
            return generateSelectAllSql(clazz);
        }
        if (queryKeys.length == 1) {
            return generateSelectByQueryKeySql(clazz, queryKeys[0]);
        }

        StringBuilder where = new StringBuilder(" WHERE ");
        for (int i = 0; i < queryKeys.length; i++) {
            if (i > 0) {
                where.append(" AND ");
            }
            String columnName = EntityMapper.getQueryColumn(clazz, queryKeys[i]);
            where.append(dialect.wrapIdentifier(columnName)).append(" = ?");
        }
        return generateSelectAllSql(clazz) + where;
    }

    /**
     * 生成带 LIMIT 的 SELECT SQL
     */
    public String generateSelectWithLimitSql(Class<?> clazz, int limit) {
        return generateSelectAllSql(clazz) + " " + dialect.getLimitClause(limit);
    }

    /**
     * 生成 EXISTS 检查 SQL
     */
    public String generateExistsSql(Class<?> clazz) {
        String tableName = dialect.wrapIdentifier(EntityMapper.getTableName(clazz));
        return "SELECT 1 FROM " + tableName + " " + dialect.getLimitClause(1);
    }

    /**
     * 生成不带主键的 UPDATE SQL（用于没有主键的表，如 database_version）
     */
    public String generateUpdateAllSql(Class<?> clazz) {
        String tableName = dialect.wrapIdentifier(EntityMapper.getTableName(clazz));
        List<ColumnMeta> columns = EntityMapper.getColumnMetas(clazz);

        String setClause = columns.stream()
                .map(meta -> dialect.wrapIdentifier(meta.columnName()) + " = ?")
                .collect(Collectors.joining(", "));

        return "UPDATE " + tableName + " SET " + setClause;
    }
}
