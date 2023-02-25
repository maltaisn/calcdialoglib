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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class CalcPresenter {

    private static final int ERROR_NONE = -1;
    private static final int ERROR_DIV_ZERO = 0;
    private static final int ERROR_OUT_OF_BOUNDS = 1;
    private static final int ERROR_WRONG_SIGN_POS = 2;
    private static final int ERROR_WRONG_SIGN_NEG = 3;

    private CalcDialog view;
    private CalcSettings settings;
    private NumberFormat nbFormat;

    /** The typed expression. */
    @NonNull
    private Expression expression = new Expression();

    /**
     * The current displayed value. Can be user input, evaluated result, answer from the answer
     * button, or can be null if there's no current value.
     */
    @Nullable
    private BigDecimal currentValue;

    /** The last evaluated result, null for none. */
    @Nullable
    private BigDecimal resultValue;

    /**
     * The number of digits shown after the decimal separator in {@link #currentValue}.
     * If -1, there's no fractional part. If 0, only the decimal separator is shown.
     * If more than 0, indicates the number of fraction digits.
     */
    private int currentValueScale;

    /** If there's an error, the error code. */
    private int errorCode;

    /** Whether {@link #currentValue} is from the answer button. */
    private boolean currentIsAnswer;

    /** Whether {@link #currentValue} is the result from the equal button. */
    private boolean currentIsResult;

    /**
     * Whether user can edit {@link #currentValue} or not.
     * If not editable, a button press clears the current value,
     * with the exception of the sign button which will still negate the value.
     */
    private boolean canEditCurrentValue;

    /**
     * Whether user can edit expression or not.
     * If not editable, a button press clears the expression.
     */
    private boolean canEditExpression;


    void attach(CalcDialog v, Bundle state) {
        view = v;

        settings = view.getSettings();
        settings.validate();

        nbFormat = settings.nbFormat;

        if (state == null) {
            reset();
            currentValue = settings.initialValue;

        } else {
            readStateFromBundle(state);
        }

        view.setExpressionVisible(settings.isExpressionShown);
        view.setDecimalSepBtnEnabled(nbFormat.getMaximumFractionDigits() > 0);
        view.setAnswerBtnVisible(settings.isAnswerBtnShown && resultValue != null);
        view.setSignBtnVisible(settings.isSignBtnShown);

        updateCurrentValue();
        updateExpression();
    }

    void detach() {
        view = null;
        settings = null;
    }

    void writeStateToBundle(Bundle bundle) {
        bundle.putParcelable("expression", expression);
        if (currentValue != null) {
            bundle.putSerializable("currentValue", currentValue);
        }
        if (resultValue != null) {
            bundle.putSerializable("resultValue", resultValue);
        }
        bundle.putInt("currentValueScale", currentValueScale);
        bundle.putInt("errorCode", errorCode);
        bundle.putBoolean("currentIsAnswer", currentIsAnswer);
        bundle.putBoolean("currentIsResult", currentIsResult);
        bundle.putBoolean("canEditCurrentValue", canEditCurrentValue);
        bundle.putBoolean("canEditExpression", canEditExpression);
    }

    private void readStateFromBundle(Bundle bundle) {
        Expression expr = bundle.getParcelable("expression");
        if (expr != null) {
            this.expression = expr;
        }
        if (bundle.containsKey("currentValue")) {
            currentValue = (BigDecimal) bundle.getSerializable("currentValue");
        }
        if (bundle.containsKey("resultValue")) {
            resultValue = (BigDecimal) bundle.getSerializable("resultValue");
        }
        currentValueScale = bundle.getInt("currentValueScale");
        errorCode = bundle.getInt("errorCode");
        currentIsAnswer = bundle.getBoolean("currentIsAnswer");
        currentIsResult = bundle.getBoolean("currentIsResult");
        canEditCurrentValue = bundle.getBoolean("canEditCurrentValue");
        canEditExpression = bundle.getBoolean("canEditExpression");
    }

    void onErasedOnce() {
        clearExpressionIfNeeded();
        if (dismissError()) return;

        currentIsAnswer = false;
        currentIsResult = false;
        view.setAnswerBtnVisible(false);

        if (!canEditCurrentValue) {
            currentValue = null;
            canEditCurrentValue = true;

        } else if (currentValue != null) {
            String valueStr = getCurrentValueString();
            valueStr = valueStr.substring(0, valueStr.length() - 1);
            try {
                currentValue = new BigDecimal(valueStr);
                if (currentValueScale >= 0) {
                    currentValueScale--;
                }
            } catch (NumberFormatException e) {
                // Happens if string is empty or "-".
                currentValue = null;
                currentValueScale = -1;
            }

        } else if (settings.isExpressionEditable && !expression.isEmpty()) {
            // No more digits to erase: pop last expression number and operator and make it current value
            currentValue = expression.numbers.remove(expression.numbers.size() - 1);
            expression.operators.remove(expression.operators.size() - 1);

            assert currentValue != null;
            currentValueScale = currentValue.scale();
            if (currentValueScale == 0) currentValueScale = -1;

            updateExpression();
        }

        updateCurrentValue();
    }

    void onErasedAll() {
        onClearBtnClicked();
    }

    void onDigitBtnClicked(int digit) {
        clearExpressionIfNeeded();
        dismissOldValue();

        String valueStr = getCurrentValueString();

        // Check if max digits has been exceeded
        int pointPos = valueStr.indexOf('.');
        int intDigits = (pointPos == -1 ? valueStr.length() : pointPos) - (!valueStr.isEmpty() && valueStr.charAt(0) == '-' ? 1 : 0);
        boolean maxIntReached = (intDigits >= settings.maxIntDigits);
        boolean maxFracReached = (pointPos != -1 && valueStr.length() - pointPos - 1 >= nbFormat.getMaximumFractionDigits());
        if (maxIntReached || maxFracReached) {
            // Can't add a new digit, it's already at the maximum.
            return;
        }

        if (pointPos != -1) {
            currentValueScale++;
        }

        currentValue = new BigDecimal(valueStr + digit);
        updateCurrentValue();
    }

    void onOperatorBtnClicked(@NonNull Expression.Operator operator) {
        clearExpressionIfNeeded();
        if (dismissError()) return;

        currentIsResult = false;
        currentValueScale = -1;

        if (!currentIsAnswer && !canEditCurrentValue && !expression.operators.isEmpty()) {
            // Undo previous operator button click if the current value is the
            // result of the expression calculated on the last button click.
            expression.operators.set(expression.operators.size() - 1, operator);

        } else {
            if (currentValue == null) {
                currentValue = BigDecimal.ZERO;
            }
            expression.numbers.add(currentValue);
            calculate();
            expression.operators.add(operator);

            if (!settings.shouldEvaluateOnOperation) {
                currentValue = null;
            }
        }

        view.setAnswerBtnVisible(settings.isAnswerBtnShown && resultValue != null);
        updateCurrentValue();
        updateExpression();
    }

    void onDecimalSepBtnClicked() {
        clearExpressionIfNeeded();
        dismissOldValue();

        if (currentValueScale == -1) {
            // Only insert a decimal point if there isn't one yet
            if (currentValue == null) {
                currentValue = BigDecimal.ZERO;
            }
            currentValueScale = 0;
            updateCurrentValue();
        }
    }

    void onSignBtnClicked() {
        dismissError();

        currentIsAnswer = false;
        view.setAnswerBtnVisible(false);

        if (!canEditCurrentValue && !currentIsResult && !expression.isEmpty()) {
            // If current value is result, it's not editable but still allow negation.
            // If current value is result and isn't editable, but expression is empty,
            // that means current value is initial value, so allow negation too.
            // Otherwise, clear value.
            currentValue = null;
            canEditCurrentValue = true;
            currentValueScale = -1;
        }

        // Negate value if there's one and it's not zero.
        if (currentValue != null && currentValue.compareTo(BigDecimal.ZERO) != 0) {
            currentValue = currentValue.negate();
        }

        updateCurrentValue();
    }

    void onEqualBtnClicked() {
        clearExpressionIfNeeded();
        if (dismissError()) return;
        equal();
    }

    void onAnswerBtnClicked() {
        assert resultValue != null;

        currentValue = resultValue;
        currentValueScale = -1;
        currentIsAnswer = true;
        canEditCurrentValue = false;

        view.setAnswerBtnVisible(false);
        updateCurrentValue();
    }

    void onClearBtnClicked() {
        clearExpressionIfNeeded();
        if (dismissError()) return;

        reset();

        view.setAnswerBtnVisible(false);
        updateCurrentValue();
        updateExpression();
    }

    void onCancelBtnClicked() {
        view.exit();
    }

    void onOkBtnClicked() {
        clearExpressionIfNeeded();
        if (dismissError()) return;

        equal();

        if (expression.numbers.size() > 1) {
            // If the expression still has more than 1 number it means it was just calculated.
            // Don't dismiss already to let user see the result.
            return;
        }

        if (resultValue != null) {
            // Check if value is out of bounds and if so, show an error.
            // Show special error messages if minimum or maximum is 0.
            if (settings.maxValue != null && resultValue.compareTo(settings.maxValue) > 0) {
                if (settings.maxValue.compareTo(BigDecimal.ZERO) == 0) {
                    setError(ERROR_WRONG_SIGN_NEG);
                } else {
                    setError(ERROR_OUT_OF_BOUNDS);
                }
                return;
            } else if (settings.minValue != null && resultValue.compareTo(settings.minValue) < 0) {
                if (settings.minValue.compareTo(BigDecimal.ZERO) == 0) {
                    setError(ERROR_WRONG_SIGN_POS);
                } else {
                    setError(ERROR_OUT_OF_BOUNDS);
                }
                return;
            }
        }

        if (errorCode == ERROR_NONE) {
            view.sendValueResult(resultValue);
            view.exit();
        }
    }

    void onDismissed() {
        reset();
    }

    private void clearExpressionIfNeeded() {
        if (!canEditExpression) {
            expression.clear();
            canEditExpression = true;
            currentIsResult = false;
            updateExpression();
        }
    }

    private void dismissOldValue() {
        dismissError();

        currentIsAnswer = false;

        view.setAnswerBtnVisible(false);

        if (!canEditCurrentValue) {
            currentValue = null;
            canEditCurrentValue = true;
            currentValueScale = -1;
        }
    }

    /**
     * Reset all variables to their initial value. Doesn't update the display.
     */
    private void reset() {
        expression.clear();
        currentValue = null;
        resultValue = null;
        currentValueScale = -1;
        errorCode = ERROR_NONE;

        currentIsAnswer = false;
        currentIsResult = false;
        canEditCurrentValue = false;
        canEditExpression = true;

        view.setAnswerBtnVisible(false);
    }

    private void calculate() {
        try {
            currentValue = expression.evaluate(settings.isOrderOfOperationsApplied,
                    settings.nbFormat.getMaximumFractionDigits(), nbFormat.getRoundingMode());
        } catch (ArithmeticException e) {
            // Division by zero occurred.
            setError(ERROR_DIV_ZERO);
            return;
        }

        currentValueScale = -1;
        currentIsAnswer = false;
        canEditCurrentValue = false;
    }

    private void equal() {
        if (!currentIsAnswer && !canEditCurrentValue && !expression.operators.isEmpty()) {
            // Remove unused last operator
            expression.operators.remove(expression.operators.size() - 1);
        } else {
            if (currentValue == null) {
                currentValue = BigDecimal.ZERO;
            }
            expression.numbers.add(currentValue);
        }

        calculate();

        if (errorCode == ERROR_NONE) {
            resultValue = currentValue;
            currentIsResult = true;
            currentValueScale = -1;
            updateCurrentValue();
        }

        canEditExpression = false;
        updateExpression();
    }

    private void setError(int error) {
        errorCode = error;

        // Reset all but not the expression.
        currentValue = null;
        resultValue = null;
        currentValueScale = -1;
        currentIsAnswer = false;
        canEditCurrentValue = false;
        canEditExpression = false;

        view.showErrorText(error);
    }

    private boolean dismissError() {
        if (errorCode != ERROR_NONE) {
            errorCode = ERROR_NONE;
            updateCurrentValue();
            return true;
        }
        return false;
    }

    private void updateCurrentValue() {
        if (currentIsAnswer) {
            view.showAnswerText();
            return;
        }

        BigDecimal value = currentValue;
        if (value == null && settings.isZeroShownWhenNoValue) {
            value = BigDecimal.ZERO;
        }

        String text = null;
        if (value != null) {
            if (currentValueScale > 0 && nbFormat.getMinimumFractionDigits() < currentValueScale) {
                // Set a minimum number of fraction digits so that trailing zeroes are shown.
                int minFracBefore = nbFormat.getMinimumFractionDigits();
                nbFormat.setMinimumFractionDigits(currentValueScale);
                text = nbFormat.format(value);
                nbFormat.setMinimumFractionDigits(minFracBefore);

            } else if (currentValueScale == 0
                    && nbFormat.getMinimumFractionDigits() == 0
                    && nbFormat instanceof DecimalFormat) {
                // Append the decimal separator at the end of the number.
                DecimalFormat fmt = (DecimalFormat) nbFormat;
                char sep = fmt.getDecimalFormatSymbols().getDecimalSeparator();
                if (value.compareTo(BigDecimal.ZERO) >= 0) {
                    String suffixBefore = fmt.getPositiveSuffix();
                    fmt.setPositiveSuffix(sep + suffixBefore);
                    text = nbFormat.format(value);
                    fmt.setPositiveSuffix(suffixBefore);
                } else {
                    String suffixBefore = fmt.getNegativeSuffix();
                    fmt.setNegativeSuffix(sep + suffixBefore);
                    text = nbFormat.format(value);
                    fmt.setNegativeSuffix(suffixBefore);
                }
            } else {
                text = nbFormat.format(value);
            }
        }

        view.updateCurrentValue(text);
    }

    private void updateExpression() {
        if (settings.isExpressionShown) {
            String text = expression.format(nbFormat);
            if (currentIsResult) {
                // If current value is the result from the equal button, append = to the expression.
                text += " =";
            }
            view.updateExpression(text);
        }
    }

    private String getCurrentValueString() {
        if (currentValue == null) return "";

        String str = currentValue.setScale(Math.max(0, currentValueScale),
                RoundingMode.UNNECESSARY).toPlainString();
        if (currentValueScale == 0) {
            str += '.';
        }
        return str;
    }

}
