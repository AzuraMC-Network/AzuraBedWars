package cc.azuramc.bedwars.api.extension;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * A tool AI clients call through the development MCP of AzuraBedWars, listed as {@code <plugin>_<id>}
 *
 * @author an5w1r@163.com
 */
@FunctionalInterface
public interface DevTool {

    /**
     * Called on the main thread. A string result reaches the AI as text, anything else as JSON
     *
     * @param arguments the arguments the AI sent, shaped by the input schema
     * @throws IllegalArgumentException for a wrong argument, the message is shown to the AI
     */
    @NotNull
    JsonElement call(@NotNull JsonObject arguments) throws Exception;
}
