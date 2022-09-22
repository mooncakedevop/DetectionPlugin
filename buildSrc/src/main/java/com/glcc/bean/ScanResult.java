package com.glcc.bean;

public class ScanResult {
    private AppInfo profile;
    private int timeUsed;
    private DetectionPoint point;

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

    public DetectionPoint getPoint() {
        return point;
    }

    public void setPoint(DetectionPoint point) {
        this.point = point;
    }
}

