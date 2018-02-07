package com.rogerxue.iot.keyless.lib;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class DeviceLog {
    public static final String COLLECTION_NAME = "deviceLog";
    public static final String TIMESTAMP = "timestamp";
    public static final String ANDROID_ID = "androidId";
    public static final String IP = "ip";


    private @ServerTimestamp
    Date timestamp; // in ms

    private String androidId;

    private String ip;

    public DeviceLog() {}

    public DeviceLog(String androidId, String ip) {
        this.androidId = androidId;
        this.ip = ip;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public static String getAndroidId() {
        return ANDROID_ID;
    }

    public String getIp() {
        return ip;
    }
}
