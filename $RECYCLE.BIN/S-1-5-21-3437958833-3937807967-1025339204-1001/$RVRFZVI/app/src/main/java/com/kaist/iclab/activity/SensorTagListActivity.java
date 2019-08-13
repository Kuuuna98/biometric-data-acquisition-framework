package com.kaist.iclab.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaist.iclab.services.BluetoothLeService;
import com.kaist.iclab.services.SensorTagService;
import com.kaist.iclab.devices.BleDeviceInfo;

import com.kaist.iclab.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SensorTagListActivity extends Activity {

    private BluetoothLeScanner bluetoothLeScanner;

    private BluetoothAdapter bluetoothAdapter = null;

    private IntentFilter mFilter;
    private Button scanButton;
    private Button showButton;

    private String deviceSetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensortag_activity);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothAdapter == null) {
            AlertDialog.Builder aB = new AlertDialog.Builder(this);
            aB.setTitle("Error !");
            aB.setMessage("This Android device does not have Bluetooth or there is an error in the " +
                    "bluetooth setup. Application cannot start, will exit.");
            aB.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });
            AlertDialog a = aB.create();
            a.show();
            return;
        }

        // Initialize device list container and device filter
        mDeviceInfoList = new ArrayList<BleDeviceInfo>();
        bLeDeviceViewAdapter = new BLeDeviceViewAdapter(getApplicationContext(), mDeviceInfoList, "Connect");

        bLeDeviceListView = (ListView) findViewById(R.id.device_listview);
        bLeDeviceListView.setAdapter(bLeDeviceViewAdapter);
        bLeDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                scanLeDevice(false);
                if(position >= 0)
                    connect(mDeviceInfoList.get(position));
            }
        });

        mConnectedDeviceInfoList = new ArrayList<BleDeviceInfo>();
        bLeConnectedDeviceViewAdapter = new BLeDeviceViewAdapter(getApplicationContext(), mConnectedDeviceInfoList, "Disconnect");

        bLeConnectedDeviceListView = (ListView)findViewById(R.id.connected_device_listview);

        bLeConnectedDeviceListView.setAdapter(bLeConnectedDeviceViewAdapter);
        bLeConnectedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position >= 0)
                    disconnect(mConnectedDeviceInfoList.get(position));
            }
        });

        // Register the BroadcastReceiver
        mFilter = new IntentFilter();
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(mReceiver, mFilter);

        scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(!isScanEnabled);
            }
        });

        showButton = (Button) findViewById(R.id.show_button);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMovementActivity();
            }
        });

        deviceSetName = getIntent().getStringExtra("device_set_name");
        startService(new Intent(this,SensorTagService.class).putExtra("device_set_name", deviceSetName));
    }

    private void startMovementActivity(){
        //startActivity(new Intent(this, MovementActivity.class));
        // to handle connection in caller activity
        if (mConnectedDeviceInfoList.size() != 0 ){
            Intent deviceIDintent = new Intent();
            int DEVICE_ID = 2;
            deviceIDintent.putExtra("deviceID",mConnectedDeviceInfoList.get(0).getBluetoothDevice());
            setResult(DEVICE_ID,deviceIDintent);
        }
        finish();
    }

    private void connect(BleDeviceInfo bleDeviceInfo){
        setConnection(bleDeviceInfo, true);
        mConnectedDeviceInfoList.add(bleDeviceInfo);
        mDeviceInfoList.remove(bleDeviceInfo);
        bLeDeviceViewAdapter.notifyDataSetChanged();
        bLeConnectedDeviceViewAdapter.notifyDataSetChanged();

    }

    private void disconnect(BleDeviceInfo bleDeviceInfo){
        setConnection(bleDeviceInfo, false);
        mConnectedDeviceInfoList.remove(bleDeviceInfo);
        bLeConnectedDeviceViewAdapter.notifyDataSetChanged();
        mDeviceInfoList.add(bleDeviceInfo);
        bLeDeviceViewAdapter.notifyDataSetChanged();
    }

    private BLeDeviceViewAdapter bLeDeviceViewAdapter;
    private ListView bLeDeviceListView;
    private List<BleDeviceInfo> mDeviceInfoList;

    private BLeDeviceViewAdapter bLeConnectedDeviceViewAdapter;
    private ListView bLeConnectedDeviceListView;
    private List<BleDeviceInfo> mConnectedDeviceInfoList;

    private void setConnection(BleDeviceInfo bleDeviceInfo, boolean isConnection) {
        BluetoothLeService bluetoothLeService = BluetoothLeService.getInstance();
        BluetoothManager bluetoothManager = bluetoothLeService.getBtManager();

        BluetoothDevice device = bleDeviceInfo.getBluetoothDevice();

        int connState = bluetoothManager.getConnectionState(device, BluetoothGatt.GATT);

        String address = device.getAddress();

        if(isConnection)
            bluetoothLeService.connect(address);
        else
            bluetoothLeService.disconnect(address);

        Log.d(TAG, String.format("setConnection address = %s, isConnection = %s",address, isConnection));
    }

    private final static String TAG = MainActivity.class.getSimpleName();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);
            final String address = intent.getStringExtra(BluetoothLeService.EXTRA_DEVICE_ADDRESS);

            //Log.d(TAG, String.format("action = %s, status = %s, address = %s",action, status, address));
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                switch (bluetoothAdapter.getState()) {
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context, R.string.app_closing, Toast.LENGTH_LONG)
                                .show();
                        finish();
                        break;
                    default:
                        break;
                }
            } else {

            }
        }
    };

    boolean isScanEnabled = false;

    private void scanLeDevice(boolean enable) {
        if (isScanEnabled && !enable) {
            bluetoothLeScanner.stopScan(scanCallback);
            isScanEnabled = false;
            scanButton.setText("Scan");
            scanButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_refresh,0, 0, 0);
        } else if (!isScanEnabled && enable) {
            mDeviceInfoList.clear();
            bluetoothLeScanner.startScan(scanCallback);
            scanButton.setText("Stop scan");
            isScanEnabled = true;
            scanButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_cancel,0, 0, 0);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BleDeviceInfo bleDeviceInfo = new BleDeviceInfo(result);
            String name = bleDeviceInfo.getBluetoothDevice().getName();

            if(name == null || (!name.equals("SensorTag2") && !name.equals("CC2650 SensorTag")))
                return;

            if ( !mDeviceInfoList.contains(bleDeviceInfo)) {
                mDeviceInfoList.add(bleDeviceInfo);
                bLeDeviceViewAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        // to handle connection in caller activity
        if (mConnectedDeviceInfoList.size() != 0 ){
            Intent deviceIDintent = new Intent();
            int DEVICE_ID = 2;
            deviceIDintent.putExtra("deviceID",mConnectedDeviceInfoList.get(0).getBluetoothDevice());
            setResult(DEVICE_ID,deviceIDintent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothAdapter = null;
        unregisterReceiver(mReceiver);
        File cache = getCacheDir();
        String path = cache.getPath();
        //stopService(new Intent(this, SensorTagService.class));

        try {
            Runtime.getRuntime().exec(String.format("rm -rf %s", path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class BLeDeviceViewAdapter extends BaseAdapter {
    private final List<BleDeviceInfo> mDevices;
    private final LayoutInflater mInflater;
    private final String buttonText;

    public BLeDeviceViewAdapter(Context context, List<BleDeviceInfo> devices,String buttonText) {
        mInflater = LayoutInflater.from(context);
        mDevices = devices;
        this.buttonText= buttonText;
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup vg;

        if (convertView != null) {
            vg = (ViewGroup) convertView;
        } else {
            vg = (ViewGroup) mInflater.inflate(R.layout.element_device, null);
        }

        BleDeviceInfo deviceInfo = mDevices.get(position);
        BluetoothDevice device = deviceInfo.getBluetoothDevice();

        String name = device.getName();
        if (name == null)
            name = new String("Unknown device");

        String descr = name + "\n" + device.getAddress() + "\nRssi: " + deviceInfo.getRssi() + " dBm";
        ((TextView) vg.findViewById(R.id.descr)).setText(descr);

        ImageView iv = (ImageView) vg.findViewById(R.id.devImage);
        if (name.equals("SensorTag2") || name.equals("CC2650 SensorTag")) {
            iv.setImageResource(R.drawable.sensortag2_300);
        } else {
            iv.setImageResource(R.drawable.sensortag_300);
        }
        // Disable connect button when connecting or connected
        Button bv = (Button) vg.findViewById(R.id.btnConnect);
        bv.setText(buttonText);

        return vg;
    }
}
