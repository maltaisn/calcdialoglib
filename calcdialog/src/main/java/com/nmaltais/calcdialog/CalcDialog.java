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


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;


/**
 * Dialog with calculator for entering and calculating a number
 */
@SuppressWarnings("unused")
public class CalcDialog extends AppCompatDialogFragment {

    private static final String TAG = CalcDialog.class.getSimpleName();

    /**
     * Parameter value to set for {@link #setMaxDigits(int, int)}
     * to have to limit on the number of digits for a part of the number (int or frac).
     */
    public static final int MAX_DIGITS_UNLIMITED = -1;

    /**
     * Parameter to set for {@link #setFormatSymbols(char, char)}
     * to use default locale's format symbol.
     * This is the default value for both decimal and group separators
     */
    public static final char FORMAT_CHAR_DEFAULT = 0;

    private static final int[] DIGIT_BTN_IDS = {
            R.id.calc_btn_0,
            R.id.calc_btn_1,
            R.id.calc_btn_2,
            R.id.calc_btn_3,
            R.id.calc_btn_4,
            R.id.calc_btn_5,
            R.id.calc_btn_6,
            R.id.calc_btn_7,
            R.id.calc_btn_8,
            R.id.calc_btn_9,
    };

    private static final int[] OPERATOR_BTN_IDS = {
            R.id.calc_btn_add,
            R.id.calc_btn_sub,
            R.id.calc_btn_mult,
            R.id.calc_btn_div,
    };

    private Context context;
    private CalcPresenter presenter;

    private CalcSettings settings;

    private TextView displayTxv;
    private TextView decimalSepBtn;
    private TextView equalBtn;
    private TextView answerBtn;
    private TextView signBtn;

    private CharSequence[] btnTexts;
    private CharSequence[] errorMessages;
    private int[] maxDialogDimensions;

    /**
     * Do not use the constructor directly for creating
     * an instance, use {@link #newInstance(int)} instead
     */
    public CalcDialog() {
        settings = new CalcSettings();
    }

    /**
     * Create a new instance of CalcDialog
     * @param requestCode request code used by callback
     *                    Useful in case there's multiple dialogs at the same time
     * @return the dialog
     */
    public static CalcDialog newInstance(int requestCode) {
        CalcDialog dialog = new CalcDialog();
        dialog.settings.requestCode = requestCode;
        return dialog;
    }

    ////////// LIFECYCLE METHODS //////////
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
    public void onCreate(Bundle state) {
        super.onCreate(state);

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

    @SuppressLint("InflateParams")
    @Override
    public @NonNull Dialog onCreateDialog(final Bundle state) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_calc, null);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            LinearLayout header = view.findViewById(R.id.calc_layout_header);
            header.setBackgroundResource(R.drawable.calc_bg_elevation);
        }

        // Value display
        displayTxv = view.findViewById(R.id.calc_txv_value);

        // Erase button
        CalcEraseButton eraseBtn = view.findViewById(R.id.calc_btn_erase);
        eraseBtn.setOnEraseListener(new CalcEraseButton.EraseListener() {
            @Override
            public void onErase() {
                presenter.onErasedOnce();
            }

            @Override
            public void onEraseAll() {
                presenter.onErasedAll();
            }
        });

        // Digit buttons
        for (int i = 0; i < 10; i++) {
            TextView digitBtn = view.findViewById(DIGIT_BTN_IDS[i]);
            digitBtn.setText(btnTexts[i]);

            final int digit = i;
            digitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onDigitBtnClicked(digit);
                }
            });
        }

        // Operator buttons
        for (int i = 0; i < 4; i++) {
            final int op = i;
            TextView operatorBtn = view.findViewById(OPERATOR_BTN_IDS[i]);
            operatorBtn.setText(btnTexts[i + 10]);
            operatorBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onOperatorBtnClicked(op);
                }
            });
        }

        // Decimal separator button
        decimalSepBtn = view.findViewById(R.id.calc_btn_decimal);
        decimalSepBtn.setText(btnTexts[15]);
        decimalSepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onDecimalSepBtnClicked();
            }
        });

        // Sign button: +/-
        signBtn = view.findViewById(R.id.calc_btn_sign);
        signBtn.setText(btnTexts[14]);
        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onSignBtnClicked();
            }
        });

        // Equal button
        equalBtn = view.findViewById(R.id.calc_btn_equal);
        equalBtn.setText(btnTexts[16]);
        equalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onEqualBtnClicked();
            }
        });

        // Answer button
        answerBtn = view.findViewById(R.id.calc_btn_answer);
        answerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onAnswerBtnClicked();
            }
        });

        // Dialog buttons
        Button clearBtn = view.findViewById(R.id.calc_btn_clear);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onClearBtnClicked();
            }
        });

        Button cancelBtn = view.findViewById(R.id.calc_btn_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onCancelBtnClicked();
            }
        });

        Button okBtn = view.findViewById(R.id.calc_btn_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onOkBtnClicked();
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

                // Presenter
                presenter = new CalcPresenter();
                presenter.attach(CalcDialog.this, state);
            }
        });

        if (state != null) {
            settings.readFromBundle(state);
            displayTxv.setText(state.getString("displayText"));
        }

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (presenter != null) {
            // On config change, presenter is detached before this is called
            presenter.onDismissed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        presenter.writeStateToBundle(state);
        settings.writeToBundle(state);

        state.putString("displayText", displayTxv.getText().toString());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.detach();

        presenter = null;
        context = null;
    }

    @Nullable
    private CalcDialogCallback getCallback() {
        CalcDialogCallback cb = null;
        if (getParentFragment() != null) {
            try {
                cb = (CalcDialogCallback) getParentFragment();
            } catch (Exception e) {
                // Interface callback is not implemented in fragment
            }
        } else if (getTargetFragment() != null) {
            try {
                cb = (CalcDialogCallback) getTargetFragment();
            } catch (Exception e) {
                // Interface callback is not implemented in fragment
            }
        } else {
            // Caller was an activity
            try {
                cb = (CalcDialog.CalcDialogCallback) requireActivity();
            } catch (Exception e) {
                // Interface callback is not implemented in activity
            }
        }
        return cb;
    }

    ////////// VIEW METHODS //////////
    CalcSettings getSettings() {
        return settings;
    }

    Locale getDefaultLocale() {
        return CalcDialogUtils.getDefaultLocale(context);
    }

    void exit() {
        dismissAllowingStateLoss();
    }

    void sendValueResult(BigDecimal value) {
        CalcDialogCallback cb = getCallback();
        if (cb != null) {
            cb.onValueEntered(settings.requestCode, value);
        }
    }

    void setAnswerBtnVisible(boolean visible) {
        answerBtn.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    void setEqualBtnVisible(boolean visible) {
        equalBtn.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    void setSignBtnVisible(boolean visible) {
        signBtn.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    void setDecimalSepBtnEnabled(boolean enabled) {
        decimalSepBtn.setEnabled(enabled);
    }

    void displayValueText(String text) {
        displayTxv.setText(text);
    }

    void displayErrorText(int error) {
        displayTxv.setText(errorMessages[error]);
    }

    void displayAnswerText() {
        displayTxv.setText(R.string.calc_answer);
    }

    ////////// CALCULATOR SETTINGS //////////
    /**
     * Set initial value to show
     * By default, initial value is null. That means value is 0 but if
     * {@link #setShowZeroWhenNoValue(boolean)} is set to true, no value will be shown.
     * @param value Initial value to display. Setting null will result in 0
     * @return the dialog
     */
    public CalcDialog setValue(@Nullable BigDecimal value) {
        settings.setValue(value);
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
        settings.setMaxValue(maxValue);
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
        settings.setMaxDigits(intPart, fracPart);
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
        settings.setRoundingMode(roundingMode);
        return this;
    }

    /**
     * Set whether to prevent leading zeroes from showing or not.
     * By default, leading zeroes are blocked.
     * blocked,  00012.34 will never be displayed
     * Not blocked, 00012.34 can be typed
     * @param block whether to block them or not
     * @return the dialog
     */
    public CalcDialog setPreventLeadingZeroes(boolean block) {
        settings.preventLeadingZeroes = block;
        return this;
    }

    /**
     * Set whether to strip trailing zeroes from the result or not.
     * By default, zeroes are stripped.
     * Strip,  12.340000 = 12.34
     * No strip, 12.340000 = 12.340000
     * @param strip whether to strip them or not
     * @return the dialog
     */
    public CalcDialog setStripTrailingZeroes(boolean strip) {
        settings.stripTrailingZeroes = strip;
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
        settings.setSignCanBeChanged(canBeChanged, sign);
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
        settings.setFormatSymbols(decimalSep, groupSep);
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
        settings.clearOnOperation = clear;
        return this;
    }

    /**
     * Set whether zero should be displayed when no value has been entered or just display nothing
     * @param show whether to show it or not
     * @return the dialog
     */
    public CalcDialog setShowZeroWhenNoValue(boolean show) {
        settings.showZeroWhenNoValue = show;
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
        settings.setGroupSize(size);
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
        settings.showAnswerBtn = show;
        return this;
    }

    /**
     * Set whether the sign button should be shown.
     * By default, the sign button is shown.
     * @param show whether to show it or not
     * @return the dialog
     */
    public CalcDialog setShowSignButton(boolean show) {
        settings.showSignBtn = show;
        return this;
    }

    public interface CalcDialogCallback {
        /**
         * Called when the dialog's OK button is clicked
         * @param value value entered. If calculator didn't strip trailing zeroes, you can call
         *              {@link CalcDialogUtils#stripTrailingZeroes(BigDecimal)} to strip them.
         *              To format the value to a String, use {@link BigDecimal#toPlainString()}.
         *              To format the value to a currency String you could do:
         *              {@code NumberFormat.getCurrencyInstance(Locale).format(BigDecimal)}
         * @param requestCode dialog request code given when dialog
         *                    was created with {@link #newInstance(int)}
         */
        void onValueEntered(int requestCode, BigDecimal value);
    }

}

