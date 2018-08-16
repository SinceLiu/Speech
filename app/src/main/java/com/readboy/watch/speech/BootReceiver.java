package com.readboy.watch.speech;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.baidu.dcs.acl.AsrParam;
import com.readboy.watch.speech.util.FileUtils;

import java.io.File;

/**
 * @author oubin
 * @date 2018/6/30
 */

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            asyncCopyLibvad(context);
        }
    }

    private void asyncCopyLibvad(final Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "asyncCopyLibvad: not permission: write external storage.");
            return;
        }
        final String vad = "libvad.dnn.so";
        File file = new File(getLibvadPath() + vad);
        if (!file.exists()) {
            final String absolutePath = file.getAbsolutePath();
            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Void... params) {
                    boolean result = FileUtils.copyAssets(context, vad, absolutePath);
                    Log.e(TAG, "asyncCopyLibvad: result = " + result);
                    AsrParam.ASR_VAD_RES_FILE_PATH = getLibvadPath();
                    Log.e(TAG, "asyncCopyLibvad: file not exit");
                    return null;
                }
            }.execute();
        }
    }

    private static String getLibvadPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/baidu/dueros" + File.separator;
//        return "system/res/";
//        return "assets:///";
    }
}
