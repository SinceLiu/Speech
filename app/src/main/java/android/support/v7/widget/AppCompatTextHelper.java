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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.v4.widget.TextViewCompat2;
import android.util.AttributeSet;
import android.util.TypedValue;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.v4.widget.AutoSizeableTextView.PLATFORM_SUPPORTS_AUTOSIZE;

@RequiresApi(9)
class AppCompatTextHelper {

    // Enum for the "typeface" XML parameter.
    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int MONOSPACE = 3;


    static AppCompatTextHelper create(AppCompatTextView textView) {
        return new AppCompatTextHelper(textView);
    }

    final AppCompatTextView mView;

    private final @NonNull AppCompatTextViewAutoSizeHelper mAutoSizeTextHelper;

    private int mStyle = Typeface.NORMAL;
    private Typeface mFontTypeface;
    private boolean mAsyncFontPending;

    AppCompatTextHelper(AppCompatTextView view) {
        mView = view;
        mAutoSizeTextHelper = new AppCompatTextViewAutoSizeHelper(mView);
    }

    @SuppressLint("NewApi")
    void loadFromAttributes(AttributeSet attrs, int defStyleAttr) {
        final Context context = mView.getContext();

        if (mFontTypeface != null) {
            mView.setTypeface(mFontTypeface, mStyle);
        }

        mAutoSizeTextHelper.loadFromAttributes(attrs, defStyleAttr);

        if (PLATFORM_SUPPORTS_AUTOSIZE) {
            // Delegate auto-size functionality to the framework implementation.
            if (mAutoSizeTextHelper.getAutoSizeTextType()
                    != TextViewCompat2.AUTO_SIZE_TEXT_TYPE_NONE) {
                final int[] autoSizeTextSizesInPx =
                        mAutoSizeTextHelper.getAutoSizeTextAvailableSizes();
                if (autoSizeTextSizesInPx.length > 0) {
                    if (mView.getAutoSizeStepGranularity() != AppCompatTextViewAutoSizeHelper
                            .UNSET_AUTO_SIZE_UNIFORM_CONFIGURATION_VALUE) {
                        // Configured with granularity, preserve details.
                        mView.setAutoSizeTextTypeUniformWithConfiguration(
                                mAutoSizeTextHelper.getAutoSizeMinTextSize(),
                                mAutoSizeTextHelper.getAutoSizeMaxTextSize(),
                                mAutoSizeTextHelper.getAutoSizeStepGranularity(),
                                TypedValue.COMPLEX_UNIT_PX);
                    } else {
                        mView.setAutoSizeTextTypeUniformWithPresetSizes(
                                autoSizeTextSizesInPx, TypedValue.COMPLEX_UNIT_PX);
                    }
                }
            }
        }
    }

    void onSetTextAppearance(Context context, int resId) {
    }

    void applyCompoundDrawablesTints() {

    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!PLATFORM_SUPPORTS_AUTOSIZE) {
            autoSizeText();
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    void setTextSize(int unit, float size) {
        if (!PLATFORM_SUPPORTS_AUTOSIZE) {
            if (!isAutoSizeEnabled()) {
                setTextSizeInternal(unit, size);
            }
        }
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    void autoSizeText() {
        mAutoSizeTextHelper.autoSizeText();
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    boolean isAutoSizeEnabled() {
        return mAutoSizeTextHelper.isAutoSizeEnabled();
    }

    private void setTextSizeInternal(int unit, float size) {
        mAutoSizeTextHelper.setTextSizeInternal(unit, size);
    }

    void setAutoSizeTextTypeWithDefaults(@TextViewCompat2.AutoSizeTextType int autoSizeTextType) {
        mAutoSizeTextHelper.setAutoSizeTextTypeWithDefaults(autoSizeTextType);
    }

    void setAutoSizeTextTypeUniformWithConfiguration(
            int autoSizeMinTextSize,
            int autoSizeMaxTextSize,
            int autoSizeStepGranularity,
            int unit) throws IllegalArgumentException {
        mAutoSizeTextHelper.setAutoSizeTextTypeUniformWithConfiguration(
                autoSizeMinTextSize, autoSizeMaxTextSize, autoSizeStepGranularity, unit);
    }

    void setAutoSizeTextTypeUniformWithPresetSizes(@NonNull int[] presetSizes, int unit)
            throws IllegalArgumentException {
        mAutoSizeTextHelper.setAutoSizeTextTypeUniformWithPresetSizes(presetSizes, unit);
    }

    @TextViewCompat2.AutoSizeTextType
    int getAutoSizeTextType() {
        return mAutoSizeTextHelper.getAutoSizeTextType();
    }

    int getAutoSizeStepGranularity() {
        return mAutoSizeTextHelper.getAutoSizeStepGranularity();
    }

    int getAutoSizeMinTextSize() {
        return mAutoSizeTextHelper.getAutoSizeMinTextSize();
    }

    int getAutoSizeMaxTextSize() {
        return mAutoSizeTextHelper.getAutoSizeMaxTextSize();
    }

    int[] getAutoSizeTextAvailableSizes() {
        return mAutoSizeTextHelper.getAutoSizeTextAvailableSizes();
    }
}
