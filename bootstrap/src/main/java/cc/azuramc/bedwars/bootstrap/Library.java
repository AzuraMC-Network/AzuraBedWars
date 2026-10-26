package cc.azuramc.bedwars.bootstrap;

import org.jetbrains.annotations.NotNull;

/**
 * @author an5w1r@163.com
 */
final class Library {

    @NotNull
    private final String group;
    @NotNull
    private final String artifact;
    @NotNull
    private final String version;
    @NotNull
    private final String sha256;

    Library(@NotNull String group, @NotNull String artifact, @NotNull String version, @NotNull String sha256) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.sha256 = sha256;
    }

    /** Must match the RuntimeLibrariesTask manifest line */
    @NotNull
    static Library parse(@NotNull String line) {
        String[] parts = line.split(" ");
        String[] coordinate = parts[0].split(":");
        return new Library(coordinate[0], coordinate[1], coordinate[2], parts[1]);
    }

    @NotNull
    String getSha256() {
        return sha256;
    }

    @NotNull
    String getCoordinate() {
        return group + ":" + artifact + ":" + version;
    }

    @NotNull
    String getRepositoryPath() {
        return group.replace('.', '/') + "/" + artifact + "/" + version + "/" + artifact + "-" + version + ".jar";
    }

    /** Must match the RuntimeLibrariesTask bundle layout */
    @NotNull
    String getBundledName() {
        return group + "/" + artifact + "-" + version + ".jarinjar";
    }
}
