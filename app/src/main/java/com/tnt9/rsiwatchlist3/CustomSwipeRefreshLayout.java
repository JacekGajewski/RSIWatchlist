package com.tnt9.rsiwatchlist3;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;


public class CustomSwipeRefreshLayout extends SwipeRefreshLayout{

    private int touch;
    private float previousX;

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        touch = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousX = MotionEvent.obtain(event).getX();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xChange = Math.abs(eventX - previousX);

                if (xChange > touch) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(event);
    }
}
