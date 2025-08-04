package cc.azuramc.bedwars.upgrade.trap;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象陷阱策略基类
 * 提供公共的陷阱逻辑
 *
 * @author an5w1r@163.com
 */
public abstract class AbstractTrapStrategy implements TrapStrategy {

    /**
     * 资源类型名称缓存
     */
    private static final Map<Material, String> RESOURCE_NAMES = new HashMap<>();

    static {
        // 初始化资源名称
        RESOURCE_NAMES.put(XMaterial.DIAMOND.get(), "钻石");
    }

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
        TrapManager trapManager = gameTeam.getTrapManager();
        GameModeType gameModeType = gamePlayer.getPlayerData().getMode();
        int price = getPrice(gamePlayer);

        synchronized (trapManager) {
            // 检查是否有足够资源
            if (!canAfford(gamePlayer, price, gameModeType)) {
                Player player = gamePlayer.getPlayer();
                player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
                player.sendMessage("§c没有足够资源购买！");
                return false;
            }

            // 检查陷阱是否可购买
            if (trapManager.isTrapActive(getTrapTypeEnum()) || trapManager.isReachedActiveLimit()) {
                return false;
            }

            // 先进行支付
            if (!processPayment(gamePlayer, price, gameModeType)) {
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
     * @param gamePlayer   游戏玩家
     * @param gameModeType 游戏模式
     * @return 陷阱物品
     */
    @Override
    public ItemStack createTrapItem(GamePlayer gamePlayer, GameModeType gameModeType) {
        ItemBuilder builder = new ItemBuilder()
                .setType(getIconMaterial())
                .setDisplayName("§a" + getDisplayName());

        // 添加Lore
        builder.setLores(getTrapLore(gamePlayer, gameModeType));

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
     * @param gamePlayer   游戏玩家
     * @param gameModeType 游戏模式
     * @return Lore列表
     */
    @Override
    public List<String> getTrapLore(GamePlayer gamePlayer, GameModeType gameModeType) {
        List<String> lore = new ArrayList<>(getDescription());
        TrapState state = getTrapState(gamePlayer);
        int price = getPrice(gamePlayer);

        lore.add("");
        lore.add("§7价格: §b" + formatPrice(price, gameModeType));
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
        TrapManager trapManager = gameTeam.getTrapManager();

        if (!trapManager.isTrapActive(getTrapTypeEnum())) {
            return TrapState.INACTIVE;
        } else if (trapManager.isReachedActiveLimit()) {
            return TrapState.FULLED;
        } else {
            return TrapState.ACTIVE;
        }
    }

    /**
     * 检查是否有足够资源支付
     *
     * @param gamePlayer   游戏玩家
     * @param price        价格
     * @param gameModeType 游戏模式
     * @return 是否有足够资源
     */
    protected boolean canAfford(GamePlayer gamePlayer, int price, GameModeType gameModeType) {
        if (gameModeType == GameModeType.DEFAULT) {
            return canAffordItem(gamePlayer, XMaterial.DIAMOND.get(), price);
        } else {
            return canAffordExperience(gamePlayer, price * 100);
        }
    }

    /**
     * 处理支付（实际扣除资源）
     *
     * @param gamePlayer   游戏玩家
     * @param price        价格
     * @param gameModeType 游戏模式
     * @return 是否支付成功
     */
    protected boolean processPayment(GamePlayer gamePlayer, int price, GameModeType gameModeType) {
        if (gameModeType == GameModeType.DEFAULT) {
            return spendItem(gamePlayer, XMaterial.DIAMOND.get(), price);
        } else {
            return spendExperience(gamePlayer, price * 100);
        }
    }

    /**
     * 检查是否有足够物品
     *
     * @param gamePlayer 玩家
     * @param material   物品类型
     * @param amount     数量
     * @return 是否有足够物品
     */
    private boolean canAffordItem(GamePlayer gamePlayer, Material material, int amount) {
        Player player = gamePlayer.getPlayer();

        // 计算玩家拥有的资源总数
        int playerTotal = 0;
        ItemStack[] inventory = player.getInventory().getContents();

        for (ItemStack item : inventory) {
            if (item != null && item.getType().equals(material)) {
                playerTotal += item.getAmount();
            }
        }

        return playerTotal >= amount;
    }

    /**
     * 扣除物品
     *
     * @param gamePlayer 玩家
     * @param material   物品类型
     * @param amount     数量
     * @return 是否扣除成功
     */
    private boolean spendItem(GamePlayer gamePlayer, Material material, int amount) {
        Player player = gamePlayer.getPlayer();
        ItemStack[] inventory = player.getInventory().getContents();

        // 扣除资源
        int remainingToDeduct = amount;
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType().equals(material) && remainingToDeduct > 0) {
                if (item.getAmount() > remainingToDeduct) {
                    item.setAmount(item.getAmount() - remainingToDeduct);
                    remainingToDeduct = 0;
                } else {
                    remainingToDeduct -= item.getAmount();
                    item.setAmount(0);
                }
                player.getInventory().setItem(i, item);
            }
        }

        player.playSound(player.getLocation(), XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
        return true;
    }

    /**
     * 检查是否有足够经验
     *
     * @param gamePlayer 游戏玩家
     * @param xpLevel    经验等级
     * @return 是否有足够经验
     */
    private boolean canAffordExperience(GamePlayer gamePlayer, int xpLevel) {
        Player player = gamePlayer.getPlayer();
        return player.getLevel() >= xpLevel;
    }

    /**
     * 扣除经验
     *
     * @param gamePlayer 游戏玩家
     * @param xpLevel    经验等级
     * @return 是否扣除成功
     */
    private boolean spendExperience(GamePlayer gamePlayer, int xpLevel) {
        Player player = gamePlayer.getPlayer();

        player.setLevel(player.getLevel() - xpLevel);
        player.playSound(player.getLocation(), XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
        return true;
    }

    /**
     * 格式化价格显示
     *
     * @param price        价格
     * @param gameModeType 游戏模式
     * @return 格式化后的价格字符串
     */
    protected String formatPrice(int price, GameModeType gameModeType) {
        if (gameModeType == GameModeType.DEFAULT) {
            return price + " " + RESOURCE_NAMES.get(XMaterial.DIAMOND.get());
        } else {
            return (price * 100) + "级";
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
            player1.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
        })));
    }
}
