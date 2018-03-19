package com.readboy.watch.speech;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.duer.dcs.androidsystemimpl.AudioRecordImpl;
import com.baidu.duer.dcs.androidsystemimpl.wakeup.kitt.KittWakeUpImpl;
import com.baidu.duer.dcs.api.DcsSdkBuilder;
import com.baidu.duer.dcs.api.IConnectionStatusListener;
import com.baidu.duer.dcs.api.IDcsSdk;
import com.baidu.duer.dcs.api.IDialogStateListener;
import com.baidu.duer.dcs.devicemodule.playbackcontroller.PlaybackControllerDeviceModule;
import com.baidu.duer.dcs.framework.DcsSdkImpl;
import com.baidu.duer.dcs.framework.HttpProxy;
import com.baidu.duer.dcs.framework.ILoginListener;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.InternalApi;
import com.baidu.duer.dcs.framework.internalapi.IASROffLineConfigProvider;
import com.baidu.duer.dcs.framework.internalapi.IDcsRequestBodySentListener;
import com.baidu.duer.dcs.framework.internalapi.IErrorListener;
import com.baidu.duer.dcs.framework.internalapi.IWakeupAgent;
import com.baidu.duer.dcs.framework.internalapi.IWakeupProvider;
import com.baidu.duer.dcs.framework.location.Location;
import com.baidu.duer.dcs.framework.message.DcsRequestBody;
import com.baidu.duer.dcs.framework.upload.contact.IUpload;
import com.baidu.duer.dcs.framework.upload.contact.UploadPreference;
import com.baidu.duer.dcs.oauth.api.credentials.BaiduOauthClientCredentialsImpl;
import com.baidu.duer.dcs.oauth.api.grant.BaiduOauthImplicitGrantIml;
import com.baidu.duer.dcs.offline.asr.bean.ASROffLineConfig;
import com.baidu.duer.dcs.sample.sdk.devicemodule.alarms.AlarmsDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.alarms.message.SetAlarmPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.alarms.message.SetTimerPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.alarms.message.ShowAlarmsPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.alarms.message.ShowTimersPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.applauncher.AppLauncherDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.applauncher.AppLauncherImpl;
import com.baidu.duer.dcs.sample.sdk.devicemodule.applauncher.message.LaunchAppPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.contacts.ContactsDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.contacts.message.CreateContactPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.contacts.message.SearchContactPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.DeviceControlDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.AdjustBrightnessPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetAssistiveTouchPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetBluetoothPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetBrightnessPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetCellularModePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetCellularPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetGpsPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetHotspotPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetNfcPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetPhoneModePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetPhonePowerPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetPortraitLockPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetSynchronizationPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetVibrationPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetVpnPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message.SetWifiPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.LocalAudioPlayerDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.message.NextPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.message.PausePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.message.PlayPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.message.PreviousPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.message.SearchAndPlayMusicPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.message.SearchAndPlayRadioPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.message.SearchAndPlayUnicastPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.message.SetPlaybackModePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.message.StopPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.offlineasr.OffLineDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.PhoneCallDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.CandidateCallee;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.CandidateCalleeNumber;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.PhonecallByNamePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.PhonecallByNumberPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.SelectCalleePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.ScreenDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.HtmlPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.RenderCardPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.RenderHintPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message.RenderVoiceInputTextPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.sms.SmsDeviceModule;
import com.baidu.duer.dcs.sample.sdk.devicemodule.sms.message.SelectRecipientPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.sms.message.SendSmsByNamePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.sms.message.SendSmsByNumberPayload;
import com.baidu.duer.dcs.sample.sdk.location.ILocation;
import com.baidu.duer.dcs.sample.sdk.location.LocationImpl;
import com.baidu.duer.dcs.sample.sdk.util.ContactsUtils;
import com.baidu.duer.dcs.sample.sdk.widget.DcsWebView;
import com.baidu.duer.dcs.systeminterface.BaseAudioRecorder;
import com.baidu.duer.dcs.systeminterface.BaseWakeup;
import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.systeminterface.IOauth;
import com.baidu.duer.dcs.util.ContactsChoiceUtil;
import com.readboy.watch.speech.util.NetworkUtils;
import com.readboy.watch.speech.util.ToastUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author oubin
 * @date 2018/2/28
 */

public abstract class BaseDcsActivity extends Activity {
    public static final String TAG = "DCS-BaseDcsActivity";
    /**
     * 正式client_id
     */
    private static final String CLIENT_ID = "WwhkTgFmkKC3jYgATjV6KESf9ZfeSwti";

    private static final String CLIENT_SECRET = "OjmUyrFKnjqvyn2gb8i6rbrwUxDXlo55";

    /**
     * 唤醒词,可以改为你自己的唤醒词,比如："茄子"
     */
    private static final String WAKEUP_WORD = "小度小度";
    /**
     * 唤醒成功后是否需要播放提示音
     */
    private static final boolean ENABLE_PLAY_WARNING = true;
    private static final int REQUEST_CODE = 123;
    protected IDcsSdk dcsSdk;
    protected ScreenDeviceModule screenDeviceModule;

    private ILocation location;
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
            return Location.EGeoCoordinateSystem.WGS84;
        }
    };
    /**
     * for dcs统计-demo
     */
    private long duerResultT;
    protected boolean isPlaying;
    protected boolean isStopListenReceiving;
    protected boolean isLoginSucceed;

    private IWakeupAgent.IWakeupAgentListener wakeupAgentListener;
    protected IDialogStateListener dialogStateListener;
    private PhoneCallDeviceModule.IPhoneCallListener phoneCallListener;
    private SmsDeviceModule.ISmsListener smsListener;
    private AppLauncherDeviceModule.IAppLauncherListener appLauncherListener;
    private LocalAudioPlayerDeviceModule.ILocalAudioPlayerListener localAudioPlayerListener;
    private DeviceControlDeviceModule.IDeviceControlListener deviceControlListener;
    private AlarmsDeviceModule.IAlarmDirectiveListener alarmListener;
    private ContactsDeviceModule.IContactsListener contactsListener;
    private PhoneCallDeviceModule phoneCallDeviceModule;
    private SmsDeviceModule smsDeviceModule;
    private AppLauncherDeviceModule appLauncherDeviceModule;
    private DeviceControlDeviceModule deviceControlDeviceModule;
    private AlarmsDeviceModule alarmsDeviceModule;
    private LocalAudioPlayerDeviceModule localAudioPlayerDeviceModule;
    private ContactsDeviceModule contactsDeviceModule;
    private ScreenDeviceModule.IScreenListener screenListener = new ScreenDeviceModule.IScreenListener() {
        @Override
        public void onRenderVoiceInputText(RenderVoiceInputTextPayload payload) {
            Log.e(TAG, "onRenderVoiceInputText() called with: header payload = " + payload.toString() + "");
            handleRenderVoiceInputTextPayload(payload);
        }

        @Override
        public void onHtmlPayload(HtmlPayload htmlPayload, int id) {
            Log.e(TAG, "onHtmlPayload() called with: htmlPayload = " + htmlPayload + ", id = " + id + "");
            handleHtmlPayload(htmlPayload);
        }

        @Override
        public void onRenderCard(RenderCardPayload renderCardPayload, int id) {
            Log.e(TAG, "onRenderCard: title = " + renderCardPayload.title +
                    ", content = " + renderCardPayload.content);
            handleRenderCard(renderCardPayload);
        }

        @Override
        public void onRenderHint(RenderHintPayload renderHintPayload, int id) {
            Log.e(TAG, "onRenderHint: " + Arrays.toString(renderHintPayload.cueWords.toArray()));
        }
    };

    private IDcsRequestBodySentListener dcsRequestBodySentListener = new IDcsRequestBodySentListener() {
        @Override
        public void onDcsRequestBody(DcsRequestBody dcsRequestBody) {
            String eventName = dcsRequestBody.getEvent().getHeader().getName();
            Log.v(TAG, "eventName:" + eventName);
            if (PlaybackEvent.PLAYBACK_STOPPED.equals(eventName)
                    || PlaybackEvent.PLAYBACK_FINISHED.equals(eventName)) {
                handlePlaybackStopped();
                isPlaying = false;
            } else if (PlaybackEvent.PLAYBACK_PAUSED.equals(eventName)) {
                handlePlaybackPause();
                isPlaying = false;
            } else if (PlaybackEvent.PLAYBACK_STARTED.equals(eventName)
                    || PlaybackEvent.PLAYBACK_RESUMED.equals(eventName)) {
                handlePlaybackStarted();
                isPlaying = true;
            }
        }
    };
    private IErrorListener errorListener = new IErrorListener() {
        @Override
        public void onErrorCode(ErrorCode errorCode) {
            Log.d(TAG, "onErrorCode:" + errorCode);
            if (errorCode == ErrorCode.VOICE_REQUEST_FAILED) {
                Toast.makeText(BaseDcsActivity.this,
                        getResources().getString(R.string.voice_err_msg),
                        Toast.LENGTH_SHORT)
                        .show();
            } else if (errorCode == ErrorCode.NETWORK_UNAVIABLE) {
                //  网络不可用
                Toast.makeText(BaseDcsActivity.this,
                        "网络不可用",
                        Toast.LENGTH_SHORT)
                        .show();
            } else if (errorCode == ErrorCode.LOGIN_FAILED) {
                // 未登录
                if (NetworkUtils.isConnected(BaseDcsActivity.this)) {
                    Toast.makeText(BaseDcsActivity.this,
                            "未登录",
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    ToastUtils.showShort(BaseDcsActivity.this, getString(R.string.error_no_network3));
                }
            }
        }
    };
    private IConnectionStatusListener connectionStatusListener = new IConnectionStatusListener() {
        @Override
        public void onConnectStatus(ConnectionStatus connectionStatus) {
            Log.e(TAG, "onConnectionStatusChange: " + connectionStatus);
            switch (connectionStatus) {
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (RequestPermissionsActivity.startPermissionActivity(this)) {
//            finish();
//        }
        initPermission();

        initSdk();
        sdkRun();

        // 设置各种监听器
        dcsSdk.addConnectionStatusListener(connectionStatusListener);
        // 指令接受
        // 错误
        getInternalApi().addErrorListener(errorListener);
        // event发送
        getInternalApi().addRequestBodySentListener(dcsRequestBodySentListener);
        // 需要定位后赋值，目前是写死的北京的
        getInternalApi().setLocationHandler(locationHandler);
        // 唤醒
//        initWakeUpListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
        // 恢复tts，音乐等有关播放
//        getInternalApi().resumeSpeaker();
        // 如果有唤醒，则恢复唤醒
//        getInternalApi().startWakeup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // 停止tts，音乐等有关播放.
//        getInternalApi().pauseSpeaker();
        // 如果有唤醒，则停止唤醒
//        getInternalApi().stopWakeup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
        // dcsWebView

        getInternalApi().pauseSpeaker();

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

        getInternalApi().setLocationHandler(null);
        locationHandler = null;

        IWakeupAgent wakeupAgent = getInternalApi().getWakeupAgent();
        if (wakeupAgent != null) {
            wakeupAgent.removeWakeupAgentListener(wakeupAgentListener);
        }

        // 第3步，释放sdk
        dcsSdk.release();

    }

    public InternalApi getInternalApi() {
        return ((DcsSdkImpl) dcsSdk).getInternalApi();
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
        audioRecorder.addRecorderListener(new BaseAudioRecorder.SimpleRecorderListener() {
            @Override
            public void onData(byte[] data) {
//                int volume = calculateVolume(data);
//                Log.d(TAG, "onVolumeChange:" + volume);
            }
        });
        IOauth oauth = getOath();

//        IOauth oauth = new DlpOauth(this);

        // 离线识别配置
        final ASROffLineConfig asrOffLineConfig = new ASROffLineConfig();
        // 应用的AppId
        asrOffLineConfig.asrAppId = "9889421";
        // 应用的AppKey
        asrOffLineConfig.asrAppKey = "BPAivB6HFuThw4o6Wm60xN0N";
        // 应用的SecretKey
        asrOffLineConfig.asrSecretKey = "78de6d7293de9f8421ef1e1880f0b702";
        // 设置离线grammer语法文件位置
        // 离线grammer的内容为Url编码
        asrOffLineConfig.grammerPath = "assets:///baidu_speech_grammar.bsg";
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
        HttpProxy httpProxy = new HttpProxy("172.24.184.92", 8888);

        // 定位
        location = new LocationImpl(getApplicationContext());
        location.requestLocation(false);

        // 构造dcs sdk
        DcsSdkBuilder builder = new DcsSdkBuilder();
        dcsSdk = builder.withClientId(CLIENT_ID)
                .withOauth(oauth)
                .withAudioRecorder(audioRecorder)
                // 设置音乐播放器的实现，MediaPlayerImpl为内部sdk的实现
//                 .withMediaPlayer(new MediaPlayerImpl())
                // .withHttpProxy(turbonetHttpProxy)
                .build();

        // ！！！！临时配置需要在run之前设置！！！！
        // 临时配置开始
        // 暂时没有定的API接口，可以通过getInternalApi设置后使用
        getInternalApi().setDebug(true);
        getInternalApi().setAsrMode(getAsrMode());
        getInternalApi().setAsrOffLineConfigProvider(asrOffLineConfigProvider);

        //唤醒功能
//        initWakeup();

        // 临时配置结束

        // 第二步：可以按需添加内置端能力和用户自定义端能力（需要继承BaseDeviceModule）
        // 屏幕展示
        IMessageSender messageSender = getInternalApi().getMessageSender();
        screenDeviceModule = new ScreenDeviceModule(messageSender);
        screenDeviceModule.addScreenListener(screenListener);
        dcsSdk.putDeviceModule(screenDeviceModule);
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
        initDeviceControlListener();
        deviceControlDeviceModule.addDeviceControlListener(deviceControlListener);
        dcsSdk.putDeviceModule(deviceControlDeviceModule);
        // 本地闹钟
//        alarmsDeviceModule = new AlarmsDeviceModule(messageSender);
//        initAlarmListener();
//        alarmsDeviceModule.addAlarmListener(alarmListener);
//        dcsSdk.putDeviceModule(alarmsDeviceModule);
        // 本地音乐
//        localAudioPlayerDeviceModule = new LocalAudioPlayerDeviceModule(messageSender);
//        initLocalAudioPlayerListener();
//        localAudioPlayerDeviceModule.addLocalAudioPlayerListener(localAudioPlayerListener);
//        dcsSdk.putDeviceModule(localAudioPlayerDeviceModule);
        // 联系人
        contactsDeviceModule = new ContactsDeviceModule(messageSender);
        initContactsListener();
        contactsDeviceModule.addContactsListener(contactsListener);
        dcsSdk.putDeviceModule(contactsDeviceModule);
//        uploadContacts();

        // 离线识别
//        OffLineDeviceModule offLineDeviceModule = new OffLineDeviceModule();
//        dcsSdk.putDeviceModule(offLineDeviceModule);

        // 设置闹钟播放源（实例代码）
//        InternalApi internalApi = ((DcsSdkImpl) dcsSdk).getInternalApi();
//        String namespace = com.baidu.duer.dcs.devicemodule.alerts.ApiConstants.NAMESPACE;
//        AlertsDeviceModule alertsDeviceModule = (AlertsDeviceModule) internalApi.getDeviceModule(namespace);
//        if (alertsDeviceModule != null) {
//            alertsDeviceModule.setAlarmSource("assets://ding.wav");
//        }

        addOtherDeviceModule(dcsSdk, messageSender);

    }

    private void uploadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "uploadContacts: no read contacts permission ");
            return;
        }
        Log.e(TAG, "uploadContacts: ");
        try {
            String contactsJson = ContactsChoiceUtil.getAllContacts(this);
            Log.e(TAG, "initSdk: contactsJson = " + contactsJson);
            String lastContactsJson = UploadPreference.getLastUploadPhoneContacts(this);
            if (contactsJson != null && !contactsJson.equals(lastContactsJson)) {
                ((DcsSdkImpl) dcsSdk).getUpload().uploadPhoneContacts(this, contactsJson, new IUpload.IUploadListener() {
                    @Override
                    public void onSucceed(int i) {
                        Log.e(TAG, "onSucceed() called with: i = " + i + "");
                    }

                    @Override
                    public void onFailed() {
                        Log.e(TAG, "onFailed() called");
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initSms(IMessageSender messageSender) {
        smsDeviceModule = new SmsDeviceModule(messageSender);
//        initSmsListener();
        smsDeviceModule.addSmsListener(smsListener);
        dcsSdk.putDeviceModule(smsDeviceModule);
    }

    private void initAppLauncher(IMessageSender messageSender) {
        appLauncherDeviceModule = new AppLauncherDeviceModule(messageSender, new AppLauncherImpl(this));
        initAppLauncherListener();
        appLauncherDeviceModule.addAppLauncherListener(appLauncherListener);
        dcsSdk.putDeviceModule(appLauncherDeviceModule);
    }

    private void initWakeup() {
        // 唤醒
        final BaseWakeup wakeup = new KittWakeUpImpl();
        // 百度语音团队的离线asr和百度语音团队的唤醒，2个so库冲突，暂时不要用WakeupImpl实现的唤醒功能！！
//        final BaseWakeup wakeup = new WakeupImpl();
        IWakeupProvider wakeupProvider = new IWakeupProvider() {
            @Override
            public String wakeupWords() {
                return WAKEUP_WORD;
            }

            @Override
            public boolean enableWarning() {
                return ENABLE_PLAY_WARNING;
            }

            @Override
            public String warningSource() {
                // assets目录下的以assets://开头
                // sd文件为绝对路径
                return "assets://ding.wav";
            }

            @Override
            public float volume() {
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
        };
        getInternalApi().setWakeupProvider(wakeupProvider);
    }

    private void initWakeUpListener() {
        IWakeupAgent wakeupAgent = getInternalApi().getWakeupAgent();
        if (wakeupAgent != null) {
            wakeupAgentListener = new IWakeupAgent.IWakeupAgentListener() {
                @Override
                public void onWakeupSucceed() {
                    Toast.makeText(BaseDcsActivity.this, "唤醒成功",
                            Toast.LENGTH_SHORT)
                            .show();
                }

                @Override
                public void onWarningCompleted() {

                }
            };
            wakeupAgent.addWakeupAgentListener(wakeupAgentListener);
        }
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
                    ContactsUtils.callByName(BaseDcsActivity.this, list.get(0).contactName);
                } else {
                    Log.e(TAG, "onPhoneCallByName: list = " + list);
                }
            }

            @Override
            public void onSelectCallee(SelectCalleePayload payload) {
                Log.e(TAG, "onSelectCallee() called with: payload = " + payload.toString() + "");
                Toast.makeText(BaseDcsActivity.this, "打电话指令（选择联系人）", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPhoneCallByNumber(PhonecallByNumberPayload payload) {
                Log.e(TAG, "onPhoneCallByNumber() called with: payload = " + payload.toString() + "");
                Toast.makeText(BaseDcsActivity.this, "打电话指令（按号码）", Toast.LENGTH_LONG).show();
                CandidateCalleeNumber callee = payload.getCallee();
                if (callee != null && !TextUtils.isEmpty(callee.getPhoneNumber())) {
                    ContactsUtils.callByName(BaseDcsActivity.this, callee.getPhoneNumber());
                } else {
                    Log.e(TAG, "onPhoneCallByName: list = " + callee);
                }
            }
        };
    }

    private void initAppLauncherListener() {
        // 打开应用指令监听
        appLauncherListener = new AppLauncherDeviceModule.IAppLauncherListener() {
            @Override
            public void onLaunchApp(LaunchAppPayload payload) {
                // 优先打开deepLink，然后是packageName、appName
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

    private void initDeviceControlListener() {
        // 设备控制指令监听
        deviceControlListener = new DeviceControlDeviceModule.IDeviceControlListener() {
            @Override
            public void onAdjustBrightness(AdjustBrightnessPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "调节亮度", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetAssistiveTouch(SetAssistiveTouchPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置悬浮球", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetBluetooth(SetBluetoothPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置蓝牙", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetBrightness(SetBrightnessPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置亮度", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetCellular(SetCellularPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置移动数据", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetCellularMode(SetCellularModePayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置移动数据模式", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetGps(SetGpsPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置GPS", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetHotspot(SetHotspotPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "打电话指令（按姓名）", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetNfc(SetNfcPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置NFC", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetPhoneMode(SetPhoneModePayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置情景模式", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetPhonePower(SetPhonePowerPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "关机/重启", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetPortraitLock(SetPortraitLockPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置屏幕旋转", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetSynchronization(SetSynchronizationPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置同步", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetVibration(SetVibrationPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置震动", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetVpn(SetVpnPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置VPN", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetWifi(SetWifiPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "设置Wifi", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void initAlarmListener() {
        // 闹钟、定制器指令监听
        alarmListener = new AlarmsDeviceModule.IAlarmDirectiveListener() {
            @Override
            public void onSetAlarmDirectiveReceived(SetAlarmPayload setAlarmPayload) {
                Toast.makeText(BaseDcsActivity.this, "设置闹钟指令", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onShowAlarmsDirectiveReceived(ShowAlarmsPayload showAlarmsPayload) {
                Toast.makeText(BaseDcsActivity.this, "查看闹钟指令", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetTimerDirectiveReceived(SetTimerPayload setTimerPayload) {
                Toast.makeText(BaseDcsActivity.this, "设置定时器指令", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onShowTimersDirectiveReceived(ShowTimersPayload showTimersPayload) {
                Toast.makeText(BaseDcsActivity.this, "查看定时器指令", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void initContactsListener() {
        // 联系人指令监听
        contactsListener = new ContactsDeviceModule.IContactsListener() {
            @Override
            public void onCreateContact(CreateContactPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "创建联系人指令", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSearchContact(SearchContactPayload payload) {
                Toast.makeText(BaseDcsActivity.this, "查看联系人指令", Toast.LENGTH_LONG).show();
            }
        };
    }

    protected void addOtherDeviceModule(IDcsSdk dcsSdk, IMessageSender messageSender) {

    }

    protected void pauseOrPlayMusic() {
        Log.e(TAG, "pauseOrPlayMusic: isPlaying = " + isPlaying);
        if (isPlaying) {
            getInternalApi().sendCommandIssuedEvent(PlaybackControllerDeviceModule.CommandIssued
                    .CommandIssuedPause);
        } else {
            getInternalApi().sendCommandIssuedEvent(PlaybackControllerDeviceModule.CommandIssued
                    .CommandIssuedPlay);
        }
        isPlaying = !isPlaying;
    }

    protected void stopMusic() {
//        if (isPlaying){
        getInternalApi().sendCommandIssuedEvent(PlaybackControllerDeviceModule.CommandIssued
                .CommandIssuedPause);
        isPlaying = false;
//        }
    }

    protected void sdkRun() {
        // 第三步，将sdk跑起来
        Log.e(TAG, "sdkRun: ");
        ((DcsSdkImpl) dcsSdk).getInternalApi().login(new ILoginListener() {
            @Override
            public void onSucceed(String accessToken) {
                dcsSdk.run();
                isLoginSucceed = true;
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
                finish();
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "onCancel() called");
                Toast.makeText(BaseDcsActivity.this.getApplicationContext(), "登录被取消", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    protected IOauth getOath() {
        if (isSilentLogin()) {
            return new BaiduOauthClientCredentialsImpl(CLIENT_ID, CLIENT_SECRET);
        } else {
            return new BaiduOauthImplicitGrantIml(CLIENT_ID, this);
        }
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
    public abstract int getAsrType();

    /**
     * 登录模式
     *
     * @return
     */
    public abstract boolean isSilentLogin();

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

}
