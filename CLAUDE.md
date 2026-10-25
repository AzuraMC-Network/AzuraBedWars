# AzuraBedWars

BedWars plugin for Spigot and Paper, supporting Minecraft 1.8.8 through 26.x from a single jar.

# Project

| Module | Purpose |
| --- | --- |
| `api/` | Public API, events and the `NMSAccess` SPI |
| `bootstrap/` | `plugin.yml` main, loads core and libraries in an isolated class loader |
| `core/` | Game logic, commands, listeners, storage |
| `nms/v1_X_RY/` | `NMSAccess` for one CraftBukkit revision, shared by Spigot and Paper |
| `nms/v26_X/` | `NMSAccess` for 26.x servers, shared by Spigot and Paper |
| `plugin/` | Assembles the regular and offline plugin jars |
| `build-logic/` | Convention plugins and `RuntimeLibrariesTask` |

Dependency versions live in `gradle/libs.versions.toml`. Repositories are declared once in `settings.gradle.kts`.

# Runtime layout

Bukkit loads `bootstrap`, `api` and every `nms/` module. Everything else runs inside `IsolatedClassLoader`, which is child first:

- `core` is packed into one inner jar
- Third-party libraries from `core`'s runtime classpath are listed with their sha256 in `META-INF/azurabedwars/libraries.txt`
- The regular jar embeds only the inner jar, and downloads the rest into `plugins/AzuraBedWars/libraries/` from `libraries.repositories` in `config.yml`
- `-offline.jar` embeds every library
- On startup `Updater` downloads a newer GitHub release of the same kind into Bukkit's update folder, so it is installed on the next restart. Release assets are attached by the build workflow

Rules that follow from this:

- `bootstrap` has no dependencies besides the Bukkit API, compiles against the 1.8.8 API, and must stay Java
- `api` stays Java because other plugins compile against it
- `cc.azuramc.bedwars.AzuraBedWars` is not a `JavaPlugin`. Anything handed to Bukkit, such as the scheduler, events, metadata or plugin channels, takes `AzuraBedWars.getInstance().getPlugin()`
- APIs that look up the plugin from the calling class, such as `JavaPlugin.getProvidingPlugin`, cannot find it from inside the isolated loader
- An `implementation` dependency of `core` must be resolvable from Maven Central or JitPack, because the regular jar downloads it
- `bootstrap`, `api`, `core` and the `nms/` modules up to 1.16 compile with `--release 8`, because Minecraft 1.8 to 1.16 servers still run on Java 8. Records, switch expressions, `var`, `List.of` and other Java 9+ language features or APIs are not available there
- Every runtime library must run on Java 8. `checkBytecode` fails the build otherwise

`core` never depends on `nms`. At runtime `NMSProvider` looks up the Minecraft version in its module table and loads `cc.azuramc.bedwars.nms.<module>.NMS_<module>` by reflection, such as `v1_8_R3` or `v26_2`. The `nms/` modules are compiled against Spigot:

- Spigot up to 1.21.11 and Paper up to 1.20.4 run them as compiled
- Paper 1.20.5 to 1.21.11 runs Mojang mappings and remaps the Spigot names in the outer jar when loading the plugin, which is why `nms/` stays out of the inner jar
- 26.x servers are not obfuscated on either side, but Paper changes member access, such as `Mob.goalSelector`. 26.x modules use only members both sides share, and `compilePaperJava` checks them against a Paper server jar during `check`

To support a new Minecraft version:

1. Copy the closest `nms/` directory and rename both the directory and the Java package, `v1_X_RY` after the CraftBukkit revision or `v26_X` after the first 26.x release it serves
2. Point its `build.gradle.kts` at the matching `org.spigotmc:spigot` jar from the CodeMC NMS repository, set `options.release` to the Java version that Minecraft version requires, and for 26.x keep `compilePaperJava` on a matching `paper-server`
3. Fix the compile errors against the new server jars
4. Add the first Minecraft version it serves to `NMSProvider`, and move `UNTESTED` past the newest version it was run on

`settings.gradle.kts` and `plugin/` pick up new `nms/` directories automatically.

# Build

```
./gradlew build
```

`AzuraBedWars-<version>.jar` and `AzuraBedWars-<version>-offline.jar` are written to `plugin/build/libs/`. Builds use a JDK 25 toolchain, downloaded automatically when missing, because 26.x server jars are Java 25 bytecode. Output targets Java 8, except the `nms/` modules for Minecraft 1.17 and newer, which target the Java version their Minecraft version requires.

# Comments

Comments are written in English.

## Default to none

Constants, locals, fields, methods and types get no comment by default. Do not restate what the name already says.

No file or method header blocks. No "what this method does", design intent, performance notes, implementation steps or debugging history. "It used to do X which caused Y, so now Z" belongs in the commit message, not the source.

Target density: about one comment per 70 lines in ordinary logic files.

## Four exceptions

Only these are worth writing:

1. Cross-file or cross-language sync constraints. Changing A requires changing B or something silently breaks.
   `/** Must match the key in messages.yml */`
2. External contract facts. Values that come from a serialization format, an OS API, a database column or a third-party interface and cannot be found in this repository.
   `/** Mojang renamed this field in 1.21 */`
3. A name that does not match what the code actually does.
   `// event driven not polled`
4. Real traps, where the intuitive version would be a bug.
   `// must run on the main thread`

Separate legacy facts from history. Describing a real external artifact that still exists, such as an old config file or an old data directory, is exception 2 and stays. Explaining what was copied from an implementation that no longer exists is history and goes.

Data contract files such as DTOs, schemas and config objects are measured per field rather than per line. Commenting about a quarter of the fields is reasonable.

## Form

A comment that describes the item right after it uses Javadoc form `/** */`. Line comments are only for statements inside a method body.

Deleting a narrative doc block does not mean downgrading a Javadoc to a line comment. Either delete it entirely or keep it as Javadoc.

A comment is a signpost, not an explanation. Name the block, do not argue for it. Keep it under about eight words.

No trailing periods. No parenthetical asides, dashes or lead-in words. Reduce the reason to a noun phrase without mechanism, numbers or causal chains.

No section dividers. No bold, arrows or numbered lists.

## Placement

Only comment where the code would otherwise look redundant or arbitrary: a run of magic strings, a listener or branch that looks safe to delete, an unexplained layer of indirection such as a delayed task or an extra copy.

## File types

Build scripts and CI configuration mostly fall under exception 2 and are treated leniently.

Tests: assertions carry no long messages, the test name is the description. Diagnostic arguments that print actual values stay.

## Not comments

Never treat these as comments to delete or rewrite: suppression annotations and directives such as `@SuppressWarnings` or `//noinspection`, formatter directives, annotations, license headers.

Commented-out dead code is deleted as a whole block.

## Samples

These files already meet the target. Match their feel when writing new code:

- `core/src/main/java/cc/azuramc/bedwars/config/yaml/YamlBinder.java`
- `core/src/main/java/cc/azuramc/bedwars/database/repository/mongodb/MongoDatabaseVersionRepository.java`
- `build-logic/src/main/kotlin/azurabedwars.java-conventions.gradle.kts`

# Code

- Do not extract helpers for one-off operations or build abstractions for hypothetical future needs.
- Do not add defensive checks for states internal code cannot reach. Validate only at system boundaries: player input, config files, the database, the network.
- Do not leave probes, diagnostic logging or "fixes" made under assumptions that turned out wrong. Remove them once the issue is located.
- Confirm the root cause before fixing a bug and back it with evidence. Do not stack patches. If a patch does not fix it, go back and locate the cause again.
- New code reads like the code around it: naming, idioms and comment density match the file it lives in.
- Version-specific behavior goes behind `NMSAccess` or `VersionUtil` checks, never into scattered string comparisons of the server version.

# Debugging

- Start with the tool closest to the problem with the least noise: the server log, a Spark profile, a unit test that reproduces it. Packet captures and full traces come last.
- When someone else has to operate a tool, spell out the steps down to which command to run and which file to send back. Do not assume familiarity with the tool.

# Commits

Format: `type(scope): summary`, with no body and no trailers.

- type: feat / fix / perf / refactor / chore
- scope: the affected module or area such as build, bootstrap, core, nms, game, shop; omit when it spans many areas
- The summary states the result, not the mechanism
- Keep it short and plain. When a second clause is needed, separate it with a space rather than expanding into sentences
- One commit does one thing. Unrelated changes go into separate commits

Good:

```
fix(game): restore bed state after reconnect
perf(scoreboard): reuse board lines between ticks
feat(nms): support Paper 26.3
refactor(build): flatten modules into api core nms plugin loader
```

Bad:

```
fix(game): fixed the bug where the bed would not come back when a player reconnected because the snapshot was cleared, now we keep it.
fix(nms): add null check in spawnIronGolem and remove debug log
```

# Verification

Run `./gradlew build` before handing over. It compiles every module against its own server jar, so it catches most cross-version breakage.

If part of the change cannot be compiled or run locally, make only changes that can be proven statically and state clearly which parts were not compiled or not run. Report failures as they are, with the output. Never say "it should work".

# Communication

- Reply to people in the language they wrote in. Code, identifiers, log output and documentation stay in English.
- Lead with the conclusion, then only the necessary detail. Do not restate the request or repeat a summary at the end.
