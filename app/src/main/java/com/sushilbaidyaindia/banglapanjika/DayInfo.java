package com.sushilbaidyaindia.banglapanjika;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Everything the app shows for one day. Step 1: only the English date, the weekday and the chosen method are real;
 * the other fields say "coming soon" and are filled in by the later steps.
 */
final class DayInfo {
    static final String SOON = "শীঘ্রই আসছে";
    static final String DRIK = "drik";
    static final String SURYA = "surya";

    final int year, month, day, weekday;      // month 1-12, weekday 0 = Sunday
    final String method;

    DayInfo(Calendar c, String method) {
        this.year = c.get(Calendar.YEAR);
        this.month = c.get(Calendar.MONTH) + 1;
        this.day = c.get(Calendar.DAY_OF_MONTH);
        this.weekday = c.get(Calendar.DAY_OF_WEEK) - 1;
        this.method = SURYA.equals(method) ? SURYA : DRIK;
    }

    static String methodName(String m) {
        return SURYA.equals(m) ? "সূর্যসিদ্ধান্ত" : "দৃকসিদ্ধান্ত";
    }

    String weekdayName() {
        return BanglaText.WEEKDAYS[weekday];
    }

    String englishDate() {
        return BanglaText.digits(day) + " " + BanglaText.MONTHS[month - 1] + " " + BanglaText.digits(year);
    }

    /** the big line at the top of the page */
    String headline() {
        return weekdayName() + ", " + englishDate();
    }

    /** label + value, in the order of the page (also the order of the copied / read-aloud text) */
    List<String[]> rows() {
        List<String[]> r = new ArrayList<>();
        r.add(new String[]{"বার", weekdayName()});
        r.add(new String[]{"ইংরেজি", englishDate()});
        r.add(new String[]{"বাংলা", SOON});
        r.add(new String[]{"শকাব্দ", SOON});
        r.add(new String[]{"পঞ্জিকা", methodName(method)});
        r.add(new String[]{"তিথি", SOON});
        r.add(new String[]{"নক্ষত্র", SOON});
        r.add(new String[]{"উৎসব", SOON});
        r.add(new String[]{"বিশেষ ব্যক্তি", SOON});
        r.add(new String[]{"ঐতিহাসিক গুরুত্ব", SOON});
        return r;
    }

    /** the text for the copy and read-aloud buttons */
    String toText() {
        StringBuilder b = new StringBuilder();
        for (String[] row : rows()) b.append(row[0]).append(": ").append(row[1]).append("\n");
        return b.toString().trim();
    }
}
