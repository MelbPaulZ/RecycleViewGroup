package com.developer.paul.recycledviewgroup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecycledViewGroup recycledViewGroup = (RecycledViewGroup) findViewById(R.id.recycled_view_group);
        recycledViewGroup.setCalendarInterface(new RecycledViewGroup.CalendarInterface() {
            @Override
            public Calendar getFirstDay() {
                Calendar c = Calendar.getInstance();
                c.setLenient(true);
                return c;
            }
        });


    }
}
