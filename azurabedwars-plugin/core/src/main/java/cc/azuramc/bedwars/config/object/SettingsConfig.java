package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.ConfigComment;
import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * 设置配置对象
 * 存储插件的基本设置信息
 *
 * @author an5w1r@163.com
 */
@Data
public class SettingsConfig {
    @ConfigComment("是否启用编辑器模式")
    private boolean editorMode = false;

    @ConfigComment({
            "是否启用调试模式",
            "开启后会在控制台输出详细的调试信息"
    })
    private boolean debugMode = false;

    @ConfigComment("队伍方块搜索半径（方块）")
    private final int teamBlockSearchRadius = 15;

    @ConfigComment("地图存储方式 (JSON/REDIS)")
    private String mapStorage = "JSON";

    @ConfigComment("是否启用 Redis 地图功能")
    private boolean enabledJedisMapFeature = false;

    @ConfigComment("默认地图名称")
    private String defaultMapName = "game";

    @ConfigComment("床的搜索半径（方块）")
    private int bedSearchRadius = 18;

    @ConfigComment("摧毁床时获得的金币奖励")
    private int bedDestroyReward = 10;

    @ConfigComment("再玩一局命令（暂未实现）")
    private String playAgainCommand = "/not impl now";

    @ConfigComment({
            "玩家最大生命值",
            "默认为20（10颗心）"
    })
    private int maxHealth = 20;

    @ConfigComment({
            "玩家最长无移动时间（秒）",
            "超过此时间将被判定为挂机并踢出游戏"
    })
    private int maxNoMovementTime = 45;

    @ConfigComment("队伍出生点保护半径（方块）")
    private int teamSpawnProtectionRadius = 6;

    @ConfigComment("资源生成点保护半径（方块）")
    private int resourceSpawnProtectionRadius = 2;

    @ConfigComment("是否启用游戏模式选择界面")
    private boolean enableGameModeSelection = true;

    @ConfigComment("是否启用队伍选择界面")
    private boolean enableTeamSelection = true;

    @ConfigComment("数据库配置")
    private DatabaseConfig database = new DatabaseConfig();

    @ConfigComment("聊天系统配置")
    private ChatConfig chatConfig = new ChatConfig();

    @ConfigComment("游戏中计分板配置")
    private GameScoreboard gameScoreboard = new GameScoreboard();

    @ConfigComment("大厅计分板配置")
    private LobbyScoreboard lobbyScoreboard = new LobbyScoreboard();

    @ConfigComment("等待状态TAB列表配置")
    private WaitingState waitingState = new WaitingState();

    @ConfigComment("游戏进行中TAB列表配置")
    private RunningState runningState = new RunningState();

    @ConfigComment("游戏结束TAB列表配置")
    private EndingState endingState = new EndingState();

    @ConfigComment("伤害显示配置")
    private DisplayDamage displayDamage = new DisplayDamage();

    @ConfigComment("火球配置")
    private FireBall fireBall = new FireBall();

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
    }

    @Data
    public static class FireBall {
        @ConfigComment({
                "火球造成的伤害值",
                "实际伤害还会受到距离等因素影响"
        })
        private int fireballDamage = 3;
    }

    @Data
    public static class ChatConfig {
        @ConfigComment("全局聊天前缀（玩家输入此前缀发送全局消息）")
        private String globalChatPrefix = MessageUtil.color("!");

        @ConfigComment("旁观者聊天前缀")
        private String spectatorPrefix = MessageUtil.color("&7[旁观者]");

        @ConfigComment("全局聊天标签")
        private String globalChatTag = MessageUtil.color("&6[全局]");

        @ConfigComment("团队聊天标签")
        private String teamChatTag = MessageUtil.color("&9[团队]");

        @ConfigComment("聊天分隔符")
        private String chatSeparator = MessageUtil.color("&7: ");

        @ConfigComment({
                "全局聊天冷却时间（秒）",
                "防止玩家刷屏"
        })
        private int globalChatCooldown = 10;
    }

    @Data
    public static class GameScoreboard {
        @ConfigComment("计分板标题")
        private String title = MessageUtil.color("&e&l起床战争");

        @ConfigComment("服务器信息")
        private String serverInfo = MessageUtil.color("&bas.azuramc.cc");

        @ConfigComment("我的队伍标记")
        private String myTeamMark = MessageUtil.color(" &7(我的队伍)");

        @ConfigComment("床被摧毁的显示符号")
        private String bedDestroyed = MessageUtil.color("&7❤");

        @ConfigComment("床存活的显示符号")
        private String bedAlive = MessageUtil.color("&c❤");

        @ConfigComment("分隔符")
        private String separator = MessageUtil.color("&f | ");

        @ConfigComment("空行")
        private String emptyLine = MessageUtil.color("");
    }

    @Data
    public static class LobbyScoreboard {
        @ConfigComment("大厅计分板标题")
        private String title = MessageUtil.color("&e&l起床战争");

        @ConfigComment("服务器信息")
        private String serverInfo = MessageUtil.color("&bas.azuramc.cc");

        @ConfigComment("等待消息")
        private String waitingMessage = MessageUtil.color("&f等待中...");

        @ConfigComment("空行")
        private String emptyLine = MessageUtil.color("");

        @ConfigComment("默认模式名称")
        private String defaultMode = MessageUtil.color("普通模式");

        @ConfigComment("经验模式名称")
        private String expMode = MessageUtil.color("经验模式");
    }

    @Data
    public static class WaitingState {
        @ConfigComment({
                "等待状态TAB列表头部",
                "支持颜色代码和变量"
        })
        private List<String> header = Arrays.asList(
                MessageUtil.color("&b你正在 &eAzuraMC &b游玩起床战争")
        );

        @ConfigComment({
                "等待状态TAB列表底部",
                "支持颜色代码和变量"
        })
        private List<String> footer = Arrays.asList(
                MessageUtil.color("&bas.azuramc.cc")
        );
    }

    @Data
    public static class RunningState {
        @ConfigComment({
                "游戏进行中TAB列表头部",
                "支持颜色代码和变量"
        })
        private List<String> header = Arrays.asList(
                MessageUtil.color("&b你正在 &eAzuraMC &b游玩起床战争")
        );

        @ConfigComment({
                "游戏进行中TAB列表底部",
                "可用变量: <currentGameKill>, <currentGameFinalKill>, <currentGameBedBreak>"
        })
        private List<String> footer = Arrays.asList(
                MessageUtil.color("&b击杀数: &e<currentGameKill> &b最终击杀数: &e<currentGameFinalKill> &b破坏床数: &e<currentGameBedBreak>"),
                MessageUtil.color("&bas.azuramc.cc")
        );
    }

    @Data
    public static class EndingState {
        @ConfigComment({
                "游戏结束TAB列表头部",
                "可用变量: <gameResult>"
        })
        private List<String> header = Arrays.asList(
                MessageUtil.color("&b你正在 &eAzuraMC &b游玩起床战争"),
                MessageUtil.color("&b游戏结束 &e<gameResult>")
        );

        @ConfigComment({
                "游戏结束TAB列表底部",
                "可用变量: <currentGameKill>, <currentGameFinalKill>, <currentGameBedBreak>"
        })
        private List<String> footer = Arrays.asList(
                MessageUtil.color("&b击杀数: &e<currentGameKill> &b最终击杀数: &e<currentGameFinalKill> &b破坏床数: &e<currentGameBedBreak>"),
                MessageUtil.color("&bas.azuramc.cc")
        );
    }

    @Data
    public static class DisplayDamage {
        @ConfigComment("是否启用箭矢伤害显示")
        private final boolean arrowDisplayEnabled = true;

        @ConfigComment("是否启用攻击伤害显示")
        private final boolean attackDisplayEnabled = true;
    }

}
