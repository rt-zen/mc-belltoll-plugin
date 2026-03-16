# For When the Bell Tolls

A lightweight [PaperMC](https://papermc.io/) plugin that announces the real-world time (RTC) to players at configurable intervals — like a church bell ringing periodically.

## Features

- Uses real-world system time (RTC), not Minecraft time
- Exact interval synchronisation — e.g. always fires at `:00`, `:15`, `:30`, `:45`
- Configurable timezone
- [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting support
- Three display modes: **Chat**, **Action Bar**, **Title**
- `/belltoll ring` command for manual announcements from console or in-game
- Hot-reload config without a server restart
- No scheduler drift — uses a once-per-second tick with minute-change detection

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

The compiled JAR will be at `target/ForWhenTheBellTolls-1.0.0.jar`.

## Configuration

**`config.yml` defaults:**

```yaml
timezone: "Europe/Lisbon"
interval-minutes: 15
time-format: "HH:mm"
display-mode: ACTIONBAR
message: "<gold>🔔 The church bell rings! <yellow>%time%</yellow></gold>"
title:
  fade-in: 10
  stay: 60
  fade-out: 10
```

| Option | Description | Default |
|--------|-------------|---------|
| `timezone` | Java timezone ID (e.g. `Europe/Lisbon`, `America/New_York`, `UTC`). Falls back to `UTC` if invalid. | `Europe/Lisbon` |
| `interval-minutes` | Minutes between announcements. Must be a divisor of 60 (1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60). | `15` |
| `time-format` | [`DateTimeFormatter`](https://docs.oracle.com/en/java/docs/api/java.base/java/time/format/DateTimeFormatter.html) pattern. | `HH:mm` |
| `display-mode` | `CHAT`, `ACTIONBAR`, or `TITLE`. Falls back to `CHAT` if invalid. | `ACTIONBAR` |
| `message` | MiniMessage-formatted announcement. Use `%time%` for the current time. | see above |
| `title.fade-in` | Title fade-in duration in ticks (20 ticks = 1 s). Only used when `display-mode: TITLE`. | `10` |
| `title.stay` | Title display duration in ticks. | `60` |
| `title.fade-out` | Title fade-out duration in ticks. | `10` |

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/belltoll reload` | Reloads `config.yml` live. | `belltoll.reload` |
| `/belltoll ring [chat\|actionbar\|title]` | Manually triggers a time announcement. The optional argument overrides the configured display mode for this single send only. | `belltoll.ring` |

Both permissions default to **operators only**.

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

1. Reads the current real-world minute.
2. Skips if this minute was already processed (`minute == lastMinute`).
3. Skips if the minute is not on the configured interval (`minute % interval != 0`).
4. Sends the announcement.

This guarantees announcements always occur at **exact clock times** regardless of when the server started.

**Example with `interval-minutes: 15`:** `00:00`, `00:15`, `00:30`, `00:45`, `01:00`, …

## License

Licensed under the [GNU General Public License v3.0](LICENSE).
