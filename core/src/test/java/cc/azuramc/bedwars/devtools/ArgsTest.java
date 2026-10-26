package cc.azuramc.bedwars.devtools;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArgsTest {

    @Test
    void rejectsFractionalAndOverflowingIntegers() {
        JsonObject json = new JsonObject();
        json.addProperty("whole", 2.0);
        json.addProperty("fraction", 1.9);
        json.addProperty("huge", 1e12);
        Args args = new Args(json);
        assertEquals(2, args.integer("whole"));
        assertThrows(ToolError.class, () -> args.integer("fraction"));
        assertThrows(ToolError.class, () -> args.integer("huge"));
    }
}
