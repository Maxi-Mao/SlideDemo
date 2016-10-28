package com.maxi.slidedemo.observablescrollview.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Mao Jiqing on 2016/10/27.
 */

public class TopOverLay extends View {

    public TopOverLay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }
}
