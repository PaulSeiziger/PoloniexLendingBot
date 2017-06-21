
package com.dremanovich.lendingbot.api.entities;


import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;

public class OfferEntity {

    private RateValue rate;
    private CurrencyValue amount;
    private int rangeMin;
    private int rangeMax;

    public RateValue getRate() {
        return rate;
    }

    public void setRate(RateValue rate) {
        this.rate = rate;
    }

    public CurrencyValue getAmount() {
        return amount;
    }

    public void setAmount(CurrencyValue amount) {
        this.amount = amount;
    }

    public int getRangeMin() {
        return rangeMin;
    }

    public void setRangeMin(int rangeMin) {
        this.rangeMin = rangeMin;
    }

    public int getRangeMax() {
        return rangeMax;
    }

    public void setRangeMax(int rangeMax) {
        this.rangeMax = rangeMax;
    }

}
