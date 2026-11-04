package com.sushilbaidyaindia.banglapanjika;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a typed date: আজ, গতকাল, আগামীকাল, 17 March, March 17 2026, 17/03/2026, 2026-03-17,
 * and the same with Bangla digits and Bangla month names (১৭ মার্চ ২০২৬).
 * Bangla-calendar dates (১৭ কার্তিক) come with the Bangabda step.
 */
final class DateParser {
    private static final Map<String, Integer> MONTH = new HashMap<>();

    static {
        String[] en = {"january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december"};
        for (int i = 0; i < 12; i++) {
            MONTH.put(en[i], i + 1);
            MONTH.put(en[i].substring(0, 3), i + 1);
        }
        MONTH.put("sept", 9);
        String[][] bn = {
                {"জানুয়ারি", "জানুয়ারী"}, {"ফেব্রুয়ারি", "ফেব্রুয়ারী"}, {"মার্চ"}, {"এপ্রিল"}, {"মে"}, {"জুন"},
                {"জুলাই"}, {"আগস্ট", "আগষ্ট"}, {"সেপ্টেম্বর"}, {"অক্টোবর"}, {"নভেম্বর"}, {"ডিসেম্বর"}};
        for (int i = 0; i < 12; i++) for (String n : bn[i]) MONTH.put(n, i + 1);
    }

    private static final Pattern NUM_DMY = Pattern.compile("^(\\d{1,2})[/\\-.](\\d{1,2})[/\\-.](\\d{4})$");
    private static final Pattern NUM_YMD = Pattern.compile("^(\\d{4})[/\\-.](\\d{1,2})[/\\-.](\\d{1,2})$");
    private static final Pattern D_MON = Pattern.compile("^(\\d{1,2})\\s*([\\p{L}\\p{M}]+)\\s*(\\d{4})?$");
    private static final Pattern MON_D = Pattern.compile("^([\\p{L}\\p{M}]+)\\s*(\\d{1,2})\\s*(\\d{4})?$");

    private DateParser() { }

    /** the date, or null when the text is not understood */
    static Calendar parse(String raw, Calendar today) {
        if (raw == null) return null;
        String s = BanglaText.asciiDigits(raw).trim().toLowerCase(Locale.ROOT);
        s = s.replace(',', ' ').replaceAll("\\s+", " ");
        if (s.isEmpty()) return null;

        if (s.equals("আজ") || s.equals("today")) return plusDays(today, 0);
        if (s.equals("আগামীকাল") || s.equals("tomorrow")) return plusDays(today, 1);
        if (s.equals("গতকাল") || s.equals("yesterday")) return plusDays(today, -1);

        Matcher m = NUM_DMY.matcher(s);
        if (m.matches()) return make(num(m.group(3)), num(m.group(2)), num(m.group(1)));
        m = NUM_YMD.matcher(s);
        if (m.matches()) return make(num(m.group(1)), num(m.group(2)), num(m.group(3)));

        m = D_MON.matcher(s);
        if (m.matches()) {
            Integer mo = MONTH.get(m.group(2));
            if (mo != null) return make(m.group(3) == null ? today.get(Calendar.YEAR) : num(m.group(3)), mo, num(m.group(1)));
        }
        m = MON_D.matcher(s);
        if (m.matches()) {
            Integer mo = MONTH.get(m.group(1));
            if (mo != null) return make(m.group(3) == null ? today.get(Calendar.YEAR) : num(m.group(3)), mo, num(m.group(2)));
        }
        return null;
    }

    private static int num(String s) {
        return Integer.parseInt(s);
    }

    private static Calendar plusDays(Calendar c, int n) {
        Calendar r = (Calendar) c.clone();
        r.add(Calendar.DAY_OF_MONTH, n);
        return r;
    }

    /** a real calendar day, or null (31 February, month 13, year out of range ...) */
    private static Calendar make(int y, int mo, int d) {
        if (y < 1600 || y > 2400 || mo < 1 || mo > 12 || d < 1 || d > 31) return null;
        try {
            GregorianCalendar c = new GregorianCalendar(y, mo - 1, d);
            c.setLenient(false);
            c.getTime();
            return c;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
