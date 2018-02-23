package com.readboy.watch.speech;

import android.os.Environment;

import java.io.File;

/**
 * Created by oubin on 2016/11/29.
 */

public final class Contracts {

    public static int RECORDING_MAX_MILLIS = 30000;

    public static boolean TEST_MODE = false;
    public static boolean INTERNET_ENABLE = true;
    public static boolean TEST_MUSIC_MODE = false;

    public static boolean DEBUG = false;

    public static final String DIR = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "speech";
    public static final String RECORDING_FILE = "recording.amr";
    public static final String PLAYING_FILE = "playing.mp3";
    public static final String TEMP_FILE = "temp.mp3";
    public static final String NO_NETWORK_FILE = DIR + "network.mp3";
    public static final String TIME_OUT_FILE = DIR + "timeout.mp3";
    public static final String INTERNAL_FILE = DIR + "internal.mp3";
    public static final String SERVER_FILE = DIR + "server.mp3";
    public static final String INTRODUCTION_FILE = DIR + "introduction.mp3";
    public static final String RECOGNITION_FILE = DIR + "recognition.mp3";
    public static final String RECOGNITION2_FILE = DIR + "recognition2.mp3";
    public static final String TEST_FILE = DIR + "test.mp3";

    public static final String GRAMMAR_FILE = DIR + "grammar.text";

    public static final String NO_RESULT_FILE1 = "noresult1.mp3";
    public static final String NO_RESULT_FILE2 = "noresult2.mp3";
    public static final String HELLO1 = "hello1.mp3";
    public static final String HELLO2 = "hello2.mp3";


    public static final int LOADING_MORE_MAX_TIME = 10;
    public static final int REQUEST_AGAIN_MAX_TIME = 5;

    //"poetry;translation;readboy;arithmetic;contacts;weather;story;joke;playmusic;baike";
    public static final String DOMAIN_POETRY = "poetry";
    public static final String DOMAIN_TRANSLATION = "translation";
    public static final String DOMAIN_READBOY = "readboy";
    public static final String DOMAIN_ARITHMETIC = "arithmetic";
    public static final String DOMAIN_CONTACTS = "contacts";
    public static final String DOMAIN_WEATHER = "weather";
    public static final String DOMAIN_STORY = "story";
    public static final String DOMAIN_JOKE = "joke";
    public static final String DOMAIN_PLAYMUSIC = "playmusic";
    public static final String DOMAIN_MUSIC = "music";
    public static final String DOMAIN_BAIKE = "baike";


}
