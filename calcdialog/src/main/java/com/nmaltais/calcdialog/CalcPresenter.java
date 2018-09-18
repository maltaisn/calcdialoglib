package com.nmaltais.calcdialog;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CalcPresenter {

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

    private CalcDialog view;

    private CalcSettings settings;

    private int operation;
    private int error;
    private StringBuilder valueStr;
    private @Nullable BigDecimal resultValue;
    private boolean resultIsDisplayed;
    private boolean overwriteValue;

    private @Nullable BigDecimal answerValue;
    private boolean currentIsAnswer;

    private String zeroString;

    void attach(CalcDialog v, Bundle state) {
        view = v;

        settings = view.getSettings();

        // Get locale's symbols for number formatting
        Locale locale = view.getDefaultLocale();
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(locale);
        if (settings.decimalSep == CalcDialog.FORMAT_CHAR_DEFAULT) {
            settings.decimalSep = dfs.getDecimalSeparator();
        }
        if (settings.groupSep == CalcDialog.FORMAT_CHAR_DEFAULT) {
            settings.groupSep = dfs.getGroupingSeparator();
        }

        // Get string to display for zero
        zeroString = BigDecimal.ZERO.toPlainString();
        int pointPos = zeroString.indexOf('.');
        if (pointPos != -1 && settings.decimalSep != '.') {
            // Replace "." with correct decimal separator
            StringBuilder sb = new StringBuilder(zeroString);
            sb.setCharAt(pointPos, settings.decimalSep);
            zeroString = sb.toString();
        }

        if (state == null) {
            answerValue = null;
            currentIsAnswer = false;
            operation = OPERATION_NONE;
            error = ERROR_NONE;
            resultValue = null;
            resultIsDisplayed = true;

            // Init values
            if (settings.initialValue != null) {
                resultValue = settings.initialValue.setScale(settings.maxFracDigits, settings.roundingMode);
                resultValue = CalcDialogUtils.stripTrailingZeroes(resultValue);
                answerValue = resultValue;

                valueStr = new StringBuilder(resultValue.toPlainString());

            } else {
                valueStr = new StringBuilder();
            }
            formatValue();

            view.displayValueText(valueStr.toString());

        } else {
            readStateFromBundle(state);
        }

        view.setDecimalSepBtnEnabled(settings.maxFracDigits > 0);
        setAnswerBtnVisible(settings.showAnswerBtn && answerValue != null);
        view.setSignBtnVisible(settings.showSignBtn);
    }

    void detach() {
        view = null;
    }

    void writeStateToBundle(Bundle bundle) {
        bundle.putInt("operation", operation);
        bundle.putInt("error", error);
        bundle.putString("valueStr", valueStr.toString());
        bundle.putBoolean("resultIsDisplayed", resultIsDisplayed);
        bundle.putBoolean("overwriteValue", overwriteValue);
        bundle.putBoolean("currentIsAnswer", currentIsAnswer);

        if (resultValue != null) {
            bundle.putString("resultValue", resultValue.toString());
        }
        if (answerValue != null) {
            bundle.putString("answerValue", answerValue.toString());
        }
    }

    private void readStateFromBundle(Bundle bundle) {
        operation = bundle.getInt("operation");
        error = bundle.getInt("error");
        valueStr = new StringBuilder(bundle.getString("valueStr"));
        resultIsDisplayed = bundle.getBoolean("resultIsDisplayed");
        overwriteValue = bundle.getBoolean("overwriteValue");
        currentIsAnswer = bundle.getBoolean("currentIsAnswer");

        if (bundle.containsKey("resultValue")) {
            resultValue = new BigDecimal(bundle.getString("resultValue"));
        }
        if (bundle.containsKey("answerValue")) {
            answerValue = new BigDecimal(bundle.getString("answerValue"));
        }
    }

    void onErasedOnce() {
        if (dismissError()) return;

        currentIsAnswer = false;
        setAnswerBtnVisible(false);

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
                    if (last == settings.decimalSep || last == '-') {
                        valueStr.deleteCharAt(valueStr.length() - 1);

                        if (valueStr.toString().equals("-0")) {
                            // Don't allow negative 0
                            valueStr.deleteCharAt(0);
                        }
                    }
                }
            }

            formatValue();
            view.displayValueText(valueStr.toString());
            resultIsDisplayed = false;
        }
    }

    void onErasedAll() {
        clear();
    }

    void onDigitBtnClicked(int digit) {
        dismissError();

        currentIsAnswer = false;
        setAnswerBtnVisible(false);

        if (resultIsDisplayed || overwriteValue) {
            valueStr.setLength(0);
            overwriteValue = false;
        }

        removeGroupSeparators();

        // Check if max digits has been exceeded
        int pointPos = valueStr.indexOf(String.valueOf(settings.decimalSep));
        boolean withinMaxInt = (pointPos == -1 && (settings.maxIntDigits == CalcDialog.MAX_DIGITS_UNLIMITED
                || valueStr.length() < settings.maxIntDigits));
        boolean withinMaxFrac = (pointPos != -1
                && (settings.maxFracDigits == CalcDialog.MAX_DIGITS_UNLIMITED
                || valueStr.length() - pointPos - 1 < settings.maxFracDigits));
        boolean isValueZero = (pointPos == -1 && valueStr.length() == 1 && valueStr.charAt(0) == '0');

        if ((withinMaxInt || withinMaxFrac)
                && (!isValueZero || !settings.preventLeadingZeroes || digit != 0))  {
            // If max int or max frac digits have not already been reached
            // Concatenate current value with new digit
            if (isValueZero && digit != 0) {
                // If current value is zero, clear it before adding new digit
                valueStr.setLength(0);
            }
            valueStr.append(digit);
        }

        formatValue();
        view.displayValueText(valueStr.toString());
        resultIsDisplayed = false;
    }

    void onOperatorBtnClicked(int op) {
        if (dismissError()) return;

        if (valueStr.length() != 0 || currentIsAnswer) {
            if (operation != OPERATION_NONE) {
                calculate();
            } else {
                resultValue = getCurrentValue();
                if (!settings.clearOnOperation) {
                    valueStr = new StringBuilder();
                    formatValue();
                    resultIsDisplayed = false;
                }
            }
        }

        operation = op;
        if (settings.clearOnOperation) {
            valueStr = new StringBuilder();
            formatValue();
            view.displayValueText(valueStr.toString());
            resultIsDisplayed = false;
        }

        setAnswerBtnVisible(settings.showAnswerBtn && answerValue != null);
    }

    void onDecimalSepBtnClicked() {
        dismissError();

        currentIsAnswer = false;
        setAnswerBtnVisible(false);

        if (resultIsDisplayed || overwriteValue) {
            valueStr.setLength(0);
            overwriteValue = false;
        }

        if (valueStr.indexOf(String.valueOf(settings.decimalSep)) == -1) {
            // Only insert a decimal point if there isn't one yet
            if (valueStr.length() == 0) {
                // Add 0 before decimal point .1 --> 0.1
                valueStr.append("0");
            }

            valueStr.append(settings.decimalSep);

            view.displayValueText(valueStr.toString());
            resultIsDisplayed = false;
        }
    }

    void onSignBtnClicked() {
        if (dismissError()) return;

        // Negate value
        String str = valueStr.toString();
        if (!str.isEmpty() && !str.equals("0") && !str.equals("0" + settings.decimalSep)) {
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

            view.displayValueText(valueStr.toString());
            resultIsDisplayed = false;
        }
    }

    void onEqualBtnClicked() {
        if (dismissError()) return;
        calculate();

        answerValue = resultValue;
    }

    void onAnswerBtnClicked() {
        setAnswerBtnVisible(false);
        view.displayAnswerText();

        currentIsAnswer = true;
        resultIsDisplayed = false;
    }

    void onClearBtnClicked() {
        clear();
    }

    void onCancelBtnClicked() {
        view.exit();
    }

    void onOkBtnClicked() {
        if (dismissError()) return;

        int opBefore = operation;

        calculate();

        // If operation was not done, make OK button act as an equal btn
        if (opBefore != OPERATION_NONE) return;

        if (error == ERROR_NONE) {
            // If sign can't be changed, check if sign is right
            if (!settings.signCanBeChanged && resultValue != null) {
                int sign = resultValue.signum();
                if (sign != 0 && sign != settings.initialSign) {
                    // Wrong sign
                    setError(sign == 1 ? ERROR_WRONG_SIGN_NEG : ERROR_WRONG_SIGN_POS);
                    return;
                }
            }

            view.sendValueResult(resultValue);
            view.exit();
        }
    }

    void onDismissed() {
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
        currentIsAnswer = false;
        setAnswerBtnVisible(false);

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
                    resultValue = resultValue.divide(operand, settings.maxFracDigits, settings.roundingMode);
                }
            }
        }

        if (CalcDialogUtils.isValueOutOfBounds(resultValue, settings.maxValue)) {
            setError(ERROR_OUT_OF_BOUNDS);
            return;
        }

        resultValue = resultValue.setScale(settings.maxFracDigits, settings.roundingMode);
        resultValue = CalcDialogUtils.stripTrailingZeroes(resultValue);

        // Display formatted result
        valueStr = new StringBuilder(resultValue.toPlainString());
        formatValue();
        view.displayValueText(valueStr.toString());
        resultIsDisplayed = true;

        operation = OPERATION_NONE;
    }

    /**
     * Display error message and disable dialog's OK button, because there is no value
     * @param error ID of the error to show
     */
    private void setError(int error) {
        this.error = error;
        view.displayErrorText(error);

        reset();
    }

    /**
     * Dismiss error from display
     * @return true if an error was dismissed
     */
    private boolean dismissError() {
        if (error != ERROR_NONE) {
            error = ERROR_NONE;
            view.displayValueText(valueStr.toString());
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

        currentIsAnswer = false;
        setAnswerBtnVisible(false);

        reset();

        view.displayValueText(valueStr.toString());
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
            int pointPos = valueStr.indexOf(String.valueOf(settings.decimalSep));
            if (pointPos != -1) valueStr.replace(pointPos, pointPos + 1, ".");
            return new BigDecimal(valueStr.toString());
        }
    }


    /**
     * Add grouping separators and change decimal separator to custom one
     * If value is empty, set it to 0 if necessary
     */
    private void formatValue() {
        if (valueStr.length() == 0 && settings.showZeroWhenNoValue) {
            valueStr.append(zeroString);
            overwriteValue = true;
            return;
        }

        // Replace "." by correct decimal separator
        int pointPos = valueStr.indexOf(".");
        if (pointPos != -1) {
            valueStr.setCharAt(pointPos, settings.decimalSep);
        } else {
            pointPos = valueStr.indexOf(String.valueOf(settings.decimalSep));
        }

        // Add group separators if needed
        if (settings.groupSize > 0) {
            int start = (pointPos == -1 ? valueStr.length() : pointPos) - settings.groupSize;
            for (int i = start; i > 0; i--) {
                if ((start - i) % settings.groupSize == 0
                        && (i != 1 || valueStr.charAt(0) != '-')) {
                    valueStr.insert(i, settings.groupSep);
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
        if (settings.groupSize > 0) {
            for (int i = valueStr.length() - 1; i >= 0; i--) {
                if (valueStr.charAt(i) == settings.groupSep) {
                    valueStr.deleteCharAt(i);
                }
            }
        }
    }

    private void setAnswerBtnVisible(boolean visible) {
        view.setAnswerBtnVisible(visible);
        view.setEqualBtnVisible(!visible);
    }

}
