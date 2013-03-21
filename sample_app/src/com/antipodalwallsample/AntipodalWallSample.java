package com.antipodalwallsample;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.antipodalwall.AntipodalWallLayout;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class AntipodalWallSample extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_antipodal_wall_sample);

		AntipodalWallLayout antipodalWall = (AntipodalWallLayout) findViewById(R.id.antipodal_wall);
		List<String> txts = new ArrayList<String>();
		SecureRandom random = new SecureRandom();
		for (int i = 0; i < 30; i++)
			txts.add("View " + String.valueOf(i + 1) + " " + (new BigInteger(random.nextInt(1000), random)).toString(32));
		antipodalWall.setAdapter(new ArrayAdapter<String>(this, R.layout.brick, R.id.text, txts));
	}
}
