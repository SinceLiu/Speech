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
package com.baidu.duer.dcs.sample.sdk.devicemodule.contacts.message;

import com.baidu.duer.dcs.util.message.Payload;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by caoyushu01 on 2017/7/28.
 */

public class SearchContactPayload extends Payload {
    private List<String> candidateNames;

    public SearchContactPayload (@JsonProperty("candidateNames") List<String> candidateNames) {
        this.candidateNames = candidateNames;
    }

    public List<String> getCandidateNames () {
        return candidateNames;
    }

    public void setCandidateNames (List<String> candidateNames) {
        this.candidateNames = candidateNames;
    }
}
