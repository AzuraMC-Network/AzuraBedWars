package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.util.LoggerUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.*;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 * @author An5w1r@163.com
 */
public class CompatibilityModeNMS implements NMSAccess {

    @Override
    public void hideArmor(Player victim, Player receiver) {
        LoggerUtil.debug("CompatibilityModeNMS$hideArmor | victim: " + victim + ", receiver: " + receiver);
        List<Pair<EnumItemSlot, ItemStack>> items = new ArrayList<>();
        items.add(new Pair<>(EnumItemSlot.f, new ItemStack(Item.b(0))));
        items.add(new Pair<>(EnumItemSlot.e, new ItemStack(Item.b(0))));
        items.add(new Pair<>(EnumItemSlot.d, new ItemStack(Item.b(0))));
        items.add(new Pair<>(EnumItemSlot.c, new ItemStack(Item.b(0))));
        PacketPlayOutEntityEquipment packet1 = new PacketPlayOutEntityEquipment(victim.getEntityId(), items);
        sendPacket(receiver, packet1);
    }

    @Override
    public void showArmor(Player victim, Player receiver) {
        LoggerUtil.debug("CompatibilityModeNMS$showArmor | victim: " + victim + ", receiver: " + receiver);
        List<Pair<EnumItemSlot, ItemStack>> items = new ArrayList<>();
        items.add(new Pair<>(EnumItemSlot.f, CraftItemStack.asNMSCopy(victim.getInventory().getHelmet())));
        items.add(new Pair<>(EnumItemSlot.e, CraftItemStack.asNMSCopy(victim.getInventory().getChestplate())));
        items.add(new Pair<>(EnumItemSlot.d, CraftItemStack.asNMSCopy(victim.getInventory().getLeggings())));
        items.add(new Pair<>(EnumItemSlot.c, CraftItemStack.asNMSCopy(victim.getInventory().getBoots())));
        PacketPlayOutEntityEquipment packet1 = new PacketPlayOutEntityEquipment(victim.getEntityId(), items);
        sendPacket(receiver, packet1);
    }

    @Override
    public void hideArmor(GamePlayer gamePlayer, List<GamePlayer> receiverList) {
        for (GamePlayer receiver : receiverList) {
            hideArmor(gamePlayer.getPlayer(), receiver.getPlayer());
        }
    }

    @Override
    public void showArmor(GamePlayer gamePlayer, List<GamePlayer> receiverList) {
        for (GamePlayer receiver : receiverList) {
            showArmor(gamePlayer.getPlayer(), receiver.getPlayer());
        }
    }

    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().c.a(packet);
    }
}
