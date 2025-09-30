package cc.azuramc.bedwars.nms.v1_8_R3;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.nms.NMSAccess;
import cc.azuramc.bedwars.util.LoggerUtil;
import net.minecraft.server.v1_8_R3.EntityFireball;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @author an5w1r@163.com
 */
public class NMS_v1_8_R3 implements NMSAccess {

    public NMS_v1_8_R3() {
        AbstractCustomEntity.initializeReflection();
    }

    @Override
    public Fireball setFireballDirection(Fireball fireball, Vector vector) {
        EntityFireball fb = ((CraftFireball) fireball).getHandle();
        fb.dirX = vector.getX() * 0.1D;
        fb.dirY = vector.getY() * 0.1D;
        fb.dirZ = vector.getZ() * 0.1D;
        return (Fireball) fb.getBukkitEntity();
    }

    @Override
    public LivingEntity spawnIronGolem(Location loc, GamePlayer gamePlayer, double speed, double health) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        LoggerUtil.debug("NMS_v1_8_R3$spawnIronGolem | loc: " + loc + ", gameTeam: " + gamePlayer.getName() + ", speed: " + speed + ", health: " + health);
        return CustomIronGolem.spawn(loc, gameTeam, speed, health);
    }

    @Override
    public LivingEntity spawnSilverfish(Location loc, GamePlayer gamePlayer, double speed, double health) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        LoggerUtil.debug("NMS_v1_8_R3$spawnSilverfish | loc: " + loc + ", gameTeam: " + gameTeam.getName() + ", speed: " + speed + ", health: " + health);
        return CustomSilverfish.spawn(loc, gameTeam, speed, health);
    }

    @Override
    public ItemStack setItemUnbreakable(ItemStack itemStack, boolean unbreakable, boolean hide) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if (unbreakable) {
            tag.setBoolean("Unbreakable", true);
            if (hide) {
                tag.setInt("HideFlags", tag.getInt("HideFlags") | 4);
            } else {
                int hideFlags = tag.getInt("HideFlags");
                hideFlags &= ~4;
                if (hideFlags <= 0) {
                    tag.remove("HideFlags");
                } else {
                    tag.setInt("HideFlags", hideFlags);
                }
            }
        } else {
            tag.remove("Unbreakable");
            int hideFlags = tag.getInt("HideFlags");
            hideFlags &= ~4;
            if (hideFlags <= 0) {
                tag.remove("HideFlags");
            } else {
                tag.setInt("HideFlags", hideFlags);
            }
        }

        nmsItem.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }
}
