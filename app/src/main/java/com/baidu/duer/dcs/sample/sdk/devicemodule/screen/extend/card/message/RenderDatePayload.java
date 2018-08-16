package com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message;

import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.TokenPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.ApiConstants;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.IScreenPayload;

import java.io.Serializable;

/**
 *
 */
public class RenderDatePayload extends TokenPayload implements Serializable, IScreenPayload {
    public String datetime;
    public String timeZoneName;
    public String day;

    @Override
    public String name() {
        return ApiConstants.Directives.RenderDate.NAME;
    }

    @Override
    public String getScreenContent() {
        return datetime;
    }
}
