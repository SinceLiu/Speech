package com.readboy.watch.speech.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationSet;

import com.readboy.watch.speech.R;

/**
 * Created by oubin on 2018/3/28.
 */

public class RecordingAnimationView extends View {

    private static final int DURATION = 200;

    private Bitmap[] bitmaps = new Bitmap[12];
    private Paint mPaint;

    public RecordingAnimationView(Context context) {
        this(context, null);
    }

    public RecordingAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordingAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.recording1);
        bitmaps[0] = bitmap;

        AnimatorSet animatorSet = new AnimatorSet();
        AnimationDrawable drawable = new AnimationDrawable();

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmaps[0], 0, 0, mPaint);
        postInvalidateDelayed(200);

    }
}

