package cc.azuramc.bedwars.extension;

import cc.azuramc.bedwars.config.ConfigProblem;
import cc.azuramc.bedwars.config.ConfigValidator;
import cc.azuramc.bedwars.config.object.TimelineConfig;
import cc.azuramc.bedwars.config.object.UpgradeConfig;
import cc.azuramc.bedwars.config.object.shop.ShopCategoryConfig;
import cc.azuramc.bedwars.config.object.shop.ShopItemSpec;
import cc.azuramc.bedwars.config.yaml.YamlBinder;
import cc.azuramc.bedwars.shop.SpecChecks;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.nodes.Node;

import java.util.List;
import java.util.Map;

/**
 * Checks the {@code <plugin>:<id>} references of the config files and binds their settings.
 * Before the first tick only the form of a reference is checked, other plugins may still register
 *
 * @author an5w1r@163.com
 */
public final class ExtensionValidator implements ConfigValidator {

    private static final String HIDDEN = "the item is hidden";
    private static final String SKIPPED = "it is skipped";

    private final Extensions extensions;

    public ExtensionValidator(Extensions extensions) {
        this.extensions = extensions;
    }

    @Override
    public void validate(View view, Sink sink) {
        for (Map.Entry<String, ShopCategoryConfig> category : view.all(ShopCategoryConfig.class).entrySet()) {
            for (Map.Entry<String, ShopItemSpec> entry : category.getValue().getItems().entrySet()) {
                checkShopItem(category.getKey(), "items." + entry.getKey(), entry.getValue(), sink);
            }
        }

        String upgrades = view.idOf(UpgradeConfig.class);
        UpgradeConfig config = view.get(UpgradeConfig.class);
        for (Map.Entry<String, UpgradeConfig.Upgrade> entry : config.getUpgrades().entrySet()) {
            List<UpgradeConfig.Level> levels = entry.getValue().getLevels();
            for (int i = 0; i < levels.size(); i++) {
                checkEffects(upgrades, "upgrades." + entry.getKey() + ".levels[" + i + "].effects", levels.get(i).getEffects(), ExtensionKind.UPGRADE_EFFECT, sink);
            }
        }
        checkEffects(upgrades, "traps.alert", config.getTraps().getAlert(), ExtensionKind.TRAP_EFFECT, sink);
        for (Map.Entry<String, UpgradeConfig.Trap> entry : config.getTraps().getTypes().entrySet()) {
            String path = "traps.types." + entry.getKey();
            checkEffects(upgrades, path + ".effects", entry.getValue().getEffects(), ExtensionKind.TRAP_EFFECT, sink);
            if (entry.getValue().getAlert() != null) {
                checkEffects(upgrades, path + ".alert", entry.getValue().getAlert(), ExtensionKind.TRAP_EFFECT, sink);
            }
        }

        String timeline = view.idOf(TimelineConfig.class);
        List<TimelineConfig.Stage> stages = view.get(TimelineConfig.class).getStages();
        for (int i = 0; i < stages.size(); i++) {
            List<TimelineConfig.Action> actions = stages.get(i).getActions();
            for (int j = 0; j < actions.size(); j++) {
                TimelineConfig.Action action = actions.get(j);
                String path = "stages[" + i + "].actions[" + j + "]";
                if (ExtensionKind.isReference(action.getType())) {
                    check(timeline, path + ".type", ExtensionKind.TIMELINE_ACTION, action.getType(), path + ".settings", action.getSettings(), SKIPPED, sink);
                } else {
                    unused(timeline, path + ".settings", action.getSettings(), sink);
                }
            }
        }
    }

    private void checkShopItem(String document, String path, ShopItemSpec item, Sink sink) {
        if (item.getCustom() != null) {
            check(document, path + ".custom", ExtensionKind.ITEM, item.getCustom(), path + ".settings", item.getSettings(), HIDDEN, sink);
        } else if (item.getAction() != null) {
            check(document, path + ".action", ExtensionKind.PURCHASE_ACTION, item.getAction(), path + ".settings", item.getSettings(), HIDDEN, sink);
        } else {
            unused(document, path + ".settings", item.getSettings(), sink);
        }
        if (item.getRequires() == null) {
            return;
        }
        List<ShopItemSpec.Condition> conditions = item.getRequires().getConditions();
        for (int i = 0; i < conditions.size(); i++) {
            String conditionPath = path + ".requires.conditions[" + i + "]";
            ShopItemSpec.Condition condition = conditions.get(i);
            check(document, conditionPath + ".type", ExtensionKind.CONDITION, condition.getType(), conditionPath + ".settings", condition.getSettings(), HIDDEN, sink);
        }
    }

    private void checkEffects(String document, String path, List<UpgradeConfig.Effect> effects, ExtensionKind kind, Sink sink) {
        for (int i = 0; i < effects.size(); i++) {
            UpgradeConfig.Effect effect = effects.get(i);
            String effectPath = path + "[" + i + "]";
            if (ExtensionKind.isReference(effect.getType())) {
                check(document, effectPath + ".type", kind, effect.getType(), effectPath + ".settings", effect.getSettings(), SKIPPED, sink);
            } else {
                unused(document, effectPath + ".settings", effect.getSettings(), sink);
            }
        }
    }

    private void check(String document, String path, ExtensionKind kind, String ref, String settingsPath, @Nullable Node settings,
                       String consequence, Sink sink) {
        if (!ExtensionKind.isReference(ref)) {
            sink.error(document, path, "Write the " + kind.getLabel() + " as plugin:id, got " + ref);
            return;
        }
        switch (extensions.status(kind, ref)) {
            case PENDING:
                return;
            case NO_PLUGIN:
                sink.warn(document, path, "Plugin " + ref.substring(0, ref.indexOf(':')) + " is not installed or not enabled, " + consequence);
                return;
            case UNKNOWN:
                sink.error(document, path, SpecChecks.unknown(kind.getLabel(), ref, extensions.settingsTypes(kind).keySet()));
                return;
            default:
                break;
        }
        YamlBinder binder = new YamlBinder(document, true);
        binder.bind(settings, extensions.settingsTypes(kind).get(ref), settingsPath);
        for (ConfigProblem problem : binder.getProblems()) {
            if (problem.isError()) {
                sink.error(document, problem.getPath(), problem.getMessage());
            } else {
                sink.warn(document, problem.getPath(), problem.getMessage());
            }
        }
    }

    private static void unused(String document, String path, @Nullable Node settings, Sink sink) {
        if (settings != null) {
            sink.warn(document, path, "settings is only read by an extension of another plugin, ignored");
        }
    }
}
