package com.readboy.watch.speech;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

/**
 *
 * @author oubin
 * @date 2018/3/28
 */

public class TestActivity extends Activity {

    private static final String TAG = "TestActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AnimationSet animationSet = new AnimationSet(false);
        AnimationDrawable drawable = new AnimationDrawable();

    }
}
