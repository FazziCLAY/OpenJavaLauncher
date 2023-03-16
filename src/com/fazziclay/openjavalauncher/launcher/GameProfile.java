package com.fazziclay.openjavalauncher.launcher;

import java.io.File;
import java.util.UUID;

public class GameProfile {
    private UUID profileUUID;
    private String versionId;
    private String jvmArguments;
    private String jvmPath;
    private File gameDirectory;
    private int windowWidth;
    private int windowHeight;
    private String name;
    private boolean downloadMissingAssets;

    public String getVersionId() {
        return versionId;
    }

    public GameProfile(UUID profileUUID, String name, String versionId, String jvmPath, String jvmArguments, File gameDirectory, int windowWidth, int windowHeight, boolean downloadMissingAssets) {
        this.profileUUID = profileUUID;
        this.name = name;
        this.versionId = versionId;
        this.jvmPath = jvmPath;
        this.jvmArguments = jvmArguments;
        this.gameDirectory = gameDirectory;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.downloadMissingAssets = downloadMissingAssets;
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

    public String getName() {
        return name;
    }

    public boolean isDownloadMissingAssets() {
        return downloadMissingAssets;
    }

    public void setDownloadMissingAssets(boolean downloadMissingAssets) {
        this.downloadMissingAssets = downloadMissingAssets;
    }

    public String getJVMPath() {
        return jvmPath;
    }
}
