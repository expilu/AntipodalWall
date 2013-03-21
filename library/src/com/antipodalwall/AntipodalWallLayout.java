package com.antipodalwall;

import android.content.Context;
import android.content.res.TypedArray;
import android.provider.SyncStateContract.Columns;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class AntipodalWallLayout extends ViewGroup {

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
	/** Left padding as stablished with android:paddingLeft in the XML layout */
	private int mPaddingL;
	/** Top padding as stablished with android:paddingTop in the XML layout */
	private int mPaddingT;
	/** Right padding as stablished with android:paddingRight in the XML layout */
	private int mPaddingR;
	/**
	 * Bottom padding as stablished with android:paddingBottom in the XML layout
	 */
	@SuppressWarnings("unused")
	// TODO Not used right now, but may be useful in the future
	private int mPaddingB;
	/** The total visible height assigned to the layout */
	int mLayoutHeight = 0;
	/**
	 * The final total height of the layout, visible or not, after adding
	 * children. All content scrolled or not.
	 */
	private int mFinalHeight = 0;
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

	public AntipodalWallLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		setWillNotDraw(false);

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
		// - general padding (padding was not being handled correctly)
		setGeneralPadding(a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_padding, 0));
		// - specific paddings
		mPaddingL = a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_paddingLeft, 0);
		mPaddingT = a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_paddingTop, 0);
		mPaddingR = a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_paddingRight, 0);
		mPaddingB = a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_paddingBottom, 0);
		// - spacing
		mHorizontalSpacing = a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_horizontalSpacing, 0);
		mVerticalSpacing = a.getDimensionPixelSize(
				R.styleable.AntipodalWallAttrs_android_verticalSpacing, 0);

		awakenScrollBars(); // TODO Scrollbars should be shown only if enabled
							// in XML attributes
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Total layout width
		int layoutWidth = MeasureSpec.getSize(widthMeasureSpec);
		// Usable layout width for children once padding is removed
		int layoutUsableWidth = layoutWidth - mPaddingL - mPaddingR;
		if (layoutUsableWidth < 0)
			layoutUsableWidth = 0;

		// Total layout height
		mLayoutHeight = MeasureSpec.getSize(heightMeasureSpec);

		// Calculate width assigned to each column: the usable width divided by
		// the number of columns, minus horizontal spacing
		mColumnWidth = layoutUsableWidth / mColumns
				- ((mHorizontalSpacing * (mColumns - 1)) / mColumns);

		// Measure each children
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			// force the width of the children to be the width previously
			// calculated for columns...
			int childWidthSpec = MeasureSpec.makeMeasureSpec(
					(int) mColumnWidth, MeasureSpec.EXACTLY);
			// ... but let them grow vertically
			int childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
			child.measure(childWidthSpec, childHeightSpec);
		}

		// Get the final total height of the layout. It will be that of the
		// higher column once all chidren are in place. Every child is added to
		// the sortest column at the moment of addition
		int[] columnsHeights = new int[mColumns];
		for (int i = 0; i < getChildCount(); i++) {
			int column = findColumn(columnsHeights, ColumnSpec.LOWEST);
			columnsHeights[column] += getChildAt(i).getMeasuredHeight();
		}
		mFinalHeight = columnsHeights[findColumn(columnsHeights,
				ColumnSpec.HIGHEST)];

		setMeasuredDimension(layoutWidth, mFinalHeight);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int[] columnsHeights = new int[mColumns];

		// We place each child in the column that has the sortest height at the
		// moment
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			int column = findColumn(columnsHeights, ColumnSpec.LOWEST);
			int left = mPaddingL + l + (int) (mColumnWidth * column)
					+ (mHorizontalSpacing * column);
			view.layout(left, columnsHeights[column] + mPaddingT,
					left + view.getMeasuredWidth(), columnsHeights[column]
							+ view.getMeasuredHeight() + mPaddingT);
			columnsHeights[column] = columnsHeights[column]
					+ view.getMeasuredHeight() + mVerticalSpacing;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#computeVerticalScrollExtent()
	 */
	@Override
	protected int computeVerticalScrollExtent() {
		return mLayoutHeight - (mFinalHeight - mLayoutHeight);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#computeVerticalScrollOffset()
	 */
	@Override
	protected int computeVerticalScrollOffset() {
		return getScrollY();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#computeVerticalScrollRange()
	 */
	@Override
	protected int computeVerticalScrollRange() {
		return mFinalHeight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			// Handle vertical scrolling
			// TODO only do this if scrolling is enabled in XML
			if (isVerticalScrollBarEnabled()) {
				if (event.getHistorySize() > 0) {
					int yMove = -(int) (event.getY() - event
							.getHistoricalY(event.getHistorySize() - 1));
					int result_scroll = getScrollY() + yMove;
					if (result_scroll >= 0
							&& result_scroll <= mFinalHeight - mLayoutHeight)
						scrollBy(0, yMove);
				}
			}
			break;
		}

		return true;
	}

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

	private void setGeneralPadding(int padding) {
		mPaddingL = padding;
		mPaddingT = padding;
		mPaddingR = padding;
		mPaddingB = padding;
	}
}
