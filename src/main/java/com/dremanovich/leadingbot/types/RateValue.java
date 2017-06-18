package com.dremanovich.leadingbot.types;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by PavelDremanovich on 17.06.17.
 */
public class RateValue implements IDecimalValueType<RateValue>{
    private static final int size = 4;
    private static final RoundingMode mode = RoundingMode.HALF_DOWN;
    private BigDecimal value;

    public RateValue(String val) {
        value = (new BigDecimal(val)).setScale(size, mode);
    }
    public RateValue(double val) {
        value = (new BigDecimal(val)).setScale(size, mode);
    }
    public RateValue(long val) {
        value = (new BigDecimal(val)).setScale(size, mode);
    }

    public RateValue add(RateValue augend) {
        if(augend != null){
            value = value.add(augend.toBigDecimal());
        }
        return this;
    }

    @Override
    public RateValue add(double augend) {

        return add(new RateValue(augend));
    }

    @Override
    public RateValue add(long augend) {
        return add(new RateValue(augend));
    }

    @Override
    public RateValue substract(RateValue subtrahend) {
        if (subtrahend != null){
            value = value.subtract(subtrahend.toBigDecimal());
        }
        return this;
    }

    @Override
    public RateValue substract(double subtrahend) {
        return substract(new RateValue(subtrahend));
    }

    @Override
    public RateValue substract(long subtrahend) {
        return substract(new RateValue(subtrahend));
    }


    @Override
    public RateValue divide(RateValue divisor) throws ArithmeticException {
        if (divisor != null){
            value = value.divide(divisor.toBigDecimal(), mode);
        } else {
            throw new ArithmeticException("Dividing by zero!");
        }
        return this;
    }

    @Override
    public RateValue divide(double divisor) {
        return divide(new RateValue(divisor));
    }

    @Override
    public RateValue divide(long divisor) {
        return divide(new RateValue(divisor));
    }

    @Override
    public RateValue multiply(RateValue multiplicand) {
        if (multiplicand != null){
            value = value.multiply(multiplicand.toBigDecimal());
        }
        return this;
    }

    @Override
    public RateValue multiply(double multiplicand) {
        return multiply(new RateValue(multiplicand));
    }

    @Override
    public RateValue multiply(long multiplicand) {
        return multiply(new RateValue(multiplicand));
    }

    @Override
    public String toString() {
        return value.setScale(size, mode).toString();
    }

    public BigDecimal toBigDecimal() {
        return value;
    }

    @Override
    public int compareTo(RateValue o) {
        if (o == this){
            return 0;
        }

        return value.compareTo(o.toBigDecimal());
    }
}
