package cc.azuramc.bedwars.gui.base.action;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GUIAction {
    private int delay;
    private Runnable runnable;
    private boolean close;
}
