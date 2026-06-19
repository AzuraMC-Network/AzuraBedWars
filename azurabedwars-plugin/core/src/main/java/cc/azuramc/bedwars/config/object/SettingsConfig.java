package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("AzuraBedWars 主配置文件")
public class SettingsConfig {

    @Comment("是否启用编辑器模式(用于地图编辑)")
    private boolean editorMode = false;

    @Comment("是否启用调试模式(输出详细日志)")
    private boolean debugMode = false;

    @Comment("队伍方块搜索半径")
    private int teamBlockSearchRadius = 15;

    @Comment({"地图存储方式", "可选: JSON, MYSQL"})
    private String mapStorage = "JSON";

    @Comment("默认地图名称")
    private String defaultMapName = "game";

    @Comment("床搜索半径(用于检测床是否被破坏)")
    private int bedSearchRadius = 18;

    @Comment("破坏床奖励金币数量")
    private int bedDestroyReward = 10;

    @Comment("再来一局命令")
    private String playAgainCommand = "/not impl now";

    @Comment("玩家最大生命值")
    private int maxHealth = 20;

    @Comment("最大静止时间(秒)，超过将被判定为挂机")
    private int maxNoMovementTime = 45;

    @Comment("队伍出生点保护半径")
    private int teamSpawnProtectionRadius = 6;

    @Comment("资源点保护半径")
    private int resourceSpawnProtectionRadius = 2;

    @Comment("是否启用游戏模式选择")
    private boolean enableGameModeSelection = true;

    @Comment("是否启用队伍选择")
    private boolean enableTeamSelection = true;

    @Comment({"数据库配置", "用于存储玩家数据和统计信息"})
    @NotNull
    private DatabaseConfig database = new DatabaseConfig();

    @Comment("聊天相关配置")
    @NotNull
    private ChatConfig chatConfig = new ChatConfig();

    @Comment("游戏中计分板配置")
    @NotNull
    private GameScoreboard gameScoreboard = new GameScoreboard();

    @Comment("大厅计分板配置")
    @NotNull
    private LobbyScoreboard lobbyScoreboard = new LobbyScoreboard();

    @Comment("游戏结束计分板配置")
    @NotNull
    private GameEndScoreboard gameEndScoreboard = new GameEndScoreboard();

    @Comment("等待状态 Tab 列表配置")
    @NotNull
    private WaitingState waitingState = new WaitingState();

    @Comment("游戏中 Tab 列表配置")
    @NotNull
    private RunningState runningState = new RunningState();

    @Comment("结束状态 Tab 列表配置")
    @NotNull
    private EndingState endingState = new EndingState();

    @Comment("伤害显示配置")
    @NotNull
    private DisplayDamage displayDamage = new DisplayDamage();

    @Comment("火球配置")
    @NotNull
    private FireBall fireBall = new FireBall();

    @Data
    public static class DatabaseConfig {
        @Comment({"数据库类型", "可选: MySQL, MongoDB"})
        private String databaseType = "MySQL";

        @Comment("数据库主机地址")
        private String host = "localhost";

        @Comment("数据库端口")
        private int port = 3306;

        @Comment("数据库用户名")
        private String username = "root";

        @Comment("数据库密码")
        private String password = "123456";

        @Comment("数据库名称")
        private String database = "azurabw";
    }

    @Data
    public static class FireBall {
        @Comment("火球爆炸伤害值")
        private int fireballDamage = 3;
    }

    @Data
    public static class ChatConfig {
        @Comment("全局聊天前缀(使用此前缀发送消息将被所有人看到)")
        private String globalChatPrefix = MessageUtil.color("!");

        @Comment("旁观者聊天前缀")
        private String spectatorPrefix = MessageUtil.color("&7[旁观者]");

        @Comment("全局聊天标签")
        private String globalChatTag = MessageUtil.color("&6[全局]");

        @Comment("队伍聊天标签")
        private String teamChatTag = MessageUtil.color("&9[团队]");

        @Comment("聊天分隔符")
        private String chatSeparator = MessageUtil.color("&7: ");

        @Comment("全局聊天冷却时间(秒)")
        private int globalChatCooldown = 10;
    }

    @Data
    public static class GameScoreboard {
        @Comment("计分板标题")
        private String title = MessageUtil.color("&e&l起床战争");

        @Comment({
                "计分板内容行",
                "可用变量: {date}, {next_event}, {time_left}, {server}",
                "特殊标记: {teams} 会被替换为所有队伍状态行"
        })
        private List<String> lines = Arrays.asList(
                "&7{date}",
                "",
                "{next_event}",
                "&a{time_left}",
                "",
                "{teams}",
                "",
                "&bas.azuramc.cc"
        );

        @Comment({
                "队伍行格式",
                "可用变量: {team_name}, {bed_status}, {alive_count}, {my_team_mark}"
        })
        private String teamLineFormat = "{team_name} {bed_status} &f| {alive_count}{my_team_mark}";

        @Comment("我的队伍标记")
        private String myTeamMark = MessageUtil.color(" &7(我的队伍)");

        @Comment("床已被破坏图标")
        private String bedDestroyed = MessageUtil.color("&7❤");

        @Comment("床存活图标")
        private String bedAlive = MessageUtil.color("&c❤");
    }

    @Data
    public static class LobbyScoreboard {
        @Comment("计分板标题")
        private String title = MessageUtil.color("&e&l起床战争");

        @Comment({
                "计分板内容行",
                "可用变量: {date}, {map_name}, {team_size}, {team_count}, {author},",
                "          {players}, {max_players}, {countdown}, {mode}, {version}, {server}"
        })
        private List<String> lines = Arrays.asList(
                "&7{date}",
                "",
                "&f地图: &a{map_name}",
                "&f队伍: &a{team_size}人 {team_count}队",
                "&f作者: &a{author}",
                "",
                "&f玩家: &a{players}/{max_players}",
                "",
                "{countdown}",
                "",
                "&f你的模式: &a{mode}",
                "",
                "&f版本: &a{version}",
                "",
                "&bas.azuramc.cc"
        );

        @Comment("等待中消息")
        private String waitingMessage = MessageUtil.color("&f等待中...");

        @Comment("倒计时格式")
        private String countdownFormat = "{seconds}秒后开始";

        @Comment("普通模式名称")
        private String defaultMode = "普通模式";

        @Comment("经验模式名称")
        private String expMode = "经验模式";
    }

    @Data
    public static class GameEndScoreboard {
        @Comment("计分板标题")
        private String title = MessageUtil.color("&e&l起床战争");

        @Comment({
                "计分板内容行",
                "可用变量: {date}, {winner}, {server}"
        })
        private List<String> lines = Arrays.asList(
                "&7{date}",
                "",
                "&c游戏结束",
                "&f胜利者: &a{winner}",
                "",
                "&bas.azuramc.cc"
        );
    }

    @Data
    public static class WaitingState {
        @Comment("Tab 列表头部")
        private List<String> header = Arrays.asList(
                MessageUtil.color("&b你正在 &eAzuraMC &b游玩起床战争")
        );

        @Comment("Tab 列表底部")
        private List<String> footer = Arrays.asList(
                MessageUtil.color("&bas.azuramc.cc")
        );
    }

    @Data
    public static class RunningState {
        @Comment("Tab 列表头部")
        private List<String> header = Arrays.asList(
                MessageUtil.color("&b你正在 &eAzuraMC &b游玩起床战争")
        );

        @Comment({"Tab 列表底部", "可用变量: <currentGameKill>, <currentGameFinalKill>, <currentGameBedBreak>"})
        private List<String> footer = Arrays.asList(
                MessageUtil.color("&b击杀数: &e<currentGameKill> &b最终击杀数: &e<currentGameFinalKill> &b破坏床数: &e<currentGameBedBreak>"),
                MessageUtil.color("&bas.azuramc.cc")
        );
    }

    @Data
    public static class EndingState {
        @Comment({"Tab 列表头部", "可用变量: <gameResult>"})
        private List<String> header = Arrays.asList(
                MessageUtil.color("&b你正在 &eAzuraMC &b游玩起床战争"),
                MessageUtil.color("&b游戏结束 &e<gameResult>")
        );

        @Comment({"Tab 列表底部", "可用变量: <currentGameKill>, <currentGameFinalKill>, <currentGameBedBreak>"})
        private List<String> footer = Arrays.asList(
                MessageUtil.color("&b击杀数: &e<currentGameKill> &b最终击杀数: &e<currentGameFinalKill> &b破坏床数: &e<currentGameBedBreak>"),
                MessageUtil.color("&bas.azuramc.cc")
        );
    }

    @Data
    public static class DisplayDamage {
        @Comment("是否启用弓箭伤害显示")
        private boolean arrowDisplayEnabled = true;

        @Comment("是否启用近战伤害显示")
        private boolean attackDisplayEnabled = true;
    }

}
