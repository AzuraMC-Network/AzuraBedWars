package cc.azuramc.bedwars.devtools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Keeps the latest plugin log lines, including errors other plugins or Bukkit log about this plugin
 *
 * @author an5w1r@163.com
 */
public final class LogBuffer extends Handler {

    private final int capacity;
    private final Deque<String[]> lines = new ArrayDeque<>();

    LogBuffer(int capacity) {
        this.capacity = capacity;
    }

    /** The root logger, because Bukkit and plugin loggers forward to it on every server version */
    void install() {
        uninstall();
        Logger.getLogger("").addHandler(this);
    }

    void uninstall() {
        Logger.getLogger("").removeHandler(this);
    }

    @Override
    public void publish(LogRecord record) {
        String message = record.getMessage() == null ? "" : record.getMessage();
        String stack = "";
        if (record.getThrown() != null) {
            StringWriter writer = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(writer));
            stack = writer.toString();
        }
        if (!isAboutPlugin(record.getLoggerName(), message, stack)) {
            return;
        }
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date(record.getMillis()));
        synchronized (lines) {
            lines.addLast(new String[]{record.getLevel().getName(), time + " " + record.getLevel().getName() + " " + message
                    + (stack.isEmpty() ? "" : "\n" + stack)});
            while (lines.size() > capacity) {
                lines.removeFirst();
            }
        }
    }

    private static boolean isAboutPlugin(String loggerName, String message, String stack) {
        return (loggerName != null && loggerName.contains("AzuraBedWars")) || message.contains("AzuraBedWars")
                || stack.contains("cc.azuramc.bedwars");
    }

    /**
     * Newest last
     */
    public List<String> tail(Level minimum, int limit, String contains) {
        List<String> result = new ArrayList<>();
        synchronized (lines) {
            for (String[] line : lines) {
                if (Level.parse(line[0]).intValue() >= minimum.intValue() && line[1].contains(contains)) {
                    result.add(line[1]);
                }
            }
        }
        return result.size() > limit ? result.subList(result.size() - limit, result.size()) : result;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
