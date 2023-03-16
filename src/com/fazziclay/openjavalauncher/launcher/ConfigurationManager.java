package com.fazziclay.openjavalauncher.launcher;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.openjavalauncher.util.Lang;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfigurationManager {
    private final File file;
    private final File launcherDir;

    private String language = Lang.DEFAULT_LANGUAGE;
    private int windowStartWidth = 500;
    private int windowStartHeight = 700;

    private List<UserProfile> userProfileList = new ArrayList<>();
    private List<GameProfile> gameProfileList = new ArrayList<>();
    private UUID selectedUserProfile;
    private UUID selectedGameProfile;

    public ConfigurationManager(File launcherDir, File file) {
        this.launcherDir = launcherDir;
        this.file = file;
        if (FileUtil.isExist(file)) {
            importJson(new JSONObject(FileUtil.getText(file, "{}")));
        }
        save();
    }

    public void save() {
        FileUtil.setText(file, exportJson().toString(2));
    }

    private JSONObject exportJson() {
        return new JSONObject()
                .put("formatVersion", 0)
                .put("latestSave", System.currentTimeMillis())
                .put("language", language)
                .put("windowStartWidth", windowStartWidth)
                .put("windowStartHeight", windowStartHeight)
                .put("selectedUserProfile", selectedUserProfile)
                .put("selectedGameProfile", selectedGameProfile)
                .put("userProfiles", exportJsonUserProfiles())
                .put("gameProfiles", exportJsonGameProfiles());
    }

    private void importJson(JSONObject json) {
        int formatVersion = json.getInt("formatVersion");
        language = json.optString("language", language);
        windowStartWidth = json.optInt("windowStartWidth", windowStartWidth);
        windowStartHeight = json.optInt("windowStartHeight", windowStartHeight);
        userProfileList = importJsonUserProfiles(json.optJSONArray("userProfiles"));
        gameProfileList = importJsonGameProfiles(json.optJSONArray("gameProfiles"));
        selectedUserProfile = UUID.fromString(json.optString("selectedUserProfile", UUID.randomUUID().toString()));
        selectedGameProfile = UUID.fromString(json.optString("selectedGameProfile", UUID.randomUUID().toString()));
    }

    private List<GameProfile> importJsonGameProfiles(JSONArray json) {
        final List<GameProfile> ret = new ArrayList<>();
        if (json == null) return ret;

        int i = 0;
        while (i < json.length()) {
            JSONObject profile = json.getJSONObject(i);

            ret.add(new GameProfile(UUID.fromString(profile.getString("profileUUID")), profile.optString("name", "name"), profile.getString("versionId"), profile.optString("jvmPath", "java"), profile.getString("jvmArguments"), new File(profile.getString("gameDirectory")), profile.optInt("windowWidth", 0), profile.optInt("windowHeight", 0), profile.optBoolean("downloadMissingAssets", true)));
            i++;
        }

        return ret;
    }

    private JSONArray exportJsonGameProfiles() {
        final JSONArray ret = new JSONArray();
        for (GameProfile gameProfile : gameProfileList) {
            ret.put(new JSONObject()
                    .put("name", gameProfile.getName())
                    .put("profileUUID", gameProfile.getProfileUUID().toString())
                    .put("versionId", gameProfile.getVersionId())
                    .put("jvmArguments", gameProfile.getJVMArguments())
                    .put("gameDirectory", gameProfile.getGameDirectory().getAbsolutePath())
                    .put("windowWidth", gameProfile.getWindowWidth())
                    .put("windowHeight", gameProfile.getWindowHeight())
                    .put("downloadMissingAssets", gameProfile.isDownloadMissingAssets())
                    .put("jvmPath", gameProfile.getJVMPath())
            );
        }
        return ret;
    }

    private List<UserProfile> importJsonUserProfiles(JSONArray json) {
        final List<UserProfile> ret = new ArrayList<>();
        if (json == null) return ret;

        int i = 0;
        while (i < json.length()) {
            JSONObject profile = json.getJSONObject(i);

            ret.add(new UserProfile(UUID.fromString(profile.getString("profileUUID")), profile.optString("name", "name"), profile.getString("nickname"), profile.getString("uuid"), profile.optBoolean("isDemo")));
            i++;
        }

        return ret;
    }

    private JSONArray exportJsonUserProfiles() {
        final JSONArray ret = new JSONArray();

        for (UserProfile userProfile : userProfileList) {
            ret.put(new JSONObject()
                    .put("name", userProfile.getName())
                    .put("profileUUID", userProfile.getProfileUUID().toString())
                    .put("nickname", userProfile.getNickname())
                    .put("isDemo", userProfile.isDemo())
                    .put("uuid", userProfile.getUuid())
            );
        }

        return ret;
    }

    public int getWindowStartWidth() {
        return windowStartWidth;
    }

    public int getWindowStartHeight() {
        return windowStartHeight;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String key) {
        this.language = key;
        save();
    }

    public List<UserProfile> getUserProfileList() {
        return userProfileList;
    }

    public UserProfile getUserProfile(UUID uuid) {
        for (UserProfile userProfile : userProfileList) {
            if (userProfile.getProfileUUID().equals(uuid)) return userProfile;
        }
        return null;
    }

    public List<GameProfile> getGameProfileList() {
        return gameProfileList;
    }

    public GameProfile getGameProfile(UUID uuid) {
        for (GameProfile gameProfile : gameProfileList) {
            if (gameProfile.getProfileUUID().equals(uuid)) return gameProfile;
        }
        return null;
    }

    public UserProfile getSelectedUserProfile() {
        return getUserProfile(selectedUserProfile);
    }

    public void setSelectedUserProfile(UserProfile profile) {
        this.selectedUserProfile = profile.getProfileUUID();
    }

    public GameProfile getSelectedGameProfile() {
        return getGameProfile(selectedGameProfile);
    }

    public void setSelectedGameProfile(GameProfile profile) {
        this.selectedGameProfile = profile.getProfileUUID();
    }

    public void addUserProfile(UserProfile profile) {
        boolean select = userProfileList.isEmpty();
        userProfileList.add(profile);
        if (select) setSelectedUserProfile(profile);
    }

    public void addGameProfile(GameProfile profile) {
        boolean select = gameProfileList.isEmpty();
        gameProfileList.add(profile);
        if (select) setSelectedGameProfile(profile);
    }
}
