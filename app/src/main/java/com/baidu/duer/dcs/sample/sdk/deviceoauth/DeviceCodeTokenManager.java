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

import com.baidu.duer.dcs.oauth.api.AccessTokenBaseManager;
import com.baidu.duer.dcs.oauth.api.OauthSPUtil;
import java.util.HashMap;

/**
 * Created by wuqi08 on 18/1/18.
 */

public class DeviceCodeTokenManager extends AccessTokenBaseManager {

    private String refreshToken = "";

    public DeviceCodeTokenManager() {
        super();
        this.refreshToken = (String) OauthSPUtil.get(context, OauthSPUtil.KEY_REFRESH_TOKEN, "");
    }

    @Override
    public void storeTokenInfo(HashMap<String, String> hashMap) {
        super.storeTokenInfo(hashMap);
        OauthSPUtil.put(context, OauthSPUtil.KEY_REFRESH_TOKEN, hashMap.get("refresh_token"));
    }

    @Override
    public HashMap<String, String> getAccessTokenInfo() {
        HashMap<String, String> hashMap = super.getAccessTokenInfo();
        hashMap.put("refresh_token", (String) OauthSPUtil.get(context, OauthSPUtil.KEY_REFRESH_TOKEN, ""));
        return hashMap;
    }


    public String getRefreshToken() {
        return refreshToken;
    }

}
