package cc.azuramc.bedwars.shop;

import cc.azuramc.bedwars.config.ConfigValidator;
import cc.azuramc.bedwars.config.object.shop.ItemSpec;
import cc.azuramc.bedwars.config.object.shop.Price;
import cc.azuramc.bedwars.config.yaml.ConfigKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks shared by the validators of files that use items, prices and character layouts
 *
 * @author an5w1r@163.com
 */
public final class SpecChecks {

    public static final int ROWS = 6;
    public static final int COLUMNS = 9;

    /**
     * Names of the running server, which tests cannot load
     */
    public interface Names {

        boolean material(String name);

        boolean enchantment(String name);

        boolean effect(String name);

        boolean flag(String name);

        boolean entity(String name);

        boolean sound(String name);
    }

    private final Names names;

    public SpecChecks(Names names) {
        this.names = names;
    }

    public Names names() {
        return names;
    }

    public void item(String document, String path, ItemSpec item, ConfigValidator.Sink sink) {
        material(document, path + ".material", item.getMaterial(), sink);
        for (String enchant : item.getEnchants().keySet()) {
            if (!names.enchantment(enchant)) {
                sink.warn(document, path + ".enchants." + enchant, "Unknown enchantment " + enchant + " on this server, it is left out");
            }
        }
        for (String flag : item.getFlags()) {
            if (!names.flag(flag)) {
                sink.warn(document, path + ".flags", "Unknown item flag " + flag + " on this server, it is left out");
            }
        }
        if (item.getPotion() != null && !names.effect(item.getPotion().getEffect())) {
            sink.warn(document, path + ".potion.effect", "Unknown potion effect " + item.getPotion().getEffect() + " on this server, it is left out");
        }
        if (item.getSpawnEgg() != null && !names.entity(item.getSpawnEgg())) {
            sink.warn(document, path + ".spawn-egg", "Unknown entity " + item.getSpawnEgg() + " on this server, it is left out");
        }
    }

    public void material(String document, String path, String material, ConfigValidator.Sink sink) {
        if (!names.material(material)) {
            sink.warn(document, path, "No material " + material + " on this server, the entry is skipped");
        }
    }

    public static void price(String document, String path, @Nullable Price price, Set<String> currencies, ConfigValidator.Sink sink) {
        if (price != null && !currencies.contains(price.getCurrency())) {
            sink.error(document, path, unknown("resource", price.getCurrency(), currencies));
        }
    }

    /**
     * @param blankRows leading rows that must stay empty
     * @return the characters placed in the layout
     */
    public static Set<String> layout(String document, String path, List<String> layout, int blankRows, ConfigValidator.Sink sink) {
        Set<String> placed = new HashSet<>();
        if (layout.size() > ROWS) {
            sink.error(document, path, "A layout has at most " + ROWS + " rows");
        }
        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            if (line.length() > COLUMNS) {
                sink.error(document, path + "[" + row + "]", "A row has at most " + COLUMNS + " characters");
            }
            if (row < blankRows && !line.trim().isEmpty()) {
                sink.error(document, path + "[" + row + "]", "The first " + blankRows + " rows are the navigation bar and must stay blank");
            }
            for (char c : line.toCharArray()) {
                placed.add(String.valueOf(c));
            }
        }
        return placed;
    }

    public static String unknown(String kind, String value, Collection<String> available) {
        String guess = ConfigKeys.closest(value, available);
        return "Unknown " + kind + " " + value + (guess != null ? ", did you mean " + guess + "?" : ", available: " + String.join(", ", available));
    }
}
