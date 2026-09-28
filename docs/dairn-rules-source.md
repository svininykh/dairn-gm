# Ability Score source baseline — Issue #3

Read on 2026-09-28 from the sibling `dairn-great-steppe` working tree:

- `dairn/player-book/character/character-in-play-master.md`, explicitly marked
  as the Chapter 3 working master. SHA-256:
  `80c337951cd891f4add9c09cf4cb5b4267980a9768b882ba6f090978500cb717`.
- `dairn/player-book/playing-in-the-world/game-procedures.md`, section
  “Проверки и последствия”, explicitly referenced by that master. SHA-256:
  `4c3c84fb3eb84b9733761590ddba5a701d6214d1aec01180c102c5ea6b99b4f2`.

These are local, unpublished working texts, not the older GitHub chapter.
The source repository HEAD was `4a8964e31f87eaf53c44e6fb8708d00a68e59f91`,
but the master was untracked and procedures modified, so that commit alone
does not identify this baseline. The hashes above identify the texts read.
No source-book files were changed or copied into the rules artifact.

The master establishes Ability Scores STR / DEX / WIL, uncertainty and meaningful
risk as prerequisites, and no numeric Experience bonus. The linked procedure
specifies one d20, success at or below the current score, automatic success on 1
and automatic failure on 20. There is no existing check implementation in Engine
to conflict with this rule.

The primitive only resolves an already designated check. Selection of the actor
in opposed/group actions, eligibility to act, consequences, damage, recovery,
and Die of Fate remain outside this change. It accepts an integer comparison
score without inventing a legal character-score range; that is not a character
validator. In particular, resolving a numeric check at zero does not allow a
dead or incapacitated character to act.

Verification covers all three abilities around the threshold, natural 1 and 20,
scores outside the creation range, invalid d20 inputs, malformed injected dice,
one-roll consumption, seeded repeatability, and an independent published-artifact
consumer of DEX/WIL with neither Great Steppe nor Cairn on its classpath.

## Great Steppe integration

The subsequent integration adds `great-steppe → dairn-rules` and character
extension functions that map the existing STR/DEX/WIL list to the shared enum.
No comparison logic is duplicated and character creation is unchanged. The
dependency is exported with `api` because the extension signatures expose common
rules types. The external Great Steppe smoke test verifies this publication path.
The reverse dependency does not exist: the neutral rules consumer still runs
without either setting module. Cairn remains unchanged.
