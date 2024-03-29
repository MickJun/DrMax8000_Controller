package com.benqmedicaltech.Q300_Table_Controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class DeviceCharacteristicActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_CHARACTERISTIC_INDEX = "CHARACTERISTIC_INDEX";
    public static final String EXTRAS_SERVICE_INDEX = "SERVICE_INDEX";

    public static Integer mCharacteristicIndex;
    public static Integer mServiceIndex;
    public EditText writeText;

    private static final int RESULT_DISCONNECT = 5;
    private static final int REQUEST_DISCONNECT = 5;

    private BLEService mBleService;
    public KeyboardView mKeyboardView;
    private boolean mNotifyStatus = false;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BLEService.LocalBinder) service).getService();
            if (!mBleService.initialize()) {
                //Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            String uuid = mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getUuid().toString();
            String charString = BLEGattAttributes.lookup(uuid, getResources().getString(R.string.unknown_characteristic));

            String charProperties = "";
            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                charProperties = charProperties.concat("Read ");
                ((Button) findViewById(R.id.characteristic_read_button)).setEnabled(true);
                ((TextView) findViewById(R.id.characteristic_read_label)).setEnabled(true);
            } else {
                ((Button) findViewById(R.id.characteristic_read_button)).setEnabled(false);
                ((TextView) findViewById(R.id.characteristic_read_label)).setEnabled(false);
            }

            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                charProperties = charProperties.concat("Write ");
                ((Button) findViewById(R.id.characteristic_write_button)).setEnabled(true);
                ((Button) findViewById(R.id.characteristic_stop_button)).setEnabled(true);
                ((EditText) findViewById(R.id.characteristic_write)).setEnabled(true);
                ((TextView) findViewById(R.id.characteristic_write_label)).setEnabled(true);
            } else {
                ((Button) findViewById(R.id.characteristic_write_button)).setEnabled(false);
                ((Button) findViewById(R.id.characteristic_stop_button)).setEnabled(true);
                ((EditText) findViewById(R.id.characteristic_write)).setEnabled(false);
                ((TextView) findViewById(R.id.characteristic_write_label)).setEnabled(false);
            }

            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                charProperties = charProperties.concat("WriteNoResponse ");
                ((Button) findViewById(R.id.characteristic_write_button)).setEnabled(true);
                ((Button) findViewById(R.id.characteristic_stop_button)).setEnabled(true);
                ((EditText) findViewById(R.id.characteristic_write)).setEnabled(true);
                ((TextView) findViewById(R.id.characteristic_write_label)).setEnabled(true);
            } else {
                //((Button) findViewById(R.id.characteristic_write_button)).setEnabled(false);
                //((EditText) findViewById(R.id.characteristic_write)).setEnabled(false);
            }

            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) > 0) {
                charProperties = charProperties.concat("SignedWrite ");
                ((Button) findViewById(R.id.characteristic_write_button)).setEnabled(true);
                ((Button) findViewById(R.id.characteristic_stop_button)).setEnabled(true);
                ((EditText) findViewById(R.id.characteristic_write)).setEnabled(true);
                ((TextView) findViewById(R.id.characteristic_write_label)).setEnabled(true);
            } else {
                //((Button) findViewById(R.id.characteristic_write_button)).setEnabled(false);
                //((EditText) findViewById(R.id.characteristic_write)).setEnabled(false);
            }

            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                charProperties = charProperties.concat("Notify ");
                ((TextView) findViewById(R.id.characteristic_notify)).setText("Notify/Indicate: " + "No");
                ((TextView) findViewById(R.id.characteristic_notify)).setEnabled(true);
                ((Switch) findViewById(R.id.charcteristic_notify_switch)).setEnabled(true);
                ((TextView) findViewById(R.id.characteristic_read_label)).setEnabled(true);
            } else {
                ((Switch) findViewById(R.id.charcteristic_notify_switch)).setEnabled(false);
                ((TextView) findViewById(R.id.characteristic_notify)).setEnabled(false);
            }

            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                charProperties = charProperties.concat("Indicate ");
                ((TextView) findViewById(R.id.characteristic_notify)).setText("Notify/Indicate: " + "No");
                ((TextView) findViewById(R.id.characteristic_notify)).setEnabled(true);
                ((Switch) findViewById(R.id.charcteristic_notify_switch)).setEnabled(true);
                ((TextView) findViewById(R.id.characteristic_read_label)).setEnabled(true);
            } else {
                //((Switch) findViewById(R.id.charcteristic_notify_switch)).setEnabled(false);
            }

            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_BROADCAST) > 0)
                charProperties = charProperties.concat("Broadcast ");

            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) > 0)
                charProperties = charProperties.concat("Extended ");


            if (charString.matches(getResources().getString(R.string.unknown_characteristic))) {
                ((TextView) findViewById(R.id.characteristic_uuid)).setText(/*"UUID:              "*/ " " + uuid);
                ((TextView) findViewById(R.id.characteristic_name)).setText(getResources().getString(R.string.unknown_characteristic) + " Characteristic");
            }
            else {
                if (charString.contains("Microchip"))
                    ((TextView) findViewById(R.id.characteristic_uuid)).setText(/*"UUID:              "*/ " " + uuid);
                else
                    ((TextView) findViewById(R.id.characteristic_uuid)).setText(/*"UUID:              "*/ " " + uuid.substring(4, 8));
                ((TextView) findViewById(R.id.characteristic_name)).setText(charString);
            }

            ((TextView) findViewById(R.id.characteristic_properties)).setText("Properties: " + charProperties);

            /*
            String charPermissions = "";
            int charPermission = mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getPermissions();
            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_READ) > 0) {
                charPermissions += "Read ";
            }
            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED) > 0) {
                charPermissions += "EncryptedRead ";
            }
            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM) > 0) {
                charPermissions += "EncryptedMITMRead ";
            }
            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_WRITE) > 0) {
                charPermissions += "Write ";
            }
            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED) > 0) {
                charPermissions += "EncryptedWrite ";
            }
            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM) > 0) {
                charPermissions += "EncryptedMITMWrite ";
            }
            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED) > 0) {
                charPermissions += "SignedWrite ";
            }
            if ((charPermission & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM) > 0) {
                charPermissions += "EncryptedMITMSignedWrite ";
            }
            ((TextView) findViewById(R.id.characteristic_permissions)).setText("Permissions: " + charPermissions);
            */

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                SetResult();
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BLEService.ACTION_GATT_WRITE.equals(action)) {
                ((Button) findViewById(R.id.characteristic_write_button)).setTextColor(Color.argb(0xFF, 0x00, 0x70, 0x00));
                ((Button) findViewById(R.id.characteristic_stop_button)).setTextColor(Color.argb(0xFF, 0x00, 0x70, 0x00));
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                ((TextView) findViewById(R.id.characteristic_read)).setText(intent.getStringExtra(BLEService.EXTRA_DATA));
            }
        }
    };

    private void SetResult() {
        setResult(RESULT_DISCONNECT, new Intent());
        finish();

    }

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

    public void toggleNotify(boolean isChecked) {
        mNotifyStatus = isChecked;
        if (isChecked) {
            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                mBleService.setCharacteristicIndication(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), true);
            } else {
                mBleService.setCharacteristicNotification(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), true);
            }
            ((TextView) findViewById(R.id.characteristic_notify)).setText("Notify/Indicate: " + "Yes");
        }
        else{
            if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                mBleService.setCharacteristicIndication(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), false);
            } else {
                mBleService.setCharacteristicNotification(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), false);
            }
            ((TextView) findViewById(R.id.characteristic_notify)).setText("Notify/Indicate: " + "No");
        }
    }

    public void hideCustomKeyboard() {
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    public void showCustomKeyboard( View v ) {
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if(v!=null)
            ((InputMethodManager)getSystemService(DeviceCharacteristicActivity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public boolean isCustomKeyboardVisible() {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    //Mick//////////////////////////////////////////////////////////////////////////////////////////////////
    private final byte Rev_Trend = 0x1;               //#define	Rev_Trend 			1
    private final byte Trend = 0x2;                   //#define	Trend				2
    private final byte Tilt_R = 0x3;                  //#define	Tilt_R				3
    private final byte Tilt_L = 0x4;                  //#define	Tilt_L				4
    private final byte Back_Up = 0x5;                 //#define	Back_Up				5
    private final byte Back_Down = 0x6;               //#define	Back_Down			6
    private final byte Table_Up = 0x7;                //#define	Table_Up			7
    private final byte Table_Down = 0x8;              //#define	Table_Down			8
    private final byte Slide_Foot = 0x9;              //#define	Slide_Foot			9
    private final byte Slide_Head = 0xa;              //#define	Slide_Head			10
    private final byte Leg_Up = 0xb;                  //#define	Leg_Up				11
    private final byte Leg_Down = 0xc;                //#define	Leg_Down			12
    private final byte Unlock = 0xd;                  //#define	Unlock				13
    private final byte Lock = 0xe;                    //#define	Lock				14
    private final byte Kidney_Up = 0xf;               //#define   Kidney_Up           15
    private final byte Kidney_Down = 0x10;               //#define   Kidney_Down           16
    private final byte Flex = 0x15;                   //#define	Flex				21
    private final byte Reflex = 0x16;                 //#define	Reflex				22
    private final byte Level = 0x17;                  //#define	Level				23
    private final  byte Normal_Function = 0x21;                 //#define     Normal_Function                  33
    private final byte Reverse_Function = 0x22;                 //#define     Reverse_Function                  34
    private byte SW_Output = 0x00;
    private byte Checksum = 0x00;
    private final byte[] Output_First = new byte[8];
    private final byte[] Output_Function = new byte[8];
    private final byte[] Output_Checksum = new byte[8];
    private final byte[] Output_Final = new byte[24];
    private void Output_First_Set()
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

    private void Function_Code()
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

    private void Checksum_Code()
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
//        }
//        for(int i =0;i<8;i++){
            Output_Final[i+8] = Output_Function[i];
//        }
//        for(int i =0;i<8;i++){
            Output_Final[i+16] = Output_Checksum[i];
        }

    }
    private void Table_Command_Send_Start(byte command){
        SW_Output = command;
//        if(f2.Reverse_Flag == 1) {
//            switch (command){
//                case  Slide_Foot:
//                    SW_Output = Slide_Head;
//                    break;
//                case  Slide_Head:
//                    SW_Output = Slide_Foot;
//                    break;
//                case  Tilt_L:
//                    SW_Output = Tilt_R;
//                    break;
//                case  Tilt_R:
//                    SW_Output = Tilt_L;
//                    break;
//                case  Trend:
//                    SW_Output = Rev_Trend;
//                    break;
//                case  Rev_Trend:
//                    SW_Output = Trend;
//                    break;
//                case  Back_Up:
//                    SW_Output = Leg_Up;
//                    break;
//                case  Back_Down:
//                    SW_Output = Leg_Down;
//                    break;
//                case  Leg_Up:
//                    SW_Output = Back_Up;
//                    break;
//                case  Leg_Down:
//                    SW_Output = Back_Down;
//                    break;
//            }
//        }
        Output_First_Set();
        Function_Code();
        Checksum_Code();
        Output_Mix();
//            mDelayTime = 90;
//            handler.postDelayed(runnable,mDelayTime);

    }
    private final Handler handler = new Handler();
    private int mDelayTime = 0;
    private int LastDelayTime = 0;
    private final int Max_Delay_Time = 111;      //Lock Time = 90ms * Max_Delay_Time    10s / 90ms = 111

    private final Runnable runnable = new Runnable() {
        public void run() {
            //update
            if(SW_Output != 0x00){
                LastDelayTime = 0;
                mBleService.writeCharacteristic(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), Output_Final);//value          //////////////////////////////////這邊傳拉
            }
            else
            {
                if(LastDelayTime < Max_Delay_Time)LastDelayTime++;
            }
            if(mDelayTime > 0){
                handler.postDelayed(this,mDelayTime);
            }

        }
    };



    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_characteristic);

        getActionBar().setTitle(R.string.title_characteristic);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        final Intent intent = getIntent();
        mCharacteristicIndex = Integer.parseInt(intent.getStringExtra(EXTRAS_CHARACTERISTIC_INDEX)); //intent.getIntExtra(EXTRAS_CHARACTERISTIC_INDEX, 0);
        mServiceIndex = Integer.parseInt(intent.getStringExtra(EXTRAS_SERVICE_INDEX)); // + 2;


        Switch notifySwitch = (Switch) findViewById(R.id.charcteristic_notify_switch);
        notifySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleNotify(isChecked);
            }
        });

        Button readButton = (Button) findViewById(R.id.characteristic_read_button);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleService.readCharacteristic(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex));
            }
        });

        writeText = (EditText) findViewById(R.id.characteristic_write);
        writeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ((Button)findViewById(R.id.characteristic_write_button)).setTextColor(Color.BLACK);
                ((Button)findViewById(R.id.characteristic_stop_button)).setTextColor(Color.BLACK);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        writeText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    showCustomKeyboard(v);
                else
                    hideCustomKeyboard();
            }
        });

        writeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomKeyboard(v);
            }
        });

        writeText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.setFocusable(true);
                edittext.setFocusableInTouchMode(true);
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);              // Restore input type
                return true; // Consume touch event
            }
        });
        final Button stopButton = (Button) findViewById(R.id.characteristic_stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDelayTime = 0;
            }
        });
        final Button writeButton = (Button) findViewById(R.id.characteristic_write_button);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!writeText.getText().toString().isEmpty()) {
//                    byte[] value = new byte[writeText.getText().toString().length() / 2];
//                    for (int i = 0; i < writeText.getText().toString().length(); i++) {
//                        char c = writeText.getText().toString().charAt(i);
//                        if ((c >= '0') && (c <= '9')) {
//                            if (i % 2 == 1)
//                                value[i / 2] = (byte) ((value[i / 2] << 4) + (c - 0x30));
//                            else
//                                value[i / 2] = (byte) (c - 0x30);
//                        }
//                        if ((c >= 'A') && (c <= 'F')) {
//                            if (i % 2 == 1)
//                                value[i / 2] = (byte) ((value[i / 2] << 4) + (0x0A + c - 0x41));
//                            else
//                                value[i / 2] = (byte) (0x0A + c - 0x41);
//                        }
//                    }
                    char a = writeText.getText().toString().charAt(0);
                    if(writeText.getText().toString().length() < 2){
                        return;
                    }
                    char b = writeText.getText().toString().charAt(1);
                    int c;
                    c = (a-0x30)*10 + (b-0x30);
                    switch (c) {
                        case 1:
                            Table_Command_Send_Start(Rev_Trend);
                            break;
                        case 2:
                            Table_Command_Send_Start(Trend);
                            break;
                        case 3:
                            Table_Command_Send_Start(Tilt_R);
                            break;
                        case 4:
                            Table_Command_Send_Start(Tilt_L);
                            break;
                        case 5:
                            Table_Command_Send_Start(Back_Up);
                            break;
                        case 6:
                            Table_Command_Send_Start(Back_Down);
                            break;
                        case 7:
                            Table_Command_Send_Start(Table_Up);
                            break;
                        case 8:
                            Table_Command_Send_Start(Table_Down);
                            break;
                        case 9:
                            Table_Command_Send_Start(Slide_Foot);
                            break;
                        case 10:
                            Table_Command_Send_Start(Slide_Head);
                            break;
                        case 11:
                            Table_Command_Send_Start(Leg_Up);
                            break;
                        case 12:
                            Table_Command_Send_Start(Leg_Down);
                            break;
                        case 13:
                            Table_Command_Send_Start(Unlock);
                            break;
                        case 14:
                            Table_Command_Send_Start(Lock);
                            break;
                        case 23:
                            Table_Command_Send_Start(Level);
                            break;
                        case 33:
                            Table_Command_Send_Start(Normal_Function);
                            break;
                        case 34:
                            Table_Command_Send_Start(Reverse_Function);
                            break;
                        default:
                            c=0;
                            break;
                    }
                    if(c == 0){
                        mDelayTime = 0;
                    }else{
                        mDelayTime = 90;
                        handler.postDelayed(runnable, mDelayTime);
                    }
                    writeButton.setTextColor(Color.BLACK);
//                    mBleService.writeCharacteristic(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), Output_Final);//value          //////////////////////////////////這邊傳拉
                }
            }
        });

        Keyboard mKeyboard = new Keyboard(getBaseContext(), R.xml.keyboard_hex);
        mKeyboardView = (KeyboardView) findViewById(R.id.keyboard_view);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {

            public final static int CodeDelete = -5; // Keyboard.KEYCODE_DELETE
            public final static int CodeCancel = -3; // Keyboard.KEYCODE_CANCEL
            public final static int CodePrev = 55000;
            public final static int CodeAllLeft = 55001;
            public final static int CodeLeft = 55002;
            public final static int CodeRight = 55003;
            public final static int CodeAllRight = 55004;
            public final static int CodeNext = 55005;
            public final static int CodeClear = 55006;

            @Override
            public void onPress(int primaryCode) {

            }

            @Override
            public void onRelease(int primaryCode) {

            }

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                // Get the EditText and its Editable
                View focusCurrent = DeviceCharacteristicActivity.this.getWindow().getCurrentFocus();
                if (focusCurrent == null || focusCurrent.getClass() != EditText.class) return;
                EditText edittext = (EditText) focusCurrent;
                Editable editable = edittext.getText();
                int start = edittext.getSelectionStart();
                // Handle key
                if (primaryCode == CodeCancel) {
                    hideCustomKeyboard();
                } else if (primaryCode == CodeDelete) {
                    if (editable != null && start > 0) editable.delete(start - 1, start);
                } else if (primaryCode == CodeClear) {
                    if (editable != null) editable.clear();
                } else if (primaryCode == CodeLeft) {
                    if (start > 0) edittext.setSelection(start - 1);
                } else if (primaryCode == CodeRight) {
                    if (start < edittext.length()) edittext.setSelection(start + 1);
                } else if (primaryCode == CodeAllLeft) {
                    edittext.setSelection(0);
                } else if (primaryCode == CodeAllRight) {
                    edittext.setSelection(edittext.length());
                } else if (primaryCode == CodePrev) {
                    //View focusNew = edittext.focusSearch(View.FOCUS_BACKWARD);
                    //if (focusNew != null) focusNew.requestFocus();
                } else if (primaryCode == CodeNext) {
                    //View focusNew = edittext.focusSearch(View.FOCUS_FORWARD);
                    //if (focusNew != null) focusNew.requestFocus();
                } else {// Insert character
                    editable.insert(start, Character.toString((char) primaryCode));
                }
            }

            @Override
            public void onText(CharSequence text) {

            }

            @Override
            public void swipeLeft() {

            }

            @Override
            public void swipeRight() {

            }

            @Override
            public void swipeDown() {

            }

            @Override
            public void swipeUp() {

            }


        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //Log.d("Index:", mCharacteristicIndex.toString() + mServiceIndex.toString());


    }

    @Override
    public void onBackPressed() {

        if (isCustomKeyboardVisible()) {
            hideCustomKeyboard();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (mNotifyStatus &&
                    (((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) ||
                            ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0))) {

                if ((mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex).getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                    mBleService.setCharacteristicIndication(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), false);
                } else {
                    mBleService.setCharacteristicNotification(mBleService.getSupportedGattServices().get(mServiceIndex).getCharacteristics().get(mCharacteristicIndex), false);
                }

                ((TextView) findViewById(R.id.characteristic_notify)).setText("Notify/Indicate: " + "No");
            }
        } catch (Exception e) {}

        unregisterReceiver(bluetoothReceiver);

        unbindService(mServiceConnection);
        mBleService = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_GATT_WRITE);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
