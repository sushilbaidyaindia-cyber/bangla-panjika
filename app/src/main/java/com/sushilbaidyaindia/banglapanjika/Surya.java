package com.sushilbaidyaindia.banglapanjika;

/**
 * Surya Siddhanta mean motions and corrections (the classical constants as given in the standard
 * reference implementations), giving sidereal longitudes of the Sun and the Moon.
 * Two tiny calibration offsets (sun -0.05 deg, moon -0.15 deg) were fitted against a printed-panjika-based
 * reference app (13 tithi/nakshatra/yoga end times and the 12 month starts of 1433).
 */
final class Surya implements Panjika.Model {
    static final double CIV = 1577917828.0;                 // civil days in a mahayuga
    static final double SUN_YEAR = CIV / 4320000.0;          // sidereal year
    static final double SUN_ANOM = 1577917828000.0 / (4320000000.0 - 387.0);
    static final double MOON_MONTH = CIV / 57753336.0;       // sidereal month
    static final double MOON_ANOM = CIV / (57753336.0 - 488199.0);
    // fraction of a revolution at the Kali epoch, from the beginning of creation (exact values)
    static final double SUN_ANOM_OFF = 0.78575, MOON_OFF = 0.0, MOON_ANOM_OFF = 0.75;
    static final double EPOCH = 588465.5;                    // Kali Yuga: 18 Feb 3102 BCE (Julian) 0:00, Julian day
    static final double UJJAIN = 75.7683 / 360.0;            // Ujjain longitude as a fraction of a day
    static final double SUN_FIX = -0.05, MOON_FIX = -0.117;

    private static double rev(double x) { return 360.0 * (x - Math.floor(x)); }

    private static double truePos(double tee, double period, double off, double size, double anomPeriod,
                                  double anomOff, double change) {
        double lam = rev(tee / period + off);
        double anom = rev(tee / anomPeriod + anomOff);
        double s = Math.sin(Math.toRadians(anom));
        double eq = Math.toDegrees(Math.asin(s * (size - Math.abs(s) * change * size)));
        return lam - eq;
    }

    @Override public double sun(double j) {
        double tee = j - EPOCH + UJJAIN;
        double v = truePos(tee, SUN_YEAR, 0.0, 14.0 / 360.0, SUN_ANOM, SUN_ANOM_OFF, 1.0 / 42.0) + SUN_FIX;
        return ((v % 360.0) + 360.0) % 360.0;
    }

    @Override public double moon(double j) {
        double tee = j - EPOCH + UJJAIN;
        double v = truePos(tee, MOON_MONTH, MOON_OFF, 32.0 / 360.0, MOON_ANOM, MOON_ANOM_OFF, 1.0 / 96.0) + MOON_FIX;
        return ((v % 360.0) + 360.0) % 360.0;
    }
}
