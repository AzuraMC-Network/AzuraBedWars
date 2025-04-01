package cc.azuramc.bedwars.game.event.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.event.GameEvent;
import org.bukkit.Bukkit;
import cc.azuramc.bedwars.compat.util.DestroyBed;
import org.bukkit.Sound;

public class BedDestroyedEvent extends GameEvent {
    public BedDestroyedEvent() {
        super("床自毁", 360, 5);
    }

    @Override
    public void excute(Game game) {
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            for (GameTeam gameTeam : game.getGameTeams()) {
                if (gameTeam.isBedDestroy()) continue;

                DestroyBed.destroyBed(gameTeam);
                gameTeam.setBedDestroy(true);
            }
        });

        playCompatibleSound(game);
        game.broadcastTitle(10, 20, 10, "§c§l床自毁", "§e所有队伍床消失");
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
