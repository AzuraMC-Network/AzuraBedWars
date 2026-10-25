package cc.azuramc.bedwars.upgrade;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.UpgradeConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.task.generator.GeneratorManager;
import cc.azuramc.bedwars.game.task.generator.PrivateResourceGenerator;
import cc.azuramc.bedwars.upgrade.gui.TeamUpgradeGUI;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
public final class UpgradeEffects {

    private UpgradeEffects() {
    }

    static void onUpgrade(GameTeam gameTeam, TeamUpgrade upgrade, int level) {
        switch (upgrade) {
            case SHARPENED_SWORDS:
                for (GamePlayer gamePlayer : gameTeam.getAlivePlayers()) {
                    for (ItemStack item : gamePlayer.getPlayer().getInventory().getContents()) {
                        enchantSword(gameTeam, item);
                    }
                }
                break;
            case REINFORCED_ARMOR:
            case CUSHIONED_BOOTS:
                gameTeam.getAlivePlayers().forEach(UpgradeEffects::enchantArmor);
                break;
            case RESOURCE_FURNACE:
                upgradeFurnace(gameTeam, level);
                break;
            default:
                // haste and healing are applied every second in tick
                break;
        }
    }

    public static void enchantArmor(GamePlayer gamePlayer) {
        GameTeam gameTeam = Objects.requireNonNull(gamePlayer.getGameTeam());
        int protection = gameTeam.getUpgrades().getLevel(TeamUpgrade.REINFORCED_ARMOR);
        int featherFalling = gameTeam.getUpgrades().getLevel(TeamUpgrade.CUSHIONED_BOOTS);
        ItemStack[] armor = gamePlayer.getPlayer().getInventory().getArmorContents();

        for (ItemStack piece : armor) {
            if (piece != null && protection > 0 && XEnchantment.PROTECTION.get() != null) {
                piece.addEnchantment(XEnchantment.PROTECTION.get(), protection);
            }
        }
        if (armor[0] != null && featherFalling > 0 && XEnchantment.FEATHER_FALLING.get() != null) {
            armor[0].addEnchantment(XEnchantment.FEATHER_FALLING.get(), featherFalling);
        }
        gamePlayer.updateInventory();
    }

    public static void enchantSword(GameTeam gameTeam, ItemStack item) {
        if (item == null || !item.getType().name().endsWith("SWORD")) {
            return;
        }
        if (gameTeam.getUpgrades().getLevel(TeamUpgrade.SHARPENED_SWORDS) > 0 && XEnchantment.SHARPNESS.get() != null) {
            item.addEnchantment(XEnchantment.SHARPNESS.get(), 1);
        }
    }

    private static void upgradeFurnace(GameTeam gameTeam, int level) {
        GeneratorManager generatorManager = gameTeam.getGameManager().getGeneratorManager();
        PrivateResourceGenerator iron = generatorManager.getPrivateResourceGenerator("铁锭" + gameTeam.getName());
        PrivateResourceGenerator gold = generatorManager.getPrivateResourceGenerator("金锭" + gameTeam.getName());

        switch (level) {
            case 1:
                iron.setInterval(20L);
                iron.setMaxStack(48);
                gold.setInterval(20L * 3);
                gold.setMaxStack(8);
                break;
            case 2:
                iron.setInterval(16L);
                iron.setMaxStack(72);
                gold.setInterval(48L);
                gold.setMaxStack(12);
                break;
            case 3:
                iron.setInterval(12L);
                iron.setMaxStack(96);
                gold.setInterval(20L * 2);
                gold.setMaxStack(16);
                generatorManager.addPrivateResourceTask(new PrivateResourceGenerator(gameTeam.getGameManager(),
                        "绿宝石" + gameTeam.getName(), gameTeam.getResourceDropLocation(), XMaterial.EMERALD.get(), 2), 20L * 60);
                break;
            default:
                iron.setInterval(8L);
                iron.setMaxStack(128);
                gold.setInterval(32L);
                gold.setMaxStack(24);
                generatorManager.getPrivateResourceGenerator("绿宝石" + gameTeam.getName()).setInterval(20L * 30);
                break;
        }
    }

    public static void tick(GameManager gameManager) {
        UpgradeConfig config = AzuraBedWars.getInstance().getUpgradeConfig();
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            TeamUpgrades upgrades = gameTeam.getUpgrades();
            int haste = upgrades.getLevel(TeamUpgrade.MANIC_MINER);
            boolean healingPool = upgrades.getLevel(TeamUpgrade.HEALING_POOL) > 0;

            for (GamePlayer member : gameTeam.getAlivePlayers()) {
                Player player = member.getPlayer();
                if (haste > 0) {
                    addEffect(player, XPotion.HASTE.get(), config.getHasteEffectDuration(), haste - 1);
                }
                if (healingPool && isNear(player, gameTeam.getSpawnLocation(), config.getHealingPoolRange())) {
                    addEffect(player, XPotion.REGENERATION.get(), config.getRegenerationEffectDuration(), config.getRegenerationEffectAmplifier());
                }
            }

            if (!gameTeam.isDead()) {
                checkIntruders(gameTeam, config);
            }
        }
    }

    private static void checkIntruders(GameTeam gameTeam, UpgradeConfig config) {
        for (GamePlayer intruder : GamePlayer.getOnlinePlayers()) {
            if (intruder.isSpectator() || gameTeam.isInTeam(intruder) || intruder.isTrapTriggerCooldown()
                    || !isNear(intruder.getPlayer(), gameTeam.getSpawnLocation(), config.getTrapTriggerRange())) {
                continue;
            }
            TrapType trap = gameTeam.getUpgrades().pollTrap();
            if (trap == null) {
                return;
            }
            intruder.startTrapTriggerCooldownTask();
            triggerTrap(gameTeam, intruder, trap, config);
        }
    }

    private static void triggerTrap(GameTeam gameTeam, GamePlayer intruder, TrapType trap, UpgradeConfig config) {
        if (!intruder.isHasTrapProtection()) {
            Player player = intruder.getPlayer();
            switch (trap) {
                case BLINDNESS:
                    addEffect(player, XPotion.BLINDNESS.get(), config.getTrapEffectDuration(), config.getTrapEffectAmplifier());
                    addEffect(player, XPotion.SLOWNESS.get(), config.getTrapEffectDuration(), config.getTrapEffectAmplifier());
                    break;
                case FIGHT_BACK:
                    for (GamePlayer member : gameTeam.getAlivePlayers()) {
                        addEffect(member.getPlayer(), XPotion.SPEED.get(), 15 * 20, 1);
                        addEffect(member.getPlayer(), XPotion.JUMP_BOOST.get(), 15 * 20, 1);
                    }
                    break;
                case ALARM:
                    if (intruder.isInvisible()) {
                        intruder.endInvisibility();
                    }
                    break;
                case MINER:
                    addEffect(player, XPotion.MINING_FATIGUE.get(), config.getMiningFatigueEffectDuration(), config.getMiningFatigueEffectAmplifier());
                    break;
                default:
                    break;
            }
        }

        GameTeam intruderTeam = intruder.getGameTeam();
        String subtitle = trap == TrapType.ALARM && intruderTeam != null ? "§e触发者 " + intruderTeam.getName() + " 队伍" : null;
        for (GamePlayer member : gameTeam.getAlivePlayers()) {
            member.sendTitle("§c§l陷阱触发！", subtitle, 0, 40, 0);
            if (XSound.ENTITY_ENDERMAN_TELEPORT.get() != null) {
                member.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
            }
        }
        TeamUpgradeGUI.refresh(gameTeam);
    }

    private static boolean isNear(Player player, Location center, double range) {
        return Objects.equals(player.getWorld(), center.getWorld()) && player.getLocation().distance(center) <= range;
    }

    private static void addEffect(Player player, PotionEffectType type, int ticks, int amplifier) {
        if (type != null) {
            player.addPotionEffect(new PotionEffect(type, ticks, amplifier));
        }
    }
}
