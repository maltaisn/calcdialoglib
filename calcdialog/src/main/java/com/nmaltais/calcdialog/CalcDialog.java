/*
 * Copyright (c) 2018 Nicolas Maltais
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.nmaltais.calcdialog;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatTextView;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 * Dialog with calculator for entering and calculating a number
 */
public class CalcDialog extends DialogFragment {

    private static final String TAG = CalcDialog.class.getSimpleName();

    // Digits button: 0-9
    private final static int[] NUMBER_BTN_IDS = {
            R.id.button_calc_0,
            R.id.button_calc_1,
            R.id.button_calc_2,
            R.id.button_calc_3,
            R.id.button_calc_4,
            R.id.button_calc_5,
            R.id.button_calc_6,
            R.id.button_calc_7,
            R.id.button_calc_8,
            R.id.button_calc_9,
    };

    // Operator button: +, -, *, /
    private final static int[] OPERATOR_BTN_IDS = {
            R.id.button_calc_add,
            R.id.button_calc_sub,
            R.id.button_calc_mult,
            R.id.button_calc_div,
    };

    private static final int OPERATION_NONE = -1;
    private static final int OPERATION_ADD = 0;
    private static final int OPERATION_SUB = 1;
    private static final int OPERATION_MULT = 2;
    private static final int OPERATION_DIV = 3;

    private static final int ERROR_NONE = -1;
    private static final int ERROR_DIV_ZERO = 0;
    private static final int ERROR_OUT_OF_BOUNDS = 1;
    private static final int ERROR_WRONG_SIGN_POS = 2;
    private static final int ERROR_WRONG_SIGN_NEG = 3;

    private Context context;

    private @Nullable BigDecimal maxValue;

    /**
     * Parameter value to set for {@link #setMaxDigits(int, int)}
     * to have to limit on the number of digits for a part of the number (int or frac).
     */
    public static final int MAX_DIGITS_UNLIMITED = -1;

    private int maxIntDigits;
    private int maxFracDigits;

    private RoundingMode roundingMode;

    private boolean stripTrailingZeroes;

    private boolean signCanBeChanged;
    private boolean hideSignButton;
    private int initialSign;

    private boolean clearOnOperation;
    private boolean showZeroWhenNoValue;

    private boolean showAnswerBtn;

    /**
     * Parameter to set for {@link #setFormatSymbols(char, char)}
     * to use default locale's format symbol.
     * This is the default value for both decimal and group separators
     */
    public static final char FORMAT_CHAR_DEFAULT = 0;

    private char decimalSep;
    private char groupSep;

    private int groupSize;

    private int operation;
    private int error;
    private StringBuilder valueStr;
    private @Nullable BigDecimal resultValue;
    private boolean resultIsDisplayed;
    private boolean overwriteValue;

    private @Nullable BigDecimal answerValue;
    private boolean currentIsAnswer;

    private AppCompatTextView textvDisplay;
    private AppCompatTextView btnAnswer;

    private CharSequence[] btnTexts;
    private CharSequence[] errorMessages;
    private int[] maxDialogDimensions;

    private String zeroString;

    /**
     * Create a new calculator dialog with default settings
     */
    public CalcDialog() {
        // Set default settings
        maxValue = new BigDecimal("1E10");
        maxIntDigits = 10;
        maxFracDigits = 8;
        roundingMode = RoundingMode.HALF_UP;
        stripTrailingZeroes = true;

        decimalSep = FORMAT_CHAR_DEFAULT;
        groupSep = FORMAT_CHAR_DEFAULT;
        groupSize = 3;

        signCanBeChanged = true;

        clearOnOperation = false;
        showZeroWhenNoValue = true;

        showAnswerBtn = false;
        answerValue = null;
        currentIsAnswer = false;

        operation = OPERATION_NONE;
        error = ERROR_NONE;
        resultValue = null;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setRetainInstance(true);

        // Get locale's symbols for number formatting
        Locale locale = getDefaultLocale(context);
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(locale);
        if (decimalSep == FORMAT_CHAR_DEFAULT) decimalSep = dfs.getDecimalSeparator();
        if (groupSep == FORMAT_CHAR_DEFAULT) groupSep = dfs.getGroupingSeparator();

        // Get string to display for zero
        BigDecimal zero = BigDecimal.ZERO;
        if (!stripTrailingZeroes) zero = zero.setScale(maxFracDigits, roundingMode);
        zeroString = zero.toPlainString();
        int pointPos = zeroString.indexOf('.');
        if (pointPos != -1 && decimalSep != '.') {
            // Replace "." with correct decimal separator
            StringBuilder sb = new StringBuilder(zeroString);
            sb.setCharAt(pointPos, decimalSep);
            zeroString = sb.toString();
        }

        // Init values
        if (resultValue != null) {
            resultValue = resultValue.setScale(maxFracDigits, roundingMode);
            if (stripTrailingZeroes) resultValue = stripTrailingZeroes(resultValue);
            answerValue = resultValue;

            valueStr = new StringBuilder(resultValue.toPlainString());

        } else {
            valueStr = new StringBuilder();
        }
        formatValue();

        // Get strings
        TypedArray ta = context.obtainStyledAttributes(R.styleable.CalcDialog);
        btnTexts = ta.getTextArray(R.styleable.CalcDialog_calcButtonTexts);
        errorMessages = ta.getTextArray(R.styleable.CalcDialog_calcErrors);
        maxDialogDimensions = new int[]{
                ta.getDimensionPixelSize(R.styleable.CalcDialog_calcDialogMaxWidth, -1),
                ta.getDimensionPixelSize(R.styleable.CalcDialog_calcDialogMaxHeight, -1)
        };
        ta.recycle();

        resultIsDisplayed = true;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle state) {
        final View view = View.inflate(context, R.layout.dialog_calc, null);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            LinearLayout header = view.findViewById(R.id.header);
            header.setBackgroundResource(R.drawable.elevation);
        }

        // Value display
        textvDisplay = view.findViewById(R.id.text_value);
        textvDisplay.setText(valueStr.toString());

        // Erase button
        CalcEraseButton eraseBtn = view.findViewById(R.id.button_calc_erase);
        eraseBtn.setOnEraseListener(new CalcEraseButton.EraseListener() {
            @Override
            public void onErase() {
                if (dismissError()) return;

                hideAnswerBtn();

                if (valueStr.length() > 0) {
                    if (resultIsDisplayed || overwriteValue) {
                        valueStr.setLength(0);
                        overwriteValue = false;

                    } else {
                        removeGroupSeparators();

                        // Erase last digit
                        valueStr.deleteCharAt(valueStr.length() - 1);
                        if (valueStr.length() > 0) {
                            // Don't leave useless negative sign or decimal separator
                            char last = valueStr.charAt(valueStr.length() - 1);
                            if (last == decimalSep || last == '-') {
                                valueStr.deleteCharAt(valueStr.length() - 1);
                            }
                        }
                    }

                    formatValue();
                    textvDisplay.setText(valueStr.toString());
                    resultIsDisplayed = false;
                }
            }

            @Override
            public void onEraseAll() {
                clear();
            }
        });

        for (int i = 0; i < NUMBER_BTN_IDS.length; i++) {
            AppCompatTextView numberBtn = view.findViewById(NUMBER_BTN_IDS[i]);
            numberBtn.setText(btnTexts[i]);

            final int nb = i;
            numberBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissError();

                    hideAnswerBtn();

                    if (resultIsDisplayed || overwriteValue) {
                        valueStr.setLength(0);
                        overwriteValue = false;
                    }

                    removeGroupSeparators();

                    // Check if max digits has been exceeded
                    int pointPos = valueStr.indexOf(String.valueOf(decimalSep));
                    if ((pointPos != -1 || maxIntDigits == MAX_DIGITS_UNLIMITED || valueStr.length() < maxIntDigits) &&
                            (pointPos == -1 || maxFracDigits == MAX_DIGITS_UNLIMITED || valueStr.length() - pointPos - 1 < maxFracDigits)) {
                        // If max int or max frac digits have not already been reached
                        // Concatenate current value with new digit
                        valueStr.append(nb);
                    }

                    formatValue();
                    textvDisplay.setText(valueStr.toString());
                    resultIsDisplayed = false;
                }
            });
        }

        for (int i = 0; i < OPERATOR_BTN_IDS.length; i++) {
            final int op = i;
            AppCompatTextView operatorBtn = view.findViewById(OPERATOR_BTN_IDS[i]);
            operatorBtn.setText(btnTexts[i + NUMBER_BTN_IDS.length]);
            operatorBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dismissError()) return;

                    if (valueStr.length() != 0 || currentIsAnswer) {
                        if (operation != OPERATION_NONE) {
                            calculate();
                        } else {
                            resultValue = getCurrentValue();
                            if (!clearOnOperation) {
                                valueStr = new StringBuilder();
                                formatValue();
                                resultIsDisplayed = false;
                            }
                        }
                    }

                    operation = op;
                    if (clearOnOperation) {
                        valueStr = new StringBuilder();
                        formatValue();
                        textvDisplay.setText(valueStr.toString());
                        resultIsDisplayed = false;
                    }

                    // Show answer button if needed
                    if (showAnswerBtn && answerValue != null) {
                        btnAnswer.setVisibility(View.VISIBLE);
                    } else {
                        hideAnswerBtn();
                    }
                }
            });
        }

        // Decimal separator button
        final AppCompatTextView decimalBtn = view.findViewById(R.id.button_calc_decimal);
        decimalBtn.setText(btnTexts[15]);
        decimalBtn.setEnabled(maxFracDigits > 0);
        decimalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissError();

                hideAnswerBtn();

                if (resultIsDisplayed || overwriteValue) {
                    valueStr.setLength(0);
                    overwriteValue = false;
                }

                if (valueStr.indexOf(String.valueOf(decimalSep)) == -1) {
                    // Only insert a decimal point if there isn't one yet
                    if (valueStr.length() == 0) {
                        // Add 0 before decimal point .1 --> 0.1
                        valueStr.append("0");
                    }

                    valueStr.append(decimalSep);

                    textvDisplay.setText(valueStr.toString());
                    resultIsDisplayed = false;
                }
            }
        });

        // Sign button: +/-
        AppCompatTextView signBtn = view.findViewById(R.id.button_calc_sign);
        if (hideSignButton) {
            signBtn.setVisibility(View.GONE);
        } else {
            signBtn.setText(btnTexts[14]);
            signBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dismissError()) return;

                    // Negate value
                    String str = valueStr.toString();
                    if (!str.isEmpty() && !str.equals("0") && !str.equals("0" + decimalSep)) {
                        // If value is not equal to zero or empty
                        if (valueStr.charAt(0) != '-') {
                            valueStr.insert(0, '-');
                        } else {
                            valueStr.deleteCharAt(0);
                        }

                        if (resultIsDisplayed) {
                            //noinspection ConstantConditions
                            resultValue = resultValue.negate();
                            //noinspection ConstantConditions
                            answerValue = answerValue.negate();
                        }

                        textvDisplay.setText(valueStr.toString());
                        resultIsDisplayed = false;
                    }
                }
            });
        }

        // Equal button
        AppCompatTextView equalBtn = view.findViewById(R.id.button_calc_equal);
        equalBtn.setText(btnTexts[16]);
        equalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dismissError()) return;
                calculate();

                answerValue = resultValue;
            }
        });

        // Answer button
        btnAnswer = view.findViewById(R.id.button_calc_answer);
        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAnswer.setVisibility(View.GONE);

                textvDisplay.setText(R.string.calc_answer);

                currentIsAnswer = true;
                resultIsDisplayed = false;
            }
        });

        // Dialog buttons
        Button clearBtn = view.findViewById(R.id.button_dialog_clear);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });

        Button cancelBtn = view.findViewById(R.id.button_dialog_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });

        Button okBtn = view.findViewById(R.id.button_dialog_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dismissError()) return;

                calculate();

                // If operation was not done, make OK button act as an equal btn
                if (operation != OPERATION_NONE) return;

                if (error == ERROR_NONE) {
                    // If sign can't be changed, check if sign is right
                    if (!signCanBeChanged && resultValue != null) {
                        int sign = resultValue.signum();
                        if (sign != 0 && sign != initialSign) {
                            // Wrong sign
                            setError(sign == 1 ? ERROR_WRONG_SIGN_NEG : ERROR_WRONG_SIGN_POS);
                            return;
                        }
                    }

                    // Call callback
                    if (getTargetFragment() != null) {
                        // Caller was a fragment
                        try {
                            ((CalcDialogCallback) getTargetFragment()).onValueEntered(resultValue);
                        } catch (Exception e) {
                            // Interface callback is not implemented in fragment
                        }
                    } else {
                        // Caller was an activity
                        try {
                            //noinspection ConstantConditions
                            ((CalcDialogCallback) getActivity()).onValueEntered(resultValue);
                        } catch (Exception e) {
                            // Interface callback is not implemented in activity
                        }
                    }
                    dismissAllowingStateLoss();
                }
            }
        });

        // Set up dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Get maximum dialog dimensions
                Rect fgPadding = new Rect();
                dialog.getWindow().getDecorView().getBackground().getPadding(fgPadding);
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                int height = metrics.heightPixels - fgPadding.top - fgPadding.bottom;
                int width = metrics.widthPixels - fgPadding.top - fgPadding.bottom;

                // Set dialog's dimensions
                if (width > maxDialogDimensions[0]) width = maxDialogDimensions[0];
                if (height > maxDialogDimensions[1]) height = maxDialogDimensions[1];
                dialog.getWindow().setLayout(width, height);

                // Set dialog's content
                view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
                dialog.setContentView(view);
            }
        });

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        reset();
    }

    /**
     * Reset value and operation to none
     * Doesn't dismiss error and doesn't update display
     */
    private void reset() {
        operation = OPERATION_NONE;
        resultValue = null;
        overwriteValue = true;

        answerValue = null;
        hideAnswerBtn();

        valueStr = new StringBuilder();
        formatValue();

        resultIsDisplayed = true;
    }

    /**
     * Calculate result of operation between current result and operand
     */
    private void calculate() {
        if (resultIsDisplayed) {
            return;
        }

        if (operation == OPERATION_NONE || valueStr.length() == 0) {
            resultValue = getCurrentValue();

        } else {
            if (resultValue == null) resultValue = BigDecimal.ZERO;
            BigDecimal operand = getCurrentValue();

            if (operation == OPERATION_ADD) {
                //noinspection ConstantConditions
                resultValue = resultValue.add(operand);
            } else if (operation == OPERATION_SUB) {
                resultValue = resultValue.subtract(operand);
            } else if (operation == OPERATION_MULT) {
                resultValue = resultValue.multiply(operand);
            } else if (operation == OPERATION_DIV) {
                if (operand.compareTo(BigDecimal.ZERO) == 0) {
                    setError(ERROR_DIV_ZERO);
                    return;
                } else {
                    resultValue = resultValue.divide(operand, maxFracDigits, roundingMode);
                }
            }
        }

        if (isValueOutOfBounds(resultValue)) {
            setError(ERROR_OUT_OF_BOUNDS);
            return;
        }

        resultValue = resultValue.setScale(maxFracDigits, roundingMode);
        if (stripTrailingZeroes) resultValue = stripTrailingZeroes(resultValue);

        // Display formatted result
        valueStr = new StringBuilder(resultValue.toPlainString());
        formatValue();
        textvDisplay.setText(valueStr.toString());
        resultIsDisplayed = true;

        operation = OPERATION_NONE;
    }

    /**
     * Display error message and disable dialog's OK button, because there is no value
     * @param error ID of the error to show
     */
    private void setError(int error) {
        this.error = error;
        textvDisplay.setText(errorMessages[error]);

        reset();
    }

    /**
     * Dismiss error from display
     * @return true if an error was dismissed
     */
    private boolean dismissError() {
        if (error != ERROR_NONE) {
            error = ERROR_NONE;
            textvDisplay.setText(valueStr.toString());
            resultIsDisplayed = false;
            return true;
        }
        return false;
    }

    /**
     * Reset, dismiss error, clear the display
     */
    private void clear() {
        if (dismissError()) return;

        hideAnswerBtn();

        reset();

        textvDisplay.setText(valueStr.toString());
        resultIsDisplayed = false;
    }

    /**
     * Get a BigDecimal corresponding to the displayed value
     * Note that separators will be removed from display
     * @return BigDecimal value of display
     */
    private BigDecimal getCurrentValue() {
        if (currentIsAnswer) {
            return answerValue;

        } else if (valueStr.length() == 0) {
            return BigDecimal.ZERO;

        } else {
            removeGroupSeparators();
            int pointPos = valueStr.indexOf(String.valueOf(decimalSep));
            if (pointPos != -1) valueStr.replace(pointPos, pointPos + 1, ".");
            return new BigDecimal(valueStr.toString());
        }
    }

    /**
     * Checks if a BigDecimal exceeds maximum value
     * @param value value to check for
     * @return true if value is greater than maximum value
     *         maximum value is applied equally for positive and negative value
     */
    private boolean isValueOutOfBounds(@NonNull BigDecimal value) {
        return maxValue != null && (value.compareTo(maxValue) > 0 ||
                value.compareTo(maxValue.negate()) < 0);
    }

    /**
     * Add grouping separators and change decimal separator to custom one
     * If value is empty, set it to 0 if necessary
     */
    private void formatValue() {
        if (valueStr.length() == 0 && showZeroWhenNoValue) {
            valueStr.append(zeroString);
            overwriteValue = true;
            return;
        }

        // Replace "." by correct decimal separator
        int pointPos = valueStr.indexOf(".");
        if (pointPos != -1) {
            valueStr.setCharAt(pointPos, decimalSep);
        } else {
            pointPos = valueStr.indexOf(String.valueOf(decimalSep));
        }

        // Add group separators if needed
        if (groupSize > 0) {
            int start = (pointPos == -1 ? valueStr.length() : pointPos) - groupSize;
            for (int i = start; i > 0; i--) {
                if ((start - i) % groupSize == 0
                        && (i != 1 || valueStr.charAt(0) != '-')) {
                    valueStr.insert(i, groupSep);
                }
            }
        }
    }

    /**
     * Remove all grouping separators from display
     * 10,000,000 becomes 10000000
     * Used to format display to BigDecimal
     */
    private void removeGroupSeparators() {
        if (groupSize > 0) {
            for (int i = valueStr.length() - 1; i >= 0; i--) {
                if (valueStr.charAt(i) == groupSep) {
                    valueStr.deleteCharAt(i);
                }
            }
        }
    }

    private void hideAnswerBtn() {
        if (showAnswerBtn) {
            btnAnswer.setVisibility(View.GONE);
            currentIsAnswer = false;
        }
    }

    /**
     * Set initial value to show
     * By default, initial value is null. That means value is 0 but if
     * {@link #setShowZeroWhenNoValue(boolean)} is set to true, no value will be shown.
     * @param value Initial value to display. Setting null will result in 0
     * @return the dialog
     */
    public CalcDialog setValue(@Nullable BigDecimal value) {
        if (value != null && maxValue != null && isValueOutOfBounds(value)) {
            value = (value.compareTo(BigDecimal.ZERO) > 0 ? maxValue : maxValue.negate());
        }
        resultValue = value;
        return this;
    }

    /**
     * Set maximum value that can be calculated
     * If maximum value is exceeded, an "Out of bounds" error will be shown.
     * Maximum value is effective both for positive and negative values.
     * Default maximum is 10,000,000,000 (1e+10)
     * @param maxValue Maximum value, use null for no maximum
     * @return the dialog
     */
    public CalcDialog setMaxValue(@Nullable BigDecimal maxValue) {
        if (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) < 0) {
            // Must be positive
            maxValue = maxValue.negate();
        }
        this.maxValue = maxValue;

        if (resultValue != null && isValueOutOfBounds(resultValue)) {
            // Initial value is greater than max value
            resultValue = maxValue;
        }

        return this;
    }

    /**
     * Set max digits that can be entered on the calculator
     * Use {@link #MAX_DIGITS_UNLIMITED} for no limit
     * @param intPart Max digits for the integer part
     * @param fracPart Max digits for the fractional part.
     *                 A value of 0 means the value can't have a fractional part
     * @return the dialog
     */
    public CalcDialog setMaxDigits(int intPart, int fracPart) {
        if (intPart != MAX_DIGITS_UNLIMITED && intPart < 1 ||
                fracPart != MAX_DIGITS_UNLIMITED && fracPart < 0) {
            throw new IllegalArgumentException("Max integer part must be at least 1 and max fractional part must be at least 0.");
        }

        maxIntDigits = intPart;
        maxFracDigits = fracPart;

        return this;
    }

    /**
     * Set calculator's rounding mode
     * Default rounding mode is RoundingMode.HALF_UP
     * Ex: 5.5 = 6, 5.49 = 5, -5.5 = -6, 2.5 = 3
     * @param roundingMode one of {@link RoundingMode}, except {@link RoundingMode#UNNECESSARY}
     * @return the dialog
     */
    public CalcDialog setRoundingMode(RoundingMode roundingMode) {
        if (roundingMode.equals(RoundingMode.UNNECESSARY)) {
            throw new IllegalArgumentException("Cannot use RoundingMode.UNNECESSARY as a rounding mode.");
        }

        this.roundingMode = roundingMode;

        return this;
    }

    /**
     * Set whether to strip zeroes from the result or not
     * By default, zeroes are stripped.
     * Strip,  12.340000 = 12.34
     * No strip, 12.340000 = 12.340000
     * @param strip whether to strip them or not
     * @return the dialog
     */
    public CalcDialog setStripTrailingZeroes(boolean strip) {
        stripTrailingZeroes = strip;

        return this;
    }

    /**
     * Set whether sign can be changed or not
     * By default, sign can be changed
     * @param canBeChanged whether sign can be changed or not
     *                     if true, dialog can't be confirmed with a value of wrong sign
     *                     and an error will be shown
     * @param sign if canBeChanged is true, sign to force, -1 or 1
     *             otherwise use any value
     * @return the dialog
     */
    public CalcDialog setSignCanBeChanged(boolean canBeChanged, int sign) {
        signCanBeChanged = canBeChanged;
        if (!signCanBeChanged) {
            if (sign != -1 && sign != 1) {
                throw new IllegalArgumentException("Sign cannot be changed was set but no valid sign is given.");
            }
            initialSign = sign;
        }

        return this;
    }

    /**
     * Set symbols for formatting number
     * Use {@link #FORMAT_CHAR_DEFAULT} to use device locale's default symbol
     * By default, formatting will use locale's symbols
     * @param decimalSep decimal separator
     * @param groupSep grouping separator
     * @return the dialog
     */
    public CalcDialog setFormatSymbols(char decimalSep, char groupSep) {
        if (decimalSep != FORMAT_CHAR_DEFAULT && decimalSep == groupSep) {
            throw new IllegalArgumentException("Decimal separator cannot be the same as grouping separator.");
        }

        this.decimalSep = decimalSep;
        this.groupSep = groupSep;

        return this;
    }

    /**
     * Set whether to clear display when an operation button is pressed (+, -, * and /)
     * If not, display will be cleared on next button press
     * Default is not clearing
     * @param clear whether to clear it or not
     * @return the dialog
     */
    public CalcDialog setClearDisplayOnOperation(boolean clear) {
        clearOnOperation = clear;

        return this;
    }

    /**
     * Set whether zero should be displayed when no value has been entered or just display nothing
     * @param show whether to show it or not
     * @return the dialog
     */
    public CalcDialog setShowZeroWhenNoValue(boolean show) {
        showZeroWhenNoValue = show;

        return this;
    }

    /**
     * Set if the sign button should be hidden
     * @param hideSignButton true to hide the sign button, false otherwise (default)
     * @return the dialog
     */
    public CalcDialog setHideSignButton(boolean hideSignButton) {
        this.hideSignButton = hideSignButton;
        return this;
    }

    /**
     * Set the size of groups separated by group separators
     * 3 does 000,000,000
     * 4 does 0,0000,0000
     * Default size is 3
     * @param size grouping size, use 0 for no grouping
     * @return the dialog
     */
    public CalcDialog setGroupSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Group size must be positive");
        }
        groupSize = size;

        return this;
    }

    /**
     * Set whether to show the answer button when an operation button is clicked or not
     * This button allows the user to reuse previous answer.
     * By default, the answer button is not shown.
     * @param show whether to show it or not
     * @return the dialog
     */
    public CalcDialog setShowAnswerButton(boolean show) {
        showAnswerBtn = show;

        return this;
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Wrap calculator dialog's theme to context
        TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.calcDialogStyle});
        int style = ta.getResourceId(0, R.style.CalcDialogStyle);
        ta.recycle();
        this.context = new ContextThemeWrapper(context, style);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        context = null;
    }

    public interface CalcDialogCallback {
        /**
         * Called when the dialog's OK button is clicked
         * @param value value entered. If calculator didn't strip trailing zeroes, you can call
         *              {@link #stripTrailingZeroes(BigDecimal)} to strip them. To format the value
         *              to a String, use {@link BigDecimal#toPlainString()}.
         *              To format the value to a currency String you could do:
         *              {@code NumberFormat.getCurrencyInstance(Locale).format(BigDecimal)}
         */
        void onValueEntered(BigDecimal value);
    }


    //// UTILITY METHODS ////
    /**
     * Get device's default locale
     * @param context any context
     * @return the default locale
     */
    public static Locale getDefaultLocale(Context context) {
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
    public static BigDecimal stripTrailingZeroes(@NonNull BigDecimal from) {
        if (from.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else {
            return from.stripTrailingZeros();
        }
    }

}

