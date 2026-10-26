# Writing extensions

Other plugins can add their own pieces to AzuraBedWars and let server owners place them through the config files:

| Kind | Interface | Written in | Example |
| --- | --- | --- | --- |
| Custom item | `CustomItem` | a shop item, `custom:` | `custom: grapple:hook` |
| Purchase action | `PurchaseAction` | a shop item, `action:` | `action: vip:give-kit` |
| Purchase condition | `PurchaseCondition` | a shop item, `requires.conditions` | `- type: vip:rank` |
| Upgrade effect | `UpgradeEffect` | `upgrades.yml`, an effect of an upgrade level | `type: magic:aura` |
| Trap effect | `TrapEffect` | `upgrades.yml`, an effect or alert of a trap | `type: magic:lightning` |
| Timeline action | `TimelineAction` | `timeline.yml`, an action of a stage | `type: events:meteor` |
| Placeholder | `Placeholder` | the scoreboard, the tab list, titles and action bars | `{stats:wins}` |

A reference is the name of your plugin in lower case, a colon and the id you registered. The settings of an
extension sit next to its reference under `settings`, and are checked with line numbers like every other config value.

The [grapple sample](../examples/grapple) is a complete plugin that sells a grappling hook.

## Setup

Compile against the API, either the `api` jar built by `./gradlew :api:jar` in this repository or the AzuraBedWars
plugin jar itself, which contains the API classes. Do not shade it.

```kotlin
dependencies {
    compileOnly(files("libs/api-1.0.0.jar"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
}
```

Depend on AzuraBedWars in `plugin.yml`, so your plugin is enabled after it:

```yaml
name: Grapple
main: com.example.grapple.GrapplePlugin
depend:
  - AzuraBedWars
```

Compile for the oldest Minecraft version your server list includes. AzuraBedWars runs from 1.8.8, where Java 8 is
still common.

## Registering

Register in `onEnable`. AzuraBedWars checks every reference on the first tick after all plugins are enabled, so the
order of your registrations does not matter.

```java
public final class GrapplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        AzuraBedWarsAPI.getRegistry().registerItem(this, "hook", GrappleSettings.class, new GrappleHook());
    }
}
```

Ids use `a-z`, `0-9`, `_` and `-`. Everything a plugin registered is removed when that plugin is disabled, and items
that referred to it leave the shop.

## Settings

A settings class is a plain class with a public no argument constructor. Fields are read by their name in kebab case,
so `cooldownMessage` is written `cooldown-message`. Values keep the defaults of the class when left out.

```java
public class GrappleSettings {

    @Comment("Farthest block the hook reaches")
    @Range(min = 5, max = 60)
    int range = 30;

    @Comment("Seconds between two pulls")
    @Range(min = 0)
    double cooldown = 3;

    String miss = "&cNothing to hook onto";
}
```

- `@Comment` shows up as a hint in editors that read the exported JSON Schema
- `@Range` is checked, a value outside it is reported and the default is used
- `&` color codes in strings are translated, `@Raw` keeps a string as written, for ids and commands
- Strings, numbers, booleans, enums, lists, maps and nested classes of the same kind are supported

Pass `NoSettings.class` when an extension reads nothing.

The same settings object may be passed to many calls, so treat it as read only.

## Using it in the config

A server owner sells the hook from any category in `shop/categories/`:

```yaml
items:
  grapple:
    name: "Grapple"
    item: FISHING_ROD
    price: 6 gold
    custom: grapple:hook
    keep-on-respawn: true
    settings:
      range: 40
      cooldown: 2
```

`item` is the stack the custom item starts from. An `action` item runs the purchase action instead of giving
anything, and `item` is only its icon. One item has either `custom` or `action`.

Conditions are listed under `requires`. A condition without settings can be written as a lone reference:

```yaml
    requires:
      conditions:
        - vip:rank
        - type: vip:level
          settings:
            min: 10
```

Upgrade and trap effects, and timeline actions, use their reference as `type`:

```yaml
# upgrades.yml
levels:
  - name: "Aura I"
    price: 4 diamond
    effects:
      - type: magic:aura
        settings:
          radius: 6

# timeline.yml
stages:
  - name: "Meteor shower"
    after: 600
    actions:
      - type: events:meteor
        settings:
          count: 20
```

## Tools for AI clients

Servers that turn on the development MCP in `devtools.yml` let AI clients such as Claude Code read and change the
config. A plugin can add its own tools there, so the AI can inspect or drive what the plugin added. They are listed as
`<plugin>_<id>`, with the plugin name in lower case.

```java
JsonObject schema = new JsonObject();
schema.addProperty("type", "object");
registry.registerDevTool(this, "pulls", "Seconds since each online player last pulled with the grappling hook",
        schema, true, arguments -> hook.lastPulls());
```

The schema is the JSON Schema of the arguments object, and the AI fills the arguments from it. The tool runs on the main
thread. Return a `JsonPrimitive` string for plain text, or any other JSON. Throw `IllegalArgumentException` for a wrong
argument and the AI sees its message. Pass `readOnly` false for a tool that changes something. Such tools are refused
while players without admin rights are online, like the built-in ones.

## What gets checked

On the first tick, again when a file is reloaded, and on `/bw config check`:

- a reference to a plugin that is not installed or not enabled is a warning, and the item is hidden or the effect skipped
- a reference to an id the plugin did not register is an error, with the same result
- settings are bound with the same checks as the built-in config, such as `utility.yml:14 items.grapple.settings.range: 100 is out of range 5 to 60, the default is used`

The exported schemas in `plugins/AzuraBedWars/schema/` include the settings of every registered extension, so an
editor with the YAML language server completes and checks them.

## Calls

Every call runs on the main thread. An exception thrown by an extension is logged with the plugin that registered it
and does not stop the game.

| Interface | Called |
| --- | --- |
| `CustomItem.create` | when the item is given, on purchase and on respawn for `keep-on-respawn` items |
| `CustomItem.onUse` | on every click with the item in the main hand, cancel the event to stop vanilla behavior |
| `PurchaseAction.purchase` | after the price is paid |
| `PurchaseCondition.check` | before the price is paid, return null to allow or the message to show |
| `UpgradeEffect.apply` | once when the team buys the level |
| `UpgradeEffect.onSpawn` | each time a member of a team owning the level spawns |
| `TrapEffect.trigger` | when an enemy sets off the trap |
| `TimelineAction.run` | when the stage starts |
| `Placeholder.apply` | each time a line holding it is shown, with the viewing player or null |
| `DevTool.call` | when an AI client calls the tool |

`CustomItem.create` receives the stack already tagged as your item. Change that stack, such as its meta, rather than
returning a new one, or `onUse` will no longer recognize it.
