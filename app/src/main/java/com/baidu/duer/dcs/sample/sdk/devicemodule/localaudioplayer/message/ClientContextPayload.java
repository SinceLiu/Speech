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
package com.baidu.duer.dcs.sample.sdk.devicemodule.localaudioplayer.message;

import com.baidu.duer.dcs.util.message.Payload;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by longyin01 on 17/9/26.
 */

public class ClientContextPayload extends Payload {

    private AudioFileTag audioFileTag;
    private String playerActivity;
    private String status;
    private String playerName;

    public ClientContextPayload(@JsonProperty("audioFileTag") AudioFileTag audioFileTag,
                                @JsonProperty("playerActivity") String playerActivity,
                                @JsonProperty("status") String status,
                                @JsonProperty("playerName") String playerName) {
        this.audioFileTag = audioFileTag;
        this.playerActivity = playerActivity;
        this.status = status;
        this.playerName = playerName;
    }

    public AudioFileTag getAudioFileTag() {
        return audioFileTag;
    }

    public void setAudioFileTag(AudioFileTag audioFileTag) {
        this.audioFileTag = audioFileTag;
    }

    public String getplayerActivity() {
        return playerActivity;
    }

    public void setPlayerActivity(String playerActivity) {
        this.playerActivity = playerActivity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
