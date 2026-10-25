package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.nms.MobGoals;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author an5w1r@163.com
 */
public class CustomEntityManager {

    private static final int DEFAULT_DESPAWN_TIME = 250;
    private static final int KILL_DAMAGE = Integer.MAX_VALUE;

    // 实体名称模板
    private static final String IRON_GOLEM_NAME_TEMPLATE = "§c{Health}❤ {TeamColor}{TeamName}队守卫 §e{Countdown}s";
    private static final String SILVERFISH_NAME_TEMPLATE = "{TeamColor}{TeamName}队蠹虫 §e{Countdown}s";

    @Getter
    private static final ConcurrentHashMap<UUID, CustomEntityManager> customEntityMap = new ConcurrentHashMap<>();

    @Getter
    private final LivingEntity livingEntity;

    @Getter
    private GamePlayer summoner;

    @Getter
    private GameTeam gameTeam;

    @Getter
    private int liveLeftTime;

    @Getter
    private UUID uuid;

    private BukkitTask refreshTask;

    /**
     * 构造函数
     *
     * @param livingEntity 生物实体
     * @param summoner     召唤玩家 (GamePlayer)
     * @param liveLeftTime 消失倒计时（0表示使用默认值）
     */
    public CustomEntityManager(LivingEntity livingEntity, GamePlayer summoner, int liveLeftTime) {
        this.livingEntity = livingEntity;

        if (livingEntity == null) {
            return;
        }

        this.uuid = livingEntity.getUniqueId();
        this.summoner = summoner;
        this.gameTeam = summoner.getGameTeam();
        this.liveLeftTime = (liveLeftTime > 0) ? liveLeftTime : DEFAULT_DESPAWN_TIME;

        // 注册到管理器
        customEntityMap.put(uuid, this);
        updateEntityName();

        startRefreshTask();
    }

    public static void spawnIronGolem(Location location, GamePlayer summoner, int liveLeftTime) {
        IronGolem golem = location.getWorld().spawn(location, IronGolem.class);
        setupGoals(golem, new CustomEntityManager(golem, summoner, liveLeftTime), 1.5D, 1.0D);
    }

    public static void spawnSilverfish(Location location, GamePlayer summoner, int liveLeftTime) {
        Silverfish silverfish = location.getWorld().spawn(location, Silverfish.class);
        setupGoals(silverfish, new CustomEntityManager(silverfish, summoner, liveLeftTime), 1.9D, 1.5D);
    }

    private static void setupGoals(Creature creature, CustomEntityManager manager, double attackSpeed, double strollSpeed) {
        creature.setCustomNameVisible(true);
        creature.setRemoveWhenFarAway(false);

        MobGoals goals = AzuraBedWars.getInstance().getNmsAccess().getGoals(creature);
        if (goals == null) {
            return;
        }

        goals.clearGoals()
                .floatInWater(1)
                .meleeAttack(2, attackSpeed)
                .moveTowardsTarget(3, 1.0D, 20.0F)
                .randomStroll(4, strollSpeed)
                .randomLookAround(5)
                .clearTargets()
                .hurtByTarget(1)
                .nearestTarget(2, Player.class, manager::isEnemy)
                .nearestTarget(3, IronGolem.class, manager::isEnemy)
                .nearestTarget(4, Silverfish.class, manager::isEnemy);
    }

    /**
     * 根据UUID获取管理器实例
     */
    public static CustomEntityManager getByUUID(UUID uuid) {
        return customEntityMap.get(uuid);
    }

    /**
     * 根据实体获取管理器实例
     */
    public static CustomEntityManager getByEntity(LivingEntity entity) {
        return entity != null ? customEntityMap.get(entity.getUniqueId()) : null;
    }

    /**
     * 清理所有已死亡或无效的实体
     */
    public static void cleanupInvalidEntities() {
        customEntityMap.entrySet().removeIf(entry -> {
            CustomEntityManager manager = entry.getValue();
            if (!manager.isValid()) {
                manager.cleanup();
                return true;
            }
            return false;
        });
    }

    /**
     * 启动自己的刷新任务
     */
    private void startRefreshTask() {
        refreshTask = Bukkit.getScheduler().runTaskTimer(
                AzuraBedWars.getInstance().getPlugin(),
                this::refresh,
                0,
                20L
        );
    }

    /**
     * 刷新实体状态，处理生命周期
     * 每秒自动调用一次
     */
    private void refresh() {
        // 检查实体是否应该被移除
        if (shouldRemoveEntity()) {
            handleEntityRemoval();
            return;
        }

        // 更新实体状态
        liveLeftTime--;
        updateEntityName();

        // 检查是否到达消失时间
        if (liveLeftTime <= 0) {
            destroyEntity();
        }
    }

    /**
     * 检查实体是否应该被移除
     */
    private boolean shouldRemoveEntity() {
        return livingEntity == null || livingEntity.isDead() || gameTeam == null;
    }

    /**
     * 处理实体移除逻辑
     */
    private void handleEntityRemoval() {
        customEntityMap.remove(uuid);

        // 如果队伍为空但实体存在，强制杀死实体
        if (gameTeam == null && livingEntity != null && !livingEntity.isDead()) {
            killEntity(livingEntity);
        }
    }

    /**
     * 更新实体显示名称
     */
    public void updateEntityName() {
        if (gameTeam == null || livingEntity == null || livingEntity.isDead()) {
            return;
        }

        String nameTemplate = getNameTemplate();
        if (nameTemplate.isEmpty()) {
            return;
        }

        String displayName = nameTemplate
                .replace("{TeamColor}", gameTeam.getChatColor().toString())
                .replace("{TeamName}", gameTeam.getName())
                .replace("{Countdown}", String.valueOf(liveLeftTime))
                .replace("{Health}", String.valueOf(Math.round((float) livingEntity.getHealth())));

        livingEntity.setCustomName(displayName);
    }

    /**
     * 根据实体类型获取名称模板
     */
    private String getNameTemplate() {
        if (livingEntity instanceof IronGolem) {
            return IRON_GOLEM_NAME_TEMPLATE;
        } else if (livingEntity instanceof Silverfish) {
            return SILVERFISH_NAME_TEMPLATE;
        }
        return "";
    }

    /**
     * 销毁实体并清理资源
     */
    public void destroy() {
        destroyEntity();
    }

    /**
     * 内部销毁实体方法
     */
    private void destroyEntity() {
        if (livingEntity != null && !livingEntity.isDead()) {
            killEntity(livingEntity);
        }
        cleanup();
    }

    /**
     * 清理资源
     */
    private void cleanup() {
        if (refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel();
            refreshTask = null;
        }

        gameTeam = null;
        customEntityMap.remove(uuid);
    }

    /**
     * 安全地杀死实体
     */
    private void killEntity(LivingEntity entity) {
        try {
            entity.damage(KILL_DAMAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取剩余生存时间
     */
    public int getRemainingTime() {
        return liveLeftTime;
    }

    /**
     * Enemy players and custom entities of other teams
     */
    public boolean isEnemy(Entity target) {
        if (gameTeam == null) {
            return false;
        }

        if (target instanceof Player) {
            Player player = (Player) target;
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            return gamePlayer != null && !gamePlayer.isSpectator() && gamePlayer.getGameTeam() != gameTeam;
        }

        CustomEntityManager other = customEntityMap.get(target.getUniqueId());
        return other != null && other.gameTeam != null && other.gameTeam != gameTeam;
    }

    /**
     * 检查实体是否仍然有效
     */
    public boolean isValid() {
        return livingEntity != null && !livingEntity.isDead() && gameTeam != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (obj instanceof CustomEntityManager) {
            CustomEntityManager other = (CustomEntityManager) obj;
            return Objects.equals(this.uuid, other.uuid);
        }

        if (obj instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) obj;
            return Objects.equals(this.uuid, entity.getUniqueId());
        }

        return false;
    }
}
