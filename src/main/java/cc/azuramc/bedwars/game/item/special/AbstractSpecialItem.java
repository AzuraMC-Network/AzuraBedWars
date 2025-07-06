package cc.azuramc.bedwars.game.item.special;

import cc.azuramc.bedwars.game.item.special.impl.RescuePlatform;
import cc.azuramc.bedwars.game.item.special.impl.WarpPowder;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
public abstract class AbstractSpecialItem {
    @Getter
    private static final List<Class<? extends AbstractSpecialItem>> AVAILABLE_SPECIALS = new ArrayList<>();

    public static void loadSpecials() {
        AbstractSpecialItem.AVAILABLE_SPECIALS.add(RescuePlatform.class);
        AbstractSpecialItem.AVAILABLE_SPECIALS.add(WarpPowder.class);
    }

    /**
     * 获取激活状态时物品的材质 （激活指的功能性道具的功能激活）
     *
     * @return 特殊道具功能激活时的Material
     */
    public abstract Material getActivatedMaterial();

    /**
     * 物品未使用时候默认材质
     *
     * @return 特殊道具默认的Material
     */
    public abstract Material getItemMaterial();

}
