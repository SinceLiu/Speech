package com.readboy.watch.speech;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
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

        String path = "";
        File file = new File(path);
        try {
            boolean result = file.createNewFile();
            if (result) {
                byte[] temp = new byte[1024 * 1024];
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(temp);
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
