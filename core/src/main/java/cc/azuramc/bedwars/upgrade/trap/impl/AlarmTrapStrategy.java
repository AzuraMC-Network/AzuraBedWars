package cc.azuramc.bedwars.upgrade.trap.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.upgrade.trap.AbstractTrapStrategy;
import cc.azuramc.bedwars.upgrade.trap.TrapManager;
import cc.azuramc.bedwars.upgrade.trap.TrapType;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;

import java.util.List;

/**
 * 警报陷阱策略实现
 *
 * @author an5w1r@163.com
 */
public class AlarmTrapStrategy extends AbstractTrapStrategy {

    @Override
    protected TrapType getTrapTypeEnum() {
        return TrapType.ALARM;
    }

    @Override
    public String getDisplayName() {
        return TrapType.ALARM.getDisplayName();
    }

    @Override
    public Material getIconMaterial() {
        return XMaterial.REDSTONE_TORCH.get();
    }

    @Override
    public List<String> getDescription() {
        return List.of(
                "§7显示隐身的玩家，",
                "§7及其名称与队伍名。"
        );
    }

    @Override
    public boolean canPurchase(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return !trapManager.isTrapActive(TrapType.ALARM) && !trapManager.isReachedActiveLimit();
    }

    @Override
    public int getPrice(GamePlayer gamePlayer) {
        GameTeam gameTeam = gamePlayer.getGameTeam();
        TrapManager trapManager = gameTeam.getTrapManager();
        return trapManager.getCurrentTrapPrice();
    }

    @Override
    protected void applyTrapEffect(GamePlayer triggerPlayer, GameTeam ownerTeam) {
        // 如果触发者隐身，则取消隐身
        if (triggerPlayer.isInvisible()) {
            triggerPlayer.endInvisibility();
        }
    }

    @Override
    protected void announceTrapTrigger(GameTeam gameTeam) {
        // 警报陷阱需要显示触发者信息
        AzuraBedWars.getInstance().mainThreadRunnable(() -> gameTeam.getAlivePlayers().forEach((player1 -> {
            player1.sendTitle("§c§l陷阱触发！", "&e触发者 " + gameTeam.getName() + " 队伍", 0, 40, 0);
            player1.playSound(XSound.ENTITY_ENDERMAN_TELEPORT.get(), 30F, 1F);
        })));
    }
}
