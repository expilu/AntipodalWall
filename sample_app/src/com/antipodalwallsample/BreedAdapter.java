package com.antipodalwallsample;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BreedAdapter extends ArrayAdapter<Breed> {
	
	private final int mViewResourceId;

	public BreedAdapter(Context context, int viewResourceId, List<Breed> breeds) {
		super(context, viewResourceId, breeds);
		mViewResourceId = viewResourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layoutView;
		
		if (convertView == null) {
			layoutView = new LinearLayout(getContext());
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(mViewResourceId, layoutView);
		} else {
			layoutView = (LinearLayout) convertView;
		}
		
		Breed breed = getItem(position);
		
		((ImageView)layoutView.findViewById(R.id.image)).setImageResource(breed.getImgId());
		((TextView)layoutView.findViewById(R.id.text)).setText(breed.getName());
		
		return layoutView;
	}
}
