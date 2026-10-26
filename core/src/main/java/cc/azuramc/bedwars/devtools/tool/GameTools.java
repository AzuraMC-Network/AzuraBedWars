package cc.azuramc.bedwars.devtools.tool;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.devtools.Args;
import cc.azuramc.bedwars.devtools.Devtools;
import cc.azuramc.bedwars.devtools.McpTool;
import cc.azuramc.bedwars.devtools.ToolError;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.economy.EconomyType;
import cc.azuramc.bedwars.game.task.generator.GeneratorManager;
import cc.azuramc.bedwars.game.timeline.GameTimeline;
import cc.azuramc.bedwars.game.timeline.TimelineEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public final class GameTools {

    private static final int MAX_GIVE = 640;

    private final AzuraBedWars plugin;
    private final Devtools devtools;

    public GameTools(AzuraBedWars plugin, Devtools devtools) {
        this.plugin = plugin;
        this.devtools = devtools;
    }

    public List<McpTool> tools() {
        return Arrays.asList(
                McpTool.read("game_state", "The game on this server: state, start countdown, timeline stage with seconds left and the stages"
                                + " after it, and every team with its players and bed. Needs game mode")
                        .handle(args -> devtools.sync(this::state)),
                McpTool.destructive("game_control", "Drive a test game. start begins the game now instead of waiting for the countdown,"
                                + " skip runs the current timeline stage now, end finishes without a winner, records stats and shuts the"
                                + " server down after the results like a real game end")
                        .choice("action", "What to do", true, "start", "skip", "end")
                        .handle(args -> devtools.sync(() -> control(args))),
                McpTool.write("player_give", "Give an online player resources to test the shop, as items in item economy"
                                + " or as levels in experience economy")
                        .string("player", "Online player name", true)
                        .string("resource", "Resource id from generators.yml, such as iron or diamond", true)
                        .integer("amount", "How many, 1 to " + MAX_GIVE, true)
                        .handle(args -> devtools.sync(() -> give(args)))
        );
    }

    private GameManager game() {
        GameManager gameManager = plugin.getGameManager();
        if (gameManager == null) {
            throw new ToolError("No game runs, the server is in editor mode because editor-mode is on in settings.yml"
                    + " or no map passed map_check at startup. Fix that and restart");
        }
        return gameManager;
    }

    private JsonElement state() {
        GameManager gameManager = game();
        JsonObject state = new JsonObject();
        state.addProperty("state", gameManager.getGameState().name());
        state.addProperty("map", gameManager.getMapData().getName());
        state.addProperty("economy", gameManager.getEconomyType().name());
        state.addProperty("onlinePlayers", GamePlayer.getOnlinePlayers().size());
        state.addProperty("minPlayers", gameManager.getMapData().getPlayers().getMin());
        state.addProperty("maxPlayers", gameManager.getMaxPlayers());
        if (gameManager.getGameState() == GameState.WAITING) {
            state.addProperty("startCountdown", gameManager.getGameStartTask() != null
                    ? gameManager.getGameStartTask().getCountdown() + " seconds"
                    : "not running, " + (gameManager.canStart() ? "starts when the next player joins" : "needs players in at least two teams"));
        }
        GameTimeline timeline = gameManager.getTimeline();
        if (gameManager.getGameState() != GameState.WAITING) {
            state.addProperty("gameSeconds", timeline.getGameSeconds());
            JsonArray stages = new JsonArray();
            boolean first = true;
            for (TimelineEvent event : timeline.getUpcoming()) {
                JsonObject stage = new JsonObject();
                stage.addProperty("name", ChatColor.stripColor(event.getName()));
                if (first) {
                    stage.addProperty("secondsLeft", timeline.getSecondsLeft());
                } else {
                    stage.addProperty("seconds", event.getSeconds());
                }
                first = false;
                stages.add(stage);
            }
            state.add("stages", stages);
        }
        JsonArray teams = new JsonArray();
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            JsonObject team = new JsonObject();
            team.addProperty("team", gameTeam.getTeamColor().name());
            team.addProperty("name", ChatColor.stripColor(gameTeam.getName()));
            team.addProperty("bed", gameTeam.isDestroyed() ? "destroyed" : "standing");
            JsonArray players = new JsonArray();
            for (GamePlayer gamePlayer : gameTeam.getGamePlayers()) {
                players.add(new JsonPrimitive(gamePlayer.getName() + (gameTeam.getAlivePlayers().contains(gamePlayer) ? "" : " (out)")));
            }
            team.add("players", players);
            teams.add(team);
        }
        state.add("teams", teams);
        return state;
    }

    private JsonElement control(Args args) {
        GameManager gameManager = game();
        String action = args.string("action");
        switch (action) {
            case "start":
                if (gameManager.getGameState() != GameState.WAITING) {
                    throw new ToolError("The game is " + gameManager.getGameState() + ", only a waiting game can start");
                }
                if (!gameManager.canStart()) {
                    throw new ToolError("A game needs at least " + gameManager.getMapData().getPlayers().getMin()
                            + " players who can fill two teams, " + GamePlayer.getOnlinePlayers().size() + " are online."
                            + " A game with one team would end at once");
                }
                gameManager.start();
                return new JsonPrimitive(gameManager.getGameState() == GameState.RUNNING
                        ? "The game started" : "Another plugin cancelled the start");
            case "skip":
                if (gameManager.getGameState() != GameState.RUNNING) {
                    throw new ToolError("The game is " + gameManager.getGameState() + ", stages run only while it is RUNNING");
                }
                String skipped = gameManager.getTimeline().skip();
                if (skipped == null) {
                    throw new ToolError("No stage is left");
                }
                return new JsonPrimitive("The stage " + ChatColor.stripColor(skipped) + " runs within a second");
            case "end":
                if (gameManager.getGameState() != GameState.RUNNING) {
                    throw new ToolError("The game is " + gameManager.getGameState() + ", only a running game can end");
                }
                gameManager.getTimeline().endNow();
                return new JsonPrimitive("The game ended without a winner, the server shuts down after the results");
            default:
                throw new ToolError("action is start, skip or end");
        }
    }

    private JsonElement give(Args args) {
        Player player = Bukkit.getPlayerExact(args.string("player"));
        if (player == null) {
            throw new ToolError("Player " + args.string("player") + " is not online");
        }
        String id = args.string("resource");
        GeneratorConfig config = plugin.getGeneratorConfig();
        GeneratorConfig.Resource resource = config.resource(id);
        if (resource == null) {
            throw new ToolError("No resource " + id + ", resources: " + String.join(", ", config.getResources().keySet()));
        }
        int amount = args.integer("amount");
        if (amount < 1 || amount > MAX_GIVE) {
            throw new ToolError("amount must be between 1 and " + MAX_GIVE);
        }
        EconomyType economy = plugin.getGameManager() != null ? plugin.getGameManager().getEconomyType() : plugin.getSettingsConfig().getEconomyMode();
        if (economy == EconomyType.EXPERIENCE) {
            int levels = resource.getXpValue() * amount;
            player.setLevel(player.getLevel() + levels);
            return new JsonPrimitive("Gave " + player.getName() + " " + levels + " levels, now " + player.getLevel());
        }
        Material material = GeneratorManager.material(resource);
        if (material == null) {
            throw new ToolError("The material " + resource.getMaterial() + " of " + id + " does not exist on this server version");
        }
        int left = 0;
        for (ItemStack rest : player.getInventory().addItem(new ItemStack(material, amount)).values()) {
            left += rest.getAmount();
        }
        return new JsonPrimitive("Gave " + player.getName() + " " + (amount - left) + " " + material.name()
                + (left > 0 ? ", " + left + " did not fit the inventory" : ""));
    }
}
