package cc.azuramc.bedwars.shop.helper;

import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.shop.ShopItemType;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author an5w1r@163.com
 */
public final class ShopPaymentHandler {

    private ShopPaymentHandler() {
    }

    /**
     * 处理支付
     *
     * @param gamePlayer   游戏玩家
     * @param shopItemType 商店物品类型
     * @param gameModeType 游戏模式类型
     * @return 支付是否成功
     */
    public static boolean processPayment(GamePlayer gamePlayer, ShopItemType shopItemType, GameModeType gameModeType) {
        if (gameModeType == GameModeType.DEFAULT) {
            return processItemPayment(gamePlayer, shopItemType);
        } else {
            return processExperiencePayment(gamePlayer, shopItemType);
        }
    }

    /**
     * 处理物品支付
     */
    public static boolean processItemPayment(GamePlayer gamePlayer, ShopItemType shopItemType) {
        Material paymentMaterial = shopItemType.getPriceCost().material();
        int requiredAmount = shopItemType.getPriceCost().amount();

        // 计算玩家拥有的资源总数
        int playerTotal = 0;
        ItemStack[] inventory = gamePlayer.getPlayer().getInventory().getContents();

        for (ItemStack item : inventory) {
            if (item != null && item.getType().equals(paymentMaterial)) {
                playerTotal += item.getAmount();
            }
        }

        // 检查是否有足够资源
        if (playerTotal < requiredAmount) {
            playFailSound(gamePlayer);
            gamePlayer.sendMessage("§c没有足够资源购买！");
            return false;
        }

        // 扣除资源
        int remainingToDeduct = requiredAmount;
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && item.getType().equals(paymentMaterial) && remainingToDeduct > 0) {
                if (item.getAmount() > remainingToDeduct) {
                    item.setAmount(item.getAmount() - remainingToDeduct);
                    remainingToDeduct = 0;
                } else {
                    remainingToDeduct -= item.getAmount();
                    item.setAmount(0);
                }
                gamePlayer.getPlayer().getInventory().setItem(i, item);
            }
        }

        playSuccessSound(gamePlayer);
        return true;
    }

    /**
     * 处理经验支付（当前简化版本）
     * TODO: [经验模式扩展设计] 原计划支持更复杂的经验支付机制，但是因为有些BUG暂时弃用
     *
     * @see #processAdvancedExperiencePayment(GamePlayer, ShopItemType) 未来可实现的高级版本
     */
    public static boolean processExperiencePayment(GamePlayer gamePlayer, ShopItemType shopItemType) {
        int requiredXp = shopItemType.getPriceCost().xp();

        if (gamePlayer.getPlayer().getLevel() < requiredXp) {
            playFailSound(gamePlayer);
            gamePlayer.sendMessage("§c没有足够资源购买！");
            return false;
        }

        gamePlayer.getPlayer().setLevel(gamePlayer.getPlayer().getLevel() - requiredXp);
        playSuccessSound(gamePlayer);
        return true;
    }

    /**
     * 高级经验支付处理，暂时弃用
     *
     * @param gamePlayer   游戏玩家
     * @param shopItemType 商店物品类型
     * @return 支付是否成功
     */
    @SuppressWarnings("unused")
    private static boolean processAdvancedExperiencePayment(GamePlayer gamePlayer, ShopItemType shopItemType) {
        // 资源类型优先级顺序（按价值递增）
        final String[] RESOURCE_PRIORITY = {"IRON", "GOLD", "DIAMOND", "EMERALD"};

        int requiredXp = shopItemType.getPriceCost().xp();
        String requiredResourceType = shopItemType.getPriceCost().material().toString().toUpperCase();

        // 检查玩家是否有足够的经验等级
        if (gamePlayer.getPlayer().getLevel() < requiredXp) {
            playFailSound(gamePlayer);
            gamePlayer.sendMessage("§c没有足够资源购买！");
            return false;
        }

        // 查找当前资源类型在列表中的位置
        int resourceIndex = -1;
        for (int i = 0; i < RESOURCE_PRIORITY.length; i++) {
            if (RESOURCE_PRIORITY[i].equals(requiredResourceType)) {
                resourceIndex = i;
                break;
            }
        }

        // 如果不是标准资源类型，直接从经验中扣除
        if (resourceIndex == -1) {
            gamePlayer.getPlayer().setLevel(gamePlayer.getPlayer().getLevel() - requiredXp);
            playSuccessSound(gamePlayer);
            return true;
        }

        // 首先尝试从指定资源类型中扣除
        int remainingXp = requiredXp;
        int available = gamePlayer.getExperience(requiredResourceType);

        if (available > 0) {
            int toDeduct = Math.min(available, remainingXp);
            gamePlayer.spendResourceExperience(requiredResourceType, toDeduct);
            remainingXp -= toDeduct;
        }

        // 如果仍需扣除，根据资源类型选择向上递增或向下递减
        if (remainingXp > 0) {
            String[] fallbackResources;
            if (requiredResourceType.equals("EMERALD")) {
                // EMERALD不足时向下递减
                fallbackResources = new String[]{"DIAMOND", "GOLD", "IRON"};
            } else {
                // 其他资源不足时向上递增
                fallbackResources = switch (requiredResourceType) {
                    case "IRON" -> new String[]{"GOLD", "DIAMOND", "EMERALD"};
                    case "GOLD" -> new String[]{"DIAMOND", "EMERALD"};
                    case "DIAMOND" -> new String[]{"EMERALD"};
                    default -> new String[0];
                };
            }

            remainingXp = deductFromResources(gamePlayer, remainingXp, fallbackResources);
        }

        // 如果所有资源尝试后仍需扣除，从玩家经验等级中扣除
        if (remainingXp > 0) {
            gamePlayer.getPlayer().setLevel(gamePlayer.getPlayer().getLevel() - remainingXp);
        }

        playSuccessSound(gamePlayer);
        return true;
    }

    /**
     * 从指定资源列表中扣除经验
     */
    private static int deductFromResources(GamePlayer gamePlayer, int remainingXp, String[] resources) {
        for (String resource : resources) {
            if (remainingXp <= 0) {
                break;
            }

            int available = gamePlayer.getExperience(resource);
            if (available > 0) {
                int toDeduct = Math.min(available, remainingXp);
                gamePlayer.spendResourceExperience(resource, toDeduct);
                remainingXp -= toDeduct;
            }
        }
        return remainingXp;
    }

    private static void playFailSound(GamePlayer gamePlayer) {
        if (XSound.ENTITY_ENDERMAN_TELEPORT.get() != null) {
            gamePlayer.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
        }
    }

    private static void playSuccessSound(GamePlayer gamePlayer) {
        if (XSound.ENTITY_ITEM_PICKUP.get() != null) {
            gamePlayer.playSound(XSound.ENTITY_ITEM_PICKUP.get(), 1F, 1F);
        }
    }
}
