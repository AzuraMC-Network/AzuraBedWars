package cc.azuramc.bedwars.database.query;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SQL查询构建器
 * 用于动态构建SQL语句，避免直接拼接字符串容易出错的问题
 * 
 * @author an5w1r@163.com
 */
public class QueryBuilder {
    private String table;
    private final List<String> columns = new ArrayList<>();
    private final List<String> values = new ArrayList<>();
    private final Map<String, Object> updateValues = new HashMap<>();
    private final List<String> whereConditions = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();
    private final List<String> orderBy = new ArrayList<>();
    private Integer limit;
    private Integer offset;
    
    /**
     * 设置查询的表名
     * 
     * @param table 表名
     * @return 当前构建器实例
     */
    public QueryBuilder table(String table) {
        this.table = table;
        return this;
    }
    
    /**
     * 添加要查询的列
     * 
     * @param columns 列名
     * @return 当前构建器实例
     */
    public QueryBuilder select(String... columns) {
        if (columns.length == 0) {
            this.columns.add("*");
        } else {
            for (String column : columns) {
                this.columns.add(column);
            }
        }
        return this;
    }
    
    /**
     * 添加要插入的列和值
     * 
     * @param column 列名
     * @param value 值
     * @return 当前构建器实例
     */
    public QueryBuilder insert(String column, Object value) {
        this.columns.add(column);
        this.values.add("?");
        this.parameters.add(value);
        return this;
    }
    
    /**
     * 添加要更新的列和值
     * 
     * @param column 列名
     * @param value 值
     * @return 当前构建器实例
     */
    public QueryBuilder update(String column, Object value) {
        this.updateValues.put(column, value);
        this.parameters.add(value);
        return this;
    }
    
    /**
     * 添加WHERE条件
     * 
     * @param condition 条件语句（使用?作为参数占位符）
     * @param params 条件参数
     * @return 当前构建器实例
     */
    public QueryBuilder where(String condition, Object... params) {
        this.whereConditions.add(condition);
        Collections.addAll(this.parameters, params);
        return this;
    }
    
    /**
     * 添加ORDER BY子句
     * 
     * @param column 列名
     * @param ascending 是否升序
     * @return 当前构建器实例
     */
    public QueryBuilder orderBy(String column, boolean ascending) {
        this.orderBy.add(column + (ascending ? " ASC" : " DESC"));
        return this;
    }
    
    /**
     * 设置LIMIT子句
     * 
     * @param limit 限制数量
     * @return 当前构建器实例
     */
    public QueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }
    
    /**
     * 设置OFFSET子句
     * 
     * @param offset 偏移量
     * @return 当前构建器实例
     */
    public QueryBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }
    
    /**
     * 构建SELECT查询语句
     * 
     * @return SQL查询语句
     */
    public String buildSelectQuery() {
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(String.join(", ", columns));
        query.append(" FROM ").append(table);
        
        if (!whereConditions.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }
        
        if (!orderBy.isEmpty()) {
            query.append(" ORDER BY ").append(String.join(", ", orderBy));
        }
        
        if (limit != null) {
            query.append(" LIMIT ").append(limit);
        }
        
        if (offset != null) {
            query.append(" OFFSET ").append(offset);
        }
        
        return query.toString();
    }
    
    /**
     * 构建INSERT查询语句
     * 
     * @return SQL查询语句
     */
    public String buildInsertQuery() {
        return "INSERT INTO " + table + " (" +
                String.join(", ", columns) +
                ") VALUES (" +
                String.join(", ", values) +
                ")";
    }
    
    /**
     * 构建UPDATE查询语句
     * 
     * @return SQL查询语句
     */
    public String buildUpdateQuery() {
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(table).append(" SET ");
        
        String setClause = updateValues.keySet().stream()
                .map(column -> column + " = ?")
                .collect(Collectors.joining(", "));
        query.append(setClause);
        
        if (!whereConditions.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }
        
        return query.toString();
    }
    
    /**
     * 构建DELETE查询语句
     * 
     * @return SQL查询语句
     */
    public String buildDeleteQuery() {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append(table);
        
        if (!whereConditions.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }
        
        return query.toString();
    }
    
    /**
     * 构建CREATE TABLE查询语句
     * 
     * @param tableDefinition 表定义（列定义和约束）
     * @return SQL查询语句
     */
    public String buildCreateTableQuery(String tableDefinition) {
        return "CREATE TABLE IF NOT EXISTS " + table + " (" + tableDefinition + ")";
    }
    
    /**
     * 获取查询参数列表
     * 
     * @return 参数列表
     */
    public List<Object> getParameters() {
        return parameters;
    }
    
    /**
     * 获取参数数组
     * 
     * @return 参数数组
     */
    public Object[] getParametersArray() {
        return parameters.toArray();
    }
} 