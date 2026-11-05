package com.goosepatrol.logic;

import java.util.Objects;

/**
 * A short feedback message for the player, such as the result of using an item or a hidden event.
 *
 * @param text the message text
 * @param tone how the message should be styled
 */
public record WorldMessage(String text, Tone tone) {

    /** Visual tone of a message. */
    public enum Tone {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    public WorldMessage {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(tone, "tone");
    }
}
