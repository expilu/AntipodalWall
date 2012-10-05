package com.antipodalwall;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.R.anim;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AnalogClock;
import android.widget.LinearLayout;


public class AntipodalWall extends ViewGroup {
	
	private int columns;

	public AntipodalWall(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//Load attrs
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AntipodalWallAttrs);
		columns = a.getInt(R.styleable.AntipodalWallAttrs_columns, 1);
        if(columns < 1)
        	columns = 1;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		
		for(int i=0;i<getChildCount();i++) {
			View child = getChildAt(i);
			int childWidthSpec = MeasureSpec.makeMeasureSpec(parentWidth / columns, MeasureSpec.EXACTLY);
			int childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			child.measure(childWidthSpec, childHeightSpec);
		}
		
		setMeasuredDimension(parentWidth, parentHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int x = 0;
		for(int i=0;i<getChildCount();i++) {
			View view = getChildAt(i);
			view.layout(l, t, l + view.getMeasuredWidth(), t + view.getMeasuredHeight());
			t = t + view.getMeasuredHeight();
		}
	}
}
