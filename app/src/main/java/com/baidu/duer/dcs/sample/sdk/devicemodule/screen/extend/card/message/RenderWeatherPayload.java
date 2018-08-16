package com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message;

import android.text.TextUtils;

import com.baidu.dcs.okhttp3.internal.ws.RealWebSocket;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.TokenPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.ApiConstants;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.IScreenPayload;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;

/**
 * "payload":{"province":"广东省","city":"中山","county":"中山","askingDay":"FRI","askingDate":"2018-06-22","weatherForecast":[{"weatherIcon":{"src":"http:\/\/g.hiphotos.baidu.com\/xiaodu\/pic\/item\/c75c10385343fbf2cc314914bb7eca8065388fec.jpg"},"highTemperature":"33℃","lowTemperature":"27℃","day":"THU","date":"2018-06-21","weatherCondition":"阴转多云","windCondition":"无持续风向微风","currentTemperature":"29℃","currentPM25":"24","currentAirQuality":"优","indexes":[{"type":"CLOTHES","level":"炎热","suggestion":"很热，建议你穿短袖、短裤或短裙等夏季服装，请注意防晒"},{"type":"CAR_WASH","level":"不宜","suggestion":"不适合洗车，当日未来24小时内有雨，雨水可能会再次弄脏你的车"},{"type":"TRIP","level":"一般","suggestion":"较热，微风，有降水，请尽量不要外出，若外出，请注意防雷并携带雨具，注意防暑降温。"},{"type":"INFLUENZA","level":"少发","suggestion":"各气象指标适宜，不太容易感冒"},{"type":"EXERCISE","level":"较不宜","suggestion":"有降水，推荐您在室内进行低强度运动；若坚持户外运动，须注意选择避雨防滑地点，并携带雨具。"},{"type":"ULTRAVIOLET","level":"中等","suggestion":"有中等强度的紫外线，建议你涂抹防晒指数高于15的防晒霜"}],"pm25":"24","airQuality":"优"},{"weatherIcon":{"src":"http:\/\/b.hiphotos.baidu.com\/xiaodu\/pic\/item\/b7fd5266d016092452454d2cdf0735fae6cd34a1.jpg"},"highTemperature":"33℃","lowTemperature":"26℃","day":"FRI","date":"2018-06-22","weatherCondition":"雷阵雨","windCondition":"无持续风向微风","indexes":[{"type":"CLOTHES","level":"热","suggestion":"温度舒适，可以穿T恤衫、衬衫和薄外套，注意早晚温差"},{"type":"CAR_WASH","level":"较不适宜","suggestion":"不适合洗车，未来24小时内有雨，雨水可能会再次弄脏你的车"},{"type":"INFLUENZA","level":"易发","suggestion":"容易感冒，请注意衣服增减，多喝开水多保暖"},{"type":"ULTRAVIOLET","level":"最弱","suggestion":"紫外线辐射弱，无需特别防护。如果长期出门，建议你涂抹防晒指数在8-12之间的防晒霜"}],"pm25":"19","airQuality":"优"}
 * 中山
 * 2018-06-22
 * 中雨转雷阵雨
 * 温度：27℃-32℃
 * 空气质量指数：32，优
 */
public class RenderWeatherPayload extends TokenPayload implements Serializable, IScreenPayload {
    public String token;
    public String city;
    public String askingDay;
    public String askingDate;
    public String askingDateDescription;
    public List<WeatherForecast> weatherForecast;

    @Override
    public String name() {
        return ApiConstants.Directives.RenderWeather.NAME;
    }

    @Override
    public String getScreenContent() {
        int index = -1;
        WeatherForecast targetForecast = null;
        if (weatherForecast != null) {
            int size = weatherForecast.size();
            for (int i = 0; i < size; i++) {
                WeatherForecast forecast = weatherForecast.get(i);
                if (TextUtils.equals(askingDate, forecast.date)) {
                    index = i;
                    targetForecast = forecast;
                    break;
                }
            }
        }
        if (targetForecast != null) {
            String regex = "\n";
            String temperature = "温度:" + targetForecast.lowTemperature + "-" +
                    targetForecast.highTemperature;
            StringBuilder result = new StringBuilder();
            result.append(city);
            result.append(regex).append(askingDate);
            if (!TextUtils.isEmpty(targetForecast.weatherCondition)) {
                result.append(regex).append(targetForecast.weatherCondition);
            }
            if (!TextUtils.isEmpty(targetForecast.lowTemperature)) {
                result.append(regex).append(temperature);
            }
            boolean pm25 = !TextUtils.isEmpty(targetForecast.pm25);
            boolean air = !TextUtils.isEmpty(targetForecast.airQuality);
            if (pm25 || air) {
                result.append(regex).append("空气质量指数:");
                if (pm25) {
                    result.append(targetForecast.pm25);
                }
                if (pm25 && air) {
                    result.append(" ");
                }
                if (air){
                    result.append(targetForecast.airQuality);
                }
            }
            return result.toString();
        }
        return null;
    }

    public static final class WeatherForecast implements Serializable {
        public ImageStructure weatherIcon;
        public String highTemperature;
        public String lowTemperature;
        public String day;
        public String date;
        public String weatherCondition;
        public String windCondition;
        public String currentTemperature;
        public String currentPM25;
        public String pm25;
        public String airQuality;
        public String currentAirQuality;
        public List<Index> indices;

        public static final class Index {
            public String type;
            public String level;
            public String suggestion;
        }

    }

    public static final class ImageStructure implements Serializable {
        public String src;
    }
}
