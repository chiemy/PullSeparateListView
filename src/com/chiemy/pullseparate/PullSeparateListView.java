package com.chiemy.pullseparate;

import android.content.Context;
import android.content.res.TypedArray;
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

import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * 当滑动到顶部后底部时，实现Item的分离效果。
 * @author chiemy
 *
 */
public class PullSeparateListView extends ListView{
	/**
	 * 最大滑动距离
	 */
	private static final float MAX_DELTAY = 200;
	/**
	 * 分离后恢复的动画时长
	 */
	private static final long SEPARATE_RECOVER_DURATION = 300;
	/**
	 * 摩擦系数
	 */
	private static final float FACTOR = 0.25f;
	/**
	 * 按下x的缩放比例
	 */
	private static final float SCALEX = 0.98f;
	/**
	 * 按下y的缩放比例
	 */
	private static final float SCALEY = 0.9f;
	/**
	 * 展开全部
	 */
	private boolean separateAll;
	
	/**
	 * 到达边界时，滑动的起始位置
	 */
	private float startY;
	/**
	 * 按下时的View
	 */
	private View downView;
	
	private int touchSlop;
	
	private boolean separate = false;
	private boolean showDownAnim;
	
	/**
	 * 原始按下位置(在所有Item中的位置)
	 */
	private int originDownPosition;
	/**
	 * 按下的位置(在屏幕中的位置)
	 */
	private int downPosition;
	
	/**
	 * 上次滑动的位置，用于判断方向
	 */
	private float preY;
	
	private float deltaY;
	private boolean reachTop,reachBottom,move;
	private OnScrollListener mScrollListener;

	public PullSeparateListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.PullSeparateListView);
		separateAll = t.getBoolean(R.styleable.PullSeparateListView_separate_all, false);
		showDownAnim = t.getBoolean(R.styleable.PullSeparateListView_showDownAnim, true);
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
	
	
	
	@SuppressWarnings("deprecation")
	private void init() {
		//不知道怎么让divider和selector和Item一起移动，所以去除，需要自己加分割线
		this.setDivider(null);
		this.setSelector(new BitmapDrawable());
		
		touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		super.setOnScrollListener(listener);
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
	
	/**
	 * 设置是否显示按下的Item的动画效果
	 * @param showDownAnim 默认为true
	 */
	public void setShowDownAnim(boolean showDownAnim) {
		this.showDownAnim = showDownAnim;
	}
	
	public boolean isShowDownAnim() {
		return showDownAnim;
	}
	
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}
	
	//核心代码
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		float currentY = ev.getY();
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			float downX = ev.getX();
			float downY = ev.getY();
			//记录按下位置，当isSeparateAll()返回false时，会用到
			originDownPosition = pointToPosition((int)downX, (int)downY);
			downPosition = originDownPosition - getFirstVisiblePosition();
			if(showDownAnim){
				performDownAnim(downPosition);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			//记录到达顶部或底部时手指的位置
			if(!separate){
				startY = currentY;
			}
			deltaY = currentY - startY;
			
			//到达顶部
			if(reachTop){
				if(!separateFromTop(currentY)){
					return super.dispatchTouchEvent(ev);
				}
				return false;
			}
			//到达底部
			if(reachBottom){
				if(!separateFromBottom(currentY)){
					return super.dispatchTouchEvent(ev);
				}
				return false;
			}
			preY = currentY;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			preY = 0;
			recoverDownView();
			if(separate){
				separate = false;
				recoverSeparate();
				//移动，不响应点击事件
				if(move){
					move = false;
					return false;
				}
			}
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	private boolean separateFromTop(float currentY){
		//不能放在外部，否则在顶部滑动没有Fling效果
		if(deltaY > touchSlop){
			move = true;
		}
		separate = true;
		//超过滑动允许的最大距离，则将起始位置向下移
		if(deltaY > MAX_DELTAY){
			startY = currentY - MAX_DELTAY;
			//超过最大距离时，出现overScroll效果//有问题
			//return super.dispatchTouchEvent(ev);
		}else if(deltaY < 0){ //为负值时（说明反方向超过了起始位置startY）归0
			deltaY = 0;
			separate = false;
		}
		
		if(deltaY <= MAX_DELTAY){
			for(int index = 0 ; index < getChildCount() ; index++){
				View child = getChildAt(index);
				int multiple = index;
				if(!separateAll){
					if(index > downPosition){
						multiple = Math.max(1, downPosition);
					}
				}
				float distance = multiple*deltaY*FACTOR;
				child.setTranslationY(distance);
			}
			//向分离方向的反方向滑动，但位置还未复原时
			if(deltaY != 0 && currentY - preY < 0){
				return true;
			}
			//deltaY=0，说明位置已经复原，然后交给父类处理
		}
		if(deltaY == 0){
			return false;
		}
		return true;
	}
	
	private boolean separateFromBottom(float currentY) {
		if(Math.abs(deltaY) > touchSlop){
			move = true;
		}
		separate = true;
		//超过滑动允许的最大距离，则将起始位置向上移
		if(Math.abs(deltaY) > MAX_DELTAY){
			startY = currentY + MAX_DELTAY;
			//超过最大距离时，出现overScroll效果
			//return super.dispatchTouchEvent(ev);
		}else if(deltaY > 0){ //为正值时（说明反方向移动超过起始位置startY），归0
			deltaY = 0;
			separate = false;
		}
		if(Math.abs(deltaY) <= MAX_DELTAY){
			int visibleCount = getChildCount();
			for(int inedex = 0 ; inedex < visibleCount ; inedex++){
				View child = getChildAt(inedex);
				int multiple = visibleCount - inedex - 1;
				if(!separateAll){
					if(inedex < downPosition){
						multiple = Math.max(1,visibleCount - downPosition - 1);
					}
				}
				float distance = multiple*deltaY*FACTOR;
				child.setTranslationY(distance);
			}
			//向分离方向的反方向滑动，但位置还未复原时
			if(deltaY != 0 && currentY - preY > 0){
				return true;
			}
			//deltaY=0，说明位置已经复原，然后交给父类处理
			if(deltaY == 0){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 恢复
	 */
	private void recoverSeparate() {
		for(int i = 0 ; i < getChildCount() ; i++){
			View child = getChildAt(i);
			ViewPropertyAnimator.animate(child)
			.translationY(0).setDuration(SEPARATE_RECOVER_DURATION)
			.setInterpolator(new AccelerateInterpolator());
		}
	}
	
	/**
	 * 按下的动画
	 * @param downPosition 在屏幕中的位置
	 */
	private void performDownAnim(int downPosition) {
		downView = getChildAt(downPosition);
		if(downView != null){
			ViewPropertyAnimator.animate(downView)
			.scaleX(SCALEX).scaleY(SCALEY).setDuration(50)
			.setInterpolator(new AccelerateInterpolator());
		}
	}
	
	/**
	 * 恢复点击的View
	 */
	private void recoverDownView() {
		if(showDownAnim && downView != null){
			ViewPropertyAnimator.animate(downView)
			.scaleX(1f).scaleY(1f).setDuration(separate ? SEPARATE_RECOVER_DURATION : 100)
			.setInterpolator(new AccelerateInterpolator());
		}
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
			
			//是否到达顶部
			if(firstVisibleItem == 0){
				View firstView = getChildAt(firstVisibleItem);
				if(firstView != null && (firstView.getTop() + getPaddingTop()) >= 0){
					downPosition = originDownPosition;
					reachTop = true;
				}else{
					reachTop = false;
				}
			}else{
				reachTop = false;
			}
			//是否到达底部
			if(firstVisibleItem + visibleItemCount == getCount()){
				View lastView = getChildAt(visibleItemCount - 1);
				if(lastView != null && (lastView.getBottom() + getPaddingBottom()) <= getHeight() && getCount() > getChildCount()){
					downPosition = originDownPosition - firstVisibleItem;
					reachBottom = true;
				}else{
					reachBottom = false;
				}
			}else{
				reachBottom = false;
			}
		}
	};
	
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
