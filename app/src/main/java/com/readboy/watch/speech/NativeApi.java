package com.readboy.watch.speech;

/**
 *
 * @author oubin
 * @date 2018/4/10
 */

public final class NativeApi {

    static {
        System.loadLibrary("speech");
    }

    public native static String getDevKey();

}

