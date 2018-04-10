package com.readboy.watch.speech.view;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by oubin on 2017/7/17.
 */

public class DragFrameLayout extends FrameLayout {
    private static final String TAG = "DragFrameLayout";
    private static final float DISMISS_MIN_RATIO = 0.2F;
    private static final int DISMISS_VELOCITY = 2000;
    private static final float MIN_ALPHA = 0.3F;

    private Point mOriginPoint = new Point();
    private float maxTranslationOffset = 0;
    private int mWidth = 0;
    private int mHeight = 0;
    private int mMinDismissDistance;

    private ViewDragHelper mViewDragHelper;
    private DecelerateInterpolator mInterpolator = new DecelerateInterpolator();

    public DragFrameLayout(Context context) {
        this(context, null);
    }

    public DragFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViewDragHelper = ViewDragHelper.create(this, 1, mCallback);
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        maxTranslationOffset = (float) (h * 0.15);
        mMinDismissDistance = (int) (h * DISMISS_MIN_RATIO);
        View view = getChildAt(0);
        if (view != null) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
            mOriginPoint.x = view.getLeft() + layoutParams.leftMargin;
            mOriginPoint.y = view.getTop() + layoutParams.topMargin;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            handlerCaptureView(child);
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
//            Log.e(TAG, "clampViewPositionVertical: top = " + top + ", dy = " + dy);
            if (top < 0) {
                return 0;
            }
            float alpha = Math.abs(top) * (MIN_ALPHA - 1) / mHeight + 1;
            child.setAlpha(alpha);
            return top;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 1;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
//            Log.e(TAG, "onViewReleased: top = " + releasedChild.getTop()
//                    + "xvel = " + xvel + ", yvel = " + yvel);
            handlerViewReleased(releasedChild);
            int top = releasedChild.getTop();
            if (top < mMinDismissDistance && yvel > DISMISS_VELOCITY) {
                handlerDismissEvent();
            } else if ((top >= mMinDismissDistance && top <= mHeight /2 )
                    && yvel > 0) {
                handlerDismissEvent();
            } else if (top > mHeight /2 && top <= (mHeight - mMinDismissDistance)
                    && yvel>= 0){
                handlerDismissEvent();
            }else if (top > mHeight - mMinDismissDistance && yvel > -DISMISS_VELOCITY) {
                handlerDismissEvent();
            } else {
                mViewDragHelper.settleCapturedViewAt(mOriginPoint.x, mOriginPoint.y);
                releasedChild.setAlpha(1.0F);
                invalidate();
            }
        }
    };

    private void handlerDismissEvent() {
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    private void handlerCaptureView(View view){
        if (mDragCallback != null){
            mDragCallback.tryCaptureView(view);
        }
    }

    private void handlerViewReleased(View view){
        if (mDragCallback != null){
            mDragCallback.onViewReleased(view);
        }
    }

    private OnDragCallback mDragCallback;

    public void setOnDragCallback(OnDragCallback callback){
        this.mDragCallback = callback;
    }

    public interface OnDragCallback{
        void tryCaptureView(View view);
        void onViewReleased(View view);
    }

    private OnDismissListener mDismissListener;

    public void setOnDismissListener(OnDismissListener listener) {
        this.mDismissListener = listener;
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}
