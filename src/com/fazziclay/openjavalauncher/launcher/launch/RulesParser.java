package com.fazziclay.openjavalauncher.launcher.launch;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class RulesParser {
    public static boolean isRuleAllow(JSONArray rules) {
        return isRuleAllow(rules, new JSONObject());
    }

    public static boolean isRuleAllow(JSONArray rules, JSONObject features) {
        if (rules == null) return true;
        if (features == null) features = new JSONObject();
        boolean b = true;
        for (Object r : rules) {
            JSONObject rule = (JSONObject) r;
            String action = rule.getString("action");
            if (action.equalsIgnoreCase("allow")) {
                // Operating system
                if (rule.has("os")) {
                    JSONObject os = rule.getJSONObject("os");
                    if (os.has("name")) {
                        String name = os.getString("name");
                        if (!name.equalsIgnoreCase(getOperatingSystemType())){
                            b = false;
                        }
                    }
                }

                if (rule.has("features")) {
                    JSONObject f = rule.getJSONObject("features");
                    for (String s : f.keySet()) {
                        boolean v = f.getBoolean(s);
                        if (v) {
                            if (!features.optBoolean(s, false)) {
                                b = false;
                            }
                        }
                    }
                }
            }
        }
        return b;
    }

    public static String getOperatingSystemType() {
        String detectedOS;
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.contains("mac")) || (OS.contains("darwin"))) {
            detectedOS = "osx";
        } else if (OS.contains("win")) {
            detectedOS = "windows";
        } else if (OS.contains("nux")) {
            detectedOS = "linux";
        } else {
            detectedOS = "other";
        }
        return detectedOS;
    }
}
