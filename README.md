# z0cken Minecraft
This repository is an archive of Spigot and BungeeCord plugins for the z0cken Minecraft server (2019-2023)
<details>
  <summary>Announcement Teaser</summary>
  <img src="https://images.pr0gramm.com/2019/01/20/e64159aadb4fb085.png">
</details>

## Core
This module is the common denominator of all plugins, providing unified access to configuration, database and user metadata.

## Checkpoint
BungeeCord plugin responsible for enforcing constraints around server access.

- Authenticates pr0gramm users
- Handles Terms of Service agreements
- Provides an invite system for outside players
- Allows for anonymization of player-facing metadata

## Claim
Player controlled chunk-based world protection.
- Enables chunk claiming via portal frame blocks
  - Additional unlock step to prevent accidental unclaim
- Guards all blocks from manipulation or access
- Prevents outside entity damage / interaction
- Grants access to owner's friends

## Capture
Simple Capture The Flag minigame for the established red/blue teams on pr0gramm.

## Economy
Fully-fledged virtual currency management with integrated shop system authored by [@florilu](https://github.com/florilu).

## Elytra
Rings parcour with timings and leaderboard for the lobby server.

## End
- Regularly switches the end between PVP and dragon bossfight mode
- Rolls back world changes after each bossfight
- Distributes XP award based on individual contribution
- Turns dragon eggs into a useful reward (portable beacons)
- Makes end crystals target and damage flying attackers to increase bossfight difficulty
- Tracks naturally generated elytras to limit supply via adaptive world border
- Safely prevents elytra flight beyond main island for more difficult exploration
- Records end-specific statistics like dragon damage and destroyed crystals

## Essentials
Provides a multitude of general-purpose functionality:
- Award badges to players for exceptional achievements
- Enhance chat messaging
  - Display player metadata in hover tooltip
  - Highlight URLs and shorten those leading to pr0gramm
  - Enable @mentions with audible notification
- Compass menu (inventory GUI) supporting global targets, home points and claims
- Track discovered points of interest to unlock in the compass menu
- Track donations to cancer research where each diamond is matched 10:1.
- Monitor and rate limit entity hot spots to mitigate lag spikes
- Provide a small interactive chat wiki of the server
- Generate a ranking of inactive AreaShop owners
- Create a minion with shared health for each player as april fools joke
- Toggle nether portals into overworld / claims
- Audibly warn players of scheduled server restarts in advance
- Support player-to-player healing by throwing textured snowballs ("blussis") from thin air
- Add command letting players teleport from the main city to a random safe point in wilderness
- Supports toggling and hot-reload on per-module basis

<details>
  <summary>Cancer research fundraiser post</summary>
  <a href="https://www.faz.net/aktuell/gesellschaft/menschen/shitstorm-gegen-brian-krebs-fuehrt-zu-spendenaktion-15516980.html">German news article documenting the entire pr0gramm fundraiser</a>
  <img src="https://images.pr0gramm.com/2019/03/28/37870355e8e15599.png">
</details>

## Metro
Dungeon within the city demanding its stations to be filled with lapis lazuli, whereby the number of active stations determines a set of global buffs/penalties. Poses a constant challenge to the community, especially extrinsically motivated players.
- Automatically spawn packs of customized monsters
- Render a dynamic map of the metro system indicating station status
- Track players within the dungeon and records statistics like contributed lapis lazuli
- Add custom inventory GUI to limit station capacity
- Reward players with XP for each contribution
- Punish combat-logging within the dungeon

Metro effects include:
- Weather changes
- Difficulty changes
- Odds of portal malfunction
- Global potion effects
- Monster spawn rate adjustments
- Zombie pigmen aggressiveness
- Daily currency bonus

## Progression
Provides simple abstraction for unified database access to player-statistics.

## Quests
Aggregation of [BetonQuest](https://github.com/BetonQuest/BetonQuest) scripts authored by [@KingJulian13](https://github.com/KingJulian13) & [@DerLukas01](https://github.com/DerLukas01).

## Raid
Minigame based on the ["Storm Area 51"](https://en.wikipedia.org/wiki/Storm_Area_51) meme in September 2019. Defending team is stationed in a military base holding aliens (endermen) hostage. Attacker's goal is to rescue as many aliens as possible, by leashing and returning them to hovering UFOs outside.
<details>
  <summary>Event announcement</summary>
  <img src="https://images.pr0gramm.com/2019/09/19/67097dd4113f89cd.png">
</details>

## Revive
Enables player-player revives by spawning a fake corpse interactible with a totem of undying, authored by @Jalau.

## Shout
In-game soundboard to amuse or confuse nearby players, authored by [@florilu](https://github.com/florilu).<br>Powered by a custom inventory GUI, compatible with permissions and currency systems.
