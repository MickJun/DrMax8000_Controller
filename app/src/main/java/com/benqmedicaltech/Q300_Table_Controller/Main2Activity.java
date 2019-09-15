package com.benqmedicaltech.Q300_Table_Controller;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class Main2Activity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = Main2Activity.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private Handler mHandler = null;
    public boolean serviceStatus = false;
    public BluetoothDevice mDevice = null;
    private boolean mBond = false;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.microchip.bluetoothsmartdiscover.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.microchip.bluetoothsmartdiscover.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_CONNECTING =
            "com.microchip.bluetoothsmartdiscover.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.microchip.bluetoothsmartdiscover.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.microchip.bluetoothsmartdiscover.ACTION_DATA_AVAILABLE";
    public final static String ACTION_GATT_WRITE =
            "com.microchip.bluetoothsmartdiscover.ACTION_GATT_WRITE";
    public final static String EXTRA_DATA =
            "com.microchip.bluetoothsmartdiscover.EXTRA_DATA";
    public final static String ACTION_GATT_BONDED =
            "com.microchip.bluetoothsmartmesh.ACTION_GATT_BONDED";

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction);
                    Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());

                    if (mDevice != null) {
                        if (mDevice.getBondState() == BluetoothDevice.BOND_NONE && mBond == true) {
                            mDevice.createBond();
                        } else {
                            mBluetoothGatt.discoverServices();
                        }

                    }

                } else {
                    Log.i(TAG, "onConnectionStateChange: " + status + " : " + newState);
                    mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connect(mBluetoothDeviceAddress, mBond);
                        }
                    }, 1000);
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
                //close(); //JAY
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                intentAction = ACTION_GATT_CONNECTING;
                mConnectionState = STATE_CONNECTING;
                Log.i(TAG, "Connecting to GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    broadcastUpdate(ACTION_GATT_WRITE, characteristic);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                Log.d(TAG, String.format("Received data:)" + new String(data) + "\n" + stringBuilder.toString()));

                if (BLEGattAttributes.lookup(characteristic.getService().getUuid().toString(), "Unknown Service").matches("Device Information"))
                    intent.putExtra(EXTRA_DATA, stringBuilder.toString() + "\n" + new String(data));
                else if (BLEGattAttributes.lookup(characteristic.getService().getUuid().toString(), "Unknown Service").matches("Battery"))
                    intent.putExtra(EXTRA_DATA, stringBuilder.toString() + "\n" + Integer.valueOf(stringBuilder.toString().trim(), 16) +  "%");
                else
                    intent.putExtra(EXTRA_DATA, stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address, boolean bond) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        mBond = bond;
        mGattCallback.onConnectionStateChange(mBluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING);   //JAY

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    public LeDeviceListAdapter mLeDeviceListAdapter;
    //public BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private boolean mScanning;
    private boolean mBluetoothStatus = false;
    private List<Integer> mRSSIs;
    private List<Long> mAdvTimes;
    private List<Long> mpAdvTimes;
    private List<ScanResult> mScanResults;
    private List<byte[]> mScanResultsDep;

    //private Handler mHandler;
    private Runnable mRunnable;
    private ScanCallback mScanCallback;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOCATION = 1;
    private static final long SCAN_PERIOD = 30000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private Button M2_Scan;
    private Button M2_Connect;
    private Button M2_Update;

    private ListView M2_ListView ;
    private TextView M2_TextView ;

    private final ArrayList<String> BT_Devicelist = new ArrayList<>();
    private final ArrayList<String> BT_Addrlist = new ArrayList<>();
    private final ArrayList<Integer> BT_Count = new ArrayList<>();

    private ArrayList<BluetoothDevice> BT_LeDevices = new ArrayList<>();

    private int BT_Select_Point = 0;

    //connect
    private BLEService mBleService ;


    private String mDeviceName;
    private String mDeviceAddress;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    case BluetoothAdapter.STATE_OFF:
                        //case BluetoothAdapter.STATE_TURNING_OFF:
                        mBluetoothStatus = true;
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.app_name)
                                .setMessage(R.string.error_bluetooth_off)
                                .setIcon(R.mipmap.ic_launcher)
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(Main2Activity.this, R.string.error_bluetooth_off, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .show();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.iqor_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        M2_TextView = this.findViewById(R.id.Main2_text);
        M2_TextView.setTextSize(25);
        M2_TextView.setText("Press SCAN Button");
        M2_ListView = this.findViewById(R.id.Main2_List);

        M2_Scan = this.findViewById(R.id.Main2_button1);
        M2_Scan.setOnClickListener(this);
        M2_Connect = this.findViewById(R.id.Main2_button2);
        M2_Connect.setOnClickListener(this);
        M2_Update = this.findViewById(R.id.Main2_button3);
        M2_Update.setOnClickListener(this);

        mGattServicesList = (ExpandableListView) findViewById(R.id.M2_gatt_services_list);
        mGattServicesListHeaderView = (LayoutInflater.from(mGattServicesList.getContext())).inflate(R.layout.list_header, null);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);

        // MBD
//        getActionBar().setTitle(R.string.smart_discovrey_title_devices);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /*
        View headerView = getLayoutInflater().inflate(R.layout.list_header, getListView(), false);
        ((TextView)headerView.findViewById(R.id.listHeader)).setText(R.string.);
        getListView().addHeaderView(, null, false);
        */
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mRSSIs = new ArrayList<Integer>();
        mAdvTimes = new ArrayList<Long>();
        mpAdvTimes = new ArrayList<Long>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mScanResultsDep = new ArrayList<byte[]>();
        else
            mScanResults = new ArrayList<ScanResult>();

        //setListAdapter(mLeDeviceListAdapter);


        setLECallbacks();

        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mRSSIs = new ArrayList<Integer>();
        mAdvTimes = new ArrayList<Long>();
        mpAdvTimes = new ArrayList<Long>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mScanResultsDep = new ArrayList<byte[]>();
        else
            mScanResults = new ArrayList<ScanResult>();


        setLECallbacks();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled() && !mBluetoothStatus) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder() //
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) //
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) //
                        .build();

                ScanFilter filter = new ScanFilter.Builder().setDeviceName(null).build();
                filters = new ArrayList<ScanFilter>();
                filters.add(filter);
            }

            scanLeDevice(true);
        }



    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator =  Main2Activity.this.getLayoutInflater();//(LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); //DeviceScanActivity.this.getLayoutInflater(); //(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); //
        }

        public void addDevice(BluetoothDevice device, int rssi, ScanResult scanResult) {

            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                mRSSIs.add(rssi);
                mpAdvTimes.add(Long.valueOf(0));
                mAdvTimes.add(scanResult.getTimestampNanos());
                mScanResults.add(scanResult);
                BT_LeDevices.add(device);
            } else {
                mRSSIs.remove(mLeDevices.indexOf(device));
                mRSSIs.add(mLeDevices.indexOf(device), rssi);

                /*
                mpAdvTimes.remove(mLeDevices.indexOf(device));
                mpAdvTimes.add(mLeDevices.indexOf(device), mAdvTimes.get(mLeDevices.indexOf(device)));
                mAdvTimes.remove(mLeDevices.indexOf(device));
                mAdvTimes.add(mLeDevices.indexOf(device), scanResult.getTimestampNanos());
                */


                Long currentTime = scanResult.getTimestampNanos();
                Long prevTime = mAdvTimes.get(mLeDevices.indexOf(device));
                Long pprevTime = mpAdvTimes.get(mLeDevices.indexOf(device));
                if ((currentTime - prevTime) < (prevTime - pprevTime)) {
                    mpAdvTimes.remove(mLeDevices.indexOf(device));
                    mpAdvTimes.add(mLeDevices.indexOf(device), mAdvTimes.get(mLeDevices.indexOf(device)));
                    mAdvTimes.remove(mLeDevices.indexOf(device));
                    mAdvTimes.add(mLeDevices.indexOf(device), currentTime);
                }


                mScanResults.remove(mLeDevices.indexOf(device));
                mScanResults.add(mLeDevices.indexOf(device), scanResult);
            }

        }

        public void addDeviceDep(BluetoothDevice device, int rssi, byte[] scanResult) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                mRSSIs.add(rssi);
                mpAdvTimes.add(Long.valueOf(0));
                mAdvTimes.add(System.currentTimeMillis());
                mScanResultsDep.add(scanResult);
            } else {
                mRSSIs.remove(mLeDevices.indexOf(device));
                mRSSIs.add(mLeDevices.indexOf(device), rssi);

                /*
                mpAdvTimes.remove(mLeDevices.indexOf(device));
                mpAdvTimes.add(mLeDevices.indexOf(device), mAdvTimes.get(mLeDevices.indexOf(device)));
                mAdvTimes.remove(mLeDevices.indexOf(device));
                mAdvTimes.add(mLeDevices.indexOf(device), System.currentTimeMillis());
                */


                Long currentTime = System.currentTimeMillis();
                Long prevTime = mAdvTimes.get(mLeDevices.indexOf(device));
                Long pprevTime = mpAdvTimes.get(mLeDevices.indexOf(device));
                if ((currentTime - prevTime) < (prevTime - pprevTime)) {
                    mpAdvTimes.remove(mLeDevices.indexOf(device));
                    mpAdvTimes.add(mLeDevices.indexOf(device), mAdvTimes.get(mLeDevices.indexOf(device)));
                    mAdvTimes.remove(mLeDevices.indexOf(device));
                    mAdvTimes.add(mLeDevices.indexOf(device), currentTime);
                }


                mScanResultsDep.remove(mLeDevices.indexOf(device));
                mScanResultsDep.add(mLeDevices.indexOf(device), scanResult);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            DeviceScanActivity.ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new DeviceScanActivity.ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.deviceRSSI = (TextView) view.findViewById(R.id.device_rssi);
                viewHolder.deviceAdvInterval = (TextView) view.findViewById(R.id.device_interval);
                view.setTag(viewHolder);
            } else {
                viewHolder = (DeviceScanActivity.ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());
            viewHolder.deviceRSSI.setText(/*"RSSI: " +*/ mRSSIs.get(i) + "dB");

            Long advertisingInterval = mAdvTimes.get(i) - mpAdvTimes.get(i);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                advertisingInterval /= 1000000;
            }


            if (advertisingInterval > 10000 || advertisingInterval < 15) {
                viewHolder.deviceAdvInterval.setText("-"/*"Adv Interval: -"*/);
            } else {
                viewHolder.deviceAdvInterval.setText(/*"Adv Interval: " +*/ advertisingInterval + "ms");
            }

            int rssi = mRSSIs.get(i);
//            if (rssi < -27 && rssi > -110) {
//                if (rssi <= -27 && rssi > -60)
//                    ((ImageView) view.findViewById(R.id.image_rssi)).setImageResource(R.drawable.rssi_strength100);
//
//                if (rssi <= -60 && rssi > -70)
//                    ((ImageView) view.findViewById(R.id.image_rssi)).setImageResource(R.drawable.rssi_strength75);
//
//                if (rssi <= -70 && rssi > -80)
//                    ((ImageView) view.findViewById(R.id.image_rssi)).setImageResource(R.drawable.rssi_strength50);
//
//                if (rssi <= -80 && rssi > -90)
//                    ((ImageView) view.findViewById(R.id.image_rssi)).setImageResource(R.drawable.rssi_strength25);
//
//                if (rssi <= -90 && rssi > -110)
//                    ((ImageView) view.findViewById(R.id.image_rssi)).setImageResource(R.drawable.rssi_strength0);
//            } else
//                ((ImageView) view.findViewById(R.id.image_rssi)).setImageResource(R.drawable.rssi_strengthnil);

            return view;
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.

            mRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mLeDeviceListAdapter.isEmpty() && mScanning) {
                        Toast.makeText(Main2Activity.this, R.string.ble_scan_empty, Toast.LENGTH_SHORT).show();
                    }
                    mScanning = false;
                    if (mBluetoothAdapter.isEnabled()) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        } else {
                            mBLEScanner.stopScan(mScanCallback);
                        }
                        invalidateOptionsMenu();
                    }

                }
            };

            mHandler.postDelayed(mRunnable, SCAN_PERIOD);


            mScanning = true;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mBLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            mScanning = false;
            if (mBluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                } else {
                    mBLEScanner.stopScan(mScanCallback);
                }
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.Main2_button1:
                mLeDeviceListAdapter.clear();
                BT_LeDevices.clear();
                //setListAdapter(mLeDeviceListAdapter);
                mRSSIs.clear();
                mAdvTimes.clear();
                mpAdvTimes.clear();
                scanLeDevice(true);
                break;
            case R.id.Main2_button2:
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //final BluetoothDevice device = mLeDeviceListAdapter.getDevice(BT_Count.get(BT_Select_Point));
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                final BluetoothDevice device = BT_LeDevices.get(BT_Count.get(BT_Select_Point));
                if (device == null) return;

                mDeviceAddress = device.getAddress();
                mDeviceName = device.getName();
                //connect(device.getAddress(),true);
                mBond = true;
                mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connect(device.getAddress(), mBond);
                    }
                }, 1000);

                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//                mBleService.mDevice = device;
//                mBleService.connect(device.getAddress(),true);



//                final Intent intent = new Intent(this, DeviceControlActivity.class);
//                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
//                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
//
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
//                    intent.putExtra(DeviceControlActivity.EXTRAS_SCAN_RESULT, mScanResultsDep.get(BT_Select_Point));
//                else
//                    intent.putExtra(DeviceControlActivity.EXTRAS_SCAN_RESULT, mScanResults.get(BT_Select_Point));
//                if (mScanning) {
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    } else {
//                        mBLEScanner.stopScan(mScanCallback);
//                    }
//                    mScanning = false;
//                }
//                startActivity(intent);
                break;
            case R.id.Main2_button3:

                //Querying paired devices
                BT_Devicelist.clear();
                BT_Addrlist.clear();
                int x = 0;
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice deviceX : BT_LeDevices) {
                    String deviceName = deviceX.getName();
                    String deviceHardwareAddress = deviceX.getAddress(); // MAC address
                    if(deviceName != null) {
                        BT_Devicelist.add(deviceName); //this adds an element to the list.
                        BT_Addrlist.add(deviceHardwareAddress);
                        BT_Count.add(x);
                    }
                    x++;
                }
                //android.R.layout.simple_list_item_1 為內建樣式，還有其他樣式可自行研究
                ArrayAdapter<String> adapter = new ArrayAdapter<>(Main2Activity.this, android.R.layout.simple_list_item_1, BT_Devicelist);
                M2_ListView.setAdapter(adapter);
                M2_ListView.setOnItemClickListener(onClickListView);       //指定事件 Method
                //Fragment1_TextView.setText("pair bluetooth is over");
                M2_TextView.setText(BT_Devicelist.get(0));
                break;
        }
    }

    private final AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Toast 快顯功能 第三個參數 Toast.LENGTH_SHORT 2秒  LENGTH_LONG 5秒
            //Toast.makeText(MainActivity.this, "點選第 " + (position + 1) + " 個 \n內容：" + BT_Addrlist.get(position).toString(), Toast.LENGTH_SHORT).show();

            BT_Select_Point = position;

            M2_TextView.setText(BT_Devicelist.get(position));
        }

    };

    private void setLECallbacks() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Device scan callback.
            mLeScanCallback =
                    new BluetoothAdapter.LeScanCallback() {

                        @Override
                        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
//                            Log.i("Result + RSSI", scanRecord.toString() + rssi);
                            /*
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLeDeviceListAdapter.addDeviceDep(device, rssi, scanRecord);
                                    mLeDeviceListAdapter.notifyDataSetChanged();
                                }
                            });
                            */


                            runOnUiThread(new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mLeDeviceListAdapter.addDeviceDep(device, rssi, scanRecord);
                                    mLeDeviceListAdapter.notifyDataSetChanged();
                                }
                            }));
                        }
                    };

        } else {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
//                    Log.i("Result + RSSI", result.toString() + result.getRssi());
                    /*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mLeDeviceListAdapter.addDevice(result.getDevice(), result.getRssi(), result);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                    */

                    runOnUiThread(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(result.getDevice(), result.getRssi(), result);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    }));

                    //super.onScanResult(callbackType, result);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    //for (ScanResult sr : results) {
                    //    Log.i("Scan Result", sr.toString());
                    //}
                    //super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.i("Scan Failed", "Code:" + errorCode);
                    //super.onScanFailed(errorCode);
                }
            };
        }
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BLEService.LocalBinder) service).getService();
            if (!mBleService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

//            String uuid = mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getUuid().toString();
//            String charString = BLEGattAttributes.lookup(uuid, getResources().getString(R.string.unknown_characteristic));
//
//            String charProperties = "";
//            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                charProperties = charProperties.concat("Read ");
//                ((Button) findViewById(R.id.characteristic_read_button)).setEnabled(true);
//                ((TextView) findViewById(R.id.characteristic_read_label)).setEnabled(true);
//            } else {
//                ((Button) findViewById(R.id.characteristic_read_button)).setEnabled(false);
//                ((TextView) findViewById(R.id.characteristic_read_label)).setEnabled(false);
//            }
//
//            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
//                charProperties = charProperties.concat("Write ");
//                ((Button) findViewById(R.id.characteristic_write_button)).setEnabled(true);
//                ((EditText) findViewById(R.id.characteristic_write)).setEnabled(true);
//                ((TextView) findViewById(R.id.characteristic_write_label)).setEnabled(true);
//            } else {
//                ((Button) findViewById(R.id.characteristic_write_button)).setEnabled(false);
//                ((EditText) findViewById(R.id.characteristic_write)).setEnabled(false);
//                ((TextView) findViewById(R.id.characteristic_write_label)).setEnabled(false);
//            }
//
//            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
//                charProperties = charProperties.concat("WriteNoResponse ");
//                ((Button) findViewById(R.id.characteristic_write_button)).setEnabled(true);
//                ((EditText) findViewById(R.id.characteristic_write)).setEnabled(true);
//                ((TextView) findViewById(R.id.characteristic_write_label)).setEnabled(true);
//            } else {
//                //((Button) findViewById(R.id.characteristic_write_button)).setEnabled(false);
//                //((EditText) findViewById(R.id.characteristic_write)).setEnabled(false);
//            }
//
//            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) > 0) {
//                charProperties = charProperties.concat("SignedWrite ");
//                ((Button) findViewById(R.id.characteristic_write_button)).setEnabled(true);
//                ((EditText) findViewById(R.id.characteristic_write)).setEnabled(true);
//                ((TextView) findViewById(R.id.characteristic_write_label)).setEnabled(true);
//            } else {
//                //((Button) findViewById(R.id.characteristic_write_button)).setEnabled(false);
//                //((EditText) findViewById(R.id.characteristic_write)).setEnabled(false);
//            }
//
//            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                charProperties = charProperties.concat("Notify ");
//                ((TextView) findViewById(R.id.characteristic_notify)).setText("Notify/Indicate: " + "No");
//                ((TextView) findViewById(R.id.characteristic_notify)).setEnabled(true);
//                ((Switch) findViewById(R.id.charcteristic_notify_switch)).setEnabled(true);
//                ((TextView) findViewById(R.id.characteristic_read_label)).setEnabled(true);
//            } else {
//                ((Switch) findViewById(R.id.charcteristic_notify_switch)).setEnabled(false);
//                ((TextView) findViewById(R.id.characteristic_notify)).setEnabled(false);
//            }
//
//            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
//                charProperties = charProperties.concat("Indicate ");
//                ((TextView) findViewById(R.id.characteristic_notify)).setText("Notify/Indicate: " + "No");
//                ((TextView) findViewById(R.id.characteristic_notify)).setEnabled(true);
//                ((Switch) findViewById(R.id.charcteristic_notify_switch)).setEnabled(true);
//                ((TextView) findViewById(R.id.characteristic_read_label)).setEnabled(true);
//            } else {
//                //((Switch) findViewById(R.id.charcteristic_notify_switch)).setEnabled(false);
//            }
//
//            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_BROADCAST) > 0)
//                charProperties = charProperties.concat("Broadcast ");
//
//            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) > 0)
//                charProperties = charProperties.concat("Extended ");
//
//
//            if (charString.matches(getResources().getString(R.string.unknown_characteristic))) {
//                ((TextView) findViewById(R.id.characteristic_uuid)).setText(/*"UUID:              "*/ " " + uuid);
//                ((TextView) findViewById(R.id.characteristic_name)).setText(getResources().getString(R.string.unknown_characteristic) + " Characteristic");
//            }
//            else {
//                if (charString.contains("Microchip"))
//                    ((TextView) findViewById(R.id.characteristic_uuid)).setText(/*"UUID:              "*/ " " + uuid);
//                else
//                    ((TextView) findViewById(R.id.characteristic_uuid)).setText(/*"UUID:              "*/ " " + uuid.substring(4, 8));
//                ((TextView) findViewById(R.id.characteristic_name)).setText(charString);
//            }
//
//            ((TextView) findViewById(R.id.characteristic_properties)).setText("Properties: " + charProperties);
//
//            /*
//            String charPermissions = "";
//            int charPermission = mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getPermissions();
//            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_READ) > 0) {
//                charPermissions += "Read ";
//            }
//            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED) > 0) {
//                charPermissions += "EncryptedRead ";
//            }
//            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM) > 0) {
//                charPermissions += "EncryptedMITMRead ";
//            }
//            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_WRITE) > 0) {
//                charPermissions += "Write ";
//            }
//            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED) > 0) {
//                charPermissions += "EncryptedWrite ";
//            }
//            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM) > 0) {
//                charPermissions += "EncryptedMITMWrite ";
//            }
//            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED) > 0) {
//                charPermissions += "SignedWrite ";
//            }
//            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM) > 0) {
//                charPermissions += "EncryptedMITMSignedWrite ";
//            }
//            ((TextView) findViewById(R.id.characteristic_permissions)).setText("Permissions: " + charPermissions);
//            */

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };

    private int skipIndex = 0;
    private boolean mConnected = false;
    private boolean mConnecting = false;
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
                displayGattServices(getSupportedGattServices());
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BLEService.EXTRA_DATA));
            }
        }
    };
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    private void displayData(String data) {

        if (data != null) {
            //mDataField.setText(data);
        }
    }
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isBonded(mDeviceAddress)) {
                    M2_TextView.setText(getResources().getString(resourceId) + " - Bonded");
                } else {
                    M2_TextView.setText(resourceId);
                }
            }
        });
    }
    public boolean isBonded(final String address) {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.getRemoteDevice(address).getBondState() == BluetoothDevice.BOND_NONE) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.

    private ExpandableListView mGattServicesList;
    private View mGattServicesListHeaderView;

    public static String UUID_GAP = "00001800-0000-1000-8000-00805f9b34fb";
    public static String UUID_GATT = "00001801-0000-1000-8000-00805f9b34fb";
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private final String LIST_PROPERTIES = "PROPERTIES";

    private static final int REQUEST_DISCONNECT = 5;
    private static final int RESULT_DISCONNECT = 5;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private void clearUI() {
        mGattServicesList.removeHeaderView(mGattServicesListHeaderView);
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
    }
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
                            //mNotifyCharacteristic = characteristic;

                            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                mBleService.setCharacteristicNotification(characteristic, false);
                            } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                                mBleService.setCharacteristicIndication(characteristic, false);
                            }
                            Log.d("Index1:", Integer.toString(groupPosition) + Integer.toString(childPosition));
                            final Intent intent = new Intent(Main2Activity.this, DeviceCharacteristicActivity.class);
                            intent.putExtra(DeviceCharacteristicActivity.EXTRAS_SERVICE_INDEX, (new Integer(groupPosition + skipIndex)).toString());
                            intent.putExtra(DeviceCharacteristicActivity.EXTRAS_CHARACTERISTIC_INDEX, (new Integer(childPosition)).toString());
                            startActivityForResult(intent, REQUEST_DISCONNECT);
                        }
                        return true;
                    }
                    return false;
                }
            };
}
