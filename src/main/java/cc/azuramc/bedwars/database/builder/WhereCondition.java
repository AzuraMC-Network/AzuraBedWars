package cc.azuramc.bedwars.database.builder;

import lombok.Getter;

/**
 * 表示SQL查询中的WHERE条件
 * @author an5w1r@163.com
 */
@Getter
public class WhereCondition {
    private String column;
    private String operator;
    private Object value;
    private String logicalOperator;

    /**
     * 创建一个WHERE条件
     * @param column 列名
     * @param operator 操作符，如 =, >, <, LIKE 等
     * @param value 值
     * @param logicalOperator 逻辑操作符，如 AND, OR
     */
    public WhereCondition(String column, String operator, Object value, String logicalOperator) {
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.logicalOperator = logicalOperator;
    }

    /**
     * 获取SQL片段
     * @return SQL片段
     */
    public String getSqlFragment() {
        return column + " " + operator + " ?";
    }
} 