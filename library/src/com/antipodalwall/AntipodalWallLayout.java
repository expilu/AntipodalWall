package com.antipodalwall;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


public class AntipodalWallLayout extends ViewGroup {
	
	private int columns;
	private float columnWidth = 0;
	private int paddingL;
	private int paddingT;
	private int paddingR;
	private int paddingB;
	int parentHeight = 0;
	private int finalHeight = 0;
	private int y_move = 0;
	private int horizontalSpacing;
	private int verticalSpacing;

	public AntipodalWallLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setWillNotDraw(false);
		
		//Load the attrs from the XML
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AntipodalWallAttrs);
		//- scrollbars
		initializeScrollbars(a);
		//- number of columns
		this.columns = a.getInt(R.styleable.AntipodalWallAttrs_android_columnCount, 1);
        if(this.columns < 1)
        	this.columns = 1;
        //- general padding (padding was not being handled correctly)
        setGeneralPadding(a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_padding, 0));
        //- specific paddings
        this.paddingL = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_paddingLeft, 0);
        this.paddingT = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_paddingTop, 0);
        this.paddingR = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_paddingRight, 0);
        this.paddingB = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_paddingBottom, 0);
        //- spacing
        this.horizontalSpacing = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_horizontalSpacing, 0);
        this.verticalSpacing = a.getDimensionPixelSize(R.styleable.AntipodalWallAttrs_android_verticalSpacing, 0);
        
        a.recycle();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentUsableWidth = parentWidth - this.paddingL - this.paddingR; //Usable width for children once padding is removed
		if(parentUsableWidth < 0)
			parentUsableWidth = 0;
		this.parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		int parentUsableHeight = this.parentHeight - this.paddingT - this.paddingB; //Usable height for children once padding is removed
		if(parentUsableHeight < 0)
			parentUsableHeight = 0;
		this.columnWidth = parentUsableWidth / this.columns - ((this.horizontalSpacing * (this.columns - 1)) / this.columns);
		
		for(int i=0;i<getChildCount();i++) {
			View child = getChildAt(i);
			 //force the width of the children to be that of the columns...
			int childWidthSpec = MeasureSpec.makeMeasureSpec((int)this.columnWidth, MeasureSpec.EXACTLY);
			 //... but let them grow vertically
			int childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			child.measure(childWidthSpec, childHeightSpec);
		}
		
		//get the final heigth of the viewgroup. it will be that of the higher
		//column once all chidren is in place
		int[] columns_t = new int[this.columns];
		for(int i=0;i<getChildCount();i++) {
			int column = findLowerColumn(columns_t);
			columns_t[column] += getChildAt(i).getMeasuredHeight();
		}
		this.finalHeight = columns_t[findHigherColumn(columns_t)];
		
		setMeasuredDimension(parentWidth, this.finalHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int[] columns_t = new int[this.columns];
		
		for(int i=0;i<getChildCount();i++) {
			View view = getChildAt(i);
			//We place each child  in the column that has the less height to the moment
			int column = findLowerColumn(columns_t);
			int left = this.paddingL + l + (int)(this.columnWidth * column) + (this.horizontalSpacing * column);
			view.layout(left, columns_t[column] + this.paddingT, left + view.getMeasuredWidth(), columns_t[column] + view.getMeasuredHeight() + this.paddingT);
			columns_t[column] = columns_t[column] + view.getMeasuredHeight() + this.verticalSpacing;
		}
	}
	
	@Override
	protected int computeVerticalScrollExtent() {
	    return this.parentHeight - (this.finalHeight - this.parentHeight);
	}

	@Override
	protected int computeVerticalScrollOffset() {
	    return getScrollY();
	}

	@Override
	protected int computeVerticalScrollRange() {
	    return this.finalHeight;
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
					this.y_move = - (int)(event.getY() - event.getHistoricalY(event.getHistorySize() - 1));
					int result_scroll = getScrollY() + this.y_move;
					if(result_scroll >= 0 && result_scroll <= this.finalHeight - this.parentHeight)
						scrollBy(0, this.y_move);
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
		this.paddingL = padding;
		this.paddingT = padding;
		this.paddingR = padding;
		this.paddingB = padding;
	}
}
