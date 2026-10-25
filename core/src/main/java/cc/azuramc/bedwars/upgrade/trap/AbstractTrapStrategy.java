package cc.azuramc.bedwars.upgrade.trap;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.TeamUpgradeConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.shop.PriceCost;
import cc.azuramc.bedwars.shop.helper.ShopPaymentHandler;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XSound;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象陷阱策略基类
 * 提供公共的陷阱逻辑
 *
 * @author an5w1r@163.com
 */
public abstract class AbstractTrapStrategy implements TrapStrategy {

    public static final TeamUpgradeConfig teamUpgradeConfig = AzuraBedWars.getInstance().getTeamUpgradeConfig();

    /**
     * 获取陷阱类型枚举
     *
     * @return 陷阱类型枚举
     */
    protected abstract TrapType getTrapTypeEnum();

    /**
     * 执行购买
     *
     * @param gamePlayer  游戏玩家
     * @param gameManager 游戏管理器
     * @return 是否购买成功
     */
    @Override
    public boolean performPurchase(GamePlayer gamePlayer, GameManager gameManager) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return false;
        }
        TrapManager trapManager = gameTeam.getTrapManager();

        synchronized (trapManager) {
            // 检查陷阱是否可购买
            if (trapManager.isTrapActive(getTrapTypeEnum()) || trapManager.isReachedActiveLimit()) {
                return false;
            }

            // 先进行支付
            if (!ShopPaymentHandler.processPayment(gamePlayer, PriceCost.teamUpgrade(getPrice(gamePlayer)))) {
                return false;
            }

            // 支付成功后激活陷阱
            trapManager.activateTrap(getTrapTypeEnum());
        }

        // 通知团队所有打开TeamShopGUI的玩家刷新界面
        gameTeam.notifyTeamShopGUIRefresh();

        return true;
    }

    /**
     * 创建陷阱物品
     *
     * @param gamePlayer 游戏玩家
     * @return 陷阱物品
     */
    @Override
    public ItemStack createTrapItem(GamePlayer gamePlayer) {
        ItemBuilder builder = new ItemBuilder()
                .setType(getIconMaterial())
                .setDisplayName("§a" + getDisplayName());

        // 添加Lore
        builder.setLores(getTrapLore(gamePlayer));

        // 如果已激活，添加附魔效果
        TrapState state = getTrapState(gamePlayer);
        if (state == TrapState.ACTIVE || state == TrapState.FULLED) {
            builder.addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS);
        }

        // 添加隐藏属性标志
        builder.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);

        return builder.getItem();
    }

    /**
     * 获取陷阱Lore
     *
     * @param gamePlayer 游戏玩家
     * @return Lore列表
     */
    @Override
    public List<String> getTrapLore(GamePlayer gamePlayer) {
        List<String> lore = new ArrayList<>(getDescription());
        TrapState state = getTrapState(gamePlayer);
        int price = getPrice(gamePlayer);

        lore.add("");
        lore.add("§7价格: §b" + AzuraBedWars.getInstance().getGameManager().getEconomy().formatPrice(PriceCost.teamUpgrade(price)));
        lore.add("");

        switch (state) {
            case INACTIVE:
                lore.add("§e点击购买");
                break;
            case ACTIVE:
                lore.add("§a已激活");
                break;
            case FULLED:
                lore.add("§c陷阱已满");
                break;
        }

        return lore;
    }

    /**
     * 检查是否可以购买
     *
     * @param gamePlayer 游戏玩家
     * @return 是否可以购买
     */
    @Override
    public boolean canPurchase(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return false;
        }
        TrapManager trapManager = gameTeam.getTrapManager();
        return !trapManager.isTrapActive(getTrapTypeEnum()) && !trapManager.isReachedActiveLimit();
    }

    /**
     * 获取陷阱状态
     *
     * @param gamePlayer 游戏玩家
     * @return 陷阱状态
     */
    @Override
    public TrapState getTrapState(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null) {
            return TrapState.INACTIVE;
        }
        TrapManager trapManager = gameTeam.getTrapManager();

        if (!trapManager.isTrapActive(getTrapTypeEnum())) {
            return TrapState.INACTIVE;
        } else if (trapManager.isReachedActiveLimit()) {
            return TrapState.FULLED;
        } else {
            return TrapState.ACTIVE;
        }
    }

    @Override
    public void triggerTrap(GamePlayer triggerPlayer, GameTeam ownerTeam) {
        TrapManager trapManager = ownerTeam.getTrapManager();
        trapManager.deactivateTrap(getTrapTypeEnum());

        // 执行具体的陷阱效果
        if (!triggerPlayer.isHasTrapProtection()) {
            applyTrapEffect(triggerPlayer, ownerTeam);
        }

        // 通知团队成员陷阱被触发
        announceTrapTrigger(ownerTeam);

        // 通知团队所有打开TeamShopGUI的玩家刷新界面
        ownerTeam.notifyTeamShopGUIRefresh();
    }

    /**
     * 应用陷阱效果
     * 子类需要重写此方法来实现具体的陷阱效果
     *
     * @param triggerPlayer 触发陷阱的玩家
     * @param ownerTeam     拥有陷阱的团队
     */
    protected abstract void applyTrapEffect(GamePlayer triggerPlayer, GameTeam ownerTeam);

    /**
     * 通知团队成员陷阱被触发
     *
     * @param gameTeam 游戏团队
     */
    protected void announceTrapTrigger(GameTeam gameTeam) {
        AzuraBedWars.getInstance().mainThreadRunnable(() -> gameTeam.getAlivePlayers().forEach((player1 -> {
            player1.sendTitle("§c§l陷阱触发！", null, 0, 40, 0);
            if (XSound.ENTITY_ENDERMAN_TELEPORT.get() != null) {
                player1.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
            }
        })));
    }
}
