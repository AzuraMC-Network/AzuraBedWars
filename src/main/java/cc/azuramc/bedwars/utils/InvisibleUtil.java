package cc.azuramc.bedwars.utils;

import cc.azuramc.bedwars.game.data.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InvisibleUtil {
    private static final String VERSION;
    private static final boolean NEW_VERSION;
    
    // 反射缓存
    private static Class<?> craftItemStackClass;
    private static Method asNMSCopyMethod;
    private static Class<?> packetEquipmentClass;
    private static Constructor<?> packetEquipmentConstructor;
    private static Method getHandleMethod;
    private static Field playerConnectionField;
    private static Method sendPacketMethod;
    private static Class<?> packetClass;
    
    // 新版本反射缓存
    private static Class<?> enumItemSlotClass;
    private static Method enumItemSlotValueOf;
    
    static {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        VERSION = name.substring(name.lastIndexOf('.') + 1);
        
        // 检测是否为新版本 (1.16+)
        boolean isNew = false;
        try {
            Class.forName("org.bukkit.Material").getField("PLAYER_HEAD");
            isNew = true;
        } catch (Exception ignored) {}
        NEW_VERSION = isNew;
        
        try {
            // 初始化反射缓存
            craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + VERSION + ".inventory.CraftItemStack");
            asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            
            // 装备包和构造器初始化
            if (NEW_VERSION && Integer.parseInt(VERSION.split("_")[1]) >= 16) {
                // 1.16+ 使用新API
                packetEquipmentClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment");
                enumItemSlotClass = Class.forName("net.minecraft.world.entity.EnumItemSlot");
                enumItemSlotValueOf = enumItemSlotClass.getMethod("valueOf", String.class);
                
                try {
                    // 1.16+ 构造器接受列表参数 (各版本略有不同)
                    Class<?> pairClass = Class.forName("com.mojang.datafixers.util.Pair");
                    packetEquipmentConstructor = packetEquipmentClass.getConstructor(int.class, java.util.List.class);
                } catch (Exception e) {
                    try {
                        // 1.17+
                        packetEquipmentConstructor = packetEquipmentClass.getConstructor(int.class, java.util.List.class);
                    } catch (Exception ex) {
                        Bukkit.getLogger().warning("无法找到匹配的PacketPlayOutEntityEquipment构造器");
                    }
                }
            } else {
                // 旧版本 (1.8-1.15)
                packetEquipmentClass = Class.forName("net.minecraft.server." + VERSION + ".PacketPlayOutEntityEquipment");
                
                try {
                    // 1.8-1.14
                    packetEquipmentConstructor = packetEquipmentClass.getConstructor(int.class, int.class, Object.class);
                } catch (Exception e) {
                    try {
                        // 1.15 使用不同构造器
                        Class<?> enumItemSlotClass = Class.forName("net.minecraft.server." + VERSION + ".EnumItemSlot");
                        packetEquipmentConstructor = packetEquipmentClass.getConstructor(int.class, enumItemSlotClass, Object.class);
                        enumItemSlotValueOf = enumItemSlotClass.getMethod("valueOf", String.class);
                    } catch (Exception ex) {
                        Bukkit.getLogger().warning("无法找到匹配的PacketPlayOutEntityEquipment构造器");
                    }
                }
            }
            
            // 通用发包方法
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer");
            getHandleMethod = craftPlayerClass.getMethod("getHandle");
            
            Class<?> entityPlayerClass;
            if (NEW_VERSION && Integer.parseInt(VERSION.split("_")[1]) >= 17) {
                entityPlayerClass = Class.forName("net.minecraft.server.level.EntityPlayer");
                packetClass = Class.forName("net.minecraft.network.protocol.Packet");
            } else {
                entityPlayerClass = Class.forName("net.minecraft.server." + VERSION + ".EntityPlayer");
                packetClass = Class.forName("net.minecraft.server." + VERSION + ".Packet");
            }
            
            playerConnectionField = getNMSConnectionField(entityPlayerClass);
            Class<?> playerConnectionClass = playerConnectionField.getType();
            sendPacketMethod = playerConnectionClass.getMethod("sendPacket", packetClass);
            
        } catch (Exception e) {
            Bukkit.getLogger().severe("初始化InvisibleUtil失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Field getNMSConnectionField(Class<?> entityPlayerClass) throws NoSuchFieldException {
        try {
            // 尝试1.17+命名
            return entityPlayerClass.getField("b");
        } catch (NoSuchFieldException e) {
            try {
                // 尝试1.8-1.16命名
                return entityPlayerClass.getField("playerConnection");
            } catch (NoSuchFieldException ex) {
                // 遍历所有字段寻找PlayerConnection类型
                for (Field field : entityPlayerClass.getFields()) {
                    if (field.getType().getSimpleName().contains("PlayerConnection")) {
                        return field;
                    }
                }
                throw new NoSuchFieldException("找不到玩家连接字段");
            }
        }
    }
    
    public void hideEquip(GamePlayer gamePlayer, boolean hide) {
        try {
            Player player = gamePlayer.getPlayer();
            int entityId = player.getEntityId();
            Object nmsAirItem = asNMSCopyMethod.invoke(null, new ItemStack(Material.AIR));
            
            if (NEW_VERSION && Integer.parseInt(VERSION.split("_")[1]) >= 16) {
                // 使用新版本API (1.16+)
                hideEquipNewVersion(gamePlayer, hide, entityId, nmsAirItem);
            } else if (enumItemSlotValueOf != null) {
                // 使用1.15版本API
                hideEquip1_15Version(gamePlayer, hide, entityId, nmsAirItem);
            } else {
                // 使用旧版本API (1.8-1.14)
                hideEquipOldVersion(gamePlayer, hide, entityId, nmsAirItem);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("隐藏装备失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void hideEquipNewVersion(GamePlayer gamePlayer, boolean hide, int entityId, Object nmsAirItem) throws Exception {
        // 为1.16+版本创建装备包
        Player player = gamePlayer.getPlayer();
        
        // 创建装备对列表
        Class<?> pairClass = Class.forName("com.mojang.datafixers.util.Pair");
        Method pairOf = pairClass.getMethod("of", Object.class, Object.class);
        
        Object helmetSlot = enumItemSlotValueOf.invoke(null, "HEAD");
        Object chestSlot = enumItemSlotValueOf.invoke(null, "CHEST");
        Object legsSlot = enumItemSlotValueOf.invoke(null, "LEGS");
        Object feetSlot = enumItemSlotValueOf.invoke(null, "FEET");
        
        Object helmetItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getHelmet());
        Object chestItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getChestplate());
        Object legsItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getLeggings());
        Object feetItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getBoots());
        
        // 创建Pair列表
        java.util.List<Object> pairs = new java.util.ArrayList<>();
        pairs.add(pairOf.invoke(null, helmetSlot, helmetItem));
        pairs.add(pairOf.invoke(null, chestSlot, chestItem));
        pairs.add(pairOf.invoke(null, legsSlot, legsItem));
        pairs.add(pairOf.invoke(null, feetSlot, feetItem));
        
        // 创建并发送包
        Object packet = packetEquipmentConstructor.newInstance(entityId, pairs);
        sendPacketToOthers(gamePlayer, packet);
    }
    
    private void hideEquip1_15Version(GamePlayer gamePlayer, boolean hide, int entityId, Object nmsAirItem) throws Exception {
        // 为1.15版本创建装备包
        Player player = gamePlayer.getPlayer();
        
        Object helmetSlot = enumItemSlotValueOf.invoke(null, "HEAD");
        Object chestSlot = enumItemSlotValueOf.invoke(null, "CHEST");
        Object legsSlot = enumItemSlotValueOf.invoke(null, "LEGS");
        Object feetSlot = enumItemSlotValueOf.invoke(null, "FEET");
        
        Object helmetItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getHelmet());
        Object chestItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getChestplate());
        Object legsItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getLeggings());
        Object feetItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getBoots());
        
        Object packet1 = packetEquipmentConstructor.newInstance(entityId, helmetSlot, helmetItem);
        Object packet2 = packetEquipmentConstructor.newInstance(entityId, chestSlot, chestItem);
        Object packet3 = packetEquipmentConstructor.newInstance(entityId, legsSlot, legsItem);
        Object packet4 = packetEquipmentConstructor.newInstance(entityId, feetSlot, feetItem);
        
        sendPacketToOthers(gamePlayer, packet1);
        sendPacketToOthers(gamePlayer, packet2);
        sendPacketToOthers(gamePlayer, packet3);
        sendPacketToOthers(gamePlayer, packet4);
    }
    
    private void hideEquipOldVersion(GamePlayer gamePlayer, boolean hide, int entityId, Object nmsAirItem) throws Exception {
        // 为1.8-1.14版本创建装备包
        Player player = gamePlayer.getPlayer();
        
        Object helmetItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getHelmet());
        Object chestItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getChestplate());
        Object legsItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getLeggings());
        Object feetItem = hide ? nmsAirItem : asNMSCopyMethod.invoke(null, player.getEquipment().getBoots());
        
        Object packet1 = packetEquipmentConstructor.newInstance(entityId, 4, helmetItem);
        Object packet2 = packetEquipmentConstructor.newInstance(entityId, 3, chestItem);
        Object packet3 = packetEquipmentConstructor.newInstance(entityId, 2, legsItem);
        Object packet4 = packetEquipmentConstructor.newInstance(entityId, 1, feetItem);
        
        sendPacketToOthers(gamePlayer, packet1);
        sendPacketToOthers(gamePlayer, packet2);
        sendPacketToOthers(gamePlayer, packet3);
        sendPacketToOthers(gamePlayer, packet4);
    }
    
    private void sendPacketToOthers(GamePlayer gamePlayer, Object packet) throws Exception {
        for (GamePlayer otherPlayer : GamePlayer.getOnlinePlayers()) {
            if (otherPlayer.equals(gamePlayer)) {
                continue;
            }
            
            Player bukkitPlayer = otherPlayer.getPlayer();
            if (bukkitPlayer == null) continue;
            
            Object entityPlayer = getHandleMethod.invoke(bukkitPlayer);
            Object playerConnection = playerConnectionField.get(entityPlayer);
            sendPacketMethod.invoke(playerConnection, packet);
        }
    }
}
