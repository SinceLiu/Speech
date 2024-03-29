package com.readboy.watch.speech;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.readboy.watch.speech", appContext.getPackageName());

        ComponentName service = new ComponentName("com.readboy.watch.speech", "TestService");
        JobInfo.Builder builder = new JobInfo.Builder(1, service);
        builder.setClipData();
        builder.setRequiresDeviceIdle()
    }
}
