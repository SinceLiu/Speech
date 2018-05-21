package com.readboy.watch.speech;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.dcs.acl.AsrParam;
import com.baidu.duer.dcs.api.DcsSdkBuilder;
import com.baidu.duer.dcs.api.IConnectionStatusListener;
import com.baidu.duer.dcs.api.IDcsSdk;
import com.baidu.duer.dcs.api.IDialogStateListener;
import com.baidu.duer.dcs.api.IFinishedDirectiveListener;
import com.baidu.duer.dcs.api.IMessageSender;
import com.baidu.duer.dcs.api.IResponseListener;
import com.baidu.duer.dcs.api.IVoiceRequestListener;
import com.baidu.duer.dcs.api.asroffline.ASROffLineConfig;
import com.baidu.duer.dcs.api.asroffline.IASROffLineConfigProvider;
import com.baidu.duer.dcs.api.config.DefaultSdkConfigProvider;
import com.baidu.duer.dcs.api.config.SdkConfigProvider;
import com.baidu.duer.dcs.api.player.ITTSPositionInfoListener;
import com.baidu.duer.dcs.api.recorder.AudioRecordImpl;
import com.baidu.duer.dcs.api.recorder.BaseAudioRecorder;
import com.baidu.duer.dcs.api.wakeup.BaseWakeup;
import com.baidu.duer.dcs.api.wakeup.IWakeupAgent;
import com.baidu.duer.dcs.api.wakeup.IWakeupProvider;
import com.baidu.duer.dcs.api.wakeup.WakeUpConfig;
import com.baidu.duer.dcs.api.wakeup.WakeUpException;
import com.baidu.duer.dcs.api.wakeup.WakeUpWord;
import com.baidu.duer.dcs.componentapi.IDcsClient;
import com.baidu.duer.dcs.devicemodule.audioplayer.ApiConstants;
import com.baidu.duer.dcs.devicemodule.audioplayer.AudioPlayerDeviceModule;
import com.baidu.duer.dcs.devicemodule.custominteraction.CustomUserInteractionDeviceModule;
import com.baidu.duer.dcs.devicemodule.form.Form;
import com.baidu.duer.dcs.devicemodule.playbackcontroller.PlaybackControllerDeviceModule;
import com.baidu.duer.dcs.framework.DcsSdkImpl;
import com.baidu.duer.dcs.framework.ILoginListener;
import com.baidu.duer.dcs.framework.InternalApi;
import com.baidu.duer.dcs.framework.internalapi.IDirectiveReceivedListener;
import com.baidu.duer.dcs.framework.internalapi.IErrorListener;
import com.baidu.duer.dcs.framework.location.Location;
import com.baidu.duer.dcs.framework.upload.contact.IUpload;
import com.baidu.duer.dcs.location.ILocation;
import com.baidu.duer.dcs.location.LocationImpl;
import com.baidu.duer.dcs.oauth.api.silent.SilentLoginImpl;
import com.baidu.duer.dcs.sample.sdk.devicemodule.applauncher.AppLauncherDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.applauncher.AppLauncherImpl;
import com.baidu.duer.dcs.sample.sdk.devicemodule.applauncher.message.LaunchAppPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.contacts.ContactsDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.contacts.message.CreateContactPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.contacts.message.SearchContactPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.DeviceControlDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.offlineasr.OffLineDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.PhoneCallDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.CandidateCallee;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.PhonecallByNamePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.PhonecallByNumberPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.SelectCalleePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.ScreenDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.ScreenExtendDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message.RenderAudioListPlayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message.RenderPlayerInfoPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.HtmlPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.RenderCardPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.RenderHintPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.RenderVoiceInputTextPayload;
import com.baidu.duer.dcs.sample.sdk.util.ContactsUtils;
import com.baidu.duer.dcs.sample.sdk.util.UploadPreference;
import com.baidu.duer.dcs.systeminterface.IOauth;
import com.baidu.duer.dcs.util.AsrType;
import com.baidu.duer.dcs.util.HttpProxy;
import com.baidu.duer.dcs.util.api.IDcsRequestBodySentListener;
import com.baidu.duer.dcs.util.dispatcher.DialogRequestIdHandler;
import com.baidu.duer.dcs.util.message.DcsRequestBody;
import com.baidu.duer.dcs.util.message.Directive;
import com.baidu.duer.dcs.util.message.Event;
import com.baidu.duer.dcs.util.message.Header;
import com.baidu.duer.dcs.util.message.MessageIdHeader;
import com.baidu.duer.dcs.util.message.Payload;
import com.baidu.duer.kitt.KittWakeUpImpl;
import com.baidu.speech.asr.SpeechConstant;
import com.readboy.watch.speech.media.MediaPlayerImpl;
import com.readboy.watch.speech.util.FileUtils;
import com.readboy.watch.speech.util.NetworkUtils;
import com.readboy.watch.speech.util.ToastUtils;

import org.json.JSONException;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author oubin
 * @date 2018/2/28
 */

public abstract class BaseDcsActivity extends Activity {
    public static final String TAG = "header_http_oubinBase";

    static {
        AsrParam.ASR_VAD_RES_FILE_PATH = getLibvadPath();
    }

    private static String getLibvadPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/baidu/dueros" + File.separator;
//        return "system/res/";
//        return "assets:///";
    }

    /**
     * 正式client_id
     */
    private static final String CLIENT_ID = "WwhkTgFmkKC3jYgATjV6KESf9ZfeSwti";

    private static final String APP_ID = "dm48A7EB63F5AC1C63";
    private static final String APP_KEY = "628F0F98221FDFA1CDCF3E53B8AC57F0";

    private static final String CLIENT_SECRET = "OjmUyrFKnjqvyn2gb8i6rbrwUxDXlo55";

    /**
     * 唤醒词,可以改为你自己的唤醒词,比如："茄子"
     */
    private static final String WAKEUP_WORD = "小度小度";

    // 唤醒配置
    private static final String WAKEUP_RES_PATH = "snowboy/common.res";
    private static final String WAKEUP_UMDL_PATH = "snowboy/xiaoduxiaodu_all_11272017.umdl";
    private static final String WAKEUP_SENSITIVITY = "0.35,0.35,0.40";
    private static final String WAKEUP_HIGH_SENSITIVITY = "0.45,0.45,0.55";
    /**
     * 唤醒成功后是否需要播放提示音
     */
    private static final boolean ENABLE_PLAY_WARNING = true;
    private static final int REQUEST_CODE = 123;

    private static final String AUDIO_PLAYER_NAMESPACE = ApiConstants.NAMESPACE;

    protected IDcsSdk dcsSdk;
    protected ScreenDeviceModule screenDeviceModule;
    private ContentObserver mContractsObserver;

    private ILocation location;
    /**
     * for dcs统计-demo
     */
    private long duerResultT;
    protected boolean isPlaying;
    /**
     * 用于停止音乐
     */
    protected boolean isPlayingAudio;
    protected boolean isLoginSucceed = false;
    protected IConnectionStatusListener.ConnectionStatus connectionStatus = IConnectionStatusListener.ConnectionStatus.DISCONNECTED;
    protected boolean isLogging = true;
    private boolean isReleased = false;
    private boolean isPausedSpeaker = false;

    protected MediaPlayerImpl mMediaPlayer;
    private AudioManager.OnAudioFocusChangeListener mAudioListener;
    private boolean mPausedByTransientLossOfFocus = false;
    private boolean hadAudioFocus = false;

    private AudioPlayerDeviceModule audioPlayerDeviceModule;
    private Method clearAudioListMethod;

    private IWakeupAgent.IWakeupAgentListener wakeupAgentListener;
    protected IDialogStateListener dialogStateListener;
    private PhoneCallDeviceModule.IPhoneCallListener phoneCallListener;
    private AppLauncherDeviceModule.IAppLauncherListener appLauncherListener;
    private ContactsDeviceModule.IContactsListener contactsListener;
    private PhoneCallDeviceModule phoneCallDeviceModule;
    private AppLauncherDeviceModule appLauncherDeviceModule;
    private DeviceControlDeviceModule.IDeviceControlListener deviceControlListener;
    private DeviceControlDeviceModule deviceControlDeviceModule;
    private ContactsDeviceModule contactsDeviceModule;

    protected IDialogStateListener.DialogState currentDialogState = IDialogStateListener.DialogState.IDLE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (RequestPermissionsActivity.startPermissionActivity(this)) {
//            finish();
//        }
        initPermission();
        copyLibvab();
        initSdk();
        sdkRun();

        initListener();
        initLocation();

        mMediaPlayer = new MediaPlayerImpl(this);

        registerContentObserver();

        test();
    }

    private void copyLibvab() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "copyLibvab: not permission.");
            return;
        }
        String vad = "libvad.dnn.so";
        File file = new File(getLibvadPath() + vad);

        if (!file.exists()) {
            boolean result = FileUtils.copyAssets(this, vad, file.getAbsolutePath());
            Log.e(TAG, "copyLibvab: result = " + result);
            AsrParam.ASR_VAD_RES_FILE_PATH = getLibvadPath();
            Log.e(TAG, "copyLibvab: file not exit");
        }
    }

    private void test() {
        File file = new File(getLibvadPath());
        Log.e(TAG, "test: file exist = " + file.exists() + ", can read = " + file.canRead()
                + ", can write = " + file.canWrite());
    }

    protected void initListener() {
        // 设置各种监听器
        dcsSdk.addConnectionStatusListener(connectionStatusListener);
        // 错误，使用addErrorListener()
//        getInternalApi().addErrorListener(errorListener);
        // event发送
        getInternalApi().addRequestBodySentListener(dcsRequestBodySentListener);
        // 需要定位后赋值，目前是写死的北京的
        getInternalApi().setLocationHandler(locationHandler);
        // 语音文本同步
//        initTTSPositionInfoListener();
        // 唤醒
        if (Config.WAKEUP_ENABLE) {
            initWakeUpAgentListener();
        }
        // 所有指令透传，建议在各自的DeviceModule中处理
        getInternalApi().addDirectiveReceivedListener(directiveReceivedListener);
        // 指令执行完毕回调
//        initFinishedDirectiveListener();
        // 语音音量回调监听
//        initVolumeListener();

        initVoiceErrorListener();
    }

    private void initLocation() {
        // 定位
        location = new LocationImpl(getApplicationContext());
        location.requestLocation(false);
    }

    protected Location.LocationHandler locationHandler = new Location.LocationHandler() {
        @Override
        public double getLongitude() {
            return location.getLocationInfo().longitude;
        }

        @Override
        public double getLatitude() {
            return location.getLocationInfo().latitude;
        }

        @Override
        public String getCity() {
            return location.getLocationInfo().city;
        }

        @Override
        public Location.EGeoCoordinateSystem getGeoCoordinateSystem() {
            return Location.EGeoCoordinateSystem.BD09LL;
        }
    };

    private ScreenDeviceModule.IScreenListener screenListener = new ScreenDeviceModule.IScreenListener() {
        @Override
        public void onRenderVoiceInputText(RenderVoiceInputTextPayload payload) {
            Log.e(TAG, "onRenderVoiceInputText() called with: header payload = " + payload.toString() + "");
            handleRenderVoiceInputTextPayload(payload);
        }

        @Override
        public void onHtmlPayload(HtmlPayload htmlPayload) {
            Log.e(TAG, "onHtmlPayload() called with: htmlPayload = " + htmlPayload);
            handleHtmlPayload(htmlPayload);
        }

        @Override
        public void onRenderCard(RenderCardPayload renderCardPayload) {
            Log.e(TAG, "onRenderCard: title = " + renderCardPayload.title +
                    ", content = " + renderCardPayload.content + ", token = " + renderCardPayload.token);
            handleRenderCard(renderCardPayload);
        }

        @Override
        public void onRenderHint(RenderHintPayload renderHintPayload) {
            Log.e(TAG, "onRenderHint: " + Arrays.toString(renderHintPayload.cueWords.toArray()));
        }
    };

    private IDcsRequestBodySentListener dcsRequestBodySentListener = new IDcsRequestBodySentListener() {
        @Override
        public void onDcsRequestBody(DcsRequestBody dcsRequestBody) {
            String eventName = dcsRequestBody.getEvent().getHeader().getName();
            Log.d(TAG, "onDcsRequestBody eventName:" + eventName);
            if (PlaybackEvent.PLAYBACK_STOPPED.equals(eventName)
                    || PlaybackEvent.PLAYBACK_FINISHED.equals(eventName)) {
                handlePlaybackStopped();
                isPlaying = false;
            } else if (PlaybackEvent.PLAYBACK_PAUSED.equals(eventName)) {
                handlePlaybackPause();
                isPlaying = false;
            } else if (PlaybackEvent.PLAYBACK_STARTED.equals(eventName)
                    || PlaybackEvent.PLAYBACK_RESUMED.equals(eventName)) {
                isPlayingAudio = true;
                handlePlaybackStarted();
                isPlaying = true;
            }
        }
    };
    private IErrorListener errorListener;

    /**
     * ConnectionStatus|DISCONNECTED（长连接断开）,PENDING(长连接正在连接中),CONNECTED(长连接连接正常)
     */
    private IConnectionStatusListener connectionStatusListener = new IConnectionStatusListener() {
        @Override
        public void onConnectStatus(ConnectionStatus status) {
            Log.e(TAG, "onConnectionStatusChange: " + status);
            connectionStatus = status;
            switch (status) {
                case CONNECTED:
                    break;
                case DISCONNECTED:
//                    ToastUtils.showShort(BaseDcsActivity.this, "连接已断开，请重新进入");
                    break;
                case PENDING:
                    break;
                default:
            }
        }
    };

    private IDirectiveReceivedListener directiveReceivedListener = new IDirectiveReceivedListener() {
        @Override
        public void onDirective(Directive directive) {
            if (directive == null) {
                return;
            }
            Log.e(TAG, "onDirective: name = " + directive.getName());
            if ("Play".equals(directive.getName())) {
                Payload mPayload = directive.getPayload();
                if (mPayload instanceof com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload) {
                    com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload.Stream stream =
                            ((com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload) mPayload)
                                    .audioItem.stream;
                    if (stream != null) {
                        mPlayToken = ((com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload) mPayload)
                                .audioItem.stream.token;
                        Log.e(TAG, "onDirective play mToken = " + mPlayToken);
                    }
                }
            } else if ("Stop".equals(directive.getName())) {
                isPlaying = false;
            } else if ("RenderPlayerInfo".equals(directive.getName())) {
                Payload mPayload = directive.getPayload();
                if (mPayload instanceof RenderPlayerInfoPayload) {
                    Log.e(TAG, "onDirective: RenderPlayerInfo " + ((RenderPlayerInfoPayload) mPayload).getContent());
                    mRenderPlayerInfoToken = ((RenderPlayerInfoPayload) mPayload).getToken();
                }
            }
        }
    };

    /**
     * tts文字同步
     */
    private void initTTSPositionInfoListener() {
        getInternalApi().addTTSPositionInfoListener(new ITTSPositionInfoListener() {
            @Override
            public void onPositionInfo(long pos, long playTimeMs, long mark) {
//                Log.i(TAG, "pos:" + pos + ",playTimeMs:" + playTimeMs + ",mark:" + mark);
            }
        });
    }

    /**
     * 语音音量回调监听
     */
    private void initVolumeListener() {
        getInternalApi().getDcsClient().addVolumeListener(new IDcsClient.IVolumeListener() {
            @Override
            public void onVolume(int volume, int percent) {
//                Log.e(TAG, "onVolume() called with: volume = " + volume + ", percent = " + percent + "");
            }
        });
    }

    private void initVoiceErrorListener() {
        getInternalApi().getDcsClient().addVoiceErrorListener(new IDcsClient.IVoiceErrorListener() {
            @Override
            public void onVoiceError(int error, int subError) {
                Log.d(TAG, "onVoiceError:" + error + ":" + subError);
                if (error == -3005 && subError == 0){
                    //主动提交录音内容，进行识别
                }else if (error == 3 && subError == 3101){
                    //拾音超时（10s）,过程都没有声音
                }else {
                    Log.d(TAG, "onVoiceError: other voice error : " + error + ":" + subError);
//                    CrashReport.postCatchedException(new Exception("Voice Error, error = " + error + ":" + subError));
                }
            }
        });
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        ArrayList<String> toApplyList = new ArrayList<String>();
        for (String perm : Contracts.PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
                Log.e(TAG, "initPermission: no permission : " + perm);
            }
        }
        if (!toApplyList.isEmpty()) {
            String[] tmpList = new String[toApplyList.size()];
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED
                    && Arrays.asList(Contracts.PERMISSIONS).contains(permissions[i])) {
                Log.e(TAG, "onRequestPermissionsResult: has not permissions2 = " + permissions[i]);
            }
        }
        copyLibvab();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            uploadContacts();
        }

    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Exception ignored) {
            // LEFT-DO-NOTHING
        }
    }

    protected void initSdk() {
        // 第一步初始化sdk
        // BaseAudioRecorder audioRecorder = new PcmAudioRecorderImpl(); pcm 输入方式
        BaseAudioRecorder audioRecorder = new AudioRecordImpl();

        IOauth oauth = getOath();

        // 离线识别配置
        final ASROffLineConfig asrOffLineConfig = new ASROffLineConfig();
        // 应用的AppId
        asrOffLineConfig.asrAppId = APP_ID;
        // 应用的AppKey
        asrOffLineConfig.asrAppKey = APP_KEY;
        // 应用的SecretKey
        asrOffLineConfig.asrSecretKey = CLIENT_SECRET;
        // 设置离线grammer语法文件位置
        // 离线grammer的内容为Url编码
        asrOffLineConfig.grammerPath = "assets:///baidu_speech_grammar.bsg";

        // 下面这个参数固定
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(SpeechConstant.ASR_AUDIO_COMPRESSION_TYPE, 1);
//        params.put(MFE_DNN_DAT_FILE, "assets://libvad.dnn.so");
//        params.put(MFE_CMVN_DAT_FILE, "assets://libglobal.cmvn.so");
//        params.put(ASR_VAD_RES_PATH, SystemServiceManager.getAppContext().getFilesDir().getAbsolutePath());
//        params.put(ASR_OFFLINE_ENGINE_DAT_FILE_PATH, "assets:///libbd_easr_s1_merge_normal_20151216.dat.so");
//        params.put(ASR_VAD_RES_FILE_PATH, "assets://libvad.dnn.so");
        asrOffLineConfig.params = params;

        // 动态替换原来的槽位数据，即grammerPath中的配置槽位
        // 如果不需要动态配置，就不需要设置offlineAsrSlots字段即可
        // 关于结构内容可以Url解码后查看

        // try {
        //    JSONObject json = new JSONObject();
        //    json.put("name", new JSONArray().put("赵六").put("赵七"))
        //            .put("appname", new JSONArray().put("手百").put("度秘"));
        //    asrOffLineConfig.offlineAsrSlots = json;
        // } catch (JSONException e) {
        //    e.printStackTrace();
        // }

        IASROffLineConfigProvider asrOffLineConfigProvider = new IASROffLineConfigProvider() {
            @Override
            public ASROffLineConfig getOfflineConfig() {
                return asrOffLineConfig;
            }
        };

        // proxyIp 为代理IP
        // proxyPort  为代理port
        HttpProxy httpProxy = new HttpProxy("172.24.194.28", 8888);

        SdkConfigProvider sdkConfigProvider = getSdkConfigProvider();

        // 构造dcs sdk
        DcsSdkBuilder builder = new DcsSdkBuilder();
        dcsSdk = builder.withSdkConfig(sdkConfigProvider)
                .withOauth(oauth)
                .withAudioRecorder(audioRecorder)
                // 设置音乐播放器的实现，sdk 内部默认实现为MediaPlayerImpl
                // .withMediaPlayer(new MediaPlayerImpl(AudioManager.STREAM_MUSIC))
                // .withHttpProxy(httpProxy)
                // 打开debug模式，重要日志写入文件和打印到控制台
                .withOpenDebug(true)
                .build();

        // ！！！！临时配置需要在run之前设置！！！！
        // 临时配置开始
        // 暂时没有定的API接口，可以通过getInternalApi设置后使用
        // 设置唤醒参数后，初始化唤醒

        getInternalApi().setAsrMode(getAsrMode());
        getInternalApi().setAsrOffLineConfigProvider(asrOffLineConfigProvider);

        //唤醒功能
        if (Config.WAKEUP_ENABLE) {
            initWakeup(audioRecorder);
        }

        // 临时配置结束
        // dbp平台
        // getInternalApi().setDebugBot("f15be387-1348-b71b-2ae5-8f19f2375ea1");

        // 第二步：可以按需添加内置端能力和用户自定义端能力（需要继承BaseDeviceModule）
        // 屏幕展示
        IMessageSender messageSender = getInternalApi().getMessageSender();

        // 上屏
        screenDeviceModule = new ScreenDeviceModule(messageSender);
        screenDeviceModule.addScreenListener(screenListener);
        dcsSdk.putDeviceModule(screenDeviceModule);

        ScreenExtendDeviceModule screenExtendDeviceModule = new ScreenExtendDeviceModule(messageSender);
        screenExtendDeviceModule.addExtensionListener(mScreenExtensionListener);
        dcsSdk.putDeviceModule(screenExtendDeviceModule);

        // 打电话
        phoneCallDeviceModule = new PhoneCallDeviceModule(messageSender);
        initPhoneCallListener();
        phoneCallDeviceModule.addPhoneCallListener(phoneCallListener);
        dcsSdk.putDeviceModule(phoneCallDeviceModule);
        uploadContacts();

        // 发短信
//        initSms(messageSender);
        // AppLauncher
//        initAppLauncher(messageSender);
        // 设置
        deviceControlDeviceModule = new DeviceControlDeviceModule(messageSender);
//        initDeviceControlListener();
        deviceControlListener = new DeviceControlDeviceModule.SimpleDeviceControlListener(this);
        deviceControlDeviceModule.addDeviceControlListener(deviceControlListener);
        dcsSdk.putDeviceModule(deviceControlDeviceModule);
        // 本地闹钟
//        alarmsDeviceModule = new AlarmsDeviceModule(messageSender);
//        initAlarmListener();
//        alarmsDeviceModule.addAlarmListener(alarmListener);
//        dcsSdk.putDeviceModule(alarmsDeviceModule);
        // 联系人
        contactsDeviceModule = new ContactsDeviceModule(messageSender);
        initContactsListener();
        contactsDeviceModule.addContactsListener(contactsListener);
        dcsSdk.putDeviceModule(contactsDeviceModule);
//        uploadContacts();

        // 离线识别
        OffLineDeviceModule offLineDeviceModule = new OffLineDeviceModule(this.getApplicationContext());
        dcsSdk.putDeviceModule(offLineDeviceModule);

        // 设置闹钟播放源（实例代码）
//        InternalApi internalApi = ((DcsSdkImpl) dcsSdk).getInternalApi();
//        String namespace = com.baidu.duer.dcs.devicemodule.alerts.ApiConstants.NAMESPACE;
//        AlertsDeviceModule alertsDeviceModule = (AlertsDeviceModule) internalApi.getDeviceModule(namespace);
//        if (alertsDeviceModule != null) {
//            alertsDeviceModule.setAlarmSource("assets://ding.wav");
//        }

        // 在线返回文本的播报，eg:你好，返回你好的播报
        DialogRequestIdHandler dialogRequestIdHandler =
                ((DcsSdkImpl) dcsSdk).getProvider().getDialogRequestIdHandler();
        CustomUserInteractionDeviceModule customUserInteractionDeviceModule =
                new CustomUserInteractionDeviceModule(messageSender, dialogRequestIdHandler);
        dcsSdk.putDeviceModule(customUserInteractionDeviceModule);

        // 扩展自定义DeviceModule,eg...
        addOtherDeviceModule(dcsSdk, messageSender);

        audioPlayerDeviceModule = (AudioPlayerDeviceModule) getInternalApi().getDeviceModule(AUDIO_PLAYER_NAMESPACE);
    }

    private void initWakeup(BaseAudioRecorder audioRecorder) {
        // 唤醒
        final BaseWakeup wakeup = new KittWakeUpImpl(audioRecorder);
        // 百度语音团队的离线asr和百度语音团队的唤醒，2个so库冲突，暂时不要用WakeupImpl实现的唤醒功能！！
//        final BaseWakeup wakeup = new WakeupImpl();
        final IWakeupProvider wakeupProvider = new IWakeupProvider() {
            @Override
            public WakeUpConfig wakeUpConfig() {
                // 添加多唤醒词和索引
                // 此处传入的index需要和Snowboy唤醒模型文件一致
                // 例：模型文件中有3个唤醒词，分别为不同语速的"小度小度"，index分别为1-3，则需要按照以下格式添加
                // 唤醒成功后，回调中会包含被唤醒的WakeUpWord
                List<WakeUpWord> wakeupWordList = new ArrayList<WakeUpWord>();
                wakeupWordList.add(new WakeUpWord(1, "小度小度"));
                wakeupWordList.add(new WakeUpWord(2, "小度小度"));
                wakeupWordList.add(new WakeUpWord(3, "小度小度"));
                final List<String> umdlPaths = new ArrayList<String>();
                umdlPaths.add(WAKEUP_UMDL_PATH);
                return new WakeUpConfig.Builder()
                        .resPath(WAKEUP_RES_PATH)
                        .umdlPath(umdlPaths)
                        .sensitivity(WAKEUP_SENSITIVITY)
                        .highSensitivity(WAKEUP_HIGH_SENSITIVITY)
                        .wakeUpWords(wakeupWordList)
                        .build();
            }

            @Override
            public boolean enableWarning() {
                return ENABLE_PLAY_WARNING;
            }

            @Override
            public String warningSource() {
                // 每次在播放唤醒提示音前调用该方法
                // assets目录下的以assets://开头
                // sd文件为绝对路径
                return "assets://ding.wav";
            }

            @Override
            public float volume() {
                // 每次在播放唤醒提示音前调用该方法
                // [0-1]
                return 0.8f;
            }

            @Override
            public boolean wakeAlways() {
                return BaseDcsActivity.this.enableWakeUp();
            }

            @Override
            public BaseWakeup wakeupImpl() {
                return wakeup;
            }

            @Override
            public int audioType() {
                // 用户自定义类型
                return AudioManager.STREAM_SYSTEM;
            }
        };
        getInternalApi().setWakeupProvider(wakeupProvider);
        getInternalApi().initWakeUp();
    }

    protected void addOtherDeviceModule(IDcsSdk dcsSdk, IMessageSender messageSender) {

    }

    protected SdkConfigProvider getSdkConfigProvider() {
        return new DefaultSdkConfigProvider() {
            @Override
            public String clientId() {
                return CLIENT_ID;
            }

            @Override
            public int pid() {
                return 729;
            }
        };
    }

    private String mRenderPlayerInfoToken = null;
    private String mPlayToken = null;
    private ScreenExtendDeviceModule.IScreenExtensionListener mScreenExtensionListener = new ScreenExtendDeviceModule
            .IScreenExtensionListener() {


        @Override
        public void onRenderPlayerInfo(RenderPlayerInfoPayload renderPlayerInfoPayload) {
            // handleRenderPlayerInfoPayload(renderPlayerInfoPayload);
            Log.e(TAG, "onRenderPlayerInfo: content = " + renderPlayerInfoPayload.getContent());
        }

        @Override
        public void onRenderAudioList(RenderAudioListPlayload renderAudioListPlayload) {
            Log.e(TAG, "onRenderAudioList: title = " + renderAudioListPlayload.getTitle());
        }
    };

    protected void sdkRun() {
        // 第三步，将sdk跑起来
        Log.e(TAG, "sdkRun: ");
        isLogging = true;
        ((DcsSdkImpl) dcsSdk).getInternalApi().login(new ILoginListener() {
            @Override
            public void onSucceed(String accessToken) {
                dcsSdk.run();
                isLoginSucceed = true;
                isLogging = false;
                Log.e(TAG, "onSucceed() called with: accessToken = " + accessToken + "");
//                Toast.makeText(BaseDcsActivity.this.getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String errorMessage) {
                Log.e(TAG, "onFailed() called with: errorMessage = " + errorMessage + "");
                if (NetworkUtils.isConnected(BaseDcsActivity.this)) {
                    ToastUtils.showShort(BaseDcsActivity.this.getApplicationContext(), "登录失败, 请检查网络");
                } else {
                    ToastUtils.showShort(BaseDcsActivity.this.getApplicationContext(), getString(R.string.error_no_network3));
                }

                isLoginSucceed = false;
                isLogging = false;
//                finish();
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "onCancel() called");
                isLogging = false;
                isLoginSucceed = false;
                Toast.makeText(BaseDcsActivity.this.getApplicationContext(), "登录被取消", Toast.LENGTH_SHORT).show();
//                finish();
            }
        });
    }

    public InternalApi getInternalApi() {
        return ((DcsSdkImpl) dcsSdk).getInternalApi();
    }

    public DcsSdkImpl getDcsSdkImpl() {
        return (DcsSdkImpl) dcsSdk;
    }

    private void initWakeUpAgentListener() {
        IWakeupAgent wakeupAgent = getInternalApi().getWakeupAgent();
        if (wakeupAgent != null) {
            wakeupAgentListener = new IWakeupAgent.SimpleWakeUpAgentListener() {
                @Override
                public void onWakeupSucceed(WakeUpWord wakeUpWord) {
                    Toast.makeText(BaseDcsActivity.this,
                            "唤醒成功，唤醒词是：" + wakeUpWord.getWord(),
                            Toast.LENGTH_SHORT).show();
                }
            };
            wakeupAgent.addWakeupAgentListener(wakeupAgentListener);
        }
    }

    protected void beginVoiceRequest(final boolean vad) {
        Log.e(TAG, "beginVoiceRequest() called with: vad = " + vad + "");
        // 必须先调用cancel
        dcsSdk.getVoiceRequest().cancelVoiceRequest(new com.baidu.duer.dcs.api.IVoiceRequestListener() {
            @Override
            public void onSucceed() {
                resumeSpeakerState();
                dcsSdk.getVoiceRequest().beginVoiceRequest(vad);
//                dcsSdk.getVoiceRequest().beginVoiceRequest(false);
            }
        });
//        dcsSdk.getVoiceRequest().beginVoiceRequest(false);
//        cancelVoiceRequest();
    }

    private void addDirectiveReceivedListener() {
        getInternalApi().addDirectiveReceivedListener(directiveReceivedListener);
    }

    private void initFinishedDirectiveListener() {
        // 所有指令执行完毕的回调监听
        getInternalApi().addFinishedDirectiveListener(new IFinishedDirectiveListener() {
            @Override
            public void onFinishedDirective() {
                Log.d(TAG, "所有指令执行完毕");
            }
        });
    }

    private void uploadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "uploadContacts: no read contacts permission ");
            return;
        }
        try {
            String contactsJson = ContactsUtils.getAllContacts(this);
            Log.e(TAG, "uploadContacts: contactsJson = " + contactsJson);
            String lastContactsJson = UploadPreference.getLastUploadPhoneContacts(this);
            if (contactsJson != null && !contactsJson.equals(lastContactsJson)) {
                getInternalApi().getUpload().uploadPhoneContacts(contactsJson, false, new IUpload.IUploadListener() {
                    @Override
                    public void onSucceed(int i) {
                        Log.e(TAG, "uploadContacts onSucceed() called with: i = " + i + "");
                    }

                    @Override
                    public void onFailed() {
                        Log.e(TAG, "uploadContacts onFailed() called");
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initAppLauncher(IMessageSender messageSender) {
        appLauncherDeviceModule = new AppLauncherDeviceModule(messageSender, AppLauncherImpl.getInstance(this));
        initAppLauncherListener();
        appLauncherDeviceModule.addAppLauncherListener(appLauncherListener);
        dcsSdk.putDeviceModule(appLauncherDeviceModule);
    }

    private void initPhoneCallListener() {
        // 打电话指令监听
        phoneCallListener = new PhoneCallDeviceModule.IPhoneCallListener() {
            @Override
            public void onPhoneCallByName(PhonecallByNamePayload payload) {
                Log.e(TAG, "onPhoneCallByName: paylaod : " + payload.toString());
//                Toast.makeText(BaseDcsActivity.this, "打电话指令（按姓名）", Toast.LENGTH_LONG).show();
                List<CandidateCallee> list = payload.getCandidateCallees();
                if (list != null && list.size() > 0) {
                    Log.e(TAG, "onPhoneCallByName: size = " + list.size());
                    ContactsUtils.callByName(BaseDcsActivity.this, list.get(0).contactName);
                } else {
                    Log.e(TAG, "onPhoneCallByName: list = " + list);
                }
            }

            @Override
            public void onSelectCallee(SelectCalleePayload payload) {
                Log.e(TAG, "onSelectCallee() called with: payload = " + payload.toString() + "");
                ToastUtils.showShort(BaseDcsActivity.this, "不支持该功能");
//                Toast.makeText(BaseDcsActivity.this, "打电话指令（选择联系人）", Toast.LENGTH_LONG).show();
//                List<CandidateCalleeNumber> list = payload.getCandidateCallees();
//                if (list != null && list.size() > 0) {
//                    Log.e(TAG, "onSelectCallee: size = " + list.size());
//                    ContactsUtils.callByNumber(BaseDcsActivity.this, list.get(0).getPhoneNumber());
//                } else {
//                    Log.e(TAG, "onPhoneCallByName: list = " + list);
//                }
            }

            @Override
            public void onPhoneCallByNumber(PhonecallByNumberPayload payload) {
                Log.e(TAG, "onPhoneCallByNumber() called with: payload = " + payload.toString() + "");
                ToastUtils.showShort(BaseDcsActivity.this, "不支持该功能");
//                Toast.makeText(BaseDcsActivity.this, "打电话指令（按号码）", Toast.LENGTH_LONG).show();
//                CandidateCalleeNumber callee = payload.getCallee();
//                if (callee != null && !TextUtils.isEmpty(callee.getPhoneNumber())) {
//                    ContactsUtils.callByNumber(BaseDcsActivity.this, callee.getPhoneNumber());
//                } else {
//                    Log.e(TAG, "onPhoneCallByNumber: list = " + callee);
//                }
            }
        };
    }

    private void initAppLauncherListener() {
        // 打开应用指令监听
        appLauncherListener = new AppLauncherDeviceModule.IAppLauncherListener() {
            @Override
            public void onLaunchApp(LaunchAppPayload payload) {
                // 优先打开deepLink，然后是packageName、appName
                Log.e(TAG, "onLaunchApp: name = " + payload.getAppName() + ", package = " + payload.getPackageName());
                if (!TextUtils.isEmpty(payload.getDeepLink())) {
                    appLauncherDeviceModule.getAppLauncher().launchAppByDeepLink(BaseDcsActivity.this,
                            payload.getDeepLink());
                } else if (!TextUtils.isEmpty(payload.getPackageName())) {
                    appLauncherDeviceModule.getAppLauncher().launchAppByPackageName(BaseDcsActivity.this,
                            payload.getPackageName());
                } else if (!TextUtils.isEmpty(payload.getAppName())) {
                    appLauncherDeviceModule.getAppLauncher().launchAppByName(BaseDcsActivity.this,
                            payload.getAppName());
                }
            }
        };
    }

    private void initContactsListener() {
        // 联系人指令监听
        contactsListener = new ContactsDeviceModule.IContactsListener() {
            @Override
            public void onCreateContact(CreateContactPayload payload) {
//                Toast.makeText(BaseDcsActivity.this, "创建联系人指令", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSearchContact(SearchContactPayload payload) {
//                Toast.makeText(BaseDcsActivity.this, "查看联系人指令", Toast.LENGTH_LONG).show();
            }
        };
    }

    protected void endVoiceRequest() {
        Log.e(TAG, "endVoiceRequest: ");
        dcsSdk.getVoiceRequest().endVoiceRequest(new IVoiceRequestListener() {
            @Override
            public void onSucceed() {
                Log.e(TAG, "endVoiceRequest onSucceed: ");
            }
        });
    }

    protected void cancelVoiceRequest() {
        Log.e(TAG, "cancelVoiceRequest: ");
        dcsSdk.getVoiceRequest().cancelVoiceRequest(new IVoiceRequestListener() {
            @Override
            public void onSucceed() {
                Log.d(TAG, "cancelVoiceRequest onSucceed");
            }
        });
    }

    private int calculateVolume(byte[] buffer) {
        short[] audioData = new short[buffer.length / 2];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);
        double sum = 0;
        // 将 buffer 内容取出，进行平方和运算
        for (int i = 0; i < audioData.length; i++) {
            sum += audioData[i] * audioData[i];
        }
        // 平方和除以数据总长度，得到音量大小
        double mean = sum / (double) audioData.length;
        final double volume = 10 * Math.log10(mean);
        return (int) volume;
    }

    private void wakeUp() {
        try {
            getInternalApi().startWakeup();
        } catch (WakeUpException e) {
            e.printStackTrace();
            Log.e(TAG, "WakeUp not initialized!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        requestAudioFocus();
        // 恢复tts，音乐等有关播放。策略一，恢复状态，恢复声音。
//        resumeSpeaker();
        // 恢复tts，音乐等有关播放。策略二：只恢复状态，不恢复声音。
        //调用getInternalApi().stopSpeaker()会让其标志位为true。
        //恢复内部播放状态，如果该标志位为true（），则会导致没有声音。
//        getDcsSdkImpl().getFramework().multiChannelMediaPlayer.a(false);

        if (Config.WAKEUP_ENABLE) {
            wakeUp();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // 停止tts，音乐等有关播放.
//        getInternalApi().stopSpeaker();
        // 如果有唤醒，则停止唤醒
        if (Config.WAKEUP_ENABLE) {
            getInternalApi().stopWakeup(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
        release();
    }

    private void registerContentObserver() {
        if (mContractsObserver == null) {
            mContractsObserver = new ContractsObserver(new Handler(Looper.getMainLooper()));
        }
//        getContentResolver().registerContentObserver(ContactsContract.Data.CONTENT_URI, true, mContractsObserver);
        getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                true, mContractsObserver);
    }

    protected void release() {
        Log.e(TAG, "release: ");
        if (isReleased) {
            Log.e(TAG, "release: had release.");
            return;
        }
        isReleased = true;

        Log.e(TAG, "release: state = " + mMediaPlayer.getPlayState());
        mMediaPlayer.stop();
        mMediaPlayer.release();

        getContentResolver().unregisterContentObserver(mContractsObserver);
        abandonAudioFocus();

        stopSpeaker();

        sendExitEvent();
    }

    private void sendExitEvent() {
        Header header = new MessageIdHeader("ai.dueros.device_interface.system", "Exited");
        Event event = new Event(header, null);
        if (getInternalApi().getMessageSender() != null) {
            getInternalApi().getMessageSender().sendEvent(event, new IResponseListener() {
                @Override
                public void onSucceed(int i) {
                    Log.e(TAG, "sendExitEvent onSucceed: ");
                    releaseDcssdk();
                }

                @Override
                public void onFailed(String s) {
                    Log.e(TAG, "sendExitEvent onFailed: ");
                    releaseDcssdk();
                }

                @Override
                public void onCancel() {
                    Log.e(TAG, "sendExitEvent onCancel: ");
                    releaseDcssdk();
                }
            });
        } else {
            Log.e(TAG, "sendExitEvent: messageSender = null.");
        }
    }

    private void releaseDcssdk() {
        if (screenDeviceModule != null) {
            screenDeviceModule.removeScreenListener(screenListener);
        }
        screenListener = null;

        dcsSdk.getVoiceRequest().removeDialogStateListener(dialogStateListener);
        dialogStateListener = null;

        dcsSdk.removeConnectionStatusListener(connectionStatusListener);
        connectionStatusListener = null;

        getInternalApi().removeErrorListener(errorListener);
        errorListener = null;

        getInternalApi().removeRequestBodySentListener(dcsRequestBodySentListener);
        dcsRequestBodySentListener = null;

        getInternalApi().removeDirectiveReceivedListener(directiveReceivedListener);
        directiveReceivedListener = null;

        getInternalApi().setLocationHandler(null);
        locationHandler = null;
        location.release();

        deviceControlDeviceModule.removeDeviceControlListener(deviceControlListener);

        IWakeupAgent wakeupAgent = getInternalApi().getWakeupAgent();
        if (wakeupAgent != null) {
            wakeupAgent.removeWakeupAgentListener(wakeupAgentListener);
        }

        // 第3步，释放sdk
        dcsSdk.release();
    }

    protected void pauseOrPlayMusic() {
        Log.e(TAG, "pauseOrPlayMusic: isPlaying = " + isPlaying);
        if (currentDialogState != IDialogStateListener.DialogState.IDLE) {
            Log.e(TAG, "pauseOrPlayMusic: result, dialog state = " + currentDialogState);
            return;
        }

        if (TextUtils.isEmpty(mRenderPlayerInfoToken) || !mRenderPlayerInfoToken.equals(mPlayToken)) {
            if (isPlaying) {
                Log.e(TAG, "pauseOrPlayMusic: pause audio.");
                getInternalApi().sendCommandIssuedEvent(PlaybackControllerDeviceModule
                        .CommandIssued
                        .CommandIssuedPause);

            } else {
                Log.e(TAG, "pauseOrPlayMusic: play audio.");
                getInternalApi().sendCommandIssuedEvent(PlaybackControllerDeviceModule
                        .CommandIssued
                        .CommandIssuedPlay);
            }
            isPlaying = !isPlaying;
        } else {
            Log.e(TAG, "pauseOrPlayMusic: playPauseButtonClicked..");
            getInternalApi().postEvent(Form.playPauseButtonClicked(mRenderPlayerInfoToken), null);
        }
    }

    protected void stopMusic() {
        //防止频繁上发数据
        if (isPlaying) {
            Log.e(TAG, "stopMusic: pause audio player");
            getInternalApi().sendCommandIssuedEvent(PlaybackControllerDeviceModule.CommandIssued
                    .CommandIssuedPause);
            isPlaying = false;
        }
    }

    /**
     *
     */
    protected void clearAudioList() {
        DcsSdkImpl sdk = (DcsSdkImpl) dcsSdk;
        try {
            Field audioPlayerField = sdk.getClass().getDeclaredField("audioPlayerDeviceModule");
            audioPlayerField.setAccessible(true);
            Object object = audioPlayerField.get(sdk);
            Method method = object.getClass().getDeclaredMethod("clearAll");
            method.setAccessible(true);
            method.invoke(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过反射清除播放列表
     */
    protected void clearAudioList2() {
        Log.e(TAG, "clearAudioList2: isPlayingAudio = " + isPlayingAudio);
        if (clearAudioListMethod == null) {
            clearAudioListMethod = getClearAudioListMethod();
        }
        if (clearAudioListMethod != null) {
            try {
                Field field = audioPlayerDeviceModule.getClass().getDeclaredField("playQueue");
                field.setAccessible(true);
                LinkedList playQueue = (LinkedList) field.get(audioPlayerDeviceModule);
                if (playQueue != null) {
                    Log.e(TAG, "clearAudioList2: size = " + playQueue.size());
                }
                clearAudioListMethod.invoke(audioPlayerDeviceModule);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                Log.e(TAG, "clearAudioList2: e : " + e.toString());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "clearAudioList2: clearAudioListMethod = null.");
        }
    }

    protected Method getClearAudioListMethod() {
        if (audioPlayerDeviceModule == null) {
            Log.e(TAG, "clearAudioList2: audioPlayerDeviceModule = null.");
        }
        try {
            Method method = audioPlayerDeviceModule.getClass().getDeclaredMethod("clearAll");
            method.setAccessible(true);
            method.invoke(audioPlayerDeviceModule);
            return method;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "getClearAudioListMethod: get method fail.");
        return null;
    }

    protected void stopPlayerByToken(String token) {
        getInternalApi().postEvent(Form.playPauseButtonClicked(token), null);
    }

    private void stopSpeaker() {
        Log.e(TAG, "stopSpeaker: ");
        getInternalApi().stopSpeaker();
        isPausedSpeaker = true;
        isPlaying = false;
    }

    protected void pauseOrResumeSpeaker() {
        Log.e(TAG, "pauseOrResumeSpeaker: isPlayingAudio = " + isPlayingAudio);
        if (isSpeaking() || isPlayingAudio) {
            if (isPausedSpeaker) {
                resumeSpeakerStateAndSound();
            } else {
                stopSpeaker();
            }
        } else {
            Log.e(TAG, "pauseOrResumeSpeaker: do nothing, dialogState = " + currentDialogState);
        }
    }

    private void resumeSpeaker() {
        Log.e(TAG, "resumeSpeaker: isPauseSpeaker = " + isPausedSpeaker);
        if (Config.RESUME_SPEAKER_SOUND) {
            resumeSpeakerStateAndSound();
        } else {
            resumeSpeakerState();
        }
    }

    private void resumeSpeakerStateAndSound() {
        Log.e(TAG, "resumeSpeakerStateAndSound: isPausedSpeaker = " + isPausedSpeaker);
        if (isPausedSpeaker) {
            getInternalApi().resumeSpeaker();
            isPausedSpeaker = false;
        }
    }

    private void resumeSpeakerState() {
        Log.e(TAG, "resumeSpeakerState: isPauseSpeaker = " + isPausedSpeaker);
//        if (isPausedSpeaker) {
        getDcsSdkImpl().getFramework().multiChannelMediaPlayer.a(false);
//            isPausedSpeaker = false;
//        }
    }


    protected IOauth getOath() {
        return new SilentLoginImpl(CLIENT_ID);
    }

    /**
     * 是否启用唤醒
     *
     * @return
     */
    public abstract boolean enableWakeUp();

    /**
     * asr的识别类型-在线or离线
     *
     * @return
     */
    public abstract int getAsrMode();

    /**
     * 识别模式
     *
     * @return
     */
    public abstract AsrType getAsrType();

//    /**
//     * 登录模式
//     *
//     * @return
//     */
//    public abstract boolean isSilentLogin();

    /**
     * asr结果回调，比如显示界面
     *
     * @param payload asr结果
     */
    protected abstract void handleRenderVoiceInputTextPayload(RenderVoiceInputTextPayload payload);

    /**
     * 部分要显示的内容回调，如果收音超时
     *
     * @param payload 要显示的文本
     */
    protected abstract void handleRenderCard(RenderCardPayload payload);

    /**
     * 界面显示，html链接，需要webView显示。
     *
     * @param htmlPayload 数据对象
     */
    protected abstract void handleHtmlPayload(HtmlPayload htmlPayload);

    /**
     * 媒体播放状态改变，停止播放
     */
    protected abstract void handlePlaybackStopped();

    /**
     * 暂停播放
     */
    protected abstract void handlePlaybackPause();

    /**
     * 开始播放
     */
    protected abstract void handlePlaybackStarted();

    protected void addErrorListener(IErrorListener listener) {
        if (errorListener != null) {
            getInternalApi().removeErrorListener(errorListener);
            errorListener = null;
        }
        this.errorListener = listener;
        getInternalApi().addErrorListener(listener);
    }

    protected boolean isConnected() {
        return connectionStatus == IConnectionStatusListener.ConnectionStatus.CONNECTED;
    }

    protected boolean isListening() {
        return currentDialogState == IDialogStateListener.DialogState.LISTENING;
    }

    protected boolean isSpeaking() {
        return currentDialogState == IDialogStateListener.DialogState.SPEAKING;
    }

    private int requestAudioFocus() {
        if (hadAudioFocus) {
            Log.e(TAG, "requestAudioFocus: had request audio focus.");
            return AudioManager.AUDIOFOCUS_GAIN;
        }
        if (mAudioListener == null) {
            mAudioListener = new AudioFocusChangeListener();
        }
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(mAudioListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_GAIN) {
            hadAudioFocus = true;
        } else {
            Log.e(TAG, "requestAudioFocus: can not request audio focus, result = " + result);
        }
        return result;
    }

    private void abandonAudioFocus() {
        if (!hadAudioFocus && mAudioListener == null) {
            return;
        }

        AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        manager.abandonAudioFocus(mAudioListener);
        mAudioListener = null;
        hadAudioFocus = false;
    }

    private class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.e(TAG, "onAudioFocusChange: focusChange = " + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    //-1
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    //-2
                    Log.e(TAG, "onAudioFocusChange: audio focus loss");
                    mPausedByTransientLossOfFocus = false;
                    hadAudioFocus = false;
                    stopSpeaker();
                    mMediaPlayer.pause();
//                    stopMusic();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    //-3
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    //1
                    //用于恢复播放处理。
                    Log.e(TAG, "onAudioFocusChange: audio focus gain");
                    if (mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                    }
//                    resumeSpeaker();
                    break;
                default:
                    Log.e(TAG, "onAudioFocusChange: Unknown audio focus change code, focusChange = " + focusChange);
            }
        }
    }

    private class ContractsObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ContractsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.e(TAG, "onChange() called with: selfChange = " + selfChange + ", uri = " + uri + "");
            uploadContacts();
        }
    }
}
