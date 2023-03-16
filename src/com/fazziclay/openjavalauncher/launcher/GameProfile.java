package com.fazziclay.openjavalauncher.launcher;

import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

public class GameProfile {
    private UUID profileUUID;
    private String versionId;
    private String jvmArguments;
    private File gameDirectory;
    private int windowWidth;
    private int windowHeight;

    public String getVersionId() {
        return versionId;
    }

    public GameProfile(UUID profileUUID, String versionId, String jvmArguments, File gameDirectory, int windowWidth, int windowHeight) {
        this.profileUUID = profileUUID;
        this.versionId = versionId;
        this.jvmArguments = jvmArguments;
        this.gameDirectory = gameDirectory;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public UUID getProfileUUID() {
        return profileUUID;
    }

    public String getJVMArguments() {
        return jvmArguments;
    }

    public File getGameDirectory() {
        return gameDirectory;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public int getWindowWidth() {
        return windowWidth;
    }
}
