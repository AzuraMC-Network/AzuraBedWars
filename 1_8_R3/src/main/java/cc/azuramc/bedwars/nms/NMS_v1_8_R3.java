package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.LoggerUtil;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author An5w1r@163.com
 */
public class NMS_v1_8_R3 implements NMSAccess {

    @Override
    public void hideArmor(Player victim, Player receiver) {
        LoggerUtil.debug("NMS_v1_8_R3$hideArmor | victim: " + victim + ", receiver: " + receiver);
        if (victim.equals(receiver)) return;
        PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(victim.getEntityId(), 1, CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.AIR)));
        PacketPlayOutEntityEquipment chest = new PacketPlayOutEntityEquipment(victim.getEntityId(), 2, CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.AIR)));
        PacketPlayOutEntityEquipment pants = new PacketPlayOutEntityEquipment(victim.getEntityId(), 3, CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.AIR)));
        PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(victim.getEntityId(), 4, CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.AIR)));
        PlayerConnection boundTo = ((CraftPlayer) receiver).getHandle().playerConnection;
        boundTo.sendPacket(helmet);
        boundTo.sendPacket(chest);
        boundTo.sendPacket(pants);
        boundTo.sendPacket(boots);
    }

    @Override
    public void showArmor(Player victim, Player receiver) {
        LoggerUtil.debug("NMS_v1_8_R3$showArmor | victim: " + victim + ", receiver: " + receiver);
        if (victim.equals(receiver)) return;
        EntityPlayer entityPlayer = ((CraftPlayer) victim).getHandle();
        PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 4, entityPlayer.inventory.getArmorContents()[3]);
        PacketPlayOutEntityEquipment chest = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 3, entityPlayer.inventory.getArmorContents()[2]);
        PacketPlayOutEntityEquipment pants = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 2, entityPlayer.inventory.getArmorContents()[1]);
        PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 1, entityPlayer.inventory.getArmorContents()[0]);
        EntityPlayer boundTo = ((CraftPlayer) receiver).getHandle();
        boundTo.playerConnection.sendPacket(helmet);
        boundTo.playerConnection.sendPacket(chest);
        boundTo.playerConnection.sendPacket(pants);
        boundTo.playerConnection.sendPacket(boots);
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

    @Override
    public Fireball setFireballDirection(Fireball fireball, @NotNull Vector vector) {
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
    public LivingEntity spawnIronGolem(Location loc, GameTeam gameTeam, double speed, double health, int despawn) {
        LoggerUtil.debug("NMS_v1_8_R3$spawnIronGolem | loc: " + loc + ", gameTeam: " + gameTeam.getName() + ", speed: " + speed + ", health: " + health + ", despawn: " + despawn);
        return CustomIronGolem.spawn(loc, gameTeam, speed, health, despawn);
    }

    @Override
    public LivingEntity spawnSilverfish(Location loc, GameTeam gameTeam, double speed, double health, int despawn, double damage) {
        LoggerUtil.debug("NMS_v1_8_R3$spawnSilverfish | loc: " + loc + ", gameTeam: " + gameTeam.getName() + ", speed: " + speed + ", health: " + health + ", despawn: " + despawn + ", damage: " + damage);
        return CustomSilverfish.spawn(loc, gameTeam, speed, health, despawn, damage);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void placeLadder(Block block, int x, int y, int z, int data) {
        block.getRelative(x, y, z).setType(Material.LADDER);
        block.getRelative(x, y, z).setData((byte)data);
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
