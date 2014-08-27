package com.chiemy.pulltoexpand;

import com.nineoldandroids.view.ViewPropertyAnimator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class PullSeparateListView extends ListView{
	private static final float MAX_DELTAY = 200;
	private Context context = null;
	private boolean separate = false;
	
	private float maxDiatance = MAX_DELTAY;
	
	/**
	 * 超出边界时，滑动的起始位置
	 */
	private float startY;
	
	/**
	 * 上次滑动的位置，用于判断方向
	 */
	private float preY;

	public PullSeparateListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public PullSeparateListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public PullSeparateListView(Context context) {
		super(context);
		this.context = context;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		float currentY = ev.getY();
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			if(!separate){
				startY = currentY;
			}
			float deltaY = currentY - startY;
			if(isReachTopBound()){
				separate = true;
				//超过滑动允许的最大距离，则将起始位置向下移
				if(deltaY > MAX_DELTAY){
					startY = currentY - MAX_DELTAY;
				}else if(deltaY < 0){ //为负值时归0
					deltaY = 0;
				}
				
				if(deltaY <= MAX_DELTAY){
					for(int i = 0 ; i < getChildCount() ; i++){
						View child = getChildAt(i);
						child.setTranslationY(i*deltaY/2);
					}
					//向分离方向的反方向滑动，但位置还未复原时
					if(deltaY != 0 && currentY - preY < 0){
						return true;
					}
					//deltaY=0，说明位置已经复原，然后交给父类处理
					if(deltaY == 0){
						separate = false;
						return super.dispatchTouchEvent(ev);
					}
				}
				return false;
			}
			if(isReachBottomBound()){
				separate = true;
				//超过滑动允许的最大距离，则将起始位置向上移
				if(Math.abs(deltaY) > MAX_DELTAY){
					startY = currentY + MAX_DELTAY;
				}else if(deltaY > 0){
					deltaY = 0;
				}
				if(Math.abs(deltaY) <= MAX_DELTAY){
					for(int i = 0 ; i < getChildCount() ; i++){
						View child = getChildAt(getChildCount() - i - 1);
						child.setTranslationY(i*deltaY/2);
					}
					//向分离方向的反方向滑动，但位置还未复原时
					if(deltaY != 0 && currentY - preY > 0){
						return true;
					}
					//deltaY=0，说明位置已经复原，然后交给父类处理
					if(deltaY == 0){
						separate = false;
						return super.dispatchTouchEvent(ev);
					}
				}
				return false;
			}
			preY = currentY;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(separate){
				separate = false;
				for(int i = 0 ; i < getChildCount() ; i++){
					View child = getChildAt(i);
					ViewPropertyAnimator.animate(child).translationY(0).setDuration(300).setInterpolator(new AccelerateInterpolator());
				}
			}
			preY = 0;
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	
	/**
	 * 是否到达顶部
	 * @return
	 */
	private boolean isReachTopBound() {
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
	
	/**
	 * 是否到达底部
	 * @return
	 */
	private boolean isReachBottomBound(){
		int lastVisPos = getLastVisiblePosition();
		if(lastVisPos == getCount() - 1){
			View lastView = getChildAt(getChildCount() - 1);
			if(lastView != null && lastView.getBottom() <= getBottom() && getCount() > getChildCount()){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
}
