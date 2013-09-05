package com.antipodalwall;

import java.util.ArrayList;
import java.util.List;

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
	
	/** The number of columns to distribute views as established with the
	 * android:columnCount property in XML */
	private int mColumns;
	
	/** The width of a column after calculating and dividing the usable width
	 * between columns */
	private float mColumnWidth = 0;
	
	/** The total visible height assigned to the layout */
	int mLayoutHeight = 0;
	
	/** Horizontal space between children as established with
	 * android:horizontalSpacing in the XML layout */
	private int mHorizontalSpacing;
	
	/** Vertical space between children as established with
	 * android:verticalSpacing in the XML layout */
	private int mVerticalSpacing;
	
	/** The Adapter to use */
	private Adapter mAdapter;
	
	/** Layout parameters for children views*/
	private LayoutParams mChildLayoutParams;
	
	/** This is used to store the current "height" (the sum of its items heights) of each column
	 * This is useful to know to which column add the next item and other calculations */
	private int[] mColumnsHeights;
	
	private int[] mAssignedColumns;
	private int[] mItemsTops;
	
	/** Flag indicating whether all items have been added and we reached the "bottom" of the adapter */
	private boolean mBottomReached;
	
	/** Vertical position of the first touching place  at the beginning of a scroll */
	private int mFirstTouchY = 0;	
	/** How much did the scroll move... */
	private int mScrollChange = 0;
	/** Current scroll offset from the top */
	private int mScrollOffset = 0;
	
	/** Current statuses of items views */
	List<ViewStatus> mViewStatuses;
	
	List<Integer> mChildAdapterPositions;
	
	//================================================================================
	// Constructor
	//================================================================================
	
	public AntipodalWallLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setWillNotDraw(true);

		// Load the attrs from those established in the XML layout, if any
		final TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.AntipodalWallAttrs);
		// - scrollbars
		initializeScrollbars(a);
		// - number of columns
		mColumns = a.getInt(R.styleable.AntipodalWallAttrs_android_columnCount,
				1);
		if (mColumns < 1)
			mColumns = 1; // the default and minimum is one column
		// - spacing
		mHorizontalSpacing = a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_horizontalSpacing, 0);
		mVerticalSpacing = a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_verticalSpacing, 0);
		a.recycle();		
		
		mChildLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		mColumnsHeights = new int[mColumns];
		
		mBottomReached = false;
		
		mViewStatuses = new ArrayList<ViewStatus>();
		
		mChildAdapterPositions = new ArrayList<Integer>();
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
		
		checkAndSetViewStates();
		
		FreeSpacePosition freeSpacePosition = findFreeSpacePosition();
		Log.i("test", String.valueOf(freeSpacePosition));
		
		while(!mBottomReached && !checkAllColumsHigherThan(mColumnsHeights, mLayoutHeight + mScrollOffset)) {
			int position = mLastPosition + 1;
			if(position  >= mAdapter.getCount()) {
				mBottomReached = true;
				break;
			}
			Integer convertibleChildIndex = pickConvertibleChildIndex();
			View view = mAdapter.getView(position, convertibleChildIndex != null ? getChildAt(convertibleChildIndex) : null, null);
			if(convertibleChildIndex == null) {
				addViewInLayout(view, -1, mChildLayoutParams, true);
				mChildAdapterPositions.add(position);
			} else {
				mChildAdapterPositions.set(convertibleChildIndex, position);
			}
			
			int childWidthSpec = MeasureSpec.makeMeasureSpec((int) mColumnWidth, MeasureSpec.EXACTLY);
			int childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			view.measure(childWidthSpec, childHeightSpec);
			int column = findColumn(mColumnsHeights, ColumnSpec.LOWEST);
			mItemsTops[position] = mColumnsHeights[column];
			mColumnsHeights[column] += view.getMeasuredHeight() + mVerticalSpacing;
			mAssignedColumns[position] = column;			
			mLastPosition = position;
		}
		
		removeNotVisibleChildren();
		
		for(int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			int position = mChildAdapterPositions.get(i);
			int column = mAssignedColumns[position];
			int left = l + mHorizontalSpacing + (int) (mColumnWidth * column) + (mHorizontalSpacing * column);
			int top = mItemsTops[position] + mVerticalSpacing - mScrollOffset;
			int right = left + child.getMeasuredWidth();
			int bottom = top + child.getMeasuredHeight();
			child.layout(left, top, right, bottom);
		}
		
		invalidate();
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
	        	int totalHeight = mColumnsHeights[findColumn(mColumnsHeights, ColumnSpec.HIGHEST)];
	        	if(totalHeight <= mLayoutHeight)
	        		return false; // No sense scrolling if the list is smaller than the screen size
	        	if (isVerticalScrollBarEnabled()) {
	        		mScrollChange = mFirstTouchY - (int)event.getY();
	        		if (Math.abs(mScrollChange) >= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
	        			mFirstTouchY -= mScrollChange;
	        			mScrollOffset += mScrollChange;
	        			if(mScrollOffset < 0) // Scrolling top limit
	        				mScrollOffset = 0;
	        			else if(mBottomReached) {
	        				totalHeight = mColumnsHeights[findColumn(mColumnsHeights, ColumnSpec.HIGHEST)];
	        				if(mScrollOffset + mLayoutHeight > totalHeight)
	        					mScrollOffset = totalHeight - mLayoutHeight + mVerticalSpacing;
	        			}
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
	private int findColumn(int[] columns, ColumnSpec which) {
		int comp = columns[0];
		int column = 0;
		for (int i = 1; i < columns.length; i++) {
			boolean replace = false;

			switch (which) {
				case LOWEST:
					replace = columns[i] < comp;
					break;
				case HIGHEST:
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
	
	private boolean isViewVisible(View view) {
		int viewPortTop = mScrollOffset;
		int viewPortBottom = mScrollOffset + mLayoutHeight;
		int viewTop = view.getTop() + mScrollOffset - mVerticalSpacing;
		int viewBottom = view.getBottom() + mScrollOffset;
		
		return !(viewBottom < viewPortTop || viewTop > viewPortBottom);
	}
	
	private void checkAndSetViewStates() {
		mViewStatuses = new ArrayList<ViewStatus>();
		for(int i = 0; i < getChildCount(); i++) {
			if(isViewVisible(getChildAt(i)))
				mViewStatuses.add(ViewStatus.VISIBLE);
			else
				mViewStatuses.add(ViewStatus.OUT_OF_VIEW);
		}
	}
	
	private Integer pickConvertibleChildIndex() {
		for(int i = 0; i < mViewStatuses.size(); i++) {
			if(mViewStatuses.get(i) == ViewStatus.OUT_OF_VIEW) {
				mViewStatuses.set(i, ViewStatus.CONVERTED);
				return i;
			}
		}
		return null;
	}
	
	private void removeNotVisibleChildren() {
		for(int i = 0; i < mViewStatuses.size(); i++) {
			if(mViewStatuses.get(i) == ViewStatus.OUT_OF_VIEW) {
				mViewStatuses.remove(i);
				mChildAdapterPositions.remove(i);				
				removeViewInLayout(getChildAt(i));
				i--;
			}
		}
	}
	
	private FreeSpacePosition findFreeSpacePosition() {
		//TODO this is wrong
		int viewPortTop = mScrollOffset;
		int viewPortBottom = mScrollOffset + mLayoutHeight;		
		
		int belowTop = 0;
		int overBottom = 0;
		for(int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			int viewTop = view.getTop() + mScrollOffset - mVerticalSpacing;
			int viewBottom = view.getBottom() + mScrollOffset;
			if(viewTop > viewPortTop)
				belowTop++;
			if(viewBottom > viewPortBottom)
				overBottom++;
		}
		
		if(belowTop == getChildCount())
			return FreeSpacePosition.TOP;
		else if(overBottom == getChildCount())
			return FreeSpacePosition.BOTTOM;
		else
			return FreeSpacePosition.NONE;
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
		// TODO unify this in a method with the code in the constructor
		mAdapter = adapter;
		mAssignedColumns = new int[mAdapter.getCount()];
		mItemsTops = new int[mAdapter.getCount()];
		mColumnsHeights = new int[mColumns];		
		mBottomReached = false;		
		mViewStatuses = new ArrayList<ViewStatus>();		
		mChildAdapterPositions = new ArrayList<Integer>();
		
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
