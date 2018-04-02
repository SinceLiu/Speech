package com.baidu.duer.dcs.sample.sdk.util;

import android.content.Context;

import com.baidu.duer.dcs.util.util.PreferenceUtil;

/**
 *
 * @author oubin
 * @date 2018/4/2
 */

public class UploadPreference {

    public static final String KEY_LAST_UPLOAD_PHONE_CONTACTS = "com.baidu.duer.dcs.framework.upload.contact.KEY_LAST_UPLOAD_CONTACTSMD5";
    public static final String KEY_LAST_UPLOAD_WECHAT_CONTACTS = "com.baidu.duer.dcs.framework.upload.wechatcontact.KEY_LAST_UPLOAD_CONTACTSMD5";

    public UploadPreference() {
    }

    public static void savePhoneContacts(Context context, String contacts) {
        PreferenceUtil.put(context, "com.baidu.duer.dcs.framework.upload.contact.KEY_LAST_UPLOAD_CONTACTSMD5", contacts);
    }

    public static String getLastUploadPhoneContacts(Context context) {
        return (String) PreferenceUtil.get(context, "com.baidu.duer.dcs.framework.upload.contact.KEY_LAST_UPLOAD_CONTACTSMD5", "");
    }

    public static void saveWechatContacts(Context context, String contacts) {
        PreferenceUtil.put(context, "com.baidu.duer.dcs.framework.upload.wechatcontact.KEY_LAST_UPLOAD_CONTACTSMD5", contacts);
    }

    public static String getLastUploadWechatContacts(Context context) {
        return (String) PreferenceUtil.get(context, "com.baidu.duer.dcs.framework.upload.wechatcontact.KEY_LAST_UPLOAD_CONTACTSMD5", "");
    }
}
