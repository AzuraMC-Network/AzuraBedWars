package cc.azuramc.bedwars.game.timer;

import cc.azuramc.bedwars.compat.util.ItemBuilderUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.map.data.MapData;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.event.Runnable;
import cc.azuramc.bedwars.utils.ArmorStandUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GeneratorRunnable {
    private final Game game;
    private boolean timer;

    public GeneratorRunnable(Game game) {
        this.game = game;
    }

    public void start() {
        if (!this.timer) {
            timer = true;

            Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance(), () -> {
                List<ArmorStand> allArmor = new ArrayList<>();
                allArmor.addAll(game.getArmorSande().keySet());
                allArmor.addAll(game.getArmorStand().keySet());
                for (ArmorStand as : allArmor) {
                    Location loc = as.getLocation();
                    if (!loc.getChunk().isLoaded()) {
                        loc.getChunk().load();
                    }
                    ArmorStandUtil.moveArmorStand(as, as.getLocation().getY());
                }
            }, 0L, 1L);

            //IRON
            game.getEventManager().registerRunnable("铁刷新", (seconds, currentEvent) -> Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(),
                    () -> game.getMapData().getDropLocations(MapData.DropType.BASE)
                            .forEach((location -> location.getWorld().dropItem(location, new ItemBuilderUtil().setType(Material.IRON_INGOT)
                                    .setDisplayName("§a§a§a§a§a§a").getItem()).setVelocity(new Vector(0.0D, 0.1D, 0.0D))))), 2);

            //GOLD
            game.getEventManager().registerRunnable("金刷新", (seconds, currentEvent) -> Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(),
                    () -> game.getMapData().getDropLocations(MapData.DropType.BASE)
                            .forEach((location -> location.getWorld().dropItem(location, new ItemBuilderUtil().setType(Material.GOLD_INGOT).setDisplayName("§a§a§a§a§a§a")
                                    .getItem()).setVelocity(new Vector(0.0D, 0.1D, 0.0D))))), 6);

            //D
            game.getEventManager().registerRunnable("钻石时间显示", (seconds, currentEvent) -> Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> {
                for (ArmorStand armorStand : game.getArmorStand().keySet()) {
                    if (!armorStand.getLocation().getChunk().isLoaded()) armorStand.getLocation().getChunk().load();
                    int s = 0;
                    Runnable runnable = game.getEventManager().getRunnables().getOrDefault("钻石刷新", null);
                    if (runnable != null) {
                        s = runnable.getSeconds() - runnable.getNextSeconds();
                    }
                    if (armorStand.getFallDistance() == 6.0F) armorStand.setCustomName("§e将在§c" + s + "§e秒后刷新");
                    if (armorStand.getFallDistance() == 5.0F) armorStand.setCustomName("§b钻石");

                    if (armorStand.getFallDistance() == 4.0F) {
                        if (currentEvent <= 1) armorStand.setCustomName("§e等级 §cI");
                        else if (currentEvent == 2) armorStand.setCustomName("§e等级 §cII");
                        else armorStand.setCustomName("§e等级 §cIII");
                    }
                }
            }));

            game.getEventManager().registerRunnable("钻石刷新", (seconds, currentEvent) -> Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(),
                    () -> game.getMapData().getDropLocations(MapData.DropType.DIAMOND)
                            .forEach(location -> location.getWorld().dropItem(location, new ItemBuilderUtil().setType(Material.DIAMOND)
                                    .setDisplayName("§a§a§a§a§a§a").getItem()).setVelocity(new Vector(0.0D, 0.1D, 0.0D)))), 30);

            //E
            game.getEventManager().registerRunnable("绿宝石时间显示", (seconds, currentEvent) -> Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(), () -> {
                for (ArmorStand armorStand : game.getArmorSande().keySet()) {
                    if (!armorStand.getLocation().getChunk().isLoaded()) armorStand.getLocation().getChunk().load();
                    int s = 0;
                    Runnable runnable = game.getEventManager().getRunnables().getOrDefault("绿宝石刷新", null);
                    if (runnable != null) {
                        s = runnable.getSeconds() - runnable.getNextSeconds();
                    }
                    if (armorStand.getFallDistance() == 6.0F) armorStand.setCustomName("§e将在§c" + s + "§e秒后刷新");
                    if (armorStand.getFallDistance() == 5.0F) armorStand.setCustomName("§2绿宝石");

                    if (armorStand.getFallDistance() == 4.0F) {
                        if (currentEvent <= 1) armorStand.setCustomName("§e等级 §cI");
                        else if (currentEvent == 2) armorStand.setCustomName("§e等级 §cII");
                        else armorStand.setCustomName("§e等级 §cIII");
                    }
                }
            }));

            game.getEventManager().registerRunnable("绿宝石刷新", (seconds, currentEvent) -> Bukkit.getScheduler().runTask(AzuraBedWars.getInstance(),
                    () -> game.getMapData().getDropLocations(MapData.DropType.EMERALD)
                            .forEach(location -> location.getWorld().dropItem(location, new ItemBuilderUtil().setType(Material.EMERALD)
                                    .setDisplayName("§a§a§a§a§a§a").getItem()).setVelocity(new Vector(0.0D, 0.1D, 0.0D)))), 55);
        }
    }
}
