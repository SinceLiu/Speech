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
package com.baidu.duer.dcs.sample.sdk.devicemodule.offlineasr;

import android.util.Log;
import android.widget.Toast;

import com.baidu.duer.dcs.devicemodule.system.HandleDirectiveException;
import com.baidu.duer.dcs.framework.BaseDeviceModule;
import com.baidu.duer.dcs.framework.message.ClientContext;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.OffLineAsrDirective;
import com.baidu.duer.dcs.util.SystemServiceManager;

import java.util.HashMap;

/**
 * 离线识别的DeviceModule
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/9/26.
 */
public class OffLineDeviceModule extends BaseDeviceModule {
    public static final String TAG = "OffLineDeviceModule";

    public OffLineDeviceModule() {
        // 名字固定的
        super(ApiConstants.NAMESPACE);
    }

    @Override
    public ClientContext clientContext() {
        return null;
    }

    @Override
    public void handleDirective(Directive directive) throws HandleDirectiveException {
        // 数据正常的，所有的指令名字都叫OffLineAsrRecognitionResult
        if (directive.getName().equals("OffLineAsrRecognitionResult")) {
            OffLineAsrDirective offLineAsrDirective = (OffLineAsrDirective) directive;
            Log.d(TAG, "handleDirective: " + offLineAsrDirective.type);
            Log.d(TAG, "handleDirective: " + offLineAsrDirective.offLineData);
            // 最终结果时提示一下，表示离线真的成功了。
            if (offLineAsrDirective.type == 2) {
                Toast.makeText(SystemServiceManager.getAppContext(), "离线识别成功.", Toast.LENGTH_SHORT).show();
            }
        } else {
            String message = "No device to handle the directive";
            throw new HandleDirectiveException(
                    HandleDirectiveException.ExceptionType.UNSUPPORTED_OPERATION,
                    message);
        }
    }

    @Override
    public HashMap<String, Class<?>> supportPayload() {
        return null;
    }

    @Override
    public void release() {

    }
}