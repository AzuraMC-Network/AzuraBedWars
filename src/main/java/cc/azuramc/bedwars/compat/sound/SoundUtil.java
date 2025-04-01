package cc.azuramc.bedwars.compat.sound;

import cc.azuramc.bedwars.compat.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class SoundUtil {

    public static Sound get(String v18, String v113) {
        Sound finalSound = null;

        try {
            if (VersionUtil.isLessThan113()) {
                finalSound = Sound.valueOf(v18);
            } else {
                finalSound = Sound.valueOf(v113);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return finalSound;
    }
}
