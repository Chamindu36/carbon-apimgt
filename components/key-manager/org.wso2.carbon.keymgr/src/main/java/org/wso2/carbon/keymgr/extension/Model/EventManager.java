package org.wso2.carbon.keymgr.extension.Model;

public  class EventManager{
    private String type = "Binary";
    private String receiverUrlGroup = "tcp://localhost:9611";
    private String authUrlGroup = "ssl://localhost:9711";
    private String username = "admin";
    private String password = "admin";
    private boolean enabled = false;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReceiverUrlGroup() {
        return receiverUrlGroup;
    }

    public void setReceiverUrlGroup(String receiverUrlGroup) {
        this.receiverUrlGroup = receiverUrlGroup;
    }

    public String getAuthUrlGroup() {
        return authUrlGroup;
    }

    public void setAuthUrlGroup(String authUrlGroup) {
        this.authUrlGroup = authUrlGroup;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}