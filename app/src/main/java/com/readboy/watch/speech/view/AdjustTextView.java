package com.readboy.watch.speech.view;

import android.content.Context;
import android.os.Build;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.Map;

/**
 *
 * @author oubin
 * @date 2018/6/16
 */

public class AdjustTextView extends TextView {
    private static final String TAG = "oubin_AdjustTextView";

    private static final float DEFAULT_MIN_TEXT_SIZE = 15;
    private static final float DEFAULT_MAX_TEXT_SIZE = 50;
    /**
     * Attributes
     */
    private TextPaint testPaint;
    private float minTextSize;
    private float maxTextSize;

    private int maxSize = 24;
    private int minSize = 18;

    /**
     * 文本长度对应需要设置的字体大小，只针对中文，
     * 中英文混排可能会有问题。
     */
    private SparseIntArray mappingArray = new SparseIntArray();

    public AdjustTextView(Context context) {
        this(context, null);
    }

    public AdjustTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdjustTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        testPaint = new TextPaint();
        testPaint.set(this.getPaint());
        // max size defaults to the intially specified text size unless it is too small
        maxTextSize = this.getTextSize();
        if (maxTextSize <= DEFAULT_MIN_TEXT_SIZE) {
            maxTextSize = DEFAULT_MAX_TEXT_SIZE;
        }
        minTextSize = DEFAULT_MIN_TEXT_SIZE;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        Log.e(TAG, "onTextChanged() called with: text = " + text + ", start = " + start + ", lengthBefore = " + lengthBefore + ", lengthAfter = " + lengthAfter + "");
        refitText(text.toString(), this.getWidth(), this.getHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e(TAG, "onSizeChanged() called with: w = " + w + ", h = " + h + ", oldw = " + oldw + ", oldh = " + oldh + "");
        if (w != oldw || h != oldh) {
            refitText(this.getText().toString(), w, h);
        }
    }

    private void refitText(String text, int textWidth, int textHeight) {
        if (textWidth > 0 && textHeight > 0) {
            //allow diplay rect
            int availableWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
            int availableHeight = textHeight - this.getPaddingBottom() - this.getPaddingTop();
            //by the line calculate allow displayWidth
            int autoWidth = availableWidth;
            float mult = 1f;
            float add = 0;
            if (Build.VERSION.SDK_INT > 16) {
                mult = getLineSpacingMultiplier();
                add = getLineSpacingExtra();
            } else {
                //the mult default is 1.0f,if you need change ,you can reflect invoke this field;
            }
            float trySize = maxTextSize;
            testPaint.setTextSize(trySize);
            int oldline = 1, newline = 1;
            while ((trySize > minTextSize)) {
                //calculate text singleline width。
                int displayW = (int) testPaint.measureText(text);
                //calculate text singleline height。
                int displaH = round(testPaint.getFontMetricsInt(null) * mult + add);
                if (displayW < autoWidth) {
                    break;
                }
                //calculate maxLines
                newline = availableHeight / displaH;
                //if line change ,calculate new autoWidth
                if (newline > oldline) {
                    oldline = newline;
                    autoWidth = availableWidth * newline;
                    continue;
                }
                //try more small TextSize
                trySize -= 1;
                if (trySize <= minTextSize) {
                    trySize = minTextSize;
                    break;
                }

                testPaint.setTextSize(trySize);
            }
            //setMultiLine
            if (newline >= 2) {
                this.setSingleLine(false);
                this.setMaxLines(newline);
            }
            float currentSize = getTextSize();
            if (currentSize != trySize) {
                this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
            }
        }
    }

    /**
     * FastMath.round()
     *
     * @param value
     * @return
     */
    public static int round(float value) {
        long lx = (long) (value * (65536 * 256f));
        return (int) ((lx + 0x800000) >> 24);
    }

}
