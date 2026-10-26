package cc.azuramc.bedwars.upgrade;

import cc.azuramc.bedwars.config.ConfigValidator;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.UpgradeConfig;
import cc.azuramc.bedwars.config.object.shop.ItemSpec;
import cc.azuramc.bedwars.extension.ExtensionKind;
import cc.azuramc.bedwars.shop.SpecChecks;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks upgrades.yml and its references to generators.yml
 *
 * @author an5w1r@163.com
 */
public final class UpgradeValidator implements ConfigValidator {

    public static final char QUEUE_SLOT = 'Q';

    private final SpecChecks checks;

    public UpgradeValidator(SpecChecks.Names names) {
        this.checks = new SpecChecks(names);
    }

    public static String upgradeId(String key, UpgradeConfig.Upgrade upgrade) {
        return upgrade.getId() != null ? upgrade.getId() : key;
    }

    public static String trapId(String key, UpgradeConfig.Trap trap) {
        return trap.getId() != null ? trap.getId() : key;
    }

    @Override
    public void validate(View view, Sink sink) {
        String document = view.idOf(UpgradeConfig.class);
        UpgradeConfig config = view.get(UpgradeConfig.class);
        Set<String> currencies = view.get(GeneratorConfig.class).getResources().keySet();

        Set<String> keys = new HashSet<>();
        Set<String> ids = new HashSet<>();
        for (Map.Entry<String, UpgradeConfig.Upgrade> entry : config.getUpgrades().entrySet()) {
            String path = "upgrades." + entry.getKey();
            UpgradeConfig.Upgrade upgrade = entry.getValue();
            checkKey(document, path, entry.getKey(), keys, sink);
            if (!ids.add(upgradeId(entry.getKey(), upgrade))) {
                sink.error(document, path + ".id", "Another upgrade already has the id " + upgradeId(entry.getKey(), upgrade));
            }
            checks.item(document, path + ".icon", upgrade.getIcon(), sink);
            if (upgrade.getLevels().isEmpty()) {
                sink.error(document, path + ".levels", "An upgrade needs at least one level");
            }
            for (int i = 0; i < upgrade.getLevels().size(); i++) {
                UpgradeConfig.Level level = upgrade.getLevels().get(i);
                SpecChecks.price(document, path + ".levels[" + i + "].price", level.getPrice(), currencies, sink);
                checkEffects(document, path + ".levels[" + i + "].effects", level.getEffects(), currencies, sink);
            }
        }

        UpgradeConfig.Traps traps = config.getTraps();
        if (traps.getPrices().isEmpty()) {
            sink.error(document, "traps.prices", "Traps need at least one price, the default prices are used");
        }
        for (int i = 0; i < traps.getPrices().size(); i++) {
            SpecChecks.price(document, "traps.prices[" + i + "]", traps.getPrices().get(i), currencies, sink);
        }
        checkEffects(document, "traps.alert", traps.getAlert(), currencies, sink);
        Set<String> trapIds = new HashSet<>();
        for (Map.Entry<String, UpgradeConfig.Trap> entry : traps.getTypes().entrySet()) {
            String path = "traps.types." + entry.getKey();
            UpgradeConfig.Trap trap = entry.getValue();
            checkKey(document, path, entry.getKey(), keys, sink);
            if (!trapIds.add(trapId(entry.getKey(), trap))) {
                sink.error(document, path + ".id", "Another trap already has the id " + trapId(entry.getKey(), trap));
            }
            checks.item(document, path + ".icon", trap.getIcon(), sink);
            checkEffects(document, path + ".effects", trap.getEffects(), currencies, sink);
            if (trap.getAlert() != null) {
                checkEffects(document, path + ".alert", trap.getAlert(), currencies, sink);
            }
        }

        for (Map.Entry<String, ItemSpec> entry : config.getDecor().entrySet()) {
            checkKey(document, "decor." + entry.getKey(), entry.getKey(), keys, sink);
            checks.item(document, "decor." + entry.getKey(), entry.getValue(), sink);
        }
        checks.item(document, "text.queue-empty", config.getText().getQueueEmpty(), sink);

        Set<String> placed = SpecChecks.layout(document, "layout", config.getLayout(), 0, sink);
        for (int row = 0; row < config.getLayout().size(); row++) {
            for (char c : config.getLayout().get(row).toCharArray()) {
                if (c != ' ' && c != QUEUE_SLOT && !keys.contains(String.valueOf(c))) {
                    sink.error(document, "layout[" + row + "]", "No upgrade, trap or decor has the key " + c);
                }
            }
        }
        for (String key : keys) {
            if (!placed.contains(key)) {
                sink.warn(document, "layout", "The layout has no " + key + ", it is not shown");
            }
        }
        if (!placed.contains(String.valueOf(QUEUE_SLOT))) {
            sink.warn(document, "layout", "The layout has no " + QUEUE_SLOT + ", the trap queue is not shown");
        }
    }

    private static void checkKey(String document, String path, String key, Set<String> keys, Sink sink) {
        if (key.length() != 1 || key.charAt(0) == QUEUE_SLOT || key.charAt(0) == ' ') {
            sink.error(document, path, "The key must be one character other than " + QUEUE_SLOT + " and space, it is used in the layout");
        } else if (!keys.add(key)) {
            sink.error(document, path, "The key " + key + " is used twice in the layout keys");
        }
    }

    private void checkEffects(String document, String path, List<UpgradeConfig.Effect> effects, Set<String> currencies, Sink sink) {
        for (int i = 0; i < effects.size(); i++) {
            checkEffect(document, path + "[" + i + "]", effects.get(i), currencies, sink);
        }
    }

    private void checkEffect(String document, String path, UpgradeConfig.Effect effect, Set<String> currencies, Sink sink) {
        String type = effect.getType();
        if (ExtensionKind.isReference(type)) {
            return;
        }
        if (!UpgradeConfig.Effect.TYPES.contains(type)) {
            sink.error(document, path + ".type", SpecChecks.unknown("effect type", type, UpgradeConfig.Effect.TYPES));
            return;
        }
        switch (type) {
            case UpgradeConfig.Effect.ENCHANT:
                checkTarget(document, path, effect.getTarget(), UpgradeConfig.Effect.ENCHANT_TARGETS, sink);
                if (effect.getEnchant() == null) {
                    sink.error(document, path + ".enchant", "An enchant effect needs enchant, such as SHARPNESS");
                } else if (!checks.names().enchantment(effect.getEnchant())) {
                    sink.warn(document, path + ".enchant", "Unknown enchantment " + effect.getEnchant() + " on this server, it is left out");
                }
                break;
            case UpgradeConfig.Effect.POTION:
                checkTarget(document, path, effect.getTarget(), UpgradeConfig.Effect.PLAYER_TARGETS, sink);
                if (effect.getEffect() == null) {
                    sink.error(document, path + ".effect", "A potion effect needs effect, such as SPEED");
                } else if (!checks.names().effect(effect.getEffect())) {
                    sink.warn(document, path + ".effect", "Unknown potion effect " + effect.getEffect() + " on this server, it is left out");
                }
                break;
            case UpgradeConfig.Effect.GENERATOR:
                if (effect.getResource() == null) {
                    sink.error(document, path + ".resource", "A generator effect needs resource, an id of generators.yml");
                } else if (!currencies.contains(effect.getResource())) {
                    sink.error(document, path + ".resource", SpecChecks.unknown("resource", effect.getResource(), currencies));
                }
                break;
            case UpgradeConfig.Effect.TITLE:
                checkTarget(document, path, effect.getTarget(), UpgradeConfig.Effect.PLAYER_TARGETS, sink);
                if (effect.getTitle() == null && effect.getSubtitle() == null) {
                    sink.error(document, path + ".title", "A title effect needs title or subtitle");
                }
                break;
            case UpgradeConfig.Effect.SOUND:
                checkTarget(document, path, effect.getTarget(), UpgradeConfig.Effect.PLAYER_TARGETS, sink);
                if (effect.getSound() == null) {
                    sink.error(document, path + ".sound", "A sound effect needs sound, such as ENTITY_ENDERMAN_TELEPORT");
                } else if (!checks.names().sound(effect.getSound())) {
                    sink.warn(document, path + ".sound", "Unknown sound " + effect.getSound() + " on this server, it is left out");
                }
                break;
            case UpgradeConfig.Effect.MESSAGE:
                checkTarget(document, path, effect.getTarget(), UpgradeConfig.Effect.PLAYER_TARGETS, sink);
                if (effect.getMessage().isEmpty()) {
                    sink.error(document, path + ".message", "A message effect needs message lines");
                }
                break;
            case UpgradeConfig.Effect.COMMAND:
                if (effect.getCommands().isEmpty()) {
                    sink.error(document, path + ".commands", "A command effect needs commands");
                }
                break;
            default:
                break;
        }
    }

    private static void checkTarget(String document, String path, String target, List<String> targets, Sink sink) {
        if (target == null) {
            sink.error(document, path + ".target", "Missing target, one of " + String.join(", ", targets));
        } else if (!targets.contains(target)) {
            sink.error(document, path + ".target", SpecChecks.unknown("target", target, targets));
        }
    }
}
