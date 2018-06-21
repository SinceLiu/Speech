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
package com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.report;

import com.baidu.duer.dcs.util.util.LogUtil;
import com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload;

/**
 * Created by guxiuzhong@baidu.com on 2017/12/21.
 * <p>
 * http://icode.baidu.com/repos/baidu/duer/open-platform-api-doc/blob/master:dueros-conversational-service/device-interface/audio-player.md
 */
public class AudioPlayerReporter {
    public static final String TAG = "AudioPlayerReporter";
    private long duration;
    private long progressReportDelay;
    private long progressReportInterval;
    private boolean isReport;
    private long next;

    public AudioPlayerReporter() {
    }

    public void setDuration(long duration) {
        this.duration = duration;
        LogUtil.dcf(TAG, "duration=" + duration);
    }

    public void setInfo(long offset, PlayPayload.ProgressReport progressReport) {
        if (progressReport == null) {
            return;
        }
        progressReportDelay = progressReport.progressReportDelayInMilliseconds;
        progressReportInterval = progressReport.progressReportIntervalInMilliseconds;
        LogUtil.dcf(TAG, "progressReportDelay=" + progressReportDelay);
        LogUtil.dcf(TAG, "progressReportInterval=" + progressReportInterval);
        isReport = false;
        next = progressReportInterval + offset;
    }


    /**
     * ProgressReportDelayElapsed事件
     *
     * @param percent
     */
    public void handleProgressReportDelay(int percent) {
        if (duration <= 0) {
            return;
        }
        if (progressReportDelay > 0 && !isReport) {
            if ((long) (1.0f * percent / 100 * duration) >= progressReportDelay) {
                isReport = true;
                if (progressReporterListener != null) {
                    progressReporterListener.progressReportDelay();
                    LogUtil.dc(TAG, "progressReportDelay callback");
                }
            }
        }
    }


    /**
     * ProgressReportIntervalElapsed事件
     *
     * @param percent
     */
    public void handleProgressReportInterval(int percent) {
        if (duration <= 0) {
            return;
        }
        if (progressReportInterval > 0) {
            LogUtil.dc(TAG, "next =" + next);
            if ((long) (1.0f * percent / 100 * duration) >= next) {
                next += progressReportInterval;
                if (progressReporterListener != null) {
                    progressReporterListener.progressReportInterval();
                    LogUtil.dc(TAG, "progressReportInterval callback");
                }
            }
        }
    }

    private IProgressReporterListener progressReporterListener;

    public void setProgressReporterListener(IProgressReporterListener listener) {
        this.progressReporterListener = listener;
    }

    public interface IProgressReporterListener {

        void progressReportDelay();

        void progressReportInterval();
    }
}