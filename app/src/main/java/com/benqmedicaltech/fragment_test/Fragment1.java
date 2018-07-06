package com.benqmedicaltech.fragment_test;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment1 extends Fragment {

    private TextView tv;
    private String name;

    public Fragment1(String fName) {
        // Required empty public constructor
            this.name = fName;
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_fragment1, container, false);
//    }


    private FrameLayout f1FL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_fragment1,container,false);
//        tv = (TextView) view.findViewById(R.id.fragment1_text);
//        tv.setText(name);
//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tv.setText("我变了-" + name);
//            }
//        });

        f1FL = (FrameLayout) view.findViewById(R.id.f1FrameLayout);
        f1FL.setBackgroundColor(Color.parseColor("#C4C4C4"));

        ((MainActivity)this.getActivity()).Get_Fragment1(view);
        return view;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            //Fragment隐藏时调用
        }else {
            //Fragment显示时调用
        }

    }

}
