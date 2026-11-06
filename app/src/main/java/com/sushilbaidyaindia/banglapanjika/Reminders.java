package com.sushilbaidyaindia.banglapanjika;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The daily morning notice and the user's own reminders on a Bangla date.
 * One alarm a day, at the chosen time; it shows today's panjika (if switched on) and any reminder whose Bangla date is today.
 * Works offline.
 */
final class Reminders {
    private Reminders() { }

    static final String ACTION_FIRE = "com.sushilbaidyaindia.banglapanjika.FIRE";
    private static final String CHANNEL = "panjika_daily";

    static final class Item {
        final int month;       // 0 = Baishakh .. 11 = Chaitra
        final int day;         // 1..32
        final String text;
        Item(int month, int day, String text) { this.month = month; this.day = day; this.text = text; }
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("panjika", Context.MODE_PRIVATE);
    }

    // ------------------------------------------------------------------ settings
    static boolean dailyOn(Context c) { return prefs(c).getBoolean("daily_on", false); }
    static void setDailyOn(Context c, boolean on) { prefs(c).edit().putBoolean("daily_on", on).apply(); schedule(c); }
    static int hour(Context c) { return prefs(c).getInt("daily_hour", 6); }
    static int minute(Context c) { return prefs(c).getInt("daily_min", 0); }
    static void setTime(Context c, int h, int m) { prefs(c).edit().putInt("daily_hour", h).putInt("daily_min", m).apply(); schedule(c); }

    static String timeText(int h, int m) {
        String part = h < 4 ? "রাত" : h < 6 ? "ভোর" : h < 12 ? "সকাল" : h < 15 ? "দুপুর" : h < 18 ? "বিকেল" : h < 20 ? "সন্ধ্যা" : "রাত";
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return part + " " + BanglaText.digits(h12) + ":" + (m < 10 ? "০" : "") + BanglaText.digits(m);
    }

    // ------------------------------------------------------------------ the user's reminders
    static List<Item> load(Context c) {
        List<Item> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(prefs(c).getString("reminders", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                out.add(new Item(o.getInt("m"), o.getInt("d"), o.getString("t")));
            }
        } catch (Exception e) {
            // unreadable list: start empty
        }
        return out;
    }

    private static void save(Context c, List<Item> items) {
        JSONArray a = new JSONArray();
        try {
            for (Item it : items) a.put(new JSONObject().put("m", it.month).put("d", it.day).put("t", it.text));
        } catch (Exception e) {
            return;
        }
        prefs(c).edit().putString("reminders", a.toString()).apply();
        schedule(c);
    }

    static void add(Context c, int month, int day, String text) {
        List<Item> l = load(c);
        l.add(new Item(month, day, text));
        save(c, l);
    }

    static void remove(Context c, int index) {
        List<Item> l = load(c);
        if (index >= 0 && index < l.size()) l.remove(index);
        save(c, l);
    }

    static String dateText(Item it) {
        return BanglaText.digits(it.day) + " " + Panjika.BENGALI_MONTHS[it.month];
    }

    // ------------------------------------------------------------------ the alarm
    private static PendingIntent pending(Context c) {
        Intent i = new Intent(c, ReminderReceiver.class).setAction(ACTION_FIRE);
        return PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** sets (or cancels) the next morning alarm */
    static void schedule(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = pending(c);
        if (!dailyOn(c) && load(c).isEmpty()) {
            am.cancel(pi);
            return;
        }
        Calendar t = Calendar.getInstance();
        t.set(Calendar.HOUR_OF_DAY, hour(c));
        t.set(Calendar.MINUTE, minute(c));
        t.set(Calendar.SECOND, 0);
        t.set(Calendar.MILLISECOND, 0);
        if (t.getTimeInMillis() <= System.currentTimeMillis() + 2000) t.add(Calendar.DAY_OF_MONTH, 1);
        long at = t.getTimeInMillis();
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            else if (Build.VERSION.SDK_INT >= 23) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            else am.setExact(AlarmManager.RTC_WAKEUP, at, pi);
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        }
    }

    // ------------------------------------------------------------------ the notices
    /** called by the alarm: shows today's notices, then sets tomorrow's alarm */
    static void fire(Context c) {
        show(c, false);
        schedule(c);
    }

    /**
     * Builds and shows the notices for today.
     * @param test true from the "try it now" button: the daily notice is shown even if switched off
     */
    static void show(Context c, boolean test) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "বাংলা পঞ্জিকার স্মারক", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("প্রতিদিন সকালে আজকের পঞ্জিকা আর নিজের বাংলা তারিখের স্মারক");
            nm.createNotificationChannel(ch);
        }
        Calendar now = Calendar.getInstance();
        String method = prefs(c).getString("method", DayInfo.DRIK);
        DayInfo di = new DayInfo(now, method);
        Panjika.Result r = Panjika.compute(di.year, di.month, di.day, method);
        String bn = BanglaText.digits(r.monthDay) + " " + Panjika.BENGALI_MONTHS[r.monthIndex] + " " + BanglaText.digits(r.bangaYear);

        if (dailyOn(c) || test) {
            StringBuilder sb = new StringBuilder();
            for (String[] row : di.rows()) {
                if (row.length < 2) continue;
                String k = row[0];
                if (k.equals("তিথি")) sb.append("তিথি: ").append(row[1]).append('\n');
                else if (k.equals("নক্ষত্র")) sb.append("নক্ষত্র: ").append(row[1]).append('\n');
                else if (k.equals("সূর্যোদয়")) sb.append("সূর্যোদয়: ").append(row[1]);
                else if (k.equals("সূর্যাস্ত")) sb.append(", সূর্যাস্ত: ").append(row[1]).append('\n');
            }
            List<String> fest = Festivals.forDay(di.year, di.month, di.day, method, r);
            if (!fest.isEmpty()) {
                sb.append("উৎসব:");
                for (String f : fest) sb.append("\n• ").append(f);
            }
            post(c, nm, 1, di.headline() + " · " + bn, sb.toString().trim());
        }
        List<Item> items = load(c);
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            if (it.month == r.monthIndex && it.day == r.monthDay) {
                post(c, nm, 100 + i, "স্মারক: আজ " + dateText(it), it.text);
            }
        }
    }

    private static void post(Context c, NotificationManager nm, int id, String title, String body) {
        Intent open = new Intent(c, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, id, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body.split("\n")[0])
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        try {
            nm.notify(id, b.build());
        } catch (SecurityException e) {
            // notification permission not given
        }
    }
}
