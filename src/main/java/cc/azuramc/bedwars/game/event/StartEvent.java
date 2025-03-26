package cc.azuramc.bedwars.game.event;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.timer.CompassRunnable;
import cc.azuramc.bedwars.game.timer.GeneratorRunnable;
import cc.azuramc.bedwars.utils.SoundUtil;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class StartEvent extends GameEvent {
    public StartEvent() {
        super("开始游戏", 5, 0);
    }

    public void excuteRunnbale(Game game, int seconds) {
        game.broadcastSound(SoundUtil.get("CLICK", "UI_BUTTON_CLICK"), 1f, 1f);
        game.broadcastTitle(1, 20, 1, "§c§l游戏即将开始", "§e§l" + seconds);
    }

    public void excute(Game game) {
        game.getEventManager().registerRunnable("团队升级", (s, c) -> GamePlayer.getOnlinePlayers().forEach(player -> {
            if (!player.isSpectator()) {

                for (GameTeam gameTeam : game.getGameTeams()) {
                    if (!Objects.equals(player.getPlayer().getLocation().getWorld(), gameTeam.getSpawn().getWorld())) {
                        continue;
                    }

                    if (gameTeam.getManicMiner() > 0) {
                        AzuraBedWars.getInstance().mainThreadRunnable(() -> gameTeam.getAlivePlayers().forEach((player1 -> {
                            PotionEffectType fastDigging = getCompatiblePotionType("FAST_DIGGING", "HASTE");
                            if (fastDigging != null) {
                                player1.getPlayer().addPotionEffect(new PotionEffect(fastDigging, 40, gameTeam.getManicMiner()));
                            }
                        })));
                    }

                    if (gameTeam.isInTeam(player)) {
                        if (player.getPlayer().getLocation().distance(gameTeam.getSpawn()) <= 7 && gameTeam.isHealPool()) {
                            AzuraBedWars.getInstance().mainThreadRunnable(() -> {
                                PotionEffectType regeneration = getCompatiblePotionType("REGENERATION", "REGENERATION");
                                if (regeneration != null) {
                                    player.getPlayer().addPotionEffect(new PotionEffect(regeneration, 60, 1));
                                }
                            });
                        }

                        continue;
                    }

                    if (player.getPlayer().getLocation().distance(gameTeam.getSpawn()) <= 20 && !gameTeam.isDead()) {
                        if (gameTeam.isTrap()) {
                            gameTeam.setTrap(false);

                            AzuraBedWars.getInstance().mainThreadRunnable(() -> {
                                PotionEffectType blindness = getCompatiblePotionType("BLINDNESS", "BLINDNESS");
                                if (blindness != null) {
                                    player.getPlayer().addPotionEffect(new PotionEffect(blindness, 200, 1));
                                }
                            });

                            AzuraBedWars.getInstance().mainThreadRunnable(() -> {
                                PotionEffectType slowness = getCompatiblePotionType("SLOW", "SLOWNESS");
                                if (slowness != null) {
                                    player.getPlayer().addPotionEffect(new PotionEffect(slowness, 200, 1));
                                }
                            });

                            AzuraBedWars.getInstance().mainThreadRunnable(() -> gameTeam.getAlivePlayers().forEach((player1 -> {
                                player1.sendTitle(0, 20, 0, "§c§l陷阱触发！", null);
                                playCompatibleEndermanSound(player1);
                            })));
                        }

                        if (gameTeam.isMiner()) {
                            AzuraBedWars.getInstance().mainThreadRunnable(() -> {
                                PotionEffectType miningFatigue = getCompatiblePotionType("SLOW_DIGGING", "MINING_FATIGUE");
                                if (miningFatigue != null) {
                                    player.getPlayer().addPotionEffect(new PotionEffect(miningFatigue, 200, 0));
                                }
                            });
                            gameTeam.setMiner(false);
                        }
                    }
                }
            }
        }));
        new GeneratorRunnable(game).start();
        new CompassRunnable().start();
    }

    /**
     * 获取兼容不同版本Minecraft的药水效果类型
     * @param oldName 1.8版本的名称
     * @param newName 1.13+版本的名称
     * @return 药水效果类型或null（如果都不可用）
     */
    private PotionEffectType getCompatiblePotionType(String oldName, String newName) {
        PotionEffectType type = PotionEffectType.getByName(oldName);
        if (type == null) {
            type = PotionEffectType.getByName(newName);
        }

        if (type == null) {
            System.out.println("无法找到兼容的药水效果类型: " + oldName + " 或 " + newName);
        }

        return type;
    }

    /**
     * 播放兼容1.8和1.21版本的末影人传送声音
     */
    private void playCompatibleEndermanSound(GamePlayer player) {
        try {
            // 尝试使用1.8版本的声音枚举
            Sound endermanSound = Sound.valueOf("ENDERMAN_TELEPORT");
            player.playSound(endermanSound, 1, 1);
        } catch (IllegalArgumentException e) {
            try {
                // 尝试使用1.9+版本的声音枚举
                Sound endermanSound = Sound.valueOf("ENTITY_ENDERMAN_TELEPORT");
                player.playSound(endermanSound, 1, 1);
            } catch (IllegalArgumentException e2) {
                try {
                    // 尝试使用最新版本的可能命名（如果有变化）
                    Sound endermanSound = Sound.valueOf("ENTITY_ENDERMAN_TP");
                    player.playSound(endermanSound, 1, 1);
                } catch (IllegalArgumentException e3) {
                    // 如果所有尝试都失败，使用一个通用的备选声音
                    try {
                        Sound fallbackSound = Sound.valueOf("BLOCK_NOTE_BLOCK_PLING");
                        player.playSound(fallbackSound, 1, 1);
                    } catch (Exception e4) {
                        // 静默失败，不影响游戏流程
                        System.out.println("无法播放兼容的末影人传送声音");
                    }
                }
            }
        }
    }
}
