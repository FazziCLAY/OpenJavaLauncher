package com.fazziclay.openjavalauncher.launcher;

import java.util.UUID;

public class UserProfile {
    private UUID profileUUID;
    private String name;
    private String nickname;
    private String uuid;
    private boolean isDemo;

    public UserProfile(UUID profileUUID, String name, String nickname, String uuid, boolean isDemo) {
        this.profileUUID = profileUUID;
        this.name = name;
        this.nickname = nickname;
        this.uuid = uuid;
        this.isDemo = isDemo;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public UUID getProfileUUID() {
        return profileUUID;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
