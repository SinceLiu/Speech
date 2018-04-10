package com.readboy.watch.speech.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;


/**
 * @author oubin
 * @date 2018/4/1
 */

public final class SettingsUtils {
    private static final String TAG = "SettingsUtils";

    public static final int RINGER_MODE_ONLY_RING = 3;

    public static boolean bluetoothEnable(Activity context, boolean enable) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (enable && !adapter.isEnabled()) {
                boolean result = adapter.enable();
                if (!result) {
                    //蓝牙未启用，提示用户打开它
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    context.startActivityForResult(intent, 1);
                }
                Log.e(TAG, "onSetBluetooth: open bluetooth result = " + result);
                return result;
            } else if (!enable && adapter.isEnabled()) {
                return adapter.disable();
            }
            return true;
        } else {
            Toast.makeText(context, "不支持蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * @param brightness 0-255
     */
    public static boolean setSystemTargetBrightness(Context context, int brightness) {
        return Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, brightness);

    }

    /**
     * @param brightnessOffset 0-255
     */
    public static boolean setSystemBrightness(Context context, int brightnessOffset) {
        try {
            int brightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
            int offset = brightnessOffset + brightness;
            int target = Math.min(255, Math.max(offset, 0));
            return Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, target);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param brightness 0-255
     */
    public static void setWindowBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = (float) brightness * (1F / 255F);
        activity.getWindow().setAttributes(lp);
    }

    private static void setVibrationWhenRinging(Context context, boolean enable) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.VIBRATE_WHEN_RINGING,
                enable ? 1 : 0);
    }

    private boolean isVibrationWhenRinging(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.VIBRATE_WHEN_RINGING, 0) != 0;
    }

    /**
     * @param mode The ringer mode, one of {@link AudioManager#RINGER_MODE_NORMAL},
     *             {@link AudioManager#RINGER_MODE_SILENT}, or {@link AudioManager#RINGER_MODE_VIBRATE}.
     */
    public static boolean setRingingMode(Context context, int mode) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setRingerMode(mode);
        switch (mode) {
            case AudioManager.RINGER_MODE_VIBRATE:
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                boolean isRingerModeChanged =
                        mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
                setVibrationWhenRinging(context, true);
                if (!isRingerModeChanged) {
                    context.sendBroadcastAsUser(
                            new Intent(AudioManager.RINGER_MODE_CHANGED_ACTION),
                            new UserHandle(null));
                }
                break;
            case RINGER_MODE_ONLY_RING:
                boolean change =
                        mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
                setVibrationWhenRinging(context, false);
                if (!change) {
                    context.sendBroadcastAsUser(
                            new Intent(AudioManager.RINGER_MODE_CHANGED_ACTION),
                            new UserHandle(null));
                }

                break;
            default:
                return false;
        }
        return true;
    }

}
