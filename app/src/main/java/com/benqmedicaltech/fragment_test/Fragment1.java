package com.benqmedicaltech.fragment_test;


import android.content.Context;
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

//    private TextView tv;
//    private String name;
//    private Context mContext;

//    public Fragment1(String fName) {
//        // Required empty public constructor
//        super();
//        this.name = fName;
//    }

//    public Fragment1() {
//        mContext = getActivity();
//    }

    public static Fragment1 newInstance(String name) {
        Fragment1 newFragment = new Fragment1();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        newFragment.setArguments(bundle);
        return newFragment;
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_fragment1, container, false);
//    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Bundle args = getArguments();
//        if (args != null) {
//            name = args.getString("name");
//        }
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_fragment1,container,false);
//        tv = (TextView) view.findViewById(R.id.fragment1_text);
//        tv.setText(name);
//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tv.setText("我变了-" + name);
//            }
//        });

        FrameLayout f1FL;
        f1FL = view.findViewById(R.id.f1FrameLayout);
        f1FL.setBackgroundColor(Color.parseColor("#C4C4C4"));

        ((MainActivity)this.getActivity()).Get_Fragment1(view);
        return view;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
//        if (hidden){
//            //Fragment隐藏时调用
//        }else {
//            //Fragment显示时调用
//        }

    }

}
