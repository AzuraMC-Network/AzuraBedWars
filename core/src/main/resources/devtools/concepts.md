# AzuraBedWars concepts

AzuraBedWars runs one BedWars game per server. Players join, wait in a lobby, are split into teams, and each team defends a bed. A player whose bed is gone is out after the next death. The last team standing wins.

## Modes

- **Editor mode**: `editor-mode: true` in `settings.yml`, or no stored map passes the map check. No game runs. Maps can be created and edited.
- **Game mode**: a game runs on the default map (`default-map-name` in `settings.yml`), or on the first map that passes the check. Maps cannot be edited because the running game uses them.

Switching between the modes needs a server restart. `server_info` shows the current mode.

## Maps

A map is stored data on top of a built world. At game start the world folder (`world_folder`) is copied into the server's world container under the map name, so the map name must differ from the main world name, and edits to the copy are lost when the game ends.

| Point | Meaning |
| --- | --- |
| waiting | Lobby where players wait before the game. Usually above or beside the map |
| respawn | Where spectators and respawning players wait. Inside the region |
| pos1, pos2 | Opposite corners of the region. Players cannot place blocks outside it. The higher corner's Y is the build limit |
| base | Team spawn point. Bases are teams in order: base 0 is the first team. The team color comes from wool within `team-block-search-radius` (gameplay.yml, default 15) |
| drop BASE | Team iron and gold generator, one per base, near it |
| drop DIAMOND, EMERALD | Shared generators, usually diamonds between bases and emeralds in the middle |
| shop ITEM, UPGRADE | Item shop and team upgrade NPCs, usually one of each per base. They face the stored yaw |

Each base needs a bed within `bed-search-radius` (gameplay.yml, default 18) of its spawn. The bed nearest the spawn is the team's bed, so two bases closer together than that radius may claim the same bed. `map_check` reports this.

The map holds `bases × team_players` players. A game starts when `min_players` players are waiting.

## Config files

Files live in `plugins/AzuraBedWars/`, keys are kebab-case, and every key is documented in the file comments and in `config_schema`. Colors use `&` codes such as `&a`.

Each document has an apply mode:

- **LIVE**: in effect right away. Keys marked as needing a restart keep their old value until a restart, `restartPaths` lists them
- **NEXT_GAME**: generators and timeline are read when a game starts, a running game keeps the old values
- **RESTART**: database settings, only read at startup

A file with errors is never applied. A single bad value in a file that loads at startup falls back to its default and is reported in the log.

`settings.yml` holds the editor mode, economy mode (ITEM or EXPERIENCE, picked once per server) and the default map. `timeline.yml` stage seconds count from the end of the previous stage, not from game start.

Values such as passwords are shown as `"<hidden>"`. Change other keys of that file with `config_set`, which leaves hidden values alone.

## Shop and upgrades

The shop is `shop/menu.yml` (title, category order, quick buy), `shop/tiers.yml` (tier chains such as pickaxes and armor that only go up) and one file per category in `shop/categories/`. A category's `layout` is six rows of nine characters, each character placing the item with that key, so a page is drawn by hand like the inventory it becomes. Prices are written as amount and resource id, such as `4 iron`, and the resources are the ones in `generators.yml`. `upgrades.yml` holds team upgrades and traps with the same kind of layout.

After a change, `shop_render` and `upgrade_render` draw the page as players will see it. In game mode `shop_open` opens it for a player in game, and `player_give` hands out resources to try a purchase.

## Test games

In game mode, `game_state` shows the timeline and the teams. `game_control` starts a waiting game once two teams can be filled, runs the current timeline stage at once, or ends the game. Ending works like a real game end: stats are recorded and the server shuts down after the results.

## Extensions

Other plugins can add shop items, purchase actions and conditions, upgrade and trap effects, timeline actions and placeholders, referenced as `<plugin>:<id>`. `extension_list` shows what is registered, where each is written and the schema of its settings. Plugins can also add their own tools to this endpoint, named `<plugin>_<id>`.

## Safety

Every config write and map edit backs up the previous version, `history_list` and `history_rollback` undo it. Ten backups are kept per file or map. Writes are refused while a player without the `azurabedwars.admin` permission is online, unless the server owner allowed it.
