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

/**
 * 打电话，手机通信录相关工具类
 * @author oubin
 * @date 2018/2/27
 */

public final class ContactsUtils {
    private static final String TAG = "ContactsUtils";

    public static boolean callByName(Context context, String name) {
        String number = getNumByName(context, name);
        if (!TextUtils.isEmpty(number)) {
            return callByNumber(context, number);
        }
        return false;
    }

    public static boolean callByNumber(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + number);
        intent.setData(data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                context.checkSelfPermission(Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent);
            return true;
        } else {
            Log.e(TAG, "contacts: 没有打电话权限");
            return false;
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
