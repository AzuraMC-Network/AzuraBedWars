package cc.azuramc.bedwars.gui.base;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * @author an5w1r@163.com
 */
@Getter
public class GUIData {

    @Getter
    private static final HashMap<Player, CustomGUI> CURRENT_GUI = new HashMap<>();

    @Getter
    private static final HashMap<Player, CustomGUI> LAST_GUI = new HashMap<>();

    @Getter
    private static final HashMap<Player, CustomGUI> LAST_REPLACE_GUI = new HashMap<>();
}
