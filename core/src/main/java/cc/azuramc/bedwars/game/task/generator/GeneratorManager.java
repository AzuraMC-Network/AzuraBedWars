package cc.azuramc.bedwars.game.task.generator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 资源刷新管理类，负责注册task等操作
 *
 * @author an5w1r@163.com
 */
public class GeneratorManager {

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private final GameManager gameManager;
    private final Map<String, PublicResourceGenerator> publicResourceGeneratorMap;
    private final Map<String, PrivateResourceGenerator> privateResourceGeneratorMap;

    public GeneratorManager(GameManager gameManager) {
        this.gameManager = gameManager;
        this.publicResourceGeneratorMap = new HashMap<>();
        this.privateResourceGeneratorMap = new HashMap<>();
    }

    public void initGeneratorTasks() {
        for (Map.Entry<String, GeneratorConfig.Resource> entry : config().getResources().entrySet()) {
            String id = entry.getKey();
            GeneratorConfig.Resource resource = entry.getValue();
            Material material = material(resource);
            if (material == null || resource.getLevels().isEmpty()) {
                continue;
            }
            GeneratorConfig.Level level = resource.level(1);
            if (resource.getAt() == GeneratorConfig.Spawn.BASE) {
                for (GameTeam gameTeam : gameManager.getGameTeams()) {
                    addPrivateResourceTask(new PrivateResourceGenerator(gameManager, privateName(id, gameTeam),
                            gameTeam.getResourceDropLocation(), material, level.getMaxStack()), ticks(level.getInterval()));
                }
            } else if (resource.getDrop() != null) {
                addPublicResourceTask(new PublicResourceGenerator(gameManager, id, material, resource.getDrop(),
                        level.getMaxStack()), ticks(level.getInterval()));
            }
        }
    }

    public static String privateName(String resource, GameTeam gameTeam) {
        return resource + ":" + gameTeam.getName();
    }

    @Nullable
    public static Material material(GeneratorConfig.Resource resource) {
        return XMaterial.matchXMaterial(resource.getMaterial()).map(XMaterial::get).orElse(null);
    }

    @Nullable
    public static GeneratorConfig.Resource resourceOf(Material material) {
        for (GeneratorConfig.Resource resource : config().getResources().values()) {
            if (material(resource) == material) {
                return resource;
            }
        }
        return null;
    }

    public static long ticks(double seconds) {
        return Math.max(1L, Math.round(seconds * 20));
    }

    private static GeneratorConfig config() {
        return AzuraBedWars.getInstance().getGeneratorConfig();
    }

    /**
     * 添加私有资源任务，指定执行间隔
     *
     * @param task     任务实例
     * @param interval 执行间隔（刻）
     */
    public void addPrivateResourceTask(PrivateResourceGenerator task, long interval) {
        task.setInterval(interval);
        PrivateResourceGenerator oldTask = privateResourceGeneratorMap.put(task.getTaskName(), task);
        if (oldTask != null && oldTask.getCurrentTask() != null) {
            oldTask.getCurrentTask().cancel();
        }
        task.startTask();
    }

    /**
     * 添加公开资源任务，指定执行间隔
     *
     * @param task     任务实例
     * @param interval 执行间隔（刻）
     */
    public void addPublicResourceTask(PublicResourceGenerator task, long interval) {
        task.setInterval(interval);
        PublicResourceGenerator oldTask = publicResourceGeneratorMap.put(task.getTaskName(), task);
        if (oldTask != null && oldTask.getCurrentTask() != null) {
            oldTask.getCurrentTask().cancel();
        }
        task.startTask();
    }

    @Nullable
    public PublicResourceGenerator getPublicResourceGenerator(String resource) {
        return publicResourceGeneratorMap.get(resource);
    }

    @Nullable
    public PrivateResourceGenerator getPrivateResourceGenerator(String resource, GameTeam gameTeam) {
        return privateResourceGeneratorMap.get(privateName(resource, gameTeam));
    }

    /**
     * @return false when the resource has no generator or no such level
     */
    public boolean setPublicLevel(String resource, int level) {
        PublicResourceGenerator generator = getPublicResourceGenerator(resource);
        GeneratorConfig.Resource config = config().resource(resource);
        if (generator == null || config == null || level < 1 || level > config.getLevels().size()) {
            return false;
        }
        GeneratorConfig.Level settings = config.level(level);
        generator.setInterval(ticks(settings.getInterval()));
        generator.setLevel(level);
        generator.setMaxStack(settings.getMaxStack());
        generator.restartTask();
        return true;
    }

    public void initDisplayUpdaters() {
        for (Map.Entry<String, List<ResourceHologram>> entry : gameManager.getResourceHolograms().entrySet()) {
            PublicResourceGenerator generator = getPublicResourceGenerator(entry.getKey());
            if (generator == null) {
                LoggerUtil.warn("No generator for the holograms of resource " + entry.getKey());
                continue;
            }
            List<ResourceHologram> holograms = new ArrayList<>(entry.getValue());
            Bukkit.getScheduler().runTaskTimer(AzuraBedWars.getInstance().getPlugin(), () -> updateDisplay(holograms, generator), 0L, 20L);
        }
    }

    public void immediateUpdateDisplay(String resource) {
        PublicResourceGenerator generator = getPublicResourceGenerator(resource);
        List<ResourceHologram> holograms = gameManager.getResourceHolograms().get(resource);
        if (generator != null && holograms != null) {
            updateDisplay(holograms, generator);
        }
    }

    private void updateDisplay(List<ResourceHologram> holograms, PublicResourceGenerator generator) {
        GeneratorConfig.Resource resource = config().resource(generator.getTaskName());
        if (resource == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        for (String line : resource.getHologram()) {
            lines.add(ChatColor.translateAlternateColorCodes('&', line
                    .replace("{level}", roman(generator.getLevel()))
                    .replace("{seconds}", String.valueOf(generator.getSecondsToNextDrop()))));
        }
        for (ResourceHologram hologram : holograms) {
            List<ArmorStand> stands = hologram.getLines();
            for (int i = 0; i < stands.size() && i < lines.size(); i++) {
                ArmorStand stand = stands.get(i);
                if (stand == null || !stand.isValid()) {
                    continue;
                }
                Location location = stand.getLocation();
                if (location.getWorld() != null && location.getChunk().isLoaded()) {
                    stand.setCustomName(lines.get(i));
                }
            }
        }
    }

    private static String roman(int level) {
        return level >= 1 && level <= ROMAN.length ? ROMAN[level - 1] : String.valueOf(level);
    }
}
