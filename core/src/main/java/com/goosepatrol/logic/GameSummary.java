package com.goosepatrol.logic;

/**
 * Snapshot of a finished session, shown on the end screen. Keeping it separate means the end
 * screen doesn't depend on the live {@link GameWorld}.
 *
 * @param won             whether the player won
 * @param elapsedSeconds  how long the session lasted (paused time excluded)
 * @param itemsUsed       number of items used
 * @param eventsTriggered number of hidden events triggered
 * @param geeseDispersed  number of geese dispersed
 * @param geeseToWin      number of dispersed geese needed to win
 */
public record GameSummary(boolean won, float elapsedSeconds, int itemsUsed, int eventsTriggered,
                          int geeseDispersed, int geeseToWin) {
}
