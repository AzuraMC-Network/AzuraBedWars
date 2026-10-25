package cc.azuramc.bedwars.extension;

import lombok.Getter;

/**
 * @author an5w1r@163.com
 */
@Getter
public enum ExtensionKind {
    ITEM("item", "custom item"),
    PURCHASE_ACTION("purchase-action", "purchase action"),
    CONDITION("condition", "purchase condition"),
    UPGRADE_EFFECT("upgrade-effect", "upgrade effect"),
    TRAP_EFFECT("trap-effect", "trap effect"),
    TIMELINE_ACTION("timeline-action", "timeline action"),
    PLACEHOLDER("placeholder", "placeholder");

    /** Must match the values of {@link ExtensionSettings} */
    private final String key;
    private final String label;

    ExtensionKind(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public static ExtensionKind of(String key) {
        for (ExtensionKind kind : values()) {
            if (kind.key.equals(key)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown extension kind " + key);
    }

    /**
     * {@code <plugin>:<id>} as written in config files
     */
    public static boolean isReference(String text) {
        return text.indexOf(':') > 0;
    }
}
