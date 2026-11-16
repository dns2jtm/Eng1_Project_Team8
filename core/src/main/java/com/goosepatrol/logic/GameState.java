package com.goosepatrol.logic;

/**
 * Top-level states of a game session, as in the game state machine diagram
 * (architecture/v2/3-state-game.puml).
 */
public enum GameState {
    /** Nothing updates. The game starts in this state. [FR_START_PAUSED, FR_PAUSE] */
    PAUSED,
    /** The timer, geese and player update every frame. */
    RUNNING,
    /** The player dispersed enough geese. [FR_WIN_CONDITION] */
    WON,
    /** The 5-minute timer ran out. [FR_TIMER_EXPIRE] */
    LOST;

    /** @return {@code true} for the end states (WON or LOST) */
    public boolean isFinished() {
        return this == WON || this == LOST;
    }
}
