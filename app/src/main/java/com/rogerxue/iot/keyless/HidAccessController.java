package com.rogerxue.iot.keyless;

import android.util.Log;


import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rogerxue.iot.keyless.lib.AccessPoint;
import com.rogerxue.iot.keyless.lib.AccessRequest;

import java.io.IOException;
import java.util.Arrays;

/**
 * Processes HID card request.
 */
class HidAccessController {
    private static final String TAG = "HidAccessController";
    private static final String I2C_DEVICE_NAME = "I2C1";
    private static final int I2C_ADDRESS = 8;
    private static final String I2C_READY_PIN = "BCM5";

    private Gpio mI2cReadyPin;
    private final UserAccessController mUserAccessController;
    private final AccessPoint mAccessPoint;
    private final PeripheralManagerService mPeripheralManager;

    private I2cDevice mArduinoNano;

    private FirebaseFirestore mFirestore;
    HidAccessController(
            MainActivity.AccessCallback accessCallback,
            AccessPoint accessPoint,
            PeripheralManagerService service) {
        mAccessPoint = accessPoint;
        mFirestore = FirebaseFirestore.getInstance();
        mUserAccessController = new UserAccessController(accessCallback, accessPoint);
        mPeripheralManager = service;
        try {
            mArduinoNano = mPeripheralManager.openI2cDevice(I2C_DEVICE_NAME, I2C_ADDRESS);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access I2C device", e);
            mArduinoNano = null;
        }
    }

    private void readI2c() {
        byte[] buffer = new byte[32];
        try {
            mArduinoNano.read(buffer, buffer.length);
            Log.d(TAG, "read byte: " + Arrays.toString(buffer));
        } catch (IOException e) {
            Log.e(TAG, "i2c failed", e);
        }
        int i = ((0xFF & buffer[3]) << 24) | ((0xFF & buffer[2]) << 16) |
                ((0xFF & buffer[1]) << 8) | (0xFF & buffer[0]);
        Log.d(TAG, "read int: " + i);
        String hidCode = String.valueOf(i);
        mUserAccessController.validateCode(AccessRequest.HID_CODE, hidCode);

        if (i != 0) {
            // log the request
            AccessRequest accessRequest = new AccessRequest(mAccessPoint.getName());
            mFirestore.collection(AccessRequest.COLLECTION_NAME)
                    .add(accessRequest.setHidCode(hidCode).setProcessed(true));
        }
    }

    public void start() {
        try {
            mI2cReadyPin = mPeripheralManager.openGpio(I2C_READY_PIN);
            mI2cReadyPin.setDirection(Gpio.DIRECTION_IN);
            mI2cReadyPin.setActiveType(Gpio.ACTIVE_LOW);
            mI2cReadyPin.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mI2cReadyPin.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    Log.d(TAG, "i2c ready");
                    readI2c();
                    return true;
                }
            });
        } catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO", e);
        }
    }

    public void stop() {
        if (mArduinoNano != null) {
            try {
                mArduinoNano.close();
                mArduinoNano = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }

        if (mI2cReadyPin != null) {
            try {
                mI2cReadyPin.close();
                mI2cReadyPin = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C pin", e);
            }
        }
    }
}
