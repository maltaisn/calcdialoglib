package com.nmaltais.calcdialog;

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
        assertEquals(0, result.compareTo(new BigDecimal("-12.7")));
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
        assertEquals(0, result1.compareTo(new BigDecimal("35")));

        BigDecimal result2 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(0, result2.compareTo(new BigDecimal("23")));
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
        assertEquals(0, result1.compareTo(new BigDecimal("12.41666667")));

        BigDecimal result2 = expr.evaluate(true, 8, RoundingMode.HALF_UP);
        assertEquals(0, result2.compareTo(new BigDecimal("12.33333333")));
    }

}
