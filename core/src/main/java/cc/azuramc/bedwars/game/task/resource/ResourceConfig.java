package cc.azuramc.bedwars.game.task.resource;

import lombok.Getter;
import lombok.Setter;

/**
 * @author An5w1r@163.com
 */
public class ResourceConfig {
    @Setter @Getter private int spawnInterval;
    private final int[] maxStackByLevel; // [level1, level2, level3]
    @Setter @Getter private boolean enabled;

    public ResourceConfig(int spawnInterval, int level1Stack, int level2Stack, int level3Stack) {
        this.spawnInterval = spawnInterval;
        this.maxStackByLevel = new int[]{level1Stack, level2Stack, level3Stack};
        this.enabled = true;
    }

    public int getMaxStack(int level) {
        int index = Math.max(0, Math.min(2, level - 1));
        return maxStackByLevel[index];
    }

    public void setMaxStack(int level, int maxStack) {
        int index = Math.max(0, Math.min(2, level - 1));
        maxStackByLevel[index] = maxStack;
    }

    public ResourceConfig copy() {
        ResourceConfig copy = new ResourceConfig(spawnInterval, maxStackByLevel[0], maxStackByLevel[1], maxStackByLevel[2]);
        copy.enabled = this.enabled;
        return copy;
    }
}