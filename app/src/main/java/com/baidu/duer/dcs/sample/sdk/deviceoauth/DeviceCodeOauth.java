/*
 * *
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
package com.baidu.duer.dcs.sample.sdk.deviceoauth;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.duer.dcs.components.httpagent.HttpRequestFactory;
import com.baidu.duer.dcs.systeminterface.IOauth;
import com.baidu.duer.dcs.util.http.CallInterface;
import com.baidu.duer.dcs.util.http.IHttpResponse;
import com.baidu.duer.dcs.util.http.callback.SimpleCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wuqi08 on 18/1/17.
 */

public class DeviceCodeOauth implements IOauth {
    private static final String TAG = "DeviceCodeOauth";

    private Timer pollTimer = new Timer();
    private String clientId;
    private String secrectId;
    private String deviceCode;
    private DeviceCodeTokenManager accessTokenManager;
    private long durationTime = 1800 * 1000;
    private IGetTokenSuccess getTokenSuccess;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     *
     * secretid  存在端上，会有安全风险，不建议这种方式
     *
     * secretid，可以放在服务端，由服务端提供接口，进行调用
     *
     * 服务端需要提供对应的获取accesstoken，和在token失效后，刷新accesstoken的接口
     *
     *  参考文档http://developer.baidu.com/wiki/index.php?title=docs/oauth/application
     *
     * */
    public DeviceCodeOauth(String clientId, String secretId, String code) {
        this.clientId = clientId;
        this.secrectId = secretId;
        this.deviceCode = code;
        accessTokenManager = new DeviceCodeTokenManager();
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }
    public void setGetTokenListener(IGetTokenSuccess listener) {
        this.getTokenSuccess = listener;
    }

    @Override
    public void getToken(IOauthCallbackListener listener) {
        if (accessTokenManager.isTokenExpired()) {
            listener.onSucceed(accessTokenManager.getAccessToken());
            return;
        }
        doRefreshToken(listener);
    }

    public void doRefreshToken(final IOauthCallbackListener listener) {
        final String refreshToken = accessTokenManager.getRefreshToken();
        if (TextUtils.isEmpty(refreshToken)) {
            listener.onError("refresh token is null");
            return;
        }

        String url = "https://openapi.baidu.com/oauth/2.0/token?"
                + "grant_type=refresh_token&"
                + "refresh_token=" + refreshToken
                + "&client_id=" + clientId
                + "&client_secret=" + secrectId
                + "&scope=basic";
        Log.d(TAG, "doRefreshToken: " + url);
        HttpRequestFactory.getHttpAgent().simpleRequest("POST", url, null, null, new SimpleCallback() {
            @Override
            public void onResponse(IHttpResponse response) {
                if (response.isSuccessful()) {
                    String resonseBody = null;
                    try {

                        resonseBody = response.body().string();
                        Log.d(TAG, "onResponse: " + resonseBody);
                        JSONObject jsonObject = new JSONObject(resonseBody);
                        String accesstoken = jsonObject.optString("access_token");
                        String refreshToken = jsonObject.optString("refresh_token");
                        String expireIn = jsonObject.optString("expires_in");
                        HashMap<String , String > map = new HashMap<>();
                        map.put("access_token", accesstoken);
                        map.put("refresh_token", refreshToken);
                        map.put("expires_in", expireIn);
                        accessTokenManager.storeTokenInfo(map);
                    } catch (IOException e) {
                        e.printStackTrace();
                        handlerError("response is not right", listener);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        handlerError("response is not right", listener);
                    }
                } else {
                    handlerError("response is not right", listener);
                }

            }

            @Override
            public void onFailure(CallInterface call, Exception e) {
                handlerError("request failure " + e.getMessage(), listener);
            }

            @Override
            public void onCancel() {
                handlerError("request been canceled.", listener);

            }
        });

    }

    private void handlerError(final String errorMsg, final IOauthCallbackListener listener) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onError(errorMsg);
            }
        });
    }
    @Override
    public void clearAccessToken() {
        accessTokenManager.clearToken();
    }

    private long startTime = 0;

    public void startPollTimer() {
        startTime = System.currentTimeMillis();
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "run: start poll ");
                getAccessTokenByDeviceCode(clientId, secrectId, deviceCode);
            }
        }, 0, 5 * 1000);
    }

    public void getAccessTokenByDeviceCode(String clientID, String sececretID, String deviceCode) {
        String url = "https://openapi.baidu.com/oauth/2.0/token"
                + "?grant_type=device_token"
                + "&code=" + deviceCode
                + "&client_id=" + clientID
                + "&client_secret=" + sececretID;
        Log.d(TAG, "getAccessTokenByDeviceCode: " + url);
        HttpRequestFactory.getHttpAgent().simpleRequest("GET", url, null, null, new SimpleCallback() {
            @Override
            public void onResponse(IHttpResponse response) {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONObject bodyJson = new JSONObject(body);
                        String accesstoken = bodyJson.optString("access_token");
                        String refreshToken = bodyJson.optString("refresh_token");
                        String expiresIn = bodyJson.optString("expires_in");
                        HashMap<String, String> map = new HashMap<>();
                        map.put("access_token", accesstoken);
                        map.put("refresh_token", refreshToken);
                        map.put("expires_in", expiresIn);
                        accessTokenManager.storeTokenInfo(map);
                        if (getTokenSuccess != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getTokenSuccess.onSuccess();
                                }
                            });
                        }
                        Log.d(TAG, "GET accesstoken success");
                        pollTimer.cancel();
                        Log.d(TAG, "cancel polltimer");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    stopTimer();
                }
            }

            @Override
            public void onFailure(CallInterface call, Exception e) {
                Log.d(TAG, "ONFFAILUE");
                stopTimer();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: ");
                stopTimer();
            }
        });
    }

    private void stopTimer() {
        long lastTime = System.currentTimeMillis() - startTime;
        if (lastTime > durationTime) {
            pollTimer.cancel();
        }
    }



    public interface IGetTokenSuccess {
        void onSuccess();
    }
}
