package com.mobile.tradesies.home;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

public class PullToRefresh extends SwipeRefreshLayout {
    private ScrollResolver mScrollResolver;

    public PullToRefresh(Context context) {
        super(context);
    }

    public PullToRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollResolver(ScrollResolver scrollResolver) {
        mScrollResolver = scrollResolver;
    }

    @Override
    public boolean canChildScrollUp() {
        if(mScrollResolver != null){
            return mScrollResolver.canScrollUp();
        }else {
            return super.canChildScrollUp();
        }
    }

    public static interface ScrollResolver{
        public boolean canScrollUp();
    }
}
