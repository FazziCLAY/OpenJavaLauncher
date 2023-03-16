package com.fazziclay.openjavalauncher.launcher.launch;

import org.json.JSONArray;
import org.json.JSONObject;

public class VersionInfo {
    public String type;
    public String mainClass;
    public String id;
    public String assets;
    public int complianceLevel;
    public Downloads downloads;
    public JSONArray libraries;
    public JSONObject assetsIndexInfo;
    public JSONArray minecraftArguments;
    public JSONArray jvmArguments;

    public static class Downloads {
        public DownloadFile client;

        public static class DownloadFile {
            public String url;
            public String sha1;
        }
    }
}
