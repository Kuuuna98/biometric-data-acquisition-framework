/**************************************************************************************************
 * Filename:       DeviceActivity.java
 * <p>
 * Copyright (c) 2013 - 2014 Texas Instruments Incorporated
 * <p>
 * All rights reserved not granted herein.
 * Limited License.
 * <p>
 * Texas Instruments Incorporated grants a world-wide, royalty-free,
 * non-exclusive license under copyrights and patents it now or hereafter
 * owns or controls to make, have made, use, import, offer to sell and sell ("Utilize")
 * this software subject to the terms herein.  With respect to the foregoing patent
 * license, such license is granted  solely to the extent that any such patent is necessary
 * to Utilize the software alone.  The patent license shall not apply to any combinations which
 * include this software, other than combinations with devices manufactured by or for TI ('TI Devices').
 * No hardware patent is licensed hereunder.
 * <p>
 * Redistributions must preserve existing copyright notices and reproduce this license (including the
 * above copyright notice and the disclaimer and (if applicable) source code license limitations below)
 * in the documentation and/or other materials provided with the distribution
 * <p>
 * Redistribution and use in binary form, without modification, are permitted provided that the following
 * conditions are met:
 * <p>
 * No reverse engineering, decompilation, or disassembly of this software is permitted with respect to any
 * software provided in binary form.
 * any redistribution and use are licensed by TI for use only with TI Devices.
 * Nothing shall obligate TI to provide you with source code for the software licensed and provided to you in object code.
 * <p>
 * If software source code is provided to you, modification and redistribution of the source code are permitted
 * provided that the following conditions are met:
 * <p>
 * any redistribution and use of the source code, including any resulting derivative works, are licensed by
 * TI for use only with TI Devices.
 * any redistribution and use of any object code compiled from the source code and any resulting derivative
 * works, are licensed by TI for use only with TI Devices.
 * <p>
 * Neither the name of Texas Instruments Incorporated nor the names of its suppliers may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * <p>
 * DISCLAIMER.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY TI AND TI'S LICENSORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL TI AND TI'S LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **************************************************************************************************/
package com.kaist.iclab.services;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.goebl.david.Webb;
import com.kaist.iclab.R;
import com.kaist.iclab.activity.MainActivity;
import com.kaist.iclab.datamanager.DAO;
import com.kaist.iclab.datamanager.DataProvider;
import com.kaist.iclab.devices.BluetoothGATTDefines;
import com.kaist.iclab.devices.GattInfo;
import com.kaist.iclab.util.Point3D;
import com.kaist.iclab.util.SensorTagValueTransform;
import com.kaist.iclab.util.Constants;

import org.json.JSONObject;

import java.util.Date;

public class SensorTagService extends Service {
    private static String TAG = "SensorTagService";
    private BroadcastReceiver mGattUpdateReceiver;

    private static int NOTIFYID_STAG = 20180320;
    private String CHANNEL_ID = "STAG_SERVICE";

    private NotificationManager nManager = null;
    private NotificationCompat.Builder ncomp = null;
    private int sensingCount = 0;
    private String mConnectedTag = "S-Tag service";

    private String deviceSetName ="";

    private int accDelta;
    private int gyroDelta;

    private PowerManager mPowermanager;
    private PowerManager.WakeLock mWakeLock;

    // BLE
    @Override
    public void onCreate() {
        super.onCreate();
        // GATT database
        Resources res = getResources();
        XmlResourceParser xpp = res.getXml(R.xml.gatt_uuid);
        new GattInfo(xpp);
        InitNotification();
        Enable();
        initAckDelta();
        mGattUpdateReceiver = new SensorTagReceiver(getApplicationContext());
    }
    public void initAckDelta(){
        accDelta = 0;
        gyroDelta = 0;
    }

    public static final String ACTION_MOVEMENT_UPDATE = "com.kaist.iclab.services.sensortagservice.ACTION_MOVEMENT_UPDATE";
    public static final String EXTRA_ENABLED = "com.kaist.iclab.services.sensortagservice.EXTRA_ENABLED";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final IntentFilter fi = new IntentFilter();
        fi.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        fi.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
        fi.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        fi.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        fi.addAction(ACTION_MOVEMENT_UPDATE);
        registerReceiver(mGattUpdateReceiver, fi);

        deviceSetName = intent.getStringExtra("device_set_name");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        Disable();
        Log.d(TAG, "onDestroy");
        //Logger.closeDB();
    }


    private void Enable(){
        mNotificationUpdateHandler.postDelayed(UpdateNotification, Constants.UPDATE_INTERVAL * 1);

        // WakeLock code section
        mPowermanager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowermanager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeAlways");
        mWakeLock.acquire();

    }
    private void Disable(){
        nManager.cancel(NOTIFYID_STAG);
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
        ncomp.setContentTitle("STAG Service");
        ncomp.setContentText("Preparing");
        ncomp.setTicker("STAG Service");
        ncomp.setSmallIcon(R.drawable.tible);
        ncomp.setAutoCancel(false);
        ncomp.setOngoing(true);

        nManager.notify(NOTIFYID_STAG, ncomp.build());
    }
    private Handler mNotificationUpdateHandler = new Handler();
    private Runnable UpdateNotification = new Runnable()
    {
        @Override
        public void run() {
            ncomp.setContentTitle(mConnectedTag + splitToComponentTimes(sensingCount += Constants.ACK_RATE));
            ncomp.setContentText("accDelta:"+ accDelta+" gyroDelta: "+gyroDelta);
            ncomp.setSmallIcon(R.drawable.tible);
            ncomp.setAutoCancel(false);
            ncomp.setOngoing(true);

            nManager.notify(NOTIFYID_STAG, ncomp.build());

            // Send ACK message
            new Thread() {
                public void run() {
                    Webb webb = Webb.create();
                    try {
                        JSONObject body = new JSONObject();
                        body.put("setNumber", deviceSetName);
                        body.put("deviceType","sensorTag");
                        body.put("accDelta", accDelta);
                        body.put("gyroDelta", gyroDelta);

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
            mNotificationUpdateHandler.postDelayed(UpdateNotification, Constants.UPDATE_INTERVAL * 1);
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class SensorTagReceiver extends BroadcastReceiver {
        private String TAG = SensorTagReceiver.class.getSimpleName();
        private BluetoothGattCharacteristic dataC;
        private boolean isMovementUpdateEnabled = false;

        private final Context context;
        public SensorTagReceiver(Context context) {
            this.context = context;
            Log.d(TAG, "GATT: strated");
        }

        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(ACTION_MOVEMENT_UPDATE)){
                isMovementUpdateEnabled = intent.getBooleanExtra(EXTRA_ENABLED,false);
                Log.d(TAG, "GATT:"+"onReceive"+ String.format("action = %s, isEnabled = %s",action, isMovementUpdateEnabled));
                //Logger._debug(Logger.DOMAIN_GATT, "onReceive", String.format("action = %s, isEnabled = %s",action, isMovementUpdateEnabled));
                return;
            }

            final int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);
            final String address = intent.getStringExtra(BluetoothLeService.EXTRA_DEVICE_ADDRESS);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                try {
                    Log.e(TAG, "connection failed with GATT error code = " + BluetoothGATTDefines.gattErrorCodeStrings.get(status));
                    //Logger.gattError(address, "connection failed with GATT error code = " + BluetoothGATTDefines.gattErrorCodeStrings.get(status));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                BluetoothLeService mBtLeService = BluetoothLeService.getInstance();
                mBtLeService.abortTimedDisconnect();
                BluetoothGatt mBtGatt = BluetoothLeService.getBluetoothGatt(address);
                mConnectedTag = address; // to show it in a notification
                if (mBtGatt != null && mBtLeService.getNumServices(address) == 0)
                    mBtGatt.discoverServices();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //mBluetoothLeService.close();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                BluetoothLeService mBtLeService = BluetoothLeService.getInstance();
                for (BluetoothGattService s : mBtLeService.getSupportedGattServices(address)) {
                    if ((s.getUuid().toString().compareTo(GattInfo.UUID_MOV_SERV.toString())) == 0) {
                        int error = 0;
                        for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                            if (c.getUuid().toString().equals(GattInfo.UUID_MOV_DATA.toString())) {
                                dataC = c;
                                if ((error = mBtLeService.setCharacteristicNotification(address, c, true)) != 0)
                                    Log.e(TAG,"movement notification enable failed with error code = " + error);
                                    //Logger.gattError(address, "movement notification enable failed with error code = " + error);
                            }
                            if (c.getUuid().toString().equals(GattInfo.UUID_MOV_CONF.toString())) {
                                byte b[] = new byte[]{0x7F, 0x00};
                                b[0] = (byte) 0xFF;
                                if ((error = mBtLeService.writeCharacteristic(address, c, b)) != 0)
                                    Log.e(TAG,"movement config failed with error code = " + error);
                                    //Logger.gattError(address, "movement config failed with error code = " + error);
                            }
                            if (c.getUuid().toString().equals(GattInfo.UUID_MOV_PERI.toString())) {
                                // perdiod 100 ~ 2450
                                int period = 100;
                                byte p = (byte) ((period / 10) + 10);
                                if ((error = mBtLeService.writeCharacteristic(address, c, p)) != 0)
                                    Log.e(TAG,"movement period failed with error code = " + error);
                                    //Logger.gattError(address, "movement period failed with error code = " + error);
                            }
                        }
                        //Logger.gattEvent(System.currentTimeMillis(), address, "found movement", null);
                        break;
                    }
                }

            } else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
                if (this.dataC != null) {
                    String device_UUID = intent.getStringExtra(BluetoothLeService.EXTRA_DEVICE_ADDRESS);
                    final byte[] value = dataC.getValue();

                    Point3D accValue = SensorTagValueTransform.convert_MOVEMENT_ACC(value);
                    Point3D gyroValue = SensorTagValueTransform.convert_MOVEMENT_GYRO(value);
                    Point3D magValue = SensorTagValueTransform.convert_MOVEMENT_MAG(value);

                    accDelta++;
                    gyroDelta++;

                    try {
                        ContentValues cv = new ContentValues();
                        String type = "STAG.VALUE";
                        cv.put(DAO.LOG_FIELD_TYPE, type);
                        cv.put(DAO.LOG_FIELD_REG, new Date().getTime());
                        JSONObject json = new JSONObject();
                        json.put("acc.x", accValue.x);
                        json.put("acc.y", accValue.y);
                        json.put("acc.z", accValue.x);
                        json.put("gyro.x", gyroValue.x);
                        json.put("gyro.y", gyroValue.y);
                        json.put("gyro.z", gyroValue.z);
                        json.put("mag.x", magValue.x);
                        json.put("mag.y", magValue.y);
                        json.put("mag.z", magValue.z);
                        cv.put(DAO.LOG_FIELD_JSON, json.toString());
                        AsyncQueryHandler mDBhandler = new AsyncQueryHandler(getContentResolver()) {};
                        mDBhandler.startInsert(-1, null, DataProvider.CONTENT_URI_LOG, cv);
                        Log.d(TAG, device_UUID + "movement value: "+json.toString());
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }

                    if(isMovementUpdateEnabled) {
                        Log.d(TAG, "BluetoothLeService value = "+value);

                        //Intent actionIntent = new Intent();
                        //actionIntent.setAction(MovementActivity.ACTION_MOVEMENT_UPDATE_NOTIFY);
                        //actionIntent.putExtra(MovementActivity.EXTRA_MOVEMENT_DEVICE_ADDRESS, address);
                        //actionIntent.putExtra(MovementActivity.EXTRA_MOVEMENT_VALUE, value);
                        //context.sendBroadcast(actionIntent);
                    }
                }
                //Log.d("movement_value", dataC.getValue().toString());
            }
        }
    };
}
