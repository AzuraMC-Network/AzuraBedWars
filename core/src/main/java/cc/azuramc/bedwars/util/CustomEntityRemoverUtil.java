package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.game.GameTeam;
import lombok.Getter;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Silverfish;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomEntityRemoverUtil {

    @Getter
    private static final ConcurrentHashMap<UUID, CustomEntityRemoverUtil> despawnables = new ConcurrentHashMap<>();

    @Getter
    private LivingEntity livingEntity;
    @Getter
    private GameTeam gameTeam;
    @Getter
    private int despawn = 250;
    private UUID uuid;

    public CustomEntityRemoverUtil(LivingEntity livingEntity, GameTeam gameTeam, int despawn) {
        this.livingEntity = livingEntity;
        if (livingEntity == null) return;
        this.uuid = livingEntity.getUniqueId();
        this.gameTeam = gameTeam;
        if (despawn != 0) {
            this.despawn = despawn;
        }
        getDespawnables().put(uuid, this);
        this.setName();
    }

    public void refresh() {
        if (livingEntity.isDead() || livingEntity == null || gameTeam == null) {
            getDespawnables().remove(uuid);
            if (gameTeam == null) {
                if (livingEntity != null) {
                    livingEntity.damage(livingEntity.getHealth() + 100);
                }
            }
            return;
        }
        setName();
        despawn--;
        if (despawn == 0) {
            livingEntity.damage(livingEntity.getHealth() + 100);
            getDespawnables().remove(livingEntity.getUniqueId());
        }
    }

    private void setName() {
        String name = "";
        if (livingEntity instanceof IronGolem) {
            name = "§c{Health}❤ {TeamColor}{TeamName}队守卫 §e{Countdown}s";
        } else if (livingEntity instanceof Silverfish) {
            name = "{TeamColor}{TeamName}队蠹虫 §e{Countdown}s";
        }

        if (gameTeam != null) {
            name = name.replace("{TeamColor}", gameTeam.getChatColor().toString())
                    .replace("{TeamName}", gameTeam.getName())
                    .replace("{Countdown}", "" + despawn)
                    .replace("{Health}", "" + (int) livingEntity.getHealth());
        }

        livingEntity.setCustomName(name);
    }

    public void destroy() {
        if (getLivingEntity() != null) {
            getLivingEntity().damage(Integer.MAX_VALUE);
        }
        gameTeam = null;
        getDespawnables().remove(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LivingEntity) return ((LivingEntity) obj).getUniqueId().equals(livingEntity.getUniqueId());
        return false;
    }
}
