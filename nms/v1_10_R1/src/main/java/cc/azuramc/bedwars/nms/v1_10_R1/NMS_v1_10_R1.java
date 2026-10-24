package cc.azuramc.bedwars.nms.v1_10_R1;

import cc.azuramc.bedwars.api.AzuraBedWarsAPI;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import cc.azuramc.bedwars.api.nms.NMSAccess;
import net.minecraft.server.v1_10_R1.EntityFireball;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
public class NMS_v1_10_R1 implements NMSAccess {

    public NMS_v1_10_R1() {
        AbstractCustomEntity.initializeReflection();
    }

    @Override
    public Fireball setFireballDirection(@NotNull Fireball fireball, @NotNull Vector vector) {
        EntityFireball fb = ((CraftFireball) fireball).getHandle();
        fb.dirX = vector.getX() * 0.1D;
        fb.dirY = vector.getY() * 0.1D;
        fb.dirZ = vector.getZ() * 0.1D;
        return (Fireball) fb.getBukkitEntity();
    }

    @Override
    public LivingEntity spawnIronGolem(Location loc, IGamePlayer gamePlayer, double speed, double health) {
        IGameTeam gameTeam = gamePlayer.getGameTeam();
        AzuraBedWarsAPI.debug("NMS_v1_10_R1$spawnIronGolem | loc: " + loc + ", gameTeam: " + gamePlayer.getName() + ", speed: " + speed + ", health: " + health);
        return CustomIronGolem.spawn(loc, gameTeam, speed, health);
    }

    @Override
    public LivingEntity spawnSilverfish(Location loc, IGamePlayer gamePlayer, double speed, double health) {
        IGameTeam gameTeam = gamePlayer.getGameTeam();
        AzuraBedWarsAPI.debug("NMS_v1_10_R1$spawnSilverfish | loc: " + loc + ", gameTeam: " + gameTeam.getName() + ", speed: " + speed + ", health: " + health);
        return CustomSilverfish.spawn(loc, gameTeam, speed, health);
    }

    @Override
    public ItemStack setItemUnbreakable(ItemStack itemStack, boolean unbreakable, boolean hide) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (hide) {
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            } else {
                meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
            itemStack.setItemMeta(meta);
        }

        net.minecraft.server.v1_10_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if (tag == null) {
            return itemStack;
        }

        if (unbreakable) {
            tag.setBoolean("Unbreakable", true);
        } else {
            tag.remove("Unbreakable");
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
