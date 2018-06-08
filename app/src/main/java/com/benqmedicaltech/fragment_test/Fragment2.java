package com.benqmedicaltech.fragment_test;


import android.os.Bundle;
//import android.app.Fragment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


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

    private Status_Thread mStatus_Thread = new Status_Thread();
    View MyView;
    public String MyString = "first";
    private Handler handler = new Handler();
    int mDelayTime = 1000;
    public TextView myTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_fragment2,container,false);
        MyView = view;
//        tv = (TextView) view.findViewById(R.id.fragment2_text);
//        tv.setText(name);
//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tv.setText("我变了-" + name);
//            }
//        });
        myTextView = view.findViewById(R.id.fragment1_text);
        handler.postDelayed(this.runnable,mDelayTime);
        ((MainActivity)this.getActivity()).Get_Fragment2(view);
        return view;
    }

    public void Show_text(String Temp, TextView mtv ){
        MyString = Temp;
        myTextView =mtv;
//        TextView myTextView = v.findViewById(R.id.fragment1_text);
//        myTextView.setText(MyString);
        //mStatus_Thread.start();
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

    private class Status_Thread extends Thread {
        public void run() {
            //update
        }
    }

    private Runnable runnable = new Runnable() {
        public void run() {
            //update
            if(myTextView != null)myTextView.setText(MyString);
            handler.postDelayed(this,mDelayTime);
        }
    };

}
