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
and group creation through capability interfaces; it does not contain ruleset ID checks or own rules logic.

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
./gradlew :dairn-gm-tui:run --args="--lang ru group new --module great-steppe --member Айбек:25 --member Баян:31 --seed 42"
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

Great Steppe group creation accepts completed characters as explicit `name:age` inputs, determines
the youngest character, and rolls the shared group Omen. If several characters share the lowest age,
the process requests an explicit choice between them. The resulting group is returned as an artifact;
nothing is persisted by the engine or TUI.

The Great Steppe engine also exposes a pure daily survival rule for the currently confirmed text:
lack of water, food, or full rest causes `Deprived`; recovery is blocked while the condition remains,
and every deprived day after the first adds one `Fatigue`. Fire is not treated as a fourth direct
cause because the pinned rules do not define that mechanic yet. A calling shell may account for fire
when deciding whether full rest was possible.

Inventory load is represented by a ruleset-neutral `InventoryLoad` in `core`: entries only declare
the slots they occupy. The core has no concept of Fatigue. Great Steppe represents each Fatigue as
its own namespaced one-slot entry and keeps the confirmed capacity of 10 in its module. Gaining
Fatigue reports how many slots must be freed before it can be applied; evaluating a resulting full
load produces the effect that sets HP to zero. The shell remains responsible for choosing discarded
equipment and applying returned effects.

Cairn-derived rules data and attribution are documented in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Rules structure is stored as JSON under each ruleset's `src/main/resources` directory, not as Kotlin
constants. The Cairn module validates Backgrounds, their two tables, Traits, Bonds, Omens, and every
localization key when resources are loaded. Background-table results are resolved as their own
stateless transition and attached to the completed character.

The ruleset-neutral interaction protocol in `core` represents shell interaction as `Roll`, `Choose`,
or `EnterText` requests and typed responses. Both rulesets use this protocol for their implemented
creation processes.

The Cairn module exposes an interactive-process implementation that owns its models, dice expressions,
request order, and validation. The TUI executes the neutral protocol without Cairn-specific logic.

## Ruleset localization

Ruleset JSON resources contain only structural data, stable identifiers, dice values, and localization
keys. Localization is split into domain catalogs such as `traits`, `bonds`, and `omens`; every Cairn
Background has its own catalog under `i18n/backgrounds`. Unqualified `.properties` files contain the
English fallback, while localized variants use a language suffix such as `_ru.properties`.

The locale-neutral `i18n/catalogs.txt` index declares the catalogs to load. The loader rejects duplicate
keys and the Cairn data validator checks that every structural key exists in both English and Russian.

Great Steppe character tables are pinned to commit `3fabe55fa366c3706a5c54614b1682bd2f7cb5d6`
of the DAIRN: Great Steppe rules repository. The currently canonical Russian text is stored in split
`_ru.properties` catalogs; structural JSON contains no table prose. English and Kazakh catalogs will be
added separately rather than treating an unapproved translation as canonical.

## Content license and attribution

Textual game materials and table content are copyright © 2026 Andrey Svininykh and licensed under
[CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/). Cairn-derived materials retain
attribution to *Cairn Second Edition* author Yochai Gal. Russian Cairn text is based on the official
translation by Hex Cat studio. See [CONTENT_LICENSE.md](CONTENT_LICENSE.md) and
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for scope, sources, and pinned revisions.

The software source code is not covered by `CONTENT_LICENSE.md`; its public license will be selected
separately before release.
