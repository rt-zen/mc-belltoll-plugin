# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] – 2026-03-17

### Added

- **Audible bell sound** — plays a configurable Minecraft sound on each announcement.
  - `sound.name` — Bukkit `Sound` enum name (default: `BLOCK_BELL_USE`).
  - `sound.volume` — sound volume (default: `1.0`).
  - `sound.pitch` — sound pitch (default: `1.0`).
- **`audio-minutes`** global config option — list of minute-of-hour values (0–59) that trigger
  audio. An empty list plays audio at every scheduled interval. Default: `[0, 30]`
  (full and half hour).
- **`default-notification-mode`** global config option — sets the default notification mode
  for players who have not configured a personal preference. Options: `FULL`, `STEALTH`,
  `DISABLED`. Default: `FULL`.
- **Per-player notification preferences**, stored in `plugins/ForWhenTheBellTolls/players.yml`
  and persisted across restarts. Each preference falls back to the server default when unset.
  - **`/belltoll prefs`** — shows the player's current preferences.
  - **`/belltoll prefs mode <disabled|stealth|full>`** — sets the player's notification mode.
    - `FULL` — text + audio notification.
    - `STEALTH` — text only; no sound.
    - `DISABLED` — completely silent (no text, no audio).
  - **`/belltoll prefs interval <minutes|reset>`** — overrides the global announcement interval
    for this player (must be a divisor of 60).
  - **`/belltoll prefs timezone <zone|reset>`** — overrides the global timezone for this player
    (e.g. `America/New_York`). The displayed time will reflect the player's own zone.
  - **`/belltoll prefs audio <minutes|all|reset>`** — overrides the global `audio-minutes` for
    this player. Accepts a comma-separated list (e.g. `0,30`), `all` for every interval,
    or `reset` to restore the server default.
- **`belltoll.prefs` permission** (`default: true`) — guards all `/belltoll prefs` subcommands.
- **Full tab completion** for all new `/belltoll prefs` subcommands and values, including common
  timezone suggestions.
- **Console warnings on unknown config values** — `DisplayMode.fromString()` and
  `NotificationMode.fromString()` now log the offending value to the server console before
  falling back to their defaults, aiding admin auditing.

### Changed

- `ClockTask` now runs a **per-player notification loop** instead of broadcasting to everyone
  uniformly. Each player's effective timezone, interval, and notification mode are resolved
  individually on every tick.
- `sendTimeMessage()` (used by `/belltoll ring`) now also respects per-player notification
  modes — disabled players are skipped, stealth players receive text only.
- Updated README to reflect all new options and commands.

### Fixed

- Clock logic description in README incorrectly referred to a single global `lastMinute`
  variable; it is now per-player.

---

## [1.0.0] – initial release

### Added

- Real-world clock announcements at configurable intervals (divisors of 60).
- Three display modes: `CHAT`, `ACTIONBAR`, `TITLE`.
- Configurable timezone, time format, and [MiniMessage](https://docs.advntr.dev/minimessage/format.html) message template.
- Title fade-in / stay / fade-out timing.
- `/belltoll reload` — hot-reloads `config.yml` without a server restart.
- `/belltoll ring [chat|actionbar|title]` — manually triggers a time announcement with an
  optional display mode override.
- No scheduler drift — once-per-second tick with minute-change detection guarantees
  announcements fire at exact clock times regardless of server start time.
