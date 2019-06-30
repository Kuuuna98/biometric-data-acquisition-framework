package com.kaist.iclab;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.widget.Toast;

import com.kaist.iclab.services.BluetoothLeService;
import com.kaist.iclab.util.CustomToast;

/**
 * Created by root on 3/19/18.
 */

public class E4SensingApplication extends Application {
    public boolean mBleSupported = true;
    private BluetoothLeService mBluetoothLeService = null;
    private IntentFilter mFilter;
    public BluetoothAdapter mBtAdapter = null;
    public static BluetoothManager mBluetoothManager;
    // Register the BroadcastReceiver

    @Override
    public void onCreate() {
        super.onCreate();
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG)
                    .show();
            mBleSupported = false;
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to BluetoothAdapter through BluetoothManager.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBtAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_LONG).show();
            mBleSupported = false;
            return;
        }
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, mFilter);
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableIntent);
        }
        // Error appears. (jemin)
        // startBluetoothLeService();
    }

    // Code to manage Service life cycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                return;
            }
            final int n = mBluetoothLeService.numConnectedDevices();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private void startBluetoothLeService() {
        boolean f;
        Intent bindIntent = new Intent(this, BluetoothLeService.class);
        startService(bindIntent);
        f = bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (!f) {
            CustomToast.middleBottom(this, "Bind to BluetoothLeService failed");
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                switch (mBtAdapter.getState()) {
                    case BluetoothAdapter.STATE_ON:
                        startBluetoothLeService();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    default:
                        break;
                }
            }
        }
    };

}
