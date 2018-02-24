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
package com.baidu.duer.dcs.sample.sdk.offlineasr;

import com.baidu.duer.dcs.framework.internalapi.DcsConfig;
import com.baidu.duer.dcs.sample.sdk.SDKBaseActivity;

/**
 * 离线识别
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/9/6.
 */
public class SDKAsrAutoWithWakeUpOffLineActivity extends SDKBaseActivity {
    @Override
    public boolean enableWakeUp() {
        return true;
    }

    @Override
    public int getAsrMode() {
        return DcsConfig.ASR_MODE_OFFLINE;
    }

    @Override
    public int getAsrType() {
        return DcsConfig.ASR_TYPE_AUTO;
    }

    @Override
    public boolean isSilentLogin() {
        return true;
    }
}