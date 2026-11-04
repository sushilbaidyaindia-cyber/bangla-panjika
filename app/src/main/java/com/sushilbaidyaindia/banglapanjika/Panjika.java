package com.sushilbaidyaindia.banglapanjika;

/** Bangla calendar and the daily panjika values, from the sky (drik method). */
final class Panjika {
    private Panjika() {}

    /** sidereal longitudes (degrees) of the Sun and the Moon: the sky (drik) or Surya Siddhanta */
    interface Model {
        double sun(double j);
        double moon(double j);
    }

    static final Model DRIK = new Model() {
        @Override public double sun(double j) { return Sky.sunSidereal(j); }
        @Override public double moon(double j) { return Sky.moonSidereal(j); }
    };
    static final Model SURYA = new Surya();

    static Model modelFor(String method) { return DayInfo.SURYA.equals(method) ? SURYA : DRIK; }

    static double tithiOf(Model m, double j) { return ((m.moon(j) - m.sun(j)) % 360.0 + 360.0) % 360.0 / 12.0; }
    static double nakOf(Model m, double j) { return m.moon(j) / (360.0 / 27.0); }
    static double yogaOf(Model m, double j) { return ((m.sun(j) + m.moon(j)) % 360.0) / (360.0 / 27.0); }

    static final String[] BENGALI_MONTHS = {"বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
            "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"};
    static final String[] TITHI = {"প্রতিপদ", "দ্বিতীয়া", "তৃতীয়া", "চতুর্থী", "পঞ্চমী", "ষষ্ঠী", "সপ্তমী", "অষ্টমী",
            "নবমী", "দশমী", "একাদশী", "দ্বাদশী", "ত্রয়োদশী", "চতুর্দশী", "পূর্ণিমা"};
    static final String[] NAKSHATRA = {"অশ্বিনী", "ভরণী", "কৃত্তিকা", "রোহিণী", "মৃগশিরা", "আর্দ্রা", "পুনর্বসু", "পুষ্যা",
            "অশ্লেষা", "মঘা", "পূর্বফাল্গুনী", "উত্তরফাল্গুনী", "হস্তা", "চিত্রা", "স্বাতী", "বিশাখা", "অনুরাধা", "জ্যেষ্ঠা",
            "মূলা", "পূর্বাষাঢ়া", "উত্তরাষাঢ়া", "শ্রবণা", "ধনিষ্ঠা", "শতভিষা", "পূর্বভাদ্রপদ", "উত্তরভাদ্রপদ", "রেবতী"};
    static final String[] YOGA = {"বিষ্কম্ভ", "প্রীতি", "আয়ুষ্মান", "সৌভাগ্য", "শোভন", "অতিগণ্ড", "সুকর্মা", "ধৃতি", "শূল",
            "গণ্ড", "বৃদ্ধি", "ধ্রুব", "ব্যাঘাত", "হর্ষণ", "বজ্র", "সিদ্ধি", "ব্যতীপাত", "বরীয়ান", "পরিঘ", "শিব", "সিদ্ধ",
            "সাধ্য", "শুভ", "শুক্ল", "ব্রহ্ম", "ঐন্দ্র", "বৈধৃতি"};
    private static final String[] MOVABLE_KARANA = {"বব", "বালব", "কৌলব", "তৈতিল", "গর", "বণিজ", "বিষ্টি (ভদ্রা)"};

    static final class Result {
        int bangaYear, shakaYear, monthIndex, monthDay;
        String tithiName, nakshatraName, yogaName, karanaName;
        double sunrise, sunset;                 // Julian days UT
        double tithiEnd, nakshatraEnd, yogaEnd; // 0 = not found
        double nextTithiAfter;                  // helper
        String tithiNext, nakshatraNext, yogaNext;
        int tithiIdxAtSunrise, tithiNextIdx;
        String sankrantiNote;
    }

    static String tithiFullName(int k) {      // k 0..29
        if (k == 29) return "অমাবস্যা";
        if (k == 14) return "পূর্ণিমা";
        return (k < 15 ? "শুক্ল " : "কৃষ্ণ ") + TITHI[k % 15];
    }

    static String karanaName(int h) {         // h 0..59
        if (h == 0) return "কিংস্তুঘ্ন";
        if (h >= 57) return h == 57 ? "শকুনি" : h == 58 ? "চতুষ্পদ" : "নাগ";
        return MOVABLE_KARANA[(h - 1) % 7];
    }

    /** the 24 solar ingresses (sankranti) from April of the previous year to March of the next; time in JD UT */
    static double[][] sankrantis(int year, Model model) {
        double[][] r = new double[24][3];       // {jd, monthIndex, gregorianYearOfThatApril}
        int n = 0;
        for (int yy = year - 1; yy <= year; yy++) {
            double apr14 = Sky.jd(yy, 4, 14.0);
            for (int k = 0; k < 12; k++) {
                double guess = apr14 + k * 30.44;
                double target = 30.0 * k;
                double lo = guess - 8, hi = guess + 8;
                for (int i = 0; i < 45; i++) {
                    double mid = (lo + hi) / 2;
                    double v = ((model.sun(mid) - target + 540.0) % 360.0) - 180.0;
                    if (v < 0) lo = mid; else hi = mid;
                }
                r[n][0] = (lo + hi) / 2; r[n][1] = k; r[n][2] = yy; n++;
            }
        }
        return r;
    }

    /** civil (IST) date number of a Julian day: days since an epoch, counted at local midnight */
    static long civilDay(double jdUt) {
        return (long) Math.floor(jdUt + Sky.IST + 0.5);
    }

    static Result compute(int y, int m, int d, String method) {
        final Model model = modelFor(method);
        Result r = new Result();
        long today = civilDay(Sky.localMidnight(y, m, d) + 0.25);   // this date's number (use a time inside the day)

        // --- Bangla date: month starts the civil day after the sankranti's civil date (midnight rule) ---
        double[][] sk = sankrantis(y, model);
        int best = -1; long bestStart = Long.MIN_VALUE;
        for (int i = 0; i < sk.length; i++) {
            long start = civilDay(sk[i][0]) + 1;
            if (start <= today && start > bestStart) { bestStart = start; best = i; }
        }
        r.monthIndex = (int) sk[best][1];
        r.monthDay = (int) (today - bestStart) + 1;
        // Bangla year: year of the Mesha sankranti that began the current Bangla year, minus 593
        int meshaYear = (int) sk[best][2];
        r.bangaYear = meshaYear - 593;

        // --- Shaka year: changes on Chaitra shukla pratipada (first sunrise after the Chaitra new moon) ---
        r.shakaYear = shaka(y, m, d, model);

        // --- daily values at sunrise ---
        double[] ss = Sky.sunriseSunset(y, m, d);
        r.sunrise = ss[0]; r.sunset = ss[1];
        double j = ss[0];
        int ti = (int) Math.floor(tithiOf(model, j));
        r.tithiIdxAtSunrise = ti;
        r.tithiName = tithiFullName(ti);
        r.tithiEnd = Sky.nextWhole(x -> tithiOf(model, x), j, 30.0);
        r.tithiNextIdx = (ti + 1) % 30;
        r.tithiNext = tithiFullName(r.tithiNextIdx);
        int ni = (int) Math.floor(nakOf(model, j));
        r.nakshatraName = NAKSHATRA[ni];
        r.nakshatraEnd = Sky.nextWhole(x -> nakOf(model, x), j, 27.0);
        r.nakshatraNext = NAKSHATRA[(ni + 1) % 27];
        int yi = (int) Math.floor(yogaOf(model, j));
        r.yogaName = YOGA[yi];
        r.yogaEnd = Sky.nextWhole(x -> yogaOf(model, x), j, 27.0);
        r.yogaNext = YOGA[(yi + 1) % 27];
        r.karanaName = karanaName((int) Math.floor(tithiOf(model, j) * 2.0));
        return r;
    }

    static int shaka(int y, int m, int d, Model model) {
        long today = civilDay(Sky.localMidnight(y, m, d) + 0.25);
        double j = Sky.jd(y, 2, 20.0);
        double end = Sky.jd(y, 4, 25.0);
        while (j < end) {
            double nm = Sky.crossing(x -> tithiOf(model, x), 0.0, j, 30.0, 40.0);
            if (nm == 0) break;
            double s = model.sun(nm);
            if (s >= 330.0) {                                    // new moon with the Sun in Meena: Chaitra begins
                long dayNo = civilDay(nm);                       // IST civil day of the new moon
                int[] ymd = fromDayNo(dayNo);
                double sr = Sky.sunriseSunset(ymd[0], ymd[1], ymd[2])[0];
                long start = nm < sr ? dayNo : dayNo + 1;        // first sunrise after the new moon
                return today >= start ? y - 78 : y - 79;
            }
            j = nm + 20.0;
        }
        return y - 78;
    }

    /** reverse of civilDay for a day number: {year, month, day} */
    static int[] fromDayNo(long dayNo) {
        long jdi = dayNo;                   // civilDay is the Julian day number of the local date
        long a = jdi + 32044;
        long b = (4 * a + 3) / 146097;
        long c = a - 146097 * b / 4;
        long dd = (4 * c + 3) / 1461;
        long e = c - 1461 * dd / 4;
        long mm = (5 * e + 2) / 153;
        int day = (int) (e - (153 * mm + 2) / 5 + 1);
        int month = (int) (mm + 3 - 12 * (mm / 10));
        int year = (int) (100 * b + dd - 4800 + mm / 10);
        return new int[]{year, month, day};
    }

    /** "সকাল ১১:৪০" for a Julian day, local time; adds "(পরদিন)" if it is not on the given day number */
    static String timeText(double jdUt, long dayNo) {
        double local = jdUt + Sky.IST + 0.5;
        long dn = (long) Math.floor(local);
        double frac = local - dn;
        int minutes = (int) Math.round(frac * 1440.0);
        if (minutes >= 1440) { minutes -= 1440; dn++; }
        int h = minutes / 60, mi = minutes % 60;
        String part;
        if (h >= 4 && h < 6) part = "ভোর";
        else if (h >= 6 && h < 12) part = "সকাল";
        else if (h >= 12 && h < 15) part = "দুপুর";
        else if (h >= 15 && h < 17) part = "বিকেল";
        else if (h >= 17 && h < 19) part = "সন্ধ্যা";
        else part = "রাত";
        int h12 = h % 12 == 0 ? 12 : h % 12;
        String s = part + " " + BanglaText.digits(h12) + ":" + BanglaText.digits(mi < 10 ? "0" + mi : String.valueOf(mi));
        if (dn > dayNo) s += " (পরদিন)";
        return s;
    }
}
