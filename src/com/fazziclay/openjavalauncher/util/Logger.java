package com.fazziclay.openjavalauncher.util;

public class Logger {
    public static void d(String tab, String d) {
        System.out.printf("[%s] %s\n", tab, d);
    }

    public static void e(String tab, String d, Exception e) {
        System.err.printf("[%s] %s\n", tab, d);
        e.printStackTrace();
    }

    public static void e(String tab, String d) {
        System.err.printf("[%s] %s\n", tab, d);
    }
}
