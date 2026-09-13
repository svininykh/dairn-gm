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
creation through `InteractiveCharacterCreationModule`; it does not contain ruleset ID checks or own rules logic.

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

Cairn 2e character creation supports interactive name and attribute-swap prompts. It can also be run
non-interactively and reproducibly for tests and integrations:

```shell
./gradlew :dairn-gm-tui:run --args="--lang en character new --module cairn-2e --seed 42 --choice cairn-2e.character.name=1 --choice cairn-2e.character.attribute-swap=str-wil"
```

Attributes are rolled in `STR`, `DEX`, `WIL` order. The player may keep them or swap one pair,
following Cairn 2e rules.
Great Steppe now exposes its own character-creation process through the same neutral protocol. Its
personal character data, free-form name, attributes, HP, traits, bond, and age are kept separate from
Cairn's background-driven process. The group Omen is intentionally excluded from a single-character
artifact because it is determined only after comparing the ages of all characters.

Cairn-derived rules data and attribution are documented in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Rules structure is stored as JSON under each ruleset's `src/main/resources` directory, not as Kotlin
constants. The Cairn module validates Backgrounds, their two tables, Traits, Bonds, Omens, and every
localization key when resources are loaded. Background-table results are resolved as their own
stateless transition and attached to the completed character.

The ruleset-neutral interaction protocol in `core` represents shell interaction as `Roll`, `Choose`,
or `EnterText` requests and typed responses. Existing Cairn creation will be migrated to this protocol
before a different Great Steppe creation process is introduced.

The Cairn module exposes an interactive-process implementation that owns its models, dice expressions,
request order, and validation. The TUI executes the neutral protocol without Cairn-specific logic.

## Ruleset localization

Ruleset JSON resources contain only structural data, stable identifiers, dice values, and localization
keys. English text is stored in an unqualified `messages.properties` bundle and is the fallback for
languages that do not yet have a complete translation. Language-specific bundles use the usual suffix,
for example `messages_ru.properties` for Russian.
