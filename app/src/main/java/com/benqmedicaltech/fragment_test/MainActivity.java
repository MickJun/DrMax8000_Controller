package com.benqmedicaltech.fragment_test;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


//    private FragmentManager manager;
//    private FragmentTransaction transaction;

    //三个fragment
    private Fragment1 f1;
    private Fragment2 f2;
    private Fragment3 f3;

    //底部三个按钮
    private Button foot1;
    private Button foot2;
    private Button foot3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //透過下方程式碼，取得Activity中執行的個體。
        //manager = getSupportFragmentManager();

        foot1 = (Button) findViewById(R.id.Button1);
        foot2 = (Button) findViewById(R.id.Button2);
        foot3 = (Button) findViewById(R.id.Button3);
        foot1.setOnClickListener(this);
        foot2.setOnClickListener(this);
        foot3.setOnClickListener(this);

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
        TextView textView1 = (TextView) findViewById(R.id.fragment1_text);
        TextView textView2 = (TextView) findViewById(R.id.fragment2_text);
        TextView textView3 = (TextView) findViewById(R.id.fragment3_text);
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
                textView2.setText("fragment2_button1");
                break;
            case R.id.fragment1_button2:
                textView1.setText("fragment2_button2");
                break;
            case R.id.fragment2_button1:
                textView2.setText("fragment2_button1");
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

}
