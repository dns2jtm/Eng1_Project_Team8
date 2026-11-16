# Goose Patrol

[![CI](https://github.com/dns2jtm/Eng1_Project_Team8/actions/workflows/ci.yml/badge.svg)](https://github.com/dns2jtm/Eng1_Project_Team8/actions/workflows/ci.yml)

A single-player 2D top-down game built for ENG1 (Cohort 2, Team 8). You play as a University of York
student surrounded by hungry geese on campus, and have to disperse 8 geese before the 5-minute timer
runs out. Built in Java 17 with [libGDX](https://libgdx.com/) and generated from the
[gdx-liftoff](https://github.com/libgdx/gdx-liftoff) template.

## Controls and gameplay

| Key | Action |
| --- | --- |
| `ENTER` / `P` / `ESC` | Start, pause and resume (the game starts paused) |
| `W A S D` / arrow keys | Move around campus |
| `SPACE` | Use the selected item on geese within the range circle |
| `1`-`9`, `Q` / `E` / `TAB` | Select an inventory slot |
| `R` / `ENTER` (end screen) | Start a new game (`ESC` to quit) |

- **Bread** feeds every goose inside the faint white range circle. Orange dots above each goose show
  how much food it still needs; once full, the goose flies away.
- **Whistle** scares nearby geese away for a few seconds without feeding them.
- Items are still used up if activated with no geese in range, so wait until geese are close.
- Geese slow you down when they crowd around you, and new geese spawn periodically up to a cap.
- Four **hidden events** (two positive, two negative) are placed around the campus walkways and lawns.
  They stay invisible until walked over, trigger at most once, and leave a coloured marker afterwards.

## Assessment 1 scope

Requirement IDs match `Req1.pdf`. The full traceability table linking requirements, UML diagrams,
classes and tests is on `website/traceability.html`.

| Requirements (A1) | Main classes |
| --- | --- |
| `FR_INITIAL_INVENTORY`, `FR_USE_ITEM`, `FR_INVENTORY_DISPLAY` | `item.FoodItem` (Bread), `item.ScareItem` (Whistle), `item.Inventory`, `logic.GameWorld#useSelectedItem`, `screen.Hud` |
| `FR_EVENT_HIDDEN`, `FR_EVENT_TRIGGER`, `FR_EVENT_EFFECT`, `FR_EVENT_COUNT` | `event.HiddenEvent` and four subclasses: `PicnicBasketEvent`, `LuckyShortcutEvent`, `GooseNestEvent`, `SlipperyPathEvent` |
| `FR_TIMER`, `FR_TIMER_EXPIRE`, `FR_START_PAUSED`, `FR_PAUSE` | `logic.GameTimer`, `logic.GameState`, `logic.GameWorld#update`, `logic.GameWorld#togglePause` |
| `FR_ITEM_COUNTER`, `FR_EVENT_COUNTER` | `logic.GameStats`, displayed in `screen.Hud` |
| `FR_PLAYER_MOVE`, `FR_MAP_BOUNDS` | `entity.Player`, `entity.GameMap#createCampus`, `entity.Obstacle` |
| `FR_GEESE_SPAWN` | `entity.Goose`, `logic.GameWorld#updateSpawning` |
| `FR_END_SCREEN` (result) | `screen.EndScreen`, `logic.GameSummary` |

Four requirements scoped to Assessment 2 in `Req1.pdf` are also implemented early so the game has a
complete playable loop: `FR_CAMPUS_THEME`, `FR_GEESE_BLOCK`, `FR_GEESE_DISPERSE` and `FR_WIN_CONDITION`.
Gameplay constants are grouped in `GameConfig`.

## Project structure

```text
core/          platform-independent game logic and libGDX screens
  src/main/java/com/goosepatrol/
    GoosePatrolGame.java   libGDX Game entry point and screen switching
    GameConfig.java        gameplay and window constants
    logic/     GameWorld, GameState, GameTimer, GameStats, GameSummary, WorldMessage
    entity/    Entity, Player, Goose, GooseState, GameMap, Obstacle
    item/      Item, FoodItem, ScareItem, Inventory
    event/     HiddenEvent, PicnicBasketEvent, GooseNestEvent, LuckyShortcutEvent, SlipperyPathEvent
    screen/    GameScreen, WorldRenderer, Hud, EndScreen
  src/test/java/           88 JUnit 5 tests (domain unit tests + headless libGDX session tests)
lwjgl3/        desktop launcher and bundled licence files (LWJGL3 backend)
architecture/  PlantUML diagrams: v1 (pre-implementation) and v2 (current)
deliverables/  HTML sources and rendered PDFs for Assessment 1 reports
website/       project website published to GitHub Pages
.github/       GitHub Actions workflows (CI, Release, Website) and PR template
```

The gameplay packages (`logic`, `entity`, `item`, `event`) do not import `Gdx.graphics` or `Gdx.input`,
so game rules run directly under JUnit 5 without opening an OpenGL window.

## Building and running

The game targets Java 17 (`--release 17`, `CR_JAVA`). Gradle downloads dependencies automatically.

```sh
./gradlew lwjgl3:run      # launch the desktop game
./gradlew test            # run JUnit 5 tests and generate JaCoCo coverage in core/build/reports/jacoco/test/html
./gradlew lwjgl3:jar      # build standalone runnable JAR in lwjgl3/build/libs/GoosePatrol-1.0.0.jar
java -jar lwjgl3/build/libs/GoosePatrol-1.0.0.jar
```

On macOS, if `./gradlew` reports "operation not permitted", run `sh ./gradlew ...` or clear the
quarantine attribute with `xattr -d com.apple.quarantine gradlew`.

## Continuous integration and website

- **CI** (`.github/workflows/ci.yml`) runs `./gradlew test lwjgl3:jar` on Linux, Windows and macOS on
  every push and pull request into `main`, uploading the JUnit report, JaCoCo report and built JAR.
- **Releases** (`.github/workflows/release.yml`) builds and attaches `GoosePatrol.jar` whenever a version
  tag such as `v1.0.0` is pushed.
- **Website** (`.github/workflows/website.yml`) deploys `website/` along with the latest test/coverage
  reports and UML diagrams to GitHub Pages (<https://dns2jtm.github.io/Eng1_Project_Team8/>).
  To build and preview the site locally:
  `./gradlew test && bash scripts/build-website.sh && python3 -m http.server -d _site 8000`.

## Third-party libraries and licences

| Library | Purpose | Licence | Bundled in JAR |
| --- | --- | --- | --- |
| libGDX 1.14.2 | Game framework and default window icons | Apache 2.0 | Yes |
| `StartupHelper` (`gdx-liftoff`, damios) | Desktop JVM launch helper | Apache 2.0 | Yes |
| LWJGL 3.4.3 (GLFW, jemalloc, stb natives) | Desktop windowing and OpenGL | BSD 3-Clause (natives: zlib, BSD 2-Clause, MIT) | Yes |
| OpenAL Soft, JLayer, JOrbis | Audio libraries bundled by libGDX (unused in A1) | LGPL 2.0+ / 2.1 | Yes |
| Liberation Sans (`lsans-15`) | Built-in libGDX bitmap font | SIL Open Font License 1.1 | Yes |
| JUnit 5, JaCoCo | Automated testing and code coverage | EPL 2.0 | No |
| Gradle, Construo, PlantUML | Build and diagram tools | Apache 2.0 / GPL | No |

`GoosePatrol-1.0.0.jar` includes `THIRD-PARTY-NOTICES.txt` and full licence texts under `licenses/`
(`lwjgl3/src/main/resources/`). Campus buildings, paths, the lake, the player and geese are drawn with
libGDX `ShapeRenderer`, so no external sprite sheets or audio files are bundled in Assessment 1.
