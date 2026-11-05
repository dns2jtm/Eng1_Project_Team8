package com.goosepatrol.item;

import com.goosepatrol.entity.Goose;

/**
 * An item that scares geese, so every goose in range runs away from the player for
 * {@code scareSeconds}. It doesn't feed them, so they come back afterwards.
 */
public class ScareItem extends Item {

    /** Range of the starting "Whistle" item. */
    public static final float WHISTLE_RANGE = 170f;
    /** Scare duration of the starting "Whistle" item. */
    public static final float WHISTLE_SCARE_SECONDS = 4f;

    private final float scareSeconds;

    /**
     * @param scareSeconds how long affected geese flee; must be positive
     */
    public ScareItem(String name, float range, float scareSeconds) {
        super(name, range);
        if (scareSeconds <= 0f) {
            throw new IllegalArgumentException("scareSeconds must be positive, was " + scareSeconds);
        }
        this.scareSeconds = scareSeconds;
    }

    /** Creates the "Whistle" starting item. */
    public static ScareItem whistle() {
        return new ScareItem("Whistle", WHISTLE_RANGE, WHISTLE_SCARE_SECONDS);
    }

    @Override
    public void applyTo(Goose goose) {
        goose.scare(scareSeconds);
    }

    @Override
    public String getEffectVerb() {
        return "scared";
    }

    @Override
    public String getDescription() {
        return getName() + ": scares nearby geese away for a few seconds.";
    }

    public float getScareSeconds() {
        return scareSeconds;
    }
}
