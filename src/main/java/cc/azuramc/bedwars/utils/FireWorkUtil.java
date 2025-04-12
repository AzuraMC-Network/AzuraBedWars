package cc.azuramc.bedwars.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

public class FireWorkUtil {

    public static void spawnFireWork(Location loc, World w) {
        Firework fw = w.spawn(loc.clone().add(0, 0.6, 0), Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        FireworkEffect effect = getFireworkEffect(getRandomColor(), getRandomColor(), getRandomColor(), getRandomColor(), getRandomColor(), getRandomType());
        meta.addEffect(effect);
        meta.setPower(getRandomNum(4, 1));
        fw.setFireworkMeta(meta);
    }

    private static FireworkEffect getFireworkEffect(Color one, Color two, Color three, Color four, Color five, Type type) {
        return FireworkEffect.builder().flicker(false).withColor(one, two, three, four).withFade(five).with(type).trail(true).build();
    }

    private static int getRandomNum(int max, int min) {
        Random rand = new Random();
        return min + rand.nextInt(((max - (min)) + 1));
    }

    private static Type getRandomType() {
        int type = getRandomNum(5, 1);
        return switch (type) {
            case 1 -> Type.STAR;
            case 2 -> Type.CREEPER;
            case 3 -> Type.BURST;
            case 4 -> Type.BALL_LARGE;
            case 5 -> Type.BALL;
            default -> Type.STAR;
        };
    }

    private static Color getRandomColor() {
        int color = getRandomNum(17, 1);
        return switch (color) {
            case 1 -> Color.AQUA;
            case 2 -> Color.BLACK;
            case 3 -> Color.BLUE;
            case 4 -> Color.FUCHSIA;
            case 5 -> Color.GRAY;
            case 6 -> Color.GREEN;
            case 7 -> Color.LIME;
            case 8 -> Color.MAROON;
            case 9 -> Color.NAVY;
            case 10 -> Color.OLIVE;
            case 11 -> Color.ORANGE;
            case 12 -> Color.PURPLE;
            case 13 -> Color.RED;
            case 14 -> Color.SILVER;
            case 15 -> Color.TEAL;
            case 16 -> Color.WHITE;
            case 17 -> Color.YELLOW;
            default -> Color.RED;
        };
    }

}
