package com.chiemy.pullseparate;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import com.chiemy.pullseparate.R;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * 当滑动到顶部后底部时，实现Item的分离效果
 * @author chiemy
 *
 */
public class PullSeparateListView extends ListView implements OnGestureListener{
	/**
	 * 最大滑动距离
	 */
	private static final float MAX_DELTAY = 200;
	
	/**
	 * 摩擦系数
	 */
	private static final float FACTOR = 0.25f;
	private static final float SCALEX = 0.98f;
	private static final float SCALEY = 0.9f;
	private int touchSlop;
	
	private boolean separate = false;
	
	/**
	 * 展开全部
	 */
	private boolean separateAll;
	/**
	 * 到达边界时，滑动的起始位置
	 */
	private float startY;
	
	/**
	 * 上次滑动的位置，用于判断方向
	 */
	private float preY;
	private float deltaY;
	private boolean reachTop,reachBottom;
	private OnScrollListener mScrollListener;
	GestureDetector detector;

	public PullSeparateListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.PullSeparateListView);
		separateAll = t.getBoolean(R.styleable.PullSeparateListView_separate_all, false);
		t.recycle();
		init();
	}

	public PullSeparateListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PullSeparateListView(Context context) {
		super(context);
		init();
	}
	
	private OnScrollListener listener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if(mScrollListener != null){
				mScrollListener.onScrollStateChanged(view, scrollState);
			}
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if(mScrollListener != null){
				mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
			if(firstVisibleItem == 0){
				View firstView = getChildAt(firstVisibleItem);
				if(firstView != null && firstView.getTop() >= 0){
					reachTop = true;
				}else{
					reachTop = false;
				}
			}else{
				reachTop = false;
			}
			if(firstVisibleItem + visibleItemCount == getCount()){
				View lastView = getChildAt(visibleItemCount - 1);
				if(lastView != null && lastView.getBottom() <= getHeight() && getCount() > getChildCount()){
					reachBottom = true;
				}else{
					reachBottom = false;
				}
			}else{
				reachBottom = false;
			}
		}
	};
	
	@SuppressWarnings("deprecation")
	private void init() {
		//不知道怎么让divider和selector和Item一起移动，所以去除，需要自己加分割线
		this.setDivider(null);
		this.setSelector(new BitmapDrawable());
		
		touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		super.setOnScrollListener(listener);
		detector = new GestureDetector(getContext(), this);
	}
	
	/**
	 * 是否全部分离
	 * @param separateAll 如果为true,那么全部都会分离。否则的话，如果是顶部下拉，只有点击位置之前的Item会分离</br>
	 * 					  如果是底部上拉，则只有点击位置之后的item会分离。默认为false
	 */
	public void setSeparateAll(boolean separateAll) {
		this.separateAll = separateAll;
	}
	
	public boolean isSeparateAll() {
		return separateAll;
	}
	
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}
	
	View downView;
	int downPosition;
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		float currentY = ev.getY();
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			float downX = ev.getX();
			float downY = ev.getY();
			downPosition = pointToPosition((int)downX, (int)downY) - getFirstVisiblePosition();
			downView = getChildAt(downPosition);
			if(downView != null){
				ViewPropertyAnimator.animate(downView)
				.scaleX(SCALEX).scaleY(SCALEY).setDuration(50)
				.setInterpolator(new AccelerateInterpolator());
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(!separate){
				startY = currentY;
			}
			deltaY = currentY - startY;
			if(reachTop){
				separate = true;
				//超过滑动允许的最大距离，则将起始位置向下移
				if(deltaY > MAX_DELTAY){
					startY = currentY - MAX_DELTAY;
				}else if(deltaY < 0){ //为负值时归0
					deltaY = 0;
					separate = false;
				}
				
				if(deltaY <= MAX_DELTAY){
					for(int i = 0 ; i < getChildCount() ; i++){
						View child = getChildAt(i);
						float distance = i*deltaY*FACTOR;
						if(!separateAll){
							if(i > downPosition){
								downPosition = Math.max(1, downPosition);
								distance = downPosition*deltaY*FACTOR;
							}
						}
						child.setTranslationY(distance);
					}
					//向分离方向的反方向滑动，但位置还未复原时
					if(deltaY != 0 && currentY - preY < 0){
						return true;
					}
					//deltaY=0，说明位置已经复原，然后交给父类处理
					if(deltaY == 0){
						return super.dispatchTouchEvent(ev);
					}
				}
				return false;
			}
			if(reachBottom){
				separate = true;
				//超过滑动允许的最大距离，则将起始位置向上移
				if(Math.abs(deltaY) > MAX_DELTAY){
					startY = currentY + MAX_DELTAY;
				}else if(deltaY > 0){
					deltaY = 0;
					separate = false;
				}
				if(Math.abs(deltaY) <= MAX_DELTAY){
					int visibleCount = getChildCount();
					for(int i = 0 ; i < visibleCount ; i++){
						View child = getChildAt(visibleCount - i - 1);
						float distance = i*deltaY*FACTOR;
						if(!separateAll){
							if((visibleCount - i - 1) < downPosition){
								if(downPosition == visibleCount - 1){
									distance = 1*deltaY*FACTOR;
								}else{
									distance = (visibleCount - downPosition - 1)*deltaY*FACTOR;
								}
							}
						}
						child.setTranslationY(distance);
					}
					//向分离方向的反方向滑动，但位置还未复原时
					if(deltaY != 0 && currentY - preY > 0){
						return true;
					}
					//deltaY=0，说明位置已经复原，然后交给父类处理
					if(deltaY == 0){
						return super.dispatchTouchEvent(ev);
					}
				}
				return false;
			}
			preY = currentY;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			preY = 0;
			if(downView != null){
				ViewPropertyAnimator.animate(downView)
				.scaleX(1f).scaleY(1f).setDuration(separate ? 300 : 100)
				.setInterpolator(new AccelerateInterpolator());
			}
			if(separate){
				separate = false;
				for(int i = 0 ; i < getChildCount() ; i++){
					View child = getChildAt(i);
					ViewPropertyAnimator.animate(child).translationY(0).setDuration(300).setInterpolator(new AccelerateInterpolator());
				}
				if(deltaY > touchSlop){
					return false;
				}
			}
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
	
	/**
	 * 是否到达顶部
	 * @return
	 */
	@Deprecated
	protected boolean isReachTopBound() {
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
	@Deprecated
	protected boolean isReachBottomBound(){
		int lastVisPos = getLastVisiblePosition();
		if(lastVisPos == getCount() - 1){
			View lastView = getChildAt(getChildCount() - 1);
			if(lastView != null && lastView.getBottom() <= getHeight() && getCount() > getChildCount()){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}

}
