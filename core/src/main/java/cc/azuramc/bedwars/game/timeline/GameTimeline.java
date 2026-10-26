package cc.azuramc.bedwars.game.timeline;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.game.BedwarsGameEndEvent;
import cc.azuramc.bedwars.config.object.TimelineConfig;
import cc.azuramc.bedwars.extension.ExtensionKind;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.spectator.task.SpectatorCompassTask;
import cc.azuramc.bedwars.game.task.GameOverTask;
import cc.azuramc.bedwars.game.task.GeneratorTask;
import cc.azuramc.bedwars.upgrade.UpgradeEffects;
import cc.azuramc.bedwars.util.MessageUtil;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class GameTimeline {

    private static final long RESULTS_DELAY_TICKS = 40L;

    private final GameManager gameManager;
    /** Captured when the game starts, so a reload never shifts a running game */
    private TimelineConfig config;
    private final List<TimelineEvent> events = new ArrayList<>();

    private BukkitTask task;
    private int index;
    private int secondsIntoEvent;
    /** Seconds since the start countdown finished */
    @Getter
    private int gameSeconds;
    @Getter
    private boolean ended;

    public GameTimeline(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void start() {
        if (task != null) {
            return;
        }
        config = AzuraBedWars.getInstance().getTimelineConfig();
        events.clear();
        events.add(new TimelineEvent(AzuraBedWars.getInstance().getLang().getGame().getStartStage(), config.getGameStartEvent().getCountDown(), this::showStartCountdown, this::startGenerators));
        for (TimelineConfig.Stage stage : config.getStages()) {
            events.add(new TimelineEvent(stage.getName(), stage.getAfter(), () -> stage.getActions().forEach(this::run)));
        }
        task = Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance().getPlugin(), this::tick, 0L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public String getCurrentEventName() {
        if (events.isEmpty()) {
            return "";
        }
        return events.get(Math.min(index, events.size() - 1)).getName();
    }

    public int getSecondsLeft() {
        return index < events.size() ? events.get(index).getSeconds() - secondsIntoEvent : 0;
    }

    /**
     * Events from the running one to the last
     */
    public List<TimelineEvent> getUpcoming() {
        return events.subList(Math.min(index, events.size()), events.size());
    }

    /**
     * Runs the current event on the next tick instead of waiting for it
     *
     * @return the event name, null when none is left
     */
    @Nullable
    public String skip() {
        if (task == null || ended || index >= events.size()) {
            return null;
        }
        secondsIntoEvent = events.get(index).getSeconds();
        return events.get(index).getName();
    }

    /**
     * Ends the game without a winner, like the end-game timeline action
     */
    public void endNow() {
        if (!ended) {
            endWithoutWinner();
        }
    }

    private void tick() {
        if (index < events.size()) {
            TimelineEvent event = events.get(index);
            if (event.getCountdown() != null) {
                event.getCountdown().accept(event.getSeconds() - secondsIntoEvent);
            }
            if (secondsIntoEvent >= event.getSeconds()) {
                index++;
                secondsIntoEvent = 0;
                event.getAction().run();
            }
        }

        if (!ended && gameManager.isOver()) {
            end();
            Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance().getPlugin(), this::showResults, RESULTS_DELAY_TICKS);
        }

        AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
        UpgradeEffects.tick(gameManager);
        showBaseDistance();

        secondsIntoEvent++;
        if (index > 0) {
            gameSeconds++;
        }
    }

    private void end() {
        ended = true;
        index = events.size();
        gameManager.end();
        Bukkit.getPluginManager().callEvent(new BedwarsGameEndEvent(gameManager));
    }

    private void endWithoutWinner() {
        end();
        showResults();
    }

    private void showResults() {
        new GameOverTask(gameManager);
    }

    private void showStartCountdown(int secondsLeft) {
        TimelineConfig.GameStartEvent start = config.getGameStartEvent();
        gameManager.broadcastSound(XSound.UI_BUTTON_CLICK.get(), 1f, 1f);
        gameManager.broadcastTitleToAll(start.getTitleString(), start.getSubtitle() + secondsLeft,
                start.getFadeIn(), start.getTitleStay(), start.getFadeOut());
    }

    private void startGenerators() {
        new GeneratorTask(gameManager).start();
        new SpectatorCompassTask().start();
    }

    private void run(TimelineConfig.Action action) {
        switch (action.getType()) {
            case TimelineConfig.Action.GENERATOR_LEVEL:
                if (action.getResource() != null && action.getLevel() != null) {
                    gameManager.getGeneratorManager().setPublicLevel(action.getResource(), action.getLevel());
                }
                break;
            case TimelineConfig.Action.DESTROY_BEDS:
                for (GameTeam gameTeam : gameManager.getGameTeams()) {
                    if (!gameTeam.isDestroyed()) {
                        gameTeam.setDestroyed(true);
                    }
                }
                break;
            case TimelineConfig.Action.END_GAME:
                if (!ended) {
                    endWithoutWinner();
                }
                break;
            case TimelineConfig.Action.TITLE:
                gameManager.broadcastTitleToAll(Objects.toString(action.getTitle(), ""), Objects.toString(action.getSubtitle(), ""),
                        orDefault(action.getFadeIn(), 10), orDefault(action.getStay(), 40), orDefault(action.getFadeOut(), 10));
                break;
            case TimelineConfig.Action.SOUND:
                Sound sound = action.getSound() == null ? null : XSound.of(action.getSound()).map(XSound::get).orElse(null);
                if (sound != null) {
                    gameManager.broadcastSound(sound, action.getVolume() != null ? action.getVolume() : 1,
                            action.getPitch() != null ? action.getPitch() : 1);
                }
                break;
            case TimelineConfig.Action.MESSAGE:
                if (action.getMessage() != null) {
                    gameManager.broadcastMessage(action.getMessage());
                }
                break;
            case TimelineConfig.Action.COMMAND:
                if (action.getCommands() != null) {
                    action.getCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
                }
                break;
            default:
                if (ExtensionKind.isReference(action.getType())) {
                    AzuraBedWars.getInstance().getExtensions().runTimeline(action.getType(), action.getSettings(), gameManager);
                }
                break;
        }
    }

    private static int orDefault(@Nullable Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    private void showBaseDistance() {
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            Location spawn = gameTeam.getSpawnLocation();
            gameTeam.getAlivePlayers().forEach(gamePlayer -> {
                Player player = gamePlayer.getPlayer();
                if (player == null || !Objects.equals(player.getWorld(), spawn.getWorld())) {
                    return;
                }
                int distance = (int) player.getLocation().distance(spawn);
                gamePlayer.sendActionBar(MessageUtil.fill(AzuraBedWars.getInstance().getLang().getGame().getBaseTracker(),
                        "team", gameTeam.getName(), "distance", gameTeam.getChatColor().toString() + distance));
            });
        }
    }
}
