package com.benqmedicaltech.fragment_test;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment3 extends Fragment {

    private TextView tv;
    private String name;

//    public Fragment3() {
//        // Required empty public constructor
//    }
    public Fragment3(String fName) {
        // Required empty public constructor
        this.name = fName;
    }


    public String MyString = "Loading...";
    private Handler handler = new Handler();
    int mDelayTime = 1000;
    public TextView myTextView;
    int LED_Check = 0;
    byte[] Table_Power_Status = new byte[3];
    byte[] Output_Table = new byte[1024];

    public void Show_text(byte[] Output_Temp, TextView mtv) {

        myTextView = mtv;
        Output_Table = Output_Temp;

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
            }
            else
            {
                MyString = MyString + "_Normal";
            }
        }





    }

    private Runnable runnable = new Runnable() {
        public void run() {
            //update
            check_Data();
            if(myTextView != null)myTextView.setText(MyString);
            handler.postDelayed(this,mDelayTime);
        }
    };




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_fragment3,container,false);
//        tv = (TextView) view.findViewById(R.id.fragment3_text);
//        tv.setText(name);
//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tv.setText("我变了-" + name);
//            }
//        });
        handler.postDelayed(this.runnable,mDelayTime);
        ((MainActivity)this.getActivity()).Get_Fragment3(view);
        return view;
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
