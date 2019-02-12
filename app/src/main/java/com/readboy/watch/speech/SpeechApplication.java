package com.readboy.watch.speech;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;

import org.w3c.dom.Text;

import java.security.Permission;

/**
 * @author oubin
 * @date 2017/12/29
 */

public class SpeechApplication extends Application {

    private static final String TAG = "oubin_Application";

    @Override
    public void onCreate() {
        super.onCreate();
//        CrashReport.initCrashReport(getApplicationContext(), "17cdb09846", false);
        //加快进入应用速度，需要放到异步
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            asyncInitBugly();
        }
    }

    private void asyncInitBugly() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                CrashReport.initCrashReport(getApplicationContext(), "17cdb09846", false);
                String imei = getImei();
                if (!TextUtils.isEmpty(imei)) {
                    CrashReport.setUserId(SpeechApplication.this, imei);
                }
                return null;
            }
        }.execute();
    }

    private String getImei() {
        String imei = "";
        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "getImei: not permission to get device imei");
                return null;
            }
        }
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = manager.getImei();
            } else {
                imei = manager.getDeviceId();
            }
        }
        return imei;
    }

}
