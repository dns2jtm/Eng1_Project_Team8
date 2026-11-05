# Goose Patrol

[![CI](https://github.com/soham-apte/Eng1_Project_Team8/actions/workflows/ci.yml/badge.svg)](https://github.com/soham-apte/Eng1_Project_Team8/actions/workflows/ci.yml)

A single-player top-down game for the ENG1 product brief. You play a University of York student
surrounded by hungry geese on campus, and you have to disperse them before the 5-minute timer
runs out. Built with [libGDX](https://libgdx.com/) and originally generated with
[gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

## How to play

| Key | Action |
| --- | --- |
| `P` / `ESC` | Start, pause and resume (the game starts paused) |
| `W A S D` / arrow keys | Move |
| `SPACE` | Use the selected item on every goose within range (the faint circle) |
| `1`-`9`, `Q` / `E` | Choose an item |
| `R` (end screen) | Play again |

- **Bread** feeds every goose in range. Orange dots above a goose show how hungry it is. When a
  goose is full it flies away.
- **Whistle** scares nearby geese away for a few seconds but doesn't feed them.
- Geese slow you down when they crowd you, and more geese arrive over time.
- **Win** by dispersing 8 geese. **Lose** if the 5-minute timer runs out.
- Two **hidden events** are somewhere on campus. They are invisible until you walk into them.

## Assessment 1 scope

| Brief requirement (A1) | Where in the code |
| --- | --- |
| Two starting inventory items | `item.FoodItem` (Bread), `item.ScareItem` (Whistle), `logic.GameWorld#createStartingInventory` |
| Two hidden events | `event.PicnicBasketEvent` (positive), `event.GooseNestEvent` (negative) |
| 5-minute timer; starts paused; pause at any time | `logic.GameTimer`, `logic.GameState`, `logic.GameWorld#togglePause` |
| Counter of items used and events triggered | `logic.GameStats`, drawn by `screen.Hud` |
| Simple map with clear limits | `entity.GameMap#createCampus`, `entity.Obstacle` |

Balance values all live in `GameConfig`.

## Project structure

```
core/      game logic and screens (platform independent)
  src/main/java/com/goosepatrol/
    GoosePatrolGame.java   application entry point, screen switching
    GameConfig.java        tunable constants
    logic/   GameWorld, GameState, GameTimer, GameStats, GameSummary, WorldMessage
    entity/  Entity, Player, Goose, GooseState, GameMap, Obstacle
    item/    Item, FoodItem, ScareItem, Inventory
    event/   HiddenEvent, PicnicBasketEvent, GooseNestEvent
    screen/  GameScreen, WorldRenderer, Hud, EndScreen
  src/test/java/           JUnit 5 tests (unit tests plus a headless libGDX smoke test)
lwjgl3/    desktop launcher (LWJGL3 backend)
architecture/  PlantUML sources and rendered diagrams
assets/    runtime assets (none needed yet: everything is drawn with shapes)
.github/   CI and release workflows, pull request template
```

The logic layer (`logic`, `entity`, `item`, `event`) has no rendering or input code, so all of the
gameplay can be unit-tested without a window.

## Building and running

Requires JDK 21 or later. Gradle downloads everything else.

```sh
./gradlew lwjgl3:run      # run the game
./gradlew test            # run all tests; coverage report in core/build/reports/jacoco/test/html
./gradlew lwjgl3:jar      # build the runnable JAR, lwjgl3/build/libs/GoosePatrol-1.0.0.jar
java -jar lwjgl3/build/libs/GoosePatrol-1.0.0.jar
```

On macOS, if `./gradlew` says "operation not permitted", run `sh ./gradlew ...` instead, or remove
the download quarantine flag with `xattr -d com.apple.quarantine gradlew`.

## Continuous integration and releases

- **CI** (`.github/workflows/ci.yml`) runs on every push and on pull requests into `main`. It runs
  all tests and builds the JAR on Linux, Windows and macOS, then uploads the test report, the
  coverage report and the JAR as artifacts. A coverage table appears on each run's summary page.
- **Releases** (`.github/workflows/release.yml`): push a tag such as `v1.0.0` to publish the JAR as
  a GitHub Release. The latest JAR is always at
  <https://github.com/soham-apte/Eng1_Project_Team8/releases/latest/download/GoosePatrol.jar>.

```sh
git tag v1.0.0
git push origin v1.0.0
```

## Workflow

Work happens on feature branches and is merged into `main` through pull requests. CI must pass and
another team member must review before merging (see `.github/pull_request_template.md`).

## Third-party libraries

| Library | Use | Licence |
| --- | --- | --- |
| libGDX 1.14 | Game framework | Apache 2.0 |
| LWJGL 3 | Desktop backend (through libGDX) | BSD 3-Clause |
| JUnit 5 | Testing (not shipped in the JAR) | EPL 2.0 |
| construo (Gradle plugin) | Optional native packaging | Apache 2.0 |

No third-party art, audio or font assets are used. The game uses libGDX's built-in default font,
and the window icons are the libGDX logo from the project template (Apache 2.0).
