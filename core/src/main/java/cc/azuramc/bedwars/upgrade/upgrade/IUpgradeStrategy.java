package cc.azuramc.bedwars.upgrade.upgrade;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 升级策略接口
 * 定义统一的升级行为
 *
 * @author an5w1r@163.com
 */
public interface IUpgradeStrategy {

    /**
     * 获取升级名称
     *
     * @return 升级名称
     */
    String getUpgradeName();

    /**
     * 获取升级图标材质
     *
     * @return 图标材质
     */
    Material getIconMaterial();

    /**
     * 获取升级描述
     *
     * @return 升级描述列表
     */
    List<String> getDescription();

    /**
     * 检查是否可以升级
     *
     * @param gamePlayer 游戏玩家
     * @return 是否可以升级
     */
    boolean canUpgrade(GamePlayer gamePlayer);

    /**
     * 获取当前等级
     *
     * @param gamePlayer 游戏玩家
     * @return 当前等级
     */
    int getCurrentLevel(GamePlayer gamePlayer);

    /**
     * 获取最大等级
     *
     * @return 最大等级
     */
    int getMaxLevel();

    /**
     * 获取升级价格
     *
     * @param currentLevel 当前等级
     * @return 升级价格
     */
    int getUpgradePrice(int currentLevel);

    /**
     * 执行升级
     *
     * @param gamePlayer  游戏玩家
     * @param gameManager 游戏管理器
     * @return 是否升级成功
     */
    boolean performUpgrade(GamePlayer gamePlayer, GameManager gameManager);

    /**
     * 创建升级物品
     *
     * @param gamePlayer   游戏玩家
     * @param gameModeType 游戏模式
     * @return 升级物品
     */
    ItemStack createUpgradeItem(GamePlayer gamePlayer, GameModeType gameModeType);

    /**
     * 获取升级Lore
     *
     * @param gamePlayer   游戏玩家
     * @param gameModeType 游戏模式
     * @return Lore列表
     */
    List<String> getUpgradeLore(GamePlayer gamePlayer, GameModeType gameModeType);
}
