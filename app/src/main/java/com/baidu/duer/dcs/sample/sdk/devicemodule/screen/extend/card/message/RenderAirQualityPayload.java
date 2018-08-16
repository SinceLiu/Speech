package com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message;

import android.text.TextUtils;

import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.TokenPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.ApiConstants;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.IScreenPayload;

import java.io.Serializable;

/**
 * {
 * "header": {
 * "namespace": "ai.dueros.device_interface.screen_extended_card",
 * "name": "RenderAirQuality",
 * "messageId": "ZHVlcl93ZWF0aGVyKzE1Mjk1ODM5NzU=",
 * "dialogRequestId": "521f820f-c243-4287-8c33-c03da35392d2"
 * },
 * "payload": {
 * "city": "中山",
 * "currentTemperature": "29℃",
 * "pm25": 31,
 * "airQuality": "优",
 * "day": "THU",
 * "date": "2018-06-21",
 * "tips": "空气很好，可以外出活动，呼吸新鲜空气",
 * "dateDescription": "今天",
 * "token": "eyJib3RfaWQiOiJ1cyIsInJlc3VsdF90b2tlbiI6IjFjZTVhODc0ZTZjYzE4YzNmNWY1MTUyMjdkNWRjMDE2IiwiYm90X3Rva2VuIjoibnVsbCJ9"
 * }
 * }
 */
public class RenderAirQualityPayload extends TokenPayload implements Serializable, IScreenPayload {
    public String city;
    public String currentTemperature;
    public String pm25;
    public String airQuality;
    public String day;
    public String date;
    public String dateDescription;
    public String tips;

    @Override
    public String name() {
        return ApiConstants.Directives.RenderAirQuality.NAME;
    }

    @Override
    public String getScreenContent() {
        StringBuilder result = new StringBuilder();
        result.append(city)
                .append(dateDescription);
        if (!TextUtils.isEmpty(pm25)) {
            result.append("空气质量指数").append(pm25);
        }
        result.append("，").append(tips);
        return result.toString();
    }
}
