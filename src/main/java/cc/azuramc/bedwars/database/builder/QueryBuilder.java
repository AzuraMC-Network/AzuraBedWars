package cc.azuramc.bedwars.database.builder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * SQL查询构建器接口，定义所有SQL构建器的通用方法
 * @author an5w1r@163.com
 */
public interface QueryBuilder {
    /**
     * 准备SQL语句，返回PreparedStatement对象
     * @return PreparedStatement对象
     * @throws SQLException 如果发生SQL异常
     */
    PreparedStatement prepare() throws SQLException;
    
    /**
     * 获取SQL语句的字符串表示
     * @return SQL语句的字符串表示
     */
    default String toSql() {
        throw new UnsupportedOperationException("此构建器不支持toSql方法");
    }
} 