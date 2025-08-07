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
 * 反击陷阱策略实现
 *
 * @author an5w1r@163.com
 */
public class FightBackTrapStrategy extends AbstractTrapStrategy {

    @Override
    protected TrapType getTrapTypeEnum() {
        return TrapType.FIGHT_BACK;
    }

    @Override
    public String getDisplayName() {
        return TrapType.FIGHT_BACK.getDisplayName();
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.FEATHER.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of(
                "§7赋予基地附近的队友速度 II 与跳跃提升 II",
                "§7效果，持续15秒。"
        );
    }

    @Override
    public int getPrice(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return trapManager.getCurrentTrapPrice();
    }

    @Override
    protected void applyTrapEffect(GamePlayer triggerPlayer, GameTeam ownerTeam) {
        // 给己方玩家添加速度和跳跃提升效果
        AzuraBedWars.getInstance().mainThreadRunnable(() -> {
            PotionEffectType speed = XPotion.SPEED.get();
            PotionEffectType jumpBoost = XPotion.JUMP_BOOST.get();
            ownerTeam.getAlivePlayers().forEach(player -> {
                if (speed != null) {
                    player.getPlayer().addPotionEffect(new PotionEffect(speed, 15 * 20, 1));
                }
                if (jumpBoost != null) {
                    player.getPlayer().addPotionEffect(new PotionEffect(jumpBoost, 15 * 20, 1));
                }
            });
        });
    }
}
