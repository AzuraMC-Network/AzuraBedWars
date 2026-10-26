package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.map.MapData;
import cc.azuramc.bedwars.game.task.generator.ResourceHologram;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private static final double RESOURCE_LINE_SPACING = 0.27D;
    /** Must match the fall distance ArmorStandUtil animates */
    private static final float BLOCK_FALL_DISTANCE = 7.0F;

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

        for (Map.Entry<String, GeneratorConfig.Resource> entry : plugin.getGeneratorConfig().getResources().entrySet()) {
            GeneratorConfig.Resource resource = entry.getValue();
            if (resource.getAt() != GeneratorConfig.Spawn.PUBLIC || resource.getDrop() == null) {
                continue;
            }
            List<ResourceHologram> holograms = new ArrayList<>();
            for (Location location : mapData.getDropLocations(resource.getDrop())) {
                ensureChunkLoaded(location);
                holograms.add(spawnResourceHologram(location.clone().add(0.0D, RESOURCE_GENERATOR_Y_OFFSET, 0.0D), resource));
            }
            gameManager.getResourceHolograms().put(entry.getKey(), holograms);
        }
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
                plugin.getLang().getShop().getItemShopName()
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
                plugin.getLang().getShop().getUpgradeShopName()
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

        // 创建村民
        Villager villager = Objects.requireNonNull(spawnLocation.getWorld()).spawn(spawnLocation, Villager.class);
        configureShopVillager(villager, profession, plugin, metadataKey);
        plugin.getNmsAccess().setAI(villager, false);

        // 1.8 still moves mobs without AI by gravity and pushing
        if (VersionUtil.isVersion1_8()) {
            Location standLocation = spawnLocation.clone().add(0.0D, VILLAGER_STAND_Y_OFFSET, 0.0D);
            ArmorStand armorStand = spawnLocation.getWorld().spawn(standLocation, ArmorStand.class);
            configureInvisibleArmorStand(armorStand);
            mountVillagerOnArmorStand(armorStand, villager);
        }

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

        villager.setMetadata(metadataKey, new FixedMetadataValue(plugin.getPlugin(), metadataKey));
    }

    /**
     * 配置不可见盔甲架
     */
    private static void configureInvisibleArmorStand(ArmorStand armorStand) {
        armorStand.setGravity(false);
        armorStand.setVisible(false);
    }

    /**
     * 将村民挂载到盔甲架上
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
    }

    private static ResourceHologram spawnResourceHologram(Location base, GeneratorConfig.Resource resource) {
        ArmorStand block = null;
        Material blockMaterial = resource.getBlock() != null
                ? XMaterial.matchXMaterial(resource.getBlock()).map(XMaterial::get).orElse(null) : null;
        if (blockMaterial != null) {
            block = createResourceArmorStand(base, BLOCK_FALL_DISTANCE, false);
            block.setHelmet(new ItemStack(blockMaterial));
        }
        List<ArmorStand> lines = new ArrayList<>();
        int count = resource.getHologram().size();
        for (int i = 0; i < count; i++) {
            lines.add(createResourceArmorStand(base.clone().add(0.0D, (count - 1 - i) * RESOURCE_LINE_SPACING, 0.0D), 0.0F, true));
        }
        return new ResourceHologram(block, lines);
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
