package cc.azuramc.bedwars.nms.v1_11_R1;

import cc.azuramc.bedwars.api.nms.MobGoals;
import cc.azuramc.bedwars.api.nms.NMSAccess;
import net.minecraft.server.v1_11_R1.EntityFireball;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Fireball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
public class NMS_v1_11_R1 implements NMSAccess {

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

    @SuppressWarnings("deprecation")
    @Override
    public void setWoolBlockData(Block block, byte data) {
        block.setData(data);
    }

    @Override
    public ItemStack setItemTag(ItemStack itemStack, String key, String value) {
        net.minecraft.server.v1_11_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        NBTTagCompound values = tag.getCompound(TAG_NAMESPACE);
        values.setString(key, value);
        tag.set(TAG_NAMESPACE, values);
        nmsItem.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public String getItemTag(ItemStack itemStack, String key) {
        net.minecraft.server.v1_11_R1.ItemStack nmsItem = itemStack != null ? CraftItemStack.asNMSCopy(itemStack) : null;
        if (nmsItem == null || !nmsItem.hasTag()) {
            return null;
        }
        NBTTagCompound values = nmsItem.getTag().getCompound(TAG_NAMESPACE);
        return values.hasKey(key) ? values.getString(key) : null;
    }
}
