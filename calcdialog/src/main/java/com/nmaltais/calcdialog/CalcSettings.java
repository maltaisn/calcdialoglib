package com.nmaltais.calcdialog;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

class CalcSettings {

    int requestCode;

    BigDecimal initialValue;

    @Nullable BigDecimal maxValue;

    int maxIntDigits;
    int maxFracDigits;

    RoundingMode roundingMode;

    boolean signCanBeChanged;
    int initialSign;

    boolean clearOnOperation;
    boolean showZeroWhenNoValue;

    boolean showAnswerBtn;
    boolean showSignBtn;

    char decimalSep;
    char groupSep;
    int groupSize;

    CalcSettings() {
        maxValue = new BigDecimal("1E10");
        maxIntDigits = 10;
        maxFracDigits = 8;
        roundingMode = RoundingMode.HALF_UP;

        decimalSep = CalcDialog.FORMAT_CHAR_DEFAULT;
        groupSep = CalcDialog.FORMAT_CHAR_DEFAULT;
        groupSize = 3;

        signCanBeChanged = true;

        clearOnOperation = false;
        showZeroWhenNoValue = true;

        showAnswerBtn = false;
        showSignBtn = true;
    }

    void writeToBundle(Bundle bundle) {
        if (maxValue != null) {
            bundle.putString("maxValue", maxValue.toString());
        }
        bundle.putInt("requestCode", requestCode);
        bundle.putInt("maxIntDigits", maxIntDigits);
        bundle.putInt("maxFracDigits", maxFracDigits);
        bundle.putString("roundingMode", roundingMode.toString());
        bundle.putBoolean("signCanBeChanged", signCanBeChanged);
        bundle.putInt("initialSign", initialSign);
        bundle.putBoolean("clearOnOperation", clearOnOperation);
        bundle.putBoolean("showZeroWhenNoValue", showZeroWhenNoValue);
        bundle.putBoolean("showAnswerBtn", showAnswerBtn);
        bundle.putBoolean("showSignBtn", showSignBtn);
        bundle.putChar("decimalSep", decimalSep);
        bundle.putChar("groupSep", groupSep);
        bundle.putInt("groupSize", groupSize);
    }

    void readFromBundle(Bundle bundle) {
        if (bundle.containsKey("maxValue")) {
            maxValue = new BigDecimal(bundle.getString("maxValue"));
        }
        requestCode = bundle.getInt("requestCode");
        maxIntDigits = bundle.getInt("maxIntDigits");
        maxFracDigits = bundle.getInt("maxFracDigits");
        roundingMode = RoundingMode.valueOf(bundle.getString("roundingMode"));
        signCanBeChanged = bundle.getBoolean("signCanBeChanged");
        initialSign = bundle.getInt("initialSign");
        clearOnOperation = bundle.getBoolean("clearOnOperation");
        showZeroWhenNoValue = bundle.getBoolean("showZeroWhenNoValue");
        showAnswerBtn = bundle.getBoolean("showAnswerBtn");
        showSignBtn = bundle.getBoolean("showSignBtn");
        decimalSep = bundle.getChar("decimalSep");
        groupSep = bundle.getChar("groupSep");
        groupSize = bundle.getInt("groupSize");
    }

    void setValue(@Nullable BigDecimal value) {
        if (value != null && maxValue != null && CalcDialogUtils.isValueOutOfBounds(value, maxValue)) {
            value = (value.compareTo(BigDecimal.ZERO) > 0 ? maxValue : maxValue.negate());
        }
        initialValue = value;
    }

    void setMaxValue(@Nullable BigDecimal maxValue) {
        if (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) < 0) {
            // Must be positive
            maxValue = maxValue.negate();
        }
        this.maxValue = maxValue;

        if (initialValue != null && CalcDialogUtils.isValueOutOfBounds(initialValue, maxValue)) {
            // Initial value is greater than max value
            initialValue = maxValue;
        }
    }

    void setMaxDigits(int intPart, int fracPart) {
        if (intPart != CalcDialog.MAX_DIGITS_UNLIMITED && intPart < 1 ||
                fracPart != CalcDialog.MAX_DIGITS_UNLIMITED && fracPart < 0) {
            throw new IllegalArgumentException("Max integer part must be at least 1 and max fractional part must be at least 0.");
        }

        maxIntDigits = intPart;
        maxFracDigits = fracPart;
    }

    void setRoundingMode(RoundingMode roundingMode) {
        if (roundingMode.equals(RoundingMode.UNNECESSARY)) {
            throw new IllegalArgumentException("Cannot use RoundingMode.UNNECESSARY as a rounding mode.");
        }

        this.roundingMode = roundingMode;
    }

    void setSignCanBeChanged(boolean canBeChanged, int sign) {
        signCanBeChanged = canBeChanged;
        if (!signCanBeChanged) {
            if (sign != -1 && sign != 1) {
                throw new IllegalArgumentException("Sign cannot be changed was set but no valid sign is given.");
            }
            initialSign = sign;
        }
    }

    void setFormatSymbols(char decimalSep, char groupSep) {
        if (decimalSep != CalcDialog.FORMAT_CHAR_DEFAULT && decimalSep == groupSep) {
            throw new IllegalArgumentException("Decimal separator cannot be the same as grouping separator.");
        }

        this.decimalSep = decimalSep;
        this.groupSep = groupSep;
    }

    void setGroupSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Group size must be positive");
        }
        groupSize = size;
    }

}
