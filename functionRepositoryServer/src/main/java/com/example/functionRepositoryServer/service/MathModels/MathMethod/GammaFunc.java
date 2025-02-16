package com.example.functionRepositoryServer.service.MathModels.MathMethod;

import static java.lang.Math.exp;
import static java.lang.Math.log;

/**
 * Класс Гамма-функции
 */
public class GammaFunc {
    /** Maximum allowed number of iterations. */
    private static final int ITMAX = 100;

    /** Relative accuracy. */
    private static final double EPS = 3.0e-7;

    /** Number near the smallest representable. */
    private static final double FPMIN = Double.MIN_VALUE;

    public static double gammln(double xx) {
        double x, y, tmp, ser;
        x = y = tmp = ser = 0.0;

        final double[] cof = { 76.18009172947146, -86.50532032941677,
                24.01409824083091, -1.231739572450155, 0.1208650973866179e-2,
                -0.5395239384953e-5 };
        int j;
        y = x = xx;
        tmp = x + 5.5;
        tmp -= (x + 0.5) * log(tmp);
        ser = 1.000000000190015;
        for (j = 0; j <= 5; j++) {
            ser += cof[j] / ++y;
        }

        return -tmp + log(2.5066282746310005 * ser / x);
    }

    public static double gamma(double xx) {
        return exp(gammln(xx));
    }

    public static double lowerIncomplete(double a, double x) {
        return regularizedGammaP(a, x) * gamma(a);
    }

    public static double upperIncomplete(double a, double x) {
        return regularizedGammaQ(a, x) * gamma(a);
    }

    public static double regularizedGammaQ(double a, double x) {
        if (a <= 0.0)
            throw new IllegalArgumentException(
                    "Invalid arguments in routine gammq");
        return 1.0 - regularizedGammaP(a, x);
    }

    public static double regularizedGammaP(double a, double x) {
        if (a <= 0.0)
            throw new IllegalArgumentException(
                    "Invalid arguments in routine gammp");
        if (x < (a + 1.0)) { // Use the series representation.
            double gamser = gser(a, x);
            return gamser;
        } else { // Use the continued fraction representation
            double gammcf = gcf(a, x);
            return 1.0 - gammcf; // and take its complement.
        }
    }

    private static double gser(double a, double x) {
        // Returns the incomplete gamma function P(a, x) evaluated by its series
        // representation as gamser.
        // Also returns ln gamma(a) as gln.
        // double sum, del, ap;
        double gln = gammln(a);
        if (x <= 0.0) {
            if (x < 0.0)
                throw new IllegalArgumentException("x < 0 in routine gser");
            return 0.0;
        } else {
            double gamser = 0.0;
            double ap = a;
            double del, sum;
            del = sum = 1.0 / a;
            for (int n = 1; n <= ITMAX; n++) {
                ++ap;
                del *= x / ap;
                sum += del;
                if (Math.abs(del) < Math.abs(sum) * EPS) {
                    gamser = sum * exp(-x + a * log(x) - gln);
                    return gamser;
                }
            }
            throw new IllegalArgumentException(
                    "a too large, ITMAX too small in routine gser");
        }
    }

    private static double gcf(double a, double x) {
        double an, del;
        int i;
        double b = x + 1.0 - a; // Set up for evaluating continued fraction
        // by modified Lentz’s method (§5.2) with b0 = 0.
        double c = 1.0 / FPMIN;
        double d = 1.0 / b;
        double h = d;
        for (i = 1; i <= ITMAX; i++) { // Iterate to convergence.
            an = -i * (i - a);
            b += 2.0;
            d = an * d + b;
            if (Math.abs(d) < FPMIN)
                d = FPMIN;
            c = b + an / c;
            if (Math.abs(c) < FPMIN)
                c = FPMIN;
            d = 1.0 / d;
            del = d * c;
            h *= del;
            if (Math.abs(del - 1.0) < EPS)
                break;
        }
        if (i > ITMAX)
            throw new IllegalArgumentException(
                    "a too large, ITMAX too small in gcf");
        // Put factors in front.
        double gln = gammln(a);
        double gammcf = exp(-x + a * log(x) - gln) * h;
        return gammcf; // imcomplete gamma function Q(a,x)
    }

}
