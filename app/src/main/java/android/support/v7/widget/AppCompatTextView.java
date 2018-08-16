/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v7.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.widget.AutoSizeableTextView;
import android.support.v4.widget.TextViewCompat2;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * <p>This will automatically be used when you use {@link TextView} in your layouts
 * and the top-level activity / dialog is provided by
 * <a href="{@docRoot}topic/libraries/support-library/packages.html#v7-appcompat">appcompat</a>.
 * You should only need to manually use this class when writing custom views.</p>
 */
public class AppCompatTextView extends TextView implements AutoSizeableTextView {

    private final AppCompatTextHelper mTextHelper;

    public AppCompatTextView(Context context) {
        this(context, null);
    }

    public AppCompatTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public AppCompatTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTextHelper = AppCompatTextHelper.create(this);
        mTextHelper.loadFromAttributes(attrs, defStyleAttr);
        mTextHelper.applyCompoundDrawablesTints();
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        super.setBackgroundResource(resId);

    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);

    }

    @Override
    public void setTextAppearance(Context context, int resId) {
        super.setTextAppearance(context, resId);
        if (mTextHelper != null) {
            mTextHelper.onSetTextAppearance(context, resId);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mTextHelper != null) {
            mTextHelper.onLayout(changed, left, top, right, bottom);
        }
    }

    @Override
    public void setTextSize(int unit, float size) {
        if (PLATFORM_SUPPORTS_AUTOSIZE) {
            super.setTextSize(unit, size);
        } else {
            if (mTextHelper != null) {
                mTextHelper.setTextSize(unit, size);
            }
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mTextHelper != null && !PLATFORM_SUPPORTS_AUTOSIZE && mTextHelper.isAutoSizeEnabled()) {
            mTextHelper.autoSizeText();
        }
    }

    /**
     * This should be accessed via
     * {@link TextViewCompat2#setAutoSizeTextTypeWithDefaults(
     *TextView, int)}
     *
     * @hide
     */
    @Override
    public void setAutoSizeTextTypeWithDefaults(
            @TextViewCompat2.AutoSizeTextType int autoSizeTextType) {
        if (mTextHelper != null) {
            mTextHelper.setAutoSizeTextTypeWithDefaults(autoSizeTextType);
        }
    }

    /**
     * This should be accessed via
     * {@link TextViewCompat2#setAutoSizeTextTypeUniformWithConfiguration(
     *TextView, int, int, int, int)}
     *
     * @hide
     */
//    @RestrictTo(LIBRARY_GROUP)
    @Override
    public void setAutoSizeTextTypeUniformWithConfiguration(
            int autoSizeMinTextSize,
            int autoSizeMaxTextSize,
            int autoSizeStepGranularity,
            int unit) throws IllegalArgumentException {
        if (PLATFORM_SUPPORTS_AUTOSIZE) {
//            super.setAutoSizeTextTypeUniformWithConfiguration(
//                    autoSizeMinTextSize, autoSizeMaxTextSize, autoSizeStepGranularity, unit);
        } else {
            if (mTextHelper != null) {
                mTextHelper.setAutoSizeTextTypeUniformWithConfiguration(
                        autoSizeMinTextSize, autoSizeMaxTextSize, autoSizeStepGranularity, unit);
            }
        }
    }

    /**
     * This should be accessed via
     * {@link TextViewCompat2#setAutoSizeTextTypeUniformWithPresetSizes(
     *TextView, int[], int)}
     *
     * @hide
     */
    @Override
    public void setAutoSizeTextTypeUniformWithPresetSizes(@NonNull int[] presetSizes, int unit)
            throws IllegalArgumentException {
        if (PLATFORM_SUPPORTS_AUTOSIZE) {
//            super.setAutoSizeTextTypeUniformWithPresetSizes(presetSizes, unit);
        } else {
            if (mTextHelper != null) {
                mTextHelper.setAutoSizeTextTypeUniformWithPresetSizes(presetSizes, unit);
            }
        }
    }

    /**
     * This should be accessed via
     * {@link TextViewCompat2#getAutoSizeTextType(TextView)}
     *
     * @hide
     */
    @Override
    @TextViewCompat2.AutoSizeTextType
    public int getAutoSizeTextType() {
        if (PLATFORM_SUPPORTS_AUTOSIZE) {
//            return super.getAutoSizeTextType() == TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
//                    ? TextViewCompat2.AUTO_SIZE_TEXT_TYPE_UNIFORM
//                    : TextViewCompat2.AUTO_SIZE_TEXT_TYPE_NONE;
        } else {
            if (mTextHelper != null) {
                return mTextHelper.getAutoSizeTextType();
            }
        }
        return TextViewCompat2.AUTO_SIZE_TEXT_TYPE_NONE;
    }

    /**
     * This should be accessed via
     * {@link TextViewCompat2#getAutoSizeStepGranularity(TextView)}
     *
     * @hide
     */
    @Override
    public int getAutoSizeStepGranularity() {
        if (PLATFORM_SUPPORTS_AUTOSIZE) {
//            return super.getAutoSizeStepGranularity();
        } else {
            if (mTextHelper != null) {
                return mTextHelper.getAutoSizeStepGranularity();
            }
        }
        return -1;
    }

    /**
     * This should be accessed via
     * {@link TextViewCompat2#getAutoSizeMinTextSize(TextView)}
     *
     * @hide
     */
    @Override
    public int getAutoSizeMinTextSize() {
        if (PLATFORM_SUPPORTS_AUTOSIZE) {
//            return super.getAutoSizeMinTextSize();
        } else {
            if (mTextHelper != null) {
                return mTextHelper.getAutoSizeMinTextSize();
            }
        }
        return -1;
    }

    /**
     * This should be accessed via
     * {@link TextViewCompat2#getAutoSizeMaxTextSize(TextView)}
     *
     * @hide
     */
    @Override
    public int getAutoSizeMaxTextSize() {
        if (mTextHelper != null) {
            return mTextHelper.getAutoSizeMaxTextSize();
        }
        return -1;
    }

    /**
     * This should be accessed via
     * {@link TextViewCompat2#getAutoSizeTextAvailableSizes(TextView)}
     *
     * @hide
     */
    @Override
    public int[] getAutoSizeTextAvailableSizes() {
        if (mTextHelper != null) {
            return mTextHelper.getAutoSizeTextAvailableSizes();
        }
        return new int[0];
    }

}
