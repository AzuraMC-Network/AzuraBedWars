package cc.azuramc.bedwars.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author An5w1r@163.com
 */
public class InvisibleUtil {

    // 槽位常量
    public static final int SLOT_HAND = 0;
    public static final int SLOT_HEAD = 1;
    public static final int SLOT_CHEST = 2;
    public static final int SLOT_LEGS = 3;
    public static final int SLOT_FEET = 4;
    public static final int SLOT_OFF_HAND = 5;

    /**
     * 隐藏指定玩家的盔甲（发送给所有其他玩家）
     * @param target 要被隐藏盔甲的玩家
     */
    public static void hide(Player target) {
        sendArmorPacketToAll(target, true);
        MessageUtil.sendDebugMessage("hide " + target.getName());
    }

    /**
     * 显示指定玩家的盔甲（发送给所有其他玩家）
     * @param target 要被显示盔甲的玩家
     */
    public static void show(Player target) {
        sendArmorPacketToAll(target, false);
        MessageUtil.sendDebugMessage("show " + target.getName());
    }

    /**
     * 创建并发送装备数据包给所有其他玩家
     * @param target 目标玩家
     * @param hide 是否隐藏装备
     */
    private static void sendArmorPacketToAll(Player target, boolean hide) {
        try {
            PacketContainer packet = ProtocolLibrary.getProtocolManager()
                    .createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);

            // 设置目标玩家的实体ID
            packet.getIntegers().write(0, target.getEntityId());

            // 根据版本处理数据包
            // 1.16+
//            if (!VersionUtil.isLessThan116()) {
//                modifyNew(packet, target, hide);
//                MessageUtil.sendDebugMessage("invisible 1.16+");
//            } else if (!VersionUtil.isVersion18()) {
//                // 1.8 - 1.16
//                modifyOld(packet, target, hide);
//                MessageUtil.sendDebugMessage("invisible 1.8-1.16");
//            } else {
                // 1.8
                MessageUtil.sendDebugMessage("invisible 1.8");
                modifyVeryOld(packet, target, hide);
//            }

            // 发送数据包给所有其他在线玩家
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(target)) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(onlinePlayer, packet);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 1.16+ 新版本处理
     */
    private static void modifyNew(PacketContainer packet, Player target, boolean hide) {
        StructureModifier<List<Pair<EnumWrappers.ItemSlot, ItemStack>>> modifier = packet.getSlotStackPairLists();

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList<>();

        // 添加所有装备槽位
        addEquipmentPair(equipmentList, EnumWrappers.ItemSlot.MAINHAND,
                hide ? null : target.getItemInHand());
        addEquipmentPair(equipmentList, EnumWrappers.ItemSlot.HEAD,
                hide ? null : target.getInventory().getHelmet());
        addEquipmentPair(equipmentList, EnumWrappers.ItemSlot.CHEST,
                hide ? null : target.getInventory().getChestplate());
        addEquipmentPair(equipmentList, EnumWrappers.ItemSlot.LEGS,
                hide ? null : target.getInventory().getLeggings());
        addEquipmentPair(equipmentList, EnumWrappers.ItemSlot.FEET,
                hide ? null : target.getInventory().getBoots());

        // 如果版本支持副手
//        if (VersionUtil.hasOffhand()) {
//            addEquipmentPair(equipmentList, EnumWrappers.ItemSlot.OFFHAND,
//                    hide ? null : getOffhandItem(target));
//        }

        modifier.write(0, equipmentList);
    }

    /**
     * 1.9 - 1.15.2 等旧版本处理
     */
    private static void modifyOld(PacketContainer packet, Player target, boolean hide) {
        // 旧版本需要为每个装备槽位创建单独的数据包
        sendMultiplePacketsOld(target, hide);
    }

    /**
     * 1.8.X 版本处理
     */
    private static void modifyVeryOld(PacketContainer packet, Player target, boolean hide) {
        // 1.8版本需要为每个装备槽位创建单独的数据包
        sendMultiplePacketsVeryOld(target, hide);
    }

    /**
     * 为旧版本发送多个单独的装备数据包 (1.9-1.15.2)
     */
    private static void sendMultiplePacketsOld(Player target, boolean hide) {
        try {
            sendSingleSlotPacketOld(target, EnumWrappers.ItemSlot.MAINHAND,
                    hide ? null : target.getItemInHand());
            sendSingleSlotPacketOld(target, EnumWrappers.ItemSlot.HEAD,
                    hide ? null : target.getInventory().getHelmet());
            sendSingleSlotPacketOld(target, EnumWrappers.ItemSlot.CHEST,
                    hide ? null : target.getInventory().getChestplate());
            sendSingleSlotPacketOld(target, EnumWrappers.ItemSlot.LEGS,
                    hide ? null : target.getInventory().getLeggings());
            sendSingleSlotPacketOld(target, EnumWrappers.ItemSlot.FEET,
                    hide ? null : target.getInventory().getBoots());

//            if (VersionUtil.hasOffhand()) {
//                sendSingleSlotPacketOld(target, EnumWrappers.ItemSlot.OFFHAND,
//                        hide ? null : getOffhandItem(target));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 为很旧版本发送多个单独的装备数据包 (1.8.x)
     */
    private static void sendMultiplePacketsVeryOld(Player target, boolean hide) {
        try {
            sendSingleSlotPacketVeryOld(target, SLOT_HAND,
                    hide ? null : target.getItemInHand());
            sendSingleSlotPacketVeryOld(target, SLOT_HEAD,
                    hide ? null : target.getInventory().getHelmet());
            sendSingleSlotPacketVeryOld(target, SLOT_CHEST,
                    hide ? null : target.getInventory().getChestplate());
            sendSingleSlotPacketVeryOld(target, SLOT_LEGS,
                    hide ? null : target.getInventory().getLeggings());
            sendSingleSlotPacketVeryOld(target, SLOT_FEET,
                    hide ? null : target.getInventory().getBoots());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送单个装备槽位数据包 (1.9-1.15.2)
     */
    private static void sendSingleSlotPacketOld(Player target, EnumWrappers.ItemSlot slot, ItemStack item) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);

        packet.getIntegers().write(0, target.getEntityId());
        packet.getItemSlots().write(0, slot);
        packet.getItemModifier().write(0, item);

        // 发送给所有其他玩家
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(target)) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(onlinePlayer, packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 发送单个装备槽位数据包 (1.8.x)
     */
    private static void sendSingleSlotPacketVeryOld(Player target, int slotNum, ItemStack item) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);

        packet.getIntegers().write(0, target.getEntityId());
        packet.getShorts().write(0, (short) slotNum);
        packet.getItemModifier().write(0, item);

        // 发送给所有其他玩家
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(target)) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(onlinePlayer, packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 添加装备对到列表
     */
    private static void addEquipmentPair(List<Pair<EnumWrappers.ItemSlot, ItemStack>> list,
                                         EnumWrappers.ItemSlot slot, ItemStack item) {
        list.add(new Pair<>(slot, item));
    }


    /**
     * 将ItemSlot转换为数字槽位
     */
    private static int convertSlot(EnumWrappers.ItemSlot slot) {
        switch (slot) {
            case MAINHAND: return SLOT_HAND;
            case HEAD: return SLOT_HEAD;
            case CHEST: return SLOT_CHEST;
            case LEGS: return SLOT_LEGS;
            case FEET: return SLOT_FEET;
            case OFFHAND: return SLOT_OFF_HAND;
            default: return -1;
        }
    }
}