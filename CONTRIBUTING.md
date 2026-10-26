# Contributing

## Support

For install help and quick questions, use the Discord: [discord.yowsef.xyz](https://discord.yowsef.xyz)

## Issues

Use the bug report template and include:

- ZoneDialogs version
- ZonePractice Pro version
- Paper version
- Steps that reproduce the problem
- The full console error, not only the last line

For setup problems, include the relevant section of `config.yml` or `menus/*.yml` with secrets removed.

## Pull requests

Build the plugin before opening a pull request:

```bash
mvn clean package
```

Keep these rules in mind:

- Use JDK 25. The 2.9 API is compiled for Java 25.
- Keep existing config keys working. New options need a default in code.
- Do not bundle Paper, the ZonePractice Pro API, or the ZonePractice Pro core jar.
- Use the public ZonePractice Pro API where it exposes the data you need.
- Avoid internal core access. The public API is the supported surface.
- Update `README.md` and `CHANGELOG.md` for changes players or server owners will notice.

## Commits

Use a short imperative subject:

```
Fix queue menu reopening after close
Add leaderboard metric tabs
```

Keep unrelated changes in separate commits.
