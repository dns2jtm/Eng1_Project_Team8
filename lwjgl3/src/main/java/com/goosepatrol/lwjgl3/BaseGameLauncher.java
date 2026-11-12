package com.goosepatrol.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * REVIEW BRANCH ONLY: launches the base game from main ({@code game.GoosePatrol}, commit 88ede63)
 * so the team can compare both implementations side by side. Run with {@code ./gradlew lwjgl3:runBaseGame}.
 * Delete this class once the team has chosen which implementation to keep.
 */
public class BaseGameLauncher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // macOS support, as in Lwjgl3Launcher.
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Goose Patrol - base game (main)");
        configuration.useVsync(true);
        configuration.setWindowedMode(800, 600);
        new Lwjgl3Application(new game.GoosePatrol(), configuration);
    }
}
