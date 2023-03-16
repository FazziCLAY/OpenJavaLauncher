package com.fazziclay.openjavalauncher.operation;

import com.fazziclay.openjavalauncher.OpenJavaLauncher;

import java.util.Random;

public abstract class FakeOperation extends Operation {

    private String id = OpenJavaLauncher.RANDOM.nextInt() + "";

    @Override
    public String getTitle() {
        return "Fake operation";
    }

    @Override
    public String getDescription() {
        return id;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
