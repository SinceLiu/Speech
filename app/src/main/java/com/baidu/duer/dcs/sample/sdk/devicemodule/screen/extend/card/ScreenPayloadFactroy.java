package com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card;

import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message.RenderAirQualityPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message.RenderDatePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message.RenderPlayerInfoPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message.RenderWeatherPayload;
import com.baidu.duer.dcs.util.message.Directive;
import com.baidu.duer.dcs.util.message.Payload;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author oubin
 * @date 2018/6/21
 */

public class ScreenPayloadFactroy {

    private static Map<String, Class<? extends IScreenPayload>> payloadMap = new HashMap<>();
    static {
        payloadMap.put(ApiConstants.Directives.RenderAirQuality.NAME, RenderAirQualityPayload.class);
    }

    public static IScreenPayload parseDirective(Directive directive){
        IScreenPayload payload = null;
        String name = directive.getName();
        Payload p = directive.getPayload();
        if (ApiConstants.Directives.RenderAirQuality.NAME.equals(name)){
            return (RenderAirQualityPayload)directive.getPayload();
        } else if (ApiConstants.Directives.RenderDate.NAME.equals(name)){
            return (RenderDatePayload)directive.getPayload();
        } else if (p instanceof RenderPlayerInfoPayload){
            return (RenderPlayerInfoPayload)p;
        } else if (p instanceof RenderWeatherPayload){
            return (RenderWeatherPayload)p;
        }
        return null;
    }

}
