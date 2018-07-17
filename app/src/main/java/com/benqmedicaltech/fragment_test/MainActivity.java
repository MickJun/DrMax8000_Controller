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
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{    //




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
            //Toast.makeText(MainActivity.this, "點選第 " + (position + 1) + " 個 \n內容：" + BT_Addrlist.get(position).toString(), Toast.LENGTH_SHORT).show();

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
//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, BT_Devicelist);
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

        BT_Devicelist.clear();
        BT_Addrlist.clear();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                BT_Devicelist.add(deviceName); //this adds an element to the list.
                BT_Addrlist.add(deviceHardwareAddress);
            }
            //android.R.layout.simple_list_item_1 為內建樣式，還有其他樣式可自行研究
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, BT_Devicelist);
            Fragment1_ListView.setAdapter(adapter);
            Fragment1_ListView.setOnItemClickListener(onClickListView);       //指定事件 Method
            //Fragment1_TextView.setText("pair bluetooth is over");
            Fragment1_TextView.setText(BT_Devicelist.get(0).toString());
            F1_Button2.setEnabled(true);
        }

        mBluetoothAdapter.cancelDiscovery();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Establish connection to the proxy.
        mBluetoothAdapter.getProfileProxy(MainActivity.this, mProfileListener, BluetoothProfile.HEADSET);
    }

    private void BT_Connecnting(){

        if(mBluetoothAdapter.isDiscovering())mBluetoothAdapter.cancelDiscovery();

        BluetoothDevice connDevices = mBluetoothAdapter.getRemoteDevice(BT_Addrlist.get(BT_Select_Point).toString());

        try {
            BTSocket = connDevices.createRfcommSocketToServiceRecord(MY_UUID);
            BTSocket.connect();
            readThread mreadThread = new readThread();
            mreadThread.start();
            if(BTSocket.isConnected()){
                F1_Button2.setText("DISCONNECT");
//                foot2.setEnabled(true);
//                foot3.setEnabled(true);
                menu_function.setEnabled(true);
                menu_test.setEnabled(true);
                initFragment2();
                SW_Output = 0;
                if(mDelayTime == 0) {
                    mDelayTime = 90;
                    handler.postDelayed(runnable, mDelayTime);
                }
            }
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
//    private Button foot1;
//    private Button foot2;
//    private Button foot3;
    private Menu menu_Main;
    private MenuItem menu_setting;
    private MenuItem menu_function;
    private MenuItem menu_test;

    private Button F1_Button1;
    private Button F1_Button2;


    private Button F2_Button1;
    private Button F2_Button2;
    private Button F2_Button3;
    private Button F2_Button4;
    private Button F2_Button5;
    private Button F2_Button6;
    private Button F2_Button7;
    private Button F2_Button8;
    private Button F2_Button9;
    private Button F2_Button10;
    private Button F2_Button11;
    private Button F2_Button12;
    private Button F2_Button13;
    private Button F2_Button14;
    private Button F2_Button15;
    private Button F2_Button16;
//    private Button F2_Button17;
    private Button F2_Button18;
    private Button F2_Button19;
//    private Button F2_Button20;

    private TextView F2_FunText1;
    private TextView F2_FunText2;
    private TextView F2_FunText3;
    private TextView F2_FunText4;
    private TextView F2_FunText5;
    private TextView F2_FunText6;
    private TextView F2_FunText7;
    private TextView F2_FunText8;
    private TextView F2_FunText9;
    private TextView F2_FunText10;
    private TextView F2_FunText11;
    private TextView F2_FunText12;
    private TextView F2_FunText13;
    private TextView F2_FunText14;
    private TextView F2_FunText15;
    private TextView F2_FunText16;
    private TextView F2_FunText17;
    private TextView F2_FunText18;
    private TextView F2_FunText19;
    private TextView F2_FunText20;

    private ImageView IV_Normal;
    private ImageView IV_Reverse;


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
    int LastDelayTime = 0;
    private final int Max_Delay_Time = 50;

    @Override
    protected void onDestroy() {

        // Don't forget to unregister the ACTION_FOUND receiver.
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET,mBluetoothHeadset);
        if(handler != null)handler.removeMessages(0);
        if(BTSocket != null && BTSocket.isConnected())
        {
            try {
                BTSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {   //原本以為可以用來擋螢幕翻轉，但沒用
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 什麼都不用寫
        }
        else {
            // 什麼都不用寫
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.iqor_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //mDelayTime = 1000;
        //handler.postDelayed(runnable,1000); //90ms timer

        //透過下方程式碼，取得Activity中執行的個體。
        //manager = getSupportFragmentManager();

//        foot1.setOnClickListener(b);
//        foot2.setOnClickListener(b);
//        foot3.setOnClickListener(b);
//        foot1.setText("LINK");
//        foot2.setText("Function");
//        foot3.setText("Data");
        //第一次初始化首页默认显示第一个fragment
        //initFragment3();
        //initFragment2();
        initFragment1();

    }

//    private TintImageView overflow;


//    MenuItem moreItem;
    /**
     *创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        moreItem = menu.add(Menu.NONE, Menu.FIRST, Menu.FIRST, null);
//        moreItem.setIcon(R.drawable.bisor_gui_element_button_option_normal);
//        moreItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        getMenuInflater().inflate(R.menu.menu_main,menu); //通过getMenuInflater()方法得到MenuInflater对象，再调用它的inflate()方法就可以给当前活动创建菜单了，第一个参数：用于指定我们通过哪一个资源文件来创建菜单；第二个参数：用于指定我们的菜单项将添加到哪一个Menu对象当中。


        menu_Main =  menu;
        menu_setting = menu.findItem(R.id.menu_setting_item);
        menu_setting.setIcon(R.drawable.bisor_gui_element_button_option_press);
        menu_setting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu_function = menu.findItem(R.id.menu_function_item);
        menu_function.setIcon(R.drawable.bisor_gui_eq_sub_icon_table_normal);
        menu_function.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu_test = menu.findItem(R.id.menu_test_item);
        menu_function.setEnabled(false);
        menu_test.setEnabled(false);
        menu_test.setVisible(false);
        //super.onCreateOptionsMenu(menu);
        return true; // true：允许创建的菜单显示出来，false：创建的菜单将无法显示。
    }

    /**
     *菜单的点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_setting_item:
                //Toast.makeText(this, "Setting！", Toast.LENGTH_SHORT).show();
                if(SW_Output== 0x00){
                    initFragment1();
                }
                break;
            case R.id.menu_function_item:
                //Toast.makeText(this, "Function！", Toast.LENGTH_SHORT).show();
                if(SW_Output== 0x00){
                    initFragment2();
                }
                break;
            case R.id.menu_test_item:
                //Toast.makeText(this, "Test！", Toast.LENGTH_SHORT).show();
                if(SW_Output== 0x00){
                    initFragment3();
                }
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /**
         * 在onCreateOptionsMenu执行后，菜单被显示前调用；如果菜单已经被创建，则在菜单显示前被调用。 同样的，
         * 返回true则显示该menu,false 则不显示; （可以通过此方法动态的改变菜单的状态，比如加载不同的菜单等） TODO
         * Auto-generated method stub
         */
        if(SW_Output != 0x00) {
            menu_setting.setVisible(false);
            menu_function.setVisible(false);
            menu_test.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        /**
         * 每次菜单被关闭时调用. （菜单被关闭有三种情形，menu按钮被再次点击、back按钮被点击或者用户选择了某一个菜单项） TODO
         * Auto-generated method stub
         */
        super.onOptionsMenuClosed(menu);
    }



    private void initFragment1(){
        //开启事务，fragment的控制是由事务来实现的
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //第一种方式（add），初始化fragment并添加到事务中，如果为null就new一个
        if(f1 == null){
            f1 = Fragment1.newInstance("F1");
            transaction.add(R.id.center, f1);
        }
        //隐藏所有fragment
        hideFragment(transaction);
        //显示需要显示的fragment
        transaction.show(f1);
        if(menu_setting != null)menu_setting.setIcon(R.drawable.bisor_gui_element_button_option_press);
        if(menu_function != null)menu_function.setIcon(R.drawable.bisor_gui_eq_sub_icon_table_press);
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
            f2 = Fragment2.newInstance("F2");
            transaction.add(R.id.center,f2);
        }
        hideFragment(transaction);
        transaction.show(f2);
        if(menu_setting != null)menu_setting.setIcon(R.drawable.bisor_gui_element_button_option_normal);
        if(menu_function != null)menu_function.setIcon(R.drawable.bisor_gui_eq_content_icon_table);
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
            f3 = Fragment3.newInstance("F3");
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
        byte Normal_Function = 0x21;                 //#define     Normal_Function                  33
        byte Reverse_Function = 0x22;                 //#define     Reverse_Function                  34
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

    public byte[] Table_Data = new byte[1024];
    public int Table_Data_Point = 0;
    public int Table_Data_Start = 0;

    private class readThread extends Thread {

        public void run() {
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
                            if(buf_data[i] == 85 && Table_Data_Start == 0)
                            {
                                Table_Data_Start = 1;
                                Table_Data_Point = 0;
                            }
                            if(Table_Data_Start == 1) {
                                Table_Data[Table_Data_Point] = buf_data[i];
                                if (Table_Data_Point < 1000) {
                                    Table_Data_Point++;
                                }
                                else{
                                    Table_Data_Point = 0;
                                    Table_Data_Start = 0;
                                }
                            }
                            if(buf_data[i] == -6 && Table_Data_Point > 24){
                                Table_Data_Start = 2;
                                i = bytes;
                            }
                        }

//                        for (int i = 0; i <= buff.Length - 1; i++) {
//
//                            if (RX_Satrt_Flag == 1) {
//                                s += buff[i].ToString("X2");
//                                Table_Buffer_counter = Table_Buffer_counter + 1;
//                                if (Table_Buffer_counter == 24) i = (buff.Length - 1);
//                            }
//                            if (buff[i].ToString("X2") == "FA" && RX_Satrt_Flag == 0) {
//                                RX_Satrt_Flag = 1;
//                                Table_Buffer_counter = 0;
//                            }
//                            if (Table_Buffer_counter > 23) {
//                                RX_data_flag = 1;
//                            }
//                        }
                        if(Table_Data_Start == 2){
                            if(f2 != null && View_Fragment2 != null) {
                                f2.Update_Status(Table_Data,textView2);
                            }
                            if(f3 != null && View_Fragment3 != null) {
                                f3.Update_Status(Table_Data,textView3);
                            }
                            Table_Data_Start = 0;
                        }
                        //if(textView2 != null)textView2.setText("Read Final");
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
            if(SW_Output != 0x00){
                LastDelayTime = 0;
                sendMessage();
            }
            else
            {
                if(LastDelayTime < Max_Delay_Time)LastDelayTime++;
                if(f2 != null && LastDelayTime == 5)
                {
                    f2.Unlock_Button_UI();
                }
            }

            if(LastDelayTime == Max_Delay_Time && f2!=null)
            {
                SW_Output = 0x00;
                F2_Button1.setEnabled(false);
                F2_Button2.setEnabled(false);
                F2_Button3.setEnabled(false);
                F2_Button4.setEnabled(false);
                F2_Button5.setEnabled(false);
                F2_Button6.setEnabled(false);
                F2_Button7.setEnabled(false);
                F2_Button8.setEnabled(false);
                F2_Button9.setEnabled(false);
                F2_Button10.setEnabled(false);
                F2_Button11.setEnabled(false);
                F2_Button12.setEnabled(false);
                F2_Button13.setEnabled(false);
                F2_Button14.setEnabled(false);
                F2_Button15.setEnabled(false);
                F2_Button16.setEnabled(false);
                //F2_Button17.setEnabled(false);
                F2_Button18.setEnabled(false);
                //F2_Button19.setEnabled(false);
                //F2_Button20.setEnabled(false);
                LastDelayTime = 100;
                //mDelayTime = 0;
                f2.Lock_Button_UI();
            }
            if(mDelayTime > 0){
                handler.postDelayed(this,mDelayTime);
            }

//            else{
//                if(Table_Data_Point >= 24){
//                    if(f2 != null && View_Fragment2 != null)textView2.setText("OK");
//                    Table_Data_Point = 0;
//                }
//                handler.postDelayed(this,1000);
//            }
        }
    };


    @Override
    public void onClick(View v) {}


    class ButtonListener implements View.OnClickListener, View.OnTouchListener {

        public void onClick(View v) {

            switch (v.getId()) {
//                case R.id.Button1:
//                    if(SW_Output== 0x00){
//                        initFragment1();
//                    }
//                    break;
//                case R.id.Button2:
//                    if(SW_Output== 0x00){
//                        initFragment2();
//                    }
//                    break;
//                case R.id.Button3:
//                    if(SW_Output== 0x00){
//                        initFragment3();
//                    }
//                    break;
                case R.id.fragment1_button1:
                    //textView1.setText("fragment1_button1");
                    BT_Scan();
                    break;
                case R.id.fragment1_button2:
                    //textView1.setText("fragment1_button2");
                    if(BTSocket != null && BTSocket.isConnected() )
                    {
                        try {
                            BTSocket.close();
                            F1_Button2.setText("CONNECT");
//                            foot2.setEnabled(false);
//                            foot3.setEnabled(false);
//                            unregisterReceiver(mReceiver);
                            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET,mBluetoothHeadset);
                            menu_function.setEnabled(false);
                            menu_test.setEnabled(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        BT_Connecnting();
                    }
                    break;
            }
        }

        private void Table_Command_Send_Start(byte command){
            SW_Output = command;
            Output_First_Set();
            Function_Code();
            Checksum_Code();
            Output_Mix();
//            mDelayTime = 90;
//            handler.postDelayed(runnable,mDelayTime);

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                if(SW_Output!= 0x00){
                    SW_Output = 0x00;
                    LastDelayTime = Max_Delay_Time;
                }
                else {
                    switch (v.getId()) {
                        case R.id.fragment2_button1:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Unlock");
                            Table_Command_Send_Start(Unlock);
                            f2.Press_Button_UI(F2_Button1);
                            break;
                        case R.id.fragment2_button2:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Normal_Function");
                            Table_Command_Send_Start(Normal_Function);
                            f2.Press_Button_UI(F2_Button2);
                            break;
                        case R.id.fragment2_button3:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Flex");
                            Table_Command_Send_Start(Flex);
                            f2.Press_Button_UI(F2_Button3);
                            break;
                        case R.id.fragment2_button4:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Slide_Foot");
                            Table_Command_Send_Start(Slide_Foot);
                            f2.Press_Button_UI(F2_Button4);
                            break;
                        case R.id.fragment2_button5:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Lock");
                            Table_Command_Send_Start(Lock);
                            f2.Press_Button_UI(F2_Button5);
                            break;
                        case R.id.fragment2_button6:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Reverse_Function");
                            Table_Command_Send_Start(Reverse_Function);
                            f2.Press_Button_UI(F2_Button6);
                            break;
                        case R.id.fragment2_button7:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Reflex");
                            Table_Command_Send_Start(Reflex);
                            f2.Press_Button_UI(F2_Button7);
                            break;
                        case R.id.fragment2_button8:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Slide_Head");
                            Table_Command_Send_Start(Slide_Head);
                            f2.Press_Button_UI(F2_Button8);
                            break;
                        case R.id.fragment2_button9:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Tilt_R");
                            Table_Command_Send_Start(Tilt_R);
                            f2.Press_Button_UI(F2_Button9);
                            break;
                        case R.id.fragment2_button10:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Rev_Trend");
                            Table_Command_Send_Start(Rev_Trend);
                            f2.Press_Button_UI(F2_Button10);
                            break;
                        case R.id.fragment2_button11:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Back_Up");
                            Table_Command_Send_Start(Back_Up);
                            f2.Press_Button_UI(F2_Button11);
                            break;
                        case R.id.fragment2_button12:
                            textView2.setText("Table_Up");
                            Table_Command_Send_Start(Table_Up);
                            f2.Press_Button_UI(F2_Button12);
                            break;
                        case R.id.fragment2_button13:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Tilt_L");
                            Table_Command_Send_Start(Tilt_L);
                            f2.Press_Button_UI(F2_Button13);
                            break;
                        case R.id.fragment2_button14:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Trend");
                            Table_Command_Send_Start(Trend);
                            f2.Press_Button_UI(F2_Button14);
                            break;
                        case R.id.fragment2_button15:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Back_Down");
                            Table_Command_Send_Start(Back_Down);
                            f2.Press_Button_UI(F2_Button15);
                            break;
                        case R.id.fragment2_button16:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Table_Down");
                            Table_Command_Send_Start(Table_Down);
                            f2.Press_Button_UI(F2_Button16);
                            break;
//                        case R.id.fragment2_button17:
//                            break;
                        case R.id.fragment2_button18:
//                            if(f2.Need_Release_Flag == 1){break;}
                            textView2.setText("Level");
                            Table_Command_Send_Start(Level);
                            f2.Press_Button_UI(F2_Button18);
                            break;
                        case R.id.fragment2_button19:
                            textView2.setText("Power");
                            SW_Output = 0x00;
                            if (F2_Button1.isEnabled()) {
                                LastDelayTime = Max_Delay_Time;
                            } else {
                                f2.Unlock_Button_UI();
                                F2_Button1.setEnabled(true);
                                F2_Button2.setEnabled(true);
                                F2_Button3.setEnabled(true);
                                F2_Button4.setEnabled(true);
                                F2_Button5.setEnabled(true);
                                F2_Button6.setEnabled(true);
                                F2_Button7.setEnabled(true);
                                F2_Button8.setEnabled(true);
                                F2_Button9.setEnabled(true);
                                F2_Button10.setEnabled(true);
                                F2_Button11.setEnabled(true);
                                F2_Button12.setEnabled(true);
                                F2_Button13.setEnabled(true);
                                F2_Button14.setEnabled(true);
                                F2_Button15.setEnabled(true);
                                F2_Button16.setEnabled(true);
                                //F2_Button17.setEnabled(true);
                                F2_Button18.setEnabled(true);
                                //F2_Button19.setEnabled(true);
                                //F2_Button20.setEnabled(true);
                                LastDelayTime = 0;
                            }
                            break;
//                        case R.id.fragment2_button20:
//                            break;


                        case R.id.fragment3_button1:
                            textView3.setText("fragment3_button1");
                            Table_Command_Send_Start(Table_Up);
                            break;
                        case R.id.fragment3_button2:
                            textView3.setText("fragment3_button2");
                            Table_Command_Send_Start(Table_Down);
                            break;
                        case R.id.fragment3_button3:
                            textView3.setText("fragment3_button3");
                            Table_Command_Send_Start(Lock);
                            break;
                        case R.id.fragment3_button4:
                            textView3.setText("fragment3_button4");
                            Table_Command_Send_Start(Level);
                            break;
                    }
                    Log.d("test", v.getId()+ " button ---> press");
                }
            }

            //Release
            if(event.getAction() == MotionEvent.ACTION_UP) {
                Log.d("test", v.getId() + " button ---> release");
                switch (v.getId()) {
                    case R.id.fragment2_button1:
                        if(f2.Locking_Flag == 0) {
                            f2.Release_Button_UI(F2_Button1);
                        }
                        else{
                            f2.Release_Button_UI(F2_Button1);
                        }
                        break;
                    case R.id.fragment2_button2:
                        f2.Release_Button_UI(F2_Button2);
                        break;
                    case R.id.fragment2_button3:
                        f2.Release_Button_UI(F2_Button3);
                        break;
                    case R.id.fragment2_button4:
                        f2.Release_Button_UI(F2_Button4);
                        break;
                    case R.id.fragment2_button5:
                        if(f2.Locking_Flag == 0) {
                            f2.Release_Button_UI(F2_Button5);
                        }
                        else{
                            f2.Release_Button_UI(F2_Button5);
                        }
                        break;
                    case R.id.fragment2_button6:
                        f2.Release_Button_UI(F2_Button6);
                        break;
                    case R.id.fragment2_button7:
                        f2.Release_Button_UI(F2_Button7);
                        break;
                    case R.id.fragment2_button8:
                        f2.Release_Button_UI(F2_Button8);
                        break;
                    case R.id.fragment2_button9:
                        f2.Release_Button_UI(F2_Button9);
                        break;
                    case R.id.fragment2_button10:
                        f2.Release_Button_UI(F2_Button10);
                        break;
                    case R.id.fragment2_button11:
                        f2.Release_Button_UI(F2_Button11);
                        break;
                    case R.id.fragment2_button12:
                        f2.Release_Button_UI(F2_Button12);
                        break;
                    case R.id.fragment2_button13:
                        f2.Release_Button_UI(F2_Button13);
                        break;
                    case R.id.fragment2_button14:
                        f2.Release_Button_UI(F2_Button14);
                        break;
                    case R.id.fragment2_button15:
                        f2.Release_Button_UI(F2_Button15);
                        break;
                    case R.id.fragment2_button16:
                        f2.Release_Button_UI(F2_Button16);
                        break;
                    case R.id.fragment2_button17:
                        break;
                    case R.id.fragment2_button18:
                        f2.Release_Button_UI(F2_Button18);
                        break;
                }
                SW_Output = 0x00;
                //LastDelayTime = 0;
                if (menu_setting.isVisible() == false) {
                    menu_setting.setVisible(true);
                    menu_function.setVisible(true);
                    menu_test.setVisible(true);
                    menu_Main.close();
                }
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
        F1_Button2.setEnabled(false);

        textView1 = (TextView) v.findViewById(R.id.fragment1_text);
        textView1.setTextSize(25);
        textView1.setText("Press SCAN Button");
        Fragment1_TextView = textView1;
        Fragment1_ListView = (ListView) v.findViewById(R.id.fragment1_List);

    }
    public void Get_Fragment2(View v)
    {
        View_Fragment2 = v;
        textView2 = (TextView) v.findViewById(R.id.fragment2_text);
        textView2.setTextSize(20);

        F2_Button1 = (Button)v.findViewById(R.id.fragment2_button1);
        F2_Button1.setOnClickListener(b);
        F2_Button1.setOnTouchListener(b);
        F2_Button1.setText("");
        f2.setF2_Button1(F2_Button1);
        F2_Button2 = (Button)v.findViewById(R.id.fragment2_button2);
        F2_Button2.setOnClickListener(b);
        F2_Button2.setOnTouchListener(b);
        F2_Button2.setText("");
        f2.setF2_Button2(F2_Button2);
        F2_Button3 = (Button)v.findViewById(R.id.fragment2_button3);
        F2_Button3.setOnClickListener(b);
        F2_Button3.setOnTouchListener(b);
        F2_Button3.setText("");
        f2.setF2_Button3(F2_Button3);
        F2_Button4 = (Button)v.findViewById(R.id.fragment2_button4);
        F2_Button4.setOnClickListener(b);
        F2_Button4.setOnTouchListener(b);
        F2_Button4.setText("");
        f2.setF2_Button4(F2_Button4);
        F2_Button5 = (Button)v.findViewById(R.id.fragment2_button5);
        F2_Button5.setOnClickListener(b);
        F2_Button5.setOnTouchListener(b);
        F2_Button5.setText("");
        f2.setF2_Button5(F2_Button5);
        F2_Button6 = (Button)v.findViewById(R.id.fragment2_button6);
        F2_Button6.setOnClickListener(b);
        F2_Button6.setOnTouchListener(b);
        F2_Button6.setText("");
        f2.setF2_Button6(F2_Button6);
        F2_Button7 = (Button)v.findViewById(R.id.fragment2_button7);
        F2_Button7.setOnClickListener(b);
        F2_Button7.setOnTouchListener(b);
        F2_Button7.setText("");
        f2.setF2_Button7(F2_Button7);
        F2_Button8 = (Button)v.findViewById(R.id.fragment2_button8);
        F2_Button8.setOnClickListener(b);
        F2_Button8.setOnTouchListener(b);
        F2_Button8.setText("");
        f2.setF2_Button8(F2_Button8);
        F2_Button9 = (Button)v.findViewById(R.id.fragment2_button9);
        F2_Button9.setOnClickListener(b);
        F2_Button9.setOnTouchListener(b);
        F2_Button9.setText("");
        f2.setF2_Button9(F2_Button9);
        F2_Button10 = (Button)v.findViewById(R.id.fragment2_button10);
        F2_Button10.setOnClickListener(b);
        F2_Button10.setOnTouchListener(b);
        F2_Button10.setText("");
        f2.setF2_Button10(F2_Button10);
        F2_Button11 = (Button)v.findViewById(R.id.fragment2_button11);
        F2_Button11.setOnClickListener(b);
        F2_Button11.setOnTouchListener(b);
        F2_Button11.setText("");
        f2.setF2_Button11(F2_Button11);
        F2_Button12 = (Button)v.findViewById(R.id.fragment2_button12);
        F2_Button12.setOnClickListener(b);
        F2_Button12.setOnTouchListener(b);
        F2_Button12.setText("");
        f2.setF2_Button12(F2_Button12);
        F2_Button13 = (Button)v.findViewById(R.id.fragment2_button13);
        F2_Button13.setOnClickListener(b);
        F2_Button13.setOnTouchListener(b);
        F2_Button13.setText("");
        f2.setF2_Button13(F2_Button13);
        F2_Button14 = (Button)v.findViewById(R.id.fragment2_button14);
        F2_Button14.setOnClickListener(b);
        F2_Button14.setOnTouchListener(b);
        F2_Button14.setText("");
        f2.setF2_Button14(F2_Button14);
        F2_Button15 = (Button)v.findViewById(R.id.fragment2_button15);
        F2_Button15.setOnClickListener(b);
        F2_Button15.setOnTouchListener(b);
        F2_Button15.setText("");
        f2.setF2_Button15(F2_Button15);
        F2_Button16 = (Button)v.findViewById(R.id.fragment2_button16);
        F2_Button16.setOnClickListener(b);
        F2_Button16.setOnTouchListener(b);
        F2_Button16.setText("");
        f2.setF2_Button16(F2_Button16);
//        F2_Button17 = (Button)v.findViewById(R.id.fragment2_button17);
//        F2_Button17.setOnClickListener(b);
//        F2_Button17.setOnTouchListener(b);
//        F2_Button17.setText("");
//        f2.setF2_Button17(F2_Button17);
        F2_Button18 = (Button)v.findViewById(R.id.fragment2_button18);
        F2_Button18.setOnClickListener(b);
        F2_Button18.setOnTouchListener(b);
        F2_Button18.setText("");
        f2.setF2_Button18(F2_Button18);
        F2_Button19 = (Button)v.findViewById(R.id.fragment2_button19);
        F2_Button19.setOnClickListener(b);
        F2_Button19.setOnTouchListener(b);
        F2_Button19.setText("");
        f2.setF2_Button19(F2_Button19);
//        F2_Button20 = (Button)v.findViewById(R.id.fragment2_button20);
//        F2_Button20.setOnClickListener(b);
//        F2_Button20.setOnTouchListener(b);
//        F2_Button20.setText("");
//        f2.setF2_Button20(F2_Button20);

        F2_FunText1 = (TextView)v.findViewById(R.id.FunctionText1);
        F2_FunText1.setTextSize(10);
        F2_FunText1.setText("UNLOCK");
        F2_FunText2 = (TextView)v.findViewById(R.id.FunctionText2);
        F2_FunText2.setTextSize(10);
        F2_FunText2.setText("NORMAL");
        F2_FunText3 = (TextView)v.findViewById(R.id.FunctionText3);
        F2_FunText3.setTextSize(10);
        F2_FunText3.setText("FLEX");
        F2_FunText4 = (TextView)v.findViewById(R.id.FunctionText4);
        F2_FunText4.setTextSize(10);
        F2_FunText4.setText("SLIDE FOOT");
        F2_FunText5 = (TextView)v.findViewById(R.id.FunctionText5);
        F2_FunText5.setTextSize(10);
        F2_FunText5.setText("LOCK");
        F2_FunText6 = (TextView)v.findViewById(R.id.FunctionText6);
        F2_FunText6.setTextSize(10);
        F2_FunText6.setText("REVERSE");
        F2_FunText7 = (TextView)v.findViewById(R.id.FunctionText7);
        F2_FunText7.setTextSize(10);
        F2_FunText7.setText("REFLEX");
        F2_FunText8 = (TextView)v.findViewById(R.id.FunctionText8);
        F2_FunText8.setTextSize(10);
        F2_FunText8.setText("SLIDE HEAD");
        F2_FunText9 = (TextView)v.findViewById(R.id.FunctionText9);
        F2_FunText9.setTextSize(10);
        F2_FunText9.setText("TILT R.");
        F2_FunText10 = (TextView)v.findViewById(R.id.FunctionText10);
        F2_FunText10.setTextSize(10);
        F2_FunText10.setText("REV. TREND.");
        F2_FunText11 = (TextView)v.findViewById(R.id.FunctionText11);
        F2_FunText11.setTextSize(10);
        F2_FunText11.setText("BACK UP");
        F2_FunText12 = (TextView)v.findViewById(R.id.FunctionText12);
        F2_FunText12.setTextSize(10);
        F2_FunText12.setText("TABLE UP");
        F2_FunText13 = (TextView)v.findViewById(R.id.FunctionText13);
        F2_FunText13.setTextSize(10);
        F2_FunText13.setText("TILT L.");
        F2_FunText14 = (TextView)v.findViewById(R.id.FunctionText14);
        F2_FunText14.setTextSize(10);
        F2_FunText14.setText("TREND.");
        F2_FunText15 = (TextView)v.findViewById(R.id.FunctionText15);
        F2_FunText15.setTextSize(10);
        F2_FunText15.setText("BACK DOWN");
        F2_FunText16 = (TextView)v.findViewById(R.id.FunctionText16);
        F2_FunText16.setTextSize(10);
        F2_FunText16.setText("TABLE DOWN");
        F2_FunText17 = (TextView)v.findViewById(R.id.FunctionText17);
        F2_FunText17.setTextSize(10);
        F2_FunText17.setText("Power Status");
        F2_FunText18 = (TextView)v.findViewById(R.id.FunctionText18);
        F2_FunText18.setTextSize(10);
        F2_FunText18.setText("LEVEL");
        F2_FunText19 = (TextView)v.findViewById(R.id.FunctionText19);
        F2_FunText19.setTextSize(10);
        F2_FunText19.setText("ON/OFF");
        F2_FunText20 = (TextView)v.findViewById(R.id.FunctionText20);
        F2_FunText20.setTextSize(10);
        F2_FunText20.setText("Slide Center");


        IV_Normal = (ImageView) v.findViewById(R.id.imageView_Normal);
        IV_Normal.bringToFront();
        IV_Reverse = (ImageView) v.findViewById(R.id.imageView_Reverse);
        IV_Reverse.bringToFront();

        SW_Output = 0x00;
        F2_Button1.setEnabled(false);
        F2_Button2.setEnabled(false);
        F2_Button3.setEnabled(false);
        F2_Button4.setEnabled(false);
        F2_Button5.setEnabled(false);
        F2_Button6.setEnabled(false);
        F2_Button7.setEnabled(false);
        F2_Button8.setEnabled(false);
        F2_Button9.setEnabled(false);
        F2_Button10.setEnabled(false);
        F2_Button11.setEnabled(false);
        F2_Button12.setEnabled(false);
        F2_Button13.setEnabled(false);
        F2_Button14.setEnabled(false);
        F2_Button15.setEnabled(false);
        F2_Button16.setEnabled(false);
//        F2_Button17.setEnabled(false);
//        F2_Button17.setVisibility(View.INVISIBLE);
        F2_Button18.setEnabled(false);
        //F2_Button19.setEnabled(false);
//        F2_Button20.setEnabled(false);
//        F2_Button20.setVisibility(View.INVISIBLE);
        LastDelayTime = 100;
    }
    public void Get_Fragment3(View v)
    {
        View_Fragment3 = v;
        textView3 = (TextView) v.findViewById(R.id.fragment3_text);
        textView3.setTextSize(20);

        F3_Button1 = (Button)v.findViewById(R.id.fragment3_button1);
        F3_Button1.setOnClickListener(b);
        F3_Button1.setOnTouchListener(b);
        F3_Button1.setText("TableUp");
        F3_Button2 = (Button)v.findViewById(R.id.fragment3_button2);
        F3_Button2.setOnClickListener(b);
        F3_Button2.setOnTouchListener(b);
        F3_Button2.setText("TableDown");
        F3_Button3 = (Button)v.findViewById(R.id.fragment3_button3);
        F3_Button3.setOnClickListener(b);
        F3_Button3.setOnTouchListener(b);
        F3_Button3.setText("Lock");
        F3_Button4 = (Button)v.findViewById(R.id.fragment3_button4);
        F3_Button4.setOnClickListener(b);
        F3_Button4.setOnTouchListener(b);
        F3_Button4.setText("Level");

    }




}



