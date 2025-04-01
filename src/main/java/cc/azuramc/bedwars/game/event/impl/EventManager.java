package cc.azuramc.bedwars.game.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.event.GameEvent;
import cc.azuramc.bedwars.game.event.Runnable;
import cc.azuramc.bedwars.game.event.StartEvent;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 游戏事件管理器
 * 负责注册、调度和执行游戏中的各种事件
 */
public class EventManager extends TimerTask {
    // 事件注册的优先级常量
    private static final int START_EVENT_PRIORITY = 0;
    private static final int DIAMOND_LEVEL2_PRIORITY = 1;
    private static final int EMERALD_LEVEL2_PRIORITY = 2;
    private static final int DIAMOND_LEVEL3_PRIORITY = 3;
    private static final int EMERALD_LEVEL3_PRIORITY = 4;
    private static final int BED_DESTROYED_PRIORITY = 5;
    private static final int OVER_EVENT_PRIORITY = 6;
    private static final int END_EVENT_PRIORITY = 7;
    
    // 游戏结束强制跳转延迟（刻）
    private static final long GAME_OVER_DELAY_TICKS = 40L;
    
    // 计时器执行间隔（毫秒）
    private static final long TIMER_PERIOD_MS = 1000;
    
    // 事件持续时间和资源刷新时间常量（秒）
    private static final int EVENT_DURATION_SECONDS = 360;

    private final Game game;
    
    // 定时任务和事件映射
    @Getter
    private final HashMap<String, cc.azuramc.bedwars.game.event.Runnable> runnables = new HashMap<>();
    private final HashMap<Integer, GameEvent> events = new HashMap<>();
    
    // 计时器和状态变量
    private Timer timer;
    private int currentEvent = 0;
    
    @Getter
    private int seconds = 0;
    
    @Getter
    private boolean over = false;

    /**
     * 创建事件管理器
     * 
     * @param game 游戏实例
     */
    public EventManager(Game game) {
        this.game = game;
        
        // 注册所有游戏事件
        registerGameEvents();
    }
    
    /**
     * 注册游戏中的所有事件
     */
    private void registerGameEvents() {
        this.registerEvent(new StartEvent());
        this.registerEvent(new DiamondUpdateEvent(2, EVENT_DURATION_SECONDS, DIAMOND_LEVEL2_PRIORITY));
        this.registerEvent(new EmeraldUpdateEvent(2, EVENT_DURATION_SECONDS, EMERALD_LEVEL2_PRIORITY));
        this.registerEvent(new DiamondUpdateEvent(3, EVENT_DURATION_SECONDS, DIAMOND_LEVEL3_PRIORITY));
        this.registerEvent(new EmeraldUpdateEvent(3, EVENT_DURATION_SECONDS, EMERALD_LEVEL3_PRIORITY));
        this.registerEvent(new BedDestroyedEvent());
        this.registerEvent(new OverEvent());
        this.registerEvent(new EndEvent());
    }

    /**
     * 计时器主循环，每秒执行一次
     * 处理事件的执行和进度
     */
    @Override
    public void run() {
        // 获取当前事件
        GameEvent event = this.currentEvent();

        // 执行事件倒计时回调
        int remainingSeconds = event.getExcuteSeconds() - seconds;
        event.excuteRunnbale(this.game, remainingSeconds);
        
        // 检查事件是否应该执行
        if (this.seconds >= event.getExcuteSeconds()) {
            this.seconds = 0;
            this.currentEvent = event.getPriority() + 1;
            event.excute(this.game);
        }

        handleGameOver();

        updatePlayerTracking();

        processRunnableTasks();

        ++this.seconds;
    }
    
    /**
     * 处理游戏结束逻辑
     */
    private void handleGameOver() {
        if (game.isOver() && !over) {
            Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance(), () -> {
                setCurrentEvent(OVER_EVENT_PRIORITY);
                currentEvent().excute(game);
            }, GAME_OVER_DELAY_TICKS);
            over = true;
        }
    }
    
    /**
     * 更新玩家位置追踪信息
     */
    private void updatePlayerTracking() {
        for (GameTeam gameTeam : game.getGameTeams()) {
            gameTeam.getAlivePlayers().forEach(player -> {
                if (Objects.equals(player.getPlayer().getLocation().getWorld(), gameTeam.getSpawn().getWorld())) {
                    int distance = (int) player.getPlayer().getLocation().distance(gameTeam.getSpawn());
                    String trackingMessage = "§f队伍: " + gameTeam.getChatColor() + gameTeam.getName() + 
                                           "§f 追踪: " + gameTeam.getChatColor() + distance + "m";
                    player.sendActionBar(trackingMessage);
                }
            });
        }
    }
    
    /**
     * 处理周期性任务
     */
    private void processRunnableTasks() {
        runnables.values().forEach(runnable -> {
            if (runnable.getSeconds() != 0) {
                // 周期性任务
                if (runnable.getNextSeconds() == runnable.getSeconds()) {
                    runnable.getEvent().run(0, currentEvent);
                    runnable.setNextSeconds(0);
                }
                runnable.setNextSeconds(runnable.getNextSeconds() + 1);
            } else {
                // 持续性任务
                runnable.getEvent().run(seconds, currentEvent);
            }
        });
    }

    /**
     * 获取当前事件
     * 
     * @return 当前事件
     */
    public GameEvent currentEvent() {
        return this.events.getOrDefault(this.currentEvent, this.events.get(OVER_EVENT_PRIORITY));
    }

    /**
     * 设置当前事件
     * 
     * @param priority 事件优先级
     */
    public void setCurrentEvent(int priority) {
        this.seconds = 0;
        this.currentEvent = priority;
    }

    /**
     * 获取当前事件剩余时间
     * 
     * @return 剩余时间（秒）
     */
    public int getLeftTime() {
        return this.currentEvent().getExcuteSeconds() - this.seconds;
    }

    /**
     * 获取下一个事件的格式化名称
     * 
     * @return 事件名称
     */
    public String formattedNextEvent() {
        GameEvent currentEvent = this.currentEvent();
        return currentEvent.getName();
    }

    /**
     * 注册无延迟执行的任务
     * 
     * @param name 任务名称
     * @param runnable 任务执行逻辑
     */
    public void registerRunnable(String name, cc.azuramc.bedwars.game.event.Runnable.Event runnable) {
        this.runnables.put(name, new cc.azuramc.bedwars.game.event.Runnable(0, 0, runnable));
    }

    /**
     * 注册周期性执行的任务
     * 
     * @param name 任务名称
     * @param runnable 任务执行逻辑
     * @param seconds 执行周期（秒）
     */
    public void registerRunnable(String name, cc.azuramc.bedwars.game.event.Runnable.Event runnable, int seconds) {
        this.runnables.put(name, new Runnable(seconds, 0, runnable));
    }

    /**
     * 注册游戏事件
     * 
     * @param event 事件实例
     */
    private void registerEvent(GameEvent event) {
        this.events.put(event.getPriority(), event);
    }

    /**
     * 启动事件管理器
     */
    public void start() {
        if (this.timer == null) {
            timer = new Timer();
            timer.schedule(this, 0, TIMER_PERIOD_MS);
        }
    }

    /**
     * 停止事件管理器
     */
    public void stop() {
        if (this.timer != null) {
            timer.cancel();
            this.currentEvent = 0;
            this.seconds = 0;
        }
    }
}
