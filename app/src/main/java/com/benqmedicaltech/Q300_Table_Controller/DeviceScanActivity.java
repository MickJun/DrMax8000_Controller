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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
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

    private Handler mHandler;
    private Runnable mRunnable;
    private ScanCallback mScanCallback;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOCATION = 1;
    private static final long SCAN_PERIOD = 30000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

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
                                        Toast.makeText(DeviceScanActivity.this, R.string.error_bluetooth_off, Toast.LENGTH_SHORT).show();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MBD", "(Build.VERSION.SDK_INT = " + (Build.VERSION.SDK_INT) );
        // MBD
        getActionBar().setTitle(R.string.smart_discovrey_title_devices);

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


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Log.i("Permission", "Granted");

                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        if (!mScanning) {
//            menu.findItem(R.id.menu_stop).setVisible(false);
//            menu.findItem(R.id.menu_scan).setVisible(true);
//            menu.findItem(R.id.menu_refresh).setActionView(null);
//        } else {
//            menu.findItem(R.id.menu_stop).setVisible(true);
//            menu.findItem(R.id.menu_scan).setVisible(false);
//            // MBD
//           // menu.findItem(R.id.menu_refresh).setActionView(
//                   // R.layout.actionbar_indeterminate_progress);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /* MBD  switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                setListAdapter(mLeDeviceListAdapter);
                mRSSIs.clear();
                mAdvTimes.clear();
                mpAdvTimes.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                if (mHandler != null && mRunnable != null) {
                    mHandler.removeCallbacks(mRunnable);
                }
                scanLeDevice(false);
                break;
        }*/
        int id = item.getItemId();

//        if (id == R.id.menu_scan ) {
//
//            mLeDeviceListAdapter.clear();
//            setListAdapter(mLeDeviceListAdapter);
//            mRSSIs.clear();
//            mAdvTimes.clear();
//            mpAdvTimes.clear();
//            scanLeDevice(true);
//        } else if (id == R.id.menu_stop ) {
//
//            if (mHandler != null && mRunnable != null) {
//                mHandler.removeCallbacks(mRunnable);
//            }
//            scanLeDevice(false);
//        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mRSSIs = new ArrayList<Integer>();
        mAdvTimes = new ArrayList<Long>();
        mpAdvTimes = new ArrayList<Long>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mScanResultsDep = new ArrayList<byte[]>();
        else
            mScanResults = new ArrayList<ScanResult>();

        setListAdapter(mLeDeviceListAdapter);

        ((ListView) getListView()).setCacheColorHint(0);
        ((ListView) getListView()).setBackgroundResource(R.drawable.ic_launcher_background);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, R.string.error_bluetooth_off, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mBluetoothAdapter.isEnabled()) {
            if (mHandler != null && mRunnable != null) {
                mHandler.removeCallbacks(mRunnable);

            }
            scanLeDevice(false);
            mLeDeviceListAdapter.clear();
            mRSSIs.clear();
            mAdvTimes.clear();
            mpAdvTimes.clear();
        }

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(bluetoothReceiver);
        super.onDestroy();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            intent.putExtra(DeviceControlActivity.EXTRAS_SCAN_RESULT, mScanResultsDep.get(position));
        else
            intent.putExtra(DeviceControlActivity.EXTRAS_SCAN_RESULT, mScanResults.get(position));
        if (mScanning) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mBLEScanner.stopScan(mScanCallback);
            }
            mScanning = false;
        }
        startActivity(intent);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.

            mRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mLeDeviceListAdapter.isEmpty() && mScanning) {
                        Toast.makeText(DeviceScanActivity.this, R.string.ble_scan_empty, Toast.LENGTH_SHORT).show();
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


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator =  DeviceScanActivity.this.getLayoutInflater();//(LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); //DeviceScanActivity.this.getLayoutInflater(); //(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); //
        }

        public void addDevice(BluetoothDevice device, int rssi, ScanResult scanResult) {

            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                mRSSIs.add(rssi);
                mpAdvTimes.add(Long.valueOf(0));
                mAdvTimes.add(scanResult.getTimestampNanos());
                mScanResults.add(scanResult);
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
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.deviceRSSI = (TextView) view.findViewById(R.id.device_rssi);
                viewHolder.deviceAdvInterval = (TextView) view.findViewById(R.id.device_interval);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
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


    private void setLECallbacks()
    {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Device scan callback.
            mLeScanCallback =
                    new BluetoothAdapter.LeScanCallback() {

                        @Override
                        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                            //Log.i("Result + RSSI", scanRecord.toString() + rssi);
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
                    //Log.i("Result + RSSI", result.toString() + result.getRssi());
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
                    //Log.i("Scan Failed", "Code:" + errorCode);
                    //super.onScanFailed(errorCode);
                }
            };
        }


    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRSSI;
        TextView deviceAdvInterval;
    }
}