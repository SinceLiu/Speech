/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.report;

import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.ApiConstants;
import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.message.AudioPlayerPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.message.PlaybackFailedPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.message.PlaybackStutterFinishedPayload;
import com.baidu.duer.dcs.api.IMessageSender;
import com.baidu.duer.dcs.util.message.ClientContext;
import com.baidu.duer.dcs.util.message.Event;
import com.baidu.duer.dcs.util.message.Header;
import com.baidu.duer.dcs.util.message.MessageIdHeader;
import com.baidu.duer.dcs.util.message.Payload;
import com.baidu.duer.dcs.api.player.IMediaPlayer;
import com.baidu.duer.dcs.util.util.LogUtil;

import java.util.ArrayList;

/**
 * Audio Player模块各种事件上报的处理，同时维护当前的端状态
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/1.
 */
public class AudioPlayStateReport {
    public static final String TAG = "AudioPlayStateReport";

    public enum AudioPlayerState {
        IDLE,
        PLAYING,
        PAUSED,
        FINISHED,
        STOPPED,
        BUFFER_UNDERRUN
    }

    private AudioPlayerState currentState = AudioPlayerState.FINISHED;
    private IMessageSender messageSender;
    private String namespace;
    private AudioPlayStateReportListener audioPlayStateReportListener;
    private int errorRetryCount;
    private static final int MAX_ERROR_RETRY_COUNT = 5;


    public AudioPlayStateReport(String namespace, IMessageSender messageSender,
                                AudioPlayStateReportListener audioPlayStateReportListener) {
        this.namespace = namespace;
        this.messageSender = messageSender;
        this.audioPlayStateReportListener = audioPlayStateReportListener;

    }

    public AudioPlayerState getState() {
        return currentState;
    }

    public void playbackResumed() {
        currentState = AudioPlayerState.PLAYING;
        Event event = createAudioPlayerEvent(ApiConstants.Events.PlaybackResumed.NAME,
                audioPlayStateReportListener.getCurrentStreamToken(),
                audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds());
        messageSender.sendEvent(event, null);
    }

    public void playbackFinished() {
        currentState = AudioPlayerState.FINISHED;
        Event event = createAudioPlayerEvent(ApiConstants.Events.PlaybackFinished.NAME,
                audioPlayStateReportListener.getCurrentStreamToken(),
                audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds());
        messageSender.sendEvent(event, null);
    }

    public void playbackStarted() {
        errorRetryCount = 0;
        currentState = AudioPlayerState.PLAYING;
        Event event = createAudioPlayerEvent(ApiConstants.Events.PlaybackStarted.NAME,
                audioPlayStateReportListener.getCurrentStreamToken(),
                audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds());
        messageSender.sendEvent(event, null);
    }

    public void playbackFailed(IMediaPlayer.ErrorType errorType) {
        if (errorRetryCount >= MAX_ERROR_RETRY_COUNT) {
            LogUtil.ecf(TAG, "report playbackFailed event errorRetryCount>=5,return !");
            return;
        }
        errorRetryCount++;
        currentState = AudioPlayerState.STOPPED;
        Header header = new MessageIdHeader(namespace,
                ApiConstants.Events.PlaybackFailed.NAME);
        Event event = new Event(header,
                new PlaybackFailedPayload(audioPlayStateReportListener.getCurrentStreamToken(),
                        errorType));
        ArrayList<ClientContext> list = new ArrayList<>();
        list.add(audioPlayStateReportListener.getClientContext());
        messageSender.sendEvent(event, list, null);
    }

    public void playbackPaused() {
        currentState = AudioPlayerState.PAUSED;
        Event event = createAudioPlayerEvent(ApiConstants.Events.PlaybackPaused.NAME,
                audioPlayStateReportListener.getCurrentStreamToken(),
                audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds());
        messageSender.sendEvent(event, null);
    }

    public void playbackNearlyFinished() {
        currentState = AudioPlayerState.FINISHED;
        Event event = createAudioPlayerEvent(ApiConstants.Events.PlaybackNearlyFinished.NAME,
                audioPlayStateReportListener.getCurrentStreamToken(),
                audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds());
        messageSender.sendEvent(event, null);
    }

    public void playbackStutterStarted() {
        currentState = AudioPlayerState.BUFFER_UNDERRUN;
        Event event = createAudioPlayerEvent(ApiConstants.Events.PlaybackStutterStarted.NAME,
                audioPlayStateReportListener.getCurrentStreamToken(),
                audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds());
        messageSender.sendEvent(event, null);
    }

    public void playbackStutterFinished() {
        currentState = AudioPlayerState.PLAYING;
        Event event = createAudioPlayerPlaybackStutterFinishedEvent(
                audioPlayStateReportListener.getCurrentStreamToken(),
                audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds(),
                audioPlayStateReportListener.getStutterDurationInMilliseconds()
        );
        messageSender.sendEvent(event, null);
    }

    public void playbackStopped() {
        currentState = AudioPlayerState.STOPPED;
        Event event = createAudioPlayerEvent(ApiConstants.Events.PlaybackStopped.NAME,
                audioPlayStateReportListener.getCurrentStreamToken(),
                audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds());
        messageSender.sendEvent(event, null);
    }

    public void clearQueueAll() {
        Event event = createAudioPlayerPlaybackQueueClearedEvent();
        messageSender.sendEvent(event, null);
        if (currentState == AudioPlayerState.PLAYING || currentState == AudioPlayerState.PAUSED
                || currentState == AudioPlayerState.BUFFER_UNDERRUN) {
            currentState = AudioPlayerState.STOPPED;
            Event eventStopped = createAudioPlayerEvent(ApiConstants.Events.PlaybackStopped.NAME,
                    audioPlayStateReportListener.getCurrentStreamToken(),
                    audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds());
            messageSender.sendEvent(eventStopped, null);
        }
    }

    public void clearQueueEnqueued() {
        Event event = createAudioPlayerPlaybackQueueClearedEvent();
        messageSender.sendEvent(event, null);
    }

    private Event createAudioPlayerEvent(String name, String streamToken,
                                         long offsetInMilliseconds) {
        Header header = new MessageIdHeader(namespace, name);
        Payload payload = new AudioPlayerPayload(streamToken, offsetInMilliseconds);
        return new Event(header, payload);
    }

    private Event createAudioPlayerPlaybackQueueClearedEvent() {
        Header header = new MessageIdHeader(ApiConstants.NAMESPACE,
                ApiConstants.Events.PlaybackQueueCleared.NAME);
        return new Event(header, new Payload());
    }

    private Event createAudioPlayerPlaybackStutterFinishedEvent(String streamToken,
                                                                long offsetInMilliseconds,
                                                                long stutterDurationInMilliseconds) {
        Header header = new MessageIdHeader(ApiConstants.NAMESPACE,
                ApiConstants.Events.PlaybackStutterFinished.NAME);
        return new Event(header, new PlaybackStutterFinishedPayload(streamToken,
                offsetInMilliseconds, stutterDurationInMilliseconds));
    }

    public void reportProgressDelay() {
        currentState = AudioPlayerState.PLAYING;
        Event event = createAudioPlayerEvent(ApiConstants.Events.ProgressReportDelayElapsed.NAME,
                audioPlayStateReportListener.getCurrentStreamToken(),
                audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds());
        messageSender.sendEvent(event, null);
    }

    public void reportProgressInterval() {
        currentState = AudioPlayerState.PLAYING;
        Event event = createAudioPlayerEvent(ApiConstants.Events.ProgressReportIntervalElapsed.NAME,
                audioPlayStateReportListener.getCurrentStreamToken(),
                audioPlayStateReportListener.getMediaPlayerCurrentOffsetInMilliseconds());
        messageSender.sendEvent(event, null);
    }

    public interface AudioPlayStateReportListener {
        ClientContext getClientContext();

        String getCurrentStreamToken();

        long getMediaPlayerCurrentOffsetInMilliseconds();

        long getStutterDurationInMilliseconds();
    }
}
