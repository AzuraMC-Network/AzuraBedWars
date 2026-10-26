package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.api.config.Comment;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Text of one language in lang/, the defaults are en_US
 *
 * @author an5w1r@163.com
 */
@Data
@Comment({"Text shown to players and admins", "Words in braces such as {player} are filled in by the plugin"})
public class LangConfig {

    private Command command = new Command();
    private Admin admin = new Admin();
    private Maps map = new Maps();
    private MapCheck mapCheck = new MapCheck();
    private Setup setup = new Setup();
    private Game game = new Game();

    @Comment("Team names by color")
    private LinkedHashMap<String, String> teams = new LinkedHashMap<>();

    private Death death = new Death();
    private TeamSelection teamSelection = new TeamSelection();
    private Shop shop = new Shop();
    private Items items = new Items();
    private Spectator spectator = new Spectator();
    private Tab tab = new Tab();

    public LangConfig() {
        teams.put("RED", "&cRed");
        teams.put("BLUE", "&9Blue");
        teams.put("GREEN", "&aGreen");
        teams.put("YELLOW", "&eYellow");
        teams.put("AQUA", "&bAqua");
        teams.put("WHITE", "&fWhite");
        teams.put("PINK", "&dPink");
        teams.put("GRAY", "&8Gray");
        teams.put("ORANGE", "&6Orange");
        teams.put("MAGENTA", "&5Magenta");
        teams.put("LIGHT_BLUE", "&bLight Blue");
        teams.put("LIME", "&aLime");
        teams.put("PURPLE", "&5Purple");
        teams.put("CYAN", "&3Cyan");
        teams.put("BLACK", "&0Black");
        teams.put("BROWN", "&4Brown");
    }

    @Data
    public static class Command {
        private String missingArgument = "&cMissing argument";
        private String playerOffline = "&cThat player is not online";
        private String noPermission = "&cYou do not have permission";
        private String playersOnly = "&cOnly players can use this command!";
        private String onlyInGame = "&cYou can only use this command during a game!";
        private String shoutUsage = "&cUsage: /shout <message>";
        private String shoutCooldown = "&cYou must wait before shouting again!";
        private String gameStarted = "&cThe game has already started or is ending!";

        @Comment("{min}")
        private String notEnoughPlayers = "&cNot enough players! At least {min} players in two teams are needed to &nstart the game&c.";

        private String damageDisplayUsage = "&cUsage: /displaydamage <arrow|attack>";
        private String damageDisplayDisabled = "&cThis feature is not enabled!";
        private String arrowDamageOn = "&aArrow damage display enabled!";
        private String arrowDamageOff = "&cArrow damage display disabled!";
        private String attackDamageOn = "&aAttack damage display enabled!";
        private String attackDamageOff = "&cAttack damage display disabled!";
    }

    @Data
    public static class Admin {
        @Comment("{version}")
        private List<String> help = new ArrayList<>(Arrays.asList(
                "&b&lAzuraBedWars &8- &7v{version} &8- &bBedWars - Commands",
                "",
                "&7 • &f/bw reload [config] &7Check and reload config files",
                "&7 • &f/bw config check &7Check every config file for errors",
                "&7 • &f/bw config schema &7Write the JSON Schema used by editors",
                "&7 • &f/map &7Map commands &3&o[editor mode only]",
                "&7 • &f/setup &7Setup tool commands &3&o[editor mode only]",
                "&7 • &f/bw editorMode true/false &7Toggle editor mode, applies after a restart",
                "&7 • &f/bw debugMode true/false &7Toggle debug logging",
                "&7 • &f/bw devtools on/off/status/restart/rotate &7AI config assistant over MCP for dev servers",
                "&7 • &f/bw toWorld <worldName> &7Go to a world",
                "&7 • &f/bw loadWorld <worldName> &7Load a world",
                "&7 • &f/bw start &7Start the game now &3&o[outside editor mode]"));

        @Comment("{config} {available}")
        private String unknownConfig = "&cNo config named {config}, available: {available}";
        private String configClean = "&aNo problems found in the config files";

        @Comment("{count}")
        private String configProblems = "&eThe config files have {count} problems:";

        @Comment("{count}")
        private String moreProblems = "&7 ...and {count} more, see the console for the full list";

        @Comment("{folder}")
        private String schemaWritten = "&aWrote the JSON Schema files in {folder}/";

        @Comment("{config}")
        private String applied = "&f{config} &aapplied";
        private String appliedNextGame = "&f{config} &esaved, applies when the next game starts";

        @Comment("{config} {paths}")
        private String appliedRestart = "&f{config} &eapplied, these apply after a restart: &f{paths}";
        private String rejected = "&f{config} &chas errors, not applied, the current config stays";

        @Comment("{world}")
        private String worldNotLoaded = "&cWorld {world} is not loaded, use /bw loadWorld {world} first";
        private String worldMissing = "&cThe server folder has no world save named {world}";
        private String worldLoadFailed = "&cCould not load world {world}";
        private String worldLoaded = "&aWorld {world} loaded, /bw toWorld {world} to go there";

        private String devtoolsOff = "&aMCP stopped, the port is free";
        private String devtoolsOffUnsaved = "&eMCP stopped, but devtools.yml could not be saved, it starts again after a restart";
        private String devtoolsNotRunning = "&eMCP is off, /bw devtools on to start it";
        private String devtoolsRotated = "&aToken replaced, the old one stops working now, add the server to the AI client again with the new command";

        @Comment("{error}")
        private String devtoolsStartFailed = "&cMCP could not start: {error}";
        private String devtoolsRotateRestartFailed = "&cToken replaced, but MCP could not restart: {error}";
        private String devtoolsTokenWriteFailed = "&cCould not write the new token: {error}";

        @Comment("{endpoint} {command}")
        private List<String> devtoolsRunning = new ArrayList<>(Arrays.asList(
                "&aMCP running &f{endpoint}",
                "&7Run this on the computer with the AI client:",
                "&f{command}",
                "&7The token is in plugins/AzuraBedWars/devtools/token, the audit log in devtools/audit.log"));
    }

    @Data
    public static class Maps {
        @Comment("{version} {range}")
        private List<String> help = new ArrayList<>(Arrays.asList(
                "&b&lAzuraBedWars &8- &7v{version} &8- &bBedWars - Map Setup",
                "",
                "&7 • &f/map create <map> &7Create a map",
                "&7 • &f/map setUrl <map> <path> &7Set the world folder of the map",
                "&7 •  &9&l▸ &fVariables {$serverDir} server folder {$baseDir} plugins {$pluginDir} plugins/AzuraBedWars",
                "&7 • &f/map setWaiting <map> &7Set the waiting lobby where you stand",
                "&7 • &f/map setRespawn <map> &7Set the spectator spawn where you stand",
                "&7 • &f/map setPos1 <map> &7 /map setPos2 <map> &7Set the map border, the higher corner sets the build limit",
                "&7 • &f/map addBase <map> &7Add a team base where you stand, nearby wool sets its color",
                "&7 • &f/map addDrop <map> <BASE/DIAMOND/EMERALD> &7Add a generator",
                "&7 • &f/map addShop <map> <ITEM/UPGRADE> &7Add a shop",
                "&7 • &f/map remove <map> &7Remove the nearest base, generator or shop within {range} blocks",
                "&7 • &f/map setAuthor <map> <author> &7Set the author",
                "&7 • &f/map setTeamPlayers <map> <players> &7Set the players per team",
                "&7 • &f/map setMinPlayers <map> <players> &7Set the players needed to start",
                "&7 • &f/map check <map> &7List what the map is missing",
                "&7 • &f/map info <map> &7Show the map",
                "&7 • &f/map list [JSON/MYSQL] &7List maps",
                "&7 • &f/map load <map> &7Read the map from storage again",
                "&7 • &f/map delete <map> confirm &7Delete the map data, the world files stay",
                "&7 • &f/map migrate <from> <to> [map] &7Move maps between storages",
                "&7 • &f/map align &7Snap your position and view to whole numbers"));

        private String invalidName = "&cMap names may only use letters, digits, underscores and hyphens, up to 30 characters";

        @Comment("{map}")
        private String exists = "&cMap {map} already exists";
        private String saveFailed = "&cCould not save the map, see the console";
        private String created = "&aMap {map} created, next run /map setUrl {map} <world folder>";
        private String notFound = "&cMap {map} not found";
        private String notFoundCreate = "&cMap {map} not found, create it with /map create {map}";
        private String deleteConfirm = "&eThis deletes the config of map {map}, the world folder stays. Run /map delete {map} confirm to go ahead";
        private String deleted = "&aMap {map} deleted";
        private String deleteFailed = "&cCould not delete the map, see the console";

        @Comment("{folder}")
        private String folderSet = "&aWorld folder set to {folder}";

        @Comment("{problem}")
        private String folderProblem = " &ebut {problem}";
        private String waitingSet = "&aWaiting lobby set";
        private String respawnSet = "&aSpectator spawn set";
        private String pos1Set = "&aMap border 1 set";
        private String pos2Set = "&aMap border 2 set";

        @Comment("{number} {color}")
        private String baseAdded = "&aBase #{number} added, nearby wool {color}";

        @Comment("{number} {radius}")
        private String baseAddedNoWool = "&aBase #{number} added &eno wool within {radius} blocks, a color is assigned when the game starts";

        @Comment("{type} {count}")
        private String dropAdded = "&a{type} generator added, {count} in total";
        private String shopAdded = "&a{type} shop added, {count} in total";

        @Comment("{range}")
        private String nothingToRemove = "&cNo base, generator or shop within {range} blocks";

        @Comment("{what} {location}")
        private String removed = "&aRemoved {what} {location}";

        @Comment("{type}")
        private String drop = "{type} generator";
        private String shop = "{type} shop";
        private String base = "base";
        private String authorTooLong = "&cAuthor names are up to 24 characters";

        @Comment("{author}")
        private String authorSet = "&aAuthor set to {author}";
        private String teamPlayersRange = "&cPlayers per team must be between 1 and 8";

        @Comment("{players} {capacity}")
        private String teamPlayersSet = "&aPlayers per team set to {players}, the map holds {capacity}";
        private String minPlayersRange = "&cAt least 2 players are needed to start";
        private String minPlayersSet = "&aPlayers needed to start set to {players}";

        @Comment("{map}")
        private String ready = "&aMap {map} is ready to play";
        private String notReady = "&cMap {map} cannot be played yet";

        @Comment({"{name} {author} {min} {team} {capacity} {height} {pos1} {pos2} {waiting} {respawn} {bases}",
                "{base-drops} {diamond-drops} {emerald-drops} {item-shops} {upgrade-shops} {folder}"})
        private List<String> info = new ArrayList<>(Arrays.asList(
                "&bMap info",
                "",
                " &9&l▸ &fName: &b{name}",
                " &9&l▸ &fAuthor: &b{author}",
                " &9&l▸ &fPlayers to start: &b{min}",
                " &9&l▸ &fPlayers per team: &b{team} &7holds {capacity}",
                " &9&l▸ &fBuild limit: &b{height}",
                "",
                " &9&l▸ &fPos1: {pos1}",
                " &9&l▸ &fPos2: {pos2}",
                " &9&l▸ &fWaiting lobby: {waiting}",
                " &9&l▸ &fSpectator spawn: {respawn}",
                "",
                " &9&l▸ &fBases:",
                "{bases}",
                "",
                " &9&l▸ &fBase generators: &b{base-drops}",
                " &9&l▸ &fDiamond generators: &b{diamond-drops}",
                " &9&l▸ &fEmerald generators: &b{emerald-drops}",
                " &9&l▸ &fItem shops: &b{item-shops}",
                " &9&l▸ &fUpgrade shops: &b{upgrade-shops}",
                "",
                " &9&l▸ &fWorld folder: &b{folder}"));

        @Comment("{number} {location}")
        private String baseLine = "    &7- Base {number}: {location}";
        private String none = "&cnone";
        private String notSet = "&cnot set";
        private String unknownWorld = "unknown world";

        @Comment("{storage} {count}")
        private String listHeader = "&b{count} maps in storage {storage}";
        private String listEmpty = " &eNo maps found";

        @Comment("{map}")
        private String reloaded = "&aRead map {map} from storage again";
        private String notInStorage = "&cMap {map} is not in storage";
        private String sameStorage = "&cThe source and target storage must differ";

        @Comment("{storage} {map}")
        private String notInSource = "&cMap {map} is not in {storage}";

        @Comment("{maps} {from} {to}")
        private String migrated = "&aMoved {maps} from {from} to {to}";
        private String allMaps = "all maps";

        @Comment("{map}")
        private String oneMap = "map {map}";
        private String migrateFailed = "&cSome maps could not be moved, see the console";

        @Comment("{x} {y} {z} {yaw} {pitch}")
        private String aligned = "&aAligned to {x}, {y}, {z} &7Yaw {yaw} Pitch {pitch}";

        @Comment("{value} {available}")
        private String invalidType = "&cInvalid type {value}, available: {available}";
        private String mysqlUnavailable = "&cMYSQL map storage needs database-type MySQL in database.yml";
    }

    @Data
    public static class MapCheck {
        @Comment("{world}")
        private String worldNotLoaded = "World {world} is not loaded, bed and wool checks skipped";
        private String noWaiting = "No waiting lobby, /map setWaiting";
        private String noRespawn = "No spectator spawn, /map setRespawn";
        private String noRegion = "No map border, /map setPos1 and /map setPos2";

        @Comment("{count}")
        private String tooFewBases = "At least 2 bases are needed, there are {count}";

        @Comment("{max} {count}")
        private String tooManyBases = "At most {max} bases, there are {count}";
        private String noBaseDrop = "No base generator, /map addDrop <map> BASE";
        private String noDiamondDrop = "No diamond generator";
        private String noEmeraldDrop = "No emerald generator";
        private String noItemShop = "No item shop";
        private String noUpgradeShop = "No team upgrade shop";

        @Comment("{map}")
        private String sameAsMainWorld = "The map cannot be named {map} like the main world";
        private String noAuthor = "No author, /map setAuthor";
        private String regionWorlds = "Border 1 and border 2 are in different worlds";
        private String respawn = "Spectator spawn";

        @Comment("{number}")
        private String base = "Base #{number}";

        @Comment("{type}")
        private String drop = "{type} generator";
        private String shop = "{type} shop";

        @Comment("{name} {world} {region-world}")
        private String otherWorld = "{name} is in world {world}, not in the border world {region-world}";

        @Comment("{name}")
        private String outsideRegion = "{name} is outside the map border, players cannot place blocks there";

        @Comment("{min} {capacity}")
        private String minAboveCapacity = "Players to start {min} is more than the map holds {capacity}, the game can never start";
        private String noFolder = "No world folder, /map setUrl";

        @Comment("{file}")
        private String notAWorld = "the world folder is not a world save, {file} is missing";

        @Comment("{folder}")
        private String folderIsGameWorld = "the world folder is the game world {folder}, which is cleared when a game starts. Move the map elsewhere";

        @Comment("{folder} {error}")
        private String folderUnreadable = "cannot resolve the world folder {folder} {error}";

        @Comment("{name} {radius}")
        private String noWool = "{name} has no wool within {radius} blocks, a color is assigned";

        @Comment("{name} {color}")
        private String sameColor = "{name} is {color} like another base, another color is assigned";

        @Comment("{name} {radius}")
        private String noBed = "{name} has no bed within {radius} blocks";
        private String sharedBed = "{name} found the same bed as another base";
        private String severalBeds = "{name} has more than one bed nearby, the one closest to the spawn is used";
        private String sharedDrop = "{name} shares a base generator with another base";

        @Comment("{name} {color} {beds} {distance}")
        private String baseLine = "{name} wool {color} bed blocks {beds} generator distance {distance}";
        private String none = "none";
    }

    @Data
    public static class Setup {
        @Comment("{version}")
        private List<String> help = new ArrayList<>(Arrays.asList(
                "&b&lAzuraBedWars &8- &7v{version} &8- &bBedWars - Setup Tools",
                "",
                "&7 • &f/setup start <map> &7Get the setup tools, right click to set where you stand",
                "&7 • &f/setup stop &7Put the setup tools away"));

        @Comment("{map}")
        private String started = "&aSetting up map {map}, right click a tool to set where you stand, every change is saved";
        private String stopped = "&aStopped setting up the map";
        private String notActive = "&cYou are not setting up a map";

        private Tool waiting = new Tool("&eSet waiting lobby", "Sets the waiting lobby where you stand");
        private Tool respawn = new Tool("&eSet spectator spawn", "Sets the spectator spawn where you stand");
        private Tool pos1 = new Tool("&cSet border 1", "Sets one corner of the map border where you stand");
        private Tool pos2 = new Tool("&cSet border 2", "Sets the other corner of the map border where you stand");
        private Tool base = new Tool("&dAdd team base", "Stand on the spawn, nearby wool sets the team color");
        private Tool baseDrop = new Tool("&7Add base generator", "Adds a base generator where you stand");
        private Tool diamondDrop = new Tool("&bAdd diamond generator", "Adds a diamond generator where you stand");
        private Tool emeraldDrop = new Tool("&aAdd emerald generator", "Adds an emerald generator where you stand");
        private Tool itemShop = new Tool("&6Add item shop", "Adds an item shop where you stand");
        private Tool upgradeShop = new Tool("&dAdd team upgrade shop", "Adds a team upgrade shop where you stand");
        private Tool remove = new Tool("&cRemove nearest point", "Removes the nearest base, generator or shop");
        private Tool check = new Tool("&aCheck map", "Lists what the map is missing");
    }

    @Data
    public static class Tool {
        private String name = "";
        private String lore = "";

        public Tool() {
        }

        public Tool(String name, String lore) {
            this.name = name;
            this.lore = lore;
        }
    }

    @Data
    public static class Game {
        @Comment("{player}")
        private String reconnected = "&7{player}&a reconnected";
        private String left = "&7{player}&e left the game";
        private String full = "Game is full";
        private String alreadyStarted = "The game has already started";
        private String normalKillReward = "&6+1 coin";
        private String finalKillReward = "&6+1 coin (Final Kill)";

        @Comment("{seconds}")
        private String respawnCountdownTitle = "&e&l{seconds}";
        private String respawnCountdownSubtitle = "&7You died and will respawn soon";
        private String respawnedTitle = "&aRespawned!";
        private String respawnedSubtitle = "&7Your bed is still standing so you respawned";
        private String eliminatedTitle = "&cYou died!";
        private String eliminatedSubtitle = "&7Your bed is gone";
        private String teamEliminatedBar = "&7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃";

        @Comment("{team} {breaker}")
        private String teamEliminated = "{team} &chas been eliminated! &eBed broken by: {breaker}";
        private String playAgain = "&cYou died! Want to play again? ";
        private String playAgainButton = "&b&lClick here!";
        private String ownBed = "&cYou cannot break your own bed";
        private String bedWithoutTeam = "&cThis bed has no team";

        @Comment("{team} {breaker}")
        private String bedBroken = "&f&lBED DESTRUCTION >>> {team}'s bed &7was destroyed by {breaker}";
        private String bedBrokenTitle = "&c&lBED DESTROYED";
        private String bedBrokenSubtitle = "&cYou will no longer respawn";

        @Comment("{coins}")
        private String bedReward = "&6+{coins} coins (Bed Destruction)";
        private String bedRewardActionBar = "&6+{coins} coins";

        @Comment("{player}")
        private String bedInDanger = "{player} says: &cWatch out, our bed is in danger!";
        private String cannotPlace = "&cYou cannot place blocks here!";
        private String cannotPlaceWater = "&cYou cannot place water here!";
        private String teamChestOnly = "&cOnly this team can open this chest";
        private String teamChestUseOnly = "&cOnly this team can use this chest";
        private String fastPutLastItem = "&cYou cannot put your only one of this item in the chest!";

        @Comment("{count}")
        private String fastPutMoved = "&aMoved {count} items!";

        @Comment("{moved} {total}")
        private String fastPutPartial = "&eSome items did not fit! Moved {moved}/{total} items, the chest is full.";
        private String fastPutFailed = "&cCould not add the items! The chest is full.";

        @Comment("{damage} {health}")
        private String damageDisplay = "&bDamage &f- &e{damage}  &bHealth &f- &e{health}";

        @Comment("{player} {health}")
        private String arrowHit = "&e&l{player} &e&lhas &c&l{health} &e&lhealth left";
        private String noTarget = "&cNo target";

        @Comment("{player} {distance}")
        private String trackerTarget = "&fPlayer {player} &fis {distance}m away";

        @Comment("{team} {distance}")
        private String baseTracker = "&fTeam: {team}&f Tracking: {distance}m";

        @Comment("{old} {new}")
        private String levelUp = "&6Congratulations! You went from level {old} to level {new}!";

        @Comment("{levels} {old} {new}")
        private String levelsUp = "&6Congratulations! You gained {levels} levels, from level {old} to level {new}!";

        @Comment("{levels}")
        private String xpGained = "&e+{levels} levels";
        private String xpPrice = "{levels} levels";
        private String noWinner = "None";
        private String startStage = "Game start";

        @Comment("{health} {team} {seconds}")
        private String golemName = "&c{health}❤ {team} Guard &e{seconds}s";
        private String silverfishName = "{team} Silverfish &e{seconds}s";
    }

    @Data
    public static class Death {
        @Comment("Added after the message of a final kill")
        private String finalKill = "&b&lFINAL KILL!";

        @Comment("{player} {killer} {color} {final}")
        private String quit = "&b&l» {player} {color}disconnected {final}";
        private String quitInCombat = "&b&l» {player} {color}disconnected while fleeing {killer} {color}{final}";

        @Comment({"Deaths without a killer by damage cause, VOID is used for the others", "{player} {color} {final}"})
        private LinkedHashMap<String, List<String>> self = new LinkedHashMap<>();

        @Comment({"Deaths with a killer by damage cause, VOID is used for the others", "{player} {killer} {color} {final}"})
        private LinkedHashMap<String, List<String>> kill = new LinkedHashMap<>();

        public Death() {
            self.put("VOID", lines("{player} {color}fell into the void", "{player} {color}fell out of the world"));
            self.put("ENTITY_ATTACK", lines("{player} {color}was slain by a mob"));
            self.put("FALL", lines("{player} {color}hit the ground too hard", "{player} {color}could not escape gravity"));
            self.put("FIRE", lines("{player} {color}went up in flames", "{player} {color}was burned to a crisp"));
            self.put("FIRE_TICK", lines("{player} {color}went up in flames", "{player} {color}was burned to a crisp"));
            self.put("LAVA", lines("{player} {color}tried to swim in lava"));
            self.put("PROJECTILE", lines("{player} {color}was shot"));
            self.put("ENTITY_EXPLOSION", lines("{player} {color}blew up"));
            self.put("BLOCK_EXPLOSION", lines("{player} {color}blew up"));

            kill.put("VOID", lines("{player} {color}was knocked into the void by {killer}{color}",
                    "{player} {color}fell into the void while fleeing {killer}{color}"));
            kill.put("ENTITY_ATTACK", lines("{player} {color}was killed by {killer}{color}",
                    "{player} {color}could not escape {killer}{color}"));
            kill.put("FALL", lines("{player} {color}hit the ground too hard while fleeing {killer}{color}"));
            kill.put("FIRE", lines("{player} {color}was burned to a crisp while fighting {killer}{color}"));
            kill.put("FIRE_TICK", lines("{player} {color}was burned to a crisp while fighting {killer}{color}"));
            kill.put("LAVA", lines("{player} {color}tried to swim in lava to escape {killer}{color}"));
            kill.put("PROJECTILE", lines("{player} {color}was shot by {killer}{color}"));
            kill.put("ENTITY_EXPLOSION", lines("{player} {color}was blown up by {killer}{color}"));
            kill.put("BLOCK_EXPLOSION", lines("{player} {color}was blown up by {killer}{color}"));
        }

        private static List<String> lines(String... lines) {
            List<String> list = new ArrayList<>();
            for (String line : lines) {
                list.add("&b&l» " + line + " {final}");
            }
            return list;
        }
    }

    @Data
    public static class TeamSelection {
        private String title = "&8Select a team";

        @Comment("{players} {max}")
        private String players = "&7Players: &f{players}&7/&f{max}";
        private String members = "&7Members:";

        @Comment("{player}")
        private String you = "&a  ▸ {player} &7(you)";
        private String member = "&7  ▸ {player}";
        private String selected = "&a&l✓ Selected";
        private String full = "&c&l✗ Team full";
        private String clickToSelect = "&7Click to join this team";
        private String alreadySelected = "&cYou are already in this team!";
        private String teamFull = "&cThis team is full!";
        private String unbalanced = "&cPlease pick a team with fewer players to keep the game fair!";

        @Comment("{team}")
        private String joined = "&aYou joined {team}";
        private String joinFailed = "&cCould not join the team, please try again!";
    }

    @Data
    public static class Shop {
        private String itemShopName = "&b&lItem Shop";
        private String upgradeShopName = "&e&lTeam Upgrades";
        private String notEnoughResources = "&cYou do not have enough resources!";
        private String noPermission = "&cYou do not have permission to buy this item!";
        private String inventoryFull = "&cYour inventory is full!";
        private String emptySlotName = "&cEmpty slot";
        private List<String> emptySlotLore = new ArrayList<>(Arrays.asList(
                "&7This is a Quick Buy slot! &bShift+Left Click", "&7any item to put it here"));
        private String emptySlotClicked = "&cThis slot is empty! Shift+Left Click an item to add it here";
        private String quickBuyEditTitle = "&8Add item to Quick Buy";
        private String quickBuyAdding = "&eAdding the item to Quick Buy...";
        private String quickBuyReplace = "&eClick to replace with this item";

        @Comment("{item}")
        private String quickBuyDefaultName = "&a{item} &7(default)";
        private List<String> quickBuyDefaultLore = new ArrayList<>(Arrays.asList(
                "&7This is a default Quick Buy item", "&eClick to replace it with the selected item"));
        private String quickBuySet = "&eClick to put the item here";
    }

    @Data
    public static class Items {
        private String invisibilityEnded = "&cYour invisibility wore off";
        private String magicMilkEnded = "&cYour Magic Milk wore off";
        private String magicMilkDrunk = "&aYou drank Magic Milk, traps cannot affect you for 30 seconds";
        private String rescuePlatformNotInAir = "&cYou are not in the air!";

        @Comment("{seconds}")
        private String rescuePlatformCooldown = "&cYou must wait &e{seconds}s&c before using another Rescue Platform!";
        private String fireballCooldown = "&cYour fireball is on cooldown";
        private String golemNoSpace = "&cAn Iron Golem cannot spawn here, pick a more open spot on the ground.";

        @Comment("{seconds}")
        private String warpPowderStart = "&aYou will be teleported in &c{seconds}&a seconds, do not move!";
        private String warpPowderCancelled = "&cYour teleport was cancelled!";
        private String warpPowderMoved = "&cYou moved, the teleport was cancelled!";
        private String warpPowderActive = "&cYou are already teleporting!";
        private String warpPowderInAir = "&cYou cannot use Warp Powder in the air!";
        private String warpPowderNoSpawn = "&cYour team spawn could not be found!";
        private String recoveryBedUsed = "&aYou used the Recovery Bed!";

        @Comment("{team}")
        private String recoveryBedBroadcast = "{team} &cused the Recovery Bed!";
        private String recoveryBedTooLate = "&cMore than 10 minutes have passed since the game started.";
        private String recoveryBedUsedBefore = "&cYour team already used the Recovery Bed";
        private String recoveryBedStanding = "&cYour bed is still standing";
        private String recoveryBedNoBed = "&cThe map has no bed for your team";
        private String recoveryBedTooFar = "&cUse it near your spawn!";
    }

    @Data
    public static class Spectator {
        private String compassItem = "&a&lTeleporter &7(Right Click)";
        private String settingsItem = "&c&lSpectator Settings &7(Right Click)";
        private String playAgainItem = "&b&lPlay Again &7(Right Click)";
        private String leaveItem = "&c&lReturn to Lobby &7(Right Click)";
        private String compassTitle = "&8Teleport to a player";

        @Comment("{value}")
        private String health = "&fHealth: &8{value}";
        private String food = "&fHunger: &8{value}";
        private String level = "&fLevel: &8{value}";
        private String distance = "&fDistance: &8{value}";
        private String settingsTitle = "&8Spectator Settings";
        private String noSpeed = "&aNo Speed";

        @Comment("{level}")
        private String speed = "&aSpeed {level}";
        private String speedRemoved = "&cYou no longer have any Speed effects!";
        private String speedAdded = "&aYou now have Speed {level}!";
        private String autoTpOn = "&aStart Auto Teleport";
        private String autoTpOff = "&cStop Auto Teleport";
        private String autoTpOnLore = "&7Click to enable auto teleport";
        private String autoTpOffLore = "&7Click to disable auto teleport";
        private String autoTpEnabled = "&aAuto teleport enabled!";
        private String autoTpDisabled = "&cYou will no longer be teleported to your target!";
        private String nightVisionOn = "&aEnable Night Vision";
        private String nightVisionOff = "&cDisable Night Vision";
        private String nightVisionOnLore = "&7Click to enable night vision";
        private String nightVisionOffLore = "&7Click to disable night vision";
        private String nightVisionEnabled = "&aYou now have night vision!";
        private String nightVisionDisabled = "&cYou no longer have night vision!";
        private String firstPersonOn = "&aEnable First Person";
        private String firstPersonOff = "&cDisable First Person";
        private List<String> firstPersonOnLore = new ArrayList<>(Arrays.asList(
                "&7Click to use first person", "&7when you pick a player with the compass!",
                "&7You can also right click a player", "&7to spectate in first person"));
        private String firstPersonOffLore = "&7Click to disable first person";
        private String firstPersonEnabled = "&aPicking a player with the compass now spectates them in first person!";
        private String firstPersonDisabled = "&cYou will use third person by default!";
        private String hideOthersOn = "&cHide Spectators";
        private String hideOthersOff = "&aShow Spectators";
        private String hideOthersOnLore = "&7Click to hide other spectators";
        private String hideOthersOffLore = "&7Click to show other spectators";
        private String hideOthersEnabled = "&cYou can no longer see other spectators!";
        private String hideOthersDisabled = "&aYou can now see other spectators!";
        private String flyOn = "&aEnable Always Flying";
        private String flyOff = "&cDisable Always Flying";
        private String flyOnLore = "&7Click to always fly";
        private String flyOffLore = "&7Click to stop always flying";
        private String flyEnabled = "&aYou can no longer stop flying!";
        private String flyDisabled = "&aYou can stop flying again!";
        private String targetLost = "&c&lTarget lost or in another world";
        private String leftSpectating = "&eStopped spectating";

        @Comment("{player}")
        private String firstPersonTitle = "&aSpectating &7{player}";
        private String firstPersonSubtitle = "&aLeft Click for the menu  &cSneak to exit";

        @Comment("{player} {health}")
        private String firstPersonActionBar = "&fTarget: &a&l{player}  &fHealth: &a&l{health} &c&l❤  &aLeft Click for the menu  &cSneak to exit";

        @Comment("{player} {health} {distance}")
        private String thirdPersonActionBar = "&fTarget: &a&l{player}  &fHealth: &a&l{health}  &fDistance: &a&l{distance}m";
    }

    @Data
    public static class Tab {
        private String unknown = "&7[Unknown] | ";
        private String spectator = "&7[Spectator] | ";
        private String unassigned = "&7[No team] | ";
    }
}
