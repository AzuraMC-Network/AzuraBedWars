package cc.azuramc.bedwars.compat.sound;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class SoundUtil {
    private static final boolean b = loadVersion().startsWith("v1_8");

    private static String loadVersion() {
        String packName = Bukkit.getServer().getClass().getPackage().getName();
        return packName.substring(packName.lastIndexOf('.') + 1);
    }


    public static Sound get(String v18, String v19) {
        Sound finalSound = null;

        try {
            if (b) {
                finalSound = Sound.valueOf(v18);
            } else {
                finalSound = Sound.valueOf(v19);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return finalSound;
    }
}
