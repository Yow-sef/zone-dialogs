# ZoneDialogs

Native Minecraft dialogs for ZonePractice Pro. Players queue for kits, check leaderboards, and view stats without opening a chest GUI.

[![Build](https://github.com/Yow-sef/zone-dialogs/actions/workflows/build.yml/badge.svg)](https://github.com/Yow-sef/zone-dialogs/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Discord](https://img.shields.io/badge/Discord-join-5865F2?logo=discord&logoColor=white)](https://discord.yowsef.xyz)

## Requirements

| Component | Version |
|---|---|
| Paper | 1.21.11 or newer |
| ZonePractice Pro | 8.0.0 with API 2.9.0 |
| Java | 25 |

The plugin uses the native Paper dialog API and ZonePractice Pro methods added in 2.7, 2.8, and 2.9. It will disable itself when the API version is below 2.9.0.

## Install

1. Put `ZoneDialogs-1.1.0.jar` in your server's `plugins/` folder.
2. Start the server once. The plugin creates `plugins/ZoneDialogs/`.
3. Edit `config.yml` and the files in `menus/`.
4. Run `/zd reload` after changes.

## Queue menu

The queue menu shows one row per ladder with the core's icon, the queue count for the selected mode, and your elo and wins on hover. Click a row to join or leave that queue. Click the star to add or remove a favorite.

The mode switch sets ranked or unranked for new joins. Queue counts follow the selected mode.

Queue All queues every ladder in the open category. It requires multi-queue permission. Keep Queuing re-enters the ladder from the last match after the match ends. Leave All clears every queue.

Use Refresh to update counts. The menu also refreshes after queue events, category changes, mode changes, and queue actions.

## Leaderboards

The carousel pages through every enabled ladder plus the global board. Metric tabs are built from `menus/leaderboard.yml`. Search filters the fetched board by player name. Each row starts with a flat player-head sprite and the footer shows your rank and score for the selected metric.

Leaderboards refresh when you change the ladder, metric, page, or search query. Set `display.show-player-heads: false` in `menus/leaderboard.yml` if you prefer rows without heads.

## Player stats

The overview shows division, matches, wins, losses, WLR, kills, deaths, KDR, and best win streak. A global division bar sits under the overview.

Each ladder row shows elo, total wins, division progress, and win milestone progress. Unranked-only ladders show `Unranked` instead of a default elo.

## Bot practice

When a ZonePractice bot provider is registered, the queue menu shows a Practice vs Bot button. It closes the dialog and opens the core bot setup. The button is hidden when no provider is installed.

## Commands

| Command | What it does | Permission |
|---|---|---|
| `/dqueue` | Opens the queue dialog | `zonedialogs.queue` |
| `/dstats [player]` | Opens your stats or another online player's stats | `zonedialogs.stats` |
| `/dtop [ladder] [metric]` | Opens the leaderboard, optionally on a ladder and metric | `zonedialogs.leaderboard` |
| `/zd open` | Opens the queue dialog | `zonedialogs.queue` or `zonedialogs.use` |
| `/zd reload` | Reloads config, menus, thresholds, and the refresh task | `zonedialogs.admin` |
| `/zd info` | Shows the plugin version | none |

Aliases:

- `/dqueue`: `dialogqueue`, `dq`, `queuemenu`, `zqueue`
- `/dstats`: `dprofile`
- `/dtop`: `dleaderboard`, `dlb`
- `/zd`: `zonedialogs`

Bare `/queue` opens this dialog when `settings.override-default-queue-command` is `true`. Commands with arguments, such as `/queue sword`, `/queue leave`, and `/queue accept`, still go to ZonePractice Pro.

## Permissions

| Permission | Default | Description |
|---|---|---|
| `zonedialogs.queue` | true | Open and use the queue menu |
| `zonedialogs.stats` | true | Open player stats |
| `zonedialogs.leaderboard` | true | Open leaderboards |
| `zonedialogs.use` | op | Access the management command |
| `zonedialogs.admin` | op | Reload the plugin |

## Configuration

```
plugins/ZoneDialogs/
  config.yml
  menus/
    queue.yml
    leaderboard.yml
    stats.yml
  userdata/
    <uuid>.yml
```

The main settings:

```yaml
settings:
  override-default-queue-command: true
  auto-refresh-interval-ticks: 0
  auto-detect-ladders: true
  fallback-category: "skills"
```

`auto-refresh-interval-ticks: 0` disables the timer. A timer re-shows the dialog on each tick, which resets the scroll position. Leave it at `0` unless you need live counts and can accept that trade-off.

Paper does not expose a dialog-close event. When a refresh timer is active, the queue menu keeps Escape disabled so a closed menu is not reopened by the next refresh. New installs disable Escape in `menus/queue.yml` for the same reason.

`auto-detect-ladders` adds any enabled core ladder that is missing from every category to the fallback category.

Queue categories, colors, custom icons, progress bars, win milestones, sounds, and dialog widths are in `menus/queue.yml`. Leaderboard metrics and page size are in `menus/leaderboard.yml`. Stats display and ladder limits are in `menus/stats.yml`.

## Build

You need JDK 25 and Maven.

```bash
mvn clean package
```

The plugin jar is written to `target/ZoneDialogs-1.1.0.jar`. The build resolves Paper and the ZonePractice Pro API from their Maven repositories. The API is `provided` and is not bundled.

## Compatibility notes

Leaderboard entries and player scores come from the public API added in ZonePractice Pro 2.9.0, so no internal core classes are used.

The public queue, division, ladder, leaderboard, and bot APIs are used everywhere else.

## Support

Join the Discord for install help, configuration questions, and bug reports: [discord.yowsef.xyz](https://discord.yowsef.xyz)

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

## License

MIT. See [LICENSE](LICENSE).
