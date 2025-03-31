package cc.azuramc.bedwars.game.event;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import java.lang.reflect.Method;

public class BedDestroyedEvent extends GameEvent {
    public BedDestroyedEvent() {
        super("床自毁", 360, 5);
    }

    @Override
    public void excute(Game game) {
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            for (GameTeam gameTeam : game.getGameTeams()) {
                if (gameTeam.isBedDestroy()) continue;
                
                // 确保兼容所有版本设置床方块为AIR
                destroyBed(gameTeam);
                gameTeam.setBedDestroy(true);
            }
        });

        // 播放兼容1.8和1.21的声音
        playCompatibleSound(game);
        game.broadcastTitle(10, 20, 10, "§c§l床自毁", "§e所有队伍床消失");
    }
    
    /**
     * 安全销毁床方块，适用于所有版本
     */
    private void destroyBed(GameTeam gameTeam) {
        try {
            // 设置床头和床脚方块为AIR
            if (gameTeam.getBedHead() != null) {
                gameTeam.getBedHead().setType(Material.AIR);
            }
            
            if (gameTeam.getBedFeet() != null) {
                gameTeam.getBedFeet().setType(Material.AIR);
            }
        } catch (Exception e) {
            // 如果设置过程中出现任何异常，记录并尝试使用替代方法
            Bukkit.getLogger().warning("销毁床时出现异常: " + e.getMessage());
            try {
                // 尝试使用反射调用旧版本中的setTypeId方法
                setBlockTypeUsingReflection(gameTeam.getBedHead());
                setBlockTypeUsingReflection(gameTeam.getBedFeet());
            } catch (Exception ex) {
                // 如果所有尝试都失败，记录错误
                Bukkit.getLogger().severe("无法销毁床: " + ex.getMessage());
            }
        }
    }
    
    /**
     * 使用反射调用旧版本的setTypeId方法设置方块为AIR
     */
    private void setBlockTypeUsingReflection(Block block) {
        if (block == null) return;
        
        try {
            // 尝试使用反射获取setTypeId方法
            Method setTypeIdMethod = Block.class.getMethod("setTypeId", int.class);
            setTypeIdMethod.invoke(block, 0); // 0是AIR的ID
        } catch (Exception e) {
            // 反射失败，最后尝试设置为AIR
            try {
                block.setType(Material.AIR);
            } catch (Exception ignored) {
                // 忽略错误，已经尝试了所有可能的方法
            }
        }
    }

    /**
     * 播放兼容不同版本Minecraft的声音
     */
    private void playCompatibleSound(Game game) {
        try {
            // 尝试使用1.8版本的声音枚举
            Sound dragonSound = Sound.valueOf("ENDERDRAGON_GROWL");
            game.broadcastSound(dragonSound, 1, 1);
        } catch (IllegalArgumentException e) {
            try {
                // 尝试使用1.9+版本的声音枚举
                Sound dragonSound = Sound.valueOf("ENTITY_ENDER_DRAGON_GROWL");
                game.broadcastSound(dragonSound, 1, 1);
            } catch (IllegalArgumentException e2) {
                try {
                    // 尝试使用最新版本的可能命名
                    Sound dragonSound = Sound.valueOf("ENTITY_DRAGON_GROWL");
                    game.broadcastSound(dragonSound, 1, 1);
                } catch (IllegalArgumentException e3) {
                    // 如果所有尝试都失败，使用一个通用的备选声音
                    try {
                        Sound fallbackSound = Sound.valueOf("ENTITY_GENERIC_EXPLODE");
                        game.broadcastSound(fallbackSound, 1, 1);
                    } catch (Exception e4) {
                        // 如果连备选声音都失败，则静默失败
                        Bukkit.getLogger().warning("无法播放兼容的声音效果");
                    }
                }
            }
        }
    }
}
