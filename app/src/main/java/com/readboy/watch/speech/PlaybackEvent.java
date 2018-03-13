package com.readboy.watch.speech;

import com.baidu.duer.dcs.devicemodule.audioplayer.ApiConstants;

/**
 * Created by oubin on 2018/2/28.
 */

public class PlaybackEvent {

    public static final String PLAYBACK_STOPPED = ApiConstants.Events.PlaybackStopped.NAME;
    public static final String PLAYBACK_FINISHED = ApiConstants.Events.PlaybackFinished.NAME;
    public static final String PLAYBACK_PAUSED = ApiConstants.Events.PlaybackPaused.NAME;
    public static final String PLAYBACK_RESUMED = ApiConstants.Events.PlaybackResumed.NAME;
    public static final String PLAYBACK_STARTED = ApiConstants.Events.PlaybackStarted.NAME;
    public static final String PLAYBACK_FAILED = ApiConstants.Events.PlaybackFailed.NAME;

}
