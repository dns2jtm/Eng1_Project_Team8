# Architecture

PlantUML sources (`.puml`) and rendered diagrams (`.png`, plus `.svg` for v2) for Goose Patrol.

| Version | Folder       | What it is                                                     |
| ------- | ------------ | -------------------------------------------------------------- |
| v1      | [`v1/`](v1/) | The design produced **before** implementation                  |
| v2      | [`v2/`](v2/) | Updated to match the **Assessment 1 implementation** (current) |

Both versions are kept as evidence of how the design evolved. Do not edit `v1/`; make new changes  
in `v2/`, or start a `v3/` for Assessment 2.

## Diagrams

Requirement IDs are those in **Req1** (UR\_, FR\_, QR_ and CR_ prefixes).

| # | Diagram               | UML type                    | Main requirements                                                                                                                           |
| - | --------------------- | --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| 1 | Component             | Structural (component)      | all; QR_EXTENSIBILITY (separate logic and presentation layers)                                                                              |
| 2 | Class                 | Structural (class)          | all; QR_EXTENSIBILITY (`Item` and `HiddenEvent` subclasses)                                                                                 |
| 3 | Game state machine    | Behavioural (state machine) | FR_START_PAUSED, FR_PAUSE, FR_TIMER_EXPIRE, FR_WIN_CONDITION, FR_END_SCREEN                                                                 |
| 4 | Goose state machine   | Behavioural (state machine) | FR_GEESE_SPAWN, FR_GEESE_BLOCK, FR_USE_ITEM, FR_GEESE_DISPERSE                                                                              |
| 5 | Use an item           | Behavioural (sequence)      | FR_USE_ITEM, FR_ITEM_COUNTER, FR_GEESE_DISPERSE, FR_WIN_CONDITION                                                                           |
| 6 | One frame of gameplay | Behavioural (sequence)      | FR_PAUSE, FR_TIMER, FR_EVENT_TRIGGER, FR_EVENT_EFFECT, FR_EVENT_COUNTER, FR_GEESE_SPAWN, FR_GEESE_BLOCK, FR_TIMER_EXPIRE, QR_TIMER_ACCURACY |

**Language and tools:** UML 2 (component, class, state machine and sequence diagrams), written as  
text in [PlantUML](https://plantuml.com/) and rendered with Graphviz. Because the diagrams are  
plain text, they live in git alongside the code and can be reviewed in pull requests like code.

To re-render after editing (needs Java, `plantuml.jar` and Graphviz):

```sh
cd architecture/v2
java -jar plantuml.jar -tpng *.puml
java -jar plantuml.jar -tsvg *.puml
```

## Changes from v1 to v2

| #  | Change                                                                                                                                                                                               | Reason                                                                                                                                                                      |
| -- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1  | `GameScreen` drawing split into `WorldRenderer` (map and entities) and `Hud` (overlay)                                                                                                               | Keeps each class to one job (high cohesion); `GameScreen` only handles input and the frame loop                                                                             |
| 2  | `GoosePatrolGame` added as the application class that switches screens                                                                                                                               | libGDX needs a `Game` subclass to manage screens; it also owns shared rendering resources                                                                                   |
| 3  | `Goose.chase(...)` and `Player.move(...)` take the `GameMap` as a parameter                                                                                                                          | Entities check collisions against the map without holding a reference to it (lower coupling, easier to test)                                                                |
| 4  | `Entity` gained a `radius` and a shared `moveBy` with per-subclass `canOccupy`                                                                                                                       | Circle collision for everything; geese can swim across the lake but the player can't                                                                                        |
| 5  | `Obstacle` added (buildings and water) inside `GameMap`                                                                                                                                              | Gives the map clear limits and campus features; data-driven, so more obstacles are easy to add                                                                              |
| 6  | `GooseState` enum replaced the `distracted : Boolean` flag; `Distracted` split into Eating / Fleeing                                                                                                 | Feeding and scaring behave differently; an explicit state is clearer than combining flags                                                                                   |
| 7  | `HiddenEvent.trigger(world)` (public, final) calls the protected `apply(world)`                                                                                                                      | Template method: "fires only once" is enforced in one place, not in every subclass                                                                                          |
| 8  | Example classes replaced by concrete ones: `PositiveEvent`/`NegativeEvent` → `PicnicBasketEvent`/`GooseNestEvent`                                                                                    | v1 used placeholders; v2 names the actual Assessment 1 events                                                                                                               |
| 9  | `GameStats` also counts dispersed geese; `GameSummary` record added for the end screen                                                                                                               | Drives the win condition; `EndScreen` doesn't depend on the live `GameWorld`                                                                                                |
| 10 | Win condition and goose dispersal implemented (v1 marked them "A2")                                                                                                                                  | Without them the game had no way to win; both were already in the v1 state machines                                                                                         |
| 11 | `GameConfig` added for all balance constants                                                                                                                                                         | One place to tune difficulty; documentation can point to a single source                                                                                                    |
| 12 | `WorldMessage` added for on-screen feedback                                                                                                                                                          | Players need to see what an item or event did                                                                                                                               |
| 13 | `GameWorld.update` advances the timer by the real frame time, then simulates movement in steps of at most 0.05 s; `GameTimer` tracks play time separately and supports `removeTime`                  | QR_TIMER_ACCURACY: capping the frame time made the timer run slow when the game lagged; small steps keep collisions stable and stop fast movement skipping events           |
| 14 | Two more hidden events, `LuckyShortcutEvent` and `SlipperyPathEvent`, from the base game on `main`                                                                                                   | Added as new subclasses with no change to existing event classes (shows QR_EXTENSIBILITY); four events in total (FR_EVENT_COUNT)                                            |
