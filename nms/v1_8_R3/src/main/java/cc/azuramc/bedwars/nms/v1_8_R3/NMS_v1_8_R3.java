package cc.azuramc.bedwars.nms.v1_8_R3;

import cc.azuramc.bedwars.api.nms.MobGoals;
import cc.azuramc.bedwars.api.nms.NMSAccess;
import net.minecraft.server.v1_8_R3.EntityFireball;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
public class NMS_v1_8_R3 implements NMSAccess {

    @Override
    public Fireball setFireballDirection(@NotNull Fireball fireball, @NotNull Vector vector) {
        EntityFireball fb = ((CraftFireball) fireball).getHandle();
        fb.dirX = vector.getX() * 0.1D;
        fb.dirY = vector.getY() * 0.1D;
        fb.dirZ = vector.getZ() * 0.1D;
        return (Fireball) fb.getBukkitEntity();
    }

    @Override
    public MobGoals getGoals(Creature creature) {
        return new MobGoalsImpl(((CraftCreature) creature).getHandle());
    }

    @Override
    public void setAI(LivingEntity entity, boolean ai) {
        if (((CraftLivingEntity) entity).getHandle() instanceof EntityInsentient) {
            EntityInsentient insentient = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
            insentient.k(!ai);
        }
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

    @SuppressWarnings("deprecation")
    @Override
    public void setWoolBlockData(Block block, byte data) {
        block.setData(data);
    }
}
