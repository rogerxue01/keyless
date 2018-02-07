package com.rogerxue.iot.keyless.lib;


import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class AccessRequest {
    // field names
    public static final String COLLECTION_NAME = "accessRequest";
    public static final String HID_CODE = "hidCode";
    public static final String ACCESS_POINT = "accessPoint";
    public static final String ANDROID_ID = "androidId";
    public static final String TIMESTAMP = "timestamp";
    public static final String BUTTON_CLICK = "buttonClick";
    public static final String PROCESSED = "processed";


    private @ServerTimestamp Date timestamp; // in ms

    private String hidCode;
    private String androidId;
    // which door this request is for.
    private String accessPoint;

    private Boolean buttonClick;
    private Boolean processed;

    public AccessRequest() {}

    public AccessRequest(String accessPoint) {
        this.accessPoint = accessPoint;
        setAllFieldsToDefault();
    }

    private void setAllFieldsToDefault() {
        hidCode = "";
        androidId = "";
        buttonClick = false;
        processed = false;
    }

    public AccessRequest setAndroidId(String androidId) {
        this.androidId = androidId;
        return this;
    }

    public AccessRequest setHidCode(String hidCode) {
        this.hidCode = hidCode;
        return this;
    }

    public AccessRequest setButtonClick(boolean buttonClick) {
        this.buttonClick = buttonClick;
        return this;
    }

    public AccessRequest setProcessed(boolean processed) {
        this.processed = processed;
        return this;
    }

    public Boolean getButtonClick() {
        return buttonClick;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getAndroidId() {
        return androidId;
    }

    public String getHidCode() {
        return hidCode;
    }

    public Boolean getProcessed() {
        return processed;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AccessRequest:");
        sb.append(" timestamp: " + timestamp);
        sb.append(" androidId: " + androidId);
        sb.append(" hidCode: " + hidCode);
        sb.append(" accessPoint: " + accessPoint);
        sb.append(" button: " + buttonClick);
        sb.append(" processed: " + processed);
        return sb.toString();
    }
}
