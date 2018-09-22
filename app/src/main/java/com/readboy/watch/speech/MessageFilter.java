package com.readboy.watch.speech;

/**
 * @author oubin
 * @date 2018/8/30
 */
public class MessageFilter {

    public static String filter(String message) {
        if (message == null || message.length() < 1) {
            return message;
        }
        String first = message.substring(0, 1);
        int index = message.indexOf(first, 1);
        if (index > 1) {
            String temp1 = message.substring(0, index);
            String temp2 = message.substring(index, message.length());
            if (temp1.equals(temp2)) {
                return temp1;
            }
        }
        return message;
    }
}
