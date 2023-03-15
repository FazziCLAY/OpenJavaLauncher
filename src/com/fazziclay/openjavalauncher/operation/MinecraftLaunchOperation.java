package com.fazziclay.openjavalauncher.operation;

import com.fazziclay.openjavalauncher.VersionManifest;

import static com.fazziclay.openjavalauncher.util.Lang.t;

public class MinecraftLaunchOperation extends Operation {
    private VersionManifest.Version version;
    private short percentage = 0;
    private String state;
    private Process process;

    public MinecraftLaunchOperation(VersionManifest.Version version) {
        this.version = version;
    }

    public void setPercentage(short percentage) {
        this.percentage = percentage;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @Override
    public String getTitle() {
        return t("operation.minecraftLaunch.title", version.getId());
    }

    @Override
    public String getDescription() {
        return state;
    }

    @Override
    public void cancel() {
        process.destroy();
    }

    @Override
    public boolean isCancelable() {
        return process != null;
    }
}
