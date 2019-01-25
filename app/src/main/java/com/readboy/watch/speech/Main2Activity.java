package com.readboy.watch.speech;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat2;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.duer.dcs.api.IDialogStateListener;
import com.baidu.duer.dcs.api.config.DcsConfig;
import com.baidu.duer.dcs.framework.internalapi.IErrorListener;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.IScreenPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message.RenderPlayerInfoPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.HtmlPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.RenderCardPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.RenderVoiceInputTextPayload;
import com.baidu.duer.dcs.util.AsrType;
import com.baidu.duer.dcs.util.util.CommonUtil;
import com.readboy.watch.speech.media.IMediaPlayer;
import com.readboy.watch.speech.util.AppUtils;
import com.readboy.watch.speech.util.ClickUtils;
import com.readboy.watch.speech.util.FileUtils;
import com.readboy.watch.speech.util.NetworkCompat;
import com.readboy.watch.speech.util.NetworkUtils;
import com.readboy.watch.speech.util.PreferencesUtils;
import com.readboy.watch.speech.util.ReadboyUtils;
import com.readboy.watch.speech.util.ToastUtils;
import com.readboy.watch.speech.view.DragFrameLayout;
import com.readboy.watch.speech.view.SilkyAnimation;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author oubin
 * @date 2018/2/1
 */
public class Main2Activity extends BaseDcsActivity implements View.OnClickListener {
    private static final String TAG = "header_http_oubinMainy";

    private static final String ACTION_CHARGING_ANIM_DISPLAY = "android.intent.action.ACTION_CHARGING_ANIM_DISPLAY";
    private static final String ACTION_POWER_PRESS_EXIT = "com.readboy.ACITON_POWER_PRESS_EXIT";
    public static final String READBOY_ACTION_CLASS_DISABLE_CHANGED = "readboy.acion.CLASS_DISABLE_CHANGED";
    private static final String ACTION_APP_DATA_CHANGED = "readboy.action.APP_DATA_SWITCH_CHANGED";

    private static final boolean IS_TEST_MODE = false;
    private static final boolean SHOW_EXTEND_SCREEN = true;
    private static final int HANDLER_WHAT_STOP_RECORD = 1;
    private static final int HANDLER_WHAT_FINISH = 2;
    private static final int HANDLER_WHAT_SHOW_HELLO = 3;
    private static final int HANDLER_WHAT_RECORD_ANIMATION = 4;
    private static final int HANDLER_WHAT_LOADING_TIME_OUT = 5;
    /**
     * 用于自动测试。
     */
    private static final int HANDLER_WHAT_TEST = 99;
    private static boolean isTestMode = false;

    private static final int RECOGNITION_TIME_OUT = 3000;
    /**
     * 加载超时时间，单位毫秒
     */
    private static final int LOADING_TIME_OUT = 30000;
    private static final int DELAYED_START_RECORD_ANIMATION = 1300;
    private static final int DELAYED_FINISH_MILLIS = 4 * 1000;
    private static final Pattern TEXT_CARD_PATTERN = Pattern.compile("^下面播放专辑| 来自蜻蜓$");

    private DragFrameLayout mRootDragFrameLayout;
    private TextView mMessageTv;
    private ImageView mHoldRecord;
    private View mLoading;
    private View speech;
    private View help;
    private SurfaceView mRecordingSurfaceView;
    private SilkyAnimation mSilkyAnimation;
    private ViewStub mDialogViewStub;
    private DragFrameLayout mFullScreenTextParent;
    private TextView mQueryTv;
    private TextView mResponseTv;

    private String mVoiceInputText;
    private String mRenderCardText;

    private boolean hasNetwork = false;
    private boolean firstBlood = true;
    private boolean mEnable = false;
    private boolean isActivated = false;
    /**
     * dcsSdk1.5.0.1版本对话状态由点混乱，为了兼容其状态。
     */
    private long mLastListeningTime = 0L;
    private static final long LISTENING_IDLE_INTERVAL_TIME = 400L;
    private long mLastSpeakingTime = 0L;
    private static final long SPEAKING_LISTENING_INTERVAL_TIME = 500L;

    private boolean interrupt = true;
    private boolean started = false;
    private float mDownX = 0;
    private float mDownY = 0;

    private BroadcastReceiver mBroadcastReceiver;
    private NetworkCompat mNetworkCompat;
    private NetworkCompat.OnNetChangeListener mNetChangeListener;

    /**
     * 控制最长录音时间
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: msg = " + msg.what);
            switch (msg.what) {
                case HANDLER_WHAT_FINISH:
                    finish();
                    break;
                case HANDLER_WHAT_STOP_RECORD:
                    ToastUtils.showShort(Main2Activity.this,
                            getString(R.string.toast_max_record_time));
                    break;
                case HANDLER_WHAT_SHOW_HELLO:
                    showHello3();
                    break;
                case HANDLER_WHAT_LOADING_TIME_OUT:
                    cancelVoiceRequest();
                    stopRecordAnim();
                    showConnectTimeOut();
                    break;
                case HANDLER_WHAT_TEST:
                    if (!isTestMode) {
                        return;
                    }
                    Log.d(TAG, "handleMessage: test, dialog state = " + currentDialogState);
                    if (isListening()) {
//                        onClick(mRecordingIv);
                    } else if (isDialogIdle()) {
                        onClick(mHoldRecord);
                    } else if (currentDialogState == IDialogStateListener.DialogState.THINKING) {
                        onClick(mLoading);
                    }
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
//        addErrorListener();
//        stopAudioPlayback();

        registerReceiver();
        mNetworkCompat = new NetworkCompat();
        mNetChangeListener = new NetChangeListener();
        mNetworkCompat.setNetChangeListener(mNetChangeListener);
        mNetworkCompat.start(this);
        if (SHOW_EXTEND_SCREEN) {
            TextViewCompat2.setAutoSizeTextTypeUniformWithConfiguration(mMessageTv, 18, 24, 3, TypedValue.COMPLEX_UNIT_PX);
        }

        Log.d(TAG, "onCreate: is debug mode = " + AppUtils.isDebugVersion(getApplicationContext()));

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
        isActivated = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: dialog state = " + currentDialogState);
        if (isListening()) {
            cancelVoiceRequest();
            showHello3();
        }

        overridePendingTransition(R.anim.activity_bottom_enter, R.anim.activity_bottom_exit);
        isActivated = false;
        cancelScreenOn();
    }

    @Override
    protected void onDestroy() {
        removeAllMessages();
        mNetworkCompat.stop(this);
        unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        stopAll();
        releaseAnimation();
//        ToastUtils.cancel();

//        overridePendingTransition(R.anim.activity_bottom_enter, R.anim.activity_bottom_exit);
    }

    private void removeAllMessages() {
        mHandler.removeMessages(HANDLER_WHAT_LOADING_TIME_OUT);
        mHandler.removeMessages(HANDLER_WHAT_FINISH);
        mHandler.removeMessages(HANDLER_WHAT_STOP_RECORD);
        mHandler.removeMessages(HANDLER_WHAT_RECORD_ANIMATION);
        mHandler.removeMessages(HANDLER_WHAT_SHOW_HELLO);
        mHandler.removeMessages(HANDLER_WHAT_TEST);
    }

    private void releaseAnimation() {
        if (mSilkyAnimation != null) {
            mSilkyAnimation.stop();
            mSilkyAnimation.release();
        }
    }

    private void initDialogStateListener() {
        // 添加会话状态监听
        dialogStateListener = new IDialogStateListener() {
            @Override
            public void onDialogStateChanged(DialogState dialogState) {
                Log.e(TAG, "onDialogStateChanged() called with: dialogState = " + dialogState + "");
                DialogState lastState = currentDialogState;
                currentDialogState = dialogState;
                switch (dialogState) {
                    case IDLE:
                        if (lastState == DialogState.LISTENING
                                && System.currentTimeMillis() - mLastListeningTime < 100) {
                            Log.e(TAG, "onDialogStateChanged: too fast to idle, filter this idle.");
                            return;
                        }
                        mHandler.removeMessages(HANDLER_WHAT_LOADING_TIME_OUT);
                        stopRecordAnim();
                        showMessage(null);
                        cancelScreenOn();
                        if (lastState == DialogState.SPEAKING) {
                            sendTestMessageDelayed(2000);
                        }
                        break;
                    case LISTENING:
                        keepScreenOn();
                        startRecordAnim();
                        mMessageTv.setText("");
//                        showWaveform();
                        mLastListeningTime = System.currentTimeMillis();
                        break;
                    case SPEAKING:
                        mLastSpeakingTime = System.currentTimeMillis();
                        mHandler.removeMessages(HANDLER_WHAT_LOADING_TIME_OUT);
                        keepScreenOn();
                        if (TextUtils.isEmpty(mMessageTv.getText())) {
                            showMessage(R.string.error_no_asr_result);
                        } else {
                            showMessage(null);
                        }
                        break;
                    case THINKING:
                        showLoading();
                        mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_LOADING_TIME_OUT, LOADING_TIME_OUT);
//                        sendTestMessageDelayed(500L);
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
    public AsrType getAsrType() {
        return AsrType.AUTO;
    }

    @Override
    protected void handleRenderVoiceInputTextPayload(RenderVoiceInputTextPayload payload) {
        Log.e(TAG, "handleRenderVoiceInputTextPayload: text = " + payload.text);
        setMessage(payload.text);
        mVoiceInputText = payload.text;
    }

    @Override
    protected void handleRenderCard(RenderCardPayload payload) {
        Log.e(TAG, "handleRenderCard: title = " + payload.title + " content = " + payload.content);
        if ("default".equals(payload.content)) {
            Log.e(TAG, "handleRenderCard: this is default card.");
        } else if (SHOW_EXTEND_SCREEN) {
            String content = payload.content;
            Matcher mt = TEXT_CARD_PATTERN.matcher(content);
            String result = mt.replaceAll("");
            result = filter(result);
            showMessage(result);
            Log.e(TAG, "handleRenderCard() called with: payload text length = " + result.length() + "");
            if (result.length() > 84) {
                mRenderCardText = result;
                showFullScreenDialog(mVoiceInputText, mRenderCardText);
            }
        }
    }

    private String filter(String message) {
        if (message == null || message.length() < 1) {
            return message;
        }
        String first = message.substring(0, 1);
        int index = message.indexOf(first, 1);
        if (index > 1) {
            String temp1 = message.substring(0, index);
            String temp2 = message.substring(index, message.length());
            if (temp1.equals(temp2)) {
                return temp1;
            }
        }
        return message;
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
    protected void handleRenderPlayerInfo(RenderPlayerInfoPayload infoPayload) {
//        String message = infoPayload.getContent().getTitleSubtext1() + ":"
//                + infoPayload.getContent().getTitle();
//        Log.d(TAG, "handleRenderPlayerInfo: message = " + message);
//        showMessage(message);
    }

    @Override
    protected void handleScreenPayload(IScreenPayload payload) {
//        super.handleScreenPayload(payload);
        if (payload != null && SHOW_EXTEND_SCREEN && !isListening()) {
            showMessage(payload.getScreenContent());
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_bottom_enter, R.anim.activity_bottom_exit);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        long start = System.currentTimeMillis();
        Log.e(TAG, "onEnterAnimationComplete: firstBlood = " + firstBlood);
        Log.d(TAG, "onEnterAnimationComplete: start = " + start);
        if (firstBlood && Contracts.INTERNET_ENABLE) {
            operateNetwork();
            firstBlood = false;
            interrupt = false;
            boolean temp = isActivated;
            Log.d(TAG, "onEnterAnimationComplete: isActivated = " + isActivated);
            isActivated = false;
            Log.d(TAG, "onEnterAnimationComplete: isFinishing = " + isFinishing()
                    + ", isDestroyed = " + isDestroyed());
            if (!isFinishing() && !isDestroyed()) {
                startDcsSdk();
                addErrorListener();
                initDialogStateListener();
                initAnimation();
                isActivated = temp;
            }
            Log.d(TAG, "onEnterAnimationComplete: interval = " + (System.currentTimeMillis() - start));
        }
    }

    private void registerReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new MyBroadcastReceiver();
            IntentFilter filter = new IntentFilter(ACTION_POWER_PRESS_EXIT);
            filter.addAction(ACTION_CHARGING_ANIM_DISPLAY);
            filter.addAction(READBOY_ACTION_CLASS_DISABLE_CHANGED);
            filter.addAction(ACTION_APP_DATA_CHANGED);
            registerReceiver(mBroadcastReceiver, filter);
        }
    }

    private void removeShowHelloMessage() {
        if (mHandler.hasMessages(HANDLER_WHAT_SHOW_HELLO)) {
            mHandler.removeMessages(HANDLER_WHAT_SHOW_HELLO);
        }
    }

    private void removeTestMessage() {
        if (isTestMode) {
            if (mHandler.hasMessages(HANDLER_WHAT_TEST)) {
                mHandler.removeMessages(HANDLER_WHAT_TEST);
            }
        }
    }

    private void sendTestMessageDelayed(long delayedTime) {
        if (isTestMode) {
            mHandler.removeMessages(HANDLER_WHAT_TEST);
            mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_TEST, delayedTime);
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
        help = LayoutInflater.from(this).inflate(R.layout.help, null);
        mMessageTv = (TextView) speech.findViewById(R.id.message_tv);
        mMessageTv.setOnClickListener(this);
        mHoldRecord = (ImageView) speech.findViewById(R.id.voice_btn);
        mHoldRecord.setOnClickListener(this);
        mLoading = speech.findViewById(R.id.loading_pb);
        //需要设置点击事件，要不然会响应speech的点击事件。
        mLoading.setOnClickListener(this);
        mHoldRecord.setActivated(true);
//        mDialog = findViewById(R.id.dialog_main);
        mRootDragFrameLayout = (DragFrameLayout) findViewById(R.id.drag_layout);
        mRootDragFrameLayout.setOnDismissListener(new DragFrameLayout.OnDismissedListener() {
            @Override
            public void onDismissed() {
                stopAll();
                finish();
            }
        });
        mRootDragFrameLayout.setOnDragCallback(new DragFrameLayout.OnDragCallback() {
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

        mRecordingSurfaceView = (SurfaceView) speech.findViewById(R.id.recording_surface_view);
        mRecordingSurfaceView.setOnClickListener(this);

    }

    private void initAnimation() {
        if (mSilkyAnimation == null) {
            mSilkyAnimation = new SilkyAnimation.Builder(mRecordingSurfaceView, "recording")
                    .setRepeatPosition(16)
                    .build();
        }
    }

    private void addErrorListener() {
        addErrorListener(new ErrorListenerSample());
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
        if (isFinishing() || isDestroyed()) {
            Log.d(TAG, "operateNetwork: is finishing, or is destroyed.");
            return;
        }
        Log.e(TAG, "operateNetwork: ");
        mHoldRecord.setActivated(true);
        hasNetwork = true;
        if (!NetworkUtils.isWifiConnected(this)
                && ReadboyManagerWrapper.isEnableOnlyWifi(this)) {
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

    private void showFullScreenDialog(String query, String response) {
        if (mFullScreenTextParent == null) {
            mFullScreenTextParent = (DragFrameLayout) findViewById(R.id.full_screen_text_parent);
            mFullScreenTextParent.setEdgeTrackingEnabled(DragFrameLayout.EDGE_LEFT);
            mFullScreenTextParent.setOnScrollCompletedListener(new DragFrameLayout.OnScrollCompletedListener() {
                @Override
                public void onCompleted(int left, int top, int dx, int dy) {
                    dismissFullScreenDialog();
                }
            });
            mQueryTv = (TextView) findViewById(R.id.query_text_view);
            mResponseTv = (TextView) findViewById(R.id.response_text_view);
            mResponseTv.setOnClickListener(this);
        }
        mFullScreenTextParent.setVisibility(View.VISIBLE);
        mFullScreenTextParent.setLeft(0);
        mFullScreenTextParent.setTop(0);
        mQueryTv.setText(query);
        mResponseTv.setText(response);
        mRootDragFrameLayout.setEnableGesture(false);
        showMessage(R.string.hello3);
    }

    private void dismissFullScreenDialog() {
        getInternalApi().stopSpeaker();
        mFullScreenTextParent.setVisibility(View.GONE);
        mRootDragFrameLayout.setEnableGesture(true);
    }

    private void updateLayout() {
//        mMessageTv.post(new Runnable() {
//            @Override
//            public void run() {
        Log.e(TAG, "run: line count = " + mMessageTv.getLineCount());
        int lineCount = mMessageTv.getLineCount();
        Log.e(TAG, "updateLayout: text = " + mMessageTv.getText() + ", lineCount = " + lineCount);
        int gravity = mMessageTv.getGravity();
        String text = mMessageTv.getText().toString();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if ((lineCount > 1 || text.length() > 9) && gravity != (Gravity.LEFT | Gravity.CENTER_VERTICAL)) {
            mMessageTv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else if (lineCount == 1 && (gravity != Gravity.CENTER)) {
            mMessageTv.setGravity(Gravity.CENTER);
        }
//            }
//        });
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
//        mMessageTv.setVisibility(View.GONE);
    }

    private void hideLoading() {
        mHoldRecord.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
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
    }

    private void stopService() {

    }

    private void showNoNetwork() {
        Log.e(TAG, "showNoNetwork: " + getString(R.string.error_no_network2));
        showMessage(getString(R.string.error_no_network2), "assets://network.mp3");
    }

    private void showListeningTimeout() {
        if (!isConnected()) {
            Log.d(TAG, "showListeningTimeout: not connected. status = " + connectionStatus);
            showMessage(R.string.connect_time_out);
            return;
        }
//        if (NetworkUtils.isConnected(this)){
//            Log.d(TAG, "showListeningTimeout: network disconnected.");
//            //息屏久了，可能导致该问题
//            showConnectTimeOut();
//            return;
//        }
        stopRecordAnim();
        String text = getString(R.string.error_no_asr_result);
//        getInternalApi().speakRequest(text);
        showMessage(R.string.error_no_asr_result, Contracts.LISTENING_TIMEOUT);
    }

    /**
     * 网络连接超时，或者服务器出问题
     */
    private void showConnectTimeOut() {
        Log.d(TAG, "showConnectTimeOut: 当前网络不稳定，请稍后再试.");
        showMessage(getString(R.string.connect_time_out), "assets://timeout_xiaodu.wav");
    }

    private void showIntroduction() {
        showMessage(getString(R.string.hello1), null);
    }

    private void showMessage(final int resId, String filePath) {
        showMessage(getString(resId), filePath);
    }

    private void showMessage(final String text, String source) {
        showMessage(text);
        if (mMediaPlayer != null && !TextUtils.isEmpty(source)) {
            getMediaPlayer().play(new IMediaPlayer.MediaResource(source));
        } else {
            CrashReport.postCatchedException(new NullPointerException("showMessage: mMediaPlayer = null."));
        }
    }

    /**
     * 暂停其他播放器
     */
    private void stopAudioPlayback() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);
    }

    private void tryShowDialog() {
        if (!NetworkUtils.isWifiConnected(Main2Activity.this)
                && ReadboyManagerWrapper.isEnableOnlyWifi(Main2Activity.this)) {
            showDialog();
        } else {
            hideDialog();
            mEnable = true;
        }
    }

    /**
     * 提醒用户正在使用移动数据流量
     */
    private void showDialog() {
        Log.d(TAG, "showDialog: ");
//        showAlertDialog();

        //endregion

        mEnable = false;
        if (mDialogViewStub == null) {
            mDialogViewStub = (ViewStub) findViewById(R.id.view_stub);
            mDialogViewStub.inflate();
        } else {
            mDialogViewStub.setVisibility(View.VISIBLE);
        }
//        mDialog.setVisibility(View.VISIBLE);
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
        if (mDialogViewStub != null) {
            mDialogViewStub.setVisibility(View.GONE);
        }
//        mDialog.setVisibility(View.GONE);
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

    public void onDialogWifiClick(View view) {
        Log.d(TAG, "onDialogWifiClick: ");
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setClassName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity");
        startActivity(intent);
    }

    public void onDialogExitClick(View view) {
        Log.d(TAG, "onDialogExitClick: ");
        finish();
    }

    private void startRecordAnim() {
        Log.e(TAG, "startRecordAnim() called");
        if (mSilkyAnimation == null) {
            initAnimation();
        }
        mRecordingSurfaceView.setVisibility(View.VISIBLE);
        mSilkyAnimation.start();
        mHoldRecord.setVisibility(View.GONE);
        mHandler.removeMessages(HANDLER_WHAT_RECORD_ANIMATION);
        mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_RECORD_ANIMATION, DELAYED_START_RECORD_ANIMATION);
    }

    private void stopRecordAnim() {
        if (mSilkyAnimation != null) {
            mSilkyAnimation.stop();
        }
        mRecordingSurfaceView.setVisibility(View.GONE);
        mHoldRecord.setVisibility(View.VISIBLE);
    }

    private void startRecord() {
        removeTestMessage();
//        showWaveform();
//        if (ClickUtils.isFastMultiClick()) {
//            Log.e(TAG, "startRecord: is fast double click.");
//            return;
//        }

//        if (isListening()) {
//            endVoiceRequest();
////            showHello3();
//            return;
//        }

//        mMessageTv.setText("");
        // 为了解决频繁的点击 而服务器没有时间返回结果造成的不能点击的bug
        if (isListening()) {
            endVoiceRequest();
        } else {
//            startRecordAnim();
            showMessage("");
            mVoiceInputText = "";
            beginVoiceRequest(getAsrType() == AsrType.AUTO);
        }
    }

    @Override
    public void onClick(View v) {
        if (!isActivated) {
            Log.e(TAG, "onClick: is not activated.");
            return;
        }
        switch (v.getId()) {
            case R.id.response_text_view:
            case R.id.message_tv:
            case R.id.speech:
                Log.d(TAG, "onClick: speech, pauseOrResumeSpeaker()");
                //暂停,ai.dueros.device_interface.voice_output内容
                if (CommonUtil.isFastDoubleClick()) {
                    Log.e(TAG, "onClick: is fast double click.");
                    return;
                }

                //暂停点播内容
//                if (isPlayingAudio) {
//                    pauseOrPlayMusic();
//                }
                pauseOrResumeSpeaker();
                stopCustomMediaPlayer();
//                if (CommonUtil.isFastDoubleClick()){
//                }
                break;
            case R.id.voice_btn:
                stopCustomMediaPlayer();
                Log.d(TAG, "onClick: voice clicked, mEnable = " + mEnable);
                if (!mEnable || !isActivated) {
                    return;
//                sendPauseMusicEvent()
                }

                if (ClickUtils.isFastMultiClick()) {
                    Log.w(TAG, "onClick: is fast multi click.");
                    return;
                }

                if (getAsrMode() == DcsConfig.ASR_MODE_ONLINE) {
                    if (!NetworkUtils.isConnected(this)) {
//                Toast.makeText(this, getResources().getString(R.string.err_net_msg), Toast.LENGTH_SHORT).show();
                        showNoNetwork();
                        return;
                    }
                }

                if (Math.abs(System.currentTimeMillis() - mLastSpeakingTime) < SPEAKING_LISTENING_INTERVAL_TIME
                        && isSpeaking()) {
                    //sdk1.5.0.1存在这问题，过快进入.
                    Log.w(TAG, "onClick: fast to start recording.");
                    return;
                }

                if (!checkLoginState()) {
                    return;
                }
                if (!checkConnectStatus()) {
                    return;
                }

                sendPauseMusicEvent();
//                getInternalApi().stopSpeaker();
                //闲聊完，或者玩完游戏，会检查Audio列表是否有内容。所有这里需要清掉。
                clearAudioList2();
                startRecord();
                break;
            case R.id.recording_surface_view:
                Log.d(TAG, "onClick: recording.");
                if (ClickUtils.isFastMultiClick()) {
                    Log.e(TAG, "onClick: recording is fast double Click.");
                    return;
                }
//                if (isListening()) {
                endVoiceRequest();
//                }
                break;
            case R.id.loading_pb:
//                Log.d(TAG, "onClick: loading.");
//                if (ClickUtils.isFastMultiClick()){
//                    Log.d(TAG, "onClick: loading is fast multi click.");
//                    return;
//                }
//                cancelVoiceRequest();
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

    private boolean checkConnectStatus() {
        boolean result = false;
        switch (connectionStatus) {
            case CONNECTED:
                result = true;
                break;
            case PENDING:
            case DISCONNECTED:
                Log.d(TAG, "checkConnectStatus: connect state = " + connectionStatus);
                ToastUtils.showShort(this, getString(R.string.logging_toast));
                result = false;
                break;
            default:
                Log.d(TAG, "checkConnectStatus: default = " + connectionStatus);
                break;
        }
        return result;
    }

    private void stopCustomMediaPlayer() {
        if (mMediaPlayer == null) {
            return;
        }
        IMediaPlayer.PlayState state = mMediaPlayer.getPlayState();
        if (state == IMediaPlayer.PlayState.PREPARING
                || state == IMediaPlayer.PlayState.PLAYING
                || state == IMediaPlayer.PlayState.PAUSED
                || state == IMediaPlayer.PlayState.PREPARED) {
            mMediaPlayer.stop();
        } else {
//            Log.e(TAG, "onClick: mMediaPlayer state = " + state);
        }
    }

    private void keepScreenOn() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    private void cancelScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void saveLog(String message) {
        if (false) {
            String filePath = Environment.getExternalStorageDirectory() + "/Speech/musicLog.txt";
            FileUtils.appendLog(filePath, message, true);
        }
    }

    private class NetChangeListener extends NetworkCompat.OnNetChangeSample {

        @Override
        public void onMobile() {
            super.onMobile();
            Log.e(TAG, "onMobile() called");
            Main2Activity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!NetworkUtils.isWifiConnected(Main2Activity.this)
                            && ReadboyManagerWrapper.isEnableOnlyWifi(Main2Activity.this)) {
                        if (isListening()) {
                            cancelVoiceRequest();
                            showHello3();
                        }
                        stopCustomMediaPlayer();
                        pauseSpeaker();
                        showDialog();
                    }
                }
            });
        }

        @Override
        public void onWifi() {
            super.onWifi();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideDialog();
                    mEnable = true;
                }
            });
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.e(TAG, "MyBroadcastReceiver onReceive: action = " + action);
                if (ACTION_POWER_PRESS_EXIT.equals(action)) {
                    //需要及时关掉声音，finish后，可能很久才会调onDestroy()
                    release();
                    finish();
                } else if (READBOY_ACTION_CLASS_DISABLE_CHANGED.equals(action)) {
                    String classState = Settings.Global.getString(context.getContentResolver(), "class_disabled");
                    Log.e(TAG, "onReceive classState = " + classState);
                    if (ReadboyUtils.isTimeEnable(Main2Activity.this, classState)) {
                        //需要及时关掉声音
                        release();
                        finish();
                    }
                } else if (ACTION_CHARGING_ANIM_DISPLAY.equals(action)) {
                    if (isListening()) {
                        cancelVoiceRequest();
                        showHello3();
                    }
                } else if (ACTION_APP_DATA_CHANGED.equals(action)) {
                    if (!NetworkUtils.isWifiConnected(Main2Activity.this)
                            && ReadboyManagerWrapper.isEnableOnlyWifi(Main2Activity.this)) {
                        if (isListening()) {
                            cancelVoiceRequest();
                            showHello3();
                        }
                        stopCustomMediaPlayer();
                        pauseSpeaker();
                        showDialog();
                    } else {
                        hideDialog();
                        mEnable = true;
                    }
                }

            }
        }
    }

    private class ErrorListenerSample implements IErrorListener {
        @Override
        public void onErrorCode(ErrorCode errorCode) {
            Log.e(TAG, "onErrorCode:" + errorCode);
            if (errorCode == ErrorCode.VOICE_REQUEST_FAILED) {
                showListeningTimeout();
            } else if (errorCode == ErrorCode.NETWORK_UNAVIABLE) {
                //  网络不可用
                Toast.makeText(Main2Activity.this,
                        "网络不可用",
                        Toast.LENGTH_SHORT)
                        .show();
            } else if (errorCode == ErrorCode.LOGIN_FAILED) {
                // 未登录
                if (NetworkUtils.isConnected(Main2Activity.this)) {
                    Toast.makeText(Main2Activity.this,
                            getString(R.string.no_login),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    ToastUtils.showShort(Main2Activity.this, getString(R.string.error_no_network3));
                }
            } else if (errorCode == ErrorCode.NETWORK_EXCEPTION) {
                Toast.makeText(Main2Activity.this,
                        "网络超时",
                        Toast.LENGTH_SHORT)
                        .show();
            } else if (errorCode == ErrorCode.SDK_VOICE_EXCEPTION) {
                Toast.makeText(Main2Activity.this,
                        "SDK语音错误",
                        Toast.LENGTH_SHORT)
                        .show();
            } else if (errorCode == ErrorCode.SDK_SERVER_EXCEPTION) {
                Toast.makeText(Main2Activity.this,
                        "SDK语音Server错误",
                        Toast.LENGTH_SHORT)
                        .show();
            } else if (errorCode == ErrorCode.SDK_VOICE_UNKNOWN_EXCEPTION) {
                Toast.makeText(Main2Activity.this,
                        "出现未知错误，请重新打开应用",
                        Toast.LENGTH_SHORT)
                        .show();
            } else if ("DECODER_FAILED".equals(errorCode)) {
                ToastUtils.showShort(Main2Activity.this, "录音出错");
            }
        }
    }


}
