package cc.azuramc.bedwars.upgrade.trap;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 陷阱策略接口
 * 定义统一的陷阱行为
 *
 * @author an5w1r@163.com
 */
public interface ITrapStrategy {

    /**
     * 获取陷阱显示名称
     *
     * @return 陷阱显示名称
     */
    String getDisplayName();

    /**
     * 获取陷阱图标材质
     *
     * @return 图标材质
     */
    Material getIconMaterial();

    /**
     * 获取陷阱描述
     *
     * @return 陷阱描述列表
     */
    List<String> getDescription();

    /**
     * 检查是否可以购买
     *
     * @param gamePlayer 游戏玩家
     * @return 是否可以购买
     */
    boolean canPurchase(GamePlayer gamePlayer);

    /**
     * 获取陷阱价格
     *
     * @param gamePlayer 游戏玩家
     * @return 陷阱价格
     */
    int getPrice(GamePlayer gamePlayer);

    /**
     * 执行购买
     *
     * @param gamePlayer  游戏玩家
     * @param gameManager 游戏管理器
     * @return 是否购买成功
     */
    boolean performPurchase(GamePlayer gamePlayer, GameManager gameManager);

    /**
     * 创建陷阱物品
     *
     * @param gamePlayer   游戏玩家
     * @param gameModeType 游戏模式
     * @return 陷阱物品
     */
    ItemStack createTrapItem(GamePlayer gamePlayer, GameModeType gameModeType);

    /**
     * 获取陷阱Lore
     *
     * @param gamePlayer   游戏玩家
     * @param gameModeType 游戏模式
     * @return Lore列表
     */
    List<String> getTrapLore(GamePlayer gamePlayer, GameModeType gameModeType);

    /**
     * 获取陷阱状态
     *
     * @param gamePlayer 游戏玩家
     * @return 陷阱状态
     */
    TrapState getTrapState(GamePlayer gamePlayer);

    /**
     * 触发陷阱
     *
     * @param triggerPlayer 游戏玩家
     * @param ownerTeam     拥有陷阱的TEAM
     */
    void triggerTrap(GamePlayer triggerPlayer, GameTeam ownerTeam);
}
