package com.fazziclay.openjavalauncher.operation;

public abstract class Operation {
    public abstract String getTitle();
    public abstract String getDescription();
    public abstract void cancel();
    public abstract boolean isCancelable();
}
