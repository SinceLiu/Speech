package com.readboy.watch.speech;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);

        Semaphore semaphore = new Semaphore(1, true);
        String jsonStr = "{name:[\"zhang\", \"ou\"]}";
        JSONObject object = new JSONObject(jsonStr);
        JSONArray names = object.optJSONArray("name");


    }


    public static class Builder {
        private String name;

        public Builder() {

        }
    }

}