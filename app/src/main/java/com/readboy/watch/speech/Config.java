package com.readboy.watch.speech;


/**
 *
 * @author oubin
 * @date 2018/3/30
 */

public final class Config {

    public static final boolean WAKEUP_ENABLE = false;

    /**
     * 恢复tts，音乐等有关播放。策略一，恢复状态，恢复声音。
     * resumeSpeaker();
     * 策略二：只恢复状态，不恢复声音。
     * 调用getInternalApi().pauseSpeaker()会让其标志位为true。
     * 恢复内部播放状态，如果该标志位为true（），则会导致没有声音。
     * getDcsSdkImpl().getFramework().multiChannelMediaPlayer.a(false)
     */
    public static final boolean RESUME_SPEAKER_SOUND = false;

}
