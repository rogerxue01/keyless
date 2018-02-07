package com.rogerxue.iot.keyless;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.firestore.QuerySnapshot;
import com.rogerxue.iot.keyless.lib.AccessPoint;
import com.rogerxue.iot.keyless.lib.AccessRequest;
import com.rogerxue.iot.keyless.lib.DeviceLog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private static final String GPIO_BOLT = "BCM27";
    private static final String GPIO_BUTTON = "BCM4";
    private static final int BOLT_OPEN_DELAY_MS = 2000;

    private Gpio mBoltPin;
    // button to open the door
    private Gpio mBtnPin;
    private HidAccessController mHidAccessController;
    private AccessPoint mAccessPoint;
    private FirebaseFirestore mFirestore;
    private String mAndroidId;
    private PendingIntent mAlarmIntent;
    private AlarmManager mAlarmMgr;

    private Handler mHandler = new Handler(Looper.myLooper());
    private final AccessCallback mAccessCallback = new AccessCallback() {
        @Override
        public void grantAccess() {
            openDoor();
        }
    };

    private PeripheralManagerService mService;

    interface AccessCallback {
        void grantAccess();
    }

    private GpioCallback gpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(final Gpio gpio) {
            Log.d(TAG, "Btn pressed");
            openDoor();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mBtnPin.registerGpioCallback(gpioCallback);
                    } catch (IOException e) {
                        Log.w(TAG, "Unable to access GPIO", e);
                    }
                }
            }, BOLT_OPEN_DELAY_MS + 500);
            mBtnPin.unregisterGpioCallback(gpioCallback);
             //log activity
            AccessRequest accessRequest = new AccessRequest(mAccessPoint.getName());
            mFirestore.collection(AccessRequest.COLLECTION_NAME)
                    .add(accessRequest.setButtonClick(true).setProcessed(true));
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAndroidId = android.provider.Settings.Secure.getString(
                getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        mFirestore = FirebaseFirestore.getInstance();

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        Log.d(TAG, "ip is: " + ip);
        mFirestore.collection(DeviceLog.COLLECTION_NAME).document(mAndroidId)
                .set(new DeviceLog(mAndroidId, ip));

        mService = new PeripheralManagerService();
        Log.d(TAG, "Available GPIO: " + mService.getGpioList());

        try {
            mBoltPin = mService.openGpio(GPIO_BOLT);
            mBtnPin = mService.openGpio(GPIO_BUTTON);
            mBtnPin.setDirection(Gpio.DIRECTION_IN);
            mBtnPin.setActiveType(Gpio.ACTIVE_LOW);
            mBtnPin.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mBtnPin.registerGpioCallback(gpioCallback);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO", e);
        }
        getThisAccessPoint();

        // set alarm to restart this app daily
        mAlarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AppRestarter.class);
        mAlarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // Set the alarm to start at 2:00 AM
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, 11);
//        calendar.set(Calendar.MINUTE, 0);
//        Calendar current = Calendar.getInstance();
//        current.setTimeInMillis(System.currentTimeMillis());
//        Log.d(TAG, "now setup alarm: " + new SimpleDateFormat("MM.dd.yyyy HH:mm:ss z").format(calendar.getTime()));
//        Log.d(TAG, "system time is: " + new SimpleDateFormat("MM.dd.yyyy HH:mm:ss z").format(current.getTime()));
//        mAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                AlarmManager.INTERVAL_DAY, mAlarmIntent);
    }

    private void getThisAccessPoint() {
        final String androidId = android.provider.Settings.Secure.getString(
                getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        Log.d(TAG, "android id: " + androidId);
        mFirestore.collection(AccessPoint.COLLECTION_NAME)
                .whereEqualTo(AccessPoint.ANDROID_ID, androidId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "There's error loading database", task.getException());
                    return;
                }
                QuerySnapshot documentSnapshot = task.getResult();
                // should only have one.
                List<DocumentSnapshot> doc = documentSnapshot.getDocuments();
                if (doc.size() != 1) {
                    Log.d(TAG, "more than one matching device " + androidId);
                    return;
                }
                mAccessPoint = doc.get(0).toObject(AccessPoint.class);
                try {
                    if (mAccessPoint.getTriggerHigh()) {
                        Log.d(TAG, "set trigger high");
                        mBoltPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                        mBoltPin.setActiveType(Gpio.ACTIVE_HIGH);
                    } else {
                        Log.d(TAG, "set trigger low");
                        mBoltPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
                        mBoltPin.setActiveType(Gpio.ACTIVE_LOW);
                    }

                } catch (IOException e) {
                    Log.w(TAG, "Unable to access GPIO", e);
                }
                mHidAccessController = new HidAccessController(
                        mAccessCallback, mAccessPoint, mService);
                mHidAccessController.start();
                CloudAccessController cloudAccessController =
                        new CloudAccessController(mAccessCallback, mAccessPoint);
            }
        });
    }

    private void openDoor() {
        try {
            Log.d(TAG, "setting bolt pin to true");
            mBoltPin.setValue(true);
            mFirestore.collection(AccessPoint.COLLECTION_NAME).document(mAccessPoint.getName())
                    .update(AccessPoint.TRIGGERED, true);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO", e);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "setting bolt pin to false");
                    mBoltPin.setValue(false);
                    mFirestore.collection(AccessPoint.COLLECTION_NAME).document(mAccessPoint.getName())
                            .update(AccessPoint.TRIGGERED, false);
                } catch (IOException e) {
                    Log.w(TAG, "Unable to access GPIO", e);
                }
            }
        }, mAccessPoint == null ? BOLT_OPEN_DELAY_MS : mAccessPoint.getTriggerDelay());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlarmMgr != null) {
            mAlarmMgr.cancel(mAlarmIntent);
        }
        mHidAccessController.stop();
        try {
            mBoltPin.close();
            mBtnPin.close();
        } catch (IOException e) {
            Log.w(TAG, "unable to close GPIO", e);
        }
    }
}
