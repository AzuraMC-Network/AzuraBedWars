# Setting up a map

Work in editor mode. If `server_info` says game mode, set `editor-mode: true` in `settings.yml` with `config_set` and ask the admin to restart the server.

1. **World**: the admin builds the map in a world save that is not the main world. `world_list` shows loaded worlds. An unloaded world folder in the server directory can be loaded with `/bw loadWorld <folder>`, which the admin types in game.
2. **Create**: `map_create` with a short name, then `map_set` `world_folder` to the world save, for example `{$serverDir}/islands`. `map_check` reports when the folder is not a world save.
3. **Region**: ask the admin to stand at two opposite corners of the playable area, one of them at the build limit height, and set `pos1` and `pos2`.
4. **Lobby and spectators**: set `waiting` where players wait before the game and `respawn` somewhere above the middle of the map.
5. **Bases**: for each team, ask the admin to stand on the spawn point facing the island and add a `base`. Check that `map_get` shows a different `nearbyWool` color for each base, and that a bed is near. `world_scan` with `match: ["_BED", "_WOOL"]` finds them.
6. **Generators**: one `drop` of type `BASE` at each base's iron generator, then `DIAMOND` and `EMERALD` drops. Use `target: true` so the drop sits on top of the block the admin looks at.
7. **Shops**: one `shop` of type `ITEM` and one of type `UPGRADE` per base, facing where players come from. Pass `center: true` to snap the facing.
8. **Players**: `map_set` `team_players` (1 for solo, 2 for doubles, 4 for squads) and `min_players`. `author` is shown to players.
9. **Check**: run `map_check` until it reports no errors. Read each warning to the admin, some are fine on purpose.
10. **Look**: `map_markers` shows labels at every point. Ask the admin to fly around and confirm, then hide them.
11. **Play**: set `default-map-name` in `settings.yml` to the map, `editor-mode: false`, and ask the admin to restart. After the restart `server_info` should say game mode on this map, and `logs_tail` shows why if it does not.
