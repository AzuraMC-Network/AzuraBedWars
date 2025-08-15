package cc.azuramc.bedwars.event;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.game.BedwarsGameEndEvent;
import cc.azuramc.bedwars.event.impl.*;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.LoggerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 游戏事件管理器
 * 负责注册、调度和执行游戏中的各种事件
 *
 * @author an5w1r@163.com
 */
public class GameEventManager implements Runnable {
    /**
     * 事件注册的优先级常量
     */
    private static final int START_EVENT_PRIORITY = 0;
    private static final int DIAMOND_LEVEL2_PRIORITY = 1;
    private static final int EMERALD_LEVEL2_PRIORITY = 2;
    private static final int DIAMOND_LEVEL3_PRIORITY = 3;
    private static final int EMERALD_LEVEL3_PRIORITY = 4;
    private static final int BED_DESTROYED_PRIORITY = 5;
    private static final int OVER_EVENT_PRIORITY = 6;
    private static final int END_EVENT_PRIORITY = 7;

    /**
     * 游戏结束强制跳转延迟（刻）
     */
    private static final long GAME_OVER_DELAY_TICKS = 40L;

    /**
     * 计时器执行间隔（毫秒）
     */
    private static final long TIMER_PERIOD_MS = 1000;

    /**
     * 事件持续时间和资源刷新时间常量（秒）
     */
    private static final int EVENT_DURATION_SECONDS = 360;

    /**
     * 线程池参数
     */
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAX_QUEUE_SIZE = 100;

    /**
     * 停止事件管理器等待任务执行超时间 （秒）
     */
    private static final int AWAIT_TERMINATION_TIMEOUT = 5;

    private final GameManager gameManager;

    /**
     * 定时任务和事件映射
     */
    @Getter
    private final HashMap<String, GameEventRunnable> runnable = new HashMap<>();
    private final HashMap<Integer, AbstractGameEvent> events = new HashMap<>();

    /**
     * 使用ScheduledThreadPoolExecutor替代Executors
     */
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;
    private int currentEvent = 0;

    @Getter
    private int seconds = 0;

    @Getter
    private boolean over = false;

    /**
     * 创建事件管理器
     *
     * @param gameManager 游戏实例
     */
    public GameEventManager(GameManager gameManager) {
        this.gameManager = gameManager;

        // 注册所有游戏事件
        registerGameEvents();
    }

    /**
     * 注册游戏中的所有事件
     */
    private void registerGameEvents() {
        this.registerEvent(new GameStartEvent());
        this.registerEvent(new DiamondUpdateEvent(2, EVENT_DURATION_SECONDS, DIAMOND_LEVEL2_PRIORITY));
        this.registerEvent(new EmeraldUpdateEvent(2, EVENT_DURATION_SECONDS, EMERALD_LEVEL2_PRIORITY));
        this.registerEvent(new DiamondUpdateEvent(3, EVENT_DURATION_SECONDS, DIAMOND_LEVEL3_PRIORITY));
        this.registerEvent(new EmeraldUpdateEvent(3, EVENT_DURATION_SECONDS, EMERALD_LEVEL3_PRIORITY));
        this.registerEvent(new BedDestroyedEvent());
        this.registerEvent(new GameOverEvent());
        this.registerEvent(new GameShutdownEvent());
    }

    /**
     * 计时器主循环，每秒执行一次
     * 处理事件的执行和进度
     */
    @Override
    public void run() {
        try {
            // 获取当前事件
            AbstractGameEvent event = this.currentEvent();

            // 执行事件倒计时回调
            int remainingSeconds = event.getExecuteSeconds() - seconds;
            event.executeRunnable(this.gameManager, remainingSeconds);

            // 检查事件是否应该执行
            if (this.seconds >= event.getExecuteSeconds()) {
                this.seconds = 0;
                this.currentEvent = event.getPriority() + 1;
                event.execute(this.gameManager);
            }

            handleGameOver();

            updatePlayerTracking();

            processRunnableTasks();

            ++this.seconds;
        } catch (Exception e) {
            // 捕获并记录异常，但不中断定时任务的执行
            LoggerUtil.error("游戏事件管理器执行错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理游戏结束逻辑
     */
    private void handleGameOver() {
        if (gameManager.isOver() && !over) {

            // 触发游戏结束事件
            BedwarsGameEndEvent gameEndEvent = new BedwarsGameEndEvent(gameManager);
            Bukkit.getPluginManager().callEvent(gameEndEvent);

            // 设置GameState
            gameManager.setGameState(GameState.ENDING);

            Bukkit.getScheduler().runTaskLater(AzuraBedWars.getInstance(), () -> {
                setCurrentEvent(OVER_EVENT_PRIORITY);
                currentEvent().execute(gameManager);
            }, GAME_OVER_DELAY_TICKS);
            over = true;
        }
    }

    /**
     * 更新玩家位置追踪信息
     */
    private void updatePlayerTracking() {
        try {
            for (GameTeam gameTeam : gameManager.getGameTeams()) {
                gameTeam.getAlivePlayers().forEach(player -> {
                    if (Objects.equals(player.getPlayer().getLocation().getWorld(), gameTeam.getSpawnLocation().getWorld())) {
                        int distance = (int) player.getPlayer().getLocation().distance(gameTeam.getSpawnLocation());
                        String trackingMessage = "§f队伍: " + gameTeam.getChatColor() + gameTeam.getName() +
                                "§f 追踪: " + gameTeam.getChatColor() + distance + "m";
                        player.sendActionBar(trackingMessage);
                    }
                });
            }
        } catch (Exception e) {
            LoggerUtil.warn("玩家追踪更新错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理周期性任务
     */
    private void processRunnableTasks() {
        runnable.forEach((name, task) -> {
            try {
                if (task.getSeconds() != 0) {
                    // 周期性任务
                    if (task.getNextSeconds() == task.getSeconds()) {
                        task.getEvent().run(0, this.currentEvent);
                        task.setNextSeconds(0);
                    }
                    task.setNextSeconds(task.getNextSeconds() + 1);
                } else {
                    // 持续性任务
                    task.getEvent().run(seconds, this.currentEvent);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LoggerUtil.warn("任务 '" + name + "' 执行错误: " + e.getMessage());
            }
        });
    }

    /**
     * 获取当前事件
     *
     * @return 当前事件
     */
    public AbstractGameEvent currentEvent() {
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
        return this.currentEvent().getExecuteSeconds() - this.seconds;
    }

    /**
     * 获取下一个事件的格式化名称
     *
     * @return 事件名称
     */
    public String formattedNextEvent() {
        AbstractGameEvent currentEvent = this.currentEvent();
        return currentEvent.getName();
    }

    /**
     * 注册无延迟执行的任务
     *
     * @param name     任务名称
     * @param runnable 任务执行逻辑
     */
    public void registerRunnable(String name, GameEventRunnable.Event runnable) {
        this.runnable.put(name, new GameEventRunnable(0, 0, runnable));
    }

    /**
     * 注册周期性执行的任务
     *
     * @param name     任务名称
     * @param runnable 任务执行逻辑
     * @param seconds  执行周期（秒）
     */
    public void registerRunnable(String name, GameEventRunnable.Event runnable, int seconds) {
        this.runnable.put(name, new GameEventRunnable(seconds, 0, runnable));
    }

    /**
     * 注册游戏事件
     *
     * @param event 事件实例
     */
    private void registerEvent(AbstractGameEvent event) {
        this.events.put(event.getPriority(), event);
    }

    /**
     * 启动事件管理器
     */
    public void start() {
        if (this.scheduler == null) {
            // 创建线程工厂
            ThreadFactory threadFactory = r -> {
                Thread thread = new Thread(r, "GameEventManager-Thread");
                thread.setDaemon(true); // 设置为守护线程，便于JVM退出
                return thread;
            };

            // 创建拒绝策略处理器
            RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

            // 创建ScheduledThreadPoolExecutor
            this.scheduler = new ScheduledThreadPoolExecutor(
                    CORE_POOL_SIZE,
                    threadFactory,
                    handler
            );

            // 设置队列大小限制
            ((ScheduledThreadPoolExecutor) scheduler).setMaximumPoolSize(CORE_POOL_SIZE);

            // 调度执行任务
            this.scheduledFuture = scheduler.scheduleAtFixedRate(
                    this, 0, TIMER_PERIOD_MS, TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * 停止事件管理器
     */
    public void stop() {
        if (this.scheduler != null) {
            // 取消当前执行的任务
            if (this.scheduledFuture != null) {
                this.scheduledFuture.cancel(false);
            }

            // 关闭调度器
            this.scheduler.shutdown();
            try {
                // 等待任务完成
                if (!this.scheduler.awaitTermination(AWAIT_TERMINATION_TIMEOUT, TimeUnit.SECONDS)) {
                    this.scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                this.scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }

            this.scheduler = null;
            this.currentEvent = 0;
            this.seconds = 0;
        }
    }
}
