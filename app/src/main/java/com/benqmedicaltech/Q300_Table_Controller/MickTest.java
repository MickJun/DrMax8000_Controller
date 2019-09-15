package com.benqmedicaltech.Q300_Table_Controller;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

public class MickTest extends Application {

    // your fields here
    private MainActivity MymainActivity = new MainActivity();

    public void setMainActivity(MainActivity mainActivity) {
        this.MymainActivity = mainActivity;
    }


    public MainActivity getMainActivity() {
        return MymainActivity;
    }


    private DeviceScanActivity MyDeviceScanActivity = new DeviceScanActivity();

    public void setDeviceScanActivity(DeviceScanActivity deviceScanActivity) {
        this.MyDeviceScanActivity = deviceScanActivity;
    }
    public DeviceScanActivity getDeviceScanActivity() {
        return MyDeviceScanActivity;
    }






    private BluetoothSocket globalBlueSocket ;

    public void setGlobalBlueSocket(BluetoothSocket globalBlueSocket){
        this.globalBlueSocket = globalBlueSocket;
    }
    public BluetoothSocket getGlobalBlueSocket(){
        return globalBlueSocket;
    }




}
