package com.antipodalwallsample;

import com.antipodalwall.AntipodalWallLayout;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class AntipodalWallSample extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antipodal_wall_sample);
        
        AntipodalWallLayout layout = (AntipodalWallLayout)findViewById(R.id.antipodal_wall);
        TextView tv = new TextView(this);
        tv.setText("This one has been added from code");
        tv.setBackgroundColor(getResources().getColor(android.R.color.black));
        layout.addView(tv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_antipodal_wall_sample, menu);
        return true;
    }

    
}
