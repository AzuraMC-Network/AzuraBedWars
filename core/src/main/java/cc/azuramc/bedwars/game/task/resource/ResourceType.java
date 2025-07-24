package cc.azuramc.bedwars.game.task.resource;

import cc.azuramc.bedwars.game.map.MapData;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.Material;

/**
 * @author An5w1r@163.com
 */
@Getter
public enum ResourceType {
    IRON(XMaterial.IRON_INGOT.get(), MapData.DropType.BASE),
    GOLD(XMaterial.GOLD_INGOT.get(), MapData.DropType.BASE),
    DIAMOND(XMaterial.DIAMOND.get(), MapData.DropType.DIAMOND),
    EMERALD(XMaterial.EMERALD.get(), MapData.DropType.EMERALD);

    private final Material material;
    private final MapData.DropType dropType;

    ResourceType(Material material, MapData.DropType dropType) {
        this.material = material;
        this.dropType = dropType;
    }

}
