# Changelog

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
