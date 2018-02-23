package com.readboy.watch.speech.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.readboy.watch.speech.R;

/**
 * Created by 1 on 2016/9/13.
 *
 */

public class PointIndicator extends View {

    private static final String TAG = "PointIndicator";

    private final int SELECTED_COLOR = 0xff00a9f4;
    private final int UNSELECTED_COLOR = 0xffa1a1a1;
    private final int PADDING = 10;
    private final int MIN_RADIUS = 5;
    private final int MAX_RADIUS = 7;

    private ViewPager mViewPager;
    private int selectedColor = SELECTED_COLOR;
    private int unselectedColor = UNSELECTED_COLOR;
    private int padding = PADDING;
    private int minRadius = MIN_RADIUS;
    private int maxRadius = MAX_RADIUS;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

    private int mCurrentPage = 0;
    private int cy = 0;
    private int mCount = 0;

    public PointIndicator(Context context) {
        this(context, null);
    }

    public PointIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PointIndicator);
        selectedColor = array.getColor(R.styleable.PointIndicator_selectedColor, SELECTED_COLOR);
        unselectedColor = array.getColor(R.styleable.PointIndicator_unselectedColor, UNSELECTED_COLOR);
        minRadius = (int) array.getDimension(R.styleable.PointIndicator_minRadius, MIN_RADIUS);
        maxRadius = (int) array.getDimension(R.styleable.PointIndicator_maxRadius, MAX_RADIUS);
        padding = array.getDimensionPixelSize(R.styleable.PointIndicator_padding, PADDING);

        array.recycle();
        init();
    }

    private void init() {
        mPaint.setColor(unselectedColor);
        mPaint.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        cy = maxRadius + getPaddingTop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null) {
            return;
        }
        final int count = mViewPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }

        if (mCurrentPage >= count) {
            setCurrentItem(count - 1);
            return;
        }

        int paddingLeft = getPaddingLeft();
        int minOffSetX = 2 * minRadius + padding;
        for (int i = 0; i < mCurrentPage; i++) {
            canvas.drawCircle(paddingLeft + minRadius + minOffSetX * i, cy,
                    minRadius, mPaint);
        }

        for (int i = mCurrentPage + 1; i < mCount; i++) {
            canvas.drawCircle(paddingLeft + minOffSetX * i + 2 * maxRadius - minRadius, cy, minRadius, mPaint);
        }

        mPaint.setColor(selectedColor);
        canvas.drawCircle(paddingLeft + minOffSetX * mCurrentPage + maxRadius, cy, maxRadius, mPaint);

        mPaint.setColor(unselectedColor);
        int id = canvas.save();
        canvas.restoreToCount(id);
    }

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        mCount = mViewPager.getAdapter().getCount();
        initListener();
        invalidate();

    }

    private void initListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPage = position;
                invalidate();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mViewPager.setCurrentItem(item);
        mCurrentPage = item;
        invalidate();
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureLong(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY) || (mViewPager == null)) {
            //We were told how big to be
            result = specSize;
        } else {
            //Calculate the width according the views count
            final int count = mViewPager.getAdapter().getCount();
            result = getPaddingLeft() + getPaddingRight() + 2 * maxRadius
                    + ((count - 1) * 2 * minRadius) + (count - 1) * padding + 1;
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureShort(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;
        } else {
            //Measure the height
            result = 2 * maxRadius + getPaddingTop() + getPaddingBottom() + 1;
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
}
