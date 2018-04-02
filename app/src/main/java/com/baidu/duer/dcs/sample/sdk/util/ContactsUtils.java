package com.baidu.duer.dcs.sample.sdk.util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 打电话，手机通信录相关工具类
 * @author oubin
 * @date 2018/2/27
 */

public final class ContactsUtils {
    private static final String TAG = "ContactsUtils";

    public static String getAllContacts(Context context) throws JSONException {
        JSONArray array = new JSONArray();
        ContentResolver resolver = context.getContentResolver();
        Cursor phoneCursor = null;

        try {
            phoneCursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, new String[]{"_id", "display_name"}, (String) null, (String[]) null, (String) null);
            if (phoneCursor != null) {
                int column = phoneCursor.getColumnIndex("display_name");

                while (phoneCursor.moveToNext() && column > -1) {
                    String displayName = phoneCursor.getString(column);
                    if (!TextUtils.isEmpty(displayName)) {
                        JSONObject object = new JSONObject();
                        object.put("name", displayName);
                        array.put(object);
                    }
                }
            }
        } finally {
            if (null != phoneCursor) {
                phoneCursor.close();
            }

        }

        return array.toString();
    }

    public static boolean callByName(Context context, String name) {
        String number = getNumByName(context, name);
        if (!TextUtils.isEmpty(number)) {
            return callByNumber(context, number);
        }
        return false;
    }

    public static boolean callByNumber(Context context, String number) {
        Log.e(TAG, "callByNumber() called with: context = " + context + ", number = " + number + "");
        String newNumber = number.replace(" ", "");
        Log.e(TAG, "callByNumber: newNumber = " + newNumber);
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + newNumber);
        intent.setData(data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                context.startActivity(intent);
                return true;
            } else {
                Log.e(TAG, "callByNumber contacts: 没有打电话权限");
                return false;
            }
        } else {
            context.startActivity(intent);
            return true;
        }
    }

    /**
     * @return first phoneNum or the home number.
     */
    private static String getNumByName(Context context, String name) {
        ContentResolver resolver = context.getContentResolver();
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?";
        String result = null;
        try (Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{"data1"}, selection, new String[]{name}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String number = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (TextUtils.isEmpty(result)) {
                        result = number;
                    } else if (!TextUtils.isEmpty(number) && number.length() < result.length()) {
                        result = number;
                    }
                } while (cursor.moveToNext());
            }
        }
        return result;
    }

    public class SortModel {
        private String contactName;
        private String phoneNumber;
        String sortKey;
        SortToken sortToken;

        SortModel(String contactName, String phoneNumber) {
            this.contactName = contactName;
            this.phoneNumber = phoneNumber;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        @Override
        public String toString() {
            return contactName + ";" + phoneNumber + ";"
                    + sortKey + ";" + sortToken;
        }
    }

    private class SortToken {
        String simpleSpell = "";
        String wholeSpell = "";

        @Override
        public String toString() {
            return "simpleSpell = " + simpleSpell + "; wholeSpell = " + wholeSpell;
        }
    }
}
