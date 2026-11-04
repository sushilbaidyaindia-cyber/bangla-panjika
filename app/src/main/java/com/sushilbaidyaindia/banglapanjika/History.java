package com.sushilbaidyaindia.banglapanjika;

import android.content.Context;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

/** Births, deaths, events and special days per calendar day, read from assets/history.json (made at build time). */
final class History {
    private History() {}

    private static JSONObject data;
    private static boolean loaded;

    static synchronized void load(Context c) {
        if (loaded) return;
        loaded = true;
        try (InputStream in = c.getAssets().open("history.json")) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            data = new JSONObject(new String(bo.toByteArray(), "UTF-8"));
        } catch (Throwable t) {
            data = null;                      // no file (or unreadable): the page just says the data is missing
        }
    }

    static synchronized JSONObject day(int month, int day) {
        if (data == null) return null;
        return data.optJSONObject(String.format(Locale.US, "%02d-%02d", month, day));
    }
}
