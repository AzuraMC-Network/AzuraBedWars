package cc.azuramc.bedwars.game.item.special;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.listener.special.RescuePlatformListener;
import cc.azuramc.bedwars.listener.special.WarpPowderListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSpecialItem {
    @Getter
    private static final List<Class<? extends AbstractSpecialItem>> availableSpecials = new ArrayList<>();

    public static void loadSpecials() {
        AbstractSpecialItem.availableSpecials.add(RescuePlatform.class);
        AbstractSpecialItem.availableSpecials.add(WarpPowder.class);
        Bukkit.getPluginManager().registerEvents(new RescuePlatformListener(), AzuraBedWars.getInstance());
        Bukkit.getPluginManager().registerEvents(new WarpPowderListener(), AzuraBedWars.getInstance());
    }

    public abstract Material getActivatedMaterial();

    public abstract Material getItemMaterial();

}
