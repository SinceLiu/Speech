package com.readboy.watch.speech;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.readboy.watch.speech.util.NetworkUtils;
import com.readboy.watch.speech.util.ToastUtils;
import com.readboy.watch.speech.view.DragFrameLayout;
import com.readboy.watch.speech.view.PointIndicator;

import java.io.File;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oubin
 * @date 2018/2/1
 */
public class Main2Activity extends Activity {
    private static final String TAG = "okHttpMain2Activity";
    private static final boolean IS_TEST_MODE = false;
    private static final int HANDLER_WHAT_STOP_RECORD = 1;
    private static final int HANDLER_WHAT_FINISH = 2;
    private static final int HANDLER_WHAT_SHOW_HELLO = 3;
    private static final int DELAYED_FINISH_MILLIS = 4 * 1000;
    private static final int DELAYED_SHOW_HELLO_MILLIS = 20 * 1000;
    private static final String ACTION_CHARGING_ANIM_DISPLAY = "android.intent.action.ACTION_CHARGING_ANIM_DISPLAY";
    private static final String ACTION_CHAGRING_ANIM_CANCEL = "android.intent.action.ACTION_CHARGING_ANIM_CANCEL";

    private TextView mMessageTv;
    private Button mHoldRecord;
    private ImageView mRecordingIv;
    private AnimationDrawable mRecordAnimator;
    private View mLoading;
    private AnimationDrawable mLoadingAnimator;
    private ViewPager mViewpager;
    private ImageView mWaveform;
    private AnimationDrawable mWaveAnimator;
    private View speech;
    private View help;
    private View mDialog;
    private AlertDialog mAlertDialog;
    private BroadcastReceiver mReceiver;
    private NetworkCallback mNetworkCallback;

    private List<String> messageList = new ArrayList<>();
    private int nextIndex;

    private boolean hasNetwork = false;
    private boolean firstBlood = true;
    private boolean mEnable = true;
    private boolean mShowHello = false;
    private boolean isPaused = false;

    private boolean interrupt = true;
    private long time = 0;
    private boolean started = false;
    private float mDownX = 0;
    private float mDownY = 0;
    private int mLoadMoreTime = 0;
    private int mCurrentRequestTime = 0;

    /**
     * 控制最长录音时间
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_WHAT_FINISH:
                    finish();
                    break;
                case HANDLER_WHAT_STOP_RECORD:
                    //TODO 超时停止录音
                    ToastUtils.showShort(Main2Activity.this,
                            getString(R.string.toast_max_record_time));
                    stopTouchEvent();
                    break;
                case HANDLER_WHAT_SHOW_HELLO:
                    showHello3();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: ");
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        setContentView(R.layout.activity_main);
        assignViews();
        initViewPager();
        init();
        stopAudioPlayback();

        //TODO 去掉检查网络状态
//        checkNetwork();
//        operateNetwork();
        String mode = Build.MODEL;
        Log.e(TAG, "onCreate: mode2 = " + mode);

    }

    private void sendFileBroadcast(String path) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(path)));
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        registerReceiver();
        isPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //非息屏停止播放， isInteractive = false 代表息屏
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        Log.e(TAG, "onPause: screen state = " + pm.isInteractive());
        isPaused = true;
        //正在打电话停止播放
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (manager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
            Log.e(TAG, "onPause: calling");
            mHandler.removeMessages(HANDLER_WHAT_STOP_RECORD);
        }
        overridePendingTransition(R.anim.activity_bottom_enter, R.anim.activity_bottom_exit);
    }

    @Override
    public void finishAndRemoveTask() {
        super.finishAndRemoveTask();
        Log.e(TAG, "finishAndRemoveTask: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        stopAll();
        removeMessages();
        unregisterReceiver();
        ToastUtils.cancel();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_bottom_enter, R.anim.activity_bottom_exit);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (firstBlood && Contracts.INTERNET_ENABLE) {
            operateNetwork();
            firstBlood = false;
            interrupt = false;
        }
    }

    private void removeMessages() {
        mHandler.removeMessages(HANDLER_WHAT_STOP_RECORD);
        mHandler.removeMessages(HANDLER_WHAT_FINISH);
        mHandler.removeMessages(HANDLER_WHAT_SHOW_HELLO);
    }

    private void sendShowHelloMessage() {
        Log.e(TAG, "sendShowHelloMessage: post handler show hello3");
        mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_SHOW_HELLO, DELAYED_SHOW_HELLO_MILLIS);
    }

    private void removeShowHelloMessage() {
        if (mHandler.hasMessages(HANDLER_WHAT_SHOW_HELLO)) {
            mHandler.removeMessages(HANDLER_WHAT_SHOW_HELLO);
        }
    }

    private void initViewPager() {
        mViewpager = (ViewPager) findViewById(R.id.viewpager);
        PagerAdapter adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = position == 0 ? speech : help;
                container.addView(view);
                return view;
            }
        };
        mViewpager.setAdapter(adapter);
    }

    private void assignViews() {
        speech = LayoutInflater.from(this).inflate(R.layout.speech, null);
        help = LayoutInflater.from(this).inflate(R.layout.help, null);
        mMessageTv = (TextView) speech.findViewById(R.id.message_tv);
        mHoldRecord = (Button) speech.findViewById(R.id.hold_record);
        mLoading = speech.findViewById(R.id.loading_ll);
        mLoadingAnimator = (AnimationDrawable) speech.findViewById(R.id.progress_bar).getBackground();
        mWaveform = (ImageView) speech.findViewById(R.id.waveform);
        mWaveAnimator = (AnimationDrawable) mWaveform.getBackground();
        speech.setOnTouchListener(new RecordTouchListener());
        speech.setOnLongClickListener(new RecordLongClickListener());
        mHoldRecord.setActivated(true);
        mRecordingIv = (ImageView) speech.findViewById(R.id.recording_iv);
        mRecordAnimator = (AnimationDrawable) mRecordingIv.getBackground();
        mDialog = findViewById(R.id.dialog_main);
        DragFrameLayout dragFrameLayout = (DragFrameLayout) findViewById(R.id.drag_layout);
        dragFrameLayout.setOnDismissListener(new DragFrameLayout.OnDismissListener() {
            @Override
            public void onDismiss() {
                stopAll();
                finish();
            }
        });
        dragFrameLayout.setOnDragCallback(new DragFrameLayout.OnDragCallback() {
            @Override
            public void tryCaptureView(View view) {
                if (!mHoldRecord.isActivated()) {
                    Log.e(TAG, "tryCaptureView: remove finish message");
                    mHandler.removeMessages(HANDLER_WHAT_FINISH);
                }
            }

            @Override
            public void onViewReleased(View view) {
                if (!mHoldRecord.isActivated()) {
                    Log.e(TAG, "onViewReleased: send finish message");
                    mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_FINISH, DELAYED_FINISH_MILLIS);
                }
            }
        });

    }

    private void init() {
    }

    private void registerReceiver() {
    }

    private void registerReceiverCharging() {
        IntentFilter filter = new IntentFilter(ACTION_CHARGING_ANIM_DISPLAY);
        registerReceiver(mChargingReceiver, filter);
    }

    private void unregisterReceiver() {

    }

    private void checkNetwork() {
        Log.e(TAG, "checkNetwork: ");
        if ((hasNetwork || firstBlood) && !NetworkUtils.isConnected(this)) {
            operateNoNetwork();
            firstBlood = false;
        } else if (!hasNetwork && NetworkUtils.isConnected(this)) {
            operateNetwork();
            firstBlood = false;
        }
    }

    private void operateNoNetwork() {
        Log.e(TAG, "operateNoNetwork: ");
        showNoNetwork();
//        mHoldRecord.setActivated(false);
//        mHoldRecord.setTextColor(ActivityCompat.getColor(this, R.color.gray_deep));
        hasNetwork = false;
    }

    private void operateNetwork() {
        Log.e(TAG, "operateNetwork: ");
        mHoldRecord.setActivated(true);
        hasNetwork = true;
        if (!NetworkUtils.isWifiConnected(this)) {
            showDialog();
        } else {
            mEnable = true;
            showIntroduction();
        }
    }

    private void showMessage(int resId) {
        showMessage(getString(resId));
    }

    private void showMessage(String message) {
        hideLoading();
        hideWaveform();
        mMessageTv.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(message)) {
            setMessage(message);
        }
    }

    private void setMessage(int resId) {
        setMessage(getString(resId));
    }

    private void setMessage(String message) {
        mMessageTv.setText(message);
        updateLayout();
    }

    private void updateLayout() {
        int lineCount = mMessageTv.getLineCount();
        int gravity = mMessageTv.getGravity();
        if (lineCount > 1 && gravity != (Gravity.LEFT | Gravity.CENTER_VERTICAL)) {
            mMessageTv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else if (lineCount == 1 && (gravity != Gravity.CENTER)) {
            mMessageTv.setGravity(Gravity.CENTER);
        }
    }

    private void showHello2() {
        showMessage(R.string.hold_record_message);
    }

    private void showHello3() {
        showMessage(R.string.hello3);
    }

    /**
     * 通用错误信息
     */
    private void showErrorMessage() {
        clearMessage();
        Log.e(TAG, "showErrorMessage: 内部错误: " + getString(R.string.error_internal));
        if (!NetworkUtils.isConnected(this)) {
            operateNoNetwork();
        } else {
            showMessage(R.string.error_internal);
        }
    }

    private void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
        mLoadingAnimator.start();
        mMessageTv.setVisibility(View.GONE);
        hideWaveform();
    }

    private void hideLoading() {
        mLoadingAnimator.stop();
        mLoading.setVisibility(View.GONE);
    }

    private void showWaveform() {
        hideLoading();
        mMessageTv.setVisibility(View.GONE);
        mWaveform.setVisibility(View.VISIBLE);
        mWaveAnimator.start();
    }

    private void hideWaveform() {
        mWaveAnimator.stop();
        mWaveform.setVisibility(View.GONE);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
//        Log.e(TAG, "dispatchTouchEvent: event = " + event.getAction()
//                + ", time = " + (System.currentTimeMillis() - time));
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            time = System.currentTimeMillis();
            keepScreenOn();
//            Log.e(TAG, "dispatchTouchEvent: down time = " + time);
        }
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            interrupt = false;
            clearScreenOn();
        }
        if (interrupt) {
//            Log.e(TAG, "dispatchTouchEvent: interrupt motion event.");
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    private void loadingMore() {

    }

    private void clearMessage() {
        if (messageList != null) {
            messageList.clear();
        }
        nextIndex = 0;
        mLoadMoreTime = 0;
        mCurrentRequestTime = 0;
    }


    //一定要先stopService再stopMedia
    private void stopAll() {
        stopService();
        hideLoading();
        hideWaveform();
    }

    private void stopService() {
        clearMessage();
    }

    private void showNoNetwork() {
        Log.e(TAG, "showNoNetwork: " + getString(R.string.error_no_network2));
        showMessage(getString(R.string.error_no_network2), "network.mp3");
    }

    private void showUnknownHost() {
        if (!NetworkUtils.isConnected(this)) {
            operateNoNetwork();
        } else {
            Log.e(TAG, "showUnknownHost: error : " + getString(R.string.error_service));
            showMessage(getString(R.string.error_service), "server.mp3");
        }
    }

    /**
     * 网络连接超时，或者服务器出问题
     */
    private void showTimeOut() {
        clearMessage();
        showMessage(getString(R.string.connect_time_out), "timeout.mp3");
    }

    private void showRecognitionError() {
        Log.e(TAG, "showRecognitionError: " + getString(R.string.error_no_result1));
        if (Math.random() > 0.5) {
            showMessage(getString(R.string.error_no_result1), Contracts.NO_RESULT_FILE1);
        } else {
            showMessage(getString(R.string.error_no_result2), Contracts.NO_RESULT_FILE2);
        }
    }

    private void showIntroduction() {
        showMessage(getString(R.string.hello1), Contracts.HELLO1);
    }

    private void showMessage(final String text, String source) {
        showMessage(text);
    }

    /**
     * 暂停其他播放器
     */
    private void stopAudioPlayback() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);
    }

    /**
     * 提醒用户正在使用移动数据流量
     */
    private void showDialog() {
//        showAlertDialog();

        //endregion

        mEnable = false;
        mDialog.setVisibility(View.VISIBLE);
    }

    private void showAlertDialog() {
        //region Show AlertDialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //是否支持点击其他区域关闭弹窗
        builder.setCancelable(false);
        builder.setMessage(R.string.using_mobile_data_traffic);
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDialogCancelClick(null);
            }
        });
        builder.setNegativeButton(R.string.once, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDialogOnceClick(null);
            }
        });
        builder.setPositiveButton(R.string.always, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDialogAlwaysClick(null);
            }
        });
        builder.create().show();
    }

    private void hideDialog() {
        mDialog.setVisibility(View.GONE);
//        mAlertDialog.cancel();
    }

    public void onDialogCancelClick(View view) {
        Log.e(TAG, "onDialogCancelClick: ");
        mEnable = false;
        setMessage(R.string.cannot_using_traffic);
        mHoldRecord.setActivated(false);
        mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_FINISH, DELAYED_FINISH_MILLIS);
        hideDialog();
    }

    public void onDialogAlwaysClick(View view) {
        Log.e(TAG, "onDialogAlwaysClick: ");
        mEnable = true;
        hideDialog();
    }

    public void onDialogOnceClick(View view) {
        Log.e(TAG, "onDialogOnceClick: ");
        mEnable = true;
        hideDialog();
    }

    private void startRecordAnim() {
        mRecordingIv.setVisibility(View.VISIBLE);
        mRecordAnimator.start();
    }

    private void stopRecordAnim() {
        mRecordAnimator.stop();
        mRecordingIv.setVisibility(View.GONE);
    }

    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void clearScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 网络连接广播
     */
    private class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                checkNetwork();
            }
        }
    }

    //    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class NetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            if (!hasNetwork && !firstBlood) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        operateNetwork();
                    }
                });
            }
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            if (hasNetwork) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        operateNoNetwork();
                    }
                });
            }
        }
    }

    private class RecordTouchListener implements View.OnTouchListener {
        private static final float THRESHOLD_STOP = 20;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            Log.e(TAG, "View onTouch: action = " + event.getAction());
            if (!mEnable) {
                return false;
            }
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
//                    Log.e(TAG, "onTouch: down event start = " + time);
                    stopAll();
                    removeShowHelloMessage();
                    mDownX = event.getX();
                    mDownY = event.getY();
                    if (mEnable) {
                        showHello2();
                    }
//                    Log.e(TAG, "onTouch: down event end = " + (System.currentTimeMillis() - time));
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (started
                            && ((Math.abs(mDownX - event.getX()) > THRESHOLD_STOP
                            || Math.abs(mDownY - event.getY()) > THRESHOLD_STOP))) {
                        stopTouchEvent();
                        interrupt = true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    stopTouchEvent();
                    break;
                default:
                    Log.e(TAG, "onTouch: other action = " + action);
                    break;
            }
            return false;
        }

    }

    private void stopTouchEvent() {
        if (!started) {
            return;
        }
        mHandler.removeMessages(HANDLER_WHAT_STOP_RECORD);
        if (started) {
            showLoading();
        }
        started = false;
        stopRecordAnim();
    }

    private void startRecord() {
        startRecordAnim();
        //限制最大录音时长
        mHandler.sendEmptyMessageDelayed(
                HANDLER_WHAT_STOP_RECORD, Contracts.RECORDING_MAX_MILLIS);
        started = true;
    }

    private class RecordLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            Log.e(TAG, "onLongClick: start record");
            if (mEnable) {
                startRecord();
            }
            return false;
        }
    }

    private BroadcastReceiver mChargingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.e(TAG, "onReceive: action = " + action);
                if (ACTION_CHARGING_ANIM_DISPLAY.equalsIgnoreCase(action)) {
                }
            }
        }
    };

}
