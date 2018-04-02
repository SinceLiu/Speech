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
package com.baidu.duer.dcs.sample.sdk.offlinetts;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.duer.dcs.api.config.DcsConfig;
import com.baidu.duer.dcs.sample.sdk.SDKBaseActivity;
import com.baidu.duer.dcs.util.AsrType;
import com.readboy.watch.speech.R;

/**
 * Created by wenzongliang on 2017/9/23.
 */

public class SDKTtsActivity extends SDKBaseActivity {

    private Button sendButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendButton.setText("在线");
        sendButton.setOnClickListener(onSendButtonClick);
        sendButton2 = (Button) findViewById(R.id.sendBtn2);
        sendButton2.setText("离线");
        sendButton2.setVisibility(View.VISIBLE);
        sendButton2.setOnClickListener(onSendButton2Click);
    }

    private View.OnClickListener onSendButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String inputText = textInput.getText().toString().trim();
            if (TextUtils.isEmpty(inputText)) {
                Toast.makeText(SDKTtsActivity.this, getResources().getString(R.string.inputed_text_cannot_be_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // 清空并收起键盘
            textInput.getEditableText().clear();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
            getInternalApi().speakRequest(inputText);
        }
    };

    private View.OnClickListener onSendButton2Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String inputText = textInput.getText().toString().trim();
            if (TextUtils.isEmpty(inputText)) {
                Toast.makeText(SDKTtsActivity.this, getResources().getString(R.string.inputed_text_cannot_be_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // 该接口线程安全，可以重复调用。内部采用排队策略，调用后将自动加入队列，SDK会按照队列的顺序进行合成及播放。
            // 注意需要合成的每个文本text不超过1024的GBK字节，即512个汉字或英文字母数字。超过请自行按照句号问号等标点切分
            // 调用多次合成接口。
            if (inputText.length() > 512) {
                Toast.makeText(SDKTtsActivity.this, "文本text不超过1024的GBK字节", Toast.LENGTH_LONG).show();
                return;
            }
            // 清空并收起键盘
            textInput.getEditableText().clear();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
            getInternalApi().speakOfflineRequest(inputText);
        }
    };

    @Override
    public boolean enableWakeUp() {
        return true;
    }

    @Override
    public int getAsrMode() {
        return DcsConfig.ASR_MODE_ONLINE;
    }

    @Override
    public AsrType getAsrType() {
        return AsrType.AUTO;
    }

}
