package com.fazziclay.openjavalauncher;

import org.json.JSONArray;
import org.json.JSONObject;

public class VersionManifest {
    private final JSONObject origin;
    private final Latest latest;
    private boolean isFresh;

    public VersionManifest(JSONObject origin, boolean isFresh) {
        this.origin = origin;
        this.latest = new Latest(origin.getJSONObject("latest"));
        this.isFresh = isFresh;
    }

    public Latest getLatest() {
        return latest;
    }

    public boolean isFresh() {
        return isFresh;
    }

    public Version[] getVersions() {
        JSONArray jsonArray = origin.getJSONArray("versions");
        Version[] versions = new Version[jsonArray.length()];
        int i = 0;
        while (i < jsonArray.length()) {
            JSONObject v = jsonArray.getJSONObject(i);
            versions[i] = new Version(v);

            i++;
        }
        return versions;
    }

    public Version getVersionById(String findingId) {
        JSONArray versions = origin.getJSONArray("versions");
        int i = 0;
        Version version = null;
        while (i < versions.length()) {
            JSONObject v = versions.getJSONObject(i);
            String id = v.getString("id");
            if (id.equals(findingId)) {
                version = new Version(v);
                break;
            }

            i++;
        }
        return version;
    }

    public static class Latest {
        private final String release;
        private final String snapshot;

        public Latest(JSONObject source) {
            String release = source.getString("release");
            String snapshot = source.getString("snapshot");

            this.release = release;
            this.snapshot = snapshot;
        }

        public String getRelease() {
            return release;
        }

        public String getSnapshot() {
            return snapshot;
        }
    }

    public static class Version {
        private final String id;
        private final String type;
        private final String url;
        private final String time;
        private final String releaseTime;
        private final String sha1;
        private final int complianceLevel;

        public Version(JSONObject v) {
            this.id = v.getString("id");
            this.type = v.getString("type");
            this.url = v.getString("url");
            this.time = v.getString("time");
            this.releaseTime = v.getString("releaseTime");
            this.sha1 = v.getString("sha1");
            this.complianceLevel = v.getInt("complianceLevel");
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        public String getTime() {
            return time;
        }

        public String getReleaseTime() {
            return releaseTime;
        }

        public String getSha1() {
            return sha1;
        }

        public int getComplianceLevel() {
            return complianceLevel;
        }

        @Override
        public String toString() {
            return "Version{" +
                    "id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    ", url='" + url + '\'' +
                    ", time='" + time + '\'' +
                    ", releaseTime='" + releaseTime + '\'' +
                    ", sha1='" + sha1 + '\'' +
                    ", complianceLevel=" + complianceLevel +
                    '}';
        }
    }
}
