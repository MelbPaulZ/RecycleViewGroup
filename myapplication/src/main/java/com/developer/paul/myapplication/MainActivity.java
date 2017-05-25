package com.developer.paul.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.developer.paul.recycleviewgroup.RecycleViewGroup;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecycleViewGroup recycleViewGroup = (RecycleViewGroup) findViewById(R.id.recycleviewgroup);
        recycleViewGroup.setDisableCellScroll(true);
        recycleViewGroup.setScrollInterface(new RecycleViewGroup.ScrollInterface() {
            @Override
            public void getMovePercent(float v) {
                Log.i("new", "getMovePercent: " + v);
            }
        });
    }
}
