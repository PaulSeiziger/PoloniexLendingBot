package com.dremanovich.lendingbot.settings;

import com.dremanovich.lendingbot.types.RateValue;

import java.util.Map;

/**
 * Created by PavelDremanovich on 04.06.17.
 */
public class StrategySettingsEntity {
    private double averageOfferMinimizingPercent;
    private int countOffersForAverageCalculating;
    private int lendingDays;
    private int waitBeforeReopenOffer;
    private Map<String, RateValue> averageOfferMinimumThresholds;

    public double getAverageOfferMinimizingPercent() {
        return averageOfferMinimizingPercent;
    }

    public void setAverageOfferMinimizingPercent(double averageOfferMinimizingPercent) {
        this.averageOfferMinimizingPercent = averageOfferMinimizingPercent;
    }

    public int getCountOffersForAverageCalculating() {
        return countOffersForAverageCalculating;
    }

    public void setCountOffersForAverageCalculating(int countOffersForAverageCalculating) {
        this.countOffersForAverageCalculating = countOffersForAverageCalculating;
    }

    public int getLendingDays() {
        return lendingDays;
    }

    public void setLendingDays(int lendingDays) {
        this.lendingDays = lendingDays;
    }

    public int getWaitBeforeReopenOffer() {
        return waitBeforeReopenOffer;
    }

    public void setWaitBeforeReopenOffer(int waitBeforeReopenOffer) {
        this.waitBeforeReopenOffer = waitBeforeReopenOffer;
    }

    public Map<String, RateValue> getAverageOfferMinimumThresholds() {
        return averageOfferMinimumThresholds;
    }

    public void setAverageOfferMinimumThresholds(Map<String, RateValue> averageOfferMinimumThresholds) {
        this.averageOfferMinimumThresholds = averageOfferMinimumThresholds;
    }
}
