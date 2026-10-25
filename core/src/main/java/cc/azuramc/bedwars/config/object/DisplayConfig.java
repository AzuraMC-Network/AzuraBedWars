package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("计分板、Tab 列表与伤害显示")
public class DisplayConfig {

    @Comment("游戏中计分板")
    private GameScoreboard gameScoreboard = new GameScoreboard();

    @Comment("大厅计分板")
    private LobbyScoreboard lobbyScoreboard = new LobbyScoreboard();

    @Comment("游戏结束计分板")
    private GameEndScoreboard gameEndScoreboard = new GameEndScoreboard();

    @Comment("Tab 列表")
    private TabLists tabList = new TabLists();

    @Comment("伤害显示")
    private DisplayDamage displayDamage = new DisplayDamage();

    @Data
    public static class GameScoreboard {
        @Comment("计分板标题")
        private String title = "&e&l起床战争";

        @Comment({
                "计分板内容行",
                "可用变量: {date}, {next_event}, {time_left}, {server}",
                "特殊标记: {teams} 会被替换为所有队伍状态行"
        })
        private List<String> lines = new ArrayList<>(Arrays.asList(
                "&7{date}",
                "",
                "{next_event}",
                "&a{time_left}",
                "",
                "{teams}",
                "",
                "&bas.azuramc.cc"
        ));

        @Comment({
                "队伍行格式",
                "可用变量: {team_name}, {bed_status}, {alive_count}, {my_team_mark}"
        })
        private String teamLineFormat = "{team_name} {bed_status} &f| {alive_count}{my_team_mark}";

        @Comment("我的队伍标记")
        private String myTeamMark = " &7(我的队伍)";

        @Comment("床已被破坏图标")
        private String bedDestroyed = "&7❤";

        @Comment("床存活图标")
        private String bedAlive = "&c❤";
    }

    @Data
    public static class LobbyScoreboard {
        @Comment("计分板标题")
        private String title = "&e&l起床战争";

        @Comment({
                "计分板内容行",
                "可用变量: {date}, {map_name}, {team_size}, {team_count}, {author},",
                "          {players}, {max_players}, {countdown}, {mode}, {version}, {server}"
        })
        private List<String> lines = new ArrayList<>(Arrays.asList(
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
                "&f模式: &a{mode}",
                "",
                "&f版本: &a{version}",
                "",
                "&bas.azuramc.cc"
        ));

        @Comment("等待中消息")
        private String waitingMessage = "&f等待中...";

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
        private String title = "&e&l起床战争";

        @Comment({
                "计分板内容行",
                "可用变量: {date}, {winner}, {server}"
        })
        private List<String> lines = new ArrayList<>(Arrays.asList(
                "&7{date}",
                "",
                "&c游戏结束",
                "&f胜利者: &a{winner}",
                "",
                "&bas.azuramc.cc"
        ));
    }

    @Data
    public static class TabLists {
        @Comment("等待中")
        private TabList waiting = new TabList(
                new ArrayList<>(Arrays.asList("&b你正在 &eAzuraMC &b游玩起床战争")),
                new ArrayList<>(Arrays.asList("&bas.azuramc.cc")));

        @Comment({"游戏中", "可用变量: <currentGameKill>, <currentGameFinalKill>, <currentGameBedBreak>"})
        private TabList running = new TabList(
                new ArrayList<>(Arrays.asList("&b你正在 &eAzuraMC &b游玩起床战争")),
                new ArrayList<>(Arrays.asList(
                        "&b击杀数: &e<currentGameKill> &b最终击杀数: &e<currentGameFinalKill> &b破坏床数: &e<currentGameBedBreak>",
                        "&bas.azuramc.cc")));

        @Comment({"游戏结束", "可用变量: <gameResult>, <currentGameKill>, <currentGameFinalKill>, <currentGameBedBreak>"})
        private TabList ending = new TabList(
                new ArrayList<>(Arrays.asList("&b你正在 &eAzuraMC &b游玩起床战争", "&b游戏结束 &e<gameResult>")),
                new ArrayList<>(Arrays.asList(
                        "&b击杀数: &e<currentGameKill> &b最终击杀数: &e<currentGameFinalKill> &b破坏床数: &e<currentGameBedBreak>",
                        "&bas.azuramc.cc")));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabList {
        @Comment("头部")
        private List<String> header = new ArrayList<>();

        @Comment("底部")
        private List<String> footer = new ArrayList<>();
    }

    @Data
    public static class DisplayDamage {
        @Comment("是否启用弓箭伤害显示")
        private boolean arrowDisplayEnabled = true;

        @Comment("是否启用近战伤害显示")
        private boolean attackDisplayEnabled = true;
    }
}
