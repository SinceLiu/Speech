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
 * @author oubin
 * @date 2017/7/17
 */

public class DragFrameLayout extends FrameLayout {
    private static final String TAG = "oubin-DragFrameLayout";
    private static final float DISMISS_MIN_RATIO = 0.4F;
    private static final int DISMISS_VELOCITY = 2000;
    private static final float MIN_ALPHA = 0.3F;

    /**
     * Edge flag indicating that the left edge should be affected.
     */
    public static final int EDGE_LEFT = ViewDragHelper.EDGE_LEFT;

    /**
     * Edge flag indicating that the right edge should be affected.
     */
    public static final int EDGE_RIGHT = ViewDragHelper.EDGE_RIGHT;

    /**
     * Edge flag indicating that the bottom edge should be affected.
     */
    public static final int EDGE_BOTTOM = ViewDragHelper.EDGE_BOTTOM;

    /**
     * Edge flag indicating that the top edge should be affected.
     */
    public static final int EDGE_TOP = ViewDragHelper.EDGE_TOP;

    private Point mOriginPoint = new Point();
    private float maxTranslationOffset = 0;
    private int mWidth = 0;
    private int mHeight = 0;
    private int mMinDismissDistance;
    private boolean mEnable = true;

    private static final int[] EDGE_FLAGS = {
            EDGE_LEFT, EDGE_RIGHT, EDGE_BOTTOM, EDGE_TOP
    };

    private int mEdgeFlag;
    /**
     * Edge being dragged
     */
    private int mTrackingEdge;

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
        ViewDragHelper.Callback mCallback = new MyDragCallBack();
        mViewDragHelper = ViewDragHelper.create(this, 1, mCallback);
        setEdgeTrackingEnabled(EDGE_TOP);
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
        if (!mEnable) {
            return false;
        }
        try {
            boolean b = mViewDragHelper.shouldInterceptTouchEvent(ev);
            return b;
        } catch (ArrayIndexOutOfBoundsException e) {
            // FIXME: handle exception
            // issues #9
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    /**
     * Enable edge tracking for the selected edges of the parent view. The
     * callback's
     * {@link ViewDragHelper.Callback#onEdgeTouched(int, int)}
     * and
     * {@link ViewDragHelper.Callback#onEdgeDragStarted(int, int)}
     * methods will only be invoked for edges for which edge tracking has been
     * enabled.
     *
     * @param edgeFlags Combination of edge flags describing the edges to watch
     * @see #EDGE_LEFT
     * @see #EDGE_RIGHT
     * @see #EDGE_BOTTOM
     */
    public void setEdgeTrackingEnabled(int edgeFlags) {
        mEdgeFlag = edgeFlags;
        mViewDragHelper.setEdgeTrackingEnabled(mEdgeFlag);
    }

    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }

    private void handlerDismissEvent() {
        if (mDismissListener != null) {
            mDismissListener.onDismissed();
        }
    }

    private void handlerCaptureView(View view) {
        if (mDragCallback != null) {
            mDragCallback.tryCaptureView(view);
        }
    }

    private void handlerViewReleased(View view) {
        if (mDragCallback != null) {
            mDragCallback.onViewReleased(view);
        }
    }

    private OnDragCallback mDragCallback;

    public void setOnDragCallback(OnDragCallback callback) {
        this.mDragCallback = callback;
    }

    public interface OnDragCallback {
        void tryCaptureView(View view);

        void onViewReleased(View view);
    }

    private OnDismissedListener mDismissListener;

    public void setOnDismissListener(OnDismissedListener listener) {
        this.mDismissListener = listener;
    }

    public interface OnDismissedListener {
        void onDismissed();
    }

    private void handleScrollCompletedEvent(int left, int top, int dx, int dy){
        if (mCompletedListener != null){
            mCompletedListener.onCompleted(left, top, dx, dy);
        }
    }

    public void setOnScrollCompletedListener(OnScrollCompletedListener listener){
        this.mCompletedListener = listener;
    }

    private OnScrollCompletedListener mCompletedListener;

    public interface OnScrollCompletedListener{
        void onCompleted(int left, int top, int dx, int dy);
    }

    private class MyDragCallBack extends ViewDragHelper.Callback {
        private boolean mIsScrollOverValid;

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
//            Log.e(TAG, "tryCaptureView() called with: pointerId = " + pointerId + "");
            handlerCaptureView(child);
            return true;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mEdgeFlag & (EDGE_LEFT | EDGE_RIGHT);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mEdgeFlag & (EDGE_BOTTOM | EDGE_TOP);
//            return 1;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
//            Log.e(TAG, "clampViewPositionHorizontal() called with: left = " + left + ", dx = " + dx + "");
            int ret = child.getLeft();
            if ((mEdgeFlag & EDGE_LEFT) != 0) {
                ret = Math.min(child.getWidth(), Math.max(left, 0));
            } else if ((mEdgeFlag & EDGE_RIGHT) != 0) {
                ret = Math.min(0, Math.max(left, -child.getWidth()));
            }
//            Log.e(TAG, "clampViewPositionHorizontal: ret = " + ret);
            return ret;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
//            Log.e(TAG, "clampViewPositionVertical: top = " + top + ", dy = " + dy);

            int ret = 0;
            float alpha = 1.0F;
            if ((mEdgeFlag & EDGE_BOTTOM) != 0) {
                ret = Math.min(0, Math.max(top, -child.getHeight()));
            } else if ((mEdgeFlag & EDGE_TOP) != 0) {
                ret = Math.min(child.getHeight(), Math.max(top, 0));
                top = Math.max(top, 0);
                alpha = Math.abs(top) * (MIN_ALPHA - 1) / mHeight + 1;
                child.setAlpha(alpha);
            }
//            Log.e(TAG, "clampViewPositionVertical: ret = " + ret);
            return ret;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
//            Log.e(TAG, "onViewPositionChanged() called with: left = " + left + ", top = " + top + ", dx = " + dx + ", dy = " + dy + "");
            boolean isCompleted = false;
            switch (mEdgeFlag){
                case EDGE_LEFT:
                    if (dx >0 && left == mWidth){
                        isCompleted = true;
                    }
                    break;
                case EDGE_TOP:
                    if (dy >0 && top == mHeight){
                        isCompleted = true;
                    }
                    break;
                default:
                    break;
            }

            if (isCompleted){
                handleScrollCompletedEvent(left, top, dx, dy);
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
//            Log.e(TAG, "onViewDragStateChanged() called with: state = " + state + "");
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
//            Log.e(TAG, "onViewReleased: top = " + releasedChild.getTop()
//                    + "xvel = " + xvel + ", yvel = " + yvel);
            handlerViewReleased(releasedChild);
            int finalLeft = mOriginPoint.x;
            int finalTop = mOriginPoint.y;
            int offsetY = Math.min(mMinDismissDistance, mHeight / 2);
            int offsetX = Math.min(mMinDismissDistance, mWidth / 2);
            if ((mEdgeFlag & EDGE_TOP) != 0) {
                int top = releasedChild.getTop();
                if (top < mMinDismissDistance && yvel > DISMISS_VELOCITY) {
                    handlerDismissEvent();
                    finalTop = mHeight;
                } else if (top > offsetY && top <= (mHeight - mMinDismissDistance)
                        && yvel >= 0) {
                    handlerDismissEvent();
                    finalTop = mHeight;
                } else if (top > mHeight - mMinDismissDistance && yvel > -DISMISS_VELOCITY) {
                    handlerDismissEvent();
                    finalTop = mHeight;
                } else {
                    releasedChild.setAlpha(1.0F);
                }
            } else if ((mEdgeFlag & EDGE_LEFT) != 0) {
                int left = releasedChild.getLeft();
                if ((left < mMinDismissDistance && xvel > DISMISS_VELOCITY)
                        || ((left >= offsetX && left <= (mWidth - mMinDismissDistance)) && xvel > 0)
                        || (left > mWidth - mMinDismissDistance && xvel > -DISMISS_VELOCITY)) {
                    finalLeft = mWidth;
                    handlerDismissEvent();
                } else {
                    releasedChild.setAlpha(1.0F);
                }
            }
            mViewDragHelper.settleCapturedViewAt(finalLeft, finalTop);
            invalidate();
        }
    }
}
