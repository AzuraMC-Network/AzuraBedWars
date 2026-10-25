package cc.azuramc.bedwars.config;

import cc.azuramc.bedwars.config.annotation.RestartRequired;
import cc.azuramc.bedwars.config.yaml.ConfigKeys;
import cc.azuramc.bedwars.config.yaml.SchemaExporter;
import cc.azuramc.bedwars.config.yaml.YamlBinder;
import cc.azuramc.bedwars.config.yaml.YamlWriter;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

/**
 * Owns every config document: creates missing files, binds them, keeps the current values and applies changes.
 * Files are only written by {@link #write} and {@link #setValue}, never on startup or shutdown
 *
 * @author an5w1r@163.com
 */
public final class ConfigService {

    public static final String SCHEMA_DIR = "schema";
    public static final String HISTORY_DIR = ".history";
    private static final int HISTORY_LIMIT = 10;

    private final File dataFolder;
    private final BooleanSupplier gameRunning;
    private final Map<String, ConfigDocument<?>> documents = new LinkedHashMap<>();
    private final Map<Class<?>, Object> values = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> pending = new ConcurrentHashMap<>();

    public ConfigService(File dataFolder, BooleanSupplier gameRunning) {
        this.dataFolder = dataFolder;
        this.gameRunning = gameRunning;
    }

    public <T> void register(String id, String path, Class<T> type, ApplyMode applyMode, int version) {
        documents.put(id, new ConfigDocument<>(id, path, type, applyMode, version));
    }

    public List<ConfigDocument<?>> documents() {
        return Collections.unmodifiableList(new ArrayList<>(documents.values()));
    }

    @Nullable
    public ConfigDocument<?> document(String id) {
        return documents.get(id);
    }

    @NotNull
    public <T> T get(Class<T> type) {
        return type.cast(Objects.requireNonNull(values.get(type), () -> type.getSimpleName() + " is not loaded"));
    }

    public File file(ConfigDocument<?> document) {
        return new File(dataFolder, document.getPath());
    }

    /**
     * Creates missing files and loads every document. A value that cannot be used falls back to its default
     */
    public List<ConfigProblem> loadAll() {
        List<ConfigProblem> problems = new ArrayList<>();
        for (ConfigDocument<?> document : documents.values()) {
            File file = file(document);
            try {
                if (!file.exists()) {
                    writeAtomically(file, template(document));
                }
                Loaded<?> loaded = bind(document, read(file));
                values.put(document.getType(), loaded.value);
                problems.addAll(loaded.problems);
            } catch (IOException e) {
                values.put(document.getType(), ConfigKeys.newInstance(document.getType()));
                problems.add(fileProblem(document, "Cannot read or create the file, defaults are used: " + e.getMessage()));
            }
        }
        exportSchemas();
        return problems;
    }

    /**
     * Template of a missing file, written from the defaults in the config class
     */
    public String template(ConfigDocument<?> document) {
        return render(document, ConfigKeys.newInstance(document.getType()));
    }

    public String render(ConfigDocument<?> document, Object value) {
        return YamlWriter.write(value, document.getVersion(), schemaPath(document));
    }

    public JsonObject schema(String id) {
        ConfigDocument<?> document = require(id);
        return SchemaExporter.export(document.getType(), document.getPath());
    }

    public void exportSchemas() {
        for (ConfigDocument<?> document : documents.values()) {
            File file = new File(new File(dataFolder, SCHEMA_DIR), document.getId() + ".json");
            try {
                writeAtomically(file, new GsonBuilder().setPrettyPrinting().create().toJson(schema(document.getId())));
            } catch (IOException ignored) {
                // editor support only
            }
        }
    }

    /**
     * Checks text as if it were the content of a document, without touching the file
     */
    public List<ConfigProblem> validate(String id, String text) {
        return bind(require(id), text).problems;
    }

    /**
     * Problems of every document as it is on disk now
     */
    public List<ConfigProblem> check() {
        List<ConfigProblem> problems = new ArrayList<>();
        for (ConfigDocument<?> document : documents.values()) {
            try {
                problems.addAll(bind(document, read(file(document))).problems);
            } catch (IOException e) {
                problems.add(fileProblem(document, "Cannot read the file: " + e.getMessage()));
            }
        }
        return problems;
    }

    /**
     * Rereads one document from disk. Rejected when the file has any error, the current values stay then
     */
    public ApplyResult reload(String id) {
        ConfigDocument<?> document = require(id);
        try {
            return apply(document, read(file(document)));
        } catch (IOException e) {
            return ApplyResult.rejected(id, Collections.singletonList(fileProblem(document, "Cannot read the file: " + e.getMessage())));
        }
    }

    /**
     * Swaps in {@link ApplyMode#NEXT_GAME} documents that were reloaded during a game
     */
    public void applyPending() {
        values.putAll(pending);
        pending.clear();
    }

    public List<ApplyResult> reloadAll() {
        List<ApplyResult> results = new ArrayList<>();
        for (String id : documents.keySet()) {
            results.add(reload(id));
        }
        return results;
    }

    /**
     * Replaces the whole file after validating it. The previous file goes to {@value #HISTORY_DIR}
     *
     * @param expectedHash {@link #hash} of the file the text was based on, null to skip the check
     */
    public ApplyResult write(String id, String text, @Nullable String expectedHash) {
        ConfigDocument<?> document = require(id);
        File file = file(document);
        try {
            if (expectedHash != null && !expectedHash.equals(hash(id))) {
                return ApplyResult.rejected(id, Collections.singletonList(fileProblem(document,
                        "The file changed on disk since it was read, read it again before writing")));
            }
            Loaded<?> loaded = bind(document, text);
            if (hasError(loaded.problems)) {
                return ApplyResult.rejected(id, loaded.problems);
            }
            backup(document, file);
            writeAtomically(file, text);
        } catch (IOException e) {
            return ApplyResult.rejected(id, Collections.singletonList(fileProblem(document, "Cannot write the file: " + e.getMessage())));
        }
        return apply(document, text);
    }

    /**
     * Changes one value in place, leaving comments and formatting of the rest of the file alone
     *
     * @param path dotted key path such as {@code debug-mode}
     */
    public ApplyResult setValue(String id, String path, Object value) {
        ConfigDocument<?> document = require(id);
        String text;
        try {
            text = read(file(document));
        } catch (IOException e) {
            return ApplyResult.rejected(id, Collections.singletonList(fileProblem(document, "Cannot read the file: " + e.getMessage())));
        }
        YamlBinder binder = new YamlBinder(document.getPath(), true);
        Node node = YamlBinder.find(binder.parse(text), path);
        String scalar = YamlWriter.scalar(value);
        String updated;
        if (node instanceof ScalarNode) {
            updated = replace(text, node.getStartMark(), node.getEndMark(), scalar);
        } else if (node == null && !path.contains(".") && binder.getProblems().isEmpty()) {
            updated = text + (text.endsWith("\n") || text.isEmpty() ? "" : "\n") + path + ": " + scalar + "\n";
        } else {
            return ApplyResult.rejected(id, Collections.singletonList(new ConfigProblem(ConfigProblem.Severity.ERROR,
                    document.getPath(), 0, 0, path, "Cannot set this key in place, edit the file instead")));
        }
        return write(id, updated, null);
    }

    /**
     * SHA-256 of the file on disk, empty when it does not exist
     */
    public String hash(String id) throws IOException {
        File file = file(require(id));
        if (!file.exists()) {
            return "";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file.toPath()));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> ApplyResult apply(ConfigDocument<T> document, String text) {
        Loaded<T> loaded = bind(document, text);
        if (hasError(loaded.problems)) {
            return ApplyResult.rejected(document.getId(), loaded.problems);
        }
        T current = document.getType().cast(values.get(document.getType()));
        List<String> restartPaths = new ArrayList<>();
        if (document.getApplyMode() == ApplyMode.RESTART) {
            restartPaths.addAll(changedKeys(current, loaded.value));
            if (!restartPaths.isEmpty()) {
                return new ApplyResult(document.getId(), ApplyResult.Status.RESTART_REQUIRED, loaded.problems, restartPaths);
            }
            return new ApplyResult(document.getId(), ApplyResult.Status.APPLIED, loaded.problems, restartPaths);
        }
        if (current != null) {
            keepRestartRequired(current, loaded.value, "", restartPaths);
        }
        if (document.getApplyMode() == ApplyMode.NEXT_GAME && gameRunning.getAsBoolean()) {
            pending.put(document.getType(), loaded.value);
            return new ApplyResult(document.getId(), ApplyResult.Status.NEXT_GAME, loaded.problems, restartPaths);
        }
        pending.remove(document.getType());
        values.put(document.getType(), loaded.value);
        ApplyResult.Status status = restartPaths.isEmpty() ? ApplyResult.Status.APPLIED : ApplyResult.Status.RESTART_REQUIRED;
        return new ApplyResult(document.getId(), status, loaded.problems, restartPaths);
    }

    private static void keepRestartRequired(Object current, Object updated, String path, List<String> restartPaths) {
        for (Map.Entry<String, Field> entry : ConfigKeys.fields(current.getClass()).entrySet()) {
            Field field = entry.getValue();
            String childPath = path.isEmpty() ? entry.getKey() : path + "." + entry.getKey();
            Object oldValue = get(field, current);
            Object newValue = get(field, updated);
            if (field.isAnnotationPresent(RestartRequired.class)) {
                if (!deepEquals(oldValue, newValue)) {
                    restartPaths.add(childPath);
                    set(field, updated, oldValue);
                }
            } else if (oldValue != null && newValue != null && !ConfigKeys.isScalar(field.getType())
                    && !field.getType().isArray() && !List.class.isAssignableFrom(field.getType())
                    && !Map.class.isAssignableFrom(field.getType())) {
                keepRestartRequired(oldValue, newValue, childPath, restartPaths);
            }
        }
    }

    private static List<String> changedKeys(Object current, Object updated) {
        List<String> changed = new ArrayList<>();
        for (Map.Entry<String, Field> entry : ConfigKeys.fields(current.getClass()).entrySet()) {
            if (!deepEquals(get(entry.getValue(), current), get(entry.getValue(), updated))) {
                changed.add(entry.getKey());
            }
        }
        return changed;
    }

    private static boolean deepEquals(@Nullable Object a, @Nullable Object b) {
        return Objects.deepEquals(a, b);
    }

    private <T> Loaded<T> bind(ConfigDocument<T> document, String text) {
        YamlBinder binder = new YamlBinder(document.getPath(), true);
        T value = binder.bind(binder.parse(text), document.getType());
        return new Loaded<>(value, binder.getProblems());
    }

    private void backup(ConfigDocument<?> document, File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        File history = new File(dataFolder, HISTORY_DIR);
        String prefix = document.getPath().replace('/', '_') + ".";
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
        Files.createDirectories(history.toPath());
        Files.copy(file.toPath(), new File(history, prefix + stamp).toPath(), StandardCopyOption.REPLACE_EXISTING);

        File[] old = history.listFiles((dir, name) -> name.startsWith(prefix));
        if (old != null && old.length > HISTORY_LIMIT) {
            Arrays.sort(old);
            for (int i = 0; i < old.length - HISTORY_LIMIT; i++) {
                Files.deleteIfExists(old[i].toPath());
            }
        }
    }

    public static void writeAtomically(File file, String text) throws IOException {
        Files.createDirectories(file.getAbsoluteFile().getParentFile().toPath());
        File temp = new File(file.getAbsoluteFile().getParentFile(), "." + file.getName() + ".tmp");
        Files.write(temp.toPath(), text.getBytes(StandardCharsets.UTF_8));
        try {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    /** SnakeYAML marks count code points, String offsets count UTF-16 units */
    private static String replace(String text, Mark start, Mark end, String replacement) {
        return text.substring(0, offset(text, start)) + replacement + text.substring(offset(text, end));
    }

    private static int offset(String text, Mark mark) {
        int lineStart = 0;
        for (int line = 0; line < mark.getLine(); line++) {
            lineStart = text.indexOf('\n', lineStart) + 1;
        }
        return text.offsetByCodePoints(lineStart, mark.getColumn());
    }

    private String schemaPath(ConfigDocument<?> document) {
        StringBuilder up = new StringBuilder();
        for (char c : document.getPath().toCharArray()) {
            if (c == '/') {
                up.append("../");
            }
        }
        return up + SCHEMA_DIR + "/" + document.getId() + ".json";
    }

    private ConfigDocument<?> require(String id) {
        ConfigDocument<?> document = documents.get(id);
        if (document == null) {
            throw new IllegalArgumentException("Unknown config document " + id + ", known: " + documents.keySet());
        }
        return document;
    }

    private static ConfigProblem fileProblem(ConfigDocument<?> document, String message) {
        return new ConfigProblem(ConfigProblem.Severity.ERROR, document.getPath(), 0, 0, "", message);
    }

    private static boolean hasError(List<ConfigProblem> problems) {
        for (ConfigProblem problem : problems) {
            if (problem.isError()) {
                return true;
            }
        }
        return false;
    }

    private static Object get(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void set(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class Loaded<T> {
        final T value;
        final List<ConfigProblem> problems;

        Loaded(T value, List<ConfigProblem> problems) {
            this.value = value;
            this.problems = problems;
        }
    }
}
