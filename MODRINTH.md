# ⚔️ OPAC: Combat Log

**Stop claim-camping and PvP abuse.** A lightweight, server-side combat logging mod built specifically for **Open Parties and Claims (OPAC)**.

---

### ❓ Why OPAC: Combat Log?
If you've ever hosted an SMP or PvP server with **Open Parties and Claims**, you know the drill:
- A player starts a duel in the wild, takes a hit, and immediately **sprints across the border into an OPAC claim or safezone** where PvP is disabled.
- Attackers or defenders toss **Ender Pearls** or eat **Chorus Fruit** right into safezones or ally claims to escape punishment.
- Combatants **hot-stash loot into Ender Chests**, log out to avoid losing gear, or use `/spawn` and `/home`.

Standard combat log mods have zero awareness of OPAC claims or safezone subclaims. **OPAC: Combat Log bridges this gap completely.**

---

### ✨ Core Features

#### 🛡️ Native OPAC Claim & Safezone Integration
* **Anti-Safezone Camping:** If combat starts in unclaimed territory (wilderness), tagged players are physically barred from entering ANY claimed land or safezone.
* **Origin Claim Memory:** If a battle starts inside a claimed base, players can fight within that claim, but are prevented from fleeing to neighboring third-party claims.
* **Smart Boundary Pushback:** Trying to sprint-jump, ride a vehicle, or exploit client prediction into a forbidden claim results in a clean server-side pushback with zero-velocity rollback and barrier audio cues.

#### 🪓 Anti-Exploit Enforcement
* **Ender Pearl & Chorus Interception:** Pearls and Chorus Fruit cannot land or teleport players inside forbidden claims while in combat.
* **Flight Disabler:** Elytra, Creative, spectator, and `/fly` flight are instantly revoked upon dealing or taking PvP damage (and safely restored when combat expires).
* **Anti-Stashing (Container Lock):** Opening Ender Chests, chests, shulker boxes, and barrels is blocked while tagged.
* **Portal & Command Lock:** Nether/End portal traversal and escape commands (`/spawn`, `/home`, `/warp`, `/tp`, `/tpa`, claim management commands) are blocked.

#### 💀 Combat Logging & Fair Play
* **Combat Logout Punishment:** Quitting the game while in combat immediately kills the player.
* **Accurate Attacker Credit:** Drops player inventory at the logout coordinate and attributes the kill (and death message) directly to the opponent.
* **Untag on Kill:** Finishing off your opponent instantly clears your combat tag — no waiting around when you win!

#### ⚡ Minimalist & Server-Side
* **100% Server-Side:** Clients don't need any mod installed. Pure vanilla clients can connect freely.
* **Clean Actionbar Timer:** An unobtrusive countdown timer on the actionbar (`⚔ In Combat: 15s`) keeps players informed without screen clutter.

---

### 📦 Installation & Requirements
1. Install **[Open Parties and Claims](https://modrinth.com/mod/open-parties-and-claims)** on your server.
2. Drop **OPAC: Combat Log** into your server's `mods` folder.
3. Restart your server. That's it!

> 💡 **Client-Side:** Not required! Clients can join with completely vanilla Minecraft.

---

### ⚙️ Configuration & Documentation
Every aspect — combat tag duration, knockback strength, flight behavior, container lock, and command blacklists — is fully customizable in `config/opac-combatlog.json`.

* 📖 **[Configuration Guide & Default Config on GitHub](https://github.com/SvinPepe/opac-combat-log#readme)**
* 🐛 **[Issue Tracker & Suggestions](https://github.com/SvinPepe/opac-combat-log/issues)**
* 💻 **[Source Code](https://github.com/SvinPepe/opac-combat-log)**
