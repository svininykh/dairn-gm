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

## Stateless process model

Engine processes are pure transitions:

```text
current state + command -> new state + effects + optional required choice
```

The engine does not persist process state. A TUI, server, StoryTeller, or StoryMaker owns the state
and supplies random results and user choices as explicit commands.

Rulesets advertise optional capabilities through their module API. The TUI discovers character
creation through `CharacterCreationModule`; it does not contain ruleset ID checks or own rules logic.

## Run

```shell
./gradlew :dairn-gm-tui:run --args="--lang en module list"
```

Supported languages are Kazakh (`kk`), Russian (`ru`), and English (`en`).

Examples:

```shell
./gradlew :dairn-gm-tui:run --args="--lang kk --help"
./gradlew :dairn-gm-tui:run --args="--lang ru module list"
./gradlew :dairn-gm-tui:run --args="--lang en character new --module cairn-2e"
```

Cairn 2e character creation supports an interactive assignment prompt. It can also be run
non-interactively and reproducibly for tests and integrations:

```shell
./gradlew :dairn-gm-tui:run --args="--lang en character new --module cairn-2e --seed 42 --assign 2,1,3"
```

The assignment numbers select the rolled values for `STR`, `DEX`, and `WIL`, respectively.
Character creation for Great Steppe remains intentionally unimplemented until its rules are defined.
