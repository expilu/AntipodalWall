package com.antipodalwallsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class AntipodalWallSample extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antipodal_wall_sample);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_antipodal_wall_sample, menu);
        return true;
    }

    
}
