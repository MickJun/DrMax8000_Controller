package com.benqmedicaltech.fragment_test;


import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
//import android.app.Fragment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment2 extends Fragment {

    private TextView tv;
    private String name;
//    public Fragment2() {
//        // Required empty public constructor
//    }
    public Fragment2(String fName) {
        // Required empty public constructor
        this.name = fName;
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_fragment2, container, false);
//    }

    private View MyView;
    private  ImageView mIV_Reverse;
    private  ImageView mIV_Normal;

    public String MyString = "Loading...";
    private Handler handler = new Handler();
    int mDelayTime = 300;
    public TextView myTextView;
    int LED_Check = 0;
    byte[] Table_Power_Status = new byte[3];
    byte[] Output_Table = new byte[1024];

    public int Update_Flag = 0;

    public void Update_Status(byte[] Output_Temp, TextView mtv) {
        Update_Flag = 1;
        myTextView = mtv;
        Output_Table = Output_Temp;
    }

    public int Change_Photo_Flag = 0;
    public int Change_Button_Number = 0;
    public int Change_Function_Number = 0;
    public int Chenged_Button = 0;
    public String Change_Photo_Name = "";


    public void Change_Button(int Change_Function,int Button_Number , String photo_Name) {
        Change_Photo_Flag = 1;
        Change_Function_Number = Change_Function;
        Change_Button_Number = Button_Number;
        Change_Photo_Name = photo_Name;
    }

    public void Press_Button_UI(int Button_Number , String photo_Name){


    }

    public void Release_Button_UI(int Button_Number , String photo_Name){


    }

    public void Lock_Button_UI(int Button_Number , String photo_Name){

    }

    public void Unlock_Button_UI(int Button_Number , String photo_Name){

    }




    public void check_Data(){
        //byte[] Output_Table = ;
        byte[] Temp_int = new byte[24];
        byte Temp_Char;
        byte Info1 = 0x00, Info2 = 0x00, Info3 = 0x00, Checksum = 0x00, Check_OK = 0x01;
        for (int x = 0; x < 24; x++)
        {
            Temp_Char = (byte)(Output_Table[x] & 0xFF);
            Output_Table[x] = (byte)0xAA;
            if (Temp_Char == 85)
            {
                Temp_int[x] = 0;
            }
            else if (Temp_Char == 0)
            {
                int y = x;
                if (y > 15) { y = y - 16; }
                if (y > 7) { y = y - 8; }
                y = 7 - y;
                Temp_int[x] = (byte)(1 << y);
            }
            else
            {
                x = 24;
                Check_OK = 0x00;
                LED_Check = 0;
            }
        }
        if (Check_OK != 0x00)
        {
            //Check_OK = 0;
            //bit 7 -> bit 0
            Info1 = (byte)(Temp_int[0] + Temp_int[1] + Temp_int[2] + Temp_int[3] + Temp_int[4] + Temp_int[5] + Temp_int[6] + Temp_int[7]);
            Info2 = (byte)(Temp_int[8] + Temp_int[9] + Temp_int[10] + Temp_int[11] + Temp_int[12] + Temp_int[13] + Temp_int[14] + Temp_int[15]);
            Info3 = (byte)(Temp_int[16] + Temp_int[17] + Temp_int[18] + Temp_int[19] + Temp_int[20] + Temp_int[21] + Temp_int[22] + Temp_int[23]);
            Checksum = (byte)(Info1 + Info2 - 1);
            if (Checksum == Info3)
            {
                Table_Power_Status[0] = Info1;
                Table_Power_Status[1] = Info2;
                LED_Check = 1;
            }
        }

        if (LED_Check == 1)
        {
            LED_Check = 0;
            MyString = "";
            //有充電板
            if (((Table_Power_Status[0] & 0x10) == 0x00))
            {
                //Image_Table_BATTERY.Visibility = System.Windows.Visibility.Visible;
                //Image_Lable_BATTERY.Visibility = System.Windows.Visibility.Visible;
                //有AC電
                if ((Table_Power_Status[0] & 0x01) == 0x01)
                {
                    //Image_Table_Power.Source = new BitmapImage(new Uri("Photo/POWER1-1.png", UriKind.Relative));
                    //充電中
                    MyString = "AC";
                }
                else//無AC電
                {
                    MyString = "non AC";
                }
                if ((Table_Power_Status[0] & 0x0E) == 0x0E)         //100%電量
                {
                    MyString = MyString + "_100%";
                }
                else if ((Table_Power_Status[0] & 0x0E) == 0x06)    //75%電量
                {
                    MyString = MyString + "_75%";
                }
                else if ((Table_Power_Status[0] & 0x0E) == 0x02)    //30%電量
                {
                    MyString = MyString + "_30%";
                }
                else        //電量過低 (<30%)
                {
                    MyString = MyString + "<30%";
                }
            }
            else    //純AC電//無充電板
            {
                MyString = "AC Table";
            }

            if (((Table_Power_Status[1] & 0x01) == 0x01)) //Slide center
            {
                MyString = MyString + "_SC";
            }
            else
            {
            }
            if (((Table_Power_Status[1] & 0x02) == 0x02)) //inter lock
            {
                MyString = MyString + "_Bz";
            }
            else
            {
            }
            //if (FL_Lock_Black_Screen_Count == 0)
            {
                if (((Table_Power_Status[0] & 0x20) == 0x20)) //Table Lock
                {
                    MyString = MyString + "_Lock";
                }
                else
                {
                    MyString = MyString + "_UnLock";
                }
            }
            if (((Table_Power_Status[1] & 0x10) == 0x10)) //inter lock
            {
                MyString = MyString + "_Reverse";
//                mIV_Reverse.setImageDrawable(getResources().getDrawable( R.drawable.bisor_gui_report_content_icon_record_light_on,null));
//                mIV_Reverse.bringToFront();
//                mIV_Normal.setImageDrawable(getResources().getDrawable( R.drawable.bisor_gui_report_content_icon_record_light_off,null));
//                mIV_Normal.bringToFront();

            }
            else
            {
                MyString = MyString + "_Normal";
//                mIV_Reverse.setImageDrawable(getResources().getDrawable( R.drawable.bisor_gui_report_content_icon_record_light_off,null));
//                mIV_Reverse.bringToFront();
//                mIV_Normal.setImageDrawable(getResources().getDrawable( R.drawable.bisor_gui_report_content_icon_record_light_on,null));
//                mIV_Normal.bringToFront();
            }
        }





    }

    private Runnable runnable = new Runnable() {
        public void run() {
            //update
            if(Update_Flag == 0) {
                check_Data();
                if (myTextView != null) myTextView.setText(MyString);
                handler.postDelayed(this, mDelayTime);
                if (((Table_Power_Status[1] & 0x10) == 0x10)) //inter lock
                {
                    mIV_Reverse.setImageDrawable(getResources().getDrawable(R.drawable.bisor_gui_report_content_icon_record_light_on, null));
                    mIV_Reverse.bringToFront();
                    mIV_Normal.setImageDrawable(getResources().getDrawable(R.drawable.bisor_gui_report_content_icon_record_light_off, null));
                    mIV_Normal.bringToFront();

                } else {
                    mIV_Reverse.setImageDrawable(getResources().getDrawable(R.drawable.bisor_gui_report_content_icon_record_light_off, null));
                    mIV_Reverse.bringToFront();
                    mIV_Normal.setImageDrawable(getResources().getDrawable(R.drawable.bisor_gui_report_content_icon_record_light_on, null));
                    mIV_Normal.bringToFront();
                }
            }



        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_fragment2,container,false);
        MyView = view;


        mIV_Reverse = (ImageView) MyView.findViewById(R.id.imageView_Reverse);
        mIV_Normal  = (ImageView) MyView.findViewById(R.id.imageView_Normal);
//        tv = (TextView) view.findViewById(R.id.fragment2_text);
//        tv.setText(name);
//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tv.setText("我变了-" + name);
//            }
//        });
        handler.postDelayed(this.runnable,mDelayTime);
        ((MainActivity)this.getActivity()).Get_Fragment2(view);
        return view;
    }

    @Override
    public void onDestroy() {
        if(handler != null)handler.removeMessages(0);
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            //Fragment隐藏时调用
        } else {
            //Fragment显示时调用
        }
    }


}
