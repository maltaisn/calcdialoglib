/*
 * Copyright 2019 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.calcdialog;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Settings for the calculator dialog.
 */
@SuppressWarnings("unused")
public class CalcSettings implements Parcelable {

    private static final String TAG = CalcSettings.class.getSimpleName();


    int requestCode = 0;

    // Appearance settings
    @NonNull NumberFormat nbFormat = NumberFormat.getInstance();
    int maxIntDigits = 10;

    @NonNull CalcNumpadLayout numpadLayout = CalcNumpadLayout.CALCULATOR;
    boolean isExpressionShown = false;
    boolean isZeroShownWhenNoValue = true;
    boolean isAnswerBtnShown = false;
    boolean isSignBtnShown = true;
    boolean isExpressionEditable = false;
    boolean shouldEvaluateOnOperation = false;

    // Behavior settings
    @Nullable BigDecimal initialValue = null;
    @Nullable BigDecimal minValue = new BigDecimal("-1E10");
    @Nullable BigDecimal maxValue = new BigDecimal("1E10");
    boolean isOrderOfOperationsApplied = true;

    CalcSettings() {
        nbFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);
        nbFormat.setMaximumFractionDigits(8);
    }

    void validate() {
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) >= 0) {
            throw new IllegalArgumentException("Minimum value must be less than maximum value.");
        }
    }

    /**
     * Set the request code by which the dialog is identified
     * @param requestCode A request code.
     * @return The settings
     */
    public CalcSettings setRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public int getRequestCode() {
        return requestCode;
    }

    /**
     * Set the number format to use for formatting the currently displayed value and the
     * values in the expression (if shown). This can be used to set a prefix and a suffix,
     * changing the grouping settings, the minimum and maximum integer and fraction digits,
     * the decimal separator, the rounding mode, and probably more.
     * By default, the locale's default decimal format is used.
     * @param format A number format.
     * @return The settings
     * @see NumberFormat
     */
    public CalcSettings setNumberFormat(@NonNull NumberFormat format) {
        if (format.getRoundingMode() == RoundingMode.UNNECESSARY) {
            throw new IllegalArgumentException("Cannot use RoundingMode.UNNECESSARY as a rounding mode.");
        }

        this.nbFormat = format;

        // The max int setting on number format is used to set the maximum int digits that can be entered.
        // However, it is possible that the user evaluates expressions resulting in bigger numbers.
        // If not changed, this would show those values as "0,000" instead of "10,000" for example.
        maxIntDigits = nbFormat.getMaximumIntegerDigits();
        nbFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);

        return this;
    }

    @NonNull
    public NumberFormat getNumberFormat() {
        return nbFormat;
    }

    /**
     * Set the layout of the calculator's numpad, either with 123 on the top row or 789.
     * Default layout is {@link CalcNumpadLayout#CALCULATOR}, with 789 on the top row.
     * @param layout Numpad layout to use
     * @return The settings
     * @see CalcNumpadLayout
     */
    public CalcSettings setNumpadLayout(@NonNull CalcNumpadLayout layout) {
        numpadLayout = layout;
        return this;
    }

    @NonNull
    public CalcNumpadLayout getNumpadLayout() {
        return numpadLayout;
    }

    /**
     * Set whether to show the expression above the value when the user is typing it.
     * By default, the expression is not shown.
     * @param shown Whether to show it or not.
     * @return The settings
     */
    public CalcSettings setExpressionShown(boolean shown) {
        isExpressionShown = shown;
        return this;
    }

    public boolean isExpressionShown() {
        return isExpressionShown;
    }

    /**
     * Set whether to the expression can be edited by erasing further than the current value.
     * By default the expression is not editable.
     * @param editable Whether to show it or not.
     * @return The settings
     */
    public CalcSettings setExpressionEditable(boolean editable) {
        isExpressionEditable = editable;
        return this;
    }

    public boolean isExpressionEditable() {
        return isExpressionEditable;
    }

    /**
     * Set whether zero should be displayed when no value has been entered or just display nothing.
     * This happens when initial value is null, when an error is dismissed, or when an operator
     * is clicked and {@link #shouldEvaluateOnOperation} is set to true.
     * @param shown Whether to show it or not.
     * @return The settings
     */
    public CalcSettings setZeroShownWhenNoValue(boolean shown) {
        isZeroShownWhenNoValue = shown;
        return this;
    }

    public boolean isZeroShownWhenNoValue() {
        return isZeroShownWhenNoValue;
    }

    /**
     * Set whether to show the answer button when an operation button is clicked or not.
     * This button allows the user to enter the value that was previously calculated.
     * By default, the answer button is not shown.
     * @param shown Whether to show it or not.
     * @return The settings
     */
    public CalcSettings setAnswerBtnShown(boolean shown) {
        isAnswerBtnShown = shown;
        return this;
    }

    public boolean isAnswerBtnShown() {
        return isAnswerBtnShown;
    }

    /**
     * Set whether the sign button should be shown. By default it is shown.
     * @param shown Whether to show it or not.
     * @return The settings
     */
    public CalcSettings setSignBtnShown(boolean shown) {
        isSignBtnShown = shown;
        return this;
    }

    public boolean isSignBtnShown() {
        return isSignBtnShown;
    }

    /**
     * Set whether to evaluate the expression when an operation button is pressed (+, -, * and /).
     * If not, the display will show zero or no value if {@link #isZeroShownWhenNoValue} is true.
     * By default, the expression is evaluated.
     * @param shouldClear Whether to evaluate it or not.
     * @return The settings
     */
    public CalcSettings setShouldEvaluateOnOperation(boolean shouldClear) {
        shouldEvaluateOnOperation = shouldClear;
        return this;
    }

    public boolean isShouldEvaluateOnOperation() {
        return shouldEvaluateOnOperation;
    }

    /**
     * Set initial value to show. It must be within minimum and maximum values.
     * If null and {@link #isZeroShownWhenNoValue} is set to false, no value will be shown.
     * By default, initial value is null, which results in a 0 for the calculator.
     * @param value Initial value to display. Use null for no value.
     * @return The settings
     */
    public CalcSettings setInitialValue(@Nullable BigDecimal value) {
        initialValue = value;
        return this;
    }

    @Nullable
    public BigDecimal getInitialValue() {
        return initialValue;
    }

    /**
     * Set minimum value that can be entered.
     * If the minimum value is exceeded, an "Out of bounds" error will be shown when user clicks OK.
     * Default minimum is -10,000,000,000 (-1e+10).
     * @param minValue Minimum value, use null for no minimum.
     * @return The settings
     */
    public CalcSettings setMinValue(@Nullable BigDecimal minValue) {
        this.minValue = minValue;
        return this;
    }

    @Nullable
    public BigDecimal getMinValue() {
        return minValue;
    }

    /**
     * Set maximum value that can be entered.
     * If the maximum value is exceeded, an "Out of bounds" error will be shown when user clicks OK.
     * Default maximum is 10,000,000,000 (1e+10).
     * @param maxValue Maximum value, use null for no maximum.
     * @return The settings
     */
    public CalcSettings setMaxValue(@Nullable BigDecimal maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    @Nullable
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    /**
     * Set whether to apply the operation priority on the entered expression, i.e. evaluating
     * products and quotients before, from left to right.
     * If not, the operations are evaluated in the same order as they are entered.
     * @param isApplied Whether to apply operation priority or not.
     * @return The settings
     */
    public CalcSettings setOrderOfOperationsApplied(boolean isApplied) {
        isOrderOfOperationsApplied = isApplied;
        return this;
    }

    public boolean isOrderOfOperationsApplied() {
        return isOrderOfOperationsApplied;
    }


    ////////// PARCELABLE //////////
    private CalcSettings(Parcel in) {
        Bundle bundle = in.readBundle(getClass().getClassLoader());
        if (bundle != null) {
            nbFormat = getNumberFormatFromBundle(bundle);
            requestCode = bundle.getInt("requestCode");
            //noinspection ConstantConditions
            numpadLayout = (CalcNumpadLayout) bundle.getSerializable("numpadLayout");
            isExpressionShown = bundle.getBoolean("isExpressionShown");
            isZeroShownWhenNoValue = bundle.getBoolean("isZeroShownWhenNoValue");
            isAnswerBtnShown = bundle.getBoolean("isAnswerBtnShown");
            isSignBtnShown = bundle.getBoolean("isSignBtnShown");
            shouldEvaluateOnOperation = bundle.getBoolean("shouldEvaluateOnOperation");

            if (bundle.containsKey("initialValue")) {
                initialValue = (BigDecimal) bundle.getSerializable("initialValue");
            }
            if (bundle.containsKey("minValue")) {
                minValue = (BigDecimal) bundle.getSerializable("minValue");
            }
            if (bundle.containsKey("maxValue")) {
                maxValue = (BigDecimal) bundle.getSerializable("maxValue");
            }
            isOrderOfOperationsApplied = bundle.getBoolean("isOrderOfOperationsApplied");
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
        Bundle bundle = new Bundle();

        bundle.putInt("requestCode", requestCode);
        bundle.putSerializable("numpadLayout", numpadLayout);
        bundle.putBoolean("isExpressionShown", isExpressionShown);
        bundle.putBoolean("isZeroShownWhenNoValue", isZeroShownWhenNoValue);
        bundle.putBoolean("isAnswerBtnShown", isAnswerBtnShown);
        bundle.putBoolean("isSignBtnShown", isSignBtnShown);
        bundle.putBoolean("shouldEvaluateOnOperation", shouldEvaluateOnOperation);
        bundle.putBoolean("isOrderOfOperationsApplied", isOrderOfOperationsApplied);

        putNumberFormatInBundle(bundle);

        if (initialValue != null) bundle.putSerializable("initialValue", initialValue);
        if (minValue != null) bundle.putSerializable("minValue", minValue);
        if (maxValue != null) bundle.putSerializable("maxValue", maxValue);

        try {
            out.writeBundle(bundle);
        } catch (UnsupportedOperationException uoe) {
            // Workaround for issue https://issuetracker.google.com/issues/37043137
            Log.e(TAG, "Failed to parcel Bundle.");
        }
    }

    private void putNumberFormatInBundle(Bundle bundle) {
        bundle.putSerializable("nbFormat", nbFormat);
        if (nbFormat instanceof DecimalFormat) {
            bundle.putSerializable("nbfmtPattern", ((DecimalFormat) nbFormat).toPattern());
        }
    }

    private NumberFormat getNumberFormatFromBundle(Bundle bundle) {
        NumberFormat nbFmt = null;
        try {
            nbFmt = (NumberFormat) bundle.getSerializable("nbFormat");
            if (nbFmt != null) {
                try {
                    //noinspection ConstantConditions
                    if (nbFmt.getRoundingMode() == null) {
                        // Often, NumberFormat deserialization succeeds but its rounding mode
                        // is still `null`, which will make some operations like division fail.
                        // So set a default one.
                        nbFmt.setRoundingMode(RoundingMode.HALF_EVEN);
                        Log.e(TAG, "Failed to deserialize DecimalFormat rounding mode, reset to HALF_EVEN.");
                    }
                } catch (UnsupportedOperationException uoe) {
                    // Number format doesn't have a rounding mode.
                }
            }
        } catch (NullPointerException npe) {
            // Very rarely and on API >= 28, Bundle will fail to get serialized NumberFormat.
            // This issue is related to: https://stackoverflow.com/a/54155356/5288316.
            // Luckily, NumberFormat is most often a DecimalFormat, which can be saved
            // using a pattern. Note that the naming of the key is important here, it must
            // not start with "nbFormat" for some reason!
            if (bundle.containsKey("nbfmtPattern")) {
                nbFmt = new DecimalFormat(bundle.getString("nbfmtPattern", ""));
            } else {
                Log.e(TAG, "Failed to deserialize NumberFormat.");
            }
            // Otherwise number format is lost for good, keep default.
        }
        if (nbFmt == null) {
            nbFmt = NumberFormat.getInstance();
        }
        return nbFmt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CalcSettings> CREATOR = new Creator<CalcSettings>() {
        @Override
        public CalcSettings createFromParcel(Parcel in) {
            return new CalcSettings(in);
        }

        @Override
        public CalcSettings[] newArray(int size) {
            return new CalcSettings[size];
        }
    };

}
