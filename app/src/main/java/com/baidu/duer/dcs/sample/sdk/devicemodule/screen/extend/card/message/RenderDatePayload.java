package com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message;

import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.TokenPayload;

import java.io.Serializable;

public class RenderDatePayload extends TokenPayload implements Serializable {
    public String datetime;
    public String timeZoneName;
    public String day;
}
