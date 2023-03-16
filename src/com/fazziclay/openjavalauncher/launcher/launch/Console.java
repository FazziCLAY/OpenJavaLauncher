package com.fazziclay.openjavalauncher.launcher.launch;

import com.fazziclay.openjavalauncher.util.Logger;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Console extends Thread {
    private Process process;
    private InputStream inputStream;
    private InputStream errorStream;

    public Console(Process process) {
        this.process = process;
        this.inputStream = process.getInputStream();
        this.errorStream = process.getErrorStream();
    }

    public void run() {
        while (process.isAlive()) {
            try {
                inputStream.transferTo(System.out);
                errorStream.transferTo(System.err);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
