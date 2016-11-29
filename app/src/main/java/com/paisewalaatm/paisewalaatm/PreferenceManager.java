package com.paisewalaatm.paisewalaatm;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by krishna on 14/11/16.
 */

public class PreferenceManager {
    private static final String SHARED_PREF_NAME = "PREF_PAISE_WALA_ATM";
    private static final String PREF_KEY_LOCATION = "PREF_KEY_LOCATION";
    private static final String PREF_KEY_ATMS = "PREF_KEY_ATMS";
    private static final String PREF_KEY_ATM_NAME = "PREF_KEY_ATM_NAME";
    private static final String PREF_KEY_TIME = "PREF_KEY_TIME";

    private static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveCurLocation(Context context, String location) {
        getSharedPref(context).edit()
                .putString(PREF_KEY_LOCATION, location)
                .apply();
    }

    public static String getLocation(Context context) {
        return getSharedPref(context).getString(PREF_KEY_LOCATION, "");
    }

    public static void saveAtms(Context context, String atms) {
        getSharedPref(context).edit()
                .putString(PREF_KEY_ATMS, atms)
                .apply();
    }

    public static String getAtms(Context context) {
        return getSharedPref(context).getString(PREF_KEY_ATMS, "");
    }

    public static void saveAtmDetailsToSync(Context context, String atmName, String timeStamp, String location) {
        getSharedPref(context).edit()
                .putString(PREF_KEY_ATM_NAME, atmName)
                .putString(PREF_KEY_TIME, timeStamp)
                .putString(PREF_KEY_LOCATION, location)
                .apply();
    }

    public static void clearAtmDetalToSync(Context context) {
        getSharedPref(context).edit()
                .remove(PREF_KEY_ATM_NAME)
                .remove(PREF_KEY_TIME)
                .apply();
    }

    public static String getAtmName(Context context) {
        return getSharedPref(context).getString(PREF_KEY_ATM_NAME, "");
    }

    public static String getTime(Context context) {
        return getSharedPref(context).getString(PREF_KEY_TIME, "");
    }

    public static String getAtmDetailJsonToSync(Context context) {
        SharedPreferences pref = getSharedPref(context);
        String detailJson =
                "atmName:" + pref.getString(PREF_KEY_ATM_NAME, "")
                        + "time:" + pref.getString(PREF_KEY_TIME, "")
                        + "location:" + pref.getString(PREF_KEY_LOCATION, "");
        return detailJson;
    }
}
