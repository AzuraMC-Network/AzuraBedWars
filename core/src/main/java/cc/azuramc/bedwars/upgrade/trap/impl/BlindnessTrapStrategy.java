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
 * 失明陷阱策略实现
 *
 * @author an5w1r@163.com
 */
public class BlindnessTrapStrategy extends AbstractTrapStrategy {

    @Override
    protected TrapType getTrapTypeEnum() {
        return TrapType.BLINDNESS;
    }

    @Override
    public String getDisplayName() {
        return TrapType.BLINDNESS.getDisplayName();
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.TRIPWIRE_HOOK.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of("§7造成失明与缓慢效果，持续8秒。");
    }

    @Override
    public boolean canPurchase(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return !trapManager.isTrapActive(TrapType.BLINDNESS) && !trapManager.isReachedActiveLimit();
    }

    @Override
    public int getPrice(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return trapManager.getCurrentTrapPrice();
    }

    @Override
    protected void applyTrapEffect(GamePlayer triggerPlayer, GameTeam ownerTeam) {
        // 给敌方玩家添加失明和缓慢效果
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            PotionEffectType blindness = XPotion.BLINDNESS.get();
            if (blindness != null) {
                triggerPlayer.getPlayer().addPotionEffect(new PotionEffect(blindness,
                        AzuraBedWars.getInstance().getEventConfig().getStartEvent().getUpgrade().getTrapEffectDuration(),
                        AzuraBedWars.getInstance().getEventConfig().getStartEvent().getUpgrade().getTrapEffectAmplifier()));
            }

            PotionEffectType slowness = XPotion.SLOWNESS.get();
            if (slowness != null) {
                triggerPlayer.getPlayer().addPotionEffect(new PotionEffect(slowness,
                        AzuraBedWars.getInstance().getEventConfig().getStartEvent().getUpgrade().getTrapEffectDuration(),
                        AzuraBedWars.getInstance().getEventConfig().getStartEvent().getUpgrade().getTrapEffectAmplifier()));
            }
        });
    }
}
