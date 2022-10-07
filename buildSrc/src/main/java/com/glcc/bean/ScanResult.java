package com.glcc.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ScanResult {
    private AppInfo profile;
    private int timeUsed;
    private List<DetectionPoint> points;
    private List<Permission> permissions;
    private Map<String, String> libs;

    public ScanResult() {
        points = new LinkedList<>();
    }

    public AppInfo getProfile() {
        return profile;
    }

    public void setProfile(AppInfo profile) {
        this.profile = profile;
    }

    public int getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(int timeUsed) {
        this.timeUsed = timeUsed;
    }

    public List<DetectionPoint> getPoints() {
        return this.points;
    }

    public void setPoints(List<DetectionPoint> points) {
        this.points = points;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public Map<String, String> getLibs() {
        return libs;
    }

    public void setLibs(Map<String, String> libs) {
        this.libs = libs;
    }
}

