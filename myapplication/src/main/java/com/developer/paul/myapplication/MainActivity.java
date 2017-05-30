package com.developer.paul.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.developer.paul.recycleviewgroup.RecycleViewGroup;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecycleViewGroup recycleViewGroup = (RecycleViewGroup) findViewById(R.id.recycleviewgroup);
//        recycleViewGroup.setDisableCellScroll(true);
        recycleViewGroup.setScrollInterface(new RecycleViewGroup.ScrollInterface() {
            @Override
            public void getMovePercent(float v, int direction) {
                Log.i("new", "getMovePercent: " + v);
            }
        });
        recycleViewGroup.setOnScrollListener(new RecycleViewGroup.OnScroll() {
            @Override
            public void onPageSelected(View v) {

            }

            @Override
            public void onHorizontalScroll(int dx, int preOffsetX) {
                Log.i("adsasda", "onHorizontalScroll: " + dx + " , " + preOffsetX);
            }

            @Override
            public void onVerticalScroll(int dy, int preOffsetY) {

            }
        });

//        recycleViewGroup.setOnSetting(new RecycleViewGroup.OnSetting() {
//            @Override
//            public int getItemHeight(int heightSpec) {
//                return 200;
//            }
//        });
//        recycleViewGroup.setDisableScroll(true);
    }
}
