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
- Four **hidden events** are somewhere on campus, two good and two bad. They are invisible until
  you walk into them.

## Assessment 1 scope

Requirement IDs are those in Req1. The full matrix (requirement → design → code → tests) is on
the website's traceability page (`website/traceability.html`).

| Requirements (A1) | Where in the code |
| --- | --- |
| FR_INITIAL_INVENTORY, FR_USE_ITEM, FR_INVENTORY_DISPLAY | `item.FoodItem` (Bread), `item.ScareItem` (Whistle), `item.Inventory`, `logic.GameWorld#useSelectedItem`, `screen.Hud` |
| FR_EVENT_HIDDEN, FR_EVENT_TRIGGER, FR_EVENT_EFFECT, FR_EVENT_COUNT | `event.HiddenEvent` and four subclasses: `PicnicBasketEvent`, `LuckyShortcutEvent` (positive); `GooseNestEvent`, `SlipperyPathEvent` (negative) |
| FR_TIMER, FR_TIMER_EXPIRE, FR_START_PAUSED, FR_PAUSE | `logic.GameTimer`, `logic.GameState`, `logic.GameWorld#update` / `#togglePause` |
| FR_ITEM_COUNTER, FR_EVENT_COUNTER | `logic.GameStats`, drawn by `screen.Hud` |
| FR_PLAYER_MOVE, FR_MAP_BOUNDS | `entity.Player`, `entity.GameMap#createCampus`, `entity.Obstacle` |
| FR_GEESE_SPAWN | `entity.Goose`, `logic.GameWorld#updateSpawning` |
| FR_END_SCREEN (result) | `screen.EndScreen` |

Implemented early (scoped A2 in Req1): FR_GEESE_BLOCK, FR_GEESE_DISPERSE, FR_WIN_CONDITION and
FR_CAMPUS_THEME. Balance values all live in `GameConfig`.

## Project structure

```
core/      game logic and screens (platform independent)
  src/main/java/com/goosepatrol/
    GoosePatrolGame.java   application entry point, screen switching
    GameConfig.java        tunable constants
    logic/   GameWorld, GameState, GameTimer, GameStats, GameSummary, WorldMessage
    entity/  Entity, Player, Goose, GooseState, GameMap, Obstacle
    item/    Item, FoodItem, ScareItem, Inventory
    event/   HiddenEvent, PicnicBasketEvent, GooseNestEvent, LuckyShortcutEvent, SlipperyPathEvent
    screen/  GameScreen, WorldRenderer, Hud, EndScreen
  src/test/java/           JUnit 5 tests (unit tests plus a headless libGDX smoke test)
lwjgl3/    desktop launcher (LWJGL3 backend)
architecture/  UML diagrams: v1 (before implementation), v2 (current), and what changed
website/   team website (published to GitHub Pages by CI)
assets/    runtime assets (none needed yet: everything is drawn with shapes)
.github/   CI and release workflows, pull request template
```

The logic layer (`logic`, `entity`, `item`, `event`) has no rendering or input code, so all of the
gameplay can be unit-tested without a window.

## Building and running

The game runs on Java 17 or later (CR_JAVA); the code is compiled with `--release 17`. The Gradle
build itself runs on JDK 21 (set in `gradle/gradle-daemon-jvm.properties`) and downloads that JDK
automatically if it isn't installed. Gradle downloads everything else too.

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
- **Website** (`.github/workflows/website.yml`): every push to `main` publishes the team website
  in `website/` to GitHub Pages, together with the architecture diagrams and the latest test and
  coverage reports: <https://soham-apte.github.io/Eng1_Project_Team8/>. A repository admin must
  first set *Settings → Pages → Source* to **GitHub Actions** (one-time setup).

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
