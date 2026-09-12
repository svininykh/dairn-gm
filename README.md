# DAIRN GM

Minimal modular game-master toolkit for DAIRN and Cairn-compatible rulesets.

## Modules

- `core` — rules-agnostic dice, tables, choices, rules, effects, and module API.
- `cairn-2e` — Cairn 2e ruleset adapter.
- `great-steppe` — DAIRN: Great Steppe ruleset and extensions.
- `cli` — localized command-line interface.

The v0.1 skeleton deliberately contains no AI, player/session model, or complex world state.

## Run

```shell
./gradlew :cli:run --args="--lang en module list"
```

Supported languages are Kazakh (`kk`), Russian (`ru`), and English (`en`).

Examples:

```shell
./gradlew :cli:run --args="--lang kk --help"
./gradlew :cli:run --args="--lang ru module list"
./gradlew :cli:run --args="--lang en character new --module great-steppe"
```
