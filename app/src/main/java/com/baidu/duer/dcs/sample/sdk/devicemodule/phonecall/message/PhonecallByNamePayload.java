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
package com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message;

import com.baidu.duer.dcs.util.message.Payload;

import java.util.List;

/**
 * Created by caoyushu01 on 17/7/6.
 */

public class PhonecallByNamePayload extends Payload {
    // 服务端返回的全部响应当前次打电话的人名列表
    private List<com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.CandidateCallee> candidateCallees;
    // 使用sim卡1/卡2
    private String useSimIndex = "";
    // 用户指定的运营商名称,可选字段
    private String useCarrier = "";

    public PhonecallByNamePayload () {

    }

    public PhonecallByNamePayload (List<CandidateCallee> candidateCallees, String useSimIndex, String useCarrier) {
        this.candidateCallees = candidateCallees;
        this.useSimIndex = useSimIndex;
        this.useCarrier = useCarrier;
    }

    public List<com.baidu.duer.dcs.sample.sdk.devicemodule.phonecall.message.CandidateCallee> getCandidateCallees () {
        return candidateCallees;
    }

    public void setCandidateCallees (List<CandidateCallee> candidateCallees) {
        this.candidateCallees = candidateCallees;
    }

    public String getUseSimIndex () {
        return useSimIndex;
    }

    public void setUseSimIndex (String useSimIndex) {
        this.useSimIndex = useSimIndex;
    }

    public String getUseCarrier () {
        return useCarrier;
    }

    public void setUseCarrier (String useCarrier) {
        this.useCarrier = useCarrier;
    }
}
