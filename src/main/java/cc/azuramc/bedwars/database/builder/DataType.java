package cc.azuramc.bedwars.database.builder;

import lombok.Getter;

/**
 * SQL数据类型和约束条件的枚举类
 * @author an5w1r@163.com
 */
public class DataType {

    /**
     * SQL数据类型枚举
     */
    @Getter
    public enum Type {
        // 整数类型
        INT("INT"),
        TINYINT("TINYINT"),
        SMALLINT("SMALLINT"),
        MEDIUMINT("MEDIUMINT"),
        BIGINT("BIGINT"),
        
        // 浮点数类型
        FLOAT("FLOAT"),
        DOUBLE("DOUBLE"),
        DECIMAL("DECIMAL"),
        
        // 字符串类型
        CHAR("CHAR"),
        VARCHAR("VARCHAR"),
        TEXT("TEXT"),
        TINYTEXT("TINYTEXT"),
        MEDIUMTEXT("MEDIUMTEXT"),
        LONGTEXT("LONGTEXT"),
        
        // 日期和时间类型
        DATE("DATE"),
        TIME("TIME"),
        DATETIME("DATETIME"),
        TIMESTAMP("TIMESTAMP"),
        YEAR("YEAR"),
        
        // 二进制类型
        BINARY("BINARY"),
        VARBINARY("VARBINARY"),
        BLOB("BLOB"),
        TINYBLOB("TINYBLOB"),
        MEDIUMBLOB("MEDIUMBLOB"),
        LONGBLOB("LONGBLOB"),
        
        // 其他类型
        ENUM("ENUM"),
        SET("SET"),
        JSON("JSON");
        
        private final String sql;
        
        Type(String sql) {
            this.sql = sql;
        }

        /**
         * 添加大小参数，如VARCHAR(255)
         * @param size 大小参数
         * @return 带大小参数的SQL类型
         */
        public String size(int size) {
            return sql + "(" + size + ")";
        }
        
        /**
         * 添加精度和小数位数，如DECIMAL(10,2)
         * @param precision 精度
         * @param scale 小数位数
         * @return 带精度和小数位数的SQL类型
         */
        public String precision(int precision, int scale) {
            return sql + "(" + precision + "," + scale + ")";
        }
        
        /**
         * 创建ENUM类型
         * @param values 枚举值
         * @return ENUM类型SQL
         */
        public String values(String... values) {
            if (this != ENUM && this != SET) {
                throw new IllegalStateException("只有ENUM和SET类型可以使用values方法");
            }
            
            StringBuilder sb = new StringBuilder(sql).append("(");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("'").append(values[i]).append("'");
            }
            sb.append(")");
            return sb.toString();
        }
    }
    
    /**
     * SQL约束条件枚举
     */
    @Getter
    public enum Constraint {
        // 基本约束
        NOT_NULL("NOT NULL"),
        NULL("NULL"),
        AUTO_INCREMENT("AUTO_INCREMENT"),
        PRIMARY_KEY("PRIMARY KEY"),
        UNIQUE("UNIQUE"),
        UNSIGNED("UNSIGNED"),
        ZEROFILL("ZEROFILL"),
        
        // 外键操作
        CASCADE("CASCADE"),
        RESTRICT("RESTRICT"),
        SET_NULL("SET NULL"),
        NO_ACTION("NO ACTION"),
        
        // 默认值约束
        DEFAULT_NULL("DEFAULT NULL"),
        DEFAULT_CURRENT_TIMESTAMP("DEFAULT CURRENT_TIMESTAMP"),
        ON_UPDATE_CURRENT_TIMESTAMP("ON UPDATE CURRENT_TIMESTAMP");
        
        private final String sql;
        
        Constraint(String sql) {
            this.sql = sql;
        }

    }
    
    /**
     * 创建DEFAULT约束
     * @param value 默认值
     * @return DEFAULT约束SQL
     */
    public static String DEFAULT(String value) {
        return "DEFAULT '" + value + "'";
    }
    
    /**
     * 创建DEFAULT约束
     * @param value 默认值
     * @return DEFAULT约束SQL
     */
    public static String DEFAULT(int value) {
        return "DEFAULT " + value;
    }
    
    /**
     * 创建DEFAULT约束
     * @param value 默认值
     * @return DEFAULT约束SQL
     */
    public static String DEFAULT(double value) {
        return "DEFAULT " + value;
    }
    
    /**
     * 创建COMMENT约束
     * @param comment 注释内容
     * @return COMMENT约束SQL
     */
    public static String COMMENT(String comment) {
        return "COMMENT '" + comment + "'";
    }
    
    // 常用组合
    
    /**
     * 主键自增整数
     * @return 主键自增整数SQL
     */
    public static String PK_INT() {
        return Type.INT.getSql() + " " + Constraint.AUTO_INCREMENT.getSql() + " " + Constraint.PRIMARY_KEY.getSql();
    }
    
    /**
     * 主键自增大整数
     * @return 主键自增大整数SQL
     */
    public static String PK_BIGINT() {
        return Type.BIGINT.getSql() + " " + Constraint.AUTO_INCREMENT.getSql() + " " + Constraint.PRIMARY_KEY.getSql();
    }
    
    /**
     * 可为空的VARCHAR
     * @param size 大小
     * @return 可为空的VARCHAR SQL
     */
    public static String VARCHAR_NULL(int size) {
        return Type.VARCHAR.size(size) + " " + Constraint.NULL.getSql();
    }
    
    /**
     * 不可为空的VARCHAR
     * @param size 大小
     * @return 不可为空的VARCHAR SQL
     */
    public static String VARCHAR_NOT_NULL(int size) {
        return Type.VARCHAR.size(size) + " " + Constraint.NOT_NULL.getSql();
    }
    
    /**
     * 不可为空的INT
     * @return 不可为空的INT SQL
     */
    public static String INT_NOT_NULL() {
        return Type.INT.getSql() + " " + Constraint.NOT_NULL.getSql();
    }
    
    /**
     * 可为空的INT
     * @return 可为空的INT SQL
     */
    public static String INT_NULL() {
        return Type.INT.getSql() + " " + Constraint.NULL.getSql();
    }
    
    /**
     * 带默认当前时间戳的TIMESTAMP
     * @return 带默认当前时间戳的TIMESTAMP SQL
     */
    public static String TIMESTAMP_DEFAULT_CURRENT() {
        return Type.TIMESTAMP.getSql() + " " + Constraint.DEFAULT_CURRENT_TIMESTAMP.getSql();
    }
    
    /**
     * 带默认当前时间戳和自动更新的TIMESTAMP
     * @return 带默认当前时间戳和自动更新的TIMESTAMP SQL
     */
    public static String TIMESTAMP_DEFAULT_CURRENT_ON_UPDATE() {
        return Type.TIMESTAMP.getSql() + " " + Constraint.DEFAULT_CURRENT_TIMESTAMP.getSql() + " " + 
               Constraint.ON_UPDATE_CURRENT_TIMESTAMP.getSql();
    }
} 