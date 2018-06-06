package com.benqmedicaltech.fragment_test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    //Bluetooth
    private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    ArrayList<String> BT_Devicelist = new ArrayList<>();
    ArrayList<String> BT_Addrlist = new ArrayList<>();
    BluetoothHeadset mBluetoothHeadset;
    BluetoothSocket BTSocket;

//    private ListView Fragment1_ListView ;
//    private TextView Fragment1_TextView ;

    // Get the default adapter
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = (BluetoothHeadset) proxy;
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null;
            }
        }
    };


    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Toast 快顯功能 第三個參數 Toast.LENGTH_SHORT 2秒  LENGTH_LONG 5秒
            Toast.makeText(MainActivity.this, "點選第 " + (position + 1) + " 個 \n內容：" + BT_Addrlist.get(position).toString(), Toast.LENGTH_SHORT).show();

            mBluetoothAdapter.cancelDiscovery();

            BluetoothDevice connDevices = mBluetoothAdapter.getRemoteDevice(BT_Addrlist.get(position).toString());

            try {
                BTSocket = connDevices.createRfcommSocketToServiceRecord(MY_UUID);
                BTSocket.connect();
                readThread mreadThread = new readThread();
                mreadThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
//            TextView mytextview = (TextView) findViewById(R.id.textView234);
//            ListView listview = (ListView) findViewById(R.id.listView234);
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                //mytextview.setText(mytextview.getText() + "\n" + deviceName + deviceHardwareAddress);
                BT_Devicelist.add(deviceName); //this adds an element to the list.
                BT_Addrlist.add(deviceHardwareAddress);


            }
            //android.R.layout.simple_list_item_1 為內建樣式，還有其他樣式可自行研究
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, BT_Devicelist);
//            Fragment1_ListView.setAdapter(adapter);
//            Fragment1_ListView.setOnItemClickListener(onClickListView);       //指定事件 Method

        }
    };
    private ListView Fragment1_ListView ;
    private TextView Fragment1_TextView ;

    private void BT_Scan(View v){

        //Bluetooth

        Fragment1_TextView.setText("藍芽沒開拉，幹！");
        if (mBluetoothAdapter == null) {
            Fragment1_TextView.setText("您的裝置沒有支援藍芽");
        }

        int REQUEST_ENABLE_BT = 1; // need greater then 0

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            Fragment1_TextView.setText("藍芽沒開拉，幹！");
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Fragment1_TextView.setText( "CancelDiscovery"); //(Fragment_TextView.getText() + "\n" + "cancelDiscovery")
        }

        //Querying paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        mBluetoothAdapter.startDiscovery();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                //string mUUID = device.getUuids()[0].getUuid(); // UUID
                //tex.setText(tex.getText() + "\n" + deviceName + deviceHardwareAddress);
                BT_Devicelist.add(deviceName); //this adds an element to the list.
                BT_Addrlist.add(deviceHardwareAddress);

                //muuid =  device.getUuids();
                //BT_UUIDlist.add(muuid[0].toString());
            }
            //android.R.layout.simple_list_item_1 為內建樣式，還有其他樣式可自行研究
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, BT_Devicelist);
            Fragment1_ListView.setAdapter(adapter);
            Fragment1_ListView.setOnItemClickListener(onClickListView);       //指定事件 Method
            Fragment1_TextView.setText("pair bluetooth is over");
        }

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Establish connection to the proxy.
        mBluetoothAdapter.getProfileProxy(MainActivity.this, mProfileListener, BluetoothProfile.HEADSET);

    }

    //三个fragment
    private Fragment1 f1;
    private Fragment2 f2;
    private Fragment3 f3;

    //底部三个按钮
    private Button foot1;
    private Button foot2;
    private Button foot3;


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //透過下方程式碼，取得Activity中執行的個體。
        //manager = getSupportFragmentManager();
        foot1 = (Button) findViewById(R.id.Button1);
        foot2 = (Button) findViewById(R.id.Button2);
        foot3 = (Button) findViewById(R.id.Button3);
        foot1.setOnClickListener(this);
        foot2.setOnClickListener(this);
        foot3.setOnClickListener(this);


        //第一次初始化首页默认显示第一个fragment
        initFragment1();

    }

    private void initFragment1(){
        //开启事务，fragment的控制是由事务来实现的
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //第一种方式（add），初始化fragment并添加到事务中，如果为null就new一个
        if(f1 == null){
            f1 = new Fragment1("連線頁面");
            transaction.add(R.id.center, f1);
        }
        //隐藏所有fragment
        hideFragment(transaction);
        //显示需要显示的fragment
        transaction.show(f1);

        //第二种方式(replace)，初始化fragment
//        if(f1 == null){
//            f1 = new MyFragment("消息");
//        }
//        transaction.replace(R.id.main_frame_layout, f1);

        //提交事务
        transaction.commit();
    }

    //显示第二个fragment
    private void initFragment2(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(f2 == null){
            f2 = new Fragment2("功能按鍵");
            transaction.add(R.id.center,f2);
        }
        hideFragment(transaction);
        transaction.show(f2);

//        if(f2 == null) {
//            f2 = new MyFragment("联系人");
//        }
//        transaction.replace(R.id.main_frame_layout, f2);

        transaction.commit();
    }
    //显示第二个fragment
    private void initFragment3(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if(f3 == null){
            f3 = new Fragment3("功能按鍵2");
            transaction.add(R.id.center,f3);
        }
        hideFragment(transaction);
        transaction.show(f3);

//        if(f2 == null) {
//            f2 = new MyFragment("联系人");
//        }
//        transaction.replace(R.id.main_frame_layout, f2);

        transaction.commit();
    }

    //隐藏所有的fragment
    private void hideFragment(FragmentTransaction transaction){
        if(f1 != null){
            transaction.hide(f1);
        }
        if(f2 != null){
            transaction.hide(f2);
        }
        if(f3 != null){
            transaction.hide(f3);
        }
    }

    @Override
    public void onClick(View v) {
//        if(v == foot1){
//            initFragment1();
//        }else if(v == foot2){
//            initFragment2();
//        }
//        else if(v == foot3){
//            initFragment3();
//        }
        TextView textView1 = (TextView) findViewById(R.id.fragment1_text);
        TextView textView2 = (TextView) findViewById(R.id.fragment2_text);
        TextView textView3 = (TextView) findViewById(R.id.fragment3_text);
        Fragment1_ListView = (ListView) findViewById(R.id.fragment1_List);
        Fragment1_TextView = (TextView) findViewById(R.id.fragment1_text);
        switch (v.getId()) {
            case R.id.Button1:
                initFragment1();
                break;
            case R.id.Button2:
                initFragment2();
                break;
            case R.id.Button3:
                initFragment3();
                break;
            case R.id.fragment1_button1:
                textView1.setText("fragment1_button1");
                BT_Scan(v);
                break;
            case R.id.fragment1_button2:
                textView1.setText("fragment1_button2");
                break;
            case R.id.fragment2_button1:
                textView2.setText("fragment2_button1");
                break;
            case R.id.fragment2_button2:
                textView2.setText("fragment2_button2");
                break;
            case R.id.fragment2_button3:
                textView2.setText("fragment2_button3");
                break;
            case R.id.fragment2_button4:
                textView2.setText("fragment2_button4");
                break;
            case R.id.fragment3_button1:
                textView3.setText("fragment3_button1");
                break;
            case R.id.fragment3_button2:
                textView3.setText("fragment3_button2");
                break;
            case R.id.fragment3_button3:
                textView3.setText("fragment3_button3");
                break;
            case R.id.fragment3_button4:
                textView3.setText("fragment3_button4");
                break;
        }

    }


    /**
     * 读取数据
     */
    private class readThread extends Thread {
        public void run() {
            //TextView mytextviewX = (TextView) findViewById(R.id.textView234);
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream is = null;
            try {
                is = BTSocket.getInputStream();
                //show("客户端:获得输入流");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            while (true) {
                try{
                    if ((bytes = is.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        //M_T = M_T + new String(buf_data);
                        //mytextviewX.setText(s.toString());
                        //show("客户端:读取数据了" + s);
                    }
                } catch (IOException e) {
                    try {
                        is.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    /**
     * 发送数据
     */
    public void sendMessage() {
        if (BTSocket == null) {
            Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            OutputStream os = BTSocket.getOutputStream();
            os.write("Mick_Test".getBytes());
            os.flush();
            //show("客户端:发送信息成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
