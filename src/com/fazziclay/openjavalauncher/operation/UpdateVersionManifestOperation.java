package com.fazziclay.openjavalauncher.operation;

import static com.fazziclay.openjavalauncher.util.Lang.t;

public class UpdateVersionManifestOperation extends Operation {
    private String state;

    @Override
    public String getTitle() {
        return t("operation.updateVersionManifest.title");
    }

    @Override
    public String getDescription() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public void cancel() {
        throw new RuntimeException("Not cancelable!");
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
