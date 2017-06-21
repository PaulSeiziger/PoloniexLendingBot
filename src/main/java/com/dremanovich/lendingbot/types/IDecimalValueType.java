package com.dremanovich.lendingbot.types;

/**
 * Created by PavelDremanovich on 18.06.17.
 */
public interface IDecimalValueType<T> extends Comparable<T> {
    T add(T augend);
    T add(double augend);
    T add(long augend);
    T substract(T subtrahend);
    T substract(double subtrahend);
    T substract(long subtrahend);
    T divide(T divisor);
    T divide(double divisor);
    T divide(long divisor);
    T multiply(T multiplicand);
    T multiply(double multiplicand);
    T multiply(long multiplicand);

    @Override
    int compareTo(T o);
}
