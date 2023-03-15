package com.fazziclay.openjavalauncher;

public class UserProfile {
    private String nickname;
    private String uuid;

    public UserProfile(String nickname, String uuid) {
        this.nickname = nickname;
        this.uuid = uuid;
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
}
