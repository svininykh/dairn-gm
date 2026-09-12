# DAIRN GM

Minimal modular game-master toolkit for DAIRN and Cairn-compatible rulesets.

## Architecture

- `dairn-gm-engine/core` — rules-agnostic dice, tables, choices, rules, effects, and module API.
- `dairn-gm-engine/cairn-2e` — Cairn 2e ruleset adapter.
- `dairn-gm-engine/great-steppe` — DAIRN: Great Steppe ruleset and extensions.
- `dairn-gm-tui` — localized text user interface and development shell.

The engine is stateless and independent of storage and user-interface concerns. The TUI depends on
the engine; engine modules never depend on the TUI. Future servers and graphical applications can
use the same engine API.

The v0.1 skeleton deliberately contains no AI, player/session model, or complex world state.

## Run

```shell
./gradlew :dairn-gm-tui:run --args="--lang en module list"
```

Supported languages are Kazakh (`kk`), Russian (`ru`), and English (`en`).

Examples:

```shell
./gradlew :dairn-gm-tui:run --args="--lang kk --help"
./gradlew :dairn-gm-tui:run --args="--lang ru module list"
./gradlew :dairn-gm-tui:run --args="--lang en character new --module great-steppe"
```
