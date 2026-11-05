package com.sushilbaidyaindia.banglapanjika;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Festivals and pujas of a day, worked out from the panjika itself:
 * lunar ones from (lunar month, tithi, time of day), Bangla-calendar ones from the Bangla date, plus a few fixed dates.
 * Every entry says where it is observed.
 * Lunar months are the amanta months used in Bengal: named after the sign the Sun is in at the preceding new moon.
 */
final class Festivals {
    private Festivals() {}

    // time of day at which the tithi decides the festival day
    private static final int SUNRISE = 0, MIDDAY = 1, PRADOSH = 2, NISHITA = 3, MORNING = 4, SUNSET = 5;
    private static final int LUNAR = 0, BENGALI_DAY = 1, BENGALI_LAST = 2, GREG = 3;

    private static final class F {
        final int kind, a, b, ref;
        final String name, where;
        F(int kind, int a, int b, int ref, String name, String where) {
            this.kind = kind; this.a = a; this.b = b; this.ref = ref; this.name = name; this.where = where;
        }
    }

    private static final List<F> ALL = new ArrayList<>();

    // lunar month: 0 Baishakh(Vaishakha) .. 11 Chaitra ; tithi: 0..14 shukla 1..purnima, 15..28 krishna 1..14, 29 amavasya
    private static void lunar(int month, int tithi, int ref, String name, String where) {
        ALL.add(new F(LUNAR, month, tithi, ref, name, where));
    }
    private static void bengali(int month, int day, String name, String where) {
        ALL.add(new F(BENGALI_DAY, month, day, SUNRISE, name, where));
    }
    private static void lastDay(int month, String name, String where) {
        ALL.add(new F(BENGALI_LAST, month, 0, SUNRISE, name, where));
    }
    private static void greg(int month, int day, String name, String where) {
        ALL.add(new F(GREG, month, day, SUNRISE, name, where));
    }

    private static final String WB = "পশ্চিমবঙ্গ";
    private static final String INDIA = "সারা ভারত";

    static {
        // ---- চৈত্র (11)
        lunar(11, 0, SUNRISE, "উগাদি ও গুড়ি পাড়োয়া (হিন্দু নববর্ষ)", "কর্ণাটক, অন্ধ্রপ্রদেশ, তেলেঙ্গানা (উগাদি); মহারাষ্ট্র, গোয়া (গুড়ি পাড়োয়া)");
        lunar(11, 5, SUNRISE, "বাসন্তী পূজা — ষষ্ঠী", WB + " (কলকাতার কিছু বনেদি বাড়ি ও মঠ-মন্দির)");
        lunar(11, 6, SUNRISE, "বাসন্তী পূজা — সপ্তমী", WB);
        lunar(11, 7, SUNRISE, "বাসন্তী পূজা — অষ্টমী, অন্নপূর্ণা পূজা", WB);
        lunar(11, 8, MIDDAY, "রামনবমী, বাসন্তী পূজা — নবমী", INDIA + " (বিশেষত উত্তরপ্রদেশের অযোধ্যা); " + WB + "-তেও পালিত");
        lunar(11, 14, SUNRISE, "চৈত্র পূর্ণিমা, হনুমান জয়ন্তী", "উত্তর ও মধ্য ভারতের বেশিরভাগ রাজ্য (উত্তরপ্রদেশ, বিহার, মধ্যপ্রদেশ, গুজরাত, রাজস্থান)");
        // ---- বৈশাখ (0)
        lunar(0, 2, MIDDAY, "অক্ষয় তৃতীয়া", INDIA + "; " + WB + "-এ হালখাতা ও গঙ্গাপূজার দিন; গুজরাত, মহারাষ্ট্রেও বিশেষ");
        lunar(0, 14, SUNRISE, "বুদ্ধ পূর্ণিমা", INDIA + " (বিশেষত বিহারের বোধগয়া, উত্তরপ্রদেশের সারনাথ, সিকিম); " + WB);
        // ---- জ্যৈষ্ঠ (1)
        lunar(1, 5, SUNRISE, "জামাইষষ্ঠী (অরণ্য ষষ্ঠী)", WB + " (ও বাংলাদেশ)");
        lunar(1, 9, MIDDAY, "গঙ্গা দশহরা", "উত্তরপ্রদেশ, উত্তরাখণ্ড, বিহার (হরিদ্বার, বারাণসী, প্রয়াগরাজ); " + WB + "-এর গঙ্গাতীরেও");
        lunar(1, 14, SUNRISE, "জ্যৈষ্ঠ পূর্ণিমা, স্নানযাত্রা (জগন্নাথদেবের)", "ওড়িশা (পুরী); " + WB + " (মাহেশ, শ্রীরামপুর)");
        // ---- আষাঢ় (2)
        lunar(2, 1, MIDDAY, "রথযাত্রা", "ওড়িশা (পুরী); " + WB + " (মাহেশ, শ্রীরামপুর); গুজরাত (আহমেদাবাদ); ঝাড়খণ্ড (রাঁচি)");
        lunar(2, 9, MIDDAY, "উল্টোরথ (বাহুড়া যাত্রা)", "ওড়িশা (পুরী); " + WB);
        lunar(2, 10, SUNRISE, "দেবশয়নী (হরিশয়নী) একাদশী", INDIA + " (মহারাষ্ট্রের পান্ঢরপুরে আষাঢ়ী একাদশী বিখ্যাত)");
        lunar(2, 14, MIDDAY, "আষাঢ়ী পূর্ণিমা, গুরু পূর্ণিমা, ব্যাসপূজা", INDIA);
        // ---- শ্রাবণ (3)
        lunar(3, 4, SUNRISE, "নাগপঞ্চমী ও মনসা পূজা", "মহারাষ্ট্র, কর্ণাটক, উত্তর ভারত (নাগপঞ্চমী); " + WB + ", অসম (মনসা পূজা)");
        lunar(3, 10, SUNRISE, "ঝুলন যাত্রা শুরু", WB + ", ওড়িশা, উত্তরপ্রদেশ (মথুরা-বৃন্দাবন)");
        lunar(3, 14, SUNRISE, "শ্রাবণ পূর্ণিমা — ঝুলন পূর্ণিমা ও রাখী বন্ধন", "রাখী বন্ধন: " + INDIA + " (বিশেষত উত্তর ও পশ্চিম ভারত); ঝুলন পূর্ণিমা: " + WB + ", ওড়িশা");
        lunar(3, 22, NISHITA, "জন্মাষ্টমী (শ্রীকৃষ্ণের জন্মোৎসব)", INDIA + " (উত্তরপ্রদেশের মথুরা-বৃন্দাবন, মহারাষ্ট্রের দহি-হান্ডি, গুজরাত, ওড়িশা, মণিপুর); " + WB + "-তেও");
        lunar(3, 23, SUNRISE, "নন্দোৎসব", WB + ", ওড়িশা, উত্তরপ্রদেশ");
        // ---- ভাদ্র (4)
        lunar(4, 2, SUNRISE, "হরতালিকা তীজ", "রাজস্থান, উত্তরপ্রদেশ, বিহার, ঝাড়খণ্ড, মধ্যপ্রদেশ, নেপাল");
        lunar(4, 3, MIDDAY, "গণেশ চতুর্থী", "মহারাষ্ট্র, গোয়া, কর্ণাটক, তেলেঙ্গানা, অন্ধ্রপ্রদেশ, গুজরাত, তামিলনাড়ু; " + WB + "-এও পালিত");
        lunar(4, 7, SUNRISE, "রাধাষ্টমী", "উত্তরপ্রদেশ (বরসানা, বৃন্দাবন); " + WB);
        lunar(4, 13, SUNRISE, "অনন্ত চতুর্দশী", "মহারাষ্ট্র (গণেশ বিসর্জন), মধ্যপ্রদেশ, রাজস্থান, উত্তরপ্রদেশ; " + WB);
        lunar(4, 29, SUNRISE, "মহালয়া (দেবীপক্ষের সূচনা, তর্পণ)", WB + ", ওড়িশা, ত্রিপুরা, অসম; অন্যত্র সর্বপিতৃ অমাবস্যা");
        // ---- আশ্বিন (5)
        lunar(5, 0, SUNRISE, "দেবীপক্ষ শুরু, ঘটস্থাপনা (শারদীয় নবরাত্রি আরম্ভ)", "গুজরাত, রাজস্থান, উত্তরপ্রদেশ, মহারাষ্ট্র ও দক্ষিণ ভারত (নবরাত্রি); " + WB);
        lunar(5, 5, SUNRISE, "মহাষষ্ঠী — দুর্গাপূজা শুরু (বোধন)", WB + ", ত্রিপুরা, অসম, ওড়িশা, বিহার-ঝাড়খণ্ড");
        lunar(5, 6, SUNRISE, "মহাসপ্তমী — দুর্গাপূজা (নবপত্রিকা প্রবেশ)", WB + ", ত্রিপুরা, অসম, ওড়িশা, বিহার-ঝাড়খণ্ড");
        lunar(5, 7, SUNRISE, "মহাষ্টমী — দুর্গাপূজা (কুমারী পূজা, সন্ধিপূজা)", WB + ", ত্রিপুরা, অসম, ওড়িশা, বিহার-ঝাড়খণ্ড");
        lunar(5, 8, SUNRISE, "মহানবমী — দুর্গাপূজা", WB + ", ত্রিপুরা, অসম, ওড়িশা, বিহার-ঝাড়খণ্ড; দক্ষিণ ভারতে আয়ুধ পূজা");
        lunar(5, 9, MIDDAY, "বিজয়া দশমী — দুর্গাপূজার শেষ (প্রতিমা বিসর্জন, সিঁদুর খেলা), দশেরা", "দুর্গাপূজার সমাপ্তি: " + WB + ", ত্রিপুরা, অসম, ওড়িশা; দশেরা (রাবণ দহন): উত্তর ভারত, কর্ণাটকের মহীশূর, হিমাচলের কুলু");
        lunar(5, 14, NISHITA, "কোজাগরী লক্ষ্মীপূজা (শরৎ পূর্ণিমা)", WB + ", ওড়িশা, অসম, ত্রিপুরা; উত্তর ও পশ্চিম ভারতে শরৎ পূর্ণিমা");
        lunar(5, 18, PRADOSH, "করবা চৌথ", "পাঞ্জাব, হরিয়ানা, রাজস্থান, উত্তরপ্রদেশ, দিল্লি, মধ্যপ্রদেশ");
        lunar(5, 27, PRADOSH, "ধনতেরস", "উত্তর ও পশ্চিম ভারত (গুজরাত, মহারাষ্ট্র, রাজস্থান, দিল্লি, উত্তরপ্রদেশ)");
        lunar(5, 28, NISHITA, "ভূত চতুর্দশী (চোদ্দ প্রদীপ, চোদ্দ শাক)", WB);
        lunar(5, 28, SUNRISE, "নরক চতুর্দশী (ছোটি দিওয়ালি)", "উত্তর ও পশ্চিম ভারত, দক্ষিণ ভারত");
        lunar(5, 29, NISHITA, "কালীপূজা ও দীপাবলি (লক্ষ্মীপূজা)", "কালীপূজা: " + WB + ", ওড়িশা, অসম, ত্রিপুরা; দীপাবলি: " + INDIA);
        // ---- কার্তিক (6)
        lunar(6, 0, SUNRISE, "গোবর্ধন পূজা, অন্নকূট", "উত্তরপ্রদেশ (মথুরা-বৃন্দাবন), রাজস্থান, গুজরাত (গুজরাটি নববর্ষ — বেস্তু বারস)");
        lunar(6, 1, MIDDAY, "ভাইফোঁটা (ভ্রাতৃদ্বিতীয়া)", WB + "; অন্যত্র ভাই দুজ (উত্তর ভারত), ভাউবিজ (মহারাষ্ট্র)");
        lunar(6, 5, SUNSET, "ছট পূজা (সন্ধ্যা অর্ঘ্য)", "বিহার, ঝাড়খণ্ড, পূর্ব উত্তরপ্রদেশ, দিল্লি; " + WB + "-এও পালিত");
        lunar(6, 7, SUNRISE, "গোষ্ঠাষ্টমী", "উত্তরপ্রদেশ (ব্রজ), " + WB);
        lunar(6, 8, SUNRISE, "জগদ্ধাত্রী পূজা", WB + " (চন্দননগর, কৃষ্ণনগর)");
        lunar(6, 10, SUNRISE, "প্রবোধিনী (দেবোত্থান) একাদশী, তুলসী বিবাহ আরম্ভ", INDIA);
        lunar(6, 14, MIDDAY, "কার্তিক পূর্ণিমা, রাস পূর্ণিমা", "রাসযাত্রা: " + WB + " (কোচবিহার, নবদ্বীপ), অসম, মণিপুর; দেব দীপাবলি: উত্তরপ্রদেশ (বারাণসী); গুরু নানক জয়ন্তী: পাঞ্জাব; বোইতা বন্দনা: ওড়িশা");
        // ---- অগ্রহায়ণ (7)
        lunar(7, 10, SUNRISE, "মোক্ষদা একাদশী, গীতা জয়ন্তী", INDIA + " (হরিয়ানার কুরুক্ষেত্রে বিশেষ)");
        lunar(7, 14, SUNRISE, "অগ্রহায়ণ পূর্ণিমা, দত্তাত্রেয় জয়ন্তী", "মহারাষ্ট্র, কর্ণাটক, গুজরাত, অন্ধ্রপ্রদেশ");
        // ---- মাঘ (9)
        lunar(9, 4, MORNING, "সরস্বতী পূজা (বসন্ত পঞ্চমী)", WB + ", ত্রিপুরা, অসম, ওড়িশা, বিহার; উত্তর ভারতে বসন্ত পঞ্চমী");
        lunar(9, 6, SUNRISE, "রথ সপ্তমী (অচলা সপ্তমী)", "ওড়িশা (কোনারক), অন্ধ্রপ্রদেশ, তামিলনাড়ু, মহারাষ্ট্র");
        lunar(9, 14, MIDDAY, "মাঘী পূর্ণিমা", "উত্তরপ্রদেশ (প্রয়াগরাজের মাঘ মেলা), পাঞ্জাব (গুরু রবিদাস জয়ন্তী); " + WB);
        lunar(9, 28, NISHITA, "মহাশিবরাত্রি", INDIA + " (বারাণসী, উজ্জয়িনী, সোমনাথ); " + WB + " (তারকেশ্বর)");
        // ---- ফাল্গুন (10)
        lunar(10, 10, SUNRISE, "আমলকী একাদশী", INDIA);
        lunar(10, 14, PRADOSH, "হোলিকা দহন (নেড়া পোড়া)", "উত্তর ভারত (উত্তরপ্রদেশ, রাজস্থান, বিহার, দিল্লি), মহারাষ্ট্র; " + WB + " (নেড়া পোড়া)");
        lunar(10, 14, MIDDAY, "দোলযাত্রা, গৌর পূর্ণিমা (শ্রীচৈতন্যের জন্মতিথি)", WB + " (নবদ্বীপ, মায়াপুর, শান্তিনিকেতন), ওড়িশা, ত্রিপুরা, অসম");
        lunar(10, 15, SUNRISE, "হোলি (রঙের উৎসব)", "উত্তরপ্রদেশ, রাজস্থান, বিহার, দিল্লি, হরিয়ানা, পাঞ্জাব, মধ্যপ্রদেশ ও উত্তর ভারত; " + WB + "-এ দোলের পর দিন");

        // ---- Bangla calendar dates
        bengali(0, 1, "পয়লা বৈশাখ, বাংলা নববর্ষ", WB + ", ত্রিপুরা, অসম; একই সময়ে: বৈশাখী (পাঞ্জাব), পুথান্ডু (তামিলনাড়ু), বিষু (কেরল), বোহাগ বিহু (অসম), মহাবিষুব সংক্রান্তি (ওড়িশা)");
        bengali(0, 25, "রবীন্দ্রজয়ন্তী (২৫ বৈশাখ)", WB + ", ত্রিপুরা, অসমের বাঙালিরা, বাংলাদেশ");
        bengali(1, 11, "নজরুল জয়ন্তী (১১ জ্যৈষ্ঠ)", WB + ", ত্রিপুরা, বাংলাদেশ");
        bengali(3, 22, "রবীন্দ্রনাথের প্রয়াণ দিবস (২২ শ্রাবণ)", WB + ", বাংলাদেশ");
        lastDay(11, "চৈত্র সংক্রান্তি — চড়ক ও গাজন", WB + " (ও বাংলাদেশ); ওড়িশা (মহাবিষুব সংক্রান্তির আগের দিন)");
        lastDay(4, "বিশ্বকর্মা পূজা (ভাদ্র সংক্রান্তি)", WB + ", ওড়িশা, ত্রিপুরা, অসম, বিহার, ঝাড়খণ্ড");
        lastDay(8, "পৌষ সংক্রান্তি, মকর সংক্রান্তি", "পৌষ পার্বণ, গঙ্গাসাগর মেলা: " + WB + "; মকর সংক্রান্তি: " + INDIA + " (গুজরাতে উত্তরায়ণ, তামিলনাড়ুতে পোঙ্গল, অসমে মাঘ বিহু, উত্তরপ্রদেশ-বিহারে খিচুড়ি, পাঞ্জাবে মাঘী)");

        // ---- fixed English dates
        greg(1, 1, "ইংরেজি নববর্ষ", INDIA);
        greg(1, 12, "স্বামী বিবেকানন্দের জন্মদিন, জাতীয় যুব দিবস", INDIA + " (বিশেষত " + WB + ")");
        greg(1, 23, "নেতাজি সুভাষচন্দ্র বসুর জন্মদিন (পরাক্রম দিবস)", INDIA + " (বিশেষত " + WB + ")");
        greg(1, 26, "প্রজাতন্ত্র দিবস", "ভারত");
        greg(2, 21, "আন্তর্জাতিক মাতৃভাষা দিবস (ভাষা শহিদ দিবস)", "বাংলাদেশ; " + WB + "-এও পালিত");
        greg(3, 26, "স্বাধীনতা ও জাতীয় দিবস", "বাংলাদেশ");
        greg(8, 15, "স্বাধীনতা দিবস", "ভারত");
        greg(10, 2, "গান্ধী জয়ন্তী", "ভারত");
        greg(12, 16, "বিজয় দিবস", "বাংলাদেশ");
        greg(12, 25, "বড়দিন (ক্রিসমাস)", INDIA + " (কলকাতায় বিশেষ উৎসব)");
    }

    // ------------------------------------------------------------------ time helpers
    private static long dayNo(int y, int m, int d) {
        return Panjika.civilDay(Sky.localMidnight(y, m, d) + 0.25);
    }

    private static double refTime(long dayNo, int ref) {
        int[] ymd = Panjika.fromDayNo(dayNo);
        double[] ss = Sky.sunriseSunset(ymd[0], ymd[1], ymd[2]);
        switch (ref) {
            case MIDDAY: return (ss[0] + ss[1]) / 2.0;
            case PRADOSH: return ss[1] + 1.0 / 24.0;
            case SUNSET: return ss[1];
            case MORNING: return ss[0] + 3.0 / 24.0;
            case NISHITA: {
                int[] n = Panjika.fromDayNo(dayNo + 1);
                double nr = Sky.sunriseSunset(n[0], n[1], n[2])[0];
                return (ss[1] + nr) / 2.0;
            }
            default: return ss[0];
        }
    }

    private static int tithiAt(Panjika.Model m, long dayNo, int ref, Map<Long, Integer> cache) {
        long key = dayNo * 10 + ref;
        Integer c = cache.get(key);
        if (c != null) return c;
        int t = (int) Math.floor(Panjika.tithiOf(m, refTime(dayNo, ref)));
        cache.put(key, t);
        return t;
    }

    /** lunar month index (0 = Baishakh .. 11 = Chaitra) at time j, or -1 if it is an adhika (extra) month */
    private static int lunarMonth(final Panjika.Model m, double j) {
        final Panjika.Model mm = m;
        double v = Panjika.tithiOf(mm, j);
        double est = j - v * (29.5306 / 30.0);
        double nm0 = Sky.crossing(x -> Panjika.tithiOf(mm, x), 0.0, est - 1.5, 30.0, 3.0);
        if (nm0 == 0) return -2;
        double nm1 = Sky.crossing(x -> Panjika.tithiOf(mm, x), 0.0, nm0 + 29.53 - 2.5, 30.0, 5.0);
        int s0 = (int) Math.floor(m.sun(nm0) / 30.0);
        if (nm1 != 0 && s0 == (int) Math.floor(m.sun(nm1) / 30.0)) return -1;
        return s0;
    }

    // ------------------------------------------------------------------ the answer for one day
    static List<String> forDay(int y, int mo, int d, String method, Panjika.Result bn) {
        List<String> out = new ArrayList<>();
        Panjika.Model model = Panjika.modelFor(method);
        long today = dayNo(y, mo, d);
        Map<Long, Integer> cache = new HashMap<>();
        int lunarIdx = Integer.MIN_VALUE;                  // worked out only when needed
        Panjika.Result tomorrow = null;
        for (F f : ALL) {
            boolean hit = false;
            if (f.kind == LUNAR) {
                int ti = tithiAt(model, today, f.ref, cache);
                int tp = tithiAt(model, today - 1, f.ref, cache);
                boolean first = ti == f.b && tp != f.b;
                boolean skipped = tp == (f.b + 29) % 30 && ti == (f.b + 1) % 30;      // the tithi never touches this time of day
                if (first || skipped) {
                    if (lunarIdx == Integer.MIN_VALUE) lunarIdx = lunarMonth(model, refTime(today, SUNRISE));
                    hit = lunarIdx == f.a;
                }
            } else if (f.kind == BENGALI_DAY) {
                hit = bn.monthIndex == f.a && bn.monthDay == f.b;
            } else if (f.kind == BENGALI_LAST) {
                if (bn.monthIndex == f.a) {
                    if (tomorrow == null) {
                        int[] n = Panjika.fromDayNo(today + 1);
                        tomorrow = Panjika.compute(n[0], n[1], n[2], method);
                    }
                    hit = tomorrow.monthIndex != bn.monthIndex;
                }
            } else {
                hit = mo == f.a && d == f.b;
            }
            if (hit) out.add(f.name + " — " + f.where);
        }
        return out;
    }
}
