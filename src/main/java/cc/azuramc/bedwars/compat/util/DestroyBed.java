package cc.azuramc.bedwars.compat.util;

import cc.azuramc.bedwars.game.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.lang.reflect.Method;

public class DestroyBed {

    /**
     * 安全销毁床方块，适用于所有版本
     */
    public static void destroyBed(GameTeam gameTeam) {
        try {
            // 设置床头和床脚方块为AIR
            if (gameTeam.getBedHead() != null) {
                gameTeam.getBedHead().setType(Material.AIR);
            }

            if (gameTeam.getBedFeet() != null) {
                gameTeam.getBedFeet().setType(Material.AIR);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("销毁床时出现异常: " + e.getMessage());
            try {
                // 反射调用
                setBlockTypeUsingReflection(gameTeam.getBedHead());
                setBlockTypeUsingReflection(gameTeam.getBedFeet());
            } catch (Exception ex) {
                Bukkit.getLogger().severe("无法销毁床: " + ex.getMessage());
            }
        }
    }

    /**
     * 使用反射调用旧版本的setTypeId方法设置方块为AIR
     */
    public static void setBlockTypeUsingReflection(Block block) {
        if (block == null) return;

        try {
            Method setTypeIdMethod = Block.class.getMethod("setTypeId", int.class);
            setTypeIdMethod.invoke(block, 0); // AIR ID = 0
        } catch (Exception e) {
            try {
                block.setType(Material.AIR);
            } catch (Exception ignored) {
            }
        }
    }

}
