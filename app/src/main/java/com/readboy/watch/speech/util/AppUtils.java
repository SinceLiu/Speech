package com.readboy.watch.speech.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 *
 * @author oubin
 * @date 2018/6/21
 */

public class AppUtils {

    public static boolean isDebugVersion(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            Log.e("oubin-AppUtils", "isDebugVersion: info flags = " + info.flags);
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
