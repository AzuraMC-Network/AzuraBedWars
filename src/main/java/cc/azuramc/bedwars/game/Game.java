package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.map.MapData;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class Game {
    private AzuraBedWars main;
    private MapData mapData;
    private boolean forceStart;

    private Location waitingLocation;
    private Location respawnLocation;

    private List<Location> blocks;
    private List<GameTeam> gameTeams;

    private HashMap<ArmorStand, String> armorSande;
    private HashMap<ArmorStand, String> armorStand;

    public Game(AzuraBedWars main, Location waitingLocation) {
        this.main = main;
        this.forceStart = false;
        this.waitingLocation = waitingLocation;
        this.gameTeams = new ArrayList<>();

        this.armorSande = new HashMap<>();
        this.armorStand = new HashMap<>();

    }

}
