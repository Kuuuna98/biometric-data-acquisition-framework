package com.kaist.iclab.services;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.goebl.david.Webb;
import com.kaist.iclab.R;
import com.kaist.iclab.activity.MainActivity;
import com.kaist.iclab.activity.PersistentActivity;
import com.kaist.iclab.datamanager.DAO;
import com.kaist.iclab.datamanager.DataProvider;
import com.kaist.iclab.datamanager.DatabaseHandler;
import com.kaist.iclab.util.Constants;

import org.json.JSONObject;

import java.util.Date;

public class E4Service extends Service implements EmpaDataDelegate, EmpaStatusDelegate {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private static final long STREAMING_TIME = 10000; // Stops streaming 10 seconds after connection
    private static final String EMPATICA_API_KEY = "205faa4683d04bfa8d100d6564505485";
    private EmpaDeviceManager deviceManager = null;
    private static final String TAG = "E4Service";

    private int persistant_activity_count;

    private String E4deviceName = "";
    private String deviceSetName ="";
    private String phoneNumber = "";

    private NotificationManager nManager = null;
    private NotificationCompat.Builder ncomp;
    private static int NOTIFYID_E4 = 20180316;
    private String CHANNEL_ID = "E4_SERVICE";

    private int sensingCount = 0;
    private AsyncQueryHandler mDBhandler;

    private float HRV;

    private int accDelta;
    private int bvpDelta;
    private int ibiDelta;
    private int gsrDelta;
    private int tempDelta;

    private PowerManager mPowermanager;
    private PowerManager.WakeLock mWakeLock;

    Intent persistentActivityIntent;

    private ContentValues[] multipleE4Data_bvp;
    private ContentValues[] multipleE4Data_temp;
    private ContentValues[] multipleE4Data_gsr;
    private ContentValues[] multipleE4Data_acc;
    private ContentValues[] multipleE4Data_ibi;
  //  private ContentValues[] multipleE4_time;

    private int BULK_COUNT_bvp=0;
    private int BULK_COUNT_temp=0;
    private int BULK_COUNT_ibi=0;
    private int BULK_COUNT_gsr=0;
    private int BULK_COUNT_acc=0;
  //  private int BULK_COUNT_time=0;


    public E4Service() {

    }

    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"onCreate");
        persistant_activity_count = 12;
        persistentActivityIntent = new Intent(this, PersistentActivity.class);


        Enable();
        initAckDelta();
        mDBhandler = new AsyncQueryHandler(getContentResolver()) {};
    }
    public void initAckDelta(){
        accDelta = 0;
        bvpDelta = 0;
        ibiDelta = 0;
        gsrDelta = 0;
        tempDelta = 0;
    }

    public void onDestroy(){
        super.onDestroy();
        deviceManager.disconnect();
//        MainActivity.e4_insert = true;
        Disable();
        lastBulkHandler();
        Log.d(TAG,"onDestroy");
    }

    public int onStartCommand (Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        initEmpaticaDeviceManager();
        if (intent != null){
            deviceSetName = intent.getStringExtra("device_set_name");
            phoneNumber  = intent.getStringExtra("phone_number");
        }
        return START_STICKY;
    }
    private void Enable(){
        InitNotification();
        mNotificationUpdateHandler.postDelayed(UpdateNotification,   Constants.UPDATE_INTERVAL);

        // WakeLock code section
        mPowermanager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowermanager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeAlways");
        mWakeLock.acquire();

        // Array of Content Values Init.
        multipleE4Data_acc = new ContentValues[Constants.DB_BULK_RATE];
        multipleE4Data_bvp = new ContentValues[Constants.DB_BULK_RATE];
        multipleE4Data_gsr = new ContentValues[Constants.DB_BULK_RATE];
        multipleE4Data_ibi = new ContentValues[Constants.DB_BULK_RATE];
        multipleE4Data_temp = new ContentValues[Constants.DB_BULK_RATE];
     //   multipleE4_time = new ContentValues[Constants.DB_BULK_RATE];
    }
    private void Disable(){
        nManager.cancel(NOTIFYID_E4);
        mNotificationUpdateHandler.removeCallbacks(UpdateNotification);

        if (mWakeLock != null){
            mWakeLock.release();
            mWakeLock = null;
        }
    }
    // Notification Init.
    private void InitNotification()
    {
        nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        ncomp = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        ncomp.setContentTitle(getResources().getString(R.string.app_title));
        ncomp.setContentText(getResources().getString(R.string.notify_text));
        ncomp.setTicker(getResources().getString(R.string.bar_title));
        ncomp.setSmallIcon(R.mipmap.ic_launcher);
        ncomp.setAutoCancel(false);
        ncomp.setOngoing(true);

        nManager.notify(NOTIFYID_E4, ncomp.build());

    }
    // Show service status
    private Handler mNotificationUpdateHandler = new Handler();
    private Runnable UpdateNotification = new Runnable()
    {
        @Override
        public void run() {

            ncomp.setContentTitle("Connected: "+E4deviceName+", "+ splitToComponentTimes(sensingCount += Constants.ACK_RATE));
            ncomp.setContentText("ac:"+accDelta+" bvp:"+bvpDelta+" ibi:"+ibiDelta+" gsr:"+gsrDelta+" te:"+tempDelta);
            ncomp.setSmallIcon(R.mipmap.ic_launcher);
            ncomp.setAutoCancel(false);
            ncomp.setOngoing(true);
            nManager.notify(NOTIFYID_E4, ncomp.build());

            persistant_activity_count += 1;
            // persistant activity
            if (persistant_activity_count >= Constants.START_TRANSPARENT_ACTIVITY){
                Log.d(TAG,"Persistant activity start to increase priority of service");
                persistant_activity_count = 0;
                persistentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(persistentActivityIntent);
            }

            // Send ACK message
            new Thread() {
                public void run() {
                    Webb webb = Webb.create();
                    try {
                        JSONObject body = new JSONObject();
                        body.put("setNumber", deviceSetName);
                        body.put("deviceType","E4");
                        body.put("accDelta", accDelta);
                        body.put("bvpDelta", bvpDelta);
                        body.put("ibiDelta", ibiDelta);
                        body.put("gsrDelta", gsrDelta);
                        body.put("tempDelta", tempDelta);

                        JSONObject result = webb.post(Constants.SERVERL_URL_ACK)
                                .useCaches(false)
                                //.param("data_type",MainActivity.deviceSetName)
                                .body(body)
                                .ensureSuccess()
                                .asJsonObject()
                                .getBody();

                        if (result == null || result.getString("result").equals("OK") == false)
                            throw new Exception("Server Error");

                    }catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                    initAckDelta(); // reset ack messages
                }
            }.start();

            //initAckDelta(); // reset ack messages

            mNotificationUpdateHandler.postDelayed(UpdateNotification, Constants.UPDATE_INTERVAL);
        }
    };

    private String splitToComponentTimes(int duration) {
        int hours = (int) duration / 3600;
        int remainder = (int) duration - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return String.format("%d:%d:%d", ints[0], ints[1], ints[2]);
    }
    private void initEmpaticaDeviceManager() {
        Log.d(TAG,"Starting: initEmpaticaDeviceManager");

        // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
        deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);
        // Initialize the Device Manager using your API key. You need to have Internet access at this point.
        deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // EmpaStatusDelegate
    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI
        Log.d(TAG,"didUpdateStatus");
        Log.d(TAG,status.name());

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            Log.d(TAG, status.name() + " - Turn on your device");
            // Start scanning
            deviceManager.startScanning();
            // The device manager has established a connection
        } else if (status == EmpaStatus.CONNECTED) {
            // Stop streaming after STREAMING_TIME
            Log.d(TAG,"CONNECTED");
            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {
            Log.d(TAG,"DISCONNECTED");
        }
    }

    @Override
    public void didEstablishConnection() {

    }

    @Override
    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {
        didUpdateOnWristStatus(status);
    }

    @Override
    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {
        if (status == EmpaSensorStatus.ON_WRIST) {
            Log.d(TAG,"ON WRIST");

        }
        else {
            Log.d(TAG,"NOT ON WRIST");

        }
    }

    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        Log.d(TAG,"didDiscoverDevice");

        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
                E4deviceName = deviceName;
                Log.d(TAG,"Device Name:" + deviceName);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Log.d(TAG,"Sorry, you can't connect to this device");
            }
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        // doing in Activity
        /**************/
//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    // Bulk Insert
   synchronized public void asyncBulkHandler(final ContentValues cv, final String type, final double timestamp){
        // bulk insert to improve async_query_handler
           // avoid Can't create handler inside thread that has not called Looper.prepare()
           Handler mHandler_t = new Handler(Looper.getMainLooper());
       mHandler_t.postDelayed(new Runnable() {
               @Override
               public void run() {
                   final DatabaseHandler time_handler = new DatabaseHandler(getContentResolver());
                   ContentValues S_time = new ContentValues();
                   S_time.put(DAO.LOG_FIELD_TIME, timestamp);
                   time_handler.startInsert(-1, null, DataProvider.CONTENT_URI_SensingTime, S_time);
               }
           }, 0);


       switch (type) {
           case "E4.TEMPERATURE":{

               if (BULK_COUNT_temp < Constants.DB_BULK_RATE -1){
                   multipleE4Data_temp[BULK_COUNT_temp] = cv;
                   BULK_COUNT_temp++;
               }else {
                   multipleE4Data_temp[BULK_COUNT_temp] = cv;
                   BULK_COUNT_temp = 0;
                   // avoid Can't create handler inside thread that has not called Looper.prepare()
                   Handler mHandler = new Handler(Looper.getMainLooper());
                   mHandler.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           final DatabaseHandler handler = new DatabaseHandler(getContentResolver());
                           handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_E4_TEMPERATURE, multipleE4Data_temp);

                       }
                   }, 0);
               }

               break;
           }

           case "E4.BVP":{

               if (BULK_COUNT_bvp < Constants.DB_BULK_RATE -1){
                   multipleE4Data_bvp[BULK_COUNT_bvp] = cv;
                   BULK_COUNT_bvp++;
               }else {
                   multipleE4Data_bvp[BULK_COUNT_bvp] = cv;
                   BULK_COUNT_bvp = 0;
                   // avoid Can't create handler inside thread that has not called Looper.prepare()
                   Handler mHandler = new Handler(Looper.getMainLooper());
                   mHandler.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           final DatabaseHandler handler = new DatabaseHandler(getContentResolver());

                           handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_E4_BVP, multipleE4Data_bvp);
                       }
                   }, 0);
               }

               break;
           }
           case "E4.GSR":{

               if (BULK_COUNT_gsr < Constants.DB_BULK_RATE -1){
                   multipleE4Data_gsr[BULK_COUNT_gsr] = cv;
                   BULK_COUNT_gsr++;
               }else {
                   multipleE4Data_gsr[BULK_COUNT_gsr] = cv;
                   BULK_COUNT_gsr = 0;
                   // avoid Can't create handler inside thread that has not called Looper.prepare()
                   Handler mHandler = new Handler(Looper.getMainLooper());
                   mHandler.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           final DatabaseHandler handler = new DatabaseHandler(getContentResolver());

                           handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_E4_GSR, multipleE4Data_gsr);
                       }
                   }, 0);
               }

               break;
           }
           case "E4.IBI":{

               if (BULK_COUNT_ibi < Constants.DB_BULK_RATE -1){
                   multipleE4Data_ibi[BULK_COUNT_ibi] = cv;
                   BULK_COUNT_ibi++;
               }else {
                   multipleE4Data_ibi[BULK_COUNT_ibi] = cv;
                   BULK_COUNT_ibi = 0;
                   // avoid Can't create handler inside thread that has not called Looper.prepare()
                   Handler mHandler = new Handler(Looper.getMainLooper());
                   mHandler.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           final DatabaseHandler handler = new DatabaseHandler(getContentResolver());

                           handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_E4_IBI, multipleE4Data_ibi);
                       }
                   }, 0);
               }

               break;
           }
           case "E4.ACC":{

               if (BULK_COUNT_acc < Constants.DB_BULK_RATE -1){
                   multipleE4Data_acc[BULK_COUNT_acc] = cv;
                   BULK_COUNT_acc++;
               }else {
                   multipleE4Data_acc[BULK_COUNT_acc] = cv;
                   BULK_COUNT_acc = 0;
                   // avoid Can't create handler inside thread that has not called Looper.prepare()
                   Handler mHandler = new Handler(Looper.getMainLooper());
                   mHandler.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           final DatabaseHandler handler = new DatabaseHandler(getContentResolver());

                           handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_E4_ACC, multipleE4Data_acc);
                       }
                   }, 0);
               }

               break;
           }

       }



        }

        synchronized public void lastBulkHandler(){
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final DatabaseHandler handler = new DatabaseHandler(getContentResolver());

                if (BULK_COUNT_acc <= Constants.DB_BULK_RATE -1){
                    handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_E4_ACC, multipleE4Data_acc);
                    BULK_COUNT_acc = 0;
                }

                if (BULK_COUNT_gsr <= Constants.DB_BULK_RATE -1){
                    handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_E4_GSR, multipleE4Data_gsr);
                    BULK_COUNT_gsr = 0;
                }

                if (BULK_COUNT_ibi <= Constants.DB_BULK_RATE -1){
                    handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_E4_IBI, multipleE4Data_ibi);
                    BULK_COUNT_ibi = 0;
                }

                if (BULK_COUNT_temp <= Constants.DB_BULK_RATE -1){
                    handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_E4_TEMPERATURE, multipleE4Data_temp);
                    BULK_COUNT_temp = 0;
                }

                if (BULK_COUNT_bvp <= Constants.DB_BULK_RATE -1){
                    handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_E4_BVP, multipleE4Data_bvp);
                    BULK_COUNT_bvp = 0;
                }

            }
        }, 0);
    }


    // EmpaDataDelegate
    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        //galvanic skin response
        // >>피부를 통해 측정되는 전기적 활동(반응)을 말하며, 정서적인 각성 상태를 나타내는 지표로 활용된다.
        try {
            gsrDelta++;
            ContentValues cv = new ContentValues();
            String type = "E4.GSR";
         //   cv.put(DAO.LOG_FIELD_PhoneNumber,phoneNumber);
            cv.put(DAO.LOG_FIELD_TYPE, type);
            cv.put(DAO.LOG_FIELD_REG, new Date().getTime());
            cv.put(DAO.LOG_FIELD_SENSINGDATA, gsr);
            cv.put(DAO.LOG_FIELD_TIME, timestamp);

//            JSONObject json = new JSONObject();
//            json.put("gsr", gsr);
//            json.put("E4_time", timestamp);
//            cv.put(DAO.LOG_FIELD_JSON, json.toString());
//            AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
//            };
//            /*token처리*/
//            handler.startInsert(-1, null, DataProvider.CONTENT_URI_E4_GSR, cv);

           asyncBulkHandler(cv,type,timestamp);
            //mDBhandler.startInsert(-1, null, DataProvider.CONTENT_URI_LOG, cv);
            Log.d(TAG, type + ":GSR: " + gsr);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        //Blood Volume Pulse (: 혈액 양,,맥박)
        try {
            bvpDelta++;
            ContentValues cv = new ContentValues();
            String type = "E4.BVP";
         //   cv.put(DAO.LOG_FIELD_PhoneNumber,phoneNumber);
            cv.put(DAO.LOG_FIELD_TYPE, type);
            cv.put(DAO.LOG_FIELD_REG, new Date().getTime());
            cv.put(DAO.LOG_FIELD_SENSINGDATA, bvp);
            cv.put(DAO.LOG_FIELD_TIME, timestamp);

//            JSONObject json = new JSONObject();
//            json.put("BVP", bvp);
//            json.put("E4_time", timestamp);
//
// cv.put(DAO.LOG_FIELD_JSON, json.toString());
            asyncBulkHandler(cv,type,timestamp);
            //mDBhandler.startInsert(-1, null, DataProvider.CONTENT_URI_LOG, cv);
//            AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
//            };
//            /*token처리*/
//            handler.startInsert(-1, null, DataProvider.CONTENT_URI_E4_BVP, cv);

            Log.d(TAG, type + ":BVP: " + bvp);
            HRV = bvp;
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        //심장이 뛰는 시간 간격(IBI: Inter Beat Interval)
        try {
            ibiDelta++;
            ContentValues cv = new ContentValues();
            String type = "E4.IBI";
         //   cv.put(DAO.LOG_FIELD_PhoneNumber,phoneNumber);
            cv.put(DAO.LOG_FIELD_TYPE, type);
            cv.put(DAO.LOG_FIELD_REG, new Date().getTime());
            cv.put(DAO.LOG_FIELD_SENSINGDATA, ibi);
            cv.put(DAO.LOG_FIELD_TIME, timestamp);
//            JSONObject json = new JSONObject();
//            json.put("IBI", ibi);
//            json.put("E4_time", timestamp);
//
//            cv.put(DAO.LOG_FIELD_JSON, json.toString());
            asyncBulkHandler(cv,type,timestamp);
            //mDBhandler.startInsert(-1, null, DataProvider.CONTENT_URI_LOG, cv);
//            AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
//            };
//            /*token처리*/
//            handler.startInsert(-1, null, DataProvider.CONTENT_URI_E4_IBI, cv);

            Log.d(TAG, type + ":IBI: " + ibi);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
    //체온
        try {
            tempDelta++;
            ContentValues cv = new ContentValues();
            String type = "E4.TEMPERATURE";
        //    cv.put(DAO.LOG_FIELD_PhoneNumber,phoneNumber);
            cv.put(DAO.LOG_FIELD_TYPE, type);
            cv.put(DAO.LOG_FIELD_REG, new Date().getTime());
            cv.put(DAO.LOG_FIELD_SENSINGDATA, temp);
            cv.put(DAO.LOG_FIELD_TIME, timestamp);

//            JSONObject json = new JSONObject();
//            json.put("skin_temp", temp);
//            json.put("E4_time", timestamp);
//
//            cv.put(DAO.LOG_FIELD_JSON, json.toString());
           asyncBulkHandler(cv,type,timestamp);
//            //mDBhandler.startInsert(-1, null, DataProvider.CONTENT_URI_LOG, cv);
//            AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
//            };
//            /*token처리*/
//            handler.startInsert(-1, null, DataProvider.CONTENT_URI_E4_TEMPERATURE, cv);
            Log.d(TAG, type + ":SKIN_TEMP: " + temp);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
    //움직임 감지
        try {
            accDelta++;
            ContentValues cv = new ContentValues();
            String type = "E4.ACC";
      //      cv.put(DAO.LOG_FIELD_PhoneNumber, phoneNumber);
            cv.put(DAO.LOG_FIELD_TYPE, type);
            cv.put(DAO.LOG_FIELD_REG, new Date().getTime());
            cv.put(DAO.LOG_FIELD_X, x);
            cv.put(DAO.LOG_FIELD_Y, y);
            cv.put(DAO.LOG_FIELD_Z, z);
            cv.put(DAO.LOG_FIELD_TIME, timestamp);

//            JSONObject json = new JSONObject();
//            json.put("x", x);
//            json.put("y", y);
//            json.put("z", z);
//            json.put("E4_time", timestamp);
//
//            cv.put(DAO.LOG_FIELD_JSON, json.toString());
            asyncBulkHandler(cv,type,timestamp);

//            AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
//            };
//            /*token처리*/
//            handler.startInsert(-1, null, DataProvider.CONTENT_URI_E4_ACC, cv);


            Log.d(TAG, type + ":x: " + x+", y: " + y+", z: " + z);
        }catch(Exception e){
            Log.e(TAG,e.getLocalizedMessage());
        }
    }

    Handler mDBHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            // This is where you do your work in the UI thread.
            // Your worker tells you in the message what to do.
        }
    };

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        Log.d(TAG, String.format("Battery: %.0f %%", battery * 100));
    }

    @Override
    public void didReceiveTag(double timestamp) {

    }
}
