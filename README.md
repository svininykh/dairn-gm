# DAIRN GM

> Early preview. The API, commands, and rules modules may change before the first stable release.

DAIRN GM is a modular, stateless toolkit for running DAIRN and Cairn-compatible tabletop role-playing
games. It separates reusable engine primitives, ruleset-specific behavior, and user interfaces so the
same engine can later power a terminal application, CRUD server, StoryTeller, or StoryMaker.

## Current capabilities

- Ruleset-neutral dice, tables, choices, rules, effects, inventory slots, and interactive processes.
- Cairn Second Edition character creation with English and Russian rules resources.
- DAIRN: Great Steppe character creation using its own Life Paths and starting inventory procedure.
- Complete Great Steppe initial-group creation: create each character, select the youngest character
  when ages are tied, and determine the shared Omen.
- Confirmed Great Steppe survival effects for water, food, full rest, Deprived, and Fatigue.
- A development TUI with help at every command level and interface messages in Kazakh, Russian,
  and English.
- Reproducible random generation through an optional seed.
- Automated tests and Gradle Wrapper validation in GitHub Actions.

## Architecture

```text
dairn-gm
├── dairn-gm-engine
│   ├── core          Ruleset-neutral engine API
│   ├── cairn-2e      Cairn Second Edition module
│   └── great-steppe  DAIRN: Great Steppe module
└── dairn-gm-tui      Text interface and development shell
```

The dependency direction is one-way:

```text
TUI / future server / future GUI
                ↓
         ruleset modules
                ↓
              core
```

`core` does not know about Cairn, Great Steppe, HP, Fatigue, water, food, or Omens. Ruleset modules
provide those meanings and expose optional capabilities such as character or group creation.

## Stateless process model

Engine processes are explicit transitions:

```text
current process state + response → next state or completed artifact
```

The engine does not save characters, groups, process state, or world state. A calling application
owns persistence and supplies dice results, choices, and text responses. This is intentional: the
same process can be hosted by different interfaces without embedding a database or UI into the
rules engine.

## Requirements

- JDK 21
- Git

No system Gradle installation is required. The repository includes Gradle Wrapper 8.8 with a pinned
SHA-256 checksum.

## Quick start

Clone the repository, enter its directory, and list the available modules:

```shell
git clone <repository-url>
cd dairn-gm
./gradlew :dairn-gm-tui:run --args="--lang en module list"
```

Show the complete command overview:

```shell
./gradlew :dairn-gm-tui:run --args="--lang en --help"
```

Supported interface language codes are `en`, `ru`, and `kk`.

## Usage examples

Start Cairn 2e character creation interactively:

```shell
./gradlew :dairn-gm-tui:run --args="--lang en character new --module cairn-2e"
```

Run the same process reproducibly and supply choices in advance:

```shell
./gradlew :dairn-gm-tui:run --args="--lang en character new \
  --module cairn-2e \
  --seed 42 \
  --choice cairn-2e.character.background=roll \
  --choice cairn-2e.character.name=1 \
  --choice cairn-2e.character.attribute-swap=str-wil"
```

Create a Great Steppe character:

```shell
./gradlew :dairn-gm-tui:run --args="--lang ru character new \
  --module great-steppe \
  --seed 42 \
  --choice great-steppe.character.life-path=18 \
  --text great-steppe.character.name=Aibek \
  --choice great-steppe.character.supplies-swap=keep \
  --text great-steppe.character.experience-detail=Caravan-life \
  --choice great-steppe.character.attribute-swap=keep"
```

Create four new Great Steppe characters and assemble their initial group interactively:

```shell
./gradlew :dairn-gm-tui:run --args="--lang ru group new \
  --module great-steppe \
  --members 4 \
  --seed 42"
```

The initial-group process composes the existing character-creation process for each member. Its
request IDs are namespaced as `great-steppe.initial-group.member-1.*`,
`great-steppe.initial-group.member-2.*`, and so on, allowing a future server or GUI to route every
response unambiguously.

If the characters already exist outside the engine, create a group from their names and ages and
determine its shared Omen with the shorter command:

```shell
./gradlew :dairn-gm-tui:run --args="--lang ru group new \
  --module great-steppe \
  --member Aibek:25 \
  --member Bayan:31 \
  --seed 42"
```

A one-character group is valid in both forms. If several characters share the youngest age, the
process asks which one determines the group Omen.

## Build and test

Run all automated tests:

```shell
./gradlew test
```

Build all modules:

```shell
./gradlew build
```

GitHub Actions performs the same test suite and validates the Gradle Wrapper for pushes to `main`
and for pull requests.

## Rules resources and localization

Rules structure is stored as JSON under each ruleset's `src/main/resources` directory. JSON files
contain stable identifiers, dice values, and localization keys rather than embedded table prose.
Localized text is stored in domain-specific `.properties` catalogs.

Cairn 2e provides English defaults and Russian catalogs. Great Steppe currently has canonical
Russian rules text only. When `en` or `kk` is requested for Great Steppe rules content, the module
falls back to Russian rather than presenting an unapproved translation. Interface messages remain
available in all three languages.

Great Steppe resources are pinned to source revision
`3fabe55fa366c3706a5c54614b1682bd2f7cb5d6`. Cairn and Russian translation sources are also pinned;
see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Known limitations

- This is an early preview with no API compatibility guarantee.
- There is no persistence, server, graphical interface, AI, Player/Session model, or complex world
  state.
- The TUI is a development shell, not the final game-master interface.
- Great Steppe English and Kazakh rules catalogs are not yet available.
- Detailed Great Steppe procedures for consuming water, food, and fire are intentionally not
  implemented because the pinned source text does not define them yet.
- Survival and inventory effects are available through the engine API but do not yet have dedicated
  TUI commands.

## Licensing and attribution

Software source code is copyright © 2026 Andrey Svininykh and licensed under the
[Apache License 2.0](LICENSE). See [NOTICE](NOTICE).

Textual game materials and table content are copyright © 2026 Andrey Svininykh and licensed under
[CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/). Cairn-derived materials retain
attribution to *Cairn Second Edition* author Yochai Gal. Russian Cairn text is based on the official
translation by Hex Cat studio.

See [CONTENT_LICENSE.md](CONTENT_LICENSE.md) and
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for license scope, source links, modifications, and
pinned revisions.
