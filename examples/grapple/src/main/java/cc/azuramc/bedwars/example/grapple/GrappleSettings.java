package cc.azuramc.bedwars.example.grapple;

import cc.azuramc.bedwars.api.config.Comment;
import cc.azuramc.bedwars.api.config.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The {@code settings} section of a shop item with {@code custom: grapple:hook}
 *
 * @author an5w1r@163.com
 */
public class GrappleSettings {

    @Comment("Farthest block the hook reaches")
    @Range(min = 5, max = 60)
    int range = 30;

    @Comment("Seconds between two pulls")
    @Range(min = 0)
    double cooldown = 3;

    @Comment("Speed of the pull")
    @Range(min = 0.1, max = 5)
    double strength = 1.6;

    @Comment("Placeholders: {seconds}")
    String cooling = "&cThe grapple is ready in {seconds}s";

    String miss = "&cNothing to hook onto";

    List<String> lore = new ArrayList<>(Arrays.asList("&7Right click to pull yourself", "&7toward the block you look at"));
}
