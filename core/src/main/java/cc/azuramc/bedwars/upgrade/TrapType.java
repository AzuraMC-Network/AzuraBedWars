package cc.azuramc.bedwars.upgrade;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Getter
public enum TrapType {
    BLINDNESS("致盲陷阱", XMaterial.TRIPWIRE_HOOK, 14, "§7造成失明与缓慢效果，持续8秒。"),
    FIGHT_BACK("反击陷阱", XMaterial.FEATHER, 15, "§7赋予基地附近的队友速度 II 与跳跃提升 II", "§7效果，持续15秒。"),
    ALARM("警报陷阱", XMaterial.REDSTONE_TORCH, 16, "§7显示隐身的玩家，", "§7及其名称与队伍名。"),
    MINER("挖掘疲劳陷阱", XMaterial.IRON_PICKAXE, 23, "§7造成挖掘疲劳效果，持续8秒。");

    private final String displayName;
    private final XMaterial icon;
    private final int slot;
    private final List<String> description;

    TrapType(String displayName, XMaterial icon, int slot, String... description) {
        this.displayName = displayName;
        this.icon = icon;
        this.slot = slot;
        this.description = Arrays.asList(description);
    }
}
