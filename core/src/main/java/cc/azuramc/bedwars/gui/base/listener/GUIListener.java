package cc.azuramc.bedwars.gui.base.listener;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.GUIData;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.gui.base.action.GUIActionRunnable;
import cc.azuramc.bedwars.gui.base.action.NewGUIAction;
import cc.azuramc.bedwars.gui.base.component.GUIItem;
import cc.azuramc.bedwars.gui.base.event.GUIActionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author an5w1r@163.com
 */
public class GUIListener implements Listener {
    private final AzuraBedWars main;

    public GUIListener(AzuraBedWars main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    /**
     * 获取库存标题（不使用 InventoryView）
     */
    private String getTitle(CustomGUI gui) {
        if (gui == null) {
            return "";
        }
        return gui.getTitle();
    }

    /**
     * 获取列表的第一个元素
     */
    private <T> T getFirstItem(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (GUIData.getCURRENT_GUI().containsKey(player)) {
            CustomGUI customGUI = GUIData.getCURRENT_GUI().get(player);

            // 检查玩家当前打开的 GUI 是否是我们的 GUI
            if (customGUI.getPlayer().equals(player)) {
                event.setCancelled(true);

                // 如果是点击自己背包则返回
                if (event.getClickedInventory() instanceof PlayerInventory) {
                    return;
                }

                List<GUIItem> guiItems = customGUI.items.stream().filter(guiItem -> guiItem.getSize() == event.getSlot()).collect(Collectors.toList());
                if (guiItems.isEmpty()) {
                    return;
                }

                GUIItem firstItem = getFirstItem(guiItems);
                if (firstItem == null) {
                    return;
                }

                GUIAction guiAction = firstItem.getGuiAction();
                NewGUIAction newGUIAction = firstItem.getNewGUIAction();

                GUIActionEvent GUIActionEvent = new GUIActionEvent(customGUI, guiAction, event);
                main.callEvent(GUIActionEvent);

                if (GUIActionEvent.isCancelled()) {
                    return;
                }

                if ((guiAction == null ? newGUIAction.getDelay() : guiAction.getDelay()) > 0) {
                    Bukkit.getScheduler().runTaskLater(main, () -> {
                        if(guiAction == null){
                            newGUIAction.getRunnable().run(event);
                        }else {
                            if (guiAction.getRunnable() instanceof GUIActionRunnable guiActionRunnable) {
                                guiActionRunnable.setEvent(event);
                                guiActionRunnable.run();
                            } else {
                                guiAction.getRunnable().run();
                            }
                        }

                        if (guiAction == null ? newGUIAction.isClose() : guiAction.isClose()) {
                            player.closeInventory();
                        }
                    }, guiAction == null ? newGUIAction.getDelay() : guiAction.getDelay());
                    return;
                }

                if (guiAction == null) {
                    newGUIAction.getRunnable().run(event);
                } else {
                    guiAction.getRunnable().run();
                }

                if (guiAction == null ? newGUIAction.isClose() : guiAction.isClose()) {
                    player.closeInventory();
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (GUIData.getCURRENT_GUI().containsKey(player)) {
            CustomGUI customGUI = GUIData.getCURRENT_GUI().get(player);

            // 检查是否是同一个玩家
            if (customGUI.getPlayer().equals(player)) {
                CustomGUI lastGui = GUIData.getLAST_GUI().getOrDefault(player, null);
                GUIData.getLAST_GUI().remove(player);

                if (lastGui != null && lastGui.getPlayer().equals(player)) {
                    return;
                }

                GUIData.getCURRENT_GUI().remove(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        GUIData.getLAST_REPLACE_GUI().remove(event.getPlayer());
    }
}
