package com.readboy.watch.speech.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by oubin on 2017/6/22.
 */

public class PreferencesUtils {
    private static final String TAG = "PreferencesUtils";
    public static final String KEY_REMINDED_NO_WIFI = "remindNoWifi";

    private PreferencesUtils(){
        Log.e(TAG, "PreferencesUtils: u can not instantiate me!");
    }

    public static void save(Context context, String key, boolean value){
        SharedPreferences.Editor edit =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

    public static boolean get(Context context, String key){
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getBoolean(key, false);
    }

}
