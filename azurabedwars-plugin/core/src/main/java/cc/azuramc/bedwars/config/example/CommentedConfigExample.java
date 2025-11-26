package cc.azuramc.bedwars.config.example;

import cc.azuramc.bedwars.config.annotation.ConfigComment;
import lombok.Data;

/**
 * 带注释的配置示例
 * 展示如何使用 @ConfigComment 注解为YAML配置文件添加字段注释
 *
 * @author AzuraBedWars Team
 */
@Data
public class CommentedConfigExample {

    // ========== 基本配置 ==========

    @ConfigComment("是否启用编辑器模式")
    private boolean editorMode = false;

    @ConfigComment({
            "是否启用调试模式",
            "开启后会在控制台输出详细的调试信息"
    })
    private boolean debugMode = false;

    @ConfigComment("地图存储类型 (JSON/REDIS)")
    private String mapStorage = "JSON";

    @ConfigComment("默认地图名称")
    private String defaultMapName = "game";

    // ========== 游戏设置 ==========

    @ConfigComment("床的搜索半径（方块）")
    private int bedSearchRadius = 18;

    @ConfigComment("摧毁床获得的金币奖励")
    private int bedDestroyReward = 10;

    @ConfigComment({
            "玩家最大生命值",
            "默认为20（10颗心）"
    })
    private int maxHealth = 20;

    @ConfigComment({
            "玩家最长无移动时间（秒）",
            "超过此时间将被踢出游戏"
    })
    private int maxNoMovementTime = 45;

    @ConfigComment("队伍出生点保护半径（方块）")
    private int teamSpawnProtectionRadius = 6;

    @ConfigComment("资源生成点保护半径（方块）")
    private int resourceSpawnProtectionRadius = 2;

    // ========== UI设置 ==========

    @ConfigComment("是否启用游戏模式选择界面")
    private boolean enableGameModeSelection = true;

    @ConfigComment("是否启用队伍选择界面")
    private boolean enableTeamSelection = true;

    // ========== 嵌套配置对象 ==========

    @ConfigComment("数据库配置")
    private DatabaseConfig database = new DatabaseConfig();

    @ConfigComment("聊天系统配置")
    private ChatConfig chatConfig = new ChatConfig();

    @ConfigComment("火球配置")
    private FireBallConfig fireBall = new FireBallConfig();

    /**
     * 数据库配置
     */
    @Data
    public static class DatabaseConfig {

        @ConfigComment("数据库类型 (MySQL/SQLite)")
        private String databaseType = "MySQL";

        @ConfigComment("数据库主机地址")
        private String host = "localhost";

        @ConfigComment("数据库端口")
        private int port = 3306;

        @ConfigComment("数据库用户名")
        private String username = "root";

        @ConfigComment("数据库密码")
        private String password = "123456";

        @ConfigComment("数据库名称")
        private String database = "azurabw";

        @ConfigComment({
                "是否使用SSL连接",
                "生产环境建议启用"
        })
        private boolean useSSL = false;
    }

    /**
     * 聊天配置
     */
    @Data
    public static class ChatConfig {

        @ConfigComment("全局聊天前缀（玩家输入此前缀发送全局消息）")
        private String globalChatPrefix = "!";

        @ConfigComment("旁观者聊天前缀")
        private String spectatorPrefix = "&7[旁观者]";

        @ConfigComment("全局聊天标签")
        private String globalChatTag = "&6[全局]";

        @ConfigComment("团队聊天标签")
        private String teamChatTag = "&9[团队]";

        @ConfigComment({
                "全局聊天冷却时间（秒）",
                "防止玩家刷屏"
        })
        private int globalChatCooldown = 10;
    }

    /**
     * 火球配置
     */
    @Data
    public static class FireBallConfig {

        @ConfigComment({
                "火球造成的伤害值",
                "注意：实际伤害还会受到距离等因素影响"
        })
        private int fireballDamage = 3;

        @ConfigComment("火球爆炸半径（方块）")
        private double explosionRadius = 3.0;

        @ConfigComment("火球飞行速度")
        private double fireballSpeed = 1.5;
    }
}
