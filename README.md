# Open Parties and Claims: Combat Log (`opac_combatlog`)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1+-orange.svg)](https://neoforged.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1%20%7C%2026.x-green.svg)](https://minecraft.net/)

A high-performance, battle-hardened **Server-Only** combat log mod for Minecraft servers, featuring native integration with **Open Parties and Claims (OPAC)**.

---

## 🌟 Key Features

### 1. 🛡️ Intelligent OPAC Safezone Boundary Protection
- **Wilderness Initiation**: If a PvP fight begins in the wilderness (unclaimed territory), tagged players are physically barred from fleeing into ANY claimed territory or safezone.
- **Claim Initiation**: If a PvP fight starts *inside* a specific claim (e.g. an assault on a player's base), the participants can fight within and return to that origin claim, but cannot flee to unrelated third-party claims.
- **Anti-Cheat Position Rollback**: When a player or vehicle attempts to cross into a forbidden claim, the server executes `player.connection.teleport(...)` with complete velocity reset (`Vec3.ZERO`), accompanied by a barrier sound and particles. Bypasses using sprint-jumping, speed hacks, or client-predicted movement are impossible.

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
  "blockContainers": true,
  "blockPortals": true,
  "blockEnderPearlsIntoClaims": true,
  "broadcastCombatLog": true,
  "actionbarNotification": true,
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
├── common/                  # Loader-agnostic core domain logic
│   ├── config/              # JSON server configuration
│   ├── model/               # CombatTag & state records
│   ├── service/             # CombatManager, ClaimBoundaryService, PvPDamageResolver
│   ├── opac/                # OpenPACServerAPI hooks
│   └── command/             # Brigadier command inspection
│
├── neoforge/                # NeoForge server distribution & events
├── fabric/                  # Fabric server distribution & mixins
└── forge/                   # Forge distribution
```

---

## 🛠️ Building & Testing

### Requirements
- **JDK 21** or newer
- **Gradle 9.5+** (included via wrapper)

### Build Command
```bash
./gradlew build
```

The compiled mod jar will be in `neoforge/build/libs/opac-combatlog-1.0.0.jar`.

### Run Unit Tests
```bash
./gradlew test
```

---

## 📄 License

This mod is released under the **MIT License**.
