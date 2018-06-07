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
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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
    int BT_Select_Point = 0;
    BluetoothHeadset mBluetoothHeadset;
    BluetoothSocket BTSocket;


    private ListView Fragment1_ListView ;
    private TextView Fragment1_TextView ;


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

            BT_Select_Point = position;

            Fragment1_TextView.setText(BT_Devicelist.get(position).toString());
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

    private void BT_Scan(){

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

    private void BT_Connecnting(){

        mBluetoothAdapter.cancelDiscovery();

        BluetoothDevice connDevices = mBluetoothAdapter.getRemoteDevice(BT_Addrlist.get(BT_Select_Point).toString());

        try {
            BTSocket = connDevices.createRfcommSocketToServiceRecord(MY_UUID);
            BTSocket.connect();
            readThread mreadThread = new readThread();
            mreadThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //三个fragment
    private Fragment1 f1;
    private Fragment2 f2;
    private Fragment3 f3;

    View View_Fragment1;
    View View_Fragment2;
    View View_Fragment3;

    //底部三个按钮
    private Button foot1;
    private Button foot2;
    private Button foot3;

    private Button F1_Button1;
    private Button F1_Button2;
    private Button F2_Button1;
    private Button F2_Button2;
    private Button F2_Button3;
    private Button F2_Button4;
    private Button F3_Button1;
    private Button F3_Button2;
    private Button F3_Button3;
    private Button F3_Button4;

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;

    public ButtonListener b = new ButtonListener();


    private Handler handler = new Handler();
    int mDelayTime = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
        if(BTSocket.isConnected())
        {
            try {
                BTSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mDelayTime = 1000;
        //handler.postDelayed(runnable,1000); //90ms timer

        //透過下方程式碼，取得Activity中執行的個體。
        //manager = getSupportFragmentManager();
        foot1 = (Button) findViewById(R.id.Button1);
        foot2 = (Button) findViewById(R.id.Button2);
        foot3 = (Button) findViewById(R.id.Button3);
        foot1.setOnClickListener(b);
        foot2.setOnClickListener(b);
        foot3.setOnClickListener(b);

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


    /*  Table Define  */

    //byte ON_OFF = 0x00;                  //#define	ON_OFF 				0
    byte Rev_Trend = 0x1;               //#define	Rev_Trend 			1
    byte Trend = 0x2;                   //#define	Trend				2
    byte Tilt_R = 0x3;                  //#define	Tilt_R				3
    byte Tilt_L = 0x4;                  //#define	Tilt_L				4
    byte Back_Up = 0x5;                 //#define	Back_Up				5
    byte Back_Down = 0x6;               //#define	Back_Down			6
    byte Table_Up = 0x7;                //#define	Table_Up			7
    byte Table_Down = 0x8;              //#define	Table_Down			8
    byte Slide_Foot = 0x9;              //#define	Slide_Foot			9
    byte Slide_Head = 0xa;              //#define	Slide_Head			10
    byte Leg_Up = 0xb;                  //#define	Leg_Up				11
    byte Leg_Down = 0xc;                //#define	Leg_Down			12
    byte Unlock = 0xd;                  //#define	Unlock				13
    byte Lock = 0xe;                    //#define	Lock				14
    byte Kidney_Up = 0xf;               //#define   Kidney_Up           15
    byte Kidney_Down = 0x10;               //#define   Kidney_Down           16
    byte Flex = 0x15;                   //#define	Flex				21
    byte Reflex = 0x16;                 //#define	Reflex				22
    byte Level = 0x17;                  //#define	Level				23
    //byte Config_TS710 = 0x18;           //#define	Config_TS710		24
    //byte Config_7000_Rev = 0x19;        //#define   Config_7000_Rev	    25
    //byte Config_Q100 = 0x1b;            //#define	Config_Q100			27
    //byte Config_650NS = 0x1c;           //#define	Config_MOT1600_650	28
    //byte Unlock_Stop = 0x1e;            //#define	Unlock_Stop			30          //650NS  進入腳踏模式=馬達無動作
    //byte Lock_Run = 0x1f;               //#define	Lock_Run			31          //650NS  取消腳踏模式=馬達有動作
    byte Set_M1 = 0x23;                 //#define     Set_M1                  35  //Save Angle in Memery now
    byte Set_M2 = 0x24;                 //#define     Set_M2                  36  //Save Angle in Memery now
    //byte Config_Mode = 0x37;
    //byte Restrict_Function = 0x42;      //#define   Restrict_Function	66
    byte No_Function = 0x4d;            //#define   No_Function		    77
    //byte Shutdown = 0x58;               //#define   Shutdown			88
    byte SW_Output = 0x00;
    byte Checksum = 0x00;

    byte[] Output_First = new byte[8];
    byte[] Output_Function = new byte[8];
    byte[] Output_Checksum = new byte[8];
    byte[] Output_Final = new byte[24];

    public void Output_First_Set()
    {
        Output_First[0] = 0x55;
        Output_First[1] = 0x0;
        Output_First[2] = 0x55;
        Output_First[3] = 0x0;
        Output_First[4] = 0x55;
        Output_First[5] = 0x0;
        Output_First[6] = 0x55;
        Output_First[7] = 0x0;
    }

    public void Function_Code()
    {
        if ((SW_Output & 0x01) == 0x01)
        {
            Output_Function[7] = 0x00;
        }
        else
        {
            Output_Function[7] = 0x55;
        }

        if ((SW_Output & 0x02) == 0x02)
        {
            Output_Function[6] = 0x00;
        }
        else
        {
            Output_Function[6] = 0x55;
        }

        if ((SW_Output & 0x04) == 0x04)
        {
            Output_Function[5] = 0x00;
        }
        else
        {
            Output_Function[5] = 0x55;
        }

        if ((SW_Output & 0x08) == 0x08)
        {
            Output_Function[4] = 0x00;
        }
        else
        {
            Output_Function[4] = 0x55;
        }

        if ((SW_Output & 0x10) == 0x10)
        {
            Output_Function[3] = 0x00;
        }
        else
        {
            Output_Function[3] = 0x55;
        }

        if ((SW_Output & 0x20) == 0x20)
        {
            Output_Function[2] = 0x00;
        }
        else
        {
            Output_Function[2] = 0x55;
        }

        if ((SW_Output & 0x40) == 0x40)
        {
            Output_Function[1] = 0x00;
        }
        else
        {
            Output_Function[1] = 0x55;
        }

        if ((SW_Output & 0x80) == 0x80)
        {
            Output_Function[0] = 0x00;
        }
        else
        {
            Output_Function[0] = 0x55;
        }
    }

    public void Checksum_Code()
    {
        Checksum = (byte)(0x55 + SW_Output - 0x01);
        if ((Checksum & 0x01) == 0x01)
        {
            Output_Checksum[7] = 0x0;
        }
        else
        {
            Output_Checksum[7] = 0x55;
        }

        if ((Checksum & 0x02) == 0x02)
        {
            Output_Checksum[6] = 0x0;
        }
        else
        {
            Output_Checksum[6] = 0x55;
        }

        if ((Checksum & 0x04) == 0x04)
        {
            Output_Checksum[5] = 0x0;
        }
        else
        {
            Output_Checksum[5] = 0x55;
        }

        if ((Checksum & 0x08) == 0x08)
        {
            Output_Checksum[4] = 0x0;
        }
        else
        {
            Output_Checksum[4] = 0x55;
        }

        if ((Checksum & 0x10) == 0x10)
        {
            Output_Checksum[3] = 0x0;
        }
        else
        {
            Output_Checksum[3] = 0x55;
        }

        if ((Checksum & 0x20) == 0x20)
        {
            Output_Checksum[2] = 0x0;
        }
        else
        {
            Output_Checksum[2] = 0x55;
        }

        if ((Checksum & 0x40) == 0x40)
        {
            Output_Checksum[1] = 0x0;
        }
        else
        {
            Output_Checksum[1] = 0x55;
        }

        if ((Checksum & 0x80) == 0x80)
        {
            Output_Checksum[0] = 0x0;
        }
        else
        {
            Output_Checksum[0] = 0x55;
        }

    }

    private void Output_Mix(){

        for(int i =0;i<8;i++){
            Output_Final[i] = Output_First[i];
        }
        for(int i =0;i<8;i++){
            Output_Final[i+8] = Output_Function[i];
        }
        for(int i =0;i<8;i++){
            Output_Final[i+16] = Output_Checksum[i];
        }

    }



    /**
     * 读取数据
     */
    private byte Table_Data[] = new byte[30];
    private int Table_Data_Point = 0;

    private class readThread extends Thread {
        public void run() {
            //TextView mytextviewX = (TextView) findViewById(R.id.textView234);
            int bytes;
            InputStream is = null;
            try {
                is = BTSocket.getInputStream();
                //show("客户端:获得输入流");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            byte[] buffer = new byte[1024];
            while (true) {
                try{
                    if ((bytes = is.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
//                            Table_Data[Table_Data_Point] = buf_data[i];
//                            if(Table_Data_Point < 30)Table_Data_Point ++;
                        }
//                        if(Table_Data_Point >= 24)
//                        {
//                            Fragment1_TextView.setText("Read Final");
//                            Table_Data_Point = 0;
//                        }
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
            os.write(Output_Final);
            os.flush();
            //show("客户端:发送信息成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable runnable = new Runnable() {
        public void run() {
            //update
            sendMessage();
            if(mDelayTime > 0){
                handler.postDelayed(this,mDelayTime);
            }
        }
    };

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
//        TextView textView1 = (TextView) findViewById(R.id.fragment1_text);
//        TextView textView2 = (TextView) findViewById(R.id.fragment2_text);
//        TextView textView3 = (TextView) findViewById(R.id.fragment3_text);
//        Fragment1_ListView = (ListView) findViewById(R.id.fragment1_List);
//        Fragment1_TextView = (TextView) findViewById(R.id.fragment1_text);
//
//        switch (v.getId()) {
//            case R.id.Button1:
//                initFragment1();
//                break;
//            case R.id.Button2:
//                initFragment2();
//                break;
//            case R.id.Button3:
//                initFragment3();
//                break;
//            case R.id.fragment1_button1:
//                textView1.setText("fragment1_button1");
//                BT_Scan();
//                break;
//            case R.id.fragment1_button2:
//                textView1.setText("fragment1_button2");
//                BT_Connecnting();
//                break;
//            case R.id.fragment2_button1:
//                textView2.setText("fragment2_button1");
//                SW_Output =0x01;
//                Output_First_Set();
//                Function_Code();
//                Checksum_Code();
//                Output_Mix();
//                sendMessage();
//                break;
//            case R.id.fragment2_button2:
//                textView2.setText("fragment2_button2");
//                break;
//            case R.id.fragment2_button3:
//                textView2.setText("fragment2_button3");
//                break;
//            case R.id.fragment2_button4:
//                textView2.setText("fragment2_button4");
//                break;
//            case R.id.fragment3_button1:
//                textView3.setText("fragment3_button1");
//                break;
//            case R.id.fragment3_button2:
//                textView3.setText("fragment3_button2");
//                break;
//            case R.id.fragment3_button3:
//                textView3.setText("fragment3_button3");
//                break;
//            case R.id.fragment3_button4:
//                textView3.setText("fragment3_button4");
//                break;
//        }

    }

    class ButtonListener implements View.OnClickListener, View.OnTouchListener {

        public void onClick(View v) {
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
                    BT_Scan();
                    break;
                case R.id.fragment1_button2:
                    textView1.setText("fragment1_button2");
                    BT_Connecnting();
                    break;
            }
        }

        private void Table_Command_Send_Start(byte command){

            SW_Output = command;
            Output_First_Set();
            Function_Code();
            Checksum_Code();
            Output_Mix();
            mDelayTime = 90;
            handler.postDelayed(runnable,90);

        }

        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                Log.d("test", "cansal button ---> down");
                switch (v.getId()) {
                    case R.id.fragment2_button1:
                        textView2.setText("fragment2_button1");
                        Table_Command_Send_Start(Rev_Trend);
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
            if(event.getAction() == MotionEvent.ACTION_UP){
                Log.d("test", "cansal button ---> cancel");
                mDelayTime = 0;
            }
            return false;
        }

    }

    public void Get_Fragment1(View v)
    {
        View_Fragment1 = v;

        F1_Button1 = (Button)v.findViewById(R.id.fragment1_button1);
        F1_Button1.setOnClickListener(b);
        F1_Button1.setOnTouchListener(b);
        F1_Button2 = (Button)v.findViewById(R.id.fragment1_button2);
        F1_Button2.setOnClickListener(b);
        F1_Button2.setOnTouchListener(b);

        textView1 = (TextView) v.findViewById(R.id.fragment1_text);
        Fragment1_ListView = (ListView) v.findViewById(R.id.fragment1_List);
        Fragment1_TextView = (TextView) v.findViewById(R.id.fragment1_text);

    }
    public void Get_Fragment2(View v)
    {
        View_Fragment2 = v;
        textView2 = (TextView) v.findViewById(R.id.fragment2_text);

        F2_Button1 = (Button)v.findViewById(R.id.fragment2_button1);
        F2_Button1.setOnClickListener(b);
        F2_Button1.setOnTouchListener(b);
        F2_Button2 = (Button)v.findViewById(R.id.fragment2_button2);
        F2_Button2.setOnClickListener(b);
        F2_Button2.setOnTouchListener(b);
        F2_Button3 = (Button)v.findViewById(R.id.fragment2_button3);
        F2_Button3.setOnClickListener(b);
        F2_Button3.setOnTouchListener(b);
        F2_Button4 = (Button)v.findViewById(R.id.fragment2_button4);
        F2_Button4.setOnClickListener(b);
        F2_Button4.setOnTouchListener(b);

    }
    public void Get_Fragment3(View v)
    {
        View_Fragment3 = v;
        textView3 = (TextView) v.findViewById(R.id.fragment3_text);

        F3_Button1 = (Button)v.findViewById(R.id.fragment3_button1);
        F3_Button1.setOnClickListener(b);
        F3_Button1.setOnTouchListener(b);
        F3_Button2 = (Button)v.findViewById(R.id.fragment3_button2);
        F3_Button2.setOnClickListener(b);
        F3_Button2.setOnTouchListener(b);
        F3_Button3 = (Button)v.findViewById(R.id.fragment3_button3);
        F3_Button3.setOnClickListener(b);
        F3_Button3.setOnTouchListener(b);
        F3_Button4 = (Button)v.findViewById(R.id.fragment3_button4);
        F3_Button4.setOnClickListener(b);
        F3_Button4.setOnTouchListener(b);

    }




}



