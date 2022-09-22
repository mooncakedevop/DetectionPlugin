package com.glcc.bean;

public class AppInfo {
    private String packageName;
    private String pluginVersion;
    private int TimeUsed;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public int getTimeUsed() {
        return TimeUsed;
    }

    public void setTimeUsed(int timeUsed) {
        TimeUsed = timeUsed;
    }
}
