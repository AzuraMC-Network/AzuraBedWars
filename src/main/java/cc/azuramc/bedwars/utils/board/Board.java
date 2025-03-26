package cc.azuramc.bedwars.utils.board;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class Board {
    private final Player owner;
    private final String title;
    private final List<String> lines;
    private final Sideline sideline;

    public Board(Player owner, String title, List<String> lines) {
        this.owner = owner;
        this.title = title;
        this.lines = Arrays.asList("",
                "LiteBoard Support",
                "",
                "BY RE",
                "QQ 1609403959");
        this.sideline = new Sideline(new Sidebar(owner));
        this.sideline.getSidebar().setName(color(title));
        this.lines.stream().map(Board::color).forEach(this.sideline::add);
        this.sideline.flush();
    }

    /*
    彩色化字符串
     */
    public static String color(String source) {
        return ChatColor.translateAlternateColorCodes('&', source);
    }

    public void send(List<String> lines) {
        lines.stream().map(Board::color).forEach(this.sideline::add);
        this.sideline.flush();
    }

    public void send(String title, List<String> lines) {
        this.sideline.getSidebar().setName(color(title));
        send(lines);
    }

    public void remove() {
        this.sideline.getSidebar().remove();
    }

    public enum SpecificWriterType {
        DISPLAY, ACTIONCHANGE, ACTIONREMOVE
    }

    public static class Sidebar {

        private final Player player;
        private final HashMap<String, Integer> linesA;
        private final HashMap<String, Integer> linesB;

        private Boolean a = true;

        private final SpecificWriterHandler handler;

        public Sidebar(Player p) {

            this.player = p;
            this.linesA = new HashMap<>();
            this.linesB = new HashMap<>();
            this.handler = new SpecificWriterHandler();

            PacketContainer createA = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            createA.getStrings().write(0, "A") // Unique name
                    .write(1, ""); // Display name
            createA.getIntegers().write(0, 0); // Mode : create
            handler.write(createA, SpecificWriterType.DISPLAY);

            PacketContainer createB = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            createB.getStrings().write(0, "B") // Unique name
                    .write(1, ""); // Display name
            createB.getIntegers().write(0, 0); // Mode : create
            handler.write(createB, SpecificWriterType.DISPLAY);

            ProtocolLibrary.getProtocolManager().sendServerPacket(p, createA);
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, createB);
        }

        private String getBuffer() {
            return a ? "A" : "B";
        }

        private HashMap<String, Integer> linesBuffer() {
            return a ? linesA : linesB;
        }

        private HashMap<String, Integer> linesDisplayed() {
            return (!a) ? linesA : linesB;
        }

        private void swapBuffer() {
            a = !a;
        }

        public void send() {
            PacketContainer display = new PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
            display.getIntegers().write(0, 1); // Position : sidebar
            display.getStrings().write(0, getBuffer()); // Unique name

            swapBuffer();

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, display);

            for (String text : linesDisplayed().keySet()) {
                if (linesBuffer().containsKey(text)) {
                    if (linesBuffer().get(text) == linesDisplayed().get(text)) {
                        continue;
                    }
                }

                setLine(text, linesDisplayed().get(text));
            }
            for (String text : new ArrayList<String>(linesBuffer().keySet())) {
                if (!linesDisplayed().containsKey(text)) {
                    removeLine(text);
                }
            }
        }

        public void remove() {
            PacketContainer removeA = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            removeA.getStrings().write(0, "A").write(1, "");
            removeA.getIntegers().write(0, 1);
            PacketContainer removeB = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            removeB.getStrings().write(0, "B").write(1, "");
            removeB.getIntegers().write(0, 1);

            ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, removeA);
            ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, removeB);
        }

        public void clear() {
            for (String text : new ArrayList<>(linesBuffer().keySet())) {
                removeLine(text);
            }
        }

        public void setLine(String text, Integer line) {
            if (text == null)
                return;

            if (text.length() > 40)
                text = text.substring(0, 40);

            if (linesBuffer().containsKey(text))
                removeLine(text);

            PacketContainer set = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
            set.getStrings().write(0, text).write(1, getBuffer());
            set.getIntegers().write(0, line);
            handler.write(set, SpecificWriterType.ACTIONCHANGE);

            ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, set);
            linesBuffer().put(text, line);
        }

        public void removeLine(String text) {

            if (text.length() > 40)
                text = text.substring(0, 40);

            if (!linesBuffer().containsKey(text))
                return;

            Integer line = linesBuffer().get(text);

            PacketContainer reset = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
            reset.getStrings().write(0, text).write(1, getBuffer());
            reset.getIntegers().write(0, line);
            handler.write(reset, SpecificWriterType.ACTIONREMOVE);

            ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, reset);
            linesBuffer().remove(text);
        }

        public String getName() {
            return player.getName();
        }

        public void setName(String displayName) {

            if (displayName.length() > 32)
                displayName = displayName.substring(0, 32);

            PacketContainer nameA = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            nameA.getStrings().write(0, "A").write(1, displayName);
            nameA.getIntegers().write(0, 2);
            handler.write(nameA, SpecificWriterType.DISPLAY);

            PacketContainer nameB = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            nameB.getStrings().write(0, "B").write(1, displayName);
            nameB.getIntegers().write(0, 2);
            handler.write(nameB, SpecificWriterType.DISPLAY);

            ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, nameA);
            ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, nameB);
        }
    }

    public static class Sideline {
        Sidebar sb;

        HashMap<Integer, String> old = new HashMap<>();
        Deque<String> buffer = new ArrayDeque<>();

        public Sideline(Sidebar sb) {
            this.sb = sb;
        }

        public void clear() {
            sb.clear();
            old.clear();
        }

        public void set(Integer i, String str) {
            if (old.containsKey(i)) {
                sb.removeLine(old.get(i));
            }

            if (str.equals(""))
                str = " ";

            str = makeUnique(str);

            old.put(i, str);
            sb.setLine(str, i);
        }

        public String makeUnique(String str) {
            while (old.containsValue(str)) {
                for (int j = 0; j < ChatColor.values().length; j++) {
                    if (!old.containsValue(str + ChatColor.values()[j])) {
                        str = str + ChatColor.values()[j];
                        return str;
                    }
                }
                str = str + ChatColor.RESET;
            }
            return str;
        }

        public void add(String s) {
            buffer.add(s);
        }

        public void flush() {
            clear();
            Integer i = 0;
            Iterator<String> it = buffer.descendingIterator();
            while (it.hasNext()) {
                String line = it.next();
                i++;
                set(i, line);
            }

            buffer.clear();

            sb.send();
        }

        public void send() {
            sb.send();
        }

        public Integer getRemainingSize() {
            return 15 - buffer.size();
        }

        public Sidebar getSidebar() {
            return this.sb;
        }
    }

    public static class SpecificWriterHandler {
        private static final String version;
        private static Class<?> healthclass;
        private static Object interger;

        static {
            version = getNMSVersion();
            try {
                healthclass = Class.forName(a("IScoreboardCriteria$EnumScoreboardHealthDisplay"));
                interger = healthclass.getEnumConstants()[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static String a(String str) {
            return "net.minecraft.server." + version + "." + str;
        }

        public static String b(String str) {
            return "org.bukkit.craftbukkit." + version + "." + str;
        }

        public static String getNMSVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        }

        public void write(PacketContainer container, SpecificWriterType type) {
            if (type == SpecificWriterType.DISPLAY) {
                container.getModifier().write(2, interger);
            } else if (type == SpecificWriterType.ACTIONCHANGE) {
                container.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.CHANGE);
            } else if (type == SpecificWriterType.ACTIONREMOVE) {
                container.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.REMOVE);
            }
        }
    }

}