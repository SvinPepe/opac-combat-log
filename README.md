# Open Parties and Claims: Combat Log

[![Build](https://github.com/SvinPepe/opac-combatlog/actions/workflows/build.yml/badge.svg)](https://github.com/SvinPepe/opac-combat-log/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1+%20%7C%2026.x-orange.svg)](https://neoforged.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.16+-blue.svg)](https://fabricmc.net/)
[![Forge](https://img.shields.io/badge/Forge-47.3+-red.svg)](https://minecraftforge.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%201.21.1%20%7C%2026.x-green.svg)](https://minecraft.net/)

A high-performance, battle-hardened **Server-Only** combat log mod for Minecraft servers, featuring native integration with **Open Parties and Claims (OPAC)** and **OPAC: Warfare**.

---

## ⚔️ Supported Platforms & Versions

Built with **Stonecutter** & **Architectury Loom** to match the full version spectrum of **[Open Parties and Claims: Warfare](https://modrinth.com/mod/open-parties-and-claims-warfare)**:

| Minecraft Version | NeoForge | Fabric | Forge | Java Version |
|:---|:---:|:---:|:---:|:---:|
| **1.20.1** | — | ✅ (0.16+) | ✅ (47.3+) | Java 17 |
| **1.21.1** | ✅ (21.1+) | ✅ (0.16+) | — | Java 21 |
| **26.1** | ✅ (26.1+) | — | — | Java 25 |

---

## 🤝 Companion Mod: Open Parties and Claims: Warfare

**OPAC: Combat Log** works seamlessly alongside **[Open Parties and Claims: Warfare](https://modrinth.com/mod/open-parties-and-claims-warfare)**:

* **War Zones as Wilderness**: When Warfare is installed and active on the server, contested siege/war chunks are automatically treated as wilderness for combat tagging purposes.
* **No Combat Ejection / Rollback in War**: Players fighting inside contested war chunks will never be knocked back, teleport-rolled back, or ejected by claim boundaries during an active siege.
* **Full Tactical Mobility**: Ender Pearls and Chorus Fruit teleports can be freely used within contested war zones without safezone blocking.
* **Anti-Fleeing Invariants Preserved**: While players have full freedom of movement across contested war chunks, they are still strictly prevented from fleeing into adjacent peaceful, uncontested claims.
* **Zero Hard Dependency**: Integration is achieved via a soft reflection bridge (`WarfareCompat` querying `dev.customclaims.api.war.WarfareBridgeHolder`). The mod runs flawlessly whether Warfare is present or not.
* **Configurable**: Behavior can be toggled via `"treatWarChunksAsWilderness": true` in `config/opac-combatlog.json`.

---

## 🌟 Key Features

### 1. 🛡️ Intelligent OPAC Safezone Boundary Protection
- **Wilderness Initiation**: If a PvP fight begins in the wilderness (unclaimed territory or active war chunk), tagged players are physically barred from fleeing into ANY claimed territory or safezone.
- **Claim Initiation**: If a PvP fight starts *inside* a specific claim (e.g. an assault on a player's base), participants can fight within and return to that origin claim, but cannot flee to unrelated third-party claims.
- **Anti-Cheat Position Rollback**: When a player or vehicle attempts to cross into a forbidden claim, the server executes `player.connection.teleport(...)` with complete velocity reset (`Vec3.ZERO`), accompanied by barrier sound and particles. Bypasses using sprint-jumping, speed hacks, or client-predicted movement are impossible.

### 2. 🐎 Complete Exploit Prevention
- **Vehicles (Boats / Horses / Minecarts)**: Intercepts vehicle movement to prevent riding across claim boundaries during combat.
- **Ender Pearls**: Teleportation is cancelled if an ender pearl lands inside a forbidden claim while in combat.
- **Chorus Fruit**: Random teleportation is cancelled if it would land inside a forbidden claim.
- **Flight Disabler**: Creative, spectator, `/fly`, or modded flight abilities are immediately revoked upon taking or dealing PvP damage.

### 3. 🔒 Battle-Hardened Locks (Inspired by CombatLogX)
- **Anti-Stashing (Container Lock)**: Opening Ender Chests, regular chests, barrels, and shulker boxes is strictly blocked during combat.
- **Portal Travel Lock**: Entering Nether or End portals during combat is disabled to prevent escaping dimensions.
- **Brigadier Command Filter**: Teleportation commands (`/spawn`, `/home`, `/tp`, `/tpa`, `/warp`, etc.) and OPAC claim management commands are intercepted at the root parser level, ignoring `/minecraft:` prefixes or aliases.

### 4. ⚔️ Punitive Combat Logout with Attacker Credit
- If a player logs out or disconnects while tagged in combat:
  - Lethal damage is dealt directly on behalf of the last attacker (`player.damageSources().playerAttack(lastAttacker)`).
  - The victim dies, their inventory drops at the logout coordinate, and the death message properly credits the opponent.
  - A server-wide notification is broadcast: `⚔ <Player> logged out during combat with <Attacker> and was eliminated!`.

### 5. 🏆 Untag on Enemy Death
- Killing your opponent in combat immediately clears your combat tag, releasing you from combat restrictions without having to wait out the remaining timer.

### 6. 📱 Minimalist Server-Side Actionbar
- Live countdown timer rendered cleanly in the Actionbar (`⚔ In Combat: 14s (Do not log out!)`) using pure vanilla text packets. Zero client mod required!

---

## ⚙️ Configuration (`config/opac-combatlog.json`)

```json
{
  "combatDurationSeconds": 15,
  "untagOnEnemyDeath": true,
  "disableFlight": true,
  "restoreFlightAfterCombat": true,
  "blockContainers": true,
  "blockPortals": true,
  "blockEnderPearlsIntoClaims": true,
  "blockChorusFruitIntoClaims": true,
  "broadcastCombatLog": true,
  "actionbarNotification": true,
  "operatorBypass": false,
  "treatWarChunksAsWilderness": true,
  "knockbackStrength": 0.5,
  "blockedCommands": [
    "spawn",
    "home",
    "sethome",
    "tp",
    "tpa",
    "tpaccept",
    "tpahere",
    "warp",
    "back",
    "rtp",
    "openpac",
    "opac"
  ]
}
```

---

## 🏗️ Architecture

```text
opac-combatlog/
├── common/                  # Loader-agnostic core domain logic & Warfare bridge
│   ├── compat/              # WarfareCompat reflection bridge
│   ├── config/              # JSON server configuration
│   ├── model/               # CombatTag & state records
│   ├── service/             # CombatManager, ClaimBoundaryService, PvPDamageResolver
│   ├── opac/                # OpenPACServerAPI hooks
│   └── command/             # Brigadier command inspection
│
├── neoforge/                # NeoForge loader distribution (1.21.1, 26.1)
├── fabric/                  # Fabric loader distribution & mixins (1.20.1, 1.21.1)
└── forge/                   # Forge loader distribution (1.20.1)
```

---

## 🛠️ Building & Testing

### Requirements
- **JDK 21** or newer
- **Gradle 9.5+** (included via wrapper)

### Build All Distribution Jars
```bash
./gradlew build
```

Generated jars:
- `fabric/versions/1.20.1/build/libs/opac-combatlog-1.20.1-fabric.jar`
- `fabric/versions/1.21.1/build/libs/opac-combatlog-1.21.1-fabric.jar`
- `forge/versions/1.20.1/build/libs/opac-combatlog-1.20.1-forge.jar`
- `neoforge/versions/1.21.1/build/libs/opac-combatlog-1.21.1-neoforge.jar`
- `neoforge/versions/26.1/build/libs/opac-combatlog-26.1-neoforge.jar`

### Run Unit Tests
```bash
./gradlew test
```

---

## 📄 License

This mod is released under the **MIT License**.
