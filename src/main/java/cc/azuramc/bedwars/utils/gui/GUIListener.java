package cc.azuramc.bedwars.utils.gui;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.utils.gui.event.GUIActionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class GUIListener implements Listener {
    private final AzuraBedWars main;
    private static final boolean NEW_VERSION;
    
    static {
        boolean newVersion = false;
        try {
            // 1.13+版本存在Material.PLAYER_HEAD
            Class.forName("org.bukkit.Material").getField("PLAYER_HEAD");
            newVersion = true;
        } catch (Exception e) {
            // 1.8-1.12版本
            newVersion = false;
        }
        NEW_VERSION = newVersion;
    }

    public GUIListener(AzuraBedWars main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
    }
    
    /**
     * 获取库存标题（兼容旧版和新版Bukkit API）
     */
    private String getInventoryTitle(InventoryClickEvent event) {
        try {
            if (NEW_VERSION) {
                // 新版本使用getTitle()方法
                return event.getView().getTitle();
            } else {
                // 旧版本尝试使用反射获取Title
                Method getTitleMethod = event.getView().getTopInventory().getClass().getMethod("getTitle");
                return (String) getTitleMethod.invoke(event.getView().getTopInventory());
            }
        } catch (Exception e) {
            return ""; // 如果无法获取标题，返回空字符串
        }
    }
    
    /**
     * 获取库存标题（兼容旧版和新版Bukkit API）
     */
    private String getInventoryTitle(InventoryCloseEvent event) {
        try {
            if (NEW_VERSION) {
                // 新版本使用getTitle()方法
                return event.getView().getTitle();
            } else {
                // 旧版本尝试使用反射获取Title
                Method getTitleMethod = event.getView().getTopInventory().getClass().getMethod("getTitle");
                return (String) getTitleMethod.invoke(event.getView().getTopInventory());
            }
        } catch (Exception e) {
            return ""; // 如果无法获取标题，返回空字符串
        }
    }
    
    /**
     * 获取列表的第一个元素（兼容不同Java版本）
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

        if (GUIData.getCurrentGui().containsKey(player)) {
            CustomGUI customGUI = GUIData.getCurrentGui().get(player);

            if (getInventoryTitle(event).equals(customGUI.getTitle())) {
                event.setCancelled(true);

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
                            if (guiAction.getRunnable() instanceof GUIActionRunnable) {
                                GUIActionRunnable guiActionRunnable = (GUIActionRunnable) guiAction.getRunnable();
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

                if(guiAction == null){
                    newGUIAction.getRunnable().run(event);
                }else {
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

        if (GUIData.getCurrentGui().containsKey(player)) {
            String title = getInventoryTitle(event);
            CustomGUI customGUI = GUIData.getCurrentGui().get(player);

            if (title.equals(customGUI.getTitle())) {
                CustomGUI lastGui = GUIData.getLastGui().getOrDefault(player, null);
                GUIData.getLastGui().remove(player);

                if (lastGui != null && title.equals(lastGui.getTitle())) {
                    return;
                }

                GUIData.getCurrentGui().remove(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        GUIData.getLastReplaceGui().remove(event.getPlayer());
    }
}
