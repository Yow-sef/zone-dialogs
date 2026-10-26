# Changelog

## 1.1.0

### Leaderboards

- Use the ZonePractice Pro 2.9.0 `getLeaderboardEntries` and `getLeaderboardEntry` methods instead of reflecting into the core.
- Show player heads as flat 1.21.9+ sprites inside each leaderboard row.
- Add `display.show-player-heads` to `menus/leaderboard.yml`.
- Search now fetches the full 100-entry API cap before filtering.
- Personal scores now come from the leaderboard entry, including global metrics that previously had no score.

### Compatibility

- Requires ZonePractice Pro with API level 8 (2.9.0 or newer).
- Removed the internal leaderboard reflection bridge.

## 1.0.0

Initial public release.

### Queue menu

- Dialog queue menu with per-ladder rows, icons, elo, wins, and queue counts.
- Ranked and unranked mode switch.
- Favorite ladders.
- Queue All for the open category, gated by multi-queue permission.
- Keep Queuing after a match.
- Leave All.
- Manual refresh plus automatic refresh after queue events and menu actions.
- Bot Practice button when a ZonePractice bot provider is available.

### Leaderboards

- Global and per-ladder leaderboards.
- Metric tabs driven by `menus/leaderboard.yml`.
- Player search.
- Personal rank and score footer.
- Configurable page size and entry count.

### Stats

- Player overview with division, matches, wins, losses, WLR, kills, deaths, KDR, and best win streak.
- Global division progress bar.
- Per-ladder elo, wins, division progress, and win milestone progress.
- Unranked-only ladders are labeled instead of showing a default elo.

### Administration

- `/zd reload` reloads config, menus, division thresholds, and the refresh task.
- Bare `/queue` can be redirected to the dialog. Arguments still go to ZonePractice Pro.
- Auto-detection for ladders missing from every category.
- Configurable queue categories, colors, icons, progress bars, sounds, and widths.
