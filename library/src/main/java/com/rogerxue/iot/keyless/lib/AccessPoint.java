package com.rogerxue.iot.keyless.lib;


public class AccessPoint {
    public static final String FRONT_DOOR = "Front door";
    public static final String GARAGE_DOOR = "Garage door";
    public static final String WEST_YARD_GATE = "West yard gate";
    public static final String EAST_YARD_GATE = "East yard gate";

    // field names
    public static final String COLLECTION_NAME = "accessPoint";
    public static final String NAME = "name";
    public static final String ANDROID_ID = "androidId";
    public static final String DESCRIPTION = "description";
    public static final String TRIGGERED = "triggered";
    public static final String TRIGGER_HIGHT = "triggerHigh";
    public static final String TRIGGER_DELAY = "triggerDelay";

    private String name;
    private String androidId;
    private String description;
    private Boolean triggered;
    private Boolean triggerHigh;
    // in ms
    private int triggerDelay;

    public AccessPoint() {}

    public AccessPoint(
            String name,
            String androidId,
            String description,
            boolean triggerHigh,
            int triggerDelay) {
        this.androidId = androidId;
        this.name = name;
        this.description = description;
        this.triggerHigh = triggerHigh;
        triggered = false;
        this.triggerDelay = triggerDelay;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }



    public String getName() {
        return name;
    }

    public String getAndroidId() {
        return androidId;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getTriggered() {
        return triggered;
    }

    public Boolean getTriggerHigh() {
        return triggerHigh;
    }

    public int getTriggerDelay() {
        return triggerDelay;
    }
}
