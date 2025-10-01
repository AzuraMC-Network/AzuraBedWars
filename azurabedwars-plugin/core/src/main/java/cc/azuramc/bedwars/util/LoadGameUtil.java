package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.map.MapData;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;
import java.util.Set;

/**
 * @author an5w1r@163.com
 */
public class LoadGameUtil {

    public static final String ITEM_SHOP_METADATA_KEY = "SHOP_VILLAGER";
    public static final String UPGRADE_SHOP_METADATA_KEY = "UPGRADE_VILLAGER";
    // 村民站立盔甲架的Y轴偏移
    private static final double VILLAGER_STAND_Y_OFFSET = -1.5D;
    // 名称标签的Y轴偏移
    private static final double NAME_TAG_Y_OFFSET = -0.2D;
    // 名称标签微调偏移
    private static final double NAME_TAG_FINE_TUNE = 0.25D;
    // 资源生成点的Y轴偏移
    private static final double RESOURCE_GENERATOR_Y_OFFSET = 1.0D;
    // 资源名称第一层偏移
    private static final double RESOURCE_NAME_OFFSET_1 = 0.25D;
    // 资源名称第二层偏移
    private static final double RESOURCE_NAME_OFFSET_2 = 0.28D;
    private static final float FALL_DISTANCE_7 = 7.0F;
    private static final float FALL_DISTANCE_6 = 6.0F;
    private static final float FALL_DISTANCE_5 = 5.0F;
    private static final float FALL_DISTANCE_4 = 4.0F;
    private static final String ITEM_SHOP_DISPLAY_NAME = "§b§l物品商人";
    private static final String UPGRADE_SHOP_DISPLAY_NAME = "§e§l团队升级";

    public static void spawnAll(AzuraBedWars plugin) {
        GameManager gameManager = plugin.getGameManager();
        MapData mapData = gameManager.getMapData();

        // 生成物品商店
        spawnShopsAtLocations(
                mapData.getShopLocations(MapData.ShopType.ITEM),
                loc -> spawnShopVillager(plugin, loc)
        );

        // 生成升级商店
        spawnShopsAtLocations(
                mapData.getShopLocations(MapData.ShopType.UPGRADE),
                loc -> spawnUpgradeVillager(plugin, loc)
        );

        // 生成钻石生成点
        spawnResourceGenerators(
                gameManager,
                mapData.getDropLocations(MapData.DropType.DIAMOND),
                MapData.DropType.DIAMOND
        );

        // 生成绿宝石生成点
        spawnResourceGenerators(
                gameManager,
                mapData.getDropLocations(MapData.DropType.EMERALD),
                MapData.DropType.EMERALD
        );
    }

    /**
     * 在指定位置批量生成商店
     *
     * @param locations 位置列表
     * @param spawner   生成器函数
     */
    private static void spawnShopsAtLocations(Iterable<Location> locations, LocationConsumer spawner) {
        for (Location location : locations) {
            ensureChunkLoaded(location);
            spawner.accept(location);
        }
    }

    /**
     * 在指定位置批量生成资源生成器
     *
     * @param gameManager 游戏管理器
     * @param locations   位置列表
     * @param dropType    掉落类型
     */
    private static void spawnResourceGenerators(GameManager gameManager, Iterable<Location> locations, MapData.DropType dropType) {
        for (Location location : locations) {
            ensureChunkLoaded(location);
            spawnResourceGenerator(gameManager, location.clone(), dropType);
        }
    }

    /**
     * 确保区块已加载
     *
     * @param location 位置
     */
    private static void ensureChunkLoaded(Location location) {
        if (!location.getChunk().isLoaded()) {
            location.getChunk().load();
        }
    }

    /**
     * 生成物品商店村民
     *
     * @param plugin   插件实例
     * @param location 生成位置
     */
    private static void spawnShopVillager(AzuraBedWars plugin, Location location) {
        spawnShopVillagerWithConfig(
                plugin,
                location,
                Villager.Profession.LIBRARIAN,
                ITEM_SHOP_METADATA_KEY,
                ITEM_SHOP_DISPLAY_NAME
        );
    }

    /**
     * 生成升级商店村民
     *
     * @param plugin   插件实例
     * @param location 生成位置
     */
    private static void spawnUpgradeVillager(AzuraBedWars plugin, Location location) {
        spawnShopVillagerWithConfig(
                plugin,
                location,
                Villager.Profession.FARMER,
                UPGRADE_SHOP_METADATA_KEY,
                UPGRADE_SHOP_DISPLAY_NAME
        );
    }

    /**
     * 使用配置生成商店村民（统一的商店村民生成逻辑）
     *
     * @param plugin      插件实例
     * @param location    生成位置
     * @param profession  村民职业
     * @param metadataKey 元数据键
     * @param displayName 显示名称
     */
    private static void spawnShopVillagerWithConfig(
            AzuraBedWars plugin,
            Location location,
            Villager.Profession profession,
            String metadataKey,
            String displayName) {

        Location spawnLocation = location.clone();
        Location standLocation = spawnLocation.clone().add(0.0D, VILLAGER_STAND_Y_OFFSET, 0.0D);

        // 创建盔甲架作为村民的底座
        ArmorStand armorStand = Objects.requireNonNull(spawnLocation.getWorld())
                .spawn(standLocation, ArmorStand.class);
        configureInvisibleArmorStand(armorStand);

        // 创建村民
        Villager villager = spawnLocation.getWorld().spawn(spawnLocation, Villager.class);
        configureShopVillager(villager, profession, plugin, metadataKey);

        // 将村民放在盔甲架上
        mountVillagerOnArmorStand(armorStand, villager);

        // 创建名称标签
        Location nameTagLocation = spawnLocation.add(0.0D, NAME_TAG_Y_OFFSET, 0.0D);
        createNameTagArmorStand(nameTagLocation, displayName);
    }

    /**
     * 配置商店村民属性
     */
    private static void configureShopVillager(
            Villager villager,
            Villager.Profession profession,
            AzuraBedWars plugin,
            String metadataKey) {

        villager.setCustomNameVisible(false);

        if (villager.getType() == EntityType.VILLAGER) {
            villager.setProfession(profession);
        }

        villager.setMetadata(metadataKey, new FixedMetadataValue(plugin, metadataKey));
    }

    /**
     * 配置不可见盔甲架
     */
    private static void configureInvisibleArmorStand(ArmorStand armorStand) {
        armorStand.setGravity(false);
        armorStand.setVisible(false);
    }

    /**
     * 将村民挂载到盔甲架上（处理版本兼容性）
     */
    private static void mountVillagerOnArmorStand(ArmorStand armorStand, Villager villager) {
        armorStand.setPassenger(villager);
    }

    /**
     * 创建名称标签盔甲架
     *
     * @param location    位置
     * @param displayName 显示名称
     */
    private static void createNameTagArmorStand(Location location, String displayName) {
        Location adjustedLocation = location.clone().add(0.0D, NAME_TAG_FINE_TUNE, 0.0D);
        ArmorStand nameTag = Objects.requireNonNull(location.getWorld())
                .spawn(adjustedLocation, ArmorStand.class);

        nameTag.setCustomName(displayName);
        nameTag.setCustomNameVisible(true);
        nameTag.setGravity(false);
        nameTag.setVisible(false);
        nameTag.setFallDistance(FALL_DISTANCE_5);
    }

    /**
     * 生成资源生成点（钻石或绿宝石）
     *
     * @param gameManager 游戏管理器
     * @param location    生成位置
     * @param dropType    掉落类型
     */
    private static void spawnResourceGenerator(
            GameManager gameManager,
            Location location,
            MapData.DropType dropType) {

        // 调整基础位置
        location.add(0.0D, RESOURCE_GENERATOR_Y_OFFSET, 0.0D);

        // 确定使用的材料和存储集合
        XMaterial blockMaterial = getBlockMaterialForDropType(dropType);
        Set<ArmorStand> storageSet = getStorageSetForDropType(gameManager, dropType);

        // 生成四层盔甲架
        spawnResourceGeneratorLayers(location, blockMaterial, storageSet);
    }

    /**
     * 根据掉落类型获取方块材料
     */
    private static XMaterial getBlockMaterialForDropType(MapData.DropType dropType) {
        return dropType == MapData.DropType.DIAMOND ? XMaterial.DIAMOND_BLOCK : XMaterial.EMERALD_BLOCK;
    }

    /**
     * 根据掉落类型获取存储集合
     */
    private static Set<ArmorStand> getStorageSetForDropType(GameManager gameManager, MapData.DropType dropType) {
        return dropType == MapData.DropType.DIAMOND
                ? gameManager.getArmorStand()
                : gameManager.getArmorSande();
    }

    /**
     * 生成资源生成点的四层盔甲架
     */
    private static void spawnResourceGeneratorLayers(
            Location baseLocation,
            XMaterial blockMaterial,
            Set<ArmorStand> storageSet) {

        // 第一层 显示方块（钻石块或绿宝石块）
        ArmorStand blockStand = createResourceArmorStand(
                baseLocation,
                FALL_DISTANCE_7,
                false
        );
        blockStand.setHelmet(new ItemStack(blockMaterial.get()));
        storageSet.add(blockStand);

        // 第二层 第一个名称标签
        ArmorStand nameTag1 = createResourceArmorStand(
                baseLocation,
                FALL_DISTANCE_6,
                true
        );
        storageSet.add(nameTag1);

        // 第三层 第二个名称标签
        Location layer3Location = baseLocation.clone().add(0.0D, RESOURCE_NAME_OFFSET_1, 0.0D);
        ArmorStand nameTag2 = createResourceArmorStand(
                layer3Location,
                FALL_DISTANCE_5,
                true
        );
        storageSet.add(nameTag2);

        // 第四层 第三个名称标签
        Location layer4Location = layer3Location.clone().add(0.0D, RESOURCE_NAME_OFFSET_2, 0.0D);
        ArmorStand nameTag3 = createResourceArmorStand(
                layer4Location,
                FALL_DISTANCE_4,
                true
        );
        storageSet.add(nameTag3);
    }

    /**
     * 创建资源生成点的盔甲架
     *
     * @param location     位置
     * @param fallDistance 坠落距离
     * @param showName     是否显示名称
     * @return 创建的盔甲架
     */
    private static ArmorStand createResourceArmorStand(
            Location location,
            float fallDistance,
            boolean showName) {

        ArmorStand armorStand = Objects.requireNonNull(location.getWorld())
                .spawn(location, ArmorStand.class);

        armorStand.setCustomNameVisible(showName);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setFallDistance(fallDistance);

        return armorStand;
    }

    /**
     * 函数式接口 用于位置消费者
     */
    @FunctionalInterface
    private interface LocationConsumer {
        void accept(Location location);
    }
}
