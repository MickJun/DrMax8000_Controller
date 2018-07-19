package com.benqmedicaltech.fragment_test;


import android.bluetooth.BluetoothProfile;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
//import android.app.Fragment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment2 extends Fragment {

//    private TextView tv;
//    private String name;
//    public Fragment2() {
//        // Required empty public constructor
//    }
//    public Fragment2(String fName) {
//        // Required empty public constructor
//        this.name = fName;
//    }

    public static Fragment2 newInstance(String name) {
        Fragment2 newFragment = new Fragment2();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        newFragment.setArguments(bundle);
        return newFragment;
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_fragment2, container, false);
//    }

//    private static final byte Rev_Trend = 0x1;               //#define	Rev_Trend 			1
//    private static final byte Trend = 0x2;                   //#define	Trend				2
//    private static final byte Tilt_R = 0x3;                  //#define	Tilt_R				3
//    private static final byte Tilt_L = 0x4;                  //#define	Tilt_L				4
//    private static final byte Back_Up = 0x5;                 //#define	Back_Up				5
//    private static final byte Back_Down = 0x6;               //#define	Back_Down			6
//    private static final byte Table_Up = 0x7;                //#define	Table_Up			7
//    private static final byte Table_Down = 0x8;              //#define	Table_Down			8
//    private static final byte Slide_Foot = 0x9;              //#define	Slide_Foot			9
//    private static final byte Slide_Head = 0xa;              //#define	Slide_Head			10
//    private static final byte Leg_Up = 0xb;                  //#define	Leg_Up				11
//    private static final byte Leg_Down = 0xc;                //#define	Leg_Down			12
//    private static final byte Unlock = 0xd;                  //#define	Unlock				13
//    private static final byte Lock = 0xe;                    //#define	Lock				14
//    private static final byte Kidney_Up = 0xf;               //#define   Kidney_Up           15
//    private static final byte Kidney_Down = 0x10;               //#define   Kidney_Down           16
//    private static final byte Flex = 0x15;                   //#define	Flex				21
//    private static final byte Reflex = 0x16;                 //#define	Reflex				22
//    private static final byte Level = 0x17;                  //#define	Level				23
//    //byte Config_TS710 = 0x18;           //#define	Config_TS710		24
//    //byte Config_7000_Rev = 0x19;        //#define   Config_7000_Rev	    25
//    //byte Config_Q100 = 0x1b;            //#define	Config_Q100			27
//    //byte Config_650NS = 0x1c;           //#define	Config_MOT1600_650	28
//    //byte Unlock_Stop = 0x1e;            //#define	Unlock_Stop			30          //650NS  進入腳踏模式=馬達無動作
//    //byte Lock_Run = 0x1f;               //#define	Lock_Run			31          //650NS  取消腳踏模式=馬達有動作
//    private static final byte Normal_Function = 0x21;                 //#define     Normal_Function                  33
//    private static final byte Reverse_Function = 0x22;                 //#define     Reverse_Function                  34
//    private static final byte Set_M1 = 0x23;                 //#define     Set_M1                  35  //Save Angle in Memery now
//    private static final byte Set_M2 = 0x24;                 //#define     Set_M2                  36  //Save Angle in Memery now
//    //byte Config_Mode = 0x37;
//    //byte Restrict_Function = 0x42;      //#define   Restrict_Function	66
//    private static final byte No_Function = 0x4d;            //#define   No_Function		    77
//    //byte Shutdown = 0x58;               //#define   Shutdown			88
//    byte SW_Output = 0x00;
//    byte Checksum = 0x00;

    private  ImageView mIV_Reverse;
    private  ImageView mIV_Normal;
    private  ImageView mIV_Status1;
    private  ImageView mIV_Status2;
    private  int status_path1 = R.drawable.bisor_gui_eq_content_icon_poweron;
    private  int status_path2 = R.drawable.bisor_gui_eq_content_icon_slidecenterlight_normal;

    private String MyString = "Loading...";
    private final Handler handler = new Handler();
    private final int mDelayTime = 100;
    private TextView myTextView;
    private int LED_Check = 0;
    private final byte[] Table_Power_Status = new byte[3];
    private byte[] Output_Table = new byte[1024];

    private int Update_Flag = 0;

    public void Update_Status(byte[] Output_Temp, TextView mtv) {
        Update_Flag = 1;
        myTextView = mtv;
        Output_Table = Output_Temp;
    }


    public void setF2_Button1(Button f2_Button1) {
        F2_Button1 = f2_Button1;
    }

    public void setF2_Button2(Button f2_Button2) {
        F2_Button2 = f2_Button2;
    }

    public void setF2_Button3(Button f2_Button3) {
        F2_Button3 = f2_Button3;
    }

    public void setF2_Button4(Button f2_Button4) {
        F2_Button4 = f2_Button4;
    }

    public void setF2_Button5(Button f2_Button5) {
        F2_Button5 = f2_Button5;
    }

    public void setF2_Button6(Button f2_Button6) {
        F2_Button6 = f2_Button6;
    }

    public void setF2_Button7(Button f2_Button7) {
        F2_Button7 = f2_Button7;
    }

    public void setF2_Button8(Button f2_Button8) {
        F2_Button8 = f2_Button8;
    }

    public void setF2_Button9(Button f2_Button9) {
        F2_Button9 = f2_Button9;
    }

    public void setF2_Button10(Button f2_Button10) {
        F2_Button10 = f2_Button10;
    }

    public void setF2_Button11(Button f2_Button11) {
        F2_Button11 = f2_Button11;
    }

    public void setF2_Button12(Button f2_Button12) {
        F2_Button12 = f2_Button12;
    }

    public void setF2_Button13(Button f2_Button13) {
        F2_Button13 = f2_Button13;
    }

    public void setF2_Button14(Button f2_Button14) {
        F2_Button14 = f2_Button14;
    }

    public void setF2_Button15(Button f2_Button15) {
        F2_Button15 = f2_Button15;
    }

    public void setF2_Button16(Button f2_Button16) {
        F2_Button16 = f2_Button16;
    }

//    public void setF2_Button17(Button f2_Button17) {
//        F2_Button17 = f2_Button17;
//    }

    public void setF2_Button18(Button f2_Button18) {
        F2_Button18 = f2_Button18;
    }

    public void setF2_Button19(Button f2_Button19) {
        F2_Button19 = f2_Button19;
    }

//    public void setF2_Button20(Button f2_Button20) {
//        F2_Button20 = f2_Button20;
//    }

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
    //private Button F2_Button17;
    private Button F2_Button18;
    private Button F2_Button19;
    //private Button F2_Button20;


    private int Button_Lock = 1;
    private int Change_Photo_Flag = 0;
//    private int Change_Button_Number = 0;
//    private int Change_Function_Number = 0;
//    private int Chenged_Button = 0;
//    private String Change_Photo_Name = "";
    private static final int Function_non = 0;
    private static final int Function_press = 1;
    private static final int Function_release = 2;
    private static final int Function_lock = 3;
    private static final int Function_unlock = 4;

    private  Button Press_Button;
    private int Press_Drawable;
    private int Need_Release_Flag = 0;
    private Button Last_Button;
    private int Last_Drawable;
    private  int Need_Release_Last = 0;

    public void Press_Button_UI(Button getButton){

        if(Need_Release_Flag == 1){
            Need_Release_Last = 1;
            Last_Button = Press_Button;
            Release_Button_UI(Last_Button);
            Last_Drawable = Release_Drawable;
        }
        Change_Photo_Flag = Function_press;
        Press_Button = getButton;
        //Press_Drawable = getPhoto_Name;

        switch (Press_Button.getId()) {
            case R.id.fragment2_button1:
                if(Locking_Flag == 0) {
                    Press_Drawable = R.drawable.bisor_gui_eq_content_button_flunlockon_press;
                }
                else{
                    Press_Drawable = R.drawable.bisor_gui_eq_content_button_flunlockoff_press;
                }
                break;
            case R.id.fragment2_button2:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_normal_press;
                break;
            case R.id.fragment2_button3:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_flex_press;
                break;
            case R.id.fragment2_button4:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_slidefoot_press;
                break;
            case R.id.fragment2_button5:
                if(Locking_Flag == 0) {
                    Press_Drawable = R.drawable.bisor_gui_eq_content_button_fllockoff_press;
                }
                else{
                    Press_Drawable = R.drawable.bisor_gui_eq_content_button_fllockon_press;
                }
                break;
            case R.id.fragment2_button6:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_reverse_press;
                break;
            case R.id.fragment2_button7:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_reflex_press;
                break;
            case R.id.fragment2_button8:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_slidehead_press;
                break;
            case R.id.fragment2_button9:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_tiltright_press;
                break;
            case R.id.fragment2_button10:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_revtrend_press;
                break;
            case R.id.fragment2_button11:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_backup_press;
                break;
            case R.id.fragment2_button12:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_tableup_press;
                break;
            case R.id.fragment2_button13:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_tiltleft_press;
                break;
            case R.id.fragment2_button14:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_trend_press;
                break;
            case R.id.fragment2_button15:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_backdw_press;
                break;
            case R.id.fragment2_button16:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_tabledw_press;
                break;
            case R.id.fragment2_button18:
                Press_Drawable = R.drawable.bisor_gui_eq_content_button_levelcenter_press;
                break;
        }

    }

    private Button Release_Button;
    private int Release_Drawable;

    public void Release_Button_UI(Button getButton){
        Change_Photo_Flag = Function_release;
        Release_Button = getButton;
        //Release_Drawable = getPhoto_Name;


        switch (Release_Button.getId()) {
            case R.id.fragment2_button1:
                if(Locking_Flag == 0) {
                    Release_Drawable = R.drawable.bisor_gui_eq_content_button_flunlockon_normal;
                }
                else{
                    Release_Drawable = R.drawable.bisor_gui_eq_content_button_flunlockoff_normal;
                }
                break;
            case R.id.fragment2_button2:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_normal_normal;
                break;
            case R.id.fragment2_button3:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_flex_normal;
                break;
            case R.id.fragment2_button4:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_slidefoot_normal;
                break;
            case R.id.fragment2_button5:
                if(Locking_Flag == 0) {
                    Release_Drawable = R.drawable.bisor_gui_eq_content_button_fllockoff_normal;
                }
                else{
                    Release_Drawable = R.drawable.bisor_gui_eq_content_button_fllockon_normal;
                }
                break;
            case R.id.fragment2_button6:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_reverse_normal;
                break;
            case R.id.fragment2_button7:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_reflex_normal;
                break;
            case R.id.fragment2_button8:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_slidehead_normal;
                break;
            case R.id.fragment2_button9:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_tiltright_normal;
                break;
            case R.id.fragment2_button10:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_revtrend_normal;
                break;
            case R.id.fragment2_button11:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_backup_normal;
                break;
            case R.id.fragment2_button12:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_tableup_normal;
                break;
            case R.id.fragment2_button13:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_tiltleft_normal;
                break;
            case R.id.fragment2_button14:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_trend_normal;
                break;
            case R.id.fragment2_button15:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_backdw_normal;
                break;
            case R.id.fragment2_button16:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_tabledw_normal;
                break;
            case R.id.fragment2_button17:
                break;
            case R.id.fragment2_button18:
                Release_Drawable = R.drawable.bisor_gui_eq_content_button_levelcenter_normal;
                break;
        }


    }

    public void Lock_Button_UI(){
        Change_Photo_Flag = Function_lock;
        Button_Lock = 1;
    }

    public void Unlock_Button_UI(){
        Change_Photo_Flag = Function_unlock;
        Button_Lock = 0;
    }


    public int Locking_Flag = 0;
    public int Reverse_Flag = 0;

    private void check_Data(){
        //byte[] Output_Table = ;
        byte[] Temp_int = new byte[24];
        byte Temp_Char;
        byte Info1, Info2, Info3, Checksum, Check_OK = 0x01;
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
                    status_path1 = R.drawable.bisor_gui_eq_content_icon_poweron_battery_charge;
                }
                else//無AC電
                {
                    MyString = "non AC";
                    if ((Table_Power_Status[0] & 0x0E) == 0x0E)         //100%電量
                    {
                        MyString = MyString + "_100%";
                        status_path1 = R.drawable.bisor_gui_eq_content_icon_battery100;
                    }
                    else if ((Table_Power_Status[0] & 0x0E) == 0x06)    //75%電量
                    {
                        MyString = MyString + "_75%";
                        status_path1 = R.drawable.bisor_gui_eq_content_icon_battery75;
                    }
                    else if ((Table_Power_Status[0] & 0x0E) == 0x02)    //30%電量
                    {
                        MyString = MyString + "_30%";
                        status_path1 = R.drawable.bisor_gui_eq_content_icon_battery30;
                    }
                    else        //電量過低 (<30%)
                    {
                        MyString = MyString + "<30%";
                        status_path1 = R.drawable.bisor_gui_eq_content_icon_battery000;
                    }
                }
            }
            else    //純AC電//無充電板
            {
                MyString = "AC Table";
                status_path1 = R.drawable.bisor_gui_eq_content_icon_poweron;
            }

            if (((Table_Power_Status[1] & 0x01) == 0x01)) //Slide center
            {
                MyString = MyString + "_SC";
                status_path2 = R.drawable.bisor_gui_eq_content_icon_slidecenterlight_focus;
            }
            else
            {
                status_path2 = R.drawable.bisor_gui_eq_content_icon_slidecenterlight_normal;
            }
            if (((Table_Power_Status[1] & 0x02) == 0x02)) //inter lock
            {
                MyString = MyString + "_Bz";
            }
//            else
//            {
//            }
            //if (FL_Lock_Black_Screen_Count == 0)
            {
                if (((Table_Power_Status[0] & 0x20) == 0x20)) //Table Lock
                {
                    MyString = MyString + "_Lock";
                    Locking_Flag = 1;
                }
                else
                {
                    MyString = MyString + "_UnLock";
                    Locking_Flag= 0;
                }
            }
            if (((Table_Power_Status[1] & 0x10) == 0x10)) //inter lock
            {
                MyString = MyString + "_Reverse";
                if(Reverse_Flag == 0){
                    ((MainActivity)this.getActivity()).Reverse_BtnLock();
                    if(Button_Lock == 0){
                        Change_Photo_Flag = Function_unlock;
                    }
                }
                Reverse_Flag = 1;
        }
            else
            {
                MyString = MyString + "_Normal";
                if(Reverse_Flag == 1){
                    ((MainActivity)this.getActivity()).Reverse_BtnUnLock();
                    if(Button_Lock == 0){
                        Change_Photo_Flag = Function_unlock;
                    }
                }
                Reverse_Flag = 0;
            }
        }





    }

    private final Runnable runnable = new Runnable() {
        public void run() {

            if(Update_Flag == 1) {//Power Status Update
                check_Data();
                if (myTextView != null) myTextView.setText(MyString);
                if (Reverse_Flag == 1){
                    mIV_Reverse.setImageDrawable(getResources().getDrawable(R.drawable.bisor_gui_report_content_icon_record_light_on, null));
                    mIV_Normal.setImageDrawable(getResources().getDrawable(R.drawable.bisor_gui_report_content_icon_record_light_off, null));

                } else {
                    mIV_Reverse.setImageDrawable(getResources().getDrawable(R.drawable.bisor_gui_report_content_icon_record_light_off, null));
                    mIV_Normal.setImageDrawable(getResources().getDrawable(R.drawable.bisor_gui_report_content_icon_record_light_on, null));
                }
                mIV_Status1.setImageDrawable(getResources().getDrawable(status_path1, null));
                mIV_Status2.setImageDrawable(getResources().getDrawable(status_path2, null));
                Update_Flag = 0;
            }

            //Button Image
            if(Need_Release_Last == 1){
                Last_Button.setBackgroundResource(Last_Drawable);
                Need_Release_Last = 0;
            }
            if(Change_Photo_Flag == Function_press ) {
                Press_Button.setBackgroundResource(Press_Drawable);
                Need_Release_Flag = 1;
                Change_Photo_Flag = Function_non;
            }
            else if(Change_Photo_Flag == Function_release){
                Release_Button.setBackgroundResource(Release_Drawable);
                Need_Release_Flag = 0;
                Change_Photo_Flag = Function_non;
            }
            else if(Change_Photo_Flag == Function_lock) {
                F2_Button1.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_flunlockon_disable);
                F2_Button2.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_normal_disable);
                F2_Button3.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_flex_disable);
                F2_Button4.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_slidefoot_disable);
                F2_Button5.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_fllockon_disable);
                F2_Button6.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_reverse_disable);
                F2_Button7.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_reflex_disable);
                F2_Button8.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_slidehead_disable);
                F2_Button9.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_tiltright_disable);
                F2_Button10.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_revtrend_disable);
                F2_Button11.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_backup_disable);
                F2_Button12.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_tableup_disable);
                F2_Button13.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_tiltleft_disable);
                F2_Button14.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_trend_disable);
                F2_Button15.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_backdw_disable);
                F2_Button16.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_tabledw_disable);
                //F2_Button17.setBackgroundResource(R.drawable.XXXXXX);
                F2_Button18.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_levelcenter_disable);
                F2_Button19.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_pwoff2);
                //F2_Button20.setBackgroundResource(R.drawable.XXXXXX);
                Change_Photo_Flag = Function_non;
                Need_Release_Flag = 0;
            }
            else if(Change_Photo_Flag == Function_unlock) {
                if(Locking_Flag == 1) {
                    F2_Button1.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_flunlockoff_normal);
                    F2_Button5.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_fllockon_normal);
                }
                else
                {

                    F2_Button1.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_flunlockon_normal);
                    F2_Button5.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_fllockoff_normal);
                }

                if(Reverse_Flag == 1){
                    F2_Button3.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_flex_disable);
                    F2_Button7.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_reflex_disable);
                    F2_Button11.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_backup_disable);
                    F2_Button15.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_backdw_disable);
                }
                else{

                    F2_Button7.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_reflex_normal);
                    F2_Button11.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_backup_normal);
                    F2_Button15.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_backdw_normal);
                    F2_Button3.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_flex_normal);
                }
                F2_Button2.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_normal_normal);
                F2_Button4.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_slidefoot_normal);
                F2_Button6.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_reverse_normal);
                F2_Button8.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_slidehead_normal);
                F2_Button9.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_tiltright_normal);
                F2_Button10.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_revtrend_normal);
                F2_Button12.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_tableup_normal);
                F2_Button13.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_tiltleft_normal);
                F2_Button14.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_trend_normal);
                F2_Button16.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_tabledw_normal);
                //F2_Button17.setBackgroundResource(R.drawable.XXXXXX);
                F2_Button18.setBackgroundResource(R.drawable.bisor_gui_eq_content_button_levelcenter_normal);
                F2_Button19.setBackgroundResource(R.drawable.bisor_gui_final_for_release_smaller);
                //F2_Button20.setBackgroundResource(R.drawable.XXXXXX);
                Change_Photo_Flag = Function_non;
                Need_Release_Flag =0;
            }


            if(mDelayTime > 0){
                handler.postDelayed(this,mDelayTime);
            }
        }
    };




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_fragment2,container,false);

        FrameLayout f2FL;
        f2FL = view.findViewById(R.id.f2FrameLayout);
        f2FL.setBackgroundColor(Color.parseColor("#F1F1F1"));


        mIV_Reverse = view.findViewById(R.id.imageView_Reverse);
        mIV_Normal  = view.findViewById(R.id.imageView_Normal);
        mIV_Status1 = view.findViewById(R.id.imageView_Status1);
        mIV_Status2 = view.findViewById(R.id.imageView_Status2);

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
//        if (hidden) {
//            //Fragment隐藏时调用
//        } else {
//            //Fragment显示时调用
//        }
    }


}
