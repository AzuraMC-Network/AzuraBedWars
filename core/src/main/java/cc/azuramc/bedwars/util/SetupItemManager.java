package cc.azuramc.bedwars.util;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于快速配置地图的物品管理器
 * 提供特殊物品来绑定指令，实现快速配置多队伍游戏地图
 *
 * @author an5w1r@163.com
 */
public class SetupItemManager {

    public static final String ITEM_SET_WAITING = "设置等待大厅";
    public static final String ITEM_SET_RESPAWN = "设置重生点";
    public static final String ITEM_SET_POS1 = "设置边界1";
    public static final String ITEM_SET_POS2 = "设置边界2";
    public static final String ITEM_SAVE_MAP = "保存地图";
    public static final String ITEM_ADD_TEAM_BASE = "添加队伍基地";
    public static final String ITEM_ADD_BASE_DROP = "添加基地资源点";
    public static final String ITEM_ADD_DIAMOND_DROP = "添加钻石资源点";
    public static final String ITEM_ADD_EMERALD_DROP = "添加绿宝石资源点";
    public static final String ITEM_ADD_ITEM_SHOP = "添加物品商店";
    public static final String ITEM_ADD_UPGRADE_SHOP = "添加团队升级商店";

    private final Map<String, String> itemCommands;
    private final Map<String, String> playerMapContext = new HashMap<>();

    public SetupItemManager() {
        this.itemCommands = new HashMap<>();
        registerItemCommands();
    }

    /**
     * 注册物品与命令的映射关系
     */
    private void registerItemCommands() {
        // 基础操作物品
        itemCommands.put(ITEM_SET_WAITING, "map setWaiting %s");
        itemCommands.put(ITEM_SET_RESPAWN, "map setRespawn %s");
        itemCommands.put(ITEM_SET_POS1, "map setPos1 %s");
        itemCommands.put(ITEM_SET_POS2, "map setPos2 %s");
        itemCommands.put(ITEM_SAVE_MAP, "map save %s");

        // 队伍出生点设置
        itemCommands.put(ITEM_ADD_TEAM_BASE, "map addBase %s");

        // 资源点设置
        itemCommands.put(ITEM_ADD_BASE_DROP, "map addDrop %s BASE");
        itemCommands.put(ITEM_ADD_DIAMOND_DROP, "map addDrop %s DIAMOND");
        itemCommands.put(ITEM_ADD_EMERALD_DROP, "map addDrop %s EMERALD");

        // 商店设置
        itemCommands.put(ITEM_ADD_ITEM_SHOP, "map addShop %s ITEM");
        itemCommands.put(ITEM_ADD_UPGRADE_SHOP, "map addShop %s UPGRADE");
    }

    /**
     * 给玩家装备地图设置物品
     *
     * @param player  玩家
     * @param mapName 地图名称
     */
    public void giveSetupItems(Player player, String mapName) {
        player.getInventory().clear();

        int slot = 0;
        // 基础设置物品
        player.getInventory().setItem(slot++, createItem(Material.COMPASS, "§e" + ITEM_SET_WAITING, "设置玩家等待游戏开始的位置"));
        player.getInventory().setItem(slot++, createItem(Material.BEACON, "§e" + ITEM_SET_RESPAWN, "设置中央重生点"));

        // 边界设置
        player.getInventory().setItem(slot++, createItem(XMaterial.GOLDEN_AXE.get(), "§c" + ITEM_SET_POS1, "设置地图边界的第一个点"));
        player.getInventory().setItem(slot++, createItem(Material.DIAMOND_AXE, "§c" + ITEM_SET_POS2, "设置地图边界的第二个点"));


        // 队伍基地设置 - 使用一个通用工具
        player.getInventory().setItem(slot++, createItem(XMaterial.RED_BED.get(), "§d" + ITEM_ADD_TEAM_BASE,
                "在你放置的羊毛颜色旁边添加队伍出生点，系统会自动识别羊毛颜色来确定队伍颜色"));

        // 资源点设置
        player.getInventory().setItem(slot++, createItem(Material.IRON_INGOT, "§7" + ITEM_ADD_BASE_DROP, "在当前位置添加基地资源生成点"));
        player.getInventory().setItem(slot++, createItem(Material.DIAMOND, "§b" + ITEM_ADD_DIAMOND_DROP, "在当前位置添加钻石资源生成点"));
        player.getInventory().setItem(slot++, createItem(Material.EMERALD, "§a" + ITEM_ADD_EMERALD_DROP, "在当前位置添加绿宝石资源生成点"));

        // 商店设置
        player.getInventory().setItem(slot++, createItem(Material.CHEST, "§6" + ITEM_ADD_ITEM_SHOP, "在当前位置添加物品商店"));
        player.getInventory().setItem(slot++, createItem(Material.ANVIL, "§d" + ITEM_ADD_UPGRADE_SHOP, "在当前位置添加团队升级商店"));

        // 保存地图
        player.getInventory().setItem(slot, createItem(Material.MAP, "§a" + ITEM_SAVE_MAP, "点击保存当前地图配置"));

        player.sendMessage(MessageUtil.color("&a已给予地图「" + mapName + "」的设置物品!"));
    }

    /**
     * 处理物品点击事件
     *
     * @param player   点击的玩家
     * @param itemName 物品名称（已经去除颜色代码）
     * @param mapName  当前操作的地图名
     * @return 是否为设置物品
     */
    public boolean handleItemClick(Player player, String itemName, String mapName) {
        String command = itemCommands.get(itemName);
        if (command != null) {
            String formattedCommand = String.format(command, mapName);
            if (player.performCommand(formattedCommand)) {
                player.sendMessage(MessageUtil.color("&a命令执行成功"));
            } else {
                player.sendMessage(MessageUtil.color("&c命令执行失败: " + formattedCommand));
            }
            return true;
        }
        return false;
    }

    /**
     * 创建一个带有名称和描述的物品
     *
     * @param material 物品材质
     * @param name     物品名称
     * @param lore     物品描述
     * @return 创建的物品栈
     */
    private ItemStack createItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            loreList.add(MessageUtil.color("&7" + lore));
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * 设置玩家当前操作的地图名
     *
     * @param playerName 玩家名
     * @param mapName    地图名
     */
    public void setPlayerMapContext(String playerName, String mapName) {
        playerMapContext.put(playerName, mapName);
    }

    /**
     * 获取玩家当前操作的地图名
     *
     * @param playerName 玩家名
     * @return 地图名
     */
    public String getPlayerMapContext(String playerName) {
        return playerMapContext.get(playerName);
    }

    /**
     * 移除玩家地图上下文
     *
     * @param playerName 玩家名
     */
    public void removePlayerMapContext(String playerName) {
        playerMapContext.remove(playerName);
    }
}
