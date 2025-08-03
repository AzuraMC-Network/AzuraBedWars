package cc.azuramc.bedwars.game;

import lombok.Getter;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Silverfish;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author An5w1r@163.com
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

    /**
     * 构造函数
     *
     * @param livingEntity 生物实体
     * @param summoner   召唤玩家 (GamePlayer)
     * @param liveLeftTime      消失倒计时（0表示使用默认值）
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
     * 刷新实体状态，处理生命周期
     */
    public void refresh() {
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
    private void updateEntityName() {
        if (gameTeam == null) {
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
                .replace("{Health}", String.valueOf((int) (livingEntity.getHealth() + 0.5)));

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
     * 检查实体是否仍然有效
     */
    public boolean isValid() {
        return livingEntity != null && !livingEntity.isDead() && gameTeam != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (obj instanceof CustomEntityManager other) {
            return Objects.equals(this.uuid, other.uuid);
        }

        if (obj instanceof LivingEntity entity) {
            return Objects.equals(this.uuid, entity.getUniqueId());
        }

        return false;
    }
}
