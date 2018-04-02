package com.baidu.duer.dcs.sample.sdk.devicemodule.screen.message;

import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.TokenPayload;

import java.io.Serializable;
import java.util.List;

public class RenderHintPayload extends TokenPayload implements Serializable {
    public List<String> cueWords;
}
