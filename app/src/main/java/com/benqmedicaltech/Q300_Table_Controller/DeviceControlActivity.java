/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.benqmedicaltech.Q300_Table_Controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BLEService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_SCAN_RESULT = "SCAN_RESULT";

    public static String UUID_GAP = "00001800-0000-1000-8000-00805f9b34fb";
    public static String UUID_GATT = "00001801-0000-1000-8000-00805f9b34fb";

    private static final int REQUEST_DISCONNECT = 5;
    private static final int RESULT_DISCONNECT = 5;

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private View mGattServicesListHeaderView;
    private BLEService mBleService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private boolean mConnecting = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ScanResult mScanResult;
    private byte[] mScanResultDep;
    private int skipIndex = 0;
    //private Handler mHandler = null;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private final String LIST_PROPERTIES = "PROPERTIES";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BLEService.LocalBinder) service).getService();
            if (!mBleService.initialize()) {
                //Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            updateConnectionState(R.string.disconnected);
            invalidateOptionsMenu();

            /*
            mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    invalidateOptionsMenu();
                }
            }, 1000);
            */

            // Automatically connects to the device upon successful start-up initialization.
            //mBleService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                skipIndex = 0;
                mConnected = true;
                mConnecting = false;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_BONDED.equals(action)) {
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();

            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                skipIndex = 0;
                mConnected = false;
                mConnecting = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BLEService.ACTION_GATT_CONNECTING.equals(action)) {
                mConnecting = true;
                mConnected = false;
                updateConnectionState(R.string.connecting);
                invalidateOptionsMenu();

            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBleService.getSupportedGattServices());
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BLEService.EXTRA_DATA));
            }
        }
    };

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    case BluetoothAdapter.STATE_OFF:
                        //case BluetoothAdapter.STATE_TURNING_OFF:
                        finish();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            mBleService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;

                            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                mBleService.setCharacteristicNotification(characteristic, false);
                            } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                                mBleService.setCharacteristicIndication(characteristic, false);
                            }
                            //Log.d("Index1:", Integer.toString(groupPosition) + Integer.toString(childPosition));
                            final Intent intent = new Intent(DeviceControlActivity.this, DeviceCharacteristicActivity.class);
                            intent.putExtra(DeviceCharacteristicActivity.EXTRAS_SERVICE_INDEX, (new Integer(groupPosition + skipIndex)).toString());
                            intent.putExtra(DeviceCharacteristicActivity.EXTRAS_CHARACTERISTIC_INDEX, (new Integer(childPosition)).toString());
                            startActivityForResult(intent, REQUEST_DISCONNECT);
                        }
                        return true;
                    }
                    return false;
                }
            };

    private void clearUI() {
        mGattServicesList.removeHeaderView(mGattServicesListHeaderView);
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gatt_services_characteristics);


        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mScanResultDep = intent.getByteArrayExtra(EXTRAS_SCAN_RESULT);
        else
            mScanResult = intent.getParcelableExtra(EXTRAS_SCAN_RESULT);

        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        if (mDeviceName != null && mDeviceName.length() > 0)
            ((TextView) findViewById(R.id.device_name)).setText(mDeviceName);
        else
            ((TextView) findViewById(R.id.device_name)).setText(R.string.unknown_device);

        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesListHeaderView = (LayoutInflater.from(mGattServicesList.getContext())).inflate(R.layout.list_header, null);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        getActionBar().setTitle(R.string.title_control);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //Mick add
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setLogo(R.drawable.iqor_logo);
        getActionBar().setDisplayUseLogoEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        //Mick add

        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        String advertisementData = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            advertisementData = "(Detailed view not supported on this Android version!)\n";
            for (int i=0; i < mScanResultDep.length; i++)
                advertisementData += (int) mScanResultDep[i] + " ";

        } else {

            if ((mScanResult.getScanRecord().getDeviceName() != null) && (!mScanResult.getScanRecord().getDeviceName().isEmpty()))
                advertisementData = advertisementData + "Device Name: " + mScanResult.getScanRecord().getDeviceName() + "\n";

            if ((mScanResult.getScanRecord().getManufacturerSpecificData() != null) && (mScanResult.getScanRecord().getManufacturerSpecificData().size() != 0)) {
                advertisementData = advertisementData + "Manufacturer Data: ";

                for (int i = 0; i < mScanResult.getScanRecord().getManufacturerSpecificData().size(); i++) {
                    int key = mScanResult.getScanRecord().getManufacturerSpecificData().keyAt(i);
                    byte[] data = (mScanResult.getScanRecord().getManufacturerSpecificData().get(key));
                    for (int j = 0; j < data.length; j++) {
                        advertisementData += (String.format("%02x ", ((int) data[j]) & 0xff));
                    }

                }
                advertisementData = advertisementData + "\n";
            }

            if ((mScanResult.getScanRecord().getServiceData() != null) && (mScanResult.getScanRecord().getServiceData().size() != 0)) {
                advertisementData = advertisementData + "Service Data: ";
                Map<ParcelUuid, byte[]> serviceDatas = mScanResult.getScanRecord().getServiceData();

                for (Map.Entry<ParcelUuid, byte[]> serviceData : serviceDatas.entrySet()) {
                    advertisementData = advertisementData + serviceData.getKey() + "-";
                    byte[] data = serviceData.getValue();
                    for (int j = 0; j < data.length; j++) {
                        advertisementData += (String.format("%02x ", ((int) data[j]) & 0xff));
                    }
                }
                advertisementData = advertisementData + "\n";
                //advertisementData = advertisementData + "Service Data: " + mScanResult.getScanRecord().getServiceData().toString().replace('{', ' ').replace('}', ' ') + "\n";
            }

            if ((mScanResult.getScanRecord().getServiceUuids() != null) && (mScanResult.getScanRecord().getServiceUuids().size() != 0))
                advertisementData = advertisementData + "Service UUIDs: " + mScanResult.getScanRecord().getServiceUuids().toString().replace('[', ' ').replace(']', ' ') + "\n";

            if (mScanResult.getScanRecord().getTxPowerLevel() != Integer.MIN_VALUE)
                advertisementData = advertisementData + "TX Power: " + mScanResult.getScanRecord().getTxPowerLevel() + "dBm\n";
        }
        if (advertisementData.isEmpty()) {
            advertisementData = "-";
        }
        mDataField.setText(advertisementData);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        /*
        if (mBleService != null) {
            final boolean result = mBleService.connect(mDeviceAddress);
            //Log.d(TAG, "Connect request result=" + result);
        }
        */
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*
        if (mHandler != null) {
            mHandler.removeCallbacks(null);
        }
        */

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGattUpdateReceiver != null) {
            unregisterReceiver(mGattUpdateReceiver);
        }
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver);
        }

        if (mBleService.serviceStatus) {
            unbindService(mServiceConnection);
        }
        mBleService.close();
        mBleService = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DISCONNECT && resultCode == RESULT_DISCONNECT) {
            mConnected = false;
            updateConnectionState(R.string.disconnected);
            invalidateOptionsMenu();
            clearUI();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);

        if (mBleService != null) {
            menu.findItem(R.id.menu_more).setVisible(true);
            if (mBleService.isBonded(mDeviceAddress)) {
                menu.findItem(R.id.menu_bond).setVisible(false);
                menu.findItem(R.id.menu_unbond).setVisible(true);
            } else {
                menu.findItem(R.id.menu_unbond).setVisible(false);
                menu.findItem(R.id.menu_bond).setVisible(true);
            }
        } else {
            menu.findItem(R.id.menu_more).setVisible(false);
        }


        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            menu.findItem(R.id.menu_bond).setEnabled(false);
            menu.findItem(R.id.menu_unbond).setEnabled(false);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            if (mConnecting) {
                // MBD
                // menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
                menu.findItem(R.id.menu_connect).setEnabled(false);
                menu.findItem(R.id.menu_bond).setEnabled(false);
                menu.findItem(R.id.menu_unbond).setEnabled(false);
            } else {
                // MBD
                //menu.findItem(R.id.menu_refresh).setActionView(null);
                menu.findItem(R.id.menu_connect).setEnabled(true);
                menu.findItem(R.id.menu_bond).setEnabled(true);
                menu.findItem(R.id.menu_unbond).setEnabled(true);
            }
            /*
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            menu.findItem(R.id.menu_bond).setEnabled(true);
            menu.findItem(R.id.menu_unbond).setEnabled(true);
            */
            super.onCreateOptionsMenu(menu);
        }



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      /* MBD  switch(item.getItemId()) {
            case R.id.menu_connect:
                mBleService.connect(mDeviceAddress, false);
                return true;

            case R.id.menu_disconnect:
                mBleService.disconnect();
                return true;

            case R.id.menu_bond:
                mBleService.connect(mDeviceAddress, true);
                return true;

            case R.id.menu_unbond:
                mBleService.unBond(mDeviceAddress);
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;
        }*/

        int id = item.getItemId();

        if (id == R.id.menu_connect ) {
            mBleService.connect(mDeviceAddress, false);
            return true;
        } else if (id == R.id.menu_disconnect ) {

            mBleService.disconnect();
            return true;
        } else if (id == R.id.menu_bond ) {

            mBleService.connect(mDeviceAddress, true);
            return true;
        }else if (id == R.id.menu_unbond ) {

            mBleService.unBond(mDeviceAddress);
            return true;
        }else if (id == android.R.id.home ) {

            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBleService.isBonded(mDeviceAddress)) {
                    mConnectionState.setText(getResources().getString(resourceId) + " - Bonded");
                } else {
                    mConnectionState.setText(resourceId);
                }
            }
        });
    }

    private void displayData(String data) {

        if (data != null) {
            //mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            mGattServicesList.removeHeaderView(mGattServicesListHeaderView);
            mGattServicesList.addHeaderView(mGattServicesListHeaderView, null, false);
            mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
            return;
        }
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            if (!(uuid.matches(UUID_GAP) || uuid.matches(UUID_GATT)))
            {

                currentServiceData.put(
                        LIST_NAME, BLEGattAttributes.lookup(uuid, unknownServiceString));

                if (unknownServiceString.matches(currentServiceData.get(LIST_NAME)) || currentServiceData.get(LIST_NAME).contains("Microchip"))
                    currentServiceData.put(LIST_UUID, uuid);
                else {
                    currentServiceData.put(LIST_NAME, currentServiceData.get(LIST_NAME) + " Service");
                    currentServiceData.put(LIST_UUID, uuid.substring(4, 8));
                }

                gattServiceData.add(currentServiceData);

                ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                        new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> charas =
                        new ArrayList<BluetoothGattCharacteristic>();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);
                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
                    uuid = gattCharacteristic.getUuid().toString();
                    currentCharaData.put(
                            LIST_NAME, BLEGattAttributes.lookup(uuid, unknownCharaString));

                    String characterestic_info = "";
                    if (unknownCharaString.matches(currentCharaData.get(LIST_NAME)) || currentCharaData.get(LIST_NAME).contains("Microchip")) {
                        currentCharaData.put(LIST_NAME, currentCharaData.get(LIST_NAME) + " Characteristic");
                        characterestic_info = uuid;
                    }
                    else
                        characterestic_info = uuid.substring(4, 8);

                    currentCharaData.put(LIST_UUID, characterestic_info);

                    characterestic_info = "";
                    if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0)
                        characterestic_info = characterestic_info.concat("Read ");

                    if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
                        characterestic_info = characterestic_info.concat("Write ");

                    if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0)
                        characterestic_info = characterestic_info.concat("WriteNoResponse ");

                    if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) > 0)
                        characterestic_info = characterestic_info.concat("SignedWrite ");

                    if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
                        characterestic_info = characterestic_info.concat("Notify ");

                    if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0)
                        characterestic_info = characterestic_info.concat("Indicate ");

                    if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_BROADCAST) > 0)
                        characterestic_info = characterestic_info.concat("Broadcast ");

                    if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) > 0)
                        characterestic_info = characterestic_info.concat("Extended ");

                    currentCharaData.put(LIST_PROPERTIES, characterestic_info);

                    gattCharacteristicGroupData.add(currentCharaData);
                }
                mGattCharacteristics.add(charas);
                gattCharacteristicData.add(gattCharacteristicGroupData);
            }
            else
                skipIndex++;
        }

        if (gattServiceData.isEmpty() && gattCharacteristicData.isEmpty()) {
            mGattServicesList.removeHeaderView(mGattServicesListHeaderView);
            mGattServicesList.addHeaderView(mGattServicesListHeaderView, null, false);
            mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        } else {

            SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                    this,
                    gattServiceData,
                    android.R.layout.simple_expandable_list_item_2,
                    new String[]{LIST_NAME, LIST_UUID},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    gattCharacteristicData,
                    R.layout.simple_expandable_list_item_3,
                    new String[]{LIST_NAME, LIST_UUID, LIST_PROPERTIES},
                    new int[]{android.R.id.text1, android.R.id.text2, R.id.text3}
            );
            mGattServicesList.setAdapter(gattServiceAdapter);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BLEService.ACTION_GATT_BONDED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }
}
