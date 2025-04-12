package cc.azuramc.bedwars.database.map;

import cc.azuramc.bedwars.database.map.data.MapData;

/**
 * 地图数据存储接口
 * 定义了地图数据存储的基本操作和迁移功能
 */
public interface IMapStorage {
    
    /**
     * 保存地图数据
     * @param mapName 地图名称
     * @param mapData 地图数据对象
     * @return 是否保存成功
     */
    boolean saveMap(String mapName, MapData mapData);
    
    /**
     * 加载地图数据
     * @param mapName 地图名称
     * @return 地图数据对象，如果不存在则返回null
     */
    MapData loadMap(String mapName);
    
    /**
     * 删除地图数据
     * @param mapName 地图名称
     * @return 是否删除成功
     */
    boolean deleteMap(String mapName);
    
    /**
     * 检查地图是否存在
     * @param mapName 地图名称
     * @return 地图是否存在
     */
    boolean exists(String mapName);
    
    /**
     * 获取所有可用的地图名称列表
     * @return 地图名称列表
     */
    java.util.List<String> getAllMapNames();
    
    /**
     * 将数据迁移到另一个存储实现
     * @param targetStorage 目标存储实现
     * @param mapName 要迁移的地图名称，如果为null则迁移所有地图
     * @return 是否迁移成功
     */
    boolean migrateTo(IMapStorage targetStorage, String mapName);
} 