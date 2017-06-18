package com.dremanovich.leadingbot.types;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mutable Object
 */
public class CurrencyValue implements IDecimalValueType<CurrencyValue>{
    private static final int SIZE = 8;
    private static final RoundingMode MODE = RoundingMode.HALF_DOWN;

    public static final CurrencyValue ONE = new CurrencyValue(1);
    public static final CurrencyValue ZERO = new CurrencyValue(0);

    private BigDecimal value;

    public CurrencyValue(String val) {
        value = (new BigDecimal(val)).setScale(SIZE, MODE);
    }
    public CurrencyValue(double val) {
        value = (new BigDecimal(val)).setScale(SIZE, MODE);
    }
    public CurrencyValue(long val) {
        value = (new BigDecimal(val)).setScale(SIZE, MODE);
    }

    public CurrencyValue add(CurrencyValue augend) {
        if(augend != null){
            value = value.add(augend.toBigDecimal());
        }
        return this;
    }

    @Override
    public CurrencyValue add(double augend) {

        return add(new CurrencyValue(augend));
    }

    @Override
    public CurrencyValue add(long augend) {
        return add(new CurrencyValue(augend));
    }

    @Override
    public CurrencyValue substract(CurrencyValue subtrahend) {
        if (subtrahend != null){
            value = value.subtract(subtrahend.toBigDecimal());
        }
        return this;
    }

    @Override
    public CurrencyValue substract(double subtrahend) {
        return substract(new CurrencyValue(subtrahend));
    }

    @Override
    public CurrencyValue substract(long subtrahend) {
        return substract(new CurrencyValue(subtrahend));
    }


    @Override
    public CurrencyValue divide(CurrencyValue divisor) throws ArithmeticException {
        if (divisor != null){
            value = value.divide(divisor.toBigDecimal(), MODE);
        } else {
            throw new ArithmeticException("Dividing by zero!");
        }
        return this;
    }

    @Override
    public CurrencyValue divide(double divisor) {
        return divide(new CurrencyValue(divisor));
    }

    @Override
    public CurrencyValue divide(long divisor) {
        return divide(new CurrencyValue(divisor));
    }

    @Override
    public CurrencyValue multiply(CurrencyValue multiplicand) {
        if (multiplicand != null){
            value = value.multiply(multiplicand.toBigDecimal());
        }
        return this;
    }

    @Override
    public CurrencyValue multiply(double multiplicand) {
        return multiply(new CurrencyValue(multiplicand));
    }

    @Override
    public CurrencyValue multiply(long multiplicand) {
        return multiply(new CurrencyValue(multiplicand));
    }

    @Override
    public String toString() {
        return value.setScale(SIZE, MODE).toString();
    }

    public BigDecimal toBigDecimal() {
        return value;
    }

    @Override
    public int compareTo(CurrencyValue o) {
        if (o == this){
            return 0;
        }

        return value.compareTo(o.toBigDecimal());
    }
}
