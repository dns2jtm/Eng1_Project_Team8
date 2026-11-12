# Side-by-side comparison (review branch)

This branch contains **both** Assessment 1 implementations so the team can try them and decide
which to build on. Neither has been merged into `main`. This file and `BaseGameLauncher` only
exist on this branch.

| | Base game (`main`, commit `88ede63`, Karthik) | Feature branch (`feature/a1-implementation`, Daniel) |
| --- | --- | --- |
| Run it | `./gradlew lwjgl3:runBaseGame` | `./gradlew lwjgl3:run` |
| Code | `core/src/main/java/com/mygame/game/` (package `game`) | `core/src/main/java/com/goosepatrol/` |
| Size | 7 classes, ~270 lines | 25 classes in 6 packages, ~2,300 lines (including Javadoc comments) |
| Window | 800×600 | 1280×720, resizable |
| Timer, starts paused, pause (P) | Yes | Yes (also ESC; auto-pauses when the window loses focus) |
| Items | Food (key 1) removes goose 1; Whistle (key 2) removes goose 2; one use each | Bread ×4 feeds geese in range (full geese fly away); Whistle ×3 scares geese in range |
| Hidden events | +20 s "lucky shortcut"; −15 s "slippery path" | Picnic basket (+2 Bread); goose nest (+3 geese) |
| Counters | Items used, events triggered | Items used, events triggered, geese dispersed |
| Geese | 2 at fixed positions, chasing | 5 at the start, +1 every 20 s (max 9), chase and crowd the player, hunger levels |
| Map | Rectangle with edges | Campus with 5 buildings, a lake and paths |
| Win / lose | Both geese gone / timer runs out | 8 geese dispersed / timer runs out; end screen with stats and restart |
| Tests | None | 80 JUnit tests (about 99% line coverage of the game logic) |
| Architecture diagrams | – | v2 diagrams matching the code; v1 kept |
| CI / website | – | GitHub Actions (tests on 3 OSes, release JAR) and a Pages website |

Notes:
- The launcher on `main` still starts the template `Main` class, so `main` currently shows the
  libGDX logo rather than the base game. `runBaseGame` on this branch launches it directly.
- The base game's time-changing events (+20 s / −15 s) are a good idea that the feature branch
  doesn't have. They would be easy to add as two more `HiddenEvent` subclasses, and Assessment 2
  needs five events anyway.

Delete this branch once the team has decided.
