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
import android.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 * Dialog with calculator for entering and calculating a number
 */
public class CalcDialog extends DialogFragment {

    private static final String TAG = CalcDialog.class.getSimpleName();

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

    public static final int MAX_DIGITS_UNLIMITED = -1;
    private int maxIntDigits;
    private int maxFracDigits;

    private int roundingMode;

    private boolean stripTrailingZeroes;

    private boolean signCanBeChanged;
    private int initialSign;

    private boolean clearOnOperation;
    private boolean showZeroWhenNoValue;

    public static final char FORMAT_CHAR_DEFAULT = 0;
    public static final char FORMAT_CHAR_NONE = 1;
    private char decimalSep;
    private char groupSep;

    private int groupingSize;

    private StringBuilder result;
    private StringBuilder display;
    private @Nullable BigDecimal resultValue;
    private int operation;

    private TextView textvValue;
    private Button okBtn;

    private CharSequence[] btnTexts;
    private CharSequence[] errorMessages;
    private int[] maxDialogDimensions;

    private String zeroString;

    public CalcDialog() {
        // Set default settings
        maxValue = new BigDecimal("1E10");
        maxIntDigits = 10;
        maxFracDigits = 8;
        roundingMode = BigDecimal.ROUND_HALF_UP;
        stripTrailingZeroes = true;

        decimalSep = FORMAT_CHAR_DEFAULT;
        groupSep = FORMAT_CHAR_DEFAULT;
        groupingSize = 3;

        clearOnOperation = false;
        showZeroWhenNoValue = true;

        reset();
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

        // Init value
        if (resultValue != null) {
            resultValue = resultValue.setScale(maxFracDigits, roundingMode);
            if (stripTrailingZeroes) resultValue = stripTrailingZeroes(resultValue);
            result = new StringBuilder(resultValue.toPlainString());
            display = result;
            addGroupSeparators();
        }

        BigDecimal zero = BigDecimal.ZERO;
        if (!stripTrailingZeroes) zero = zero.setScale(maxFracDigits, roundingMode);
        zeroString = zero.toPlainString();

        // Get strings
        TypedArray ta = context.obtainStyledAttributes(R.styleable.CalcDialog);
        btnTexts = ta.getTextArray(R.styleable.CalcDialog_calcButtonTexts);
        errorMessages = ta.getTextArray(R.styleable.CalcDialog_calcErrors);
        maxDialogDimensions = new int[]{
                ta.getDimensionPixelSize(R.styleable.CalcDialog_calcDialogMaxWidth, -1),
                ta.getDimensionPixelSize(R.styleable.CalcDialog_calcDialogMaxHeight, -1)
        };
        ta.recycle();
    }

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle state) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_calc, null);

        // Value display
        textvValue = view.findViewById(R.id.text_value);
        updateDisplay();

        // Erase button
        EraseButton eraseBtn = view.findViewById(R.id.button_calc_erase);
        eraseBtn.setOnEraseListener(new EraseButton.EraseListener() {
            @Override
            public void onErase() {
                okBtn.setEnabled(true);

                if (display.length() > 0) {
                    removeGroupSeparators();

                    // Erase last digit
                    display.deleteCharAt(display.length() - 1);
                    if (display.length() > 0) {
                        // Don't leave useless negative sign or decimal separator
                        char last = display.charAt(display.length() - 1);
                        if (last == decimalSep || last == '-') {
                            display.deleteCharAt(display.length() - 1);
                        }
                    }

                    addGroupSeparators();
                }

                updateDisplay();
            }
        });

        // Digits button: 0-9
        int[] numberBtnIds = {
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
        for (int i = 0; i < 10; i++) {
            TextView numberBtn = view.findViewById(numberBtnIds[i]);
            numberBtn.setText(btnTexts[i]);

            final int nb = i;
            numberBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    okBtn.setEnabled(true);

                    // Remove trailing zero if needed
                    if (display.length() == 1 && display.charAt(0) == '0') {
                        display.deleteCharAt(0);
                    }

                    removeGroupSeparators();

                    // Check if max digits has been exceeded
                    int pointPos = display.indexOf(String.valueOf(decimalSep));
                    if (pointPos == -1 && maxIntDigits != MAX_DIGITS_UNLIMITED && display.length() >= maxIntDigits ||
                            pointPos != -1 && maxFracDigits != MAX_DIGITS_UNLIMITED && display.length() - pointPos - 1 >= maxFracDigits) {
                        addGroupSeparators();
                        return;
                    }

                    // Concatenate current value with new digit
                    display.append(nb);

                    addGroupSeparators();

                    updateDisplay();
                }
            });
        }

        // Operator button: +, -, *, /
        int[] operatorBtnIds = {
                R.id.button_calc_add,
                R.id.button_calc_sub,
                R.id.button_calc_mult,
                R.id.button_calc_div,
        };
        for (int i = 0; i < 4; i++) {
            final int op = i;
            TextView operatorBtn = view.findViewById(operatorBtnIds[i]);
            operatorBtn.setText(btnTexts[i + 10]);
            operatorBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    okBtn.setEnabled(true);

                    if (display.length() != 0) {
                        if (operation != OPERATION_NONE) {
                            equal();
                        } else {
                            removeGroupSeparators();
                            resultValue = new BigDecimal(display.toString());
                        }
                    }
                    operation = op;

                    display = new StringBuilder();
                    if (clearOnOperation) updateDisplay();
                }
            });
        }

        // Decimal separator button
        final TextView decimalBtn = view.findViewById(R.id.button_calc_decimal);
        decimalBtn.setText(btnTexts[15]);
        decimalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (display.indexOf(String.valueOf(decimalSep)) == -1) {
                    okBtn.setEnabled(true);

                    // Only insert a decimal point if there isn't one yet
                    if (display.length() == 0) {
                        // Add 0 before decimal point .1 --> 0.1
                        display.append("0");
                    }

                    display.append(decimalSep);

                    updateDisplay();
                }
            }
        });

        // Sign button: +/-
        TextView signBtn = view.findViewById(R.id.button_calc_sign);
        signBtn.setText(btnTexts[14]);
        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okBtn.setEnabled(true);

                // Add or remove the negative sign depending or whether there is already one or not
                String str = display.toString();
                if (!str.isEmpty() && !str.equals("0") && !str.equals("0" + decimalSep)) {
                    // Can't negate "0"
                    if (display.charAt(0) != '-') {
                        display.insert(0, '-');
                    } else {
                        display.deleteCharAt(0);
                    }
                    updateDisplay();
                }
            }
        });

        // Equal button
        TextView equalBtn = view.findViewById(R.id.button_calc_equal);
        equalBtn.setText(btnTexts[16]);
        equalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okBtn.setEnabled(true);
                equal();
            }
        });

        // Dialog buttons
        Button clearBtn = view.findViewById(R.id.button_dialog_clear);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
                updateDisplay();
            }
        });

        Button cancelBtn = view.findViewById(R.id.button_dialog_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        okBtn = view.findViewById(R.id.button_dialog_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int error = (operation == OPERATION_NONE ? equal() : ERROR_NONE);
                if (error == ERROR_NONE) {
                    // If sign can't be change, check if sign is right
                    if (!signCanBeChanged && resultValue != null) {
                        int sign = resultValue.signum();
                        if (sign != 0 && sign != initialSign) {
                            setError(sign == 1 ? ERROR_WRONG_SIGN_POS : ERROR_WRONG_SIGN_NEG);
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
                            ((CalcDialogCallback) getActivity()).onValueEntered(resultValue);
                        } catch (Exception e) {
                            // Interface callback is not implemented in activity
                        }
                    }
                    dismiss();
                }
            }
        });

        // Set up dialog
        final Dialog dialog = new Dialog(context);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
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

    private void reset() {
        operation = OPERATION_NONE;
        result = new StringBuilder();
        resultValue = null;
        display = result;
    }

    /**
     * Calculate result of operation between current result and operand
     * @return error that occurred or ERROR_NONE if none
     */
    private int calculate() {
        removeGroupSeparators();

        if (operation == OPERATION_NONE || display.length() == 0) {
            display = result;
            if (display.length() == 0) {
                resultValue = BigDecimal.ZERO;
            } else {
                resultValue = new BigDecimal(display.toString());
            }

        } else {
            if (resultValue == null) resultValue = BigDecimal.ZERO;
            BigDecimal operand = new BigDecimal(display.toString());

            if (operation == OPERATION_ADD) {
                resultValue = resultValue.add(operand);
            } else if (operation == OPERATION_SUB) {
                resultValue = resultValue.subtract(operand);
            } else if (operation == OPERATION_MULT) {
                resultValue = resultValue.multiply(operand);
            } else if (operation == OPERATION_DIV) {
                if (operand.equals(BigDecimal.ZERO)) {
                    return ERROR_DIV_ZERO;
                } else {
                    resultValue = resultValue.divide(operand, maxFracDigits, roundingMode);
                }
            }
        }

        if (isValueOutOfBounds(resultValue)) {
            return ERROR_OUT_OF_BOUNDS;
        }

        resultValue = resultValue.setScale(maxFracDigits, roundingMode);
        if (stripTrailingZeroes) resultValue = stripTrailingZeroes(resultValue);
        result = new StringBuilder(resultValue.toPlainString());
        display = result;

        addGroupSeparators();

        operation = OPERATION_NONE;
        return ERROR_NONE;
    }

    /**
     * Calculate result of operation and display error if there is one
     * @return error ID or ERROR_NONE if there is no error
     */
    private int equal() {
        int error = calculate();
        if (error == ERROR_NONE) {
            updateDisplay();
        } else {
            setError(error);
        }
        return error;
    }

    private void setError(int error) {
        textvValue.setText(errorMessages[error]);
        okBtn.setEnabled(false);

        reset();
    }

    private boolean isValueOutOfBounds(@NonNull BigDecimal value) {
        return maxValue != null && (value.compareTo(maxValue) == 1 ||
                value.compareTo(maxValue.negate()) == -1);
    }

    private void addGroupSeparators() {
        if (groupingSize > 0 && groupSep != FORMAT_CHAR_NONE) {
            int pointPos = display.indexOf(String.valueOf(decimalSep));
            int start = (pointPos == -1 ? display.length() : pointPos) - groupingSize;
            for (int i = start; i > 0; i--) {
                if ((start - i) % groupingSize == 0
                        && (i != 1 || display.charAt(0) != '-')) {
                    display.insert(i, groupSep);
                }
            }
        }
    }

    private void removeGroupSeparators() {
        if (groupSep != FORMAT_CHAR_NONE) {
            for (int i = display.length() - 1; i >= 0; i--) {
                if (display.charAt(i) == groupSep) {
                    display.deleteCharAt(i);
                }
            }
        }
    }

    private static BigDecimal stripTrailingZeroes(@NonNull BigDecimal from) {
        // Fixes bug: http://hg.openjdk.java.net/jdk8/jdk8/jdk/rev/2ee772cda1d6
        // BigDecimal.stripTrailingZeroes() doesn't work for zero.
        if (from.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else {
            return from.stripTrailingZeros();
        }
    }

    private void updateDisplay() {
        textvValue.setText(display.length() == 0 && showZeroWhenNoValue ? zeroString : display.toString());
    }

    /**
     * Set initial value to show
     * By default, initial value is null. That means value is 0 but if
     * {@link #setShowZeroWhenNoValue(boolean)} is set to true, no value will be shown.
     * @param value Initial value to display. Setting null will result in 0
     */
    public CalcDialog setValue(@Nullable BigDecimal value) {
        if (value != null && maxValue != null && isValueOutOfBounds(value)) {
            value = (value.compareTo(BigDecimal.ZERO) == 1 ? maxValue : maxValue.negate());
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
     */
    public CalcDialog setMaxValue(@Nullable BigDecimal maxValue) {
        if (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) == -1) {
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
     * Default rounding mode is BigDecimal.ROUND_HALF_UP
     * Ex: 5.5 = 6, 5.49 = 5, -5.5 = -6, 2.5 = 3
     * @param roundingMode one of BigDecimal.ROUND_*
     */
    public CalcDialog setRoundingMode(int roundingMode) {
        this.roundingMode = roundingMode;

        return this;
    }

    /**
     * Set whether to strip zeroes from the result or not
     * By default, zeroes are stripped.
     * Strip,  12.340000 = 12.34
     * No strip, 12.340000 = 12.340000
     * @param strip whether to strip them or not
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
     * Set character for formatting number
     * Use {@link #FORMAT_CHAR_DEFAULT} to use device locale's default character
     * By default, formatting will use locale's characters
     * @param decimalSep decimal separator
     * @param groupSep grouping separator, use {@link #FORMAT_CHAR_NONE} for no grouping
     */
    public CalcDialog setFormatChars(char decimalSep, char groupSep) {
        if (decimalSep == FORMAT_CHAR_NONE) {
            throw new IllegalArgumentException("There cannot be no decimal separator character.");
        } else if (decimalSep != FORMAT_CHAR_DEFAULT && decimalSep == groupSep) {
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
     */
    public CalcDialog setClearDisplayOnOperation(boolean clear) {
        clearOnOperation = clear;

        return this;
    }

    /**
     * Set whether zero should be displayed when no value has been entered or just display nothing
     * @param show whether to show it or not
     */
    public CalcDialog setShowZeroWhenNoValue(boolean show) {
        showZeroWhenNoValue = show;

        if (!showZeroWhenNoValue) {
            result = new StringBuilder();
            display = result;
        }

        return this;
    }

    /**
     * Set the size of groups separated by group separators
     * 3 does 000,000,000
     * 4 does 0,0000,0000
     * Default size is 3
     * @param groupingSize grouping size, use 0 for no grouping
     */
    public CalcDialog setGroupingSize(int groupingSize) {
        if (groupingSize < 0) {
            throw new IllegalArgumentException("Grouping size must be positive");
        }
        this.groupingSize = groupingSize;

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
         * Called when the dialog's positive button is clicked
         * @param value value entered
         */
        void onValueEntered(BigDecimal value);
    }

}

