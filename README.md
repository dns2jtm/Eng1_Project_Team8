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
- **Website** (`.github/workflows/website.yml`): publishes the team website in `website/` to
  GitHub Pages, together with the architecture diagrams and the latest test and coverage reports:
  <https://soham-apte.github.io/Eng1_Project_Team8/>. The repository stays **private until
  submission**, so the workflow skips itself while the repo is private. To preview locally:
  `./gradlew test && scripts/build-website.sh && python3 -m http.server -d _site 8000`.

### At submission

1. Merge the final work into `main` and tag the release: `git tag v1.0.0 && git push origin v1.0.0`
   (on a private repo the Release workflow still runs, but only collaborators can download the JAR
   until the repo is public).
2. Make the repository public (Settings → General → Danger Zone → Change visibility).
3. Settings → Pages → Source: **GitHub Actions**, then Actions → Website → **Run workflow**.
4. Check that the website, the JAR download link and the repository link all work in a private
   browser window, since that's what markers and other teams will see.

```sh
git tag v1.0.0
git push origin v1.0.0
```

## Workflow

Work happens on feature branches and is merged into `main` through pull requests. CI must pass and
another team member must review before merging (see `.github/pull_request_template.md`).

## Third-party libraries

| Library | Use | Licence | In the JAR |
| --- | --- | --- | --- |
| libGDX 1.14 | Game framework | Apache 2.0 | Yes |
| LWJGL 3.4 (with GLFW, jemalloc, stb natives) | Desktop backend (through libGDX) | BSD 3-Clause (natives: zlib, BSD 2-Clause, MIT) | Yes |
| OpenAL Soft, JLayer, JOrbis | Audio, bundled by libGDX (no sound used yet) | LGPL 2.0+ / 2.1 | Yes |
| Liberation Sans (libGDX default font) | On-screen text | SIL OFL 1.1 | Yes |
| JUnit 5, JaCoCo | Testing and coverage | EPL 2.0 | No |
| Gradle, construo | Build and packaging | Apache 2.0 | No |

The JAR includes `THIRD-PARTY-NOTICES.txt` and the full licence texts (`licenses/`), as these
licences require (source: `lwjgl3/src/main/resources/`). No third-party art or audio assets are
used, and the window icons are the libGDX logo from the project template. Any future assets must be
CC0, CC BY or OFL, and must be added to the notices file (CR_LICENCES).
