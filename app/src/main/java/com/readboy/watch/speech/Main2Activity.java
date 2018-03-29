package com.readboy.watch.speech;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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

import com.baidu.duer.dcs.api.IDialogStateListener;
import com.baidu.duer.dcs.framework.internalapi.DcsConfig;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.HtmlPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.RenderCardPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.RenderVoiceInputTextPayload;
import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.util.CommonUtil;
import com.baidu.duer.dcs.util.NetWorkUtil;
import com.readboy.watch.speech.util.NetworkUtils;
import com.readboy.watch.speech.util.PreferencesUtils;
import com.readboy.watch.speech.util.ToastUtils;
import com.readboy.watch.speech.view.DragFrameLayout;

import java.io.File;

/**
 * @author oubin
 * @date 2018/2/1
 */
public class Main2Activity extends BaseDcsActivity implements View.OnClickListener {
    private static final String TAG = "DCS_Main2Activity";

    private static final String ACTION_CHARGING_ANIM_DISPLAY = "android.intent.action.ACTION_CHARGING_ANIM_DISPLAY";

    private static final boolean IS_TEST_MODE = false;
    private static final int HANDLER_WHAT_STOP_RECORD = 1;
    private static final int HANDLER_WHAT_FINISH = 2;
    private static final int HANDLER_WHAT_SHOW_HELLO = 3;
    private static final int HANDLER_WHAT_RECORD_ANIMATION = 4;
    private static final int HANDLER_WHAT_LOADING_TIME_OUT = 5;


    private static final int RECOGNITION_TIME_OUT = 3000;
    /**
     * 加载超时时间，单位毫秒
     */
    private static final int LOADING_TIME_OUT = 30000;
    private static final int DELAYED_START_RECORD_ANIMATION = 1100;
    private static final int DELAYED_FINISH_MILLIS = 4 * 1000;
    private static final int DELAYED_SHOW_HELLO_MILLIS = 20 * 1000;

    private TextView mMessageTv;
    private Button mHoldRecord;
    private ImageView mRecordingIv;
    private ImageView mRecordingIv2;
    private AnimationDrawable mRecordAnimator;
    private AnimationDrawable mRecordAnimator2;
    private View mLoading;
    //    private AnimationDrawable mLoadingAnimator;
    private View speech;
    private View help;
    private View mDialog;

    private boolean hasNetwork = false;
    private boolean firstBlood = true;
    private boolean mEnable = true;

    private boolean interrupt = true;
    private boolean started = false;
    private float mDownX = 0;
    private float mDownY = 0;

    private BroadcastReceiver mBroadcastReceiver;

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
                case HANDLER_WHAT_RECORD_ANIMATION:
                    startRecordAnim2();
                    break;
                case HANDLER_WHAT_LOADING_TIME_OUT:
                    dcsSdk.getVoiceRequest().cancelVoiceRequest();
                    showConnectTimeOut();
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
        initViewPager();
        init();
        stopAudioPlayback();

        initVoiceRequestListener();

        registerReceiver();

        //TODO 去掉检查网络状态
//        checkNetwork();
//        operateNetwork();
//        String mode = Build.MODEL;
//        Log.e(TAG, "onCreate: mode2 = " + mode);

//        getInternalApi().speakRequest("你好，我是你的好朋友小蛙，有什么可以帮到你的吗");

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        //非息屏停止播放， isInteractive = false 代表息屏
//        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//        正在打电话停止播放
//        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        Log.e(TAG, "onPause: telephony state = " + manager.getCallState());
        Log.e(TAG, "onPause: isStopListenReceiving = " + isStopListenReceiving);
        if (isStopListenReceiving) {
            dcsSdk.getVoiceRequest().cancelVoiceRequest();
            showHello3();
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
        unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
//        ToastUtils.cancel();

//        overridePendingTransition(R.anim.activity_bottom_enter, R.anim.activity_bottom_exit);
    }

    private void initVoiceRequestListener() {
        // 添加会话状态监听
        dialogStateListener = new IDialogStateListener() {
            @Override
            public void onDialogStateChanged(DialogState dialogState) {
                Log.e(TAG, "onDialogStateChanged() called with: dialogState = " + dialogState + "");
                switch (dialogState) {
                    case IDLE:
                        mHandler.removeMessages(HANDLER_WHAT_LOADING_TIME_OUT);
                        isStopListenReceiving = false;
                        //TODO 点击说话
                        stopRecordAnim();
                        showMessage(null);
                        cancelScreenOn();
                        break;
                    case LISTENING:
                        keepScreenOn();
                        isStopListenReceiving = true;
                        startRecordAnim();
//                        showWaveform();
                        break;
                    case SPEAKING:
                        showMessage(null);
                        break;
                    case THINKING:
                        showLoading();
                        mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_LOADING_TIME_OUT, LOADING_TIME_OUT);
                        break;
                    default:
                        Log.e(TAG, "onDialogStateChanged: default state = " + dialogState);
                        break;
                }
            }
        };
        dcsSdk.getVoiceRequest().addDialogStateListener(dialogStateListener);
    }

    @Override
    public boolean enableWakeUp() {
        return false;
    }

    @Override
    public int getAsrMode() {
        return DcsConfig.ASR_MODE_ONLINE;
    }

    @Override
    public int getAsrType() {
        return DcsConfig.ASR_TYPE_AUTO;
    }

    @Override
    public boolean isSilentLogin() {
        return true;
    }

    @Override
    protected void handleRenderVoiceInputTextPayload(RenderVoiceInputTextPayload payload) {
        Log.e(TAG, "handleRenderVoiceInputTextPayload: text = " + payload.text);
        setMessage(payload.text);
    }

    @Override
    protected void handleRenderCard(RenderCardPayload payload) {
        Log.e(TAG, "handleRenderCard: title = " + payload.title + " content = " + payload.content);
        if ("default".equals(payload.content)) {
            Log.e(TAG, "handleRenderCard: this is default card.");
        } else if (TextUtils.isEmpty(mMessageTv.getText())) {
            showMessage(payload.content);
        }
    }

    @Override
    protected void handleHtmlPayload(HtmlPayload htmlPayload) {
        Log.e(TAG, "handleHtmlPayload: ");
    }

    @Override
    protected void handlePlaybackStopped() {
        Log.e(TAG, "handlePlaybackStopped: ");
    }

    @Override
    protected void handlePlaybackPause() {
        Log.e(TAG, "handlePlaybackPause: ");
    }

    @Override
    protected void handlePlaybackStarted() {
        Log.e(TAG, "handlePlaybackStarted: ");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_bottom_enter, R.anim.activity_bottom_exit);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        Log.e(TAG, "onEnterAnimationComplete: firstBlood = " + firstBlood);
        if (firstBlood && Contracts.INTERNET_ENABLE) {
            operateNetwork();
            firstBlood = false;
            interrupt = false;
        }
    }

    private void registerReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new MyBroadcastReceiver();
            IntentFilter filter = new IntentFilter(ACTION_POWER_PRESS_EXIT);
            filter.addAction(ACTION_CHARGING_ANIM_DISPLAY);
            registerReceiver(mBroadcastReceiver, filter);
        }
    }

    private void removeMessages() {
        mHandler.removeMessages(HANDLER_WHAT_STOP_RECORD);
        mHandler.removeMessages(HANDLER_WHAT_FINISH);
        mHandler.removeMessages(HANDLER_WHAT_SHOW_HELLO);
    }

    private void removeShowHelloMessage() {
        if (mHandler.hasMessages(HANDLER_WHAT_SHOW_HELLO)) {
            mHandler.removeMessages(HANDLER_WHAT_SHOW_HELLO);
        }
    }

    private void initViewPager() {
        ViewPager mViewpager = (ViewPager) findViewById(R.id.viewpager);
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
        speech.setOnClickListener(this);
        help = LayoutInflater.from(this).inflate(R.layout.help, null);
        mMessageTv = (TextView) speech.findViewById(R.id.message_tv);
        mHoldRecord = (Button) speech.findViewById(R.id.hold_record);
        mHoldRecord.setOnClickListener(this);
        mLoading = speech.findViewById(R.id.loading_pb);
//        mLoadingAnimator = () mLoading.getBackground();
//        speech.setOnTouchListener(new RecordTouchListener());
//        speech.setOnLongClickListener(new RecordLongClickListener());

        mHoldRecord.setActivated(true);
        mRecordingIv = (ImageView) speech.findViewById(R.id.recording_iv);
        mRecordAnimator = (AnimationDrawable) mRecordingIv.getBackground();
        mRecordingIv2 = (ImageView) speech.findViewById(R.id.recording_iv2);
        mRecordAnimator2 = (AnimationDrawable) mRecordingIv2.getBackground();
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
        hasNetwork = false;
    }

    private void operateNetwork() {
        Log.e(TAG, "operateNetwork: ");
        mHoldRecord.setActivated(true);
        hasNetwork = true;
        if (!NetworkUtils.isWifiConnected(this)
                && !PreferencesUtils.get(this, PreferencesUtils.KEY_REMINDED_NO_WIFI)) {
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
        if (message != null) {
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

    /**
     * 请长按录音
     */
    private void showHello2() {
        showMessage(R.string.hold_record_message);
    }

    /**
     * 小主人，请问还有什么可以帮你的？
     */
    private void showHello3() {
        showMessage(R.string.hello3);
    }

    /**
     * 通用错误信息
     */
    private void showErrorMessage() {
        Log.e(TAG, "showErrorMessage: 内部错误: " + getString(R.string.error_internal));
        if (!NetworkUtils.isConnected(this)) {
            operateNoNetwork();
        } else {
            showMessage(R.string.error_internal);
        }
    }

    private void showLoading() {
        stopRecordAnim();
        mHoldRecord.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
//        mLoadingAnimator.start();
        mMessageTv.setVisibility(View.GONE);
    }

    private void hideLoading() {
        mHoldRecord.setVisibility(View.VISIBLE);
//        mLoadingAnimator.stop();
        mLoading.setVisibility(View.GONE);
    }

    private void showWaveform() {
        hideLoading();
        mMessageTv.setVisibility(View.GONE);
    }

    private void hideWaveform() {
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
//        Log.e(TAG, "dispatchTouchEvent: event = " + event.getAction()
//                + ", time = " + (System.currentTimeMillis() - time));
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            keepScreenOn();
//            Log.e(TAG, "dispatchTouchEvent: down time = " + time);
        }
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            interrupt = false;
//            cancelScreenOn();
        }
        if (interrupt) {
//            Log.e(TAG, "dispatchTouchEvent: interrupt motion event.");
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    private void loadingMore() {

    }

    /**
     * 一定要先stopService再stopMedia
     */
    private void stopAll() {
        stopService();
        hideLoading();
        hideWaveform();
    }

    private void stopService() {

    }

    private void showNoNetwork() {
        Log.e(TAG, "showNoNetwork: " + getString(R.string.error_no_network2));
        showMessage(getString(R.string.error_no_network2), "assets://network.mp3");
    }

    private void showUnknownHost() {
        if (!NetworkUtils.isConnected(this)) {
            operateNoNetwork();
        } else {
            Log.e(TAG, "showUnknownHost: error : " + getString(R.string.error_service));
            showMessage(getString(R.string.error_service), "assets://server.mp3");
        }
    }

    /**
     * 网络连接超时，或者服务器出问题
     */
    private void showConnectTimeOut() {
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
        mMediaPlayer.play(new IMediaPlayer.MediaResource(source));
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
        PreferencesUtils.save(this, PreferencesUtils.KEY_REMINDED_NO_WIFI, true);
        mEnable = true;
        hideDialog();
    }

    public void onDialogOnceClick(View view) {
        Log.e(TAG, "onDialogOnceClick: ");
        mEnable = true;
        hideDialog();
    }

    private void startRecordAnim() {
        mHoldRecord.setVisibility(View.GONE);
        mRecordingIv.setVisibility(View.VISIBLE);
        mRecordAnimator.start();
        mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_RECORD_ANIMATION, DELAYED_START_RECORD_ANIMATION);
    }

    private void startRecordAnim2() {
        mRecordAnimator.stop();
        mRecordingIv.setVisibility(View.GONE);
        mRecordingIv2.setVisibility(View.VISIBLE);
        mRecordAnimator2.start();
    }

    private void stopRecordAnim() {
        mHoldRecord.setVisibility(View.VISIBLE);
        mRecordAnimator.stop();
        mRecordingIv.setVisibility(View.GONE);
        mRecordingIv2.setVisibility(View.GONE);
        mHandler.removeMessages(HANDLER_WHAT_RECORD_ANIMATION);
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
            if (!mEnable) {
                return false;
            }
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    stopAll();
                    removeShowHelloMessage();
                    mDownX = event.getX();
                    mDownY = event.getY();
                    if (mEnable) {
                        showHello2();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (started && ((Math.abs(mDownX - event.getX()) > THRESHOLD_STOP
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
//        showWaveform();
        if (getAsrMode() == DcsConfig.ASR_MODE_ONLINE) {
            if (!NetWorkUtil.isNetworkConnected(this)) {
//                Toast.makeText(this, getResources().getString(R.string.err_net_msg), Toast.LENGTH_SHORT).show();
                showNoNetwork();
                return;
            }
        }
        if (CommonUtil.isFastDoubleClick()) {
            return;
        }
        if (isStopListenReceiving) {
            dcsSdk.getVoiceRequest().endVoiceRequest();
            isStopListenReceiving = false;
//            showHello3();
            return;
        }
        startRecordAnim();
        showMessage("");
        isStopListenReceiving = true;
//        voiceButton.setText("录音中...");
        mMessageTv.setText("");
        Log.e(TAG, "startRecord: begin voice request.");
        dcsSdk.getVoiceRequest().beginVoiceRequest(getAsrType() == DcsConfig.ASR_TYPE_AUTO);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.speech:
                Log.e(TAG, "onClick: speech, pauseSpeaker()");
                //暂停,ai.dueros.device_interface.voice_output内容
//                if (isPlaying) {
//                    getInternalApi().pauseSpeaker();
//
//                }else {
//                    getInternalApi().resumeSpeaker();
//                }
                //暂停点播内容
                if (isPlayingAudio) {
                    pauseOrPlayMusic();
                }
                stopCustomMediaPlayer();
//                if (CommonUtil.isFastDoubleClick()){
//                }
                break;
            case R.id.hold_record:
                stopCustomMediaPlayer();
                Log.e(TAG, "onClick: mEnable = " + mEnable);
                if (!mEnable) {
                    return;
//                stopMusic()
                }
                if (!checkLoginState()) {
                    return;
                }
                if (!checkConnectState()) {
                    return;
                }

                stopMusic();
                isPlayingAudio = false;
                startRecord();
                break;
            default:
                Log.e(TAG, "onClick: default = " + v.getId());
                break;
        }
    }

    private boolean checkLoginState() {
        if (isLogging) {
            Log.e(TAG, "checkLoginState: is logging.");
            ToastUtils.showShort(this, getString(R.string.logging_toast));
            return false;
        } else if (!isLoginSucceed) {
            Log.e(TAG, "onClick: 未登录成功。");
            ToastUtils.showShort(this, getString(R.string.login_failed_toast));
            return false;
        }
        return true;
    }

    private boolean checkConnectState() {
        boolean result = false;
        switch (connectionStatus) {
            case CONNECTED:
                result = true;
                break;
            case PENDING:
            case DISCONNECTED:
                Log.e(TAG, "checkConnectState: connect state = " + connectionStatus);
                ToastUtils.showShort(this, getString(R.string.logging_toast));
                result = false;
                break;
            default:
                break;
        }
        return result;
    }

    private void stopCustomMediaPlayer() {
        IMediaPlayer.PlayState state = mMediaPlayer.getPlayState();
        if (state == IMediaPlayer.PlayState.PREPARING
                || state == IMediaPlayer.PlayState.PLAYING
                || state == IMediaPlayer.PlayState.PAUSED
                || state == IMediaPlayer.PlayState.PREPARED) {
            mMediaPlayer.stop();
        } else {
            Log.e(TAG, "onClick: mMediaPlayer state = " + state);
        }
    }

    private void keepScreenOn() {
        Log.e(TAG, "keepScreenOn: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    private void cancelScreenOn() {
        Log.e(TAG, "cancelScreenOn: ");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.e(TAG, "MyBroadcastReceiver onReceive: action = " + action);
                if (ACTION_POWER_PRESS_EXIT.equals(action)) {
                    release();
                    finish();
                } else if (ACTION_CHARGING_ANIM_DISPLAY.equals(action)) {
                    if (isStopListenReceiving) {
                        dcsSdk.getVoiceRequest().cancelVoiceRequest();
                        showHello3();
                    }
                }

            }
        }
    }


}
