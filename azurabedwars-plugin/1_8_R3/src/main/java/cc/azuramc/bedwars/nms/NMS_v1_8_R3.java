package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.LoggerUtil;
import net.minecraft.server.v1_8_R3.EntityFireball;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author An5w1r@163.com
 */
public class NMS_v1_8_R3 implements NMSAccess {

    @Override
    public Fireball setFireballDirection(Fireball fireball, Vector vector) {
        EntityFireball fb = ((CraftFireball) fireball).getHandle();
        fb.dirX = vector.getX() * 0.1D;
        fb.dirY = vector.getY() * 0.1D;
        fb.dirZ = vector.getZ() * 0.1D;
        return (Fireball) fb.getBukkitEntity();
    }

    @Override
    public void registerCustomEntities() {
        registerCustomEntity("CustomSilverfish", 60, CustomSilverfish.class);
        registerCustomEntity("CustomIronGolem", 99, CustomIronGolem.class);
    }

    @Override
    public LivingEntity spawnIronGolem(Location loc, GamePlayer gamePlayer, double speed, double health) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        LoggerUtil.debug("NMS_v1_8_R3$spawnIronGolem | loc: " + loc + ", gameTeam: " + gamePlayer.getName() + ", speed: " + speed + ", health: " + health);
        return CustomIronGolem.spawn(loc, gameTeam, speed, health);
    }

    @Override
    public LivingEntity spawnSilverfish(Location loc, GamePlayer gamePlayer, double speed, double health, double damage) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        LoggerUtil.debug("NMS_v1_8_R3$spawnSilverfish | loc: " + loc + ", gameTeam: " + gameTeam.getName() + ", speed: " + speed + ", health: " + health + ", damage: " + damage);
        return CustomSilverfish.spawn(loc, gameTeam, speed, health, damage);
    }

    @SuppressWarnings("rawtypes")
    private void registerCustomEntity(String name, int id, Class customClass) {
        try {
            ArrayList<Map> dataMap = new ArrayList<>();
            for (Field field : EntityTypes.class.getDeclaredFields()) {
                if (!field.getType().getSimpleName().equals(Map.class.getSimpleName())) continue;
                field.setAccessible(true);
                dataMap.add((Map) field.get(null));
            }
            if (dataMap.get(2).containsKey(id)) {
                dataMap.get(0).remove(name);
                dataMap.get(2).remove(id);
            }
            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, Integer.TYPE);
            method.setAccessible(true);
            method.invoke(null, customClass, name, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
