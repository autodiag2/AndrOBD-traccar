package com.autodiag2.androbd.plugin.traccar;

import android.content.Context;
import android.content.SharedPreferences;

public final class SettingsStore {

    private static final String PREFS = "traccar_settings";

    public static String getHost(Context c) {
        return prefs(c).getString("host", "172.20.10.4");
    }

    public static int getPort(Context c) {
        return prefs(c).getInt("port", 5055);
    }

    public static String getDeviceId(Context c) {
        return prefs(c).getString("device", "some");
    }

    public static void set(Context c, String host, int port, String device) {
        prefs(c).edit()
                .putString("host", host)
                .putInt("port", port)
                .putString("device", device)
                .apply();
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}