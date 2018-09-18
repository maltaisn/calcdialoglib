package com.nmaltais.calcdialog;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Locale;

class CalcDialogUtils {

    /**
     * Checks if a BigDecimal exceeds maximum value
     * @param value value to check for
     * @return true if value is greater than maximum value
     *         maximum value is applied equally for positive and negative value
     */
    static boolean isValueOutOfBounds(@NonNull BigDecimal value, @Nullable BigDecimal maxValue) {
        return maxValue != null && (value.compareTo(maxValue) > 0 ||
                value.compareTo(maxValue.negate()) < 0);
    }

    /**
     * Get device's default locale
     * @param context any context
     * @return the default locale
     */
    static Locale getDefaultLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    /**
     * Strip trailing zeroes from a BigDecimal. Essentially calls {@link BigDecimal#stripTrailingZeros()}
     * but fixes bug (http://hg.openjdk.java.net/jdk8/jdk8/jdk/rev/2ee772cda1d6) where zeroes
     * aren't stripped if value is zero.
     * @param from BigDecimal to strip trailing zeroes from
     * @return BigDecimal with stripped trailing zeroes
     */
    static BigDecimal stripTrailingZeroes(@NonNull BigDecimal from) {
        if (from.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else {
            return from.stripTrailingZeros();
        }
    }

}
