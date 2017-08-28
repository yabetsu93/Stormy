package com.jabespauya.stormy;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class DailyForecastActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

        String[] DaysOfWeeks = {"Monday", "Tuesday","Wednesday", "Thursday", "Friday", "Saturday", "Sunday","Freetime"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, DaysOfWeeks);
        setListAdapter(adapter);
    }
}
