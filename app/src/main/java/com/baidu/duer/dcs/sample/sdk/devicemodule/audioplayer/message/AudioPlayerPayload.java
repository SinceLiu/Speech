/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.message;

import com.baidu.duer.dcs.util.message.Payload;

import java.io.Serializable;

/**
 * Audio Player模块上报各种事件的payload结构
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/4.
 */
public class AudioPlayerPayload extends Payload implements Serializable {
    public String token;
    public long offsetInMilliseconds;

    public AudioPlayerPayload(String token, long offsetInMilliseconds) {
        this.token = token;
        this.offsetInMilliseconds = offsetInMilliseconds;
    }
}