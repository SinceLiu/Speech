package com.readboy.watch.speech;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by oubin on 2017/12/29.
 */

public class SpeechApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CrashReport.initCrashReport(getApplicationContext(), "17cdb09846", false);
    }
}
