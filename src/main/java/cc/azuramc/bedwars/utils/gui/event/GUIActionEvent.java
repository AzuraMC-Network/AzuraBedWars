package cc.azuramc.bedwars.utils.gui.event;

import cc.azuramc.bedwars.utils.gui.CustomGUI;
import cc.azuramc.bedwars.utils.gui.GUIAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

@Getter
@RequiredArgsConstructor
@ToString
public class GUIActionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final CustomGUI customGUI;

    private final GUIAction guiAction;

    private final InventoryClickEvent event;

    @Setter
    private boolean cancelled = false;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}