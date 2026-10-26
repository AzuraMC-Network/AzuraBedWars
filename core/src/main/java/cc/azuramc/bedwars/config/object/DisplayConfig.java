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
@Comment("Scoreboards, tab list and damage display")
public class DisplayConfig {

    @Comment("In-game scoreboard")
    private GameScoreboard gameScoreboard = new GameScoreboard();

    @Comment("Lobby scoreboard")
    private LobbyScoreboard lobbyScoreboard = new LobbyScoreboard();

    @Comment("Game end scoreboard")
    private GameEndScoreboard gameEndScoreboard = new GameEndScoreboard();

    @Comment("Tab list")
    private TabLists tabList = new TabLists();

    @Comment("Damage display")
    private DisplayDamage displayDamage = new DisplayDamage();

    @Data
    public static class GameScoreboard {
        @Comment("Scoreboard title")
        private String title = "&e&lBED WARS";

        @Comment({
                "Scoreboard lines",
                "Placeholders: {date}, {next_event}, {time_left}, {server}",
                "{teams} is replaced by one line per team"
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
                "Team line",
                "Placeholders: {team_name}, {bed_status}, {alive_count}, {my_team_mark}"
        })
        private String teamLineFormat = "{team_name} {bed_status} &f| {alive_count}{my_team_mark}";

        @Comment("Your team mark")
        private String myTeamMark = " &7YOU";

        @Comment("Destroyed bed icon")
        private String bedDestroyed = "&7❤";

        @Comment("Standing bed icon")
        private String bedAlive = "&c❤";
    }

    @Data
    public static class LobbyScoreboard {
        @Comment("Scoreboard title")
        private String title = "&e&lBED WARS";

        @Comment({
                "Scoreboard lines",
                "Placeholders: {date}, {map_name}, {team_size}, {team_count}, {author},",
                "          {players}, {max_players}, {countdown}, {mode}, {version}, {server}"
        })
        private List<String> lines = new ArrayList<>(Arrays.asList(
                "&7{date}",
                "",
                "&fMap: &a{map_name}",
                "&fTeams: &a{team_count}x{team_size}",
                "&fAuthor: &a{author}",
                "",
                "&fPlayers: &a{players}/{max_players}",
                "",
                "{countdown}",
                "",
                "&fMode: &a{mode}",
                "",
                "&fVersion: &a{version}",
                "",
                "&bas.azuramc.cc"
        ));

        @Comment("Waiting text")
        private String waitingMessage = "&fWaiting...";

        @Comment("Countdown text")
        private String countdownFormat = "Starting in {seconds}s";

        @Comment("Name of the item mode")
        private String defaultMode = "Normal";

        @Comment("Name of the experience mode")
        private String expMode = "Experience";
    }

    @Data
    public static class GameEndScoreboard {
        @Comment("Scoreboard title")
        private String title = "&e&lBED WARS";

        @Comment({
                "Scoreboard lines",
                "Placeholders: {date}, {winner}, {server}"
        })
        private List<String> lines = new ArrayList<>(Arrays.asList(
                "&7{date}",
                "",
                "&cGame Over",
                "&fWinner: &a{winner}",
                "",
                "&bas.azuramc.cc"
        ));
    }

    @Data
    public static class TabLists {
        @Comment("Waiting")
        private TabList waiting = new TabList(
                new ArrayList<>(Arrays.asList("&bYou are playing &eBed Wars &bon &eAzuraMC")),
                new ArrayList<>(Arrays.asList("&bas.azuramc.cc")));

        @Comment({"In game", "Placeholders: <currentGameKill>, <currentGameFinalKill>, <currentGameBedBreak>"})
        private TabList running = new TabList(
                new ArrayList<>(Arrays.asList("&bYou are playing &eBed Wars &bon &eAzuraMC")),
                new ArrayList<>(Arrays.asList(
                        "&bKills: &e<currentGameKill> &bFinal Kills: &e<currentGameFinalKill> &bBeds Broken: &e<currentGameBedBreak>",
                        "&bas.azuramc.cc")));

        @Comment({"Game over", "Placeholders: <gameResult>, <currentGameKill>, <currentGameFinalKill>, <currentGameBedBreak>"})
        private TabList ending = new TabList(
                new ArrayList<>(Arrays.asList("&bYou are playing &eBed Wars &bon &eAzuraMC", "&bGame over &e<gameResult>")),
                new ArrayList<>(Arrays.asList(
                        "&bKills: &e<currentGameKill> &bFinal Kills: &e<currentGameFinalKill> &bBeds Broken: &e<currentGameBedBreak>",
                        "&bas.azuramc.cc")));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabList {
        @Comment("Header")
        private List<String> header = new ArrayList<>();

        @Comment("Footer")
        private List<String> footer = new ArrayList<>();
    }

    @Data
    public static class DisplayDamage {
        @Comment("Show arrow damage")
        private boolean arrowDisplayEnabled = true;

        @Comment("Show melee damage")
        private boolean attackDisplayEnabled = true;
    }
}
