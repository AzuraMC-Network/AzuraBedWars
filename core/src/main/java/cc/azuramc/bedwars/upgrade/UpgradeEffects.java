package cc.azuramc.bedwars.upgrade;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.UpgradeConfig;
import cc.azuramc.bedwars.config.object.UpgradeConfig.Effect;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.task.generator.GeneratorManager;
import cc.azuramc.bedwars.game.task.generator.PrivateResourceGenerator;
import cc.azuramc.bedwars.upgrade.gui.TeamUpgradeGUI;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Runs the effects of upgrades.yml
 *
 * @author an5w1r@163.com
 */
public final class UpgradeEffects {

    private UpgradeEffects() {
    }

    static void onUpgrade(GameTeam gameTeam, UpgradeConfig.Level level) {
        boolean enchanted = false;
        for (Effect effect : level.getEffects()) {
            switch (effect.getType()) {
                case Effect.ENCHANT:
                    enchanted = true;
                    break;
                case Effect.GENERATOR:
                    applyGenerator(gameTeam, effect);
                    break;
                case Effect.POTION:
                    // applied every second in tick
                    break;
                default:
                    run(effect, gameTeam, null);
                    break;
            }
        }
        if (enchanted) {
            for (GamePlayer gamePlayer : gameTeam.getAlivePlayers()) {
                for (ItemStack item : gamePlayer.getPlayer().getInventory().getContents()) {
                    enchantSword(gameTeam, item);
                }
                enchantArmor(gamePlayer);
            }
        }
    }

    public static void enchantArmor(GamePlayer gamePlayer) {
        GameTeam gameTeam = Objects.requireNonNull(gamePlayer.getGameTeam());
        ItemStack[] armor = gamePlayer.getPlayer().getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            enchant(piece, enchantments(gameTeam, Effect.ARMOR));
        }
        enchant(armor[0], enchantments(gameTeam, Effect.BOOTS));
        gamePlayer.updateInventory();
    }

    public static void enchantSword(GameTeam gameTeam, ItemStack item) {
        if (item != null && item.getType().name().endsWith("SWORD")) {
            enchant(item, enchantments(gameTeam, Effect.SWORD));
        }
    }

    private static void enchant(@Nullable ItemStack item, Map<Enchantment, Integer> enchantments) {
        if (item == null) {
            return;
        }
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }
    }

    private static Map<Enchantment, Integer> enchantments(GameTeam gameTeam, String target) {
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
        for (Effect effect : ownedEffects(gameTeam, Effect.ENCHANT)) {
            Enchantment enchantment = effect.getEnchant() == null ? null : XEnchantment.of(effect.getEnchant()).map(XEnchantment::get).orElse(null);
            if (enchantment != null && target.equals(effect.getTarget())) {
                enchantments.put(enchantment, effect.getLevel());
            }
        }
        return enchantments;
    }

    /**
     * Effects of every owned level in level order, so a later level overrides an earlier one
     */
    private static List<Effect> ownedEffects(GameTeam gameTeam, String type) {
        List<Effect> effects = new ArrayList<>();
        for (Map.Entry<String, UpgradeConfig.Upgrade> entry : config().getUpgrades().entrySet()) {
            UpgradeConfig.Upgrade upgrade = entry.getValue();
            int owned = gameTeam.getUpgrades().getLevel(UpgradeValidator.upgradeId(entry.getKey(), upgrade));
            for (int level = 0; level < owned && level < upgrade.getLevels().size(); level++) {
                for (Effect effect : upgrade.getLevels().get(level).getEffects()) {
                    if (type.equals(effect.getType())) {
                        effects.add(effect);
                    }
                }
            }
        }
        return effects;
    }

    private static void applyGenerator(GameTeam gameTeam, Effect effect) {
        GeneratorConfig.Resource resource = effect.getResource() == null ? null
                : AzuraBedWars.getInstance().getGeneratorConfig().resource(effect.getResource());
        Material material = resource == null ? null : GeneratorManager.material(resource);
        if (material == null || resource.getLevels().isEmpty()) {
            return;
        }
        GeneratorManager generatorManager = gameTeam.getGameManager().getGeneratorManager();
        PrivateResourceGenerator generator = generatorManager.getPrivateResourceGenerator(effect.getResource(), gameTeam);
        if (generator == null) {
            GeneratorConfig.Level first = resource.level(1);
            generatorManager.addPrivateResourceTask(new PrivateResourceGenerator(gameTeam.getGameManager(),
                    GeneratorManager.privateName(effect.getResource(), gameTeam), gameTeam.getResourceDropLocation(), material,
                    effect.getMaxStack() != null ? effect.getMaxStack() : first.getMaxStack()),
                    GeneratorManager.ticks(effect.getInterval() != null ? effect.getInterval() : first.getInterval()));
            return;
        }
        if (effect.getMaxStack() != null) {
            generator.setMaxStack(effect.getMaxStack());
        }
        if (effect.getInterval() != null) {
            generator.setInterval(GeneratorManager.ticks(effect.getInterval()));
            generator.restartTask();
        }
    }

    public static void tick(GameManager gameManager) {
        UpgradeConfig config = config();
        for (GameTeam gameTeam : gameManager.getGameTeams()) {
            Map<String, Effect> potions = new LinkedHashMap<>();
            for (Effect effect : ownedEffects(gameTeam, Effect.POTION)) {
                potions.put(effect.getEffect() + "@" + effect.getTarget(), effect);
            }
            for (Effect effect : potions.values()) {
                run(effect, gameTeam, null);
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
            String id = gameTeam.getUpgrades().pollTrap();
            if (id == null) {
                return;
            }
            intruder.startTrapTriggerCooldownTask();
            UpgradeConfig.Trap trap = trap(config, id);
            if (trap != null) {
                triggerTrap(gameTeam, intruder, trap, config);
            }
        }
    }

    @Nullable
    public static UpgradeConfig.Trap trap(UpgradeConfig config, String id) {
        for (Map.Entry<String, UpgradeConfig.Trap> entry : config.getTraps().getTypes().entrySet()) {
            if (UpgradeValidator.trapId(entry.getKey(), entry.getValue()).equals(id)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static void triggerTrap(GameTeam gameTeam, GamePlayer intruder, UpgradeConfig.Trap trap, UpgradeConfig config) {
        if (!intruder.isHasTrapProtection()) {
            for (Effect effect : trap.getEffects()) {
                run(effect, gameTeam, intruder);
            }
        }
        for (Effect effect : trap.getAlert() != null ? trap.getAlert() : config.getTraps().getAlert()) {
            run(effect, gameTeam, intruder);
        }
        TeamUpgradeGUI.refresh(gameTeam);
    }

    /**
     * Runs an effect that acts on players, enchant and generator effects are handled on purchase
     */
    @SuppressWarnings("deprecation")
    private static void run(Effect effect, GameTeam gameTeam, @Nullable GamePlayer intruder) {
        if (Effect.REVEAL.equals(effect.getType())) {
            if (intruder != null && intruder.isInvisible()) {
                intruder.endInvisibility();
            }
            return;
        }
        if (Effect.COMMAND.equals(effect.getType())) {
            for (String command : effect.getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replace(command, gameTeam, intruder));
            }
            return;
        }
        for (GamePlayer target : targets(effect.getTarget(), gameTeam, intruder)) {
            Player player = target.getPlayer();
            switch (effect.getType()) {
                case Effect.POTION:
                    PotionEffectType type = effect.getEffect() == null ? null : XPotion.of(effect.getEffect()).map(XPotion::get).orElse(null);
                    if (type != null) {
                        player.addPotionEffect(new PotionEffect(type, (int) Math.round(effect.getSeconds() * 20), effect.getLevel() - 1), true);
                    }
                    break;
                case Effect.TITLE:
                    target.sendTitle(replace(Objects.toString(effect.getTitle(), ""), gameTeam, intruder),
                            effect.getSubtitle() == null ? null : replace(effect.getSubtitle(), gameTeam, intruder),
                            effect.getFadeIn(), effect.getStay(), effect.getFadeOut());
                    break;
                case Effect.SOUND:
                    Sound sound = effect.getSound() == null ? null : XSound.of(effect.getSound()).map(XSound::get).orElse(null);
                    if (sound != null) {
                        target.playSound(sound, effect.getVolume(), effect.getPitch());
                    }
                    break;
                case Effect.MESSAGE:
                    for (String line : effect.getMessage()) {
                        target.sendMessage(replace(line, gameTeam, intruder));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static List<GamePlayer> targets(@Nullable String target, GameTeam gameTeam, @Nullable GamePlayer intruder) {
        if (Effect.INTRUDER.equals(target)) {
            return intruder != null ? Collections.singletonList(intruder) : Collections.<GamePlayer>emptyList();
        }
        List<GamePlayer> members = new ArrayList<>(gameTeam.getAlivePlayers());
        if (Effect.BASE.equals(target)) {
            members.removeIf(member -> !isNear(member.getPlayer(), gameTeam.getSpawnLocation(), config().getBaseRange()));
        }
        return members;
    }

    private static String replace(String text, GameTeam gameTeam, @Nullable GamePlayer intruder) {
        GameTeam intruderTeam = intruder != null ? intruder.getGameTeam() : null;
        return text.replace("{team}", gameTeam.getName())
                .replace("{intruder-team}", intruderTeam != null ? intruderTeam.getName() : "")
                .replace("{intruder}", intruder != null ? intruder.getName() : "");
    }

    private static boolean isNear(Player player, Location center, double range) {
        return Objects.equals(player.getWorld(), center.getWorld()) && player.getLocation().distance(center) <= range;
    }

    private static UpgradeConfig config() {
        return AzuraBedWars.getInstance().getUpgradeConfig();
    }
}
