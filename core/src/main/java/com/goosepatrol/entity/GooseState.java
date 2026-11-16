package com.goosepatrol.entity;

/**
 * States of a {@link Goose}, as in the goose state machine diagram (architecture/v2/4-state-goose.puml).
 */
public enum GooseState {
    /** Walking towards the player, demanding food. */
    CHASING,
    /** Busy eating, or running away after being scared, for a limited time. */
    DISTRACTED,
    /** Fed enough; the goose leaves and is removed from the map. [FR_GEESE_DISPERSE] */
    DISPERSED
}
