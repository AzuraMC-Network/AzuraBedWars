package cc.azuramc.bedwars.api.game.map;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 地图数据 API
 *
 * @author an5w1r@163.com
 */
public interface IMapData {

    /**
     * 获取地图最大玩家数
     *
     * @return 最大玩家数
     */
    int getMaxPlayers();

    /**
     * 地图数据是否有效（配置完整）
     *
     * @return 有效返回 true
     */
    boolean isValid();

    /**
     * 获取地图建筑高度上限对应的 Y 坐标
     *
     * @return 高位 Y 坐标
     */
    double getHigherY();

    /**
     * 获取区域边界点 1
     *
     * @return 边界点 1，未设置时为 null
     */
    @Nullable
    Location getPos1Location();

    /**
     * 获取区域边界点 2
     *
     * @return 边界点 2，未设置时为 null
     */
    @Nullable
    Location getPos2Location();

    /**
     * 获取所有队伍基地位置
     *
     * @return 基地位置列表（不为 null）
     */
    @NotNull
    List<Location> getBaseLocations();

    /**
     * 获取所有资源点位置
     *
     * @return 资源点位置列表（不为 null）
     */
    @NotNull
    List<Location> getAllDropLocations();

    /**
     * 获取所有商店位置
     *
     * @return 商店位置列表（不为 null）
     */
    @NotNull
    List<Location> getAllShopLocations();
}
