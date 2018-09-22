package com.readboy.watch.speech;

import android.app.readboy.PersonalInfo;
import android.app.readboy.ReadboyWearManager;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * @author oubin
 * @date 2018/8/29
 */

public class ReadboyManagerWrapper {

    private static final String TAG = "oubin_readboyService";


    private static final String PACKAGE_NAME = "com.readboy.watch.speech";

    public static boolean isEnableOnlyWifi(Context context) {
        ReadboyWearManager manager = (ReadboyWearManager) context.getSystemService(Context.RBW_SERVICE);
        PersonalInfo info = manager.getPersonalInfo();
        List<String> appList = info.getWifiAppList();
        Log.e(TAG, "isEnableOnlyWifi: appList = " + Arrays.toString(appList.toArray()));
        if (appList == null || !appList.contains(PACKAGE_NAME)) {
            return true;
        }
        Log.e(TAG, "isEnableOnlyWifi: enable = " + info.getWifiAppEnable());
        return info.getWifiAppEnable() == 0;
    }
}
