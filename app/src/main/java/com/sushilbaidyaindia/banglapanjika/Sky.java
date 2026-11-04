package com.sushilbaidyaindia.banglapanjika;

import java.util.function.DoubleUnaryOperator;

/**
 * Sun and Moon positions (truncated Meeus series, about 0.3 degree) and everything built from them.
 * Own code, no outside library. Times are Julian days (UT); local time is IST = UT + 5.5 h.
 */
final class Sky {
    private Sky() {}

    static final double IST = 5.5 / 24.0;
    static final double LAT = 22.5726, LON = 88.3639;       // Kolkata (default city)

    /** Julian day at 00:00 UT of a Gregorian date (day may carry a fraction) */
    static double jd(int y, int m, double d) {
        if (m <= 2) { y -= 1; m += 12; }
        int a = y / 100, b = 2 - a + a / 4;
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d + b - 1524.5;
    }

    /** Julian day (UT) of local midnight (IST) starting the given date */
    static double localMidnight(int y, int m, int d) {
        return jd(y, m, d) - IST;
    }

    private static double rad(double x) { return Math.toRadians(x); }
    private static double norm(double x) { x %= 360.0; return x < 0 ? x + 360.0 : x; }

    static double moon(double j) {
        double t = (j - 2451545.0) / 36525.0;
        double lp = 218.3164477 + 481267.88123421 * t;
        double d = 297.8501921 + 445267.1114034 * t;
        double m = 357.5291092 + 35999.0502909 * t;
        double mp = 134.9633964 + 477198.8675055 * t;
        double f = 93.2720950 + 483202.0175233 * t;
        return norm(lp + 6.289 * Math.sin(rad(mp)) + 1.274 * Math.sin(rad(2 * d - mp)) + 0.658 * Math.sin(rad(2 * d))
                + 0.214 * Math.sin(rad(2 * mp)) - 0.186 * Math.sin(rad(m)) - 0.114 * Math.sin(rad(2 * f)));
    }

    static double sun(double j) {
        double t = (j - 2451545.0) / 36525.0;
        double l = 280.46646 + 36000.76983 * t;
        double m = 357.5291092 + 35999.0502909 * t;
        return norm(l + 1.915 * Math.sin(rad(m)) + 0.020 * Math.sin(rad(2 * m)));
    }

    /** Lahiri ayanamsa, degrees */
    static double ayanamsa(double j) {
        return 23.857 + 0.01397 * ((j - 2451545.0) / 365.25);
    }

    static double sunSidereal(double j) { return norm(sun(j) - ayanamsa(j)); }
    static double moonSidereal(double j) { return norm(moon(j) - ayanamsa(j)); }

    /** tithi as a number 0..30 (integer part = tithi index, 0 = Shukla pratipada) */
    static double tithi(double j) { return norm(moon(j) - sun(j)) / 12.0; }
    static double nakshatra(double j) { return moonSidereal(j) / (360.0 / 27.0); }
    static double yoga(double j) { return norm(sunSidereal(j) + moonSidereal(j)) / (360.0 / 27.0); }

    /** first time after j0 when f (cyclic, values 0..mod) reaches the next whole number; 0 if not found in 3 days */
    static double nextWhole(DoubleUnaryOperator f, double j0, double mod) {
        double target = (Math.floor(f.applyAsDouble(j0)) + 1.0) % mod;
        return crossing(f, target, j0, mod, 3.0);
    }

    /** time when f reaches target (going upward), searched from j0 for at most 'days' days; 0 if none */
    static double crossing(DoubleUnaryOperator f, double target, double j0, double mod, double days) {
        double step = 1.0 / 24.0, half = mod / 2.0;
        double a = j0;
        double ga = wrap(f.applyAsDouble(a) - target, half, mod);
        if (ga >= 0 && ga < half * 0.5) { /* already just past it: look for the next lap */ }
        for (double x = j0 + step; x <= j0 + days; x += step) {
            double gb = wrap(f.applyAsDouble(x) - target, half, mod);
            if (ga < 0 && gb >= 0) {
                double lo = x - step, hi = x;
                for (int i = 0; i < 40; i++) {
                    double mid = (lo + hi) / 2;
                    if (wrap(f.applyAsDouble(mid) - target, half, mod) < 0) lo = mid; else hi = mid;
                }
                return (lo + hi) / 2;
            }
            ga = gb;
        }
        return 0;
    }

    private static double wrap(double v, double half, double mod) {
        v = (v + half) % mod;
        if (v < 0) v += mod;
        return v - half;
    }

    /** {sunrise, sunset} as Julian days (UT) for the local date; Kolkata */
    static double[] sunriseSunset(int y, int m, int d) {
        double j0 = jd(y, m, d);                          // UT midnight of that calendar date (solar noon is near j0 + 0.5 - lon/360)
        double transit = j0 + 0.5 - LON / 360.0;
        double[] out = new double[2];
        for (int it = 0; it < 3; it++) {
            double t = (transit - 2451545.0) / 36525.0;
            double lam = rad(sun(transit));
            double eps = rad(23.4393 - 0.0130 * t);
            double ra = Math.toDegrees(Math.atan2(Math.cos(eps) * Math.sin(lam), Math.cos(lam)));
            double l0 = 280.46646 + 36000.76983 * t;
            double eot = ((l0 - ra + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
            double dec = Math.asin(Math.sin(eps) * Math.sin(lam));
            double phi = rad(LAT);
            double cosH = (Math.sin(rad(-0.833)) - Math.sin(phi) * Math.sin(dec)) / (Math.cos(phi) * Math.cos(dec));
            cosH = Math.max(-1, Math.min(1, cosH));
            double h = Math.toDegrees(Math.acos(cosH)) / 360.0;
            transit = j0 + 0.5 - LON / 360.0 - eot / 360.0;
            out[0] = transit - h;
            out[1] = transit + h;
        }
        return out;
    }
}
