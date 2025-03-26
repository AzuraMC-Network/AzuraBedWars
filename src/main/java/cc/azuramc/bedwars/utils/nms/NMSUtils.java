package cc.azuramc.bedwars.utils.nms;

import cc.azuramc.bedwars.utils.reflect.ReflectionUtils;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static cc.azuramc.bedwars.utils.reflect.ReflectionUtils.*;


public final class NMSUtils {

    /**
     * -- GETTER --
     *  取服务器版本 如 v1_10_R1
     *  <p>
     *  get the server version, returns a string similar to v1_10_R1
     *
     */
    @Getter
    private static final String version;
    private static Field playerConnectionField;
    private static Method sendPacketMethod;
    private static Method asNMSCopyMethod;
    private static Method asCraftCopyMethod;
    private static Method asBukkitCopyMethod;
    private static Method stringAsIChatBaseComponentMethod;
    private static Method craftBukkitEntityPlayerGetHandleMethod;

    static {
        // org.bukkit.craftbukkit.vX_XX_RX;
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        // initial
        try {
            playerConnectionField = getFieldByFieldName(getNMSClass("EntityPlayer"), "playerConnection");
            sendPacketMethod = getMethod(getNMSClass("PlayerConnection"), "sendPacket", getNMSClass("Packet"));
            asNMSCopyMethod = getMethod(getOBCClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);
            asCraftCopyMethod = getMethod(getOBCClass("inventory.CraftItemStack"), "asCraftCopy", ItemStack.class);
            asBukkitCopyMethod = getMethod(getOBCClass("inventory.CraftItemStack"), "asBukkitCopy", getNMSClass("ItemStack"));
            stringAsIChatBaseComponentMethod = getMethod(getNMSClass("IChatBaseComponent$ChatSerializer"), "a", String.class);
            craftBukkitEntityPlayerGetHandleMethod = getMethod(getOBCClass("entity.CraftPlayer"), "getHandle");
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    // Prevent accidental construction
    private NMSUtils() {
    }

    /**
     * 取 org.bukkit.craftbukkit 包下的类对象
     * <p>
     * get org.bukkit.craftbukkit's class object
     *
     * @param className a class's name in the package obc
     * @return {@link Class}
     */
    public static Class<?> getOBCClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + cc.azuramc.bedwars.utils.nms.NMSUtils.getVersion() + "." + className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将NMSItem转换为BukkitItem
     * <p>
     * Convert NMS Item to Bukkit Item
     *
     * @param nmsItem the NMSItem Object
     * @return {@link Object}
     */
    public static Object getBukkitItem(Object nmsItem) {
        if (asBukkitCopyMethod == null) {
            try {
                asNMSCopyMethod = getMethod(getOBCClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            return invokeMethod(asBukkitCopyMethod, null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 取物品的 NMS 对象
     * <p>
     * get a item's nms object
     *
     * @param itemStack a itemStack object
     * @return {@link Object}
     */
    public static Object getNMSItem(ItemStack itemStack) {
        Validate.notNull(itemStack);

        if (asNMSCopyMethod == null) {
            Class<?> craftItemStack = cc.azuramc.bedwars.utils.nms.NMSUtils.getOBCClass("inventory.CraftItemStack");

            try {
                // CraftItemStack
                asNMSCopyMethod = getMethod(craftItemStack, "asNMSCopy", ItemStack.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            return invokeMethod(asNMSCopyMethod, null, itemStack);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemStack;
    }

    /**
     * 取物品的 OBC 对象
     * <p>
     * Get the OBC object of the item
     *
     * @param itemStack the itemStack
     * @return {@link Object}
     */
    public static Object getOBCItem(ItemStack itemStack) {
        Validate.notNull(itemStack);

        if (asCraftCopyMethod == null) {
            Class<?> craftItemStack = cc.azuramc.bedwars.utils.nms.NMSUtils.getOBCClass("inventory.CraftItemStack");

            try {
                // CraftItemStack
                asCraftCopyMethod = getMethod(craftItemStack, "asCraftCopy", ItemStack.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            return invokeMethod(asCraftCopyMethod, null, itemStack);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemStack;
    }

    /**
     * 取对应的 NMS 下的类
     * <p>
     * get net.minecraft.server's class object
     *
     * @param className a class's name in the package nms
     * @return {@link Class}
     */
    public static Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + className);
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
        return null;
    }

    /**
     * 给一名玩家发送 NMS 数据包
     * <p>
     * send a NMS packet to a player
     *
     * @param player player object
     * @param packet packet object
     * @see #getNMSPlayer(Player)
     * @see ReflectionUtils#invokeMethod(Method, Object, Object...)
     */
    public static void sendPacket(Player player, Object packet) {
        Object entityPlayer = getNMSPlayer(player);

        if (playerConnectionField == null) {
            try {
                playerConnectionField = getFieldByFieldName(getNMSClass("EntityPlayer"), "playerConnection");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        if (sendPacketMethod == null) {
            try {
                sendPacketMethod = getMethod(getNMSClass("PlayerConnection"), "sendPacket", getNMSClass("Packet"));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        try {
            // get playerConnection instance
            Object playerConnection = null;
            if (playerConnectionField != null) {
                playerConnection = playerConnectionField.get(entityPlayer);
            }
            // invoke method sendPacket()
            if (sendPacketMethod != null) {
                invokeMethod(sendPacketMethod, playerConnection, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取玩家的 NMS 对象
     * <p>
     * get a player's nms object
     *
     * @param player player object
     * @return {@link Object}
     * @see ReflectionUtils#invokeMethod(Method, Object, Object...)
     */
    public static Object getNMSPlayer(Player player) {
        try {
            return invokeMethod(craftBukkitEntityPlayerGetHandleMethod, player);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return player;
    }

    /**
     * 将一行文本转换为 IChatBaseComponent 对象
     * <p>
     * Convert a text to IChatBaseComponent
     *
     * @param text String object
     * @return {@link Object}
     * @see ReflectionUtils#getMethod(Class, String, Class[])
     */
    public static Object stringToIChatBaseComponent(String text) {
        if (stringAsIChatBaseComponentMethod == null) {
            try {
                // IChatBaseComponent$ChatSerializer
                stringAsIChatBaseComponentMethod = getMethod(getNMSClass("IChatBaseComponent$ChatSerializer"), "a", String.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            return invokeMethod(stringAsIChatBaseComponentMethod, String.class, Validate.notNull(text));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
