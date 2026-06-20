package cc.azuramc.bedwars.nms.v1_19_R3;

import cc.azuramc.bedwars.api.AzuraBedWarsAPI;
import net.minecraft.world.entity.EntityInsentient;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 * @author an5w1r@163.com
 */
public class EntityConverter {

    /**
     * 在指定位置生成Bukkit实体
     *
     * @param location   生成位置
     * @param entityType 实体类型
     * @return 生成的Bukkit实体
     * @throws IllegalArgumentException 如果世界为null
     */
    public static Entity spawnBukkitEntity(Location location, EntityType entityType) {
        World world = location.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("世界不能为null");
        }
        return world.spawnEntity(location, entityType);
    }

    /**
     * 将Bukkit实体转换为NMS EntityInsentient
     *
     * @param bukkitEntity Bukkit实体
     * @return NMS EntityInsentient实体，如果转换失败返回null
     */
    public static EntityInsentient bukkitToNms(Entity bukkitEntity) {
        if (!(bukkitEntity instanceof CraftEntity craftEntity)) {
            AzuraBedWarsAPI.warn("实体不是CraftEntity类型: " + bukkitEntity.getClass().getSimpleName());
            return null;
        }

        net.minecraft.world.entity.Entity nmsEntity = craftEntity.getHandle();

        if (!(nmsEntity instanceof EntityInsentient)) {
            AzuraBedWarsAPI.warn("NMS实体不是EntityInsentient类型: " + nmsEntity.getClass().getSimpleName());
            return null;
        }

        return (EntityInsentient) nmsEntity;
    }

    /**
     * 生成实体并转换为NMS EntityInsentient
     *
     * @param location   生成位置
     * @param entityType 实体类型
     * @return NMS EntityInsentient实体，如果生成或转换失败返回null
     */
    public static EntityInsentient spawnAndConvert(Location location, EntityType entityType) {
        try {
            Entity bukkitEntity = spawnBukkitEntity(location, entityType);
            return bukkitToNms(bukkitEntity);
        } catch (Exception e) {
            AzuraBedWarsAPI.error("生成并转换实体失败", e);
            return null;
        }
    }

    /**
     * 检查实体是否为有效的EntityInsentient
     *
     * @param bukkitEntity 要检查的Bukkit实体
     * @return 如果实体可以转换为EntityInsentient返回true
     */
    public static boolean isValidEntityInsentient(Entity bukkitEntity) {
        return bukkitToNms(bukkitEntity) != null;
    }
}
