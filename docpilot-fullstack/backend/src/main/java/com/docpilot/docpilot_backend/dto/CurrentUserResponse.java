package com.docpilot.docpilot_backend.dto;

public class CurrentUserResponse {
    private boolean connected;
    private String login;
    private String name;
    private String avatarUrl;

    public CurrentUserResponse() {}

    public CurrentUserResponse(boolean connected, String login, String name, String avatarUrl) {
        this.connected = connected;
        this.login = login;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public boolean isConnected() { return connected; }
    public String getLogin() { return login; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }
}
