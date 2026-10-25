package cc.azuramc.bedwars.game.task.generator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author an5w1r@163.com
 */
@Getter
@RequiredArgsConstructor
public class ResourceHologram {
    @Nullable
    private final ArmorStand block;
    /** Top line first, in the order of the hologram config */
    private final List<ArmorStand> lines;
}
