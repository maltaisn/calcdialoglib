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

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.assertEquals;

public class ExpressionTest {

    @Test
    public void twoSum() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("12.1"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("-24.8"));

        BigDecimal result = expr.evaluate(false, 8, RoundingMode.HALF_UP);
        assertEquals(result, new BigDecimal("-12.7"));
    }

    @Test
    public void sumAndMultiply() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("3"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("4"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("5"));

        BigDecimal result1 = expr.evaluate(false, 8, RoundingMode.HALF_UP);
        assertEquals(result1, new BigDecimal("35"));

        BigDecimal result2 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(result2, new BigDecimal("23"));
    }

    @Test
    public void longExpression() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("-1"));
        expr.operators.add(Expression.Operator.DIVIDE);
        expr.numbers.add(new BigDecimal("6"));
        expr.operators.add(Expression.Operator.ADD);
        expr.numbers.add(new BigDecimal("5"));
        expr.operators.add(Expression.Operator.MULTIPLY);
        expr.numbers.add(new BigDecimal("12"));
        expr.operators.add(Expression.Operator.DIVIDE);
        expr.numbers.add(new BigDecimal("3"));
        expr.operators.add(Expression.Operator.DIVIDE);
        expr.numbers.add(new BigDecimal("8"));
        expr.operators.add(Expression.Operator.SUBTRACT);
        expr.numbers.add(new BigDecimal("-10"));

        BigDecimal result1 = expr.evaluate(false, 8, RoundingMode.HALF_UP);
        assertEquals(result1, new BigDecimal("12.41666667"));

        BigDecimal result2 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(result2, new BigDecimal("12.33333333"));
    }

    @Test
    public void stripTrailingZeroes() {
        Expression expr = new Expression();
        expr.numbers.add(new BigDecimal("1"));
        expr.operators.add(Expression.Operator.DIVIDE);
        expr.numbers.add(new BigDecimal("8"));

        BigDecimal result = expr.evaluate(false, 8, RoundingMode.HALF_UP);
        assertEquals(result, new BigDecimal("0.125"));
    }

}
