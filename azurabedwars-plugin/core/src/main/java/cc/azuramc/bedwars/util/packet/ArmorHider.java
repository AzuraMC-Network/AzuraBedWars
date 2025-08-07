package cc.azuramc.bedwars.util.packet;

import cc.azuramc.bedwars.game.GamePlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author An5w1r@163.com
 */
public class ArmorHider {

    /**
     * 隐藏victim玩家的盔甲，只对receiver玩家生效
     *
     * @param victim   被隐藏盔甲的玩家
     * @param receiver 接收数据包的玩家（看不到victim的盔甲）
     */
    public static void hideArmor(Player victim, Player receiver) {
        if (receiver == null) {
            return;
        }
        if (victim.equals(receiver)) {
            return;
        }

        // 创建空气物品用于隐藏盔甲
        ItemStack air = new ItemStack(Material.AIR);

        // 创建装备列表 将所有盔甲槽位设置为空气
        List<Equipment> equipmentList = new ArrayList<>();
        equipmentList.add(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(air)));
        equipmentList.add(new Equipment(EquipmentSlot.CHEST_PLATE, SpigotConversionUtil.fromBukkitItemStack(air)));
        equipmentList.add(new Equipment(EquipmentSlot.LEGGINGS, SpigotConversionUtil.fromBukkitItemStack(air)));
        equipmentList.add(new Equipment(EquipmentSlot.BOOTS, SpigotConversionUtil.fromBukkitItemStack(air)));

        WrapperPlayServerEntityEquipment equipmentPacket = new WrapperPlayServerEntityEquipment(victim.getEntityId(), equipmentList);

        PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, equipmentPacket);
    }

    /**
     * 显示victim玩家的真实盔甲，只对receiver玩家生效
     *
     * @param victim   要显示盔甲的玩家
     * @param receiver 接收数据包的玩家（能看到victim的真实盔甲）
     */
    public static void showArmor(Player victim, Player receiver) {
        if (receiver == null) {
            return;
        }
        if (victim.equals(receiver)) {
            return;
        }

        // 获取victim玩家的真实盔甲
        ItemStack helmet = victim.getInventory().getHelmet();
        ItemStack chestplate = victim.getInventory().getChestplate();
        ItemStack leggings = victim.getInventory().getLeggings();
        ItemStack boots = victim.getInventory().getBoots();

        // 如果盔甲为null 则使用空气
        if (helmet == null) helmet = new ItemStack(Material.AIR);
        if (chestplate == null) chestplate = new ItemStack(Material.AIR);
        if (leggings == null) leggings = new ItemStack(Material.AIR);
        if (boots == null) boots = new ItemStack(Material.AIR);

        // 创建装备列表 设置为真实的盔甲
        List<Equipment> equipmentList = new ArrayList<>();
        equipmentList.add(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(helmet)));
        equipmentList.add(new Equipment(EquipmentSlot.CHEST_PLATE, SpigotConversionUtil.fromBukkitItemStack(chestplate)));
        equipmentList.add(new Equipment(EquipmentSlot.LEGGINGS, SpigotConversionUtil.fromBukkitItemStack(leggings)));
        equipmentList.add(new Equipment(EquipmentSlot.BOOTS, SpigotConversionUtil.fromBukkitItemStack(boots)));

        WrapperPlayServerEntityEquipment equipmentPacket = new WrapperPlayServerEntityEquipment(victim.getEntityId(), equipmentList);

        PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, equipmentPacket);
    }

    public static void hideArmor(GamePlayer gamePlayer, List<GamePlayer> receiverList) {
        for (GamePlayer receiver : receiverList) {
            hideArmor(gamePlayer.getPlayer(), receiver.getPlayer());
        }
    }

    public static void showArmor(GamePlayer gamePlayer, List<GamePlayer> receiverList) {
        for (GamePlayer receiver : receiverList) {
            showArmor(gamePlayer.getPlayer(), receiver.getPlayer());
        }
    }
}
