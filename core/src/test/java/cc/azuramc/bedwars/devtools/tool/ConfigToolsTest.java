package cc.azuramc.bedwars.devtools.tool;

import cc.azuramc.bedwars.devtools.ToolError;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigToolsTest {

    @Test
    void hidesSecretValuesButKeepsCommentsAndEmptySections() {
        String text = "# 数据库密码\npassword: \"123456\"\nnested:\n  api-token: abc\n  password-hint:\nusername: root\n";
        assertEquals("# 数据库密码\npassword: \"<hidden>\"\nnested:\n  api-token: \"<hidden>\"\n  password-hint:\nusername: root\n",
                ConfigTools.hideSecrets(text));
    }

    @Test
    void keepsWholeNumbersIntegral() {
        assertEquals(5L, ConfigTools.scalar("a", new JsonPrimitive(5)));
        assertEquals(5L, ConfigTools.scalar("a", new JsonPrimitive(5.0)));
        assertEquals(0L, ConfigTools.scalar("a", new JsonPrimitive(0.0)));
        assertEquals(0.4, ConfigTools.scalar("a", new JsonPrimitive(0.4)));
        assertEquals(true, ConfigTools.scalar("a", new JsonPrimitive(true)));
        assertEquals("&a", ConfigTools.scalar("a", new JsonPrimitive("&a")));
        assertThrows(ToolError.class, () -> ConfigTools.scalar("a", new JsonArray()));
    }
}
