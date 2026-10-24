package cc.azuramc.bedwars.listener.packet;

import cc.azuramc.bedwars.game.GamePlayer;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class InvisibilityPacketListener extends PacketListenerAbstract {

    private static final com.github.retrooper.packetevents.protocol.item.ItemStack PE_AIR =
            SpigotConversionUtil.fromBukkitItemStack(new ItemStack(Material.AIR));

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT) {
            return;
        }

        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(event);
        int entityId = packet.getEntityId();

        // 在游戏玩家列表中查找与该实体 ID 匹配的隐身玩家
        GamePlayer invisibleGamePlayer = null;
        for (GamePlayer gamePlayer : GamePlayer.getGamePlayers()) {
            Player player = gamePlayer.getPlayer();
            if (player != null && player.getEntityId() == entityId && gamePlayer.isInvisible()) {
                invisibleGamePlayer = gamePlayer;
                break;
            }
        }

        if (invisibleGamePlayer == null) {
            return;
        }

        // 隐身玩家自己仍然可见
        Player receiver = event.getPlayer();
        if (receiver.getUniqueId().equals(invisibleGamePlayer.getUuid())) {
            return;
        }

        // 将所有装备槽替换为空气
        List<Equipment> emptyEquipment = new ArrayList<>();
        for (Equipment equipment : packet.getEquipment()) {
            emptyEquipment.add(new Equipment(equipment.getSlot(), PE_AIR));
        }
        packet.setEquipment(emptyEquipment);
        event.markForReEncode(true);
    }
}
