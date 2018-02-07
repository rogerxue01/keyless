package com.rogerxue.iot.keyless;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.Arrays;

/**
 * Directly read Data1 and Data0 pin, but current version of Android things
 * is not capable of such short interrupt.
 */
class HidReader {
    private static final String TAG = "HidReader";

    private static final String DATA0_PIN_NAME = "BCM6";
    private static final String DATA1_PIN_NAME = "BCM5";
    private static final int MAX_BITS = 100;
    private static final int TRANSMISSION_TIMEOUT_MS = 200;

    private Gpio mData0;
    private Gpio mData1;

    private byte[] mBuffer = new byte[MAX_BITS];
    private int mIndex;

    private final PeripheralManagerService mService;
    private final HidCallback mHidCallback;
    private final Handler mHander;

    private long startingNenoTime;

    public interface HidCallback {
        void onHidready(int data);
    }

    HidReader(Context context, PeripheralManagerService service, HidCallback hidCallback) {
        mService = service;
        mHidCallback = hidCallback;

        mHander = new Handler(context.getMainLooper());
    }

    public void start() {
        mIndex = 0;
        try {
            mData0 = mService.openGpio(DATA0_PIN_NAME);
            mData0.setDirection(Gpio.DIRECTION_IN);
            mData0.setActiveType(Gpio.ACTIVE_LOW);
            mData0.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mData0.registerGpioCallback(new HidGpioCallback((byte) 0));
            mData1 = mService.openGpio(DATA1_PIN_NAME);
            mData1.setDirection(Gpio.DIRECTION_IN);
            mData1.setActiveType(Gpio.ACTIVE_LOW);
            mData1.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mData1.registerGpioCallback(new HidGpioCallback((byte) 1));
        } catch (IOException e) {
            Log.e(TAG, "unable to open GPIO", e);
        }
    }

    public void stop() {
        try {
            mData0.close();
            mData1.close();
        } catch(IOException e) {
            Log.e(TAG, "nuable to close GPIO", e);
        }
        mIndex = 0;
        mBuffer = new byte[MAX_BITS];
    }

    private class HidGpioCallback extends GpioCallback {
        private byte mBit;

        HidGpioCallback(byte bit) {
            mBit = bit;
        }

        @Override
        public boolean onGpioEdge(Gpio gpio) {
            // at the start of the transmission, set the timeout and call back and clean up
            if (mIndex == 0) {
                startingNenoTime = System.nanoTime();
                mHander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        explainBits();
                        mHidCallback.onHidready(1);
                        mIndex = 0;
                        mBuffer = new byte[MAX_BITS];
                    }
                }, TRANSMISSION_TIMEOUT_MS);
            }
            mBuffer[mIndex ++] = mBit;
            Log.w(TAG, "time since first signal: " + (System.nanoTime() - startingNenoTime));
            // Continue listening for more interrupts
            return true;
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.w(TAG, gpio + ": Error event " + error);
        }
    };

    private void explainBits() {
        decode26Format();
    }
    void decode26Format() {
        int facilityCode = 0;
        // standard 26 bit format
        for (int i = 1; i < 9; ++i) {
            facilityCode = facilityCode | (mBuffer[i] << (8 - i));
        }

        int cardCode = 0;
        for (int i = 9; i < 25; ++i) {
            cardCode = cardCode | (mBuffer[i] << (24 - i));
        }
        Log.d(TAG, "buffer: " + Arrays.toString(mBuffer));
        Log.d(TAG, String.format("facility code: %16s", Integer.toBinaryString(facilityCode)));
        Log.d(TAG, "Facility: " + facilityCode);
        Log.d(TAG, String.format("card code: %16s", Integer.toBinaryString(cardCode)));
        Log.d(TAG, "Card: " + cardCode);
    }
}
