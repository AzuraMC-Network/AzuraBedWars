package cc.azuramc.bedwars.spectator;

import cc.azuramc.bedwars.utils.gui.CustomGUI;
import cc.azuramc.bedwars.utils.gui.GUIAction;
import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.utils.MaterialUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpectatorSettingGUI extends CustomGUI {
    public SpectatorSettingGUI(Player player) {
        super(player, "§8旁观者设置", 36);
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        SpectatorSettings spectatorSettings = SpectatorSettings.get(gamePlayer);

        setItem(11, new ItemBuilderUtil().setType(MaterialUtil.LEATHER_BOOTS()).setDisplayName("§a没有速度效果").getItem(), new GUIAction(0, () -> {
            if (spectatorSettings.getSpeed() == 0) {
                if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                }
                return;
            }
            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
            player.sendMessage("§c你不再有任何速度效果！");
            spectatorSettings.setSpeed(0);
        }, true));

        setItem(12, new ItemBuilderUtil().setType(MaterialUtil.CHAINMAIL_BOOTS()).setDisplayName("§a速度 I").getItem(), new GUIAction(0, () -> {
            if (spectatorSettings.getSpeed() == 1) {
                if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                return;
            }
            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
            player.sendMessage("§a你获得了 速度 I 效果！");
            spectatorSettings.setSpeed(1);
        }, true));

        setItem(13, new ItemBuilderUtil().setType(MaterialUtil.IRON_BOOTS()).setDisplayName("§a速度 II").getItem(), new GUIAction(0, () -> {
            if (spectatorSettings.getSpeed() == 2) {
                if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                return;
            }
            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
            player.sendMessage("§a你获得了 速度 II 效果！");
            spectatorSettings.setSpeed(2);
        }, true));

        setItem(14, new ItemBuilderUtil().setType(MaterialUtil.GOLDEN_BOOTS()).setDisplayName("§a速度 III").getItem(), new GUIAction(0, () -> {
            if (spectatorSettings.getSpeed() == 3) {
                if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                return;
            }
            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
            player.sendMessage("§a你获得了 速度 III 效果！");
            spectatorSettings.setSpeed(3);
        }, true));

        setItem(15, new ItemBuilderUtil().setType(MaterialUtil.DIAMOND_BOOTS()).setDisplayName("§a速度 IV").getItem(), new GUIAction(0, () -> {
            if (spectatorSettings.getSpeed() == 4) {
                if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));
                return;
            }
            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));
            player.sendMessage("§a你获得了 速度 IV 效果！");
            spectatorSettings.setSpeed(4);
        }, true));

        if (spectatorSettings.getOption(SpectatorSettings.Option.AUTOTP)) {
            setItem(20, new ItemBuilderUtil().setType(MaterialUtil.COMPASS()).setDisplayName("§c停用自动传送").setLores("§7点击停用自动传送").getItem(), new GUIAction(0, () -> {
                spectatorSettings.setOption(SpectatorSettings.Option.AUTOTP, false);
                player.sendMessage("§c你不再被自动传送到目标位置！");
            }, true));
        }else {
            setItem(20, new ItemBuilderUtil().setType(MaterialUtil.COMPASS()).setDisplayName("§a启动自动传送").setLores("§7点击启用自动传送").getItem(), new GUIAction(0, () -> {
                spectatorSettings.setOption(SpectatorSettings.Option.AUTOTP, true);
                player.sendMessage("§a你开启了自动传送功能！");
            }, true));
        }

        if (spectatorSettings.getOption(SpectatorSettings.Option.NIGHTVISION)) {
            setItem(21, new ItemBuilderUtil().setType(MaterialUtil.getMaterial("ENDER_EYE", "EYE_OF_ENDER")).setDisplayName("§c停用夜视").setLores("§7点击停用夜视").getItem(), new GUIAction(0, () -> {
                if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
                spectatorSettings.setOption(SpectatorSettings.Option.NIGHTVISION, false);
                player.sendMessage("§c你不再有夜视效果了！");
            }, true));
        }else {
            setItem(21, new ItemBuilderUtil().setType(MaterialUtil.getMaterial("ENDER_EYE", "EYE_OF_ENDER")).setDisplayName("§a启动夜视").setLores("§7点击启用夜视").getItem(), new GUIAction(0, () -> {
                if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
                spectatorSettings.setOption(SpectatorSettings.Option.NIGHTVISION, true);
                player.sendMessage("§a你现在拥有了夜视！");
            }, true));
        }

        if (spectatorSettings.getOption(SpectatorSettings.Option.FIRSTPERSON)) {
            setItem(22, new ItemBuilderUtil().setType(MaterialUtil.CLOCK()).setDisplayName("§c停用第一人称旁观").setLores("§7点击停用第一人称旁观").getItem(), new GUIAction(0, () -> {
                spectatorSettings.setOption(SpectatorSettings.Option.FIRSTPERSON, false);
                player.sendMessage("§c你将默认使用第三人称模式！");
                if (gamePlayer.isSpectator() && player.getGameMode() == GameMode.SPECTATOR) {
                    gamePlayer.sendTitle(0, 20, 0, "§e退出旁观模式", null);
                    player.setGameMode(GameMode.ADVENTURE);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            }, true));
        } else {
            setItem(22, new ItemBuilderUtil().setType(MaterialUtil.CLOCK()).setDisplayName("§a启动第一人称旁观").setLores("§7点击确认使用指南针时", "§7自动沿用第一人称旁观！", "§7你也可以右键点击一位玩家", "§7来启用第一人称旁观").getItem(), new GUIAction(0, () -> {
                spectatorSettings.setOption(SpectatorSettings.Option.FIRSTPERSON, true);
                player.sendMessage("§a当你用你的指南针现在一个玩家后，你会被自动传送到他那里！");
            }, true));
        }

        if (spectatorSettings.getOption(SpectatorSettings.Option.HIDEOTHER)) {
            setItem(23, new ItemBuilderUtil().setType(MaterialUtil.getMaterial("GLOWSTONE_DUST", "GLOWSTONE_DUST")).setDisplayName("§a查看旁观者").setLores("§7点击以显示其他旁观者").getItem(), new GUIAction(0, () -> {
                spectatorSettings.setOption(SpectatorSettings.Option.HIDEOTHER, false);
                player.sendMessage("§a你现在可以看见其他旁观者了！");
            }, true));
        } else {
            setItem(23, new ItemBuilderUtil().setType(MaterialUtil.getMaterial("REDSTONE", "REDSTONE")).setDisplayName("§c隐藏旁观者").setLores("§7点击来隐藏其他旁观者").getItem(), new GUIAction(0, () -> {
                spectatorSettings.setOption(SpectatorSettings.Option.HIDEOTHER, true);
                player.sendMessage("§c你不会再看到其他的旁观者！");
            }, true));
        }

        if (spectatorSettings.getOption(SpectatorSettings.Option.FLY)) {
            setItem(24, new ItemBuilderUtil().setType(MaterialUtil.getMaterial("FEATHER", "FEATHER")).setDisplayName("§c停用持续飞行").setLores("§7点击停用飞行").getItem(), new GUIAction(0, () -> {
                spectatorSettings.setOption(SpectatorSettings.Option.FLY, false);
                player.sendMessage("§a你现在能停止飞行！");
            }, true));
        } else {
            setItem(24, new ItemBuilderUtil().setType(MaterialUtil.getMaterial("FEATHER", "FEATHER")).setDisplayName("§a启动持续飞行").setLores("§7点击启用飞行").getItem(), new GUIAction(0, () -> {
                spectatorSettings.setOption(SpectatorSettings.Option.FLY, true);
                player.sendMessage("§a你现在不能停止飞行！");
                if (player.isOnGround()) {
                    player.getLocation().setY(player.getLocation().getY() + 0.1D);
                }
                player.setAllowFlight(true);
                player.setFlying(true);
            }, true));
        }
    }
}
