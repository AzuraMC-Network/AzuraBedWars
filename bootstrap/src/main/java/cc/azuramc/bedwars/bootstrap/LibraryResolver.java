package cc.azuramc.bedwars.bootstrap;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Reuses verified files first, then the bundled copy, then downloads
 *
 * @author an5w1r@163.com
 */
final class LibraryResolver {

    /** Must match the paths in plugin/build.gradle.kts */
    private static final String MANIFEST = "/META-INF/azurabedwars/libraries.txt";
    private static final String BUNDLE_DIRECTORY = "/META-INF/azurabedwars/libraries/";

    /** Must match libraries.repositories in config.yml */
    static final List<String> DEFAULT_REPOSITORIES = Collections.unmodifiableList(Arrays.asList(
            "https://repo1.maven.org/maven2/",
            "https://maven.aliyun.com/repository/public/",
            "https://jitpack.io/"
    ));

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int READ_TIMEOUT_MILLIS = 60_000;

    @NotNull
    private final Path directory;
    @NotNull
    private final List<String> repositories;
    @NotNull
    private final Logger logger;

    LibraryResolver(@NotNull Path directory, @NotNull List<String> repositories, @NotNull Logger logger) {
        this.directory = directory;
        this.logger = logger;
        List<String> normalized = new ArrayList<>();
        for (String repository : repositories) {
            String trimmed = repository.trim();
            // the Maven path is appended directly
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed.endsWith("/") ? trimmed : trimmed + "/");
            }
        }
        if (normalized.isEmpty()) {
            logger.warning("libraries.repositories is empty, using the default repositories");
            normalized.addAll(DEFAULT_REPOSITORIES);
        }
        this.repositories = normalized;
    }

    @NotNull
    static List<Library> readManifest() throws IOException {
        InputStream in = LibraryResolver.class.getResourceAsStream(MANIFEST);
        if (in == null) {
            throw new IOException("missing " + MANIFEST);
        }
        List<Library> libraries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    libraries.add(Library.parse(line));
                }
            }
        }
        return libraries;
    }

    @NotNull
    URL[] resolveAll(@NotNull List<Library> libraries) throws IOException {
        URL[] urls = new URL[libraries.size()];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = resolve(libraries.get(i)).toUri().toURL();
        }
        return urls;
    }

    @NotNull
    Path resolve(@NotNull Library library) throws IOException {
        Path target = directory.resolve(library.getRepositoryPath());
        if (Files.isRegularFile(target) && library.getSha256().equals(sha256(target))) {
            return target;
        }
        Files.createDirectories(target.getParent());
        InputStream bundled = LibraryResolver.class.getResourceAsStream(BUNDLE_DIRECTORY + library.getBundledName());
        if (bundled != null) {
            try (InputStream in = bundled) {
                install(in, library, target);
            }
            return target;
        }
        logger.info("Downloading " + library.getCoordinate());
        IOException last = null;
        for (String repository : repositories) {
            try (InputStream in = open(new URL(repository + library.getRepositoryPath()))) {
                install(in, library, target);
                return target;
            } catch (IOException e) {
                logger.warning("Failed to download " + library.getCoordinate() + " from " + repository + ": " + e.getMessage());
                last = e;
            }
        }
        throw new IOException("all repositories failed for " + library.getCoordinate(), last);
    }

    private static void install(@NotNull InputStream in, @NotNull Library library, @NotNull Path target) throws IOException {
        Path temp = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".part");
        try {
            MessageDigest digest = newDigest();
            Files.copy(new DigestInputStream(in, digest), temp, StandardCopyOption.REPLACE_EXISTING);
            String actual = hex(digest.digest());
            if (!library.getSha256().equals(actual)) {
                throw new IOException("sha256 mismatch for " + library.getCoordinate() + ": " + actual);
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /** Repositories may also be local file URLs */
    @NotNull
    private static InputStream open(@NotNull URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        connection.setRequestProperty("User-Agent", "AzuraBedWars");
        if (connection instanceof HttpURLConnection) {
            int code = ((HttpURLConnection) connection).getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                ((HttpURLConnection) connection).disconnect();
                throw new IOException("HTTP " + code);
            }
        }
        return connection.getInputStream();
    }

    @NotNull
    private static String sha256(@NotNull Path file) throws IOException {
        MessageDigest digest = newDigest();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return hex(digest.digest());
    }

    @NotNull
    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @NotNull
    private static String hex(byte @NotNull [] hash) {
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
