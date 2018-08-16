package com.readboy.watch.speech;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void additionIsCorrect() throws Exception {
//        assertEquals(4, 2 + 2);
//
//        Semaphore semaphore = new Semaphore(1, true);
//        String jsonStr = "{name:[\"zhang\", \"ou\"]}";
//        JSONObject object = new JSONObject(jsonStr);
//        JSONArray names = object.optJSONArray("name");

//        String imei = "jladfja@qq.com";
//        String regex = "/^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\\.[a-zA-Z0-9_-])+$/";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(imei);
//        boolean result = matcher.find();

        int a = 'a';
        int k = 'k';
        int d = k -a;
        int p = 22 % 22 + 16;
        

        String str1 = "zhong中";
        String str2 = "zhogn";
        String str3 = "中国";
        int length1 = str1.length();
        int length2 = str2.length();
        int length3 = str3.length();


    }

    @Test
    public void regexTest() throws Exception{

    }


    public static class Builder {
        private String name;

        public Builder() {

        }
    }

}