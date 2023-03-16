package com.fazziclay.openjavalauncher.operation;

import com.fazziclay.openjavalauncher.OpenJavaLauncher;
import com.fazziclay.openjavalauncher.launcher.GameProfile;
import com.fazziclay.openjavalauncher.launcher.UserProfile;
import com.fazziclay.openjavalauncher.launcher.launch.MinecraftInstance;

import java.io.File;
import java.util.Objects;

import static com.fazziclay.openjavalauncher.util.Lang.t;

public class MinecraftLaunchOperation extends Operation {
    private static final String TAG = "MinecraftLaunchOperation";
    private final GameProfile gameProfile;
    private final UserProfile userProfile;
    private short percentage = -1;
    private String state;
    private MinecraftInstance minecraftInstance;

    public MinecraftLaunchOperation(GameProfile gameProfile, UserProfile userProfile, File versionsDir, File librariesDir, File assetsDir) {
        Objects.requireNonNull(gameProfile, "gameProfile");
        Objects.requireNonNull(userProfile, "userProfile");
        this.gameProfile = gameProfile;
        this.userProfile = userProfile;
        this.minecraftInstance = new MinecraftInstance(gameProfile, userProfile, OpenJavaLauncher.getInstance().getVersionManifest(), versionsDir, librariesDir, assetsDir);
    }

    public void setPercentage(short percentage) {
        this.percentage = percentage;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String getTitle() {
        return t("operation.minecraftLaunch.title", gameProfile.getVersionId(), userProfile.getNickname());
    }

    @Override
    public String getDescription() {
        return (percentage < 0 ? "" : percentage + "% ") + state;
    }

    @Override
    public void cancel() {
        minecraftInstance.cancel();
    }

    @Override
    public boolean isCancelable() {
        return minecraftInstance.isCancelable();
    }

    public void run() {
        minecraftInstance.setLogsChangedListener(() -> {
            MinecraftInstance.LogRecord l = minecraftInstance.getLastLog();
            state = l.getText();
            percentage = l.getProgress();
        });
        minecraftInstance.run();
    }
}
