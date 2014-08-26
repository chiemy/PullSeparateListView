package com.chiemy.pulltoexpand;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class HeaderListView extends ListView{
    private Scroller scroller;

    private int len = 50;

    private boolean scrollerType = false;

    private ViewPager viewPager;

    float startY;

    int bottom;

    public HeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scroller = new Scroller(context);

    }

    public HeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);

    }

    public HeaderListView(Context context) {
        super(context);
        scroller = new Scroller(context);

    }

    @Override
    public void addHeaderView(View v, Object data, boolean isSelectable) {
        super.addHeaderView(v, data, isSelectable);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        float currentY = ev.getY();

        Log.e("currentY", currentY + "");
        Log.e("viewPager.getBottom()", viewPager.getBottom() + "");

        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:

            startY = currentY;
            bottom = viewPager.getBottom();
            break;
        case MotionEvent.ACTION_MOVE:

            if (startY > bottom && viewPager.isShown() && viewPager.getTop() >= 0) {

                int y = (int) (bottom + (currentY - startY) / 2.5f);
                Log.e("ACTION_MOVE", "y =" + y);

                if (y < viewPager.getBottom() + len && y >= bottom) {

                }
                scrollerType = false;

            }
            break;
        case MotionEvent.ACTION_UP:

            scrollerType = true;
            Log.e("ACTION_UP", "" + (bottom - viewPager.getBottom()));

            scroller.startScroll(viewPager.getLeft(), viewPager.getBottom(), 0 - viewPager.getLeft(), bottom
                    - viewPager.getBottom(), 200);
            invalidate();
            break;

        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void computeScroll() {

        if (scroller.computeScrollOffset()) {
            int x = scroller.getCurrX();
            int y = scroller.getCurrY();
            viewPager.layout(0, 0, x + viewPager.getWidth(), y);
            if (!scroller.isFinished() && scrollerType && y > bottom) {
            }

            invalidate();

        }
    }

}
