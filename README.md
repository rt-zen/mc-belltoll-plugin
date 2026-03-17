# For When the Bell Tolls

A lightweight [PaperMC](https://papermc.io/) plugin that announces the real-world time (RTC) to players at configurable intervals — like a church bell ringing periodically.

## Features

- Uses real-world system time (RTC), not Minecraft time
- Exact interval synchronisation — e.g. always fires at `:00`, `:15`, `:30`, `:45`
- Configurable timezone (server-wide and per-player)
- Audible bell sound with configurable sound name, volume, and pitch
- Audio-only-at-certain-intervals — e.g. ring audibly only at the full and half hour
- Per-player notification preferences: **Full** (text + audio), **Stealth** (text only), **Disabled**
- Per-player interval and timezone overrides
- [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting support
- Three display modes: **Chat**, **Action Bar**, **Title**
- `/belltoll prefs` command for players to manage their own settings
- `/belltoll ring` command for manual announcements from console or in-game
- Hot-reload config without a server restart
- No scheduler drift — uses a once-per-second tick with per-player minute-change detection

## Requirements

- PaperMC **1.21.4+**
- Java **21+**

## Installation

1. Download the latest JAR from [Releases](../../releases).
2. Place it in your server's `plugins/` directory.
3. Start or reload the server — `plugins/ForWhenTheBellTolls/config.yml` is generated automatically.
4. Edit the config to your liking.
5. Run `/belltoll reload` to apply changes without restarting.

## Building from Source

```bash
mvn clean package
```

The compiled JAR will be at `target/ForWhenTheBellTolls-1.1.0.jar`.

## Configuration

**`config.yml` defaults:**

```yaml
timezone: "GMT"
interval-minutes: 15
time-format: "HH:mm"
display-mode: ACTIONBAR
message: "<gold>🔔 The church bell rings! <yellow>%time%</yellow></gold>"
title:
  fade-in: 10
  stay: 60
  fade-out: 10
sound:
  name: "BLOCK_BELL_USE"
  volume: 1.0
  pitch: 1.0
audio-minutes: [0, 30]
default-notification-mode: FULL
```

| Option | Description | Default |
|--------|-------------|---------|
| `timezone` | Java timezone ID (e.g. `Europe/Lisbon`, `America/New_York`, `UTC`). Falls back to `UTC` if invalid. | `GMT` |
| `interval-minutes` | Minutes between announcements. Must be a divisor of 60 (1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60). | `15` |
| `time-format` | [`DateTimeFormatter`](https://docs.oracle.com/en/java/docs/api/java.base/java/time/format/DateTimeFormatter.html) pattern. | `HH:mm` |
| `display-mode` | `CHAT`, `ACTIONBAR`, or `TITLE`. Falls back to `CHAT` if invalid. | `ACTIONBAR` |
| `message` | MiniMessage-formatted announcement. Use `%time%` for the current time. | see above |
| `title.fade-in` | Title fade-in duration in ticks (20 ticks = 1 s). Only used when `display-mode: TITLE`. | `10` |
| `title.stay` | Title display duration in ticks. | `60` |
| `title.fade-out` | Title fade-out duration in ticks. | `10` |
| `sound.name` | Bukkit `Sound` enum name for the bell audio (e.g. `BLOCK_BELL_USE`, `BLOCK_NOTE_BLOCK_BELL`). | `BLOCK_BELL_USE` |
| `sound.volume` | Sound volume. | `1.0` |
| `sound.pitch` | Sound pitch (0.5–2.0). | `1.0` |
| `audio-minutes` | Minute-of-hour values (0–59) that trigger audio. Empty list = every interval. Example: `[0, 30]` = full & half hour. | `[0, 30]` |
| `default-notification-mode` | Default for players with no personal preference: `FULL` (text + audio), `STEALTH` (text only), or `DISABLED` (silent). | `FULL` |

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/belltoll reload` | Reloads `config.yml` live. | `belltoll.reload` (op) |
| `/belltoll ring [chat\|actionbar\|title]` | Manually triggers a time announcement for all players. The optional argument overrides the display mode for this send only. | `belltoll.ring` (op) |
| `/belltoll prefs` | Shows your current notification preferences. | `belltoll.prefs` (all players) |
| `/belltoll prefs mode <disabled\|stealth\|full>` | Sets your personal notification mode. | `belltoll.prefs` |
| `/belltoll prefs interval <minutes\|reset>` | Sets your personal announcement interval (must be a divisor of 60), or resets to server default. | `belltoll.prefs` |
| `/belltoll prefs timezone <zone\|reset>` | Sets your personal timezone (e.g. `America/New_York`), or resets to server default. | `belltoll.prefs` |
| `/belltoll prefs audio <minutes\|all\|reset>` | Sets which minute-of-hour values trigger audio for you (e.g. `0,30`), `all` for every interval, or `reset` to server default. | `belltoll.prefs` |

### Notification Modes

| Mode | Text | Audio |
|------|------|-------|
| `FULL` | ✔ | ✔ (at configured `audio-minutes`) |
| `STEALTH` | ✔ | ✗ |
| `DISABLED` | ✗ | ✗ |

Per-player preferences are stored in `plugins/ForWhenTheBellTolls/players.yml` and persist across restarts.

## Display Modes

### `CHAT`
Sends the formatted message to all players in chat.

### `ACTIONBAR`
Displays the message above the hotbar. Good default — keeps chat clean.

### `TITLE`
Shows a large centred message on screen. Timing is tunable with `title.fade-in`, `title.stay`, and `title.fade-out`.

## MiniMessage Formatting

The `message` field supports the full [MiniMessage](https://docs.advntr.dev/minimessage/format.html) syntax.

| Example | Effect |
|---------|--------|
| `<gold>%time%</gold>` | Gold-coloured time |
| `<bold><red>🔔 %time%</red></bold>` | Bold red text |
| `<gradient:gold:yellow>Church Bell: %time%</gradient>` | Gold → yellow gradient |

`%time%` is replaced with the current formatted time on every send.

## How the Clock Logic Works

Instead of scheduling a task every X minutes (which drifts based on server start time), the plugin runs a task **once per second** that:

1. Reads the current real-world minute for each online player, using their personal timezone (or the server default).
2. Skips if this minute was already processed for that player.
3. Skips if the minute is not on the player's configured interval (or the server default).
4. Sends the text announcement (respects per-player notification mode).
5. Plays the bell sound if the player's mode is `FULL` and the current minute is in their audio-minutes list.

This guarantees announcements always occur at **exact clock times** regardless of when the server started.

**Example with `interval-minutes: 15`:** `00:00`, `00:15`, `00:30`, `00:45`, `01:00`, …

## License

Licensed under the [GNU General Public License v3.0](LICENSE).
