package cc.azuramc.bedwars.gui.base.event;

import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
@Getter
@RequiredArgsConstructor
@ToString
public class GUIActionEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final CustomGUI customGUI;

    private final GUIAction guiAction;

    private final InventoryClickEvent event;

    @Setter
    private boolean cancelled = false;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}