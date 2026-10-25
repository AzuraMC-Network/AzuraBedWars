package cc.azuramc.bedwars.config;

import lombok.Value;

/**
 * @author an5w1r@163.com
 */
@Value
public class ConfigProblem {

    public enum Severity {
        ERROR,
        WARNING
    }

    Severity severity;
    /** Relative to the plugin data folder, such as {@code shop/menu.yml} */
    String file;
    /** 1 based, 0 when the problem has no position */
    int line;
    int column;
    /** Dotted key path, such as {@code chat.global-chat-cooldown} or {@code lines[2]} */
    String path;
    String message;

    public boolean isError() {
        return severity == Severity.ERROR;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder(file);
        if (line > 0) {
            text.append(':').append(line);
        }
        if (!path.isEmpty()) {
            text.append(' ').append(path);
        }
        return text.append(": ").append(message).toString();
    }
}
