package com.fazziclay.openjavalauncher;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.openjavalauncher.util.Lang;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationManager {
    private File file;
    private File launcherDir;

    private String language = Lang.DEFAULT_LANGUAGE;
    private int windowStartWidth = 500;
    private int windowStartHeight = 700;

    private List<UserProfile> userProfileList = new ArrayList<>();
    private List<GameProfile> gameProfileList = new ArrayList<>();
    private UserProfile selectedUserProfile = new UserProfile("FazziCLAY", "");
    private GameProfile selectedGameProfile = new GameProfile("1.19.4");

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
                .put("windowStartHeight", windowStartHeight);
    }

    private void importJson(JSONObject json) {
        int formatVersion = json.getInt("formatVersion");
        language = json.optString("language", language);
        windowStartWidth = json.optInt("windowStartWidth", windowStartWidth);
        windowStartHeight = json.optInt("windowStartHeight", windowStartHeight);
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

    public List<GameProfile> getGameProfileList() {
        return gameProfileList;
    }

    public UserProfile getSelectedUserProfile() {
        return selectedUserProfile;
    }

    public void setSelectedUserProfile(UserProfile selectedUserProfile) {
        this.selectedUserProfile = selectedUserProfile;
    }

    public GameProfile getSelectedGameProfile() {
        return selectedGameProfile;
    }

    public void setSelectedGameProfile(GameProfile selectedGameProfile) {
        this.selectedGameProfile = selectedGameProfile;
    }
}
