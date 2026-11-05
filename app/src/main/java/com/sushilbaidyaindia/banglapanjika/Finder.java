package com.sushilbaidyaindia.banglapanjika;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/** Search by Bangla date or by festival name. Works offline; festival-name search scans a whole year. */
final class Finder {
    private Finder() { }

    static final class Hit {
        final Calendar cal;
        final String label;
        Hit(Calendar cal, String label) { this.cal = cal; this.label = label; }
    }

    private static String norm(String s) {
        return BanglaText.asciiDigits(s).toLowerCase(Locale.ROOT).replace(",", " ").replaceAll("\\s+", " ").trim();
    }

    private static Calendar cal(int y, int m, int d) {
        return new GregorianCalendar(y, m - 1, d);
    }

    /** Bangla date such as "১৭ কার্তিক" or "17 kartik 1433"; null if the text has no Bangla month name. */
    static List<Hit> byBanglaDate(String raw, String method, int todayBangaYear) {
        String s = norm(raw);
        int month = -1;
        for (int i = 0; i < 12; i++) {
            String n = Panjika.BENGALI_MONTHS[i];
            if (s.contains(n) || (i == 2 && s.contains("আষাঢ়")) ) { month = i; break; }
        }
        if (month < 0) {
            if (s.contains("আষাঢ")) month = 2; else if (s.contains("জ্যৈষ্ঠ") || s.contains("জৈষ্ঠ")) month = 1; else return null;
        }
        int day = -1, year = todayBangaYear;
        for (String tok : s.split(" ")) {
            if (!tok.matches("\\d+")) continue;
            int v = Integer.parseInt(tok);
            if (tok.length() >= 3) year = v; else if (day < 0) day = v;
        }
        if (day < 1 || day > 32) return null;
        List<Hit> out = new ArrayList<>();
        int g = year + 593;                         // Bangla year starts in April of this Gregorian year
        Calendar c = cal(g, 3, 25);
        for (int k = 0; k < 400; k++) {
            int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH) + 1, d = c.get(Calendar.DAY_OF_MONTH);
            Panjika.Result r = Panjika.compute(y, m, d, method);
            if (r.bangaYear == year && r.monthIndex == month && r.monthDay == day) {
                out.add(new Hit((Calendar) c.clone(), ""));
                break;
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        return out;
    }

    /** every day of the given English year whose festival list has a name containing the query */
    static List<Hit> byFestival(String raw, String method, int year) {
        String q = norm(raw).replace(" ", "");
        List<Hit> out = new ArrayList<>();
        if (q.length() < 2) return out;
        Calendar c = cal(year, 1, 1);
        while (c.get(Calendar.YEAR) == year) {
            int y = year, m = c.get(Calendar.MONTH) + 1, d = c.get(Calendar.DAY_OF_MONTH);
            Panjika.Result r = Panjika.compute(y, m, d, method);
            for (String f : Festivals.forDay(y, m, d, method, r)) {
                String name = f.substring(0, f.lastIndexOf(" — "));
                if (name.replace(" ", "").toLowerCase(Locale.ROOT).contains(q)) out.add(new Hit((Calendar) c.clone(), f));
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        return out;
    }
}
