package cc.azuramc.bedwars.devtools;

/**
 * A tool call that cannot be done, the message goes back to the AI client as the tool result
 *
 * @author an5w1r@163.com
 */
public class ToolError extends RuntimeException {

    public ToolError(String message) {
        super(message);
    }
}
