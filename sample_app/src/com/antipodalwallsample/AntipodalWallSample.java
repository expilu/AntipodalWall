package com.antipodalwallsample;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import com.antipodalwall.AntipodalWallLayout;

public class AntipodalWallSample extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_antipodal_wall_sample);
		
		List<Breed> breeds = new ArrayList<Breed>();
		for(int i = 0; i < 100; i++) {
		breeds.add(new Breed("Akita Inu", R.drawable.dog_0));
		breeds.add(new Breed("Perro de presa canario", R.drawable.dog_1));
		breeds.add(new Breed("Papillon", R.drawable.dog_2));
		breeds.add(new Breed("German shepherd", R.drawable.dog_3));
		breeds.add(new Breed("American staffordshire terrier", R.drawable.dog_4));
		breeds.add(new Breed("Belgian shepherd", R.drawable.dog_5));
		breeds.add(new Breed("Blue lacy", R.drawable.dog_7));
		breeds.add(new Breed("Boykin spaniel", R.drawable.dog_8));
		breeds.add(new Breed("Bull terrier", R.drawable.dog_9));
		}

		AntipodalWallLayout antipodalWall = (AntipodalWallLayout) findViewById(R.id.antipodal_wall);		
		antipodalWall.setAdapter(new BreedAdapter(this, R.layout.breed, breeds));
	}
}
