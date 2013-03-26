package com.antipodalwall;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Adapter;
import android.widget.AdapterView;

public class AntipodalWallLayout extends AdapterView<Adapter> {
	
	//================================================================================
	// Definitions
    //================================================================================
	
	/**
	 * The number of columns to distribute views as stablished with the
	 * android:columnCount property in XML
	 */
	private int mColumns;
	/**
	 * The width of a column after calculating and dividing the usable width
	 * between columns
	 */
	private float mColumnWidth = 0;	
	/** The total visible height assigned to the layout */
	int mLayoutHeight = 0;
	/**
	 * Horizontal space between chldren as stablished with
	 * android:horizontalSpacing in the XML layout
	 */
	private int mHorizontalSpacing;
	/**
	 * Horizontal space between chldren as stablished with
	 * android:verticalSpacing in the XML layout
	 */
	private int mVerticalSpacing;
	/** The Adapter to use */
	private Adapter mAdapter;
	/** Layout params for children views*/
	private LayoutParams mChildLayoutParams;
	private int[] mColumnsHeights;
	private int[] mAssignedColumns;
	private int[] mItemsTops;
	
	private int mFirstTouchY = 0;
	private int mScrollChange = 0;
	private int mScrollOffset = 0;
	
	//================================================================================
	// Constructor
	//================================================================================
	
	public AntipodalWallLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Load the attrs from the stablished in the XML layout, if any
		final TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.AntipodalWallAttrs);
		// - scrollbars
		initializeScrollbars(a);
		// - number of columns
		mColumns = a.getInt(R.styleable.AntipodalWallAttrs_android_columnCount,
				1);
		if (mColumns < 1)
			mColumns = 1; // the default is one column
		// - spacing
		mHorizontalSpacing = a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_horizontalSpacing, 0);
		mVerticalSpacing = a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_verticalSpacing, 0);
		a.recycle();		
		
		mChildLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		mColumnsHeights = new int[mColumns];
	}
	
	//================================================================================
	// API Events
    //================================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Total layout width
		int layoutWidth = MeasureSpec.getSize(widthMeasureSpec);

		// Total layout height
		mLayoutHeight = MeasureSpec.getSize(heightMeasureSpec);

		// Calculate width assigned to each column: the usable width divided by
		// the number of columns, minus horizontal spacing
		mColumnWidth = layoutWidth / mColumns - (((mColumns + 1) * mHorizontalSpacing) / mColumns);

		setMeasuredDimension(layoutWidth, mLayoutHeight);
	}
	
	private int mLastPosition = -1;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		
		if (mAdapter == null)
	        return;
		
		while(!checkAllColumsHigherThan(mColumnsHeights, mLayoutHeight)) {
			int position = mLastPosition + 1;
			View newChild = mAdapter.getView(mLastPosition + 1, null, null);
			addViewInLayout(newChild, -1, mChildLayoutParams);
			int childWidthSpec = MeasureSpec.makeMeasureSpec((int) mColumnWidth, MeasureSpec.EXACTLY);
			int childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			newChild.measure(childWidthSpec, childHeightSpec);
			int column = findColumn(mColumnsHeights, ColumnSpec.LOWEST);
			mItemsTops[position] = mColumnsHeights[column];
			mColumnsHeights[column] += newChild.getMeasuredHeight() + mVerticalSpacing;
			mAssignedColumns[position] = column;			
			mLastPosition = position;
		}
		
		for(int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			int position = mLastPosition + (i - getChildCount()) + 1;
			int column = mAssignedColumns[position];
			int left = l + mHorizontalSpacing + (int) (mColumnWidth * column) + (mHorizontalSpacing * column);
			int top = mItemsTops[position] + mVerticalSpacing - mScrollOffset;
			int right = left + child.getMeasuredWidth();
			int bottom = top + child.getMeasuredHeight();
			child.layout(left, top, right, bottom);
		}
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (getChildCount() == 0) {
	        return false;
	    }
	    switch (event.getAction() & MotionEvent.ACTION_MASK) {
	    	case MotionEvent.ACTION_DOWN:
	    		mFirstTouchY = (int)event.getY();
	    		break;
	        case MotionEvent.ACTION_MOVE:
	        	if (isVerticalScrollBarEnabled()) {
	        		mScrollChange = mFirstTouchY - (int)event.getY();
	        		if (Math.abs(mScrollChange) >= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
	        			mFirstTouchY -= mScrollChange;
	        			mScrollOffset += mScrollChange;
	        			if(mScrollOffset < 0) // Scrolling top limit
	        				mScrollOffset = 0;
	        			Log.i("MOVEMENT", String.valueOf(mScrollChange));
						requestLayout();
	        		}
				}
				break;
	        default:
	            break;
	    }
	    return true;
	}
	
	//================================================================================
	// Private functions
    //================================================================================

	/**
	 * Finds a column according to the specification passed (right now only
	 * HIGHEST or LOWEST)
	 * 
	 * @param columns
	 *            the columns array
	 * @param which
	 *            which column specification to find
	 * @return the found column index
	 */
	private int findColumn(int[] columns, int which) {
		int comp = columns[0];
		int column = 0;
		for (int i = 1; i < columns.length; i++) {
			boolean replace = false;

			switch (which) {
			case ColumnSpec.LOWEST:
				replace = columns[i] < comp;
				break;
			case ColumnSpec.HIGHEST:
				replace = columns[i] > comp;
				break;
			}

			if (replace) {
				comp = columns[i];
				column = i;
			}
		}
		return column;
	}
	
	private boolean checkAllColumsHigherThan(int[] columns, int height) {
		for (int i = 0; i < columns.length; i++) 
			if(columns[i] < height)
				return false;
		return true;
	}
	
	//================================================================================
	// Getters & Setters
    //================================================================================
	
	/* (non-Javadoc)
	 * @see android.widget.AdapterView#getAdapter()
	 */
	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}	

	/* (non-Javadoc)
	 * @see android.widget.AdapterView#setAdapter(android.widget.Adapter)
	 */
	@Override
	public void setAdapter(Adapter adapter) {
		mAdapter = adapter;
		mAssignedColumns = new int[mAdapter.getCount()];
		mItemsTops = new int[mAdapter.getCount()];
		removeAllViewsInLayout();
		requestLayout();
	}
	
	/* (non-Javadoc)
	 * @see android.widget.AdapterView#getSelectedView()
	 */
	@Override
	public View getSelectedView() {
		throw new UnsupportedOperationException("Not supported");

	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView#setSelection(int)
	 */
	@Override
	public void setSelection(int position) {
		throw new UnsupportedOperationException("Not supported");

	}
}
