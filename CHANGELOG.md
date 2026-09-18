# Changelog

All notable changes to DAIRN GM will be documented in this file.

The project follows [Semantic Versioning](https://semver.org/) after the `0.1.0` preview line. While
the project is in preview, its public API may change between releases.

## 0.1.0-preview.4 — 2026-09-18

### Added

- Automatic Great Steppe character generation using the existing rules process and injectable dice.
- Support for temporarily unnamed heroes and unset reader-authored life-path details.

### Changed

- Initial group creation displays unnamed heroes without assigning a domain name.

## 0.1.0-preview.1 — Unreleased

### Added

- Ruleset-neutral Kotlin Engine with dice, tables, rules, effects, inventory load, module metadata,
  and a stateless interactive process protocol.
- Cairn Second Edition character-creation module with English and Russian rules resources.
- DAIRN: Great Steppe character and initial-group creation, structured starting inventory, shared
  group Omen, and confirmed survival and Fatigue effects.
- Development TUI with English, Russian, and Kazakh interface messages.
- Maven publication for consuming `dairn-gm-engine` through JitPack.
- Independent external-module smoke test and GitHub Actions validation.

### Limitations

- Process state is transient and opaque; persistence and serialization are not part of Engine 0.1.
- Third-party module JAR discovery is not implemented.
- The TUI is a development shell rather than the final game-master interface.
