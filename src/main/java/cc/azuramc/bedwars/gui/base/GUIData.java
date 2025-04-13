package cc.azuramc.bedwars.gui.base;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;

@Getter
public class GUIData {

    @Getter
    private static final HashMap<Player, CustomGUI> currentGui = new HashMap<>();

    @Getter
    private static final HashMap<Player, CustomGUI> lastGui = new HashMap<>();

    @Getter
    private static final HashMap<Player, CustomGUI> lastReplaceGui = new HashMap<>();
}
