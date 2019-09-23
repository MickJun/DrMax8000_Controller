package com.benqmedicaltech.Q300_Table_Controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Map;

public class Main3Activity extends AppCompatActivity implements View.OnClickListener {

    private Button M3_Scan;
    private Button M3_Connect;
    private Button M3_Update;
    private ListView M3_ListView ;
    private TextView M3_TextView ;

    private DeviceScanActivity DeviceScanAct = new DeviceScanActivity() ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.iqor_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);



        M3_TextView = this.findViewById(R.id.Main3_text);
        M3_TextView.setTextSize(25);
        M3_TextView.setText("Press SCAN Button");
//        M3_ListView = this.findViewById(R.id.Main3_List);

        M3_Scan = this.findViewById(R.id.Main3_button1);
        M3_Scan.setOnClickListener(this);
        M3_Connect = this.findViewById(R.id.Main3_button2);
        M3_Connect.setOnClickListener(this);
        M3_Update = this.findViewById(R.id.Main3_button3);
        M3_Update.setOnClickListener(this);

        Intent intent = new Intent();
        intent.setClass(Main3Activity.this, DeviceScanActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.Main3_button1:

//                DeviceScanAct.GetM3View(v);
                Intent intent = new Intent();
                intent.setClass(Main3Activity.this, DeviceScanActivity.class);
                startActivity(intent);

                break;
            case R.id.Main3_button2:
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                break;
            case R.id.Main3_button3: //list
                //Querying paired devices


                break;
        }
    }

}
