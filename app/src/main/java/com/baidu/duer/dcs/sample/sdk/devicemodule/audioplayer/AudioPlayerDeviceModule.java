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
package com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer;

import android.util.Log;

import com.baidu.duer.dcs.api.BaseDeviceModule;
import com.baidu.duer.dcs.api.IChannelMediaPlayer;
import com.baidu.duer.dcs.api.IMessageSender;
import com.baidu.duer.dcs.api.player.IMediaPlayer;
import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.*;
import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.message.ClearQueuePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.message.PlayPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.message.PlaybackStatePayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.message.StopPayload;
import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.report.AudioPlayStateReport;
import com.baidu.duer.dcs.sample.sdk.devicemodule.audioplayer.report.AudioPlayerReporter;
import com.baidu.duer.dcs.util.message.ClientContext;
import com.baidu.duer.dcs.util.message.Directive;
import com.baidu.duer.dcs.util.message.HandleDirectiveException;
import com.baidu.duer.dcs.util.message.Header;
import com.baidu.duer.dcs.util.util.LogUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 音乐播放的端能力实现，处理指令：Play，Stop，ClearQueue
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/31.
 */
public class AudioPlayerDeviceModule extends BaseDeviceModule {
    private static final String TAG = "oubin_AudioPlayerModule";
    // 播放列表，先进先出
    private LinkedList<PlayPayload.Stream> playQueue = new LinkedList<>();
    // 当前stream的token
    private String latestStreamToken = "";
    // 当前manager的播放器
    private IChannelMediaPlayer mediaPlayer;
    // 播放上报
    private AudioPlayStateReport audioPlayStateReport;
    // 开始时的缓冲时间
    private long bufferingStartMilliseconds;
    // 结束时的缓冲时间
    private long bufferingEndMilliseconds;
    // 回调接口
    private List<IMediaPlayer.IMediaPlayerListener> audioPlayerListeners;

    private long mStoppedOffsetInMilliseconds = 0;
    private AudioPlayerReporter audioPlayerReporter;

    public AudioPlayerDeviceModule(IChannelMediaPlayer mediaPlayer,
                                   IMessageSender messageSender) {
        super(ApiConstants.NAMESPACE, messageSender);
        this.mediaPlayer = mediaPlayer;
        this.mediaPlayer.addMediaPlayerListener(mediaPlayerListener);
        this.audioPlayStateReport = new AudioPlayStateReport(getNameSpace(),
                messageSender,
                audioPlayStateReportListener);
        this.audioPlayerListeners = new CopyOnWriteArrayList<>();
        this.audioPlayerReporter = new AudioPlayerReporter();
        this.audioPlayerReporter.setProgressReporterListener(progressReporterListener);
    }

    private AudioPlayerReporter.IProgressReporterListener progressReporterListener = new AudioPlayerReporter.IProgressReporterListener() {
        @Override
        public void progressReportDelay() {
            audioPlayStateReport.reportProgressDelay();
        }

        @Override
        public void progressReportInterval() {
            audioPlayStateReport.reportProgressInterval();
        }
    };

    @Override
    public ClientContext clientContext() {
        return getClientContext();
    }

    private ClientContext getClientContext() {
        String namespace = ApiConstants.NAMESPACE;
        String name = ApiConstants.Events.PlaybackState.NAME;
        Header header = new Header(namespace, name);
        PlaybackStatePayload payload = new PlaybackStatePayload(latestStreamToken,
                mediaPlayer.getCurrentPosition(),
                audioPlayStateReport.getState().name());
        return new ClientContext(header, payload);
    }

    @Override
    public void handleDirective(Directive directive) throws HandleDirectiveException {
        String directiveName = directive.getName();
        Log.d(TAG, "handleDirective: directive = " + directive.toString());
        Log.d(TAG, "handleDirective: directiveName = " + directiveName);
        LogUtil.dcf(TAG, "dcs-speak-directiveName:" + directiveName);
        if (ApiConstants.Directives.Play.NAME.equals(directiveName)) {
            handlePlay((PlayPayload) directive.getPayload());
        } else if (ApiConstants.Directives.Stop.NAME.equals(directiveName)) {
            handleStop((StopPayload) directive.getPayload());
        } else if (ApiConstants.Directives.ClearQueue.NAME.equals(directiveName)) {
            handleClearQueue((ClearQueuePayload) directive.getPayload());
        } else {
            String message = "audioPlayer cannot handle the directive";
            throw (new HandleDirectiveException(
                    HandleDirectiveException.ExceptionType.UNSUPPORTED_OPERATION, message));
        }
    }

    @Override
    public HashMap<String, Class<?>> supportPayload() {
        HashMap<String, Class<?>> map = new HashMap<>();
        map.put(getNameSpace() + ApiConstants.Directives.Play.NAME, PlayPayload.class);
        map.put(getNameSpace() + ApiConstants.Directives.Stop.NAME, StopPayload.class);
        map.put(getNameSpace() + ApiConstants.Directives.ClearQueue.NAME, ClearQueuePayload.class);
        return map;
    }

    /**
     * 处理播放指令（Play）
     *
     * @param payload payload
     */
    private void handlePlay(PlayPayload payload) {
        PlayPayload.AudioItem item = payload.audioItem;
        if (payload.playBehavior == PlayPayload.PlayBehavior.REPLACE_ALL) {
            clearAll();
        } else if (payload.playBehavior == PlayPayload.PlayBehavior.REPLACE_ENQUEUED) {
            clearEnqueued();
        }
        final PlayPayload.Stream stream = item.stream;
        String streamUrl = stream.url;
        String streamId = stream.token;
        long offset = stream.offsetInMilliseconds;
        LogUtil.icf(TAG, "URL:" + streamUrl);
        LogUtil.icf(TAG, "StreamId:" + streamId);
        LogUtil.icf(TAG, "Offset:" + offset);
        add(stream);
    }

    /**
     * 处理停止指令（Stop）
     *
     * @param payload payload
     */
    private void handleStop(StopPayload payload) {
        stop();
    }

    /**
     * 处理清空队列指令（Stop）
     *
     * @param clearQueuePayload clearQueuePayload
     */
    private void handleClearQueue(ClearQueuePayload clearQueuePayload) {
        // 清除播放列表，并停止当前播放的音频（如果有）
        if (clearQueuePayload.clearBehavior == ClearQueuePayload.ClearBehavior.CLEAR_ALL) {
            audioPlayStateReport.clearQueueAll();
            clearAll();
        } else if (clearQueuePayload.clearBehavior == ClearQueuePayload.ClearBehavior
                .CLEAR_ENQUEUED) {
            // 清除播放列表，但不影响当前播放
            audioPlayStateReport.clearQueueEnqueued();
            clearEnqueued();
        }
    }

    private void add(PlayPayload.Stream stream) {
        String expectedPreviousToken = stream.expectedPreviousToken;
        boolean startPlaying = playQueue.isEmpty();
        if (expectedPreviousToken == null || latestStreamToken.isEmpty()
                || latestStreamToken.equals(expectedPreviousToken)) {
            playQueue.add(stream);
        }
        LogUtil.dcf(TAG, " coming  playQueue size :" + playQueue.size());
        if (startPlaying) {
            startPlay();
        }
    }

    /**
     * 开始播放音乐
     */
    private void startPlay() {
        if (playQueue.isEmpty()) {
            LogUtil.dcf(TAG, "startPlay-playQueue isEmpty ！！");
            return;
        }
        PlayPayload.Stream currentStream = playQueue.peek();
        if (currentStream == null) {
            return;
        }
        latestStreamToken = currentStream.token;
        String url = currentStream.url;
        // 从哪个位置开始播放
        long offset = currentStream.offsetInMilliseconds;
        // 判断是否是流类型还是URL类型
        mediaPlayer.setActive(true);
        if (currentStream.hasAttachedContent()) {
            mediaPlayer.play(new IMediaPlayer.MediaResource(currentStream.getAttachedContent()));
        } else {
            mediaPlayer.play(new IMediaPlayer.MediaResource(url));
        }

        mediaPlayer.seekTo((int) offset);
    }

    private IMediaPlayer.IMediaPlayerListener mediaPlayerListener = new IMediaPlayer
            .SimpleMediaPlayerListener() {
        // 是否处于暂停
        private boolean isPause;
        // 是否第一次到达了100
        private boolean stutterFinished;
        // 是否处于缓冲中
        private boolean bufferUnderRunInProgress;
        private boolean isNearlyFinished;
        private PlayPayload.Stream stream;
        private boolean isProgressReportRequired;

        @Override
        public void onInit() {
            super.onInit();
            LogUtil.dcf(TAG, "onInit");
            isPause = false;
            stutterFinished = false;
            bufferUnderRunInProgress = false;
            isNearlyFinished = false;
            stream = playQueue.peek();
            if (stream != null) {
                isProgressReportRequired = stream.getProgressReportRequired();
                audioPlayerReporter.setInfo(stream.offsetInMilliseconds, stream.progressReport);
            }
        }

        @Override
        public void onPrepared() {
            super.onPrepared();
            LogUtil.dcf(TAG, "onPrepared");
            fireOnPrepared();
            if (mediaPlayer != null) {
                fireOnDuration(mediaPlayer.getDuration());
                audioPlayerReporter.setDuration(mediaPlayer.getDuration());
            }
        }

        @Override
        public void onPlaying() {
            super.onPlaying();
            LogUtil.dcf(TAG, "onPlaying");
            // 暂停后继续播放
            if (isPause) {
                isPause = false;
                audioPlayStateReport.playbackResumed();
            } else {
                // 第一次播放
                LogUtil.dcf(TAG, "onPlaying-Duration：" + mediaPlayer.getDuration());
                // 上报PlaybackStarted事件
                audioPlayStateReport.playbackStarted();
            }
            fireOnPlaying();
        }

        @Override
        public void onPaused() {
            LogUtil.dcf(TAG, "onPaused");
            isPause = true;
            audioPlayStateReport.playbackPaused();
            fireOnPaused();
        }

        @Override
        public void onStopped() {
            super.onStopped();
            audioPlayStateReport.playbackStopped();
            fireOnStopped();
        }

        @Override
        public void onCompletion() {
            LogUtil.dcf(TAG, "onCompletion");
            playQueue.poll();
            audioPlayStateReport.playbackFinished();
            fireOnCompletion();
            if (playQueue.isEmpty()) {
                mediaPlayer.setActive(false);
            } else {
                startPlay();
            }
        }

        @Override
        public void onRelease() {
            LogUtil.dcf(TAG, "onRelease");
            fireOnRelease();
        }

        @Override
        public void onError(String error, IMediaPlayer.ErrorType errorType) {
            LogUtil.dcf(TAG, "onError");
            playQueue.clear();
            audioPlayStateReport.playbackFailed(errorType);
            fireOnError(error, errorType);
            mediaPlayer.setActive(false);
        }

        @Override
        public void onBufferingUpdate(int percent) {
            LogUtil.dc(TAG, "onBufferingUpdate：" + percent);
            fireOnBufferingUpdate(percent);
            // 已经缓冲完成了
            if (stutterFinished) {
                return;
            }
            float currPos = mediaPlayer.getCurrentPosition();
            long duration = mediaPlayer.getDuration();
            float result = (currPos / duration) * 100;
            LogUtil.dc(TAG, "rel-percent " + result + ",duration=" + duration + "currPos= "
                    + currPos + "buf-percent = " + percent);
            if (Math.abs(result - percent) > 0.5) {
                if (result >= percent) {
                    if (!bufferUnderRunInProgress) {
                        LogUtil.dc(TAG, "Start ");
                        bufferingStartMilliseconds = System.currentTimeMillis();
                        audioPlayStateReport.playbackStutterStarted();
                        bufferUnderRunInProgress = true;
                    }
                } else if (result < percent) {
                    if (bufferUnderRunInProgress) {
                        LogUtil.dc(TAG, " Finish ");
                        bufferingEndMilliseconds = System.currentTimeMillis();
                        audioPlayStateReport.playbackStutterFinished();
                        bufferUnderRunInProgress = false;
                    }
                }
            }
            if (percent >= 99) {
                stutterFinished = true;
            }
        }

        @Override
        public void onUpdateProgress(int percent) {
            super.onUpdateProgress(percent);
            fireUpdateProgress(percent);
            LogUtil.dc(TAG, "onUpdateProgress:" + percent);
            if (percent >= 90 && !isNearlyFinished) {
                LogUtil.dc(TAG, "playbackNearlyFinished ok. ");
                isNearlyFinished = true;
                audioPlayStateReport.playbackNearlyFinished();
            }
            if (isProgressReportRequired) {
                audioPlayerReporter.handleProgressReportDelay(percent);
                audioPlayerReporter.handleProgressReportInterval(percent);
            }
        }
    };

    private void clearAll() {
        stop();
        playQueue.clear();
    }

    private void clearEnqueued() {
        PlayPayload.Stream top = playQueue.poll();
        playQueue.clear();
        if (top != null) {
            playQueue.add(top);
        }
    }

    private void stop() {
        if (!playQueue.isEmpty() && isPlayingOrPaused()) {
            // 要把播放策略中的对应的那一条删除了
            mStoppedOffsetInMilliseconds = mediaPlayer.getCurrentPosition();
            mediaPlayer.stop();
            playQueue.poll();
            // latestStreamToken = "";
        }
    }

    private boolean isPlaying() {
        return (audioPlayStateReport.getState() == AudioPlayStateReport.AudioPlayerState.PLAYING
                || audioPlayStateReport.getState() == AudioPlayStateReport.AudioPlayerState.PAUSED
                || audioPlayStateReport.getState() == AudioPlayStateReport.AudioPlayerState
                .BUFFER_UNDERRUN);
    }

    private boolean isPlayingOrPaused() {
        return isPlaying() || audioPlayStateReport.getState() == AudioPlayStateReport
                .AudioPlayerState.PAUSED;
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer.removeMediaPlayerListener(mediaPlayerListener);
        }
        audioPlayerListeners.clear();
    }

    public void seekTo(int milliseconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(milliseconds);
            if (mediaPlayer.getPlayState() != IMediaPlayer.PlayState.PLAYING) {
                long duration = mediaPlayer.getDuration();
                long currentPosition = mediaPlayer.getCurrentPosition();
                if (duration > 0 && currentPosition >= 0) {
                    int percent = (int) (currentPosition * 100 / duration);
                    if (percent >= 0 && percent <= 100) {
                        fireUpdateProgress(percent);
                    }
                }
            }
        }

    }

    /**
     * 播放上报时需要的信息
     */
    private AudioPlayStateReport.AudioPlayStateReportListener audioPlayStateReportListener =
            new AudioPlayStateReport.AudioPlayStateReportListener() {
                @Override
                public ClientContext getClientContext() {
                    return AudioPlayerDeviceModule.this.getClientContext();
                }

                @Override
                public String getCurrentStreamToken() {
                    return latestStreamToken;
                }

                @Override
                public long getMediaPlayerCurrentOffsetInMilliseconds() {
                    if (mediaPlayer.getDuration() == 0 && mediaPlayer.getCurrentPosition() == 0) {
                        return mStoppedOffsetInMilliseconds;
                    }
                    return mediaPlayer.getCurrentPosition();
                }

                @Override
                public long getStutterDurationInMilliseconds() {
                    // 缓冲时间ms
                    return bufferingEndMilliseconds - bufferingStartMilliseconds;
                }
            };

    private void fireOnPrepared() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onPrepared();
        }
    }

    private void fireOnRelease() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onRelease();
        }
    }

    private void fireOnPlaying() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onPlaying();
        }
    }

    private void fireOnPaused() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onPaused();
        }
    }

    private void fireOnStopped() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onStopped();
        }
    }

    private void fireOnCompletion() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onCompletion();
        }
    }

    private void fireOnError(String error, IMediaPlayer.ErrorType errorType) {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onError(error, errorType);
        }
    }

    private void fireOnBufferingUpdate(int percent) {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onBufferingUpdate(percent);
        }
    }

    private void fireBufferingStart() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onBufferingStart();
        }
    }

    private void fireBufferingEnd() {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onBufferingEnd();
        }
    }

    private void fireUpdateProgress(int percent) {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onUpdateProgress(percent);
        }
    }

    private void fireOnDuration(long milliseconds) {
        for (IMediaPlayer.IMediaPlayerListener listener : audioPlayerListeners) {
            listener.onDuration(milliseconds);
        }
    }

    public void addAudioPlayListener(IMediaPlayer.IMediaPlayerListener listener) {
        audioPlayerListeners.add(listener);
    }

    public void removeAudioPlayListener(IMediaPlayer.IMediaPlayerListener listener) {
        audioPlayerListeners.remove(listener);
    }

    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public String getLatestStreamToken() {
        return latestStreamToken;
    }

    public AudioPlayStateReport getAudioPlayStateReport() {
        return audioPlayStateReport;
    }

}
