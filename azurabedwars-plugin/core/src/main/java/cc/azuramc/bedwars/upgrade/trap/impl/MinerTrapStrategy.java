package cc.azuramc.bedwars.upgrade.trap.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.upgrade.trap.AbstractTrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.TrapManager;
import cc.azuramc.bedwars.upgrade.trap.TrapType;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * 挖掘疲劳陷阱策略实现
 *
 * @author an5w1r@163.com
 */
public class MinerTrapStrategy extends AbstractTrapStrategy {

    @Override
    protected TrapType getTrapTypeEnum() {
        return TrapType.MINER;
    }

    @Override
    public String getDisplayName() {
        return TrapType.MINER.getDisplayName();
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.IRON_PICKAXE.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of("§7造成挖掘疲劳效果，持续8秒。");
    }

    @Override
    public int getPrice(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return trapManager.getCurrentTrapPrice();
    }

    @Override
    protected void applyTrapEffect(GamePlayer triggerPlayer, GameTeam ownerTeam) {
        // 给敌方玩家添加挖掘疲劳效果
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            PotionEffectType miningFatigue = XPotion.MINING_FATIGUE.get();
            if (miningFatigue != null) {
                triggerPlayer.getPlayer().addPotionEffect(new PotionEffect(miningFatigue,
                        teamUpgradeConfig.getMiningFatigueEffectDuration(),
                        teamUpgradeConfig.getMiningFatigueEffectAmplifier()));
            }
        });
    }
}
