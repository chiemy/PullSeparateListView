package com.chiemy.pulltoexpand;

import com.nineoldandroids.view.ViewPropertyAnimator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ListView;

/**
 * 具有弹性效果的ListView。主要是实现父类dispatchTouchEvent方法和OnGestureListener中onScroll方法。
 * 
 * @author E
 */
public class FlexibleListView extends ListView {
	private static final int MAX_DELTAY = 200;
	private Context context = null;
	private boolean outTopBound = false;

	public FlexibleListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public FlexibleListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public FlexibleListView(Context context) {
		super(context);
		this.context = context;
	}
	
	private float startY;
	private float deltaY;
	
	private float preY;
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			float currentY = ev.getY();
			if(isOutTopBound()){
				if(startY <= 0){
					startY = currentY;
				}
				deltaY = currentY - startY;
				deltaY = Math.min(deltaY, MAX_DELTAY);
				if(deltaY >= 0){
					outTopBound = true;
					for(int i = 1 ; i < getChildCount() ; i++){
						View child = getChildAt(i);
						child.setTranslationY(i*deltaY/2);
					}
					if(currentY - preY < 0){
						return true;
					}
				}
				preY = currentY;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(outTopBound){
				for(int i = 1 ; i < getChildCount() ; i++){
					View child = getChildAt(i);
					ViewPropertyAnimator.animate(child).translationY(0).setDuration(300).setInterpolator(new AccelerateInterpolator());
				}
			}
			outTopBound = false;
			startY = 0;
			preY = 0;
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	private boolean isOutTopBound() {
		int firstVisPos = getFirstVisiblePosition();
		if(firstVisPos == 0){
			View firstView = getChildAt(firstVisPos);
			if(firstView != null && firstView.getTop() >= 0){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	
}