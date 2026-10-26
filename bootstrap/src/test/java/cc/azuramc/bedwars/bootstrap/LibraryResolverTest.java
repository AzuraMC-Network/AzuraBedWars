package cc.azuramc.bedwars.bootstrap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author an5w1r@163.com
 */
class LibraryResolverTest {

    private static final byte[] CONTENT = "fake jar".getBytes(StandardCharsets.UTF_8);

    private final Logger logger = Logger.getLogger("LibraryResolverTest");

    @Test
    void parsesManifestLine() {
        Library library = Library.parse("org.mongodb:bson:5.5.1 abc");

        assertEquals("org/mongodb/bson/5.5.1/bson-5.5.1.jar", library.getRepositoryPath());
        assertEquals("org.mongodb/bson-5.5.1.jarinjar", library.getBundledName());
        assertEquals("abc", library.getSha256());
    }

    @Test
    void fallsBackToNextRepository(@TempDir Path dir) throws Exception {
        Path repository = publish(dir.resolve("repo"));
        LibraryResolver resolver = new LibraryResolver(dir.resolve("libs"),
                Arrays.asList(dir.resolve("missing").toUri().toString(), repository.toUri().toString()), logger);

        Path resolved = resolver.resolve(library(sha256(CONTENT)));

        assertArrayEquals(CONTENT, Files.readAllBytes(resolved));
    }

    @Test
    void appendsTrailingSlash(@TempDir Path dir) throws Exception {
        Path repository = publish(dir.resolve("repo"));
        String withoutSlash = repository.toUri().toString().replaceAll("/$", "");
        LibraryResolver resolver = new LibraryResolver(dir.resolve("libs"),
                Arrays.asList(" ", withoutSlash), logger);

        Path resolved = resolver.resolve(library(sha256(CONTENT)));

        assertArrayEquals(CONTENT, Files.readAllBytes(resolved));
    }

    @Test
    void replacesCorruptedFile(@TempDir Path dir) throws Exception {
        Path repository = publish(dir.resolve("repo"));
        LibraryResolver resolver = new LibraryResolver(dir.resolve("libs"),
                Collections.singletonList(repository.toUri().toString()), logger);
        Library library = library(sha256(CONTENT));
        Path target = dir.resolve("libs").resolve(library.getRepositoryPath());
        Files.createDirectories(target.getParent());
        Files.write(target, "broken".getBytes(StandardCharsets.UTF_8));

        resolver.resolve(library);

        assertArrayEquals(CONTENT, Files.readAllBytes(target));
    }

    @Test
    void rejectsChecksumMismatch(@TempDir Path dir) throws Exception {
        Path repository = publish(dir.resolve("repo"));
        LibraryResolver resolver = new LibraryResolver(dir.resolve("libs"),
                Collections.singletonList(repository.toUri().toString()), logger);
        Library library = library("0000");

        assertThrows(IOException.class, () -> resolver.resolve(library));
        assertFalse(Files.exists(dir.resolve("libs").resolve(library.getRepositoryPath())));
    }

    private static Library library(String sha256) {
        return new Library("com.example", "demo", "1.0", sha256);
    }

    private static Path publish(Path repository) throws IOException {
        Path file = repository.resolve(library("").getRepositoryPath());
        Files.createDirectories(file.getParent());
        Files.write(file, CONTENT);
        return repository;
    }

    private static String sha256(byte[] bytes) throws Exception {
        StringBuilder hex = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
