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
package com.baidu.duer.dcs.sample.sdk.devicemodule.devicecontrol.message;

import com.baidu.duer.dcs.util.message.Payload;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by caoyushu01 on 2017/7/26.
 */

public class SetSynchronizationPayload extends Payload {
    private String target;
    private Boolean synchronization;

    public SetSynchronizationPayload (@JsonProperty("target") String target,
                                      @JsonProperty("synchronization") Boolean synchronization) {
        this.target = target;
        this.synchronization = synchronization;
    }

    public String getTarget () {
        return target;
    }

    public void setTarget (String target) {
        this.target = target;
    }

    public Boolean getSynchronization () {
        return synchronization;
    }

    public void setSynchronization (Boolean synchronization) {
        this.synchronization = synchronization;
    }

    @Override
    public String toString() {
        return "SetSynchronizationPayload{" +
                "target='" + target + '\'' +
                ", synchronization=" + synchronization +
                '}';
    }
}
