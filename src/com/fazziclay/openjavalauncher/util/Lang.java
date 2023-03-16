package com.fazziclay.openjavalauncher.util;

import org.json.JSONArray;
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

    public static String[] getLanguages() {
        try {
            InputStream is = Lang.class.getClassLoader().getResourceAsStream("languages.json");
            String s = new String(is.readAllBytes());
            JSONArray json = new JSONArray(s);
            String[] ret = new String[json.length()];
            int i = 0;
            for (Object o : json) {
                ret[i] = o.toString();
                i++;
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLanguageName(String code) throws Exception {
        InputStream is = Lang.class.getClassLoader().getResourceAsStream("lang/" + code + ".json");
        String s = new String(is.readAllBytes());
        JSONObject json = new JSONObject(s);
        return json.getString("language_name");
    }

    public static String t(String key, Object... args) {
        if (langJson.has(key)) {
            return String.format(langJson.getString(key), args);
        }
        Logger.e("Lang", "Unknown key in " + langCode, new RuntimeException("Unknown key requested: " + key));
        return key;
    }
}
