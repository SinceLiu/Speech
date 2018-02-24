/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.baidu.duer.dcs.sample.sdk;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.duer.dcs.androidsystemimpl.AudioRecordImpl;
import com.baidu.duer.dcs.androidsystemimpl.wakeup.kitt.KittWakeUpImpl;
import com.baidu.duer.dcs.api.DcsSdkBuilder;
import com.baidu.duer.dcs.api.IConnectionStatusListener;
import com.baidu.duer.dcs.api.IDcsSdk;
import com.baidu.duer.dcs.api.IDialogStateListener;
import com.baidu.duer.dcs.devicemodule.playbackcontroller.PlaybackControllerDeviceModule;
import com.baidu.duer.dcs.framework.DcsSdkImpl;
import com.baidu.duer.dcs.framework.ILoginListener;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.InternalApi;
import com.baidu.duer.dcs.framework.HttpProxy;
import com.baidu.duer.dcs.framework.internalapi.DcsConfig;
import com.baidu.duer.dcs.framework.internalapi.IASROffLineConfigProvider;
import com.baidu.duer.dcs.framework.internalapi.IDcsRequestBodySentListener;
import com.baidu.duer.dcs.framework.internalapi.IErrorListener;
import com.baidu.duer.dcs.framework.internalapi.IWakeupAgent;
import com.baidu.duer.dcs.framework.internalapi.IWakeupProvider;
import com.baidu.duer.dcs.framework.location.Location;
import com.baidu.duer.dcs.framework.message.DcsRequestBody;
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
import com.baidu.duer.dcs.sample.sdk.widget.DcsWebView;
import com.baidu.duer.dcs.statistics.DCSStatistics;
import com.baidu.duer.dcs.systeminterface.BaseAudioRecorder;
import com.baidu.duer.dcs.systeminterface.BaseWakeup;
import com.baidu.duer.dcs.systeminterface.IOauth;
import com.baidu.duer.dcs.util.CommonUtil;
import com.baidu.duer.dcs.util.FileUtil;
import com.baidu.duer.dcs.util.NetWorkUtil;
import com.readboy.watch.speech.R;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 录音权限问题，需要自己处理
 */
public abstract class SDKBaseActivity extends Activity implements
        View.OnClickListener {
    public static final String TAG = "DCS-SDK";
    //原始id
//    private static final String CLIENT_ID = "d8ITlI9aeTPaGcxKKsZit8tq";
    //音响场景，手表id
//    private static final String CLIENT_ID = "sbfBsm3sYk1TKcC5ZuDqQXOav0ynMVG2";
    /**
     * 正式client_id
     */
    private static final String CLIENT_ID = "WwhkTgFmkKC3jYgATjV6KESf9ZfeSwti";

    //    private static final String CLIENT_ID = "mjUEvj9wH88FE4Ou2AE0YTnZ9ERcsslY"; // 电视ClientId

    //初始client_secret
//    private static final String CLIENT_SECRET = "497033453de2c5dfb33c0dff4a59fe36";
    private static final String CLIENT_SECRET = "OjmUyrFKnjqvyn2gb8i6rbrwUxDXlo55";

    // 唤醒词,可以改为你自己的唤醒词,比如："茄子"
    private static final String WAKEUP_WORD = "小度小度";
    // 唤醒成功后是否需要播放提示音
    private static final boolean ENABLE_PLAY_WARNING = true;
    private static final int REQUEST_CODE = 123;
    protected EditText textInput;
    protected Button sendButton;
    protected IDcsSdk dcsSdk;
    protected ScreenDeviceModule screenDeviceModule;
    private Button nextButton;
    private Button preButton;
    private Button playButton;
    private Button voiceButton;
    private Button cancelVoiceButton;
    private boolean isPlaying;
    private Button volumeBtn;
    private boolean isStopListenReceiving;
    private TextView textViewWakeUpTip;
    private LinearLayout mTopLinearLayout;
    private DcsWebView dcsWebView;
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
    // for dcs统计-demo
    private long duerResultT;
    /**
     * for dcs统计-demo
     */
    private TextView textViewRenderVoiceInputText;
    private IWakeupAgent.IWakeupAgentListener wakeupAgentListener;
    private IDialogStateListener dialogStateListener;
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
            Log.e(TAG, "onRenderVoiceInputText() called with: payload = " + payload.toString() + "");
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
            if (eventName.equals("PlaybackStopped") || eventName.equals("PlaybackFinished")) {
                playButton.setText("等待音乐");
                isPlaying = false;
            } else if (eventName.equals("PlaybackPaused")) {
                playButton.setText("暂停中");
                isPlaying = false;
            } else if (eventName.equals("PlaybackStarted") || eventName.equals("PlaybackResumed")) {
                playButton.setText("播放中...");
                isPlaying = true;
            }
        }
    };
    private IErrorListener errorListener = new IErrorListener() {
        @Override
        public void onErrorCode(ErrorCode errorCode) {
            Log.d(TAG, "onErrorCode:" + errorCode);
            if (errorCode == ErrorCode.VOICE_REQUEST_FAILED) {
                Toast.makeText(SDKBaseActivity.this,
                        getResources().getString(R.string.voice_err_msg),
                        Toast.LENGTH_SHORT)
                        .show();
            } else if (errorCode == ErrorCode.NETWORK_UNAVIABLE) {
                //  网络不可用
                Toast.makeText(SDKBaseActivity.this,
                        "网络不可用",
                        Toast.LENGTH_SHORT)
                        .show();
            } else if (errorCode == ErrorCode.LOGIN_FAILED) {
                // 未登录
                Toast.makeText(SDKBaseActivity.this,
                        "未登录",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    };
    private IConnectionStatusListener connectionStatusListener = new IConnectionStatusListener() {
        @Override
        public void onConnectStatus(ConnectionStatus connectionStatus) {
            Log.d(TAG, "onConnectionStatusChange: " + connectionStatus);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdk_main);
        initViews();

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
        initWakeUpListener();
        // 对话
        initVoiceRequestListener();
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ArrayList<String> toApplyList = new ArrayList<>();
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
            }
        }
        if (!toApplyList.isEmpty()) {
            String tmpList[] = new String[toApplyList.size()];
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。

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
                return SDKBaseActivity.this.enableWakeUp();
            }

            @Override
            public BaseWakeup wakeupImpl() {
                return wakeup;
            }
        };

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
                // .withMediaPlayer(new MediaPlayerImpl())
                // .withHttpProxy(turbonetHttpProxy)
                .build();

        // ！！！！临时配置需要在run之前设置！！！！
        // 临时配置开始
        // 暂时没有定的API接口，可以通过getInternalApi设置后使用
        getInternalApi().setDebug(true);
        getInternalApi().setWakeupProvider(wakeupProvider);
        getInternalApi().setAsrMode(getAsrMode());
        getInternalApi().setAsrOffLineConfigProvider(asrOffLineConfigProvider);
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
        // 发短信
        smsDeviceModule = new SmsDeviceModule(messageSender);
        initSmsListener();
        smsDeviceModule.addSmsListener(smsListener);
        dcsSdk.putDeviceModule(smsDeviceModule);
        // AppLauncher
        appLauncherDeviceModule = new AppLauncherDeviceModule(messageSender, new AppLauncherImpl(this));
        initAppLauncherListener();
        appLauncherDeviceModule.addAppLauncherListener(appLauncherListener);
        dcsSdk.putDeviceModule(appLauncherDeviceModule);
        // 设置
        deviceControlDeviceModule = new DeviceControlDeviceModule(messageSender);
        initDeviceControlListener();
        deviceControlDeviceModule.addDeviceControlListener(deviceControlListener);
        dcsSdk.putDeviceModule(deviceControlDeviceModule);
        // 本地闹钟
        alarmsDeviceModule = new AlarmsDeviceModule(messageSender);
        initAlarmListener();
        alarmsDeviceModule.addAlarmListener(alarmListener);
        dcsSdk.putDeviceModule(alarmsDeviceModule);
        // 本地音乐
        localAudioPlayerDeviceModule = new LocalAudioPlayerDeviceModule(messageSender);
        initLocalAudioPlayerListener();
        localAudioPlayerDeviceModule.addLocalAudioPlayerListener(localAudioPlayerListener);
        dcsSdk.putDeviceModule(localAudioPlayerDeviceModule);
        // 联系人
        contactsDeviceModule = new ContactsDeviceModule(messageSender);
        initContactsListener();
        contactsDeviceModule.addContactsListener(contactsListener);
        dcsSdk.putDeviceModule(contactsDeviceModule);
        // 离线识别
        OffLineDeviceModule offLineDeviceModule = new OffLineDeviceModule();
        dcsSdk.putDeviceModule(offLineDeviceModule);

        // 设置闹钟播放源（实例代码）
//        InternalApi internalApi = ((DcsSdkImpl) dcsSdk).getInternalApi();
//        String namespace = com.baidu.duer.dcs.devicemodule.alerts.ApiConstants.NAMESPACE;
//        AlertsDeviceModule alertsDeviceModule = (AlertsDeviceModule) internalApi.getDeviceModule(namespace);
//        if (alertsDeviceModule != null) {
//            alertsDeviceModule.setAlarmSource("assets://ding.wav");
//        }

        addOtherDeviceModule(dcsSdk, messageSender);

    }

    protected void addOtherDeviceModule(IDcsSdk dcsSdk, IMessageSender messageSender) {

    }

    protected void sdkRun() {
        // 第三步，将sdk跑起来
        ((DcsSdkImpl) dcsSdk).getInternalApi().login(new ILoginListener() {
            @Override
            public void onSucceed(String accessToken) {
                dcsSdk.run();
                Toast.makeText(SDKBaseActivity.this.getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String errorMessage) {
                Toast.makeText(SDKBaseActivity.this.getApplicationContext(), "登录失败", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancel() {
                Toast.makeText(SDKBaseActivity.this.getApplicationContext(), "登录被取消", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initViews() {
        textViewWakeUpTip = (TextView) findViewById(R.id.id_tv_wakeup_tip);
        nextButton = (Button) findViewById(R.id.id_next_audio_btn);
        nextButton.setOnClickListener(this);
        preButton = (Button) findViewById(R.id.id_previous_audio);
        preButton.setOnClickListener(this);
        playButton = (Button) findViewById(R.id.id_audio_default_btn);
        playButton.setOnClickListener(this);
        textInput = (EditText) findViewById(R.id.textInput);
        sendButton = (Button) findViewById(R.id.sendBtn);
        sendButton.setOnClickListener(this);
        voiceButton = (Button) findViewById(R.id.voiceBtn);
        voiceButton.setOnClickListener(this);
        cancelVoiceButton = (Button) findViewById(R.id.cancelBtn);
        cancelVoiceButton.setOnClickListener(this);
        volumeBtn = (Button) findViewById(R.id.id_btn_volume);
        volumeBtn.setOnClickListener(this);
        Button openLogBtn = (Button) findViewById(R.id.openLogBtn);
        openLogBtn.setOnClickListener(this);
        textViewRenderVoiceInputText = (TextView) findViewById(R.id.id_tv_RenderVoiceInputText);
        mTopLinearLayout = (LinearLayout) findViewById(R.id.topLinearLayout);
        dcsWebView = new DcsWebView(this.getApplication());
        mTopLinearLayout.addView(dcsWebView);

        textViewWakeUpTip.setVisibility(enableWakeUp() ? View.VISIBLE : View.GONE);
        initDcsWebView();
    }

    private void initDcsWebView() {
        dcsWebView.setLoadListener(new DcsWebView.LoadListener() {
            @Override
            public void onPageStarted() {

            }

            @Override
            public void onPageFinished() {
                DCSStatistics.getInstance().reportView(duerResultT, System.currentTimeMillis());

                Toast.makeText(SDKBaseActivity.this, (System.currentTimeMillis() - duerResultT)
                        + " ms", Toast.LENGTH_LONG).show();
            }
        });
    }

    public InternalApi getInternalApi() {
        return ((DcsSdkImpl) dcsSdk).getInternalApi();
    }

    private void initWakeUpListener() {
        IWakeupAgent wakeupAgent = getInternalApi().getWakeupAgent();
        if (wakeupAgent != null) {
            wakeupAgentListener = new IWakeupAgent.IWakeupAgentListener() {
                @Override
                public void onWakeupSucceed() {
                    Toast.makeText(SDKBaseActivity.this, "唤醒成功",
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

    private void initVoiceRequestListener() {
        // 添加会话状态监听
        dialogStateListener = new IDialogStateListener() {
            @Override
            public void onDialogStateChanged(DialogState dialogState) {
                switch (dialogState) {
                    case IDLE:
                        isStopListenReceiving = false;
                        voiceButton.setText(getResources().getString(R.string.stop_record));
                        break;
                    case LISTENING:
                        isStopListenReceiving = true;
                        voiceButton.setText(getResources().getString(R.string.start_record));
                        break;
                    case SPEAKING:
                        voiceButton.setText(getResources().getString(R.string.speaking));
                        break;
                    case THINKING:
                        voiceButton.setText(getResources().getString(R.string.think));
                        break;
                    default:
                        break;
                }
            }
        };
        dcsSdk.getVoiceRequest().addDialogStateListener(dialogStateListener);
    }

    private void initPhoneCallListener() {
        // 打电话指令监听
        phoneCallListener = new PhoneCallDeviceModule.IPhoneCallListener() {
            @Override
            public void onPhoneCallByName(PhonecallByNamePayload payload) {
                Toast.makeText(SDKBaseActivity.this, "打电话指令（按姓名）", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSelectCallee(SelectCalleePayload payload) {
                Toast.makeText(SDKBaseActivity.this, "打电话指令（选择联系人）", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPhoneCallByNumber(PhonecallByNumberPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "打电话指令（按号码）", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void initSmsListener() {
        // 发短信指令监听
        smsListener = new SmsDeviceModule.ISmsListener() {
            @Override
            public void onSendSmsByName(SendSmsByNamePayload payload) {
                Toast.makeText(SDKBaseActivity.this, "发短信指令（按姓名）", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSelectRecipient(SelectRecipientPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "打电话指令（选择联系人）", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSendSmsByNumber(SendSmsByNumberPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "打电话指令（按号码）", Toast.LENGTH_LONG).show();
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
                    appLauncherDeviceModule.getAppLauncher().launchAppByDeepLink(SDKBaseActivity.this,
                            payload.getDeepLink());
                } else if (!TextUtils.isEmpty(payload.getPackageName())) {
                    appLauncherDeviceModule.getAppLauncher().launchAppByPackageName(SDKBaseActivity.this,
                            payload.getPackageName());
                } else if (!TextUtils.isEmpty(payload.getAppName())) {
                    appLauncherDeviceModule.getAppLauncher().launchAppByName(SDKBaseActivity.this,
                            payload.getAppName());
                }
            }
        };
    }

    private void initLocalAudioPlayerListener() {
        // 本地音乐指令监听
        localAudioPlayerListener = new LocalAudioPlayerDeviceModule.ILocalAudioPlayerListener() {
            @Override
            public void onSearchAndPlayMusic(SearchAndPlayMusicPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "搜索并播放音乐", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSearchAndPlayUnicast(SearchAndPlayUnicastPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "搜索并播放有声节目", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSearchAndPlayRadio(SearchAndPlayRadioPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "搜索并播放直播电台", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(NextPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "播放下一首", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPrevious(PreviousPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "播放上一首", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPause(PausePayload payload) {
                Toast.makeText(SDKBaseActivity.this, "暂停播放", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStop(StopPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "停止播放", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPlay(PlayPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "继续播放", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetPlaybackMode(SetPlaybackModePayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置循环模式", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void initDeviceControlListener() {
        // 设备控制指令监听
        deviceControlListener = new DeviceControlDeviceModule.IDeviceControlListener() {
            @Override
            public void onAdjustBrightness(AdjustBrightnessPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "调节亮度", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetAssistiveTouch(SetAssistiveTouchPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置悬浮球", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetBluetooth(SetBluetoothPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置蓝牙", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetBrightness(SetBrightnessPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置亮度", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetCellular(SetCellularPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置移动数据", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetCellularMode(SetCellularModePayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置移动数据模式", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetGps(SetGpsPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置GPS", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetHotspot(SetHotspotPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "打电话指令（按姓名）", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetNfc(SetNfcPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置NFC", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetPhoneMode(SetPhoneModePayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置情景模式", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetPhonePower(SetPhonePowerPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "关机/重启", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetPortraitLock(SetPortraitLockPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置屏幕旋转", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetSynchronization(SetSynchronizationPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置同步", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetVibration(SetVibrationPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置震动", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetVpn(SetVpnPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置VPN", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetWifi(SetWifiPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "设置Wifi", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void initAlarmListener() {
        // 闹钟、定制器指令监听
        alarmListener = new AlarmsDeviceModule.IAlarmDirectiveListener() {
            @Override
            public void onSetAlarmDirectiveReceived(SetAlarmPayload setAlarmPayload) {
                Toast.makeText(SDKBaseActivity.this, "设置闹钟指令", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onShowAlarmsDirectiveReceived(ShowAlarmsPayload showAlarmsPayload) {
                Toast.makeText(SDKBaseActivity.this, "查看闹钟指令", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSetTimerDirectiveReceived(SetTimerPayload setTimerPayload) {
                Toast.makeText(SDKBaseActivity.this, "设置定时器指令", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onShowTimersDirectiveReceived(ShowTimersPayload showTimersPayload) {
                Toast.makeText(SDKBaseActivity.this, "查看定时器指令", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void initContactsListener() {
        // 联系人指令监听
        contactsListener = new ContactsDeviceModule.IContactsListener() {
            @Override
            public void onCreateContact(CreateContactPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "创建联系人指令", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSearchContact(SearchContactPayload payload) {
                Toast.makeText(SDKBaseActivity.this, "查看联系人指令", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void handleHtmlPayload(HtmlPayload payload) {
        dcsWebView.loadUrl(payload.getUrl());
        duerResultT = System.currentTimeMillis();
    }

    private void handleRenderVoiceInputTextPayload(RenderVoiceInputTextPayload payload) {
        textViewRenderVoiceInputText.setText(payload.text);
        if (payload.type == RenderVoiceInputTextPayload.Type.FINAL) {
            FileUtil.appendStrToFileNew("ASR-FINAL-RESULT:" + payload.text + "," + System.currentTimeMillis() + "\n");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_next_audio_btn:
                getInternalApi().sendCommandIssuedEvent(PlaybackControllerDeviceModule.CommandIssued.CommandIssuedNext);
                break;
            case R.id.id_previous_audio:
                getInternalApi().sendCommandIssuedEvent(PlaybackControllerDeviceModule.CommandIssued
                        .CommandIssuedPrevious);
                break;
            case R.id.id_audio_default_btn:
                if (isPlaying) {
                    getInternalApi().sendCommandIssuedEvent(PlaybackControllerDeviceModule.CommandIssued
                            .CommandIssuedPause);
                } else {
                    getInternalApi().sendCommandIssuedEvent(PlaybackControllerDeviceModule.CommandIssued
                            .CommandIssuedPlay);
                }
                isPlaying = !isPlaying;
                break;
            case R.id.sendBtn:
                String inputText = textInput.getText().toString().trim();
                if (TextUtils.isEmpty(inputText)) {
                    Toast.makeText(this, getResources().getString(R.string.inputed_text_cannot_be_empty),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // 清空并收起键盘
                textInput.getEditableText().clear();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
                if (!NetWorkUtil.isNetworkConnected(this)) {
                    Toast.makeText(this,
                            getResources().getString(R.string.err_net_msg),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                getInternalApi().sendQuery(inputText);
                break;
            case R.id.voiceBtn:
                if (getAsrMode() == DcsConfig.ASR_MODE_ONLINE) {
                    if (!NetWorkUtil.isNetworkConnected(this)) {
                        Toast.makeText(this,
                                getResources().getString(R.string.err_net_msg),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                if (isStopListenReceiving) {
                    dcsSdk.getVoiceRequest().endVoiceRequest();
                    isStopListenReceiving = false;
                    voiceButton.setText("点击说话");
                    return;
                }
                isStopListenReceiving = true;
                voiceButton.setText("录音中...");
                textViewRenderVoiceInputText.setText("");
                dcsSdk.getVoiceRequest().beginVoiceRequest(getAsrType() == DcsConfig.ASR_TYPE_AUTO);
                break;
            case R.id.cancelBtn:
                // 取消识别，不再返回任何识别结果
                dcsSdk.getVoiceRequest().cancelVoiceRequest();
                break;
            case R.id.id_btn_volume:
                Log.d(TAG, "CurrentVolume:" + getInternalApi().getCurrentVolume());
                getInternalApi().setVolume(1.0f);
                break;
            case R.id.openLogBtn:
                openAssignFolder(FileUtil.getLogFilePath());
                break;
            default:
                break;
        }
    }

    private void openAssignFolder(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(this,
                    getResources().getString(R.string.no_log),
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "text/plain");
        try {
            startActivity(Intent.createChooser(intent,
                    getResources().getString(R.string.open_file_title)));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // 停止tts，音乐等有关播放.
//        getInternalApi().pauseSpeaker();
        // 如果有唤醒，则停止唤醒
        getInternalApi().stopWakeup();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
        // 恢复tts，音乐等有关播放
        getInternalApi().resumeSpeaker();
        // 如果有唤醒，则恢复唤醒
        getInternalApi().startWakeup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // dcsWebView

        getInternalApi().pauseSpeaker();

        dcsWebView.setLoadListener(null);
        mTopLinearLayout.removeView(dcsWebView);
        dcsWebView.removeAllViews();
        dcsWebView.destroy();

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

    protected IOauth getOath() {
        return new BaiduOauthImplicitGrantIml(CLIENT_ID, this);
    }

    // -------------------------abstract

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

    // -------------------------abstract
}
