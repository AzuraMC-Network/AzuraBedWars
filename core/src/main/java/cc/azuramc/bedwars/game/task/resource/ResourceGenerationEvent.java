package cc.azuramc.bedwars.game.task.resource;

import lombok.Getter;
import org.bukkit.Location;

/**
 * @author An5w1r@163.com
 */
@Getter
public class ResourceGenerationEvent {
    private final ResourceType resourceType;
    private final Location location;
    private final int amount;
    private final int eventLevel;
    private final long timestamp;

    public ResourceGenerationEvent(ResourceType resourceType, Location location, int amount, int eventLevel) {
        this.resourceType = resourceType;
        this.location = location;
        this.amount = amount;
        this.eventLevel = eventLevel;
        this.timestamp = System.currentTimeMillis();
    }

}