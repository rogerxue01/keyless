package com.rogerxue.iot.keyless.lib;

import android.util.Log;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class User {
    private static final String TAG = "User";

    // fields names
    public static final String COLLECTION_NAME = "user";
    public static final String NAME = "name";
    public static final String ANDROID_ID = "androidId";
    public static final String HID_CODE = "hidCode";
    public static final String DEVELOPER = "developer";

    private String name;
    private String androidId;
    private String hidCode;
    private boolean developer;

    // from string to Calendar dayOfWeek int
    public static final Map<Integer, String> DAY_OF_WEEK = new HashMap<>();
    static {
        DAY_OF_WEEK.put(Calendar.SUNDAY, "SUNDAY");
        DAY_OF_WEEK.put(Calendar.MONDAY, "MONDAY");
        DAY_OF_WEEK.put(Calendar.TUESDAY, "TUESDAY");
        DAY_OF_WEEK.put(Calendar.WEDNESDAY, "WEDNESDAY");
        DAY_OF_WEEK.put(Calendar.THURSDAY, "THURSDAY");
        DAY_OF_WEEK.put(Calendar.FRIDAY, "FRIDAY");
        DAY_OF_WEEK.put(Calendar.SATURDAY, "SATURDAY");
    }

    // match the DayOfWeek enum, 0 represents all days,
    // -1 represents none of the days.
    // Monday - Sunday : Calendar.DayOfWeek
    // access point to day of week
    public Map<String, Map<String, Boolean>> accessPointDayOfWeek = new HashMap<>();

    public User() {}

    public User(
            String name,
            String androidId,
            String hidCode) {
        this.name = name;
        this.androidId = androidId;
        this.hidCode = hidCode;
        developer = false;
    }

    public void grantAccess(
            String accessPoint,
            boolean sunday,
            boolean mondy,
            boolean tuesday,
            boolean wednesday,
            boolean thursday,
            boolean friday,
            boolean saturday) {
        Map<String, Boolean> permission = new HashMap<>();
        permission.put(DAY_OF_WEEK.get(Calendar.SUNDAY), sunday);
        permission.put(DAY_OF_WEEK.get(Calendar.MONDAY), mondy);
        permission.put(DAY_OF_WEEK.get(Calendar.TUESDAY), tuesday);
        permission.put(DAY_OF_WEEK.get(Calendar.WEDNESDAY), wednesday);
        permission.put(DAY_OF_WEEK.get(Calendar.THURSDAY), thursday);
        permission.put(DAY_OF_WEEK.get(Calendar.FRIDAY), friday);
        permission.put(DAY_OF_WEEK.get(Calendar.SATURDAY), saturday);
        accessPointDayOfWeek.put(accessPoint, permission);
    }

    public User setDeveloper(boolean enabled) {
        developer = enabled;
        return this;
    }

    public boolean getDeveloper() {
        return developer;
    }

    public String getAndroidId() {
        return androidId;
    }

    public String getHidCode() {
        return hidCode;
    }

    public String getName() {
        return name;
    }

    public boolean isGrantedNow(AccessPoint accessPoint) {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        Map<String, Boolean> permission =
                accessPointDayOfWeek.get(accessPoint.getName());
        if (permission == null) {
            Log.d(TAG, "no permission set for: " + accessPoint.getName());
            return false;
        } else {
            Log.d(TAG, "" + DAY_OF_WEEK.get(currentDay) + " day granted for: " + name);
            return permission.get(DAY_OF_WEEK.get(currentDay));
        }
    }
}