package com.antipodalwall;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


public class AntipodalWall extends ViewGroup {
	
	private int columns;
	private float columnWidth = 0;

	public AntipodalWall(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//Load the attrs from the XML
		//- number of columns
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AntipodalWallAttrs);
		columns = a.getInt(R.styleable.AntipodalWallAttrs_columns, 1);
        if(columns < 1)
        	columns = 1;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		columnWidth = parentWidth / columns;
		
		for(int i=0;i<getChildCount();i++) {
			View child = getChildAt(i);
			 //force the width of the children to be that of the columns...
			int childWidthSpec = MeasureSpec.makeMeasureSpec((int)columnWidth, MeasureSpec.EXACTLY);
			 //... but let them grow vertically
			int childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			child.measure(childWidthSpec, childHeightSpec);
		}
		
		setMeasuredDimension(parentWidth, parentHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int[] columns_t = new int[columns];
		
		for(int i=0;i<getChildCount();i++) {
			View view = getChildAt(i);
			//We place each child  in the column that has the less height to the moment
			int column = findLowerColumn(columns_t);
			int left = l + (int)(columnWidth * column);
			view.layout(left, columns_t[column], left + view.getMeasuredWidth(), columns_t[column] + view.getMeasuredHeight());
			columns_t[column] = columns_t[column] + view.getMeasuredHeight();
		}
	}
	
	private int findLowerColumn(int[] columns) {
		int minValue = columns[0];
		int column = 0;
		for(int i=1;i<columns.length;i++){  
			if(columns[i] < minValue){  
				minValue = columns[i];
				column = i;
			}  
		}  
		return column;  
	}
}
