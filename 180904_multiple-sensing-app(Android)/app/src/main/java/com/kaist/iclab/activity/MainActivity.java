package com.kaist.iclab.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kaist.iclab.R;
import com.kaist.iclab.datamanager.DAO;
import com.kaist.iclab.datamanager.DataProvider;
import com.kaist.iclab.datamanager.DatabaseHandler;
import com.kaist.iclab.devices.BleDeviceInfo;
import com.kaist.iclab.services.BluetoothLeService;
import com.kaist.iclab.services.E4Service;
import com.kaist.iclab.services.LocationService;
import com.kaist.iclab.services.SensorTagService;
import com.kaist.iclab.util.Constants;
import com.kaist.iclab.util.CustomToast;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE = 11;
    private static final int REQUEST_CANCLE_CODE = 22;

    private final int DEVICE_ID = 2;

    private Button mButtonE4Scan;
    private Button mButtonE4Stop;

    private Button mButtonSmartphoneSensingStart;
    private Button mButtonSmartphoneSensingStop;

    private Button mButtonSensorTagStart;
    private Button mButtonSensorTagStop;

    private Button mButtonDBExport;
    private Button mButtonFileTransfer;

    private Button mButtonCheckDBInsert;
    private Button mButtonRenameVideo;

    private TextView mTextUpdatedDate;
    private TextView mTextInfo;
    private TextView mTextDeleteDate;
    private TextView mTextNumberOfRecords;

    private Intent E4serviceIntent;
    private Intent LocationServiceIntent;
    private Intent SensorTagListActivityIntent;
    private Intent SensorTagServiceIntent;

    private BluetoothLeService mBluetoothLeService = null;

    private BluetoothDevice mConnectedBLEDevice;

    private SharedPreferences prefs;
    private String userName;
    private String phoneNumber;
    private String update_date;
    private String delete_date;
    public static String deviceSetName;
    public static String smartphone_mode;

    // Compressed files
    String infoPath = null;
    private ArrayList<String> mFilePathList = new ArrayList<String>();
    private CompressFiles mCompressFiles;
    boolean done = false;

    //"http://168.188.127.108:5555/AppRating/FileReceiver.jsp"
    //String FILE_SERVER_URL = "http://suggestbot.kse.smoon.kr/upload/"; //origin
    String FILE_SERVER_URL = "http://168.188.127.192/upload"; //modified
    String groupId = null;
    String userId = null;
    private String address = "";

    private PowerManager mPowermanager;
    private PowerManager.WakeLock mWakeLock;

    Intent settingActivityIntent;
    Intent persistentActivityIntent;

    AlertDialog.Builder mTransferAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonE4Scan = (Button) findViewById(R.id.button_e4_scan);
        mButtonE4Scan.setOnClickListener(pOnClickListener);
        mButtonE4Stop = (Button) findViewById(R.id.button_e4_stop);
        mButtonE4Stop.setOnClickListener(pOnClickListener);

        mButtonSmartphoneSensingStart = (Button)findViewById(R.id.button_smartphone_scan);
        mButtonSmartphoneSensingStart.setOnClickListener(pOnClickListener);
        mButtonSmartphoneSensingStop = (Button)findViewById(R.id.button_smartphone_stop);
        mButtonSmartphoneSensingStop.setOnClickListener(pOnClickListener);

        mButtonSensorTagStart = (Button)findViewById(R.id.button_sensortag_scan);
        mButtonSensorTagStart.setOnClickListener(pOnClickListener);
        mButtonSensorTagStop = (Button)findViewById(R.id.button_sensortag_stop);
        mButtonSensorTagStop.setOnClickListener(pOnClickListener);

        mButtonDBExport = (Button)findViewById(R.id.button_dbexport);
        mButtonDBExport.setOnClickListener(pOnClickListener);

        mButtonFileTransfer = (Button)findViewById(R.id.button_filetransfer);
        mButtonFileTransfer.setOnClickListener(pOnClickListener);

        mButtonCheckDBInsert = (Button)findViewById(R.id.button_check_db_insert);
        mButtonCheckDBInsert.setOnClickListener(pOnClickListener);

        mButtonRenameVideo = (Button)findViewById(R.id.button_rename_video);
        mButtonRenameVideo.setOnClickListener(pOnClickListener);

        mTextUpdatedDate = (TextView)findViewById(R.id.text_update_date);
        mTextInfo = (TextView)findViewById(R.id.text_info);
        mTextDeleteDate = (TextView)findViewById(R.id.text_delete_date);
        mTextNumberOfRecords = (TextView)findViewById(R.id.text_check_db_insert);

        E4serviceIntent = new Intent(this, E4Service.class);
        LocationServiceIntent = new Intent(this, LocationService.class);
        SensorTagListActivityIntent = new Intent(this, SensorTagListActivity.class);
        SensorTagServiceIntent = new Intent(this, SensorTagService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WAKE_LOCK
            }, 1);
            //}
        }

        initEmpaticaDeviceManager();
        mark(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        startBluetoothLeService();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        settingActivityIntent = new Intent(this, SettingsActivity.class);
        persistentActivityIntent = new Intent(this, PersistentActivity.class);

        mTransferAlert = new AlertDialog.Builder(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_PREFERENCES, 0, "User Info.");
        menu.add(0, MENU_DELETE, 1, "Delete DB File");
        return true;
    }
    private static final int MENU_PREFERENCES = 0;
    private static final int MENU_DELETE = 1;

    // Configuration for setting
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        // password is shown as a star
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());

        switch(item.getItemId()) {
            case MENU_PREFERENCES:
                alert.setTitle("Admin Login");
                alert.setMessage("Enter your password");
                // Set an EditText view to get user input
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        if (!value.equals(Constants.admin_password)) {
                            //HERE I AM STUCK!!!
                            Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_LONG).show();
                        } else {
                            startActivity(settingActivityIntent);
                        }
                    }
                });
                alert.show();
                return true;
            case MENU_DELETE:
                alert.setTitle("Admin Login");
                alert.setMessage("Enter your password");
                // Set an EditText view to get user input
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        if (!value.equals(Constants.admin_password)) {
                            //HERE I AM STUCK!!!
                            Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_LONG).show();
                        } else {
                            SQLiteDelete();
                        }
                    }
                });
                alert.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        userName = prefs.getString("user_name", "");
        phoneNumber = prefs.getString("phone_number", "");
        deviceSetName = prefs.getString("device_set_name", "0");
        smartphone_mode = prefs.getString("smartphone_mode","sensingPhone1");
        update_date = prefs.getString("update_date","NA");
        delete_date = prefs.getString("delete_date","NA");

        Toast.makeText(getApplicationContext(), deviceSetName+" "+smartphone_mode + " "+
                userName + " "+phoneNumber, Toast.LENGTH_SHORT).show();

        mTextInfo.setText("info: "+deviceSetName+" "+smartphone_mode + " "+phoneNumber);
        mTextUpdatedDate.setText(update_date);
        mTextDeleteDate.setText(delete_date);

        // WakeLock code section
        mPowermanager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowermanager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeAlways");
        mWakeLock.acquire();


        // Check service status for button visibility setting
        if (isE4ServiceRunning()){
            mButtonE4Scan.setEnabled(false);
            mButtonE4Stop.setEnabled(true);
        }else{
            mButtonE4Scan.setEnabled(true);
            mButtonE4Stop.setEnabled(false);
        }
        if (isLocationServiceRunning()){
            mButtonSmartphoneSensingStart.setEnabled(false);
            mButtonSmartphoneSensingStop.setEnabled(true);
        }else{
            mButtonSmartphoneSensingStart.setEnabled(true);
            mButtonSmartphoneSensingStop.setEnabled(false);
        }
        if (isSensorTagServiceRunning()){
            mButtonSensorTagStart.setEnabled(false);
            mButtonSensorTagStop.setEnabled(true);
        }else{
            mButtonSensorTagStart.setEnabled(true);
            mButtonSensorTagStop.setEnabled(false);
        }

    }
    public boolean isE4ServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (E4Service.class.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
    }
    public boolean isSensorTagServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (SensorTagService.class.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
    }
    public boolean isLocationServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (LocationService.class.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
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

    View.OnClickListener pOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_e4_scan:
                    mButtonE4Scan.setEnabled(false);
                    mButtonE4Stop.setEnabled(true);
                    E4serviceIntent.putExtra("device_set_name", deviceSetName);
                    startService(E4serviceIntent);
                    break;

                case R.id.button_e4_stop:
                    mButtonE4Scan.setEnabled(true);
                    mButtonE4Stop.setEnabled(false);
                    stopService(E4serviceIntent);
                    break;

                case R.id.button_smartphone_scan:
                    mButtonSmartphoneSensingStart.setEnabled(false);
                    mButtonSmartphoneSensingStop.setEnabled(true);
                    LocationServiceIntent.putExtra("device_set_name", deviceSetName);
                    LocationServiceIntent.putExtra("smartphone_mode", smartphone_mode);
                    startService(LocationServiceIntent);
                    break;

                case R.id.button_smartphone_stop:
                    mButtonSmartphoneSensingStart.setEnabled(true);
                    mButtonSmartphoneSensingStop.setEnabled(false);
                    stopService(LocationServiceIntent);
                    break;

                case R.id.button_sensortag_scan:
                    mButtonSensorTagStart.setEnabled(false);
                    mButtonSensorTagStop.setEnabled(true);
                    SensorTagListActivityIntent.putExtra("device_set_name", deviceSetName);
                    startActivityForResult(SensorTagListActivityIntent,REQUEST_CODE);
                    break;

                case R.id.button_sensortag_stop:
                    mButtonSensorTagStart.setEnabled(true);
                    mButtonSensorTagStop.setEnabled(false);

                    // disconnect BLE device
                    BluetoothLeService bluetoothLeService = BluetoothLeService.getInstance();
                    if (bluetoothLeService !=null && address != ""){
                        bluetoothLeService.disconnect(address);
                    }
                    //stop service
                    stopService(SensorTagServiceIntent);
                    break;

                case R.id.button_dbexport:
                    SQLiteExport();
                    break;

                case R.id.button_filetransfer:
                    mTransferAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Transfer~", Toast.LENGTH_LONG).show();
                            doSubmit();
                        } });
                    mTransferAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        } });
                    mTransferAlert.show();
                    break;

                case R.id.button_check_db_insert:
                    Cursor cursor = getContentResolver().query(DataProvider.CONTENT_URI_LOG, new String[] {"count(*)"},null,null,null);
                    if (cursor.getCount() == 0){
                        cursor.close();
                        mTextNumberOfRecords.setText("# of records: no result");
                    }
                    else{
                        cursor.moveToFirst();
                        int result = cursor.getInt(0);
                        mTextNumberOfRecords.setText("# of records: "+result);
                        cursor.close();
                    }

                    /*final DatabaseHandler handler = new DatabaseHandler(getContentResolver());
                    ContentValues[] multipleSensorData;
                    multipleSensorData = new ContentValues[1000];

                    ContentValues cv = new ContentValues();
                    String type = "Phone.ACC";
                    cv.put(DAO.LOG_FIELD_TYPE, type);
                    cv.put(DAO.LOG_FIELD_REG, new Date().getTime());

                    try {
                        JSONObject json = new JSONObject();
                        json.put("x", "1");
                        json.put("y", "2");
                        json.put("z", "3");
                        cv.put(DAO.LOG_FIELD_JSON, json.toString());
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                    for (int i=0; i< 1000; i++){
                        multipleSensorData[i] = cv;
                    }
                    handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_LOG, multipleSensorData);*/

                    break;
                case R.id.button_rename_video:
                    renameRecentRecordVideo();
                    break;
                default:
                    break;
            }
        }
    };

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

    public void SQLiteExport() {
        try {
            File internal = Environment.getDataDirectory();
            File external = Environment.getExternalStorageDirectory();

            File directory = new File(external.getAbsolutePath() + "/E4_Sensing");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            if (external.canWrite()) {
                File currentDB = new File(internal, "/data/com.kaist.iclab/databases/sensors_data.db");
                File exportDB = new File(external, "/E4_Sensing/"+ phoneNumber+"_" + new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis())) + ".db");

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(exportDB).getChannel();

                    dst.transferFrom(src, 0, src.size());

                    src.close();
                    dst.close();

                    Log.d("Ria", ">>> SQLite > SQLiteExport");
                    Toast.makeText(getApplicationContext(), "DB export success !", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {

        }
    }

    public void SQLiteDelete(){
        /* Instead of deleting DB file, trigger query to delete all rows from the table.
        it is more safe.
        File internal = Environment.getDataDirectory();
        File currentDB = new File(internal, "/data/com.kaist.iclab/databases/sensors_data.db");
        currentDB.delete();
        */
        AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {};
        handler.startDelete(-1, null, DataProvider.CONTENT_URI_LOG,null ,null);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("delete_date","The last deletion: "+new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis())));
        editor.commit();
    }



    private void mark(boolean start) {
        try {
            ContentValues values = new ContentValues();
            values.put(DAO.LOG_FIELD_TYPE, "MainActivity");
            values.put(DAO.LOG_FIELD_REG, new Date().getTime());
            values.put(DAO.LOG_FIELD_JSON, start ? "StartTracking" : "StopTracking");

            AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
            };
            handler.startInsert(-1, null, DataProvider.CONTENT_URI_LOG, values);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager();
                } else {
                    // Permission denied, boo!
                    final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // try again
                                    if (needRationale) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    private void initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWakeLock != null){
            mWakeLock.release();
            mWakeLock = null;
        }
        unbindService(mServiceConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        else if (requestCode == REQUEST_CODE){
            if (resultCode == DEVICE_ID){
                mConnectedBLEDevice = data.getParcelableExtra("deviceID");
                address = mConnectedBLEDevice.getAddress();
                Toast.makeText(this, "connected device: "+mConnectedBLEDevice.getAddress(), Toast.LENGTH_SHORT).show();
                //startService(new Intent(this,SensorTagService.class).putExtra("device_set_name", deviceSetName));
            }
            else{
                Toast.makeText(this, "no one selection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }
        });
    }

    private void doSubmit() {
        saveInfo();
        SQLiteExport();

        addFilesToZip();
        mCompressFiles = new CompressFiles();
        mCompressFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void saveInfo() {
        try {
            JSONObject json = new JSONObject();
            json.put("phone", phoneNumber);
            writeStringAsFile(json.toString(), "info.json");
        } catch (Exception e) {}
    }

    public void writeStringAsFile(final String fileContents, String fileName) {
        Context context = getBaseContext();
        try {
            File file = new File(context.getFilesDir(), fileName);
            infoPath = file.getAbsolutePath();
            FileWriter out = new FileWriter(infoPath);
            out.write(fileContents);
            out.close();
        } catch (IOException e) {

        }
    }

    private void addFilesToZip() {

        try {
            ApplicationInfo applicationInfo = getBaseContext().getPackageManager().getApplicationInfo(getBaseContext().getPackageName(), PackageManager.GET_META_DATA);
            String name = "sensors_data.db";
            String path = getBaseContext().getDatabasePath(name).getPath();

            Log.i(TAG, "db Path = " + path);

            mFilePathList.add(path);
            mFilePathList.add(infoPath);
            //mFilePathList.add(getRecentRecordVideo()); // server can't receive big size of file.
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }

    public String getRecentRecordVideo() {
        String path = Environment.getExternalStorageDirectory().toString()+"/DCIM/Look&Tell";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }
        return Environment.getExternalStorageDirectory().toString()+"/DCIM/Look&Tell/"+files[files.length-1].getName();
    }

    public void renameRecentRecordVideo() {
        String path = Environment.getExternalStorageDirectory().toString()+"/DCIM/Look&Tell";
        String path2 = Environment.getExternalStorageDirectory().toString()+"/LooknTell";

        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        if (files.length == 0){
            return;
        }
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }
        File internal = Environment.getDataDirectory();
        File external = Environment.getExternalStorageDirectory();

        File directory_creation = new File(external.getAbsolutePath() + "/LooknTell");
        if (!directory_creation.exists()) {
            directory_creation.mkdirs();
        }
        try{
            File from = new File(path,files[files.length-1].getName());
            File to = new File(path2, phoneNumber+"_"+new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis()))+".mp4");
            from.renameTo(to);
            Toast.makeText(this, "Renamed: "+to, Toast.LENGTH_SHORT).show();
        }catch (Exception e){

        }


    }

    public static File getOgetOutputZipFileutputZipFile(String fileName) {
        //File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "output");
        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private class CompressFiles extends AsyncTask<Void, Integer, Boolean> {

        ProgressDialog asyncDialog;
        File file;
        @Override
        protected void onPreExecute() {
            asyncDialog = new ProgressDialog(MainActivity.this);
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            asyncDialog.setMessage("압축중...");
            asyncDialog.setMax(100);
            asyncDialog.setCancelable(false);
            asyncDialog.setCanceledOnTouchOutside(false);

            asyncDialog.show();

            try {
                Log.i(TAG, "0% Completed");
            } catch (Exception ignored) {
            }
        }

        protected Boolean doInBackground(Void... urls) {
            file = getOgetOutputZipFileutputZipFile("Log.zip");
            Log.i(TAG, file.toString());
            String zipFileName;
            if (file != null) {
                zipFileName = file.getAbsolutePath();
                if (mFilePathList.size() > 0) {
                    zip(zipFileName);
                }
            }

            publishProgress(0, 100, 1);
            final boolean[] success = {false};
            try {
                //업로드
                SyncHttpClient client = new SyncHttpClient();
                Log.d(TAG, "url:"+ FILE_SERVER_URL+"/"+phoneNumber);
                RequestParams params = new RequestParams();
                try {
                    params.put("file", file);
                } catch (FileNotFoundException e) {
                }
                client.post(FILE_SERVER_URL+"/"+phoneNumber, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d(TAG, "454: onSuccess");
                        success[0] = true;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, final Throwable error) {
                        Log.e(TAG, "459: onFailure: " + statusCode);
                    }

                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {
                        publishProgress((int) ((double) bytesWritten / (double) totalSize * 100.0), 100, 1);
                    }
                });
            } catch (final Exception e) {
                e.printStackTrace();
            }
            file.deleteOnExit(); // delete zip file
            return success[0];
        }

        public void publish(int filesCompressionCompleted) {
            int totalNumberOfFiles = mFilePathList.size();
            publishProgress(filesCompressionCompleted, totalNumberOfFiles, 0);
        }

        protected void onProgressUpdate(Integer... progress) {
            try {
                asyncDialog.setProgress(progress[0]);
                asyncDialog.setMax(progress[1]);
                asyncDialog.setMessage(progress[2] == 0 ? "압축중..." : "업로드중");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }

        protected void onPostExecute(Boolean flag) {
            Log.d(TAG, "COMPLETED");
            asyncDialog.dismiss();

            if (flag) Toast.makeText(getApplicationContext(), "제출 완료", Toast.LENGTH_SHORT).show();
            mTextUpdatedDate.setText("The latest updated date: "+new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis())));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("update_date","The latest updated date: "+new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date(System.currentTimeMillis())));
            editor.commit();

            //finish();
        }
    }
    private static final int BUFFER = 2048;
    //Zipping function
    public void zip(String zipFilePath) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFilePath);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];
            for (int i = 0; i < mFilePathList.size(); i++) {
                setCompressProgress(i + 1);

                FileInputStream fi = new FileInputStream(mFilePathList.get(i));
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(mFilePathList.get(i).substring(mFilePathList.get(i).lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setCompressProgress(int filesCompressionCompleted) {
        mCompressFiles.publish(filesCompressionCompleted);
    }
}
