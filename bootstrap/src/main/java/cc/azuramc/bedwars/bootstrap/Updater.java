package cc.azuramc.bedwars.bootstrap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Downloads the newest GitHub release of the same jar kind for Bukkit to install on restart
 *
 * @author an5w1r@163.com
 */
final class Updater implements Runnable {

    private static final String RELEASES = "https://api.github.com/repos/AzuraMC-Network/AzuraBedWars/releases?per_page=20";

    /** Must match archivesName and the offline classifier in plugin/build.gradle.kts */
    private static final String ASSET_PREFIX = "AzuraBedWars-";
    private static final String OFFLINE_SUFFIX = "-offline.jar";

    /** GitHub release asset digest format */
    private static final String DIGEST_PREFIX = "sha256:";

    @NotNull
    private final Logger logger;
    @NotNull
    private final String currentVersion;
    private final boolean offline;
    private final boolean includePrereleases;
    @NotNull
    private final Path target;

    Updater(@NotNull Logger logger, @NotNull String currentVersion, boolean offline, boolean includePrereleases, @NotNull Path target) {
        this.logger = logger;
        this.currentVersion = currentVersion;
        this.offline = offline;
        this.includePrereleases = includePrereleases;
        this.target = target;
    }

    @Override
    public void run() {
        try {
            JsonArray releases;
            try (Reader reader = new InputStreamReader(LibraryResolver.open(new URL(RELEASES)), StandardCharsets.UTF_8)) {
                releases = new Gson().fromJson(reader, JsonArray.class);
            }
            Release release = findUpdate(releases, currentVersion, offline, includePrereleases);
            if (release == null) {
                return;
            }
            if (Files.isRegularFile(target) && release.sha256.equals(LibraryResolver.sha256(target))) {
                return;
            }
            Files.createDirectories(target.getParent());
            try (InputStream in = LibraryResolver.open(new URL(release.url))) {
                LibraryResolver.install(in, release.sha256, release.name, target);
            }
            logger.info("Downloaded " + release.name + " into " + target.getParent() + ", restart the server to apply it");
        } catch (IOException | RuntimeException e) {
            logger.warning("Failed to check for updates: " + e);
        }
    }

    @Nullable
    static Release findUpdate(@NotNull JsonArray releases, @NotNull String currentVersion, boolean offline, boolean includePrereleases) {
        Release newest = null;
        String newestVersion = currentVersion;
        for (JsonElement element : releases) {
            JsonObject release = element.getAsJsonObject();
            if (release.get("draft").getAsBoolean() || (!includePrereleases && release.get("prerelease").getAsBoolean())) {
                continue;
            }
            String version = release.get("tag_name").getAsString().replaceFirst("^v", "");
            if (compareVersions(version, newestVersion) <= 0) {
                continue;
            }
            for (JsonElement assetElement : release.getAsJsonArray("assets")) {
                JsonObject asset = assetElement.getAsJsonObject();
                String name = asset.get("name").getAsString();
                JsonElement digest = asset.get("digest");
                if (name.startsWith(ASSET_PREFIX) && name.endsWith(".jar") && name.endsWith(OFFLINE_SUFFIX) == offline
                        && digest != null && !digest.isJsonNull() && digest.getAsString().startsWith(DIGEST_PREFIX)) {
                    newest = new Release(name, asset.get("browser_download_url").getAsString(),
                            digest.getAsString().substring(DIGEST_PREFIX.length()));
                    newestVersion = version;
                    break;
                }
            }
        }
        return newest;
    }

    /** Semantic versioning order */
    static int compareVersions(@NotNull String a, @NotNull String b) {
        String[] aParts = a.split("-", 2);
        String[] bParts = b.split("-", 2);
        int core = compareIdentifiers(aParts[0], bParts[0]);
        if (core != 0) {
            return core;
        }
        if (aParts.length != bParts.length) {
            return aParts.length == 1 ? 1 : -1;
        }
        return aParts.length == 1 ? 0 : compareIdentifiers(aParts[1], bParts[1]);
    }

    private static int compareIdentifiers(@NotNull String a, @NotNull String b) {
        String[] aIds = a.split("\\.");
        String[] bIds = b.split("\\.");
        for (int i = 0; i < Math.min(aIds.length, bIds.length); i++) {
            int result = aIds[i].matches("\\d+") && bIds[i].matches("\\d+")
                    ? Long.compare(Long.parseLong(aIds[i]), Long.parseLong(bIds[i]))
                    : aIds[i].compareTo(bIds[i]);
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(aIds.length, bIds.length);
    }

    static final class Release {

        @NotNull
        final String name;
        @NotNull
        final String url;
        @NotNull
        final String sha256;

        Release(@NotNull String name, @NotNull String url, @NotNull String sha256) {
            this.name = name;
            this.url = url;
            this.sha256 = sha256;
        }
    }
}
