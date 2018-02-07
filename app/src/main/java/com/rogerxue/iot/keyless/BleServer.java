package com.rogerxue.iot.keyless;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.rogerxue.iot.keyless.lib.DeviceProfile;

/**
 * Created by rogerxue on 9/14/17.
 */
class BleServer {
    private static final String TAG = "BleServer";

    private final Context mContext;
    private final BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBtAdapter;
    private BluetoothGattServer mGattServer;
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(TAG, "onconnectionStateChange");
        }

        @Override
        public void onCharacteristicWriteRequest(
                BluetoothDevice device,
                int requestId,
                BluetoothGattCharacteristic characteristic,
                boolean preparedWrite,
                boolean responseNeeded,
                int offset,
                byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.d(TAG, "write request, device: " + device);
            Log.d(TAG, "write request, charact: " + characteristic);
            Log.d(TAG, "write request, value: " + value);
        }
    };

    BleServer(Context context) {
        mContext = context;
        mBluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBluetoothManager.getAdapter();
    }

    public void start() {
        if (!mBtAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is currently disabled...enabling");
            mBtAdapter.enable();
        }
        Log.d(TAG, "bt enabled: " + mBtAdapter.isEnabled());
        mGattServer = mBluetoothManager.openGattServer(mContext, mGattServerCallback);
        if (mGattServer == null) {
            Log.w(TAG, "can't start Gatt server.");
            return;
        }
        initServer();
    }

    public void stop() {
        mGattServer.close();
    }

    private void initServer() {
        BluetoothGattService service = new BluetoothGattService(DeviceProfile.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic pinCharacteristic =
                new BluetoothGattCharacteristic(DeviceProfile.CHARACTERISTIC_PIN_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(pinCharacteristic);
        Log.d(TAG, "adding to service: " + mGattServer);
        mGattServer.addService(service);
    }
}
