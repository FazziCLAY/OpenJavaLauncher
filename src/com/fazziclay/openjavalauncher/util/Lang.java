package com.fazziclay.openjavalauncher.util;

import org.json.JSONObject;

import java.io.InputStream;

public class Lang {
    public static final String DEFAULT_LANGUAGE = "en_us";
    private static JSONObject langJson;
    private static String langCode;

    static {
        try {
            setLanguage(DEFAULT_LANGUAGE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setLanguage(String code) throws Exception {
        try {
            InputStream is = Lang.class.getClassLoader().getResourceAsStream("lang/" + code + ".json");
            String s = new String(is.readAllBytes());
            JSONObject json = new JSONObject(s);

            langCode = code;
            langJson = json;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static String t(String key, Object... args) {
        if (langJson.has(key)) {
            return String.format(langJson.getString(key), args);
        }
        Logger.e("Lang", "Unknown key in " + langCode, new RuntimeException("Unknown key requested: " + key));
        return "!!! UNKNOWN KEY " + key + " IN " + langCode + " !!!";
    }
}
