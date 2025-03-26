package cc.azuramc.bedwars.utils;

import cc.azuramc.bedwars.utils.nms.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ArmorStandUtil {

    private static final HashMap<ArmorStand, Location> armorloc = new HashMap<>();
    private static final HashMap<ArmorStand, Boolean> armorupward = new HashMap<>();
    private static final HashMap<ArmorStand, Integer> armoralgebra = new HashMap<>();

    public static void moveArmorStand(ArmorStand armorStand, double height) {
        if (!armorloc.containsKey(armorStand)) {
            armorloc.put(armorStand, armorStand.getLocation().clone());
        }
        if (!armorupward.containsKey(armorStand)) {
            armorupward.put(armorStand, true);
        }
        if (!armoralgebra.containsKey(armorStand)) {
            armoralgebra.put(armorStand, 0);
        }
        armoralgebra.put(armorStand, armoralgebra.get(armorStand) + 1);
        Location location = armorloc.get(armorStand);
        if (location.getY() >= height + 0.30) {
            armoralgebra.put(armorStand, 0);
            armorupward.put(armorStand, false);
        } else if (location.getY() <= height - 0.30) {
            armoralgebra.put(armorStand, 0);
            armorupward.put(armorStand, true);
        }
        Integer algebra = armoralgebra.get(armorStand);
        if (39 > algebra || algebra >= 50) {
            if (armorupward.get(armorStand)) {
                location.setY(location.getY() + 0.015);
            } else {
                location.setY(location.getY() - 0.015);
            }
        }
        float turn = 1f;
        if (!armorupward.get(armorStand)) {
            turn = -turn;
        }
        float yaw = location.getYaw();
        if (algebra == 1 || algebra == 40) {
            yaw += 2f * turn;
        } else if (algebra == 2 || algebra == 39) {
            yaw += 3f * turn;
        } else if (algebra == 3 || algebra == 38) {
            yaw += 4f * turn;
        } else if (algebra == 4 || algebra == 37) {
            yaw += 5f * turn;
        } else if (algebra == 5 || algebra == 36) {
            yaw += 6f * turn;
        } else if (algebra == 6 || algebra == 35) {
            yaw += 7f * turn;
        } else if (algebra == 7 || algebra == 34) {
            yaw += 8f * turn;
        } else if (algebra == 8 || algebra == 33) {
            yaw += 9f * turn;
        } else if (algebra == 9 || algebra == 32) {
            yaw += 10f * turn;
        } else if (algebra == 10 || algebra == 31) {
            yaw += 11f * turn;
        } else if (algebra == 11 || algebra == 30) {
            yaw += 11f * turn;
        } else if (algebra == 12 || algebra == 29) {
            yaw += 12f * turn;
        } else if (algebra == 13 || algebra == 28) {
            yaw += 12f * turn;
        } else if (algebra == 14 || algebra == 27) {
            yaw += 13f * turn;
        } else if (algebra == 15 || algebra == 26) {
            yaw += 13f * turn;
        } else if (algebra == 16 || algebra == 25) {
            yaw += 14f * turn;
        } else if (algebra == 17 || algebra == 24) {
            yaw += 14f * turn;
        } else if (algebra == 18 || algebra == 23) {
            yaw += 15f * turn;
        } else if (algebra == 19 || algebra == 22) {
            yaw += 15f * turn;
        } else if (algebra == 20 || algebra == 21) {
            yaw += 16f * turn;
        } else if (algebra == 41) {
            yaw += 2f * turn;
        } else if (algebra == 42) {
            yaw += 2f * turn;
        } else if (algebra == 43) {
            yaw += 2f * turn;
        } else if (algebra == 44) {
            yaw += 1f * turn;
        } else if (algebra == 45) {
            yaw += -1f * turn;
        } else if (algebra == 46) {
            yaw += -1f * turn;
        } else if (algebra == 47) {
            yaw += -2f * turn;
        } else if (algebra == 48) {
            yaw += -2f * turn;
        } else if (algebra == 49) {
            yaw += -2f * turn;
        } else if (algebra == 50) {
            yaw += -2f * turn;
        }
        yaw = yaw > 360 ? (yaw - 360) : yaw;
        yaw = yaw < -360 ? (yaw + 360) : yaw;
        location.setYaw(yaw);

        if (armorStand.getFallDistance() != 7) {
            return;
        }

        try {
            Constructor constructor = NMSUtils.getNMSClass("PacketPlayOutEntityTeleport").getConstructor(int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class);
            Method method = NMSUtils.getNMSClass("MathHelper").getMethod("floor", double.class);
            Object packet = constructor.newInstance(armorStand.getEntityId(), method.invoke(null, location.getX() * 32.0D), method.invoke(null, location.getY() * 32.0D), method.invoke(null, location.getZ() * 32.0D), (byte) (location.getYaw() * 256.0f / 360.0f), (byte) (location.getPitch() * 256.0f / 360.0f), true);
            for (Player player : Bukkit.getOnlinePlayers()) {
                NMSUtils.sendPacket(player, packet);
            }
        } catch (Exception ignored) {
        }
    }
}
