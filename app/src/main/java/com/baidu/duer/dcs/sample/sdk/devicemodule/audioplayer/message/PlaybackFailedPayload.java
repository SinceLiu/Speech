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
import com.baidu.duer.dcs.api.player.IMediaPlayer;

import java.io.Serializable;

/**
 * Audio Player模块上报PlaybackFailed事件对应的payload结构
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/8.
 */
public class PlaybackFailedPayload extends Payload implements Serializable {
    private String token;
    private ErrorStructure error;

    public PlaybackFailedPayload(String token,
                                 IMediaPlayer.ErrorType errorType) {
        this.token = token;
        error = new ErrorStructure(errorType);
    }

    public String getToken() {
        return token;
    }

    public ErrorStructure getError() {
        return error;
    }

    private static final class ErrorStructure {
        private IMediaPlayer.ErrorType type;
        private String message;

        public ErrorStructure(IMediaPlayer.ErrorType type) {
            this.type = type;
            this.message = type.getMessage();
        }

        public IMediaPlayer.ErrorType getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }
    }
}