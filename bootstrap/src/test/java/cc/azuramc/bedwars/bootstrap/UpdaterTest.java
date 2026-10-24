package cc.azuramc.bedwars.bootstrap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author an5w1r@163.com
 */
class UpdaterTest {

    private static final String RELEASES = "["
            + release("v1.0.0-beta.13", true, false,
            asset("AzuraBedWars-1.0.0-beta.13.jar", null))
            + "," + release("v1.0.0-beta.12", true, false,
            asset("AzuraBedWars-1.0.0-beta.12.jar", "sha256:aa"),
            asset("AzuraBedWars-1.0.0-beta.12-offline.jar", "sha256:bb"))
            + "," + release("v1.0.0-beta.10", true, false,
            asset("azurabedwars-plugin-1.0.0-beta.10-all.jar", "sha256:cc"))
            + "]";

    @Test
    void ordersVersionsSemantically() {
        assertTrue(Updater.compareVersions("1.0.0-beta.12", "1.0.0-beta.9") > 0);
        assertTrue(Updater.compareVersions("1.0.0", "1.0.0-beta.12") > 0);
        assertTrue(Updater.compareVersions("1.0.10", "1.0.9") > 0);
        assertTrue(Updater.compareVersions("1.0.0-beta.1", "1.0.0-alpha.2") > 0);
        assertEquals(0, Updater.compareVersions("1.0.0-beta.11", "1.0.0-beta.11"));
    }

    @Test
    void picksNewestReleaseOfTheSameKind() {
        Updater.Release regular = Updater.findUpdate(releases(), "1.0.0-beta.11", false, true);
        Updater.Release offline = Updater.findUpdate(releases(), "1.0.0-beta.11", true, true);

        assertEquals("AzuraBedWars-1.0.0-beta.12.jar", regular.name);
        assertEquals("aa", regular.sha256);
        assertEquals("AzuraBedWars-1.0.0-beta.12-offline.jar", offline.name);
        assertEquals("bb", offline.sha256);
    }

    @Test
    void ignoresOlderAndPrereleasesWhenExcluded() {
        assertNull(Updater.findUpdate(releases(), "1.0.0-beta.12", false, true));
        assertNull(Updater.findUpdate(releases(), "1.0.0-beta.11", false, false));
    }

    private static JsonArray releases() {
        return new Gson().fromJson(RELEASES, JsonArray.class);
    }

    private static String release(String tag, boolean prerelease, boolean draft, String... assets) {
        return "{\"tag_name\":\"" + tag + "\",\"prerelease\":" + prerelease + ",\"draft\":" + draft
                + ",\"assets\":[" + String.join(",", assets) + "]}";
    }

    private static String asset(String name, String digest) {
        return "{\"name\":\"" + name + "\",\"browser_download_url\":\"https://example.com/" + name + "\",\"digest\":"
                + (digest == null ? "null" : "\"" + digest + "\"") + "}";
    }
}
