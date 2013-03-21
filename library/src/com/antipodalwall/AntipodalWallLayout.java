package com.antipodalwall;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class AntipodalWallLayout extends ViewGroup {
	
	private int mColumns;
	private float mColumnWidth = 0;
	private int mPaddingL;
	private int mPaddingT;
	private int mPaddingR;
	private int mPaddingB;
	int mParentHeight = 0;
	private int mFinalHeight = 0;
	private int mYMove = 0;
	private int mHorizontalSpacing;
	private int mVerticalSpacing;

	public AntipodalWallLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setWillNotDraw(false);
		
		//Load the attrs from the XML
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AntipodalWallAttrs);
		//- scrollbars
		initializeScrollbars(a);
		//- number of columns
		mColumns = a.getInt(R.styleable.AntipodalWallAttrs_android_columnCount, 1);
        if(mColumns < 1)
        	mColumns = 1;
        //- general padding (padding was not being handled correctly)
        setGeneralPadding(a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_padding, 0));
        //- specific paddings
        mPaddingL = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_paddingLeft, 0);
        mPaddingT = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_paddingTop, 0);
        mPaddingR = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_paddingRight, 0);
        mPaddingB = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_paddingBottom, 0);
        //- spacing
        mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_horizontalSpacing, 0);
        mVerticalSpacing = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_verticalSpacing, 0);
        
        a.recycle();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentUsableWidth = parentWidth - mPaddingL - mPaddingR; //Usable width for children once padding is removed
		if(parentUsableWidth < 0)
			parentUsableWidth = 0;
		mParentHeight = MeasureSpec.getSize(heightMeasureSpec);
		int parentUsableHeight = mParentHeight - mPaddingT - mPaddingB; //Usable height for children once padding is removed
		if(parentUsableHeight < 0)
			parentUsableHeight = 0;
		mColumnWidth = parentUsableWidth / mColumns - ((mHorizontalSpacing * (mColumns - 1)) / mColumns);
		
		for(int i=0;i<getChildCount();i++) {
			View child = getChildAt(i);
			 //force the width of the children to be that of the columns...
			int childWidthSpec = MeasureSpec.makeMeasureSpec((int)mColumnWidth, MeasureSpec.EXACTLY);
			 //... but let them grow vertically
			int childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			child.measure(childWidthSpec, childHeightSpec);
		}
		
		//get the final heigth of the viewgroup. it will be that of the higher
		//column once all chidren is in place
		int[] columns_t = new int[mColumns];
		for(int i=0;i<getChildCount();i++) {
			int column = findLowerColumn(columns_t);
			columns_t[column] += getChildAt(i).getMeasuredHeight();
		}
		mFinalHeight = columns_t[findHigherColumn(columns_t)];
		
		setMeasuredDimension(parentWidth, mFinalHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int[] columns_t = new int[mColumns];
		
		for(int i=0;i<getChildCount();i++) {
			View view = getChildAt(i);
			//We place each child  in the column that has the less height to the moment
			int column = findLowerColumn(columns_t);
			int left = mPaddingL + l + (int)(mColumnWidth * column) + (mHorizontalSpacing * column);
			view.layout(left, columns_t[column] + mPaddingT, left + view.getMeasuredWidth(), columns_t[column] + view.getMeasuredHeight() + mPaddingT);
			columns_t[column] = columns_t[column] + view.getMeasuredHeight() + mVerticalSpacing;
		}
	}
	
	@Override
	protected int computeVerticalScrollExtent() {
	    return mParentHeight - (mFinalHeight - mParentHeight);
	}

	@Override
	protected int computeVerticalScrollOffset() {
	    return getScrollY();
	}

	@Override
	protected int computeVerticalScrollRange() {
	    return mFinalHeight;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    awakenScrollBars();
	    int eventaction = event.getAction();
	    switch (eventaction) {
		case MotionEvent.ACTION_MOVE:
			//handle vertical scrolling
			if(isVerticalScrollBarEnabled()) {
				if(event.getHistorySize() > 0) {
					mYMove = - (int)(event.getY() - event.getHistoricalY(event.getHistorySize() - 1));
					int result_scroll = getScrollY() + mYMove;
					if(result_scroll >= 0 && result_scroll <= mFinalHeight - mParentHeight)
						scrollBy(0, mYMove);
		    	}
			}
			break;
		}
	    invalidate();
	    return true;
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
	
	private int findHigherColumn(int[] columns) {
		int maxValue = columns[0];
		int column = 0;
		for(int i=1;i<columns.length;i++){  
			if(columns[i] > maxValue){  
				maxValue = columns[i];
				column = i;
			}  
		}  
		return column;
	}
	
	private void setGeneralPadding(int padding) {
		mPaddingL = padding;
		mPaddingT = padding;
		mPaddingR = padding;
		mPaddingB = padding;
	}
}
