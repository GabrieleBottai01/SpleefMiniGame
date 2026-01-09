# SpleefMiniGame

**The ultimate automated Spleef solution for Paper servers.**

SpleefMiniGame is a modern, lightweight plugin that allows you to generate, manage, and play Spleef games with zero manual effort. Featuring dynamic arena generation, automatic resets, and stunning RGB visuals using MiniMessage.

---

## Features

* **Instant Arena Generation:** Create arenas of any size (e.g., 11x11, 25x25, 101x101) with a single command.
* **Auto-Reset System:** The arena automatically rebuilds itself 5 seconds after a game ends. No manual work required!
* **MiniMessage Support:** Full support for RGB gradients, hex colors, and bold text in chat and titles.
* **Fully Configurable:** Edit every single message, title, and prefix via `messages.yml`.
* **Smart Game Loop:**
    * Instant elimination detection (obsidian floor).
    * Glowing effect for all active players.
    * No fall damage during matches.
    * Spectator mode upon elimination.
* **Glass Walls:** Arenas are automatically encased in glass to prevent players from falling out.

---

## Commands

| Command | Description |
| :--- | :--- |
| `/spleef create [size]` | Creates a new arena. You can use presets (`small`, `medium`, `big`) or a custom number (e.g., `35`). |
| `/spleef start` | Teleports all nearby players into the arena, starts the countdown, and begins the game. |
| `/spleef delete` | Completely removes the arena structure from the world. |
| `/spleef reload` | Reloads the `messages.yml` configuration file without restarting the server. |

### Examples:
* `/spleef create` (Default 11x11)
* `/spleef create big` (Creates 31x31)
* `/spleef create 55` (Creates a massive 55x55 arena)

---

## Configuration

The plugin generates a `messages.yml` file where you can customize the plugin's language and style. It supports **MiniMessage** format.

**Example `messages.yml` snippet:**
```yaml
prefix: "<gradient:#00AAFF:#0055AA><bold>SPLEEF</bold></gradient> <dark_gray>»</dark_gray> "
game:
  victory-title: "<gradient:#FFD700:#FFFF00><bold>♛ VICTORY ♛</bold></gradient>"
  eliminated-chat: "<red>{player} <gray>was disintegrated!"