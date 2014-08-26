package com.chiemy.pulltoexpand;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class PullExpandListView extends ListView{
	public PullExpandListView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public PullExpandListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PullExpandListView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
	}
	
	float mDownY;
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			mDownY = ev.getRawY();
			break;
		case MotionEvent.ACTION_UP:
			mDownY = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			float deltaY = ev.getRawY() - mDownY;
			int count = getChildCount();
			int total = getCount();
			int firstVisibleposition = getFirstVisiblePosition();
			if(firstVisibleposition == 0){
				//System.out.println(">>>顶部");
			}else if(firstVisibleposition + count == total){
				//System.out.println(">>>底部");
			}
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		System.out.println(">>>deltaY:" + deltaY);
		System.out.println(">>>>scrollY:" + scrollY + ",scrollRangeY:" + scrollRangeY);
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
				scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
	}
	
	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
			boolean clampedY) {
		//System.out.println(">>>onOverScrolled:" + scrollX + ":" + scrollY + ":" + clampedX + ":" + clampedY);
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
	}
	
}
