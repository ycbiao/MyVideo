package com.example.ycb.myvideo.widget;

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.ActionMode
import android.view.MotionEvent
import android.widget.SeekBar

/**
 * Created by biao on 2019/1/14.
 * 垂直seekbar
 */
class VerticalSeekBar : SeekBar{
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h,w,oldh,oldw)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure( heightMeasureSpec,widthMeasureSpec)
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth())
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.rotate(-90f)
        canvas?.translate(-getHeight().toFloat(),0f)
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled()) {
            return false;
        }

        when(event?.action){
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE ,MotionEvent.ACTION_UP ->{
                        var i = 0;
                        i = getMax() - (getMax() * event.getY() / getHeight()).toInt();
                        setProgress(i)
                        isPressed = true
                        onSizeChanged(getWidth(),getHeight(), 0, 0);
                    }
        }
        return true
    }
}
