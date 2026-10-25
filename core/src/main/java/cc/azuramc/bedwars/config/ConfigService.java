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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Owns every config document: creates missing files, binds them, keeps the current values and applies changes.
 * Files are only written by {@link #write}, {@link #setValue} and layout migrations, never on startup or shutdown
 *
 * @author an5w1r@163.com
 */
public final class ConfigService {

    public static final String SCHEMA_DIR = "schema";
    public static final String HISTORY_DIR = ".history";
    private static final String TEMPLATE_ROOT = "/config-templates/";
    private static final int HISTORY_LIMIT = 10;

    private final File dataFolder;
    private final BooleanSupplier gameRunning;
    private final Map<String, ConfigDocument<?>> documents = new LinkedHashMap<>();
    private final Map<String, Directory> directories = new LinkedHashMap<>();
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, Object> pending = new HashMap<>();
    private final Map<String, TreeMap<Integer, UnaryOperator<Map<String, Object>>>> migrations = new HashMap<>();
    private final List<ConfigValidator> validators = new ArrayList<>();
    private Supplier<String> language = () -> null;

    public ConfigService(File dataFolder, BooleanSupplier gameRunning) {
        this.dataFolder = dataFolder;
        this.gameRunning = gameRunning;
    }

    public <T> void register(String id, String path, Class<T> type, ApplyMode applyMode, int version) {
        documents.put(id, new ConfigDocument<>(id, path, type, applyMode, version, id, null));
    }

    /**
     * A document whose missing file is copied from {@code config-templates/<path>} in the jar
     */
    public <T> void registerTemplate(String id, String path, Class<T> type, ApplyMode applyMode, int version, String kind) {
        documents.put(id, new ConfigDocument<>(id, path, type, applyMode, version, kind, TEMPLATE_ROOT + path));
    }

    /**
     * Every {@code .yml} file in a directory is one document with the id {@code <directory>/<file name>}.
     * When the directory is missing the bundled files are copied into it
     */
    public <T> void registerDirectory(String directory, Class<T> type, ApplyMode applyMode, int version, String kind, String... bundled) {
        directories.put(directory, new Directory(directory, type, applyMode, version, kind, Arrays.asList(bundled)));
    }

    /**
     * @param step turns the raw content of {@code fromVersion} into {@code fromVersion + 1}
     */
    public void addMigration(String kind, int fromVersion, UnaryOperator<Map<String, Object>> step) {
        migrations.computeIfAbsent(kind, k -> new TreeMap<>()).put(fromVersion, step);
    }

    public void addValidator(ConfigValidator validator) {
        validators.add(validator);
    }

    /**
     * Picks {@code config-templates/<language>/<path>} over {@code config-templates/<path>} for missing files and migrations
     */
    public void setLanguage(Supplier<String> language) {
        this.language = language;
    }

    public Supplier<String> getLanguage() {
        return language;
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
        return get(idOf(type), type);
    }

    @NotNull
    public <T> T get(String id, Class<T> type) {
        return type.cast(Objects.requireNonNull(values.get(id), () -> id + " is not loaded"));
    }

    /**
     * Every loaded document of a type in file order, keyed by document id
     */
    @NotNull
    public <T> Map<String, T> all(Class<T> type) {
        Map<String, T> all = new LinkedHashMap<>();
        for (ConfigDocument<?> document : documents.values()) {
            Object value = values.get(document.getId());
            if (document.getType() == type && value != null) {
                all.put(document.getId(), type.cast(value));
            }
        }
        return all;
    }

    /**
     * {@code shop/categories/blocks} becomes {@code blocks}
     */
    public static String name(String documentId) {
        return documentId.substring(documentId.lastIndexOf('/') + 1);
    }

    public File file(ConfigDocument<?> document) {
        return new File(dataFolder, document.getPath());
    }

    /**
     * Creates missing files and loads every document. A value that cannot be used falls back to its default
     */
    public List<ConfigProblem> loadAll() {
        List<ConfigProblem> problems = new ArrayList<>();
        for (ConfigDocument<?> document : new ArrayList<>(documents.values())) {
            if (directoryOf(document) == null) {
                problems.addAll(load(document));
            }
        }
        for (Directory directory : directories.values()) {
            problems.addAll(scan(directory));
            for (String id : documentsIn(directory)) {
                problems.addAll(load(documents.get(id)));
            }
        }
        problems.addAll(validate(Collections.<String, Object>emptyMap(), Collections.<String, String>emptyMap()));
        exportSchemas();
        return problems;
    }

    private List<ConfigProblem> load(ConfigDocument<?> document) {
        File file = file(document);
        try {
            if (!file.exists()) {
                writeAtomically(file, template(document));
            }
            String text = read(file);
            Loaded<?> loaded = bind(document, text);
            if (loaded.upgraded != null) {
                persistMigration(document, file, loaded.upgraded);
            }
            values.put(document.getId(), loaded.value);
            return loaded.problems;
        } catch (IOException e) {
            values.put(document.getId(), new YamlBinder(document.getPath(), true).bind(null, document.getType()));
            return Collections.singletonList(fileProblem(document, "Cannot read or create the file, defaults are used: " + e.getMessage()));
        }
    }

    /**
     * Template of a missing file, the bundled one or the defaults of the config class
     */
    public String template(ConfigDocument<?> document) throws IOException {
        String bundled = localized(document.getPath());
        if (bundled != null) {
            return bundled;
        }
        if (document.getTemplate() == null) {
            return render(document, ConfigKeys.newInstance(document.getType()));
        }
        throw new IOException("Missing bundled template " + document.getTemplate());
    }

    public String render(ConfigDocument<?> document, Object value) {
        return YamlWriter.write(value, document.getVersion(), schemaPath(document));
    }

    public JsonObject schema(String id) {
        ConfigDocument<?> document = require(id);
        return SchemaExporter.export(document.getType(), document.getKind());
    }

    public void exportSchemas() {
        Set<String> written = new HashSet<>();
        for (ConfigDocument<?> document : documents.values()) {
            if (!written.add(document.getKind())) {
                continue;
            }
            File file = new File(new File(dataFolder, SCHEMA_DIR), document.getKind().replace('/', '-') + ".json");
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
        ConfigDocument<?> document = require(id);
        Loaded<?> loaded = bind(document, text);
        List<ConfigProblem> problems = new ArrayList<>(loaded.problems);
        problems.addAll(validate(Collections.<String, Object>singletonMap(id, loaded.value), Collections.singletonMap(id, text)));
        return problems;
    }

    /**
     * Problems of every document as it is on disk now
     */
    public List<ConfigProblem> check() {
        List<ConfigProblem> problems = new ArrayList<>();
        Map<String, Object> candidates = new HashMap<>();
        Map<String, String> texts = new HashMap<>();
        for (ConfigDocument<?> document : documents.values()) {
            try {
                String text = read(file(document));
                Loaded<?> loaded = bind(document, text);
                problems.addAll(loaded.problems);
                candidates.put(document.getId(), loaded.value);
                texts.put(document.getId(), text);
            } catch (IOException e) {
                problems.add(fileProblem(document, "Cannot read the file: " + e.getMessage()));
            }
        }
        problems.addAll(validate(candidates, texts));
        return problems;
    }

    /**
     * Rereads one document, or every document of a registered directory, from disk.
     * Rejected when the file has any error, the current values stay then
     */
    public List<ApplyResult> reload(String id) {
        Directory directory = directories.get(id);
        if (directory != null) {
            return reloadDirectory(directory);
        }
        return Collections.singletonList(reloadDocument(require(id)));
    }

    public boolean isReloadable(String id) {
        return documents.containsKey(id) || directories.containsKey(id);
    }

    public List<String> reloadableIds() {
        List<String> ids = new ArrayList<>(directories.keySet());
        ids.addAll(documents.keySet());
        return ids;
    }

    private ApplyResult reloadDocument(ConfigDocument<?> document) {
        try {
            return apply(document, read(file(document)));
        } catch (IOException e) {
            return ApplyResult.rejected(document.getId(), Collections.singletonList(fileProblem(document, "Cannot read the file: " + e.getMessage())));
        }
    }

    private List<ApplyResult> reloadDirectory(Directory directory) {
        List<ApplyResult> results = new ArrayList<>();
        Set<String> before = new HashSet<>(documentsIn(directory));
        List<ConfigProblem> scanProblems = scan(directory);
        for (String id : documentsIn(directory)) {
            ApplyResult result = reloadDocument(documents.get(id));
            if (!scanProblems.isEmpty()) {
                List<ConfigProblem> problems = new ArrayList<>(scanProblems);
                problems.addAll(result.getProblems());
                result = new ApplyResult(id, result.getStatus(), problems, result.getRestartPaths());
                scanProblems = Collections.emptyList();
            }
            results.add(result);
        }
        for (String id : before) {
            if (!documents.containsKey(id)) {
                values.remove(id);
                pending.remove(id);
                results.add(new ApplyResult(id, ApplyResult.Status.APPLIED, Collections.<ConfigProblem>emptyList(), Collections.<String>emptyList()));
            }
        }
        return results;
    }

    public List<ApplyResult> reloadAll() {
        List<ApplyResult> results = new ArrayList<>();
        for (String directory : new ArrayList<>(directories.keySet())) {
            results.addAll(reloadDirectory(directories.get(directory)));
        }
        for (ConfigDocument<?> document : new ArrayList<>(documents.values())) {
            if (directoryOf(document) == null) {
                results.add(reloadDocument(document));
            }
        }
        return results;
    }

    /**
     * Swaps in {@link ApplyMode#NEXT_GAME} documents that were reloaded during a game
     */
    public void applyPending() {
        values.putAll(pending);
        pending.clear();
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
            List<ConfigProblem> problems = new ArrayList<>(loaded.problems);
            problems.addAll(newErrors(document, loaded.value, text));
            if (hasError(problems)) {
                return ApplyResult.rejected(id, problems);
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
        return setValues(id, Collections.singletonMap(path, value), null);
    }

    /**
     * Changes several scalar values in place and applies them together, or none of them
     *
     * @param expectedHash {@link #hash} of the file the values were based on, null to skip the check
     */
    public ApplyResult setValues(String id, Map<String, Object> changes, @Nullable String expectedHash) {
        ConfigDocument<?> document = require(id);
        String text;
        try {
            if (expectedHash != null && !expectedHash.equals(hash(id))) {
                return ApplyResult.rejected(id, Collections.singletonList(fileProblem(document,
                        "The file changed on disk since it was read, read it again before writing")));
            }
            text = read(file(document));
        } catch (IOException e) {
            return ApplyResult.rejected(id, Collections.singletonList(fileProblem(document, "Cannot read the file: " + e.getMessage())));
        }
        YamlBinder binder = new YamlBinder(document.getPath(), true);
        Node root = binder.parse(text);
        List<int[]> ranges = new ArrayList<>();
        List<String> replacements = new ArrayList<>();
        StringBuilder appended = new StringBuilder();
        for (Map.Entry<String, Object> change : changes.entrySet()) {
            String path = change.getKey();
            Node node = YamlBinder.find(root, path);
            String scalar = YamlWriter.scalar(change.getValue());
            if (node instanceof ScalarNode) {
                ranges.add(new int[]{offset(text, node.getStartMark()), offset(text, node.getEndMark())});
                replacements.add(scalar);
            } else if (node == null && !path.contains(".") && binder.getProblems().isEmpty()) {
                appended.append(path).append(": ").append(scalar).append('\n');
            } else {
                return ApplyResult.rejected(id, Collections.singletonList(new ConfigProblem(ConfigProblem.Severity.ERROR,
                        document.getPath(), 0, 0, path, "Cannot set this key in place, edit the file instead")));
            }
        }
        StringBuilder updated = new StringBuilder(text);
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < ranges.size(); i++) {
            order.add(i);
        }
        order.sort((a, b) -> Integer.compare(ranges.get(b)[0], ranges.get(a)[0]));
        for (int i : order) {
            updated.replace(ranges.get(i)[0], ranges.get(i)[1], replacements.get(i));
        }
        if (appended.length() > 0 && updated.length() > 0 && updated.charAt(updated.length() - 1) != '\n') {
            updated.append('\n');
        }
        return write(id, updated.append(appended).toString(), null);
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
        List<ConfigProblem> problems = new ArrayList<>(loaded.problems);
        problems.addAll(newErrors(document, loaded.value, text));
        if (hasError(problems)) {
            return ApplyResult.rejected(document.getId(), problems);
        }
        if (loaded.upgraded != null) {
            try {
                persistMigration(document, file(document), loaded.upgraded);
            } catch (IOException e) {
                problems.add(fileProblem(document, "Cannot write the upgraded layout: " + e.getMessage()));
            }
        }
        T current = document.getType().cast(values.get(document.getId()));
        List<String> restartPaths = new ArrayList<>();
        if (document.getApplyMode() == ApplyMode.RESTART) {
            if (current != null) {
                restartPaths.addAll(changedKeys(current, loaded.value));
            }
            ApplyResult.Status status = restartPaths.isEmpty() ? ApplyResult.Status.APPLIED : ApplyResult.Status.RESTART_REQUIRED;
            return new ApplyResult(document.getId(), status, problems, restartPaths);
        }
        if (current != null) {
            keepRestartRequired(current, loaded.value, "", restartPaths);
        }
        if (document.getApplyMode() == ApplyMode.NEXT_GAME && gameRunning.getAsBoolean()) {
            pending.put(document.getId(), loaded.value);
            return new ApplyResult(document.getId(), ApplyResult.Status.NEXT_GAME, problems, restartPaths);
        }
        pending.remove(document.getId());
        values.put(document.getId(), loaded.value);
        ApplyResult.Status status = restartPaths.isEmpty() ? ApplyResult.Status.APPLIED : ApplyResult.Status.RESTART_REQUIRED;
        return new ApplyResult(document.getId(), status, problems, restartPaths);
    }

    /**
     * Reference errors the candidate adds, so an error elsewhere never blocks an unrelated reload
     */
    private List<ConfigProblem> newErrors(ConfigDocument<?> document, Object candidate, String text) {
        if (validators.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> existing = new HashSet<>();
        for (ConfigProblem problem : validate(Collections.<String, Object>emptyMap(), Collections.<String, String>emptyMap())) {
            existing.add(problem.getFile() + "|" + problem.getPath() + "|" + problem.getMessage());
        }
        List<ConfigProblem> added = new ArrayList<>();
        for (ConfigProblem problem : validate(Collections.singletonMap(document.getId(), candidate), Collections.singletonMap(document.getId(), text))) {
            if (!existing.contains(problem.getFile() + "|" + problem.getPath() + "|" + problem.getMessage())) {
                added.add(problem);
            }
        }
        return added;
    }

    private List<ConfigProblem> validate(Map<String, Object> candidates, Map<String, String> texts) {
        List<ConfigProblem> problems = new ArrayList<>();
        if (validators.isEmpty()) {
            return problems;
        }
        Map<String, Object> view = new HashMap<>(values);
        view.putAll(candidates);
        Map<String, Node> roots = new HashMap<>();
        ConfigValidator.View lookup = new ConfigValidator.View() {
            @NotNull
            @Override
            public <T> T get(Class<T> type) {
                return type.cast(Objects.requireNonNull(view.get(idOf(type)), () -> type.getSimpleName() + " is not loaded"));
            }

            @NotNull
            @Override
            public <T> Map<String, T> all(Class<T> type) {
                Map<String, T> all = new LinkedHashMap<>();
                for (ConfigDocument<?> document : documents.values()) {
                    Object value = view.get(document.getId());
                    if (document.getType() == type && value != null) {
                        all.put(document.getId(), type.cast(value));
                    }
                }
                return all;
            }

            @NotNull
            @Override
            public String idOf(Class<?> type) {
                return ConfigService.this.idOf(type);
            }
        };
        ConfigValidator.Sink sink = new ConfigValidator.Sink() {
            @Override
            public void error(String documentId, String path, String message) {
                problems.add(located(ConfigProblem.Severity.ERROR, documentId, path, message, texts, roots));
            }

            @Override
            public void warn(String documentId, String path, String message) {
                problems.add(located(ConfigProblem.Severity.WARNING, documentId, path, message, texts, roots));
            }
        };
        for (ConfigValidator validator : validators) {
            validator.validate(lookup, sink);
        }
        return problems;
    }

    private ConfigProblem located(ConfigProblem.Severity severity, String documentId, String path, String message,
                                  Map<String, String> texts, Map<String, Node> roots) {
        ConfigDocument<?> document = documents.get(documentId);
        String file = document != null ? document.getPath() : documentId;
        Node root = roots.computeIfAbsent(documentId, id -> {
            try {
                String text = texts.containsKey(id) ? texts.get(id) : document != null ? read(file(document)) : null;
                return text != null ? new YamlBinder(file, false).parse(text) : null;
            } catch (IOException e) {
                return null;
            }
        });
        Node node = null;
        // shorthand values have no node for the nested path
        for (String at = path; node == null && !at.isEmpty(); at = at.contains(".") ? at.substring(0, at.lastIndexOf('.')) : "") {
            node = YamlBinder.find(root, at);
        }
        Mark mark = node != null ? node.getStartMark() : null;
        return new ConfigProblem(severity, file, mark != null ? mark.getLine() + 1 : 0, mark != null ? mark.getColumn() + 1 : 0, path, message);
    }

    private static void keepRestartRequired(Object current, Object updated, String path, List<String> restartPaths) {
        if (current instanceof Map) {
            return;
        }
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
                    && !Map.class.isAssignableFrom(field.getType()) && !Node.class.isAssignableFrom(field.getType())) {
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
        Node root = binder.parse(text);
        int version = version(root, document.getVersion());
        if (version < document.getVersion() && binder.getProblems().isEmpty()) {
            String upgraded = upgrade(document, text, version);
            if (upgraded != null) {
                YamlBinder upgradedBinder = new YamlBinder(document.getPath(), true);
                T value = upgradedBinder.bind(upgradedBinder.parse(upgraded), document.getType());
                return new Loaded<>(value, upgradedBinder.getProblems(), upgraded);
            }
        }
        T value = binder.bind(root, document.getType());
        return new Loaded<>(value, binder.getProblems(), null);
    }

    /**
     * Content of an older layout run through the migration steps, null when no step applies
     */
    @Nullable
    private String upgrade(ConfigDocument<?> document, String text, int version) {
        TreeMap<Integer, UnaryOperator<Map<String, Object>>> steps = migrations.get(document.getKind());
        if (steps == null || steps.isEmpty()) {
            return null;
        }
        Map<String, Object> content;
        try {
            Object loaded = new Yaml().load(text);
            content = loaded instanceof Map ? castMap(loaded) : new LinkedHashMap<String, Object>();
        } catch (YAMLException e) {
            return null;
        }
        content = upgrade(document.getKind(), content, version, document.getVersion());
        return dump(content);
    }

    /**
     * Runs the migration steps of a kind on raw content
     */
    public Map<String, Object> upgrade(String kind, Map<String, Object> content, int fromVersion, int toVersion) {
        Map<String, Object> upgraded = new LinkedHashMap<>(content);
        TreeMap<Integer, UnaryOperator<Map<String, Object>>> steps = migrations.get(kind);
        for (int version = fromVersion; version < toVersion; version++) {
            UnaryOperator<Map<String, Object>> step = steps != null ? steps.get(version) : null;
            if (step != null) {
                upgraded = step.apply(upgraded);
            }
        }
        upgraded.remove(ConfigKeys.VERSION_KEY);
        return upgraded;
    }

    /**
     * Content of a file in the data folder as written, empty when it is missing or unreadable
     */
    public Map<String, Object> content(String path) {
        try {
            Object loaded = new Yaml().load(read(new File(dataFolder, path)));
            return loaded instanceof Map ? castMap(loaded) : new LinkedHashMap<String, Object>();
        } catch (IOException | YAMLException e) {
            return new LinkedHashMap<>();
        }
    }

    public static String dump(Map<String, Object> content) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);
        return new Yaml(options).dump(content);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object map) {
        return (Map<String, Object>) map;
    }

    /**
     * Upgraded files lose their comments, the old file stays in {@value #HISTORY_DIR}
     */
    private void persistMigration(ConfigDocument<?> document, File file, String upgraded) throws IOException {
        backup(document, file);
        writeAtomically(file, migrated(document, upgraded));
    }

    /**
     * File text of migrated content, the commented template with the changed values when only values differ from it
     */
    public String migrated(ConfigDocument<?> document, String upgraded) throws IOException {
        Map<String, Object> bundled = bundled(document.getPath());
        Object content = new Yaml().load(upgraded);
        Map<String, Object> changes = new LinkedHashMap<>();
        if (!bundled.isEmpty() && content instanceof Map && scalarChanges("", bundled, castMap(content), changes)) {
            String patched = patch(template(document), document.getPath(), changes);
            if (patched != null) {
                return patched;
            }
        }
        if (document.getTemplate() == null) {
            YamlBinder binder = new YamlBinder(document.getPath(), false);
            return render(document, binder.bind(binder.parse(upgraded), document.getType()));
        }
        return "# yaml-language-server: $schema=" + schemaPath(document) + "\n"
                + ConfigKeys.VERSION_KEY + ": " + document.getVersion() + "\n" + upgraded;
    }

    /**
     * Keys missing from the content keep the bundled value
     *
     * @return false when the two differ in more than scalar values
     */
    private static boolean scalarChanges(String prefix, Map<String, Object> bundled, Map<String, Object> content, Map<String, Object> changes) {
        if (!bundled.keySet().containsAll(content.keySet())) {
            return false;
        }
        for (Map.Entry<String, Object> entry : content.entrySet()) {
            Object original = bundled.get(entry.getKey());
            Object value = entry.getValue();
            String path = prefix + entry.getKey();
            if (original instanceof Map && value instanceof Map) {
                if (!scalarChanges(path + ".", castMap(original), castMap(value), changes)) {
                    return false;
                }
            } else if (!Objects.equals(original, value)) {
                if (value instanceof Map || value instanceof List || original instanceof Map || original instanceof List) {
                    return false;
                }
                changes.put(path, value);
            }
        }
        return true;
    }

    @Nullable
    private static String patch(String text, String file, Map<String, Object> changes) {
        for (Map.Entry<String, Object> change : changes.entrySet()) {
            Node node = YamlBinder.find(new YamlBinder(file, true).parse(text), change.getKey());
            if (!(node instanceof ScalarNode)) {
                return null;
            }
            text = text.substring(0, offset(text, node.getStartMark())) + YamlWriter.scalar(change.getValue())
                    + text.substring(offset(text, node.getEndMark()));
        }
        return text;
    }

    private static int version(@Nullable Node root, int fallback) {
        Node node = root instanceof MappingNode ? YamlBinder.find(root, ConfigKeys.VERSION_KEY) : null;
        if (node instanceof ScalarNode) {
            try {
                return Integer.parseInt(((ScalarNode) node).getValue().trim());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private List<ConfigProblem> scan(Directory directory) {
        File folder = new File(dataFolder, directory.path);
        List<ConfigProblem> problems = new ArrayList<>();
        if (!folder.exists()) {
            for (String name : directory.bundled) {
                String text = localized(directory.path + "/" + name);
                try {
                    if (text != null) {
                        writeAtomically(new File(folder, name), text);
                    }
                } catch (IOException e) {
                    problems.add(new ConfigProblem(ConfigProblem.Severity.ERROR, directory.path + "/" + name, 0, 0, "",
                            "Cannot create the file: " + e.getMessage()));
                }
            }
        }
        for (String id : documentsIn(directory)) {
            documents.remove(id);
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml") && !name.startsWith("."));
        if (files == null) {
            return problems;
        }
        Arrays.sort(files);
        for (File file : files) {
            String name = file.getName().substring(0, file.getName().length() - ".yml".length());
            String id = directory.path + "/" + name;
            documents.put(id, new ConfigDocument<>(id, directory.path + "/" + file.getName(), directory.type,
                    directory.applyMode, directory.version, directory.kind, null));
        }
        return problems;
    }

    private List<String> documentsIn(Directory directory) {
        List<String> ids = new ArrayList<>();
        for (ConfigDocument<?> document : documents.values()) {
            if (directory == directoryOf(document)) {
                ids.add(document.getId());
            }
        }
        return ids;
    }

    @Nullable
    private Directory directoryOf(ConfigDocument<?> document) {
        int slash = document.getId().lastIndexOf('/');
        return slash < 0 ? null : directories.get(document.getId().substring(0, slash));
    }

    private String idOf(Class<?> type) {
        for (ConfigDocument<?> document : documents.values()) {
            if (document.getType() == type) {
                return document.getId();
            }
        }
        throw new IllegalArgumentException(type.getSimpleName() + " is not a registered config document");
    }

    /**
     * Content of a bundled template without its version, for migrations that fill in a new layout
     */
    public Map<String, Object> bundled(String path) {
        String text = localized(path);
        Object loaded = text == null ? null : new Yaml().load(text);
        Map<String, Object> content = loaded instanceof Map ? castMap(loaded) : new LinkedHashMap<String, Object>();
        content.remove(ConfigKeys.VERSION_KEY);
        return content;
    }

    /**
     * Bundled template bound to its type with {@code &} codes kept, the class defaults when there is none
     */
    public <T> T bundled(String path, Class<T> type) {
        String text = localized(path);
        if (text == null) {
            return type.cast(ConfigKeys.newInstance(type));
        }
        YamlBinder binder = new YamlBinder(path, false);
        return binder.bind(binder.parse(text), type);
    }

    @Nullable
    private String localized(String path) {
        String language = this.language.get();
        String text = language == null ? null : resource(TEMPLATE_ROOT + language + "/" + path);
        return text != null ? text : resource(TEMPLATE_ROOT + path);
    }

    @Nullable
    private static String resource(String path) {
        try (InputStream in = ConfigService.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private void backup(ConfigDocument<?> document, File file) throws IOException {
        if (file.exists()) {
            backup(dataFolder, historyPrefix(document), Files.readAllBytes(file.toPath()));
        }
    }

    /**
     * Prefix of the backups of a document in {@value #HISTORY_DIR}, each file name continues with a timestamp
     */
    public static String historyPrefix(ConfigDocument<?> document) {
        return document.getPath().replace('/', '_') + ".";
    }

    /**
     * Stores content in {@value #HISTORY_DIR} and keeps the newest {@value #HISTORY_LIMIT} copies with the same prefix
     */
    public static void backup(File dataFolder, String prefix, byte[] content) throws IOException {
        File history = new File(dataFolder, HISTORY_DIR);
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
        Files.createDirectories(history.toPath());
        Files.write(new File(history, prefix + stamp).toPath(), content);

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
        return up + SCHEMA_DIR + "/" + document.getKind().replace('/', '-') + ".json";
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
        /** Content after the migration steps, null when the file already had the current layout */
        @Nullable
        final String upgraded;

        Loaded(T value, List<ConfigProblem> problems, @Nullable String upgraded) {
            this.value = value;
            this.problems = problems;
            this.upgraded = upgraded;
        }
    }

    private static final class Directory {
        final String path;
        final Class<?> type;
        final ApplyMode applyMode;
        final int version;
        final String kind;
        final List<String> bundled;

        Directory(String path, Class<?> type, ApplyMode applyMode, int version, String kind, List<String> bundled) {
            this.path = path;
            this.type = type;
            this.applyMode = applyMode;
            this.version = version;
            this.kind = kind;
            this.bundled = bundled;
        }
    }
}
