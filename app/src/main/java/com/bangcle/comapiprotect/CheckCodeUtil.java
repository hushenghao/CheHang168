package com.bangcle.comapiprotect;

import android.content.Context;
import android.content.SharedPreferences;

/* loaded from: classes.dex */
public class CheckCodeUtil {
    public static final int DATA_TYPE_JSON = 1;
    public static final int DATA_TYPE_OTHER = 2;
    public static final int DATA_TYPE_XML = 0;
    private static final String SAVE_TIME_KEY = "timeDifference";
    private static final String SHARE_TAG = "checkCodeUtilServiceInfo";
    private static SharedPreferences userInfo = null;
    private static CheckCodeUtil util = null;
    public static int value = 1;

    public native String checkcode(String str, int i, String str2);

    public native String decheckcode(String str);

    public static void set() {
        int i = value;
        if (i > 10000) {
            value = 2;
        } else {
            value = i * 2;
        }
    }

    public static CheckCodeUtil getInstance(Context context) {
        if (util == null) {
            util = new CheckCodeUtil();
        }
        if (userInfo == null && context != null) {
            userInfo = context.getSharedPreferences(SHARE_TAG, 0);
        }
        return util;
    }

    public boolean updateServiceTime(long j) {
        if (userInfo == null) {
            return false;
        }
        return userInfo.edit().putLong(SAVE_TIME_KEY, j - System.currentTimeMillis()).commit();
    }

    public String checkcode(String str, int i) {
        long currentTimeMillis = System.currentTimeMillis();
        SharedPreferences sharedPreferences = userInfo;
        long j = sharedPreferences != null ? sharedPreferences.getLong(SAVE_TIME_KEY, 0L) : 0L;
        return checkcode(str, i, String.valueOf(currentTimeMillis + j));
    }

    static {
        System.loadLibrary("encrypt");
    }
}
