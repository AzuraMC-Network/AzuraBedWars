package cc.azuramc.bedwars.shop.helper;

import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import cc.azuramc.bedwars.shop.PriceCost;
import cc.azuramc.bedwars.shop.ShopItemType;
import com.cryptomorin.xseries.XMaterial;

/**
 * @author an5w1r@163.com
 */
public final class ToolDisplayHelper {

    private ToolDisplayHelper() {
    }

    /**
     * 更新镐子显示和价格
     *
     * @param gamePlayer   游戏玩家
     * @param itemBuilder  物品构建器
     * @param shopItemType 商店物品类型
     */
    public static void updatePickaxeDisplay(GamePlayer gamePlayer, ItemBuilder itemBuilder, ShopItemType shopItemType) {
        ToolType currentType = gamePlayer.getPickaxeType();
        switch (currentType) {
            case WOOD:
                itemBuilder.setType(XMaterial.STONE_PICKAXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.IRON_INGOT.get(), 20, 20));
                break;
            case STONE:
                itemBuilder.setType(XMaterial.IRON_PICKAXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.GOLD_INGOT.get(), 8, 24));
                break;
            case IRON:
            case DIAMOND:
                itemBuilder.setType(XMaterial.DIAMOND_PICKAXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.GOLD_INGOT.get(), 12, 36));
                break;
            default:
                itemBuilder.setType(XMaterial.WOODEN_PICKAXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.IRON_INGOT.get(), 10, 10));
                break;
        }
    }

    /**
     * 更新斧头显示和价格
     *
     * @param gamePlayer   游戏玩家
     * @param itemBuilder  物品构建器
     * @param shopItemType 商店物品类型
     */
    public static void updateAxeDisplay(GamePlayer gamePlayer, ItemBuilder itemBuilder, ShopItemType shopItemType) {
        ToolType currentType = gamePlayer.getAxeType();
        switch (currentType) {
            case WOOD:
                itemBuilder.setType(XMaterial.STONE_AXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.IRON_INGOT.get(), 20, 20));
                break;
            case STONE:
                itemBuilder.setType(XMaterial.IRON_AXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.GOLD_INGOT.get(), 8, 24));
                break;
            case IRON:
            case DIAMOND:
                itemBuilder.setType(XMaterial.DIAMOND_AXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.GOLD_INGOT.get(), 12, 36));
                break;
            default:
                itemBuilder.setType(XMaterial.WOODEN_AXE.get());
                shopItemType.setPriceCost(new PriceCost(XMaterial.IRON_INGOT.get(), 10, 10));
                break;
        }
    }
}
