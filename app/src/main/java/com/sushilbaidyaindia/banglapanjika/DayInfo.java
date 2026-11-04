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
    private Panjika.Result calc;

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

    private Panjika.Result calc() {
        if (calc == null) calc = Panjika.compute(year, month, day, method);
        return calc;
    }

    private String withEnd(String name, double end, String next) {
        long dn = Panjika.civilDay(Sky.localMidnight(year, month, day) + 0.25);
        if (end == 0) return name;
        return name + " — শেষ " + Panjika.timeText(end, dn) + ", তারপর " + next;
    }

    /** label + value, in the order of the page (also the order of the copied / read-aloud text) */
    List<String[]> rows() {
        List<String[]> r = new ArrayList<>();
        r.add(new String[]{"বার", weekdayName()});
        r.add(new String[]{"ইংরেজি", englishDate()});
        Panjika.Result c = calc();
        long dn = Panjika.civilDay(Sky.localMidnight(year, month, day) + 0.25);
        r.add(new String[]{"বাংলা", BanglaText.digits(c.monthDay) + " " + Panjika.BENGALI_MONTHS[c.monthIndex] + " " + BanglaText.digits(c.bangaYear)});
        r.add(new String[]{"শকাব্দ", BanglaText.digits(c.shakaYear)});
        r.add(new String[]{"পঞ্জিকা", SURYA.equals(method)
                ? "সূর্যসিদ্ধান্ত (কলকাতার সূর্যোদয়ে; ছাপা পঞ্জিকাভেদে কয়েক মিনিট থেকে ঘণ্টাখানেক পার্থক্য হতে পারে)"
                : "দৃকসিদ্ধান্ত (আকাশের হিসাব, কলকাতা)"});
        r.add(new String[]{"সূর্যোদয়", Panjika.timeText(c.sunrise, dn)});
        r.add(new String[]{"সূর্যাস্ত", Panjika.timeText(c.sunset, dn)});
        r.add(new String[]{"তিথি", withEnd(c.tithiName, c.tithiEnd, c.tithiNext)});
        r.add(new String[]{"নক্ষত্র", withEnd(c.nakshatraName, c.nakshatraEnd, c.nakshatraNext)});
        r.add(new String[]{"যোগ", withEnd(c.yogaName, c.yogaEnd, c.yogaNext)});
        r.add(new String[]{"করণ", c.karanaName});
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
