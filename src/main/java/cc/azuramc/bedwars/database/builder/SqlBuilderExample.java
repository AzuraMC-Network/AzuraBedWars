package cc.azuramc.bedwars.database.builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static cc.azuramc.bedwars.database.builder.DataType.*;
import static cc.azuramc.bedwars.database.builder.DataType.Type.*;
import static cc.azuramc.bedwars.database.builder.DataType.Constraint.*;

/**
 * SQL构建器示例类，展示如何使用SQL构建器
 * @author an5w1r@163.com
 */
public class SqlBuilderExample {

    /**
     * 展示如何使用PreparedStatementBuildManager和DataType工具类
     * @param connection 数据库连接
     * @throws SQLException 如果发生SQL异常
     */
    public static void example(Connection connection) throws SQLException {
        // 创建SQL构建管理器
        PreparedStatementBuildManager manager = new PreparedStatementBuildManager(connection, true);
        
        // 创建用户表（使用DataType工具类）
        try (PreparedStatement createUserStmt = manager.createTable("users")
                .ifNotExists()
                .column("id", PK_INT())
                .column("username", VARCHAR_NOT_NULL(50), UNIQUE.getSql())
                .column("password", VARCHAR.size(255), NOT_NULL.getSql())
                .column("email", VARCHAR.size(100), NOT_NULL.getSql())
                .column("created_at", TIMESTAMP_DEFAULT_CURRENT())
                .engine("InnoDB")
                .charset("utf8mb4")
                .prepare()) {
            createUserStmt.executeUpdate();
            System.out.println("用户表创建成功");
        }
        
        // 创建帖子表（使用DataType工具类）
        try (PreparedStatement createPostStmt = manager.createTable("posts")
                .ifNotExists()
                .column("id", PK_INT())
                .column("user_id", INT_NOT_NULL())
                .column("post_title", VARCHAR.size(100), NOT_NULL.getSql())
                .column("post_content", TEXT.getSql(), NOT_NULL.getSql())
                .column("created_at", TIMESTAMP_DEFAULT_CURRENT())
                .foreignKey("user_id", "users", "id", CASCADE.getSql(), CASCADE.getSql())
                .index("idx_user_id", "user_id")
                .engine("InnoDB")
                .charset("utf8mb4")
                .prepare()) {
            createPostStmt.executeUpdate();
            System.out.println("帖子表创建成功");
        }
        
        // 插入用户数据
        try (PreparedStatement insertStmt = manager.insertInto("users")
                .values("username", "user1")
                .values("password", "password1")
                .values("email", "user1@example.com")
                .returnGeneratedKeys()
                .prepare()) {
            insertStmt.executeUpdate();
            try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int userId = keys.getInt(1);
                    System.out.println("插入成功，用户ID: " + userId);
                    
                    // 使用获取的用户ID插入帖子
                    try (PreparedStatement insertPostStmt = manager.insertInto("posts")
                            .values("user_id", userId)
                            .values("post_title", "第一篇帖子")
                            .values("post_content", "这是第一篇帖子的内容")
                            .prepare()) {
                        insertPostStmt.executeUpdate();
                        System.out.println("插入帖子成功");
                    }
                }
            }
        }
        
        // 批量插入用户
        try (PreparedStatement batchInsertStmt = manager.insertInto("users")
                .columns("username", "password", "email")
                .addBatch("user2", "password2", "user2@example.com")
                .addBatch("user3", "password3", "user3@example.com")
                .addBatch("user4", "password4", "user4@example.com")
                .prepare()) {
            int[] results = batchInsertStmt.executeBatch();
            System.out.println("批量插入用户成功，影响行数: " + results.length);
            
            // 获取新插入用户的ID
            try (PreparedStatement selectStmt = manager.select("id")
                    .from("users")
                    .where("username", "IN", "('user2', 'user3', 'user4')")
                    .prepare();
                 ResultSet rs = selectStmt.executeQuery()) {
                
                while (rs.next()) {
                    int userId = rs.getInt("id");
                    
                    // 为每个用户插入帖子
                    try (PreparedStatement insertPostStmt = manager.insertInto("posts")
                            .values("user_id", userId)
                            .values("post_title", "用户" + userId + "的帖子")
                            .values("post_content", "这是用户" + userId + "发布的帖子内容")
                            .prepare()) {
                        insertPostStmt.executeUpdate();
                        System.out.println("为用户" + userId + "插入帖子成功");
                    }
                }
            }
        }
        
        // 复杂查询：查询每个用户及其发布的帖子数量
        try (PreparedStatement complexStmt = manager.select("u.id", "u.username", "u.email", "COUNT(p.id) as post_count")
                .from("users u")
                .leftJoin("posts p", "u.id = p.user_id")
                .groupBy("u.id", "u.username", "u.email")
                .orderBy("post_count", "DESC")
                .prepare();
             ResultSet rs = complexStmt.executeQuery()) {
            
            System.out.println("\n用户及其帖子数量：");
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                int postCount = rs.getInt("post_count");
                System.out.println("用户ID: " + id + ", 用户名: " + username + 
                                   ", 邮箱: " + email + ", 帖子数量: " + postCount);
            }
        }
        
        // 更新特定用户的邮箱
        try (PreparedStatement updateStmt = manager.update("users")
                .set("email", "updated@example.com")
                .whereEquals("username", "user1")
                .prepare()) {
            int rows = updateStmt.executeUpdate();
            System.out.println("\n更新用户邮箱成功，影响行数: " + rows);
        }
        
        // 删除没有发帖的用户
        try (PreparedStatement deleteStmt = manager.deleteFrom("users")
                .where("id", "NOT IN", "(SELECT DISTINCT user_id FROM posts)")
                .prepare()) {
            int rows = deleteStmt.executeUpdate();
            System.out.println("\n删除无帖子用户成功，影响行数: " + rows);
        }
        
        // 输出SQL构建结果而不执行
        SelectBuilder selectBuilder = manager.select("u.*", "COUNT(p.id) as post_count")
                .from("users u")
                .leftJoin("posts p", "u.id = p.user_id")
                .where("u.created_at", ">", "2023-01-01")
                .groupBy("u.id")
                .having("COUNT(p.id)", ">", 2)
                .orderBy("post_count", "DESC")
                .limit(10);
        
        System.out.println("\n生成的SQL: " + selectBuilder.toSql());
    }
    
    /**
     * 展示如何使用DataType工具类创建复杂表结构
     * @param connection 数据库连接
     * @throws SQLException 如果发生SQL异常
     */
    public static void createComplexTable(Connection connection) throws SQLException {
        PreparedStatementBuildManager manager = new PreparedStatementBuildManager(connection, true);
        
        // 创建一个包含各种数据类型的商品表
        try (PreparedStatement createProductStmt = manager.createTable("products")
                .ifNotExists()
                .column("id", PK_INT())
                .column("name", VARCHAR.size(100), NOT_NULL.getSql(), UNIQUE.getSql())
                .column("description", TEXT.getSql())
                .column("price", DECIMAL.precision(10, 2), NOT_NULL.getSql(), DEFAULT("0.00"))
                .column("stock", INT.getSql(), NOT_NULL.getSql(), DEFAULT(0), UNSIGNED.getSql())
                .column("category_id", INT.getSql(), NOT_NULL.getSql())
                .column("is_featured", TINYINT.size(1), NOT_NULL.getSql(), DEFAULT(0), COMMENT("1表示精选商品，0表示普通商品"))
                .column("rating", FLOAT.getSql(), DEFAULT(0))
                .column("tags", SET.values("新品", "热卖", "促销", "限量"))
                .column("status", ENUM.values("在售", "下架", "缺货"), NOT_NULL.getSql(), DEFAULT("在售"))
                .column("created_at", TIMESTAMP_DEFAULT_CURRENT())
                .column("updated_at", TIMESTAMP_DEFAULT_CURRENT_ON_UPDATE())
                .column("image_data", MEDIUMBLOB.getSql())
                .column("properties", JSON.getSql())
                .primaryKey("id")
                .foreignKey("category_id", "categories", "id", RESTRICT.getSql(), CASCADE.getSql())
                .index("idx_price", "price")
                .index("idx_stock", "stock")
                .uniqueKey("uk_name_category", "name", "category_id")
                .engine("InnoDB")
                .charset("utf8mb4")
                .collate("utf8mb4_unicode_ci")
                .prepare()) {
            createProductStmt.executeUpdate();
            System.out.println("创建复杂商品表成功");
        }
        
        // 创建订单表
        try (PreparedStatement createOrderStmt = manager.createTable("orders")
                .ifNotExists()
                .column("id", PK_INT())
                .column("order_no", VARCHAR.size(50), NOT_NULL.getSql(), UNIQUE.getSql(), COMMENT("订单编号"))
                .column("user_id", INT_NOT_NULL())
                .column("total_amount", DECIMAL.precision(10, 2), NOT_NULL.getSql(), DEFAULT("0.00"))
                .column("status", ENUM.values("待付款", "已付款", "已发货", "已完成", "已取消"), NOT_NULL.getSql(), DEFAULT("待付款"))
                .column("payment_method", VARCHAR.size(50))
                .column("shipping_address", VARCHAR.size(255), NOT_NULL.getSql())
                .column("created_at", TIMESTAMP_DEFAULT_CURRENT())
                .column("paid_at", DATETIME.getSql())
                .column("shipped_at", DATETIME.getSql())
                .column("completed_at", DATETIME.getSql())
                .foreignKey("user_id", "users", "id", CASCADE.getSql(), CASCADE.getSql())
                .index("idx_order_no", "order_no")
                .index("idx_user_id", "user_id")
                .engine("InnoDB")
                .charset("utf8mb4")
                .prepare()) {
            createOrderStmt.executeUpdate();
            System.out.println("创建订单表成功");
        }
        
        // 创建订单明细表
        try (PreparedStatement createOrderItemStmt = manager.createTable("order_items")
                .ifNotExists()
                .column("id", PK_INT())
                .column("order_id", INT_NOT_NULL())
                .column("product_id", INT_NOT_NULL())
                .column("quantity", INT_NOT_NULL(), DEFAULT(1))
                .column("price", DECIMAL.precision(10, 2), NOT_NULL.getSql())
                .column("created_at", TIMESTAMP_DEFAULT_CURRENT())
                .foreignKey("order_id", "orders", "id", CASCADE.getSql(), CASCADE.getSql())
                .foreignKey("product_id", "products", "id", RESTRICT.getSql(), RESTRICT.getSql())
                .index("idx_order_product", "order_id", "product_id")
                .engine("InnoDB")
                .charset("utf8mb4")
                .prepare()) {
            createOrderItemStmt.executeUpdate();
            System.out.println("创建订单明细表成功");
        }
    }
} 