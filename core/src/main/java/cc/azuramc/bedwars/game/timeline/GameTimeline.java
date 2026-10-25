package cc.azuramc.bedwars.game.timeline;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.game.BedwarsGameEndEvent;
import cc.azuramc.bedwars.config.object.EventSettingsConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.spectator.task.SpectatorCompassTask;
import cc.azuramc.bedwars.game.task.GameOverTask;
import cc.azuramc.bedwars.game.task.GeneratorTask;
import cc.azuramc.bedwars.game.task.generator.GeneratorManager;
import cc.azuramc.bedwars.game.task.generator.PublicResourceGenerator;
import cc.azuramc.bedwars.upgrade.UpgradeEffects;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public class GameTimeline {

    private static final long RESULTS_DELAY_TICKS = 40L;

    private final GameManager gameManager;
    private final EventSettingsConfig config;
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
        this.config = AzuraBedWars.getInstance().getEventSettingsConfig();

        EventSettingsConfig.DiamondUpdateEvent diamond = config.getDiamondUpdateEvent();
        EventSettingsConfig.EmeraldUpdateEvent emerald = config.getEmeraldUpdateEvent();
        events.add(new TimelineEvent("开始游戏", config.getGameStartEvent().getCountDown(), this::showStartCountdown, this::startGenerators));
        events.add(new TimelineEvent("钻石资源点升级到II级", 360, () -> upgradeGenerator("钻石", 2, diamond.getLevel2RefreshSecond())));
        events.add(new TimelineEvent("绿宝石升级到II级", 360, () -> upgradeGenerator("绿宝石", 2, emerald.getLevel2RefreshSecond())));
        events.add(new TimelineEvent("钻石资源点升级到III级", 360, () -> upgradeGenerator("钻石", 3, diamond.getLevel3RefreshSecond())));
        events.add(new TimelineEvent("绿宝石升级到III级", 360, () -> upgradeGenerator("绿宝石", 3, emerald.getLevel3RefreshSecond())));
        events.add(new TimelineEvent("床自毁", config.getBedDestroyedEvent().getExecuteSeconds(), this::destroyAllBeds));
        events.add(new TimelineEvent("游戏结束", config.getGameOverEvent().getExecuteSeconds(), this::endWithoutWinner));
    }

    public void start() {
        if (task == null) {
            task = Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance().getPlugin(), this::tick, 0L, 20L);
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public String getCurrentEventName() {
        return events.get(Math.min(index, events.size() - 1)).getName();
    }

    public int getSecondsLeft() {
        return index < events.size() ? events.get(index).getSeconds() - secondsIntoEvent : 0;
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
        EventSettingsConfig.GameStartEvent start = config.getGameStartEvent();
        gameManager.broadcastSound(XSound.UI_BUTTON_CLICK.get(), 1f, 1f);
        gameManager.broadcastTitleToAll(start.getTitleString(), start.getSubtitle() + secondsLeft,
                start.getFadeIn(), start.getTitleStay(), start.getFadeOut());
    }

    private void startGenerators() {
        new GeneratorTask(gameManager).start();
        new SpectatorCompassTask().start();
    }

    private void upgradeGenerator(String resource, int level, int refreshSeconds) {
        GeneratorManager generatorManager = gameManager.getGeneratorManager();
        PublicResourceGenerator generator = generatorManager.getPublicResourceGenerator(resource);
        if (generator == null) {
            return;
        }
        generator.setInterval(refreshSeconds * 20L);
        generator.setLevel(level);
        generator.setMaxStack(generatorManager.getMaxStackForResource(resource, level));
        generator.restartTask();
    }

    private void destroyAllBeds() {
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            if (!gameTeam.isDestroyed()) {
                gameTeam.setDestroyed(true);
            }
        }

        EventSettingsConfig.BedDestroyedEvent bedDestroyed = config.getBedDestroyedEvent();
        gameManager.broadcastSound(XSound.ENTITY_ENDER_DRAGON_GROWL.get(), 1, 1);
        gameManager.broadcastTitleToAll(bedDestroyed.getTitle(), bedDestroyed.getSubtitle(),
                bedDestroyed.getFadeIn(), bedDestroyed.getTitleStay(), bedDestroyed.getFadeOut());
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
                gamePlayer.sendActionBar("§f队伍: " + gameTeam.getChatColor() + gameTeam.getName()
                        + "§f 追踪: " + gameTeam.getChatColor() + distance + "m");
            });
        }
    }
}
