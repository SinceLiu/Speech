package com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message;

import android.util.Log;

import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.TokenPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.ApiConstants;
import com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.IScreenPayload;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class RenderDatePayload extends TokenPayload implements Serializable, IScreenPayload {
    public String datetime;
    public String timeZoneName;
    public String day;
    public static final String TAG = "lxx-RenderDatePayload";

    @Override
    public String name() {
        return ApiConstants.Directives.RenderDate.NAME;
    }

    @Override
    public String getScreenContent() {
        Log.e(TAG, "UTCTime: " + datetime);
        String time = UTCToCST(datetime);
        Log.e(TAG, "time: " + time);
        return time;
    }

    public String UTCToCST(String UTCstr) {
        try {
            SimpleDateFormat UTCFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = UTCFormat.parse(UTCstr);
            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return timeFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return UTCstr;
        }
    }
}
