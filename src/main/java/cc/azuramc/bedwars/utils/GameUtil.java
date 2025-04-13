package cc.azuramc.bedwars.utils;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.arena.MapData;
import cc.azuramc.bedwars.game.manager.GameManager;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.game.team.TeamColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;

public class GameUtil {

    public static void spawnAll(AzuraBedWars plugin) {
        GameManager gameManager = plugin.getGameManager();

        for (Location loc : gameManager.getMapData().getShopLocations(MapData.ShopType.ITEM)) {
            if (!loc.getChunk().isLoaded()) {
                loc.getChunk().load();
            }

            spawnVillager(plugin, loc);
        }

        for (Location loc : gameManager.getMapData().getShopLocations(MapData.ShopType.UPGRADE)) {
            if (!loc.getChunk().isLoaded()) {
                loc.getChunk().load();
            }

            spawnVillager2(plugin, loc);
        }

        for (Location loc : gameManager.getMapData().getDropLocations(MapData.DropType.DIAMOND)) {
            if (!loc.getChunk().isLoaded()) {
                loc.getChunk().load();
            }

            spawnArmorStand(gameManager, loc, MapData.DropType.DIAMOND, "qwq");
        }

        for (Location loc : gameManager.getMapData().getDropLocations(MapData.DropType.EMERALD)) {
            if (!loc.getChunk().isLoaded()) {
                loc.getChunk().load();
            }

            spawnArmorStand(gameManager, loc, MapData.DropType.EMERALD, "qaq");
        }
    }

    private static void spawnVillager(AzuraBedWars plugin, Location location) {
        Location loc = location.add(0.0D, -1.5D, 0.0D);
        ArmorStand ab = Objects.requireNonNull(location.getWorld()).spawn(loc, ArmorStand.class);
        Villager v = location.getWorld().spawn(location, Villager.class);
        v.setCustomNameVisible(false);
        if (v.getType() == EntityType.VILLAGER) {
            v.setProfession(Villager.Profession.LIBRARIAN);
        }

        v.setMetadata("Shop", new FixedMetadataValue(plugin, "Shop"));
        ab.setGravity(false);
        ab.setVisible(false);
        ab.addPassenger(v);
        spawnArmorStand2(location.add(0.0D, 1.3D, 0.0D), "§b§l物品商人");
    }

    private static void spawnVillager2(AzuraBedWars plugin, Location location) {
        Location loc = location.add(0.0D, -1.5D, 0.0D);
        ArmorStand ab = Objects.requireNonNull(location.getWorld()).spawn(loc, ArmorStand.class);
        Villager v = location.getWorld().spawn(location, Villager.class);
        v.setCustomNameVisible(false);
        if (v.getType() == EntityType.VILLAGER) {
            v.setProfession(Villager.Profession.FARMER);
//            v.setProfession(Villager.Profession.BLACKSMITH);
        }

        v.setMetadata("Shop2", new FixedMetadataValue(plugin, "Shop2"));
        spawnArmorStand2(location.add(0.0D, 1.3D, 0.0D), "§e§l团队升级");
        ab.setGravity(false);
        ab.setVisible(false);
        ab.addPassenger(v);
        // ab.setPassenger(v);
    }

    private static void spawnArmorStand2(Location location, String name) {
        ArmorStand a2 = Objects.requireNonNull(location.getWorld()).spawn(location.add(0.0D, 0.25D, 0.0D), ArmorStand.class);
        a2.setCustomName(name);
        a2.setCustomNameVisible(true);
        a2.setGravity(false);
        a2.setVisible(false);
        a2.setFallDistance(5.0F);
    }

    private static void spawnArmorStand(GameManager gameManager, Location location, MapData.DropType type, String name) {
        ArmorStand as;
        location.add(0.0D, 1.0D, 0.0D);

        if (type == MapData.DropType.EMERALD) {
            as = Objects.requireNonNull(location.getWorld()).spawn(location, ArmorStand.class);
            as.setGravity(false);
            as.setVisible(false);
            as.setFallDistance(7.0F);
            as.setHelmet(new ItemStack(Material.EMERALD_BLOCK));
            gameManager.getArmorSande().put(as, name);

            as = location.getWorld().spawn(location, ArmorStand.class);
            as.setCustomName(name);
            as.setCustomNameVisible(true);
            as.setGravity(false);
            as.setVisible(false);
            as.setFallDistance(6.0F);
            gameManager.getArmorSande().put(as, name);

            as = location.getWorld().spawn(location.add(0.0D, 0.25D, 0.0D), ArmorStand.class);
            as.setCustomName(name);
            as.setCustomNameVisible(true);
            as.setGravity(false);
            as.setVisible(false);
            as.setFallDistance(5.0F);
            gameManager.getArmorSande().put(as, name);

            as = location.getWorld().spawn(location.add(0.0D, 0.28D, 0.0D), ArmorStand.class);
            as.setCustomName(name);
            as.setCustomNameVisible(true);
            as.setGravity(false);
            as.setFallDistance(4.0F);
            as.setVisible(false);
            gameManager.getArmorSande().put(as, name);
        }

        if (type == MapData.DropType.DIAMOND) {
            as = Objects.requireNonNull(location.getWorld()).spawn(location, ArmorStand.class);
            as.setGravity(false);
            as.setVisible(false);
            as.setFallDistance(7.0F);
            as.setHelmet(new ItemStack(Material.DIAMOND_BLOCK));
            gameManager.getArmorStand().put(as, name);

            as = location.getWorld().spawn(location, ArmorStand.class);
            as.setCustomName(name);
            as.setCustomNameVisible(true);
            as.setGravity(false);
            as.setVisible(false);
            as.setFallDistance(6.0F);
            gameManager.getArmorStand().put(as, name);

            as = location.getWorld().spawn(location.add(0.0D, 0.25D, 0.0D), ArmorStand.class);
            as.setCustomName(name);
            as.setCustomNameVisible(true);
            as.setGravity(false);
            as.setVisible(false);
            as.setFallDistance(5.0F);
            gameManager.getArmorStand().put(as, name);

            as = location.getWorld().spawn(location.add(0.0D, 0.28D, 0.0D), ArmorStand.class);
            as.setCustomName(name);
            as.setCustomNameVisible(true);
            as.setGravity(false);
            as.setFallDistance(4.0F);
            as.setVisible(false);
            gameManager.getArmorStand().put(as, name);
        }

    }

    public static void setPlayerTeamTab() {
        GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

        for (TeamColor teamColor : TeamColor.values()) {
            GameTeam gameTeam = gameManager.getTeam(teamColor);
            if (gameTeam == null) {
                continue;
            }

//            gameTeam.getAlivePlayers().forEach(gamePlayer -> {
//                Player player = gamePlayer.getPlayer();
//                NametagEdit.getApi().clearNametag(player);
//                NametagEdit.getApi().setNametag(player, gameTeam.getName() + " ", gamePlayer.getName().equals(gamePlayer.getNickName()) ? "" : gamePlayer.getNickName());
//            });
        }
    }
}
