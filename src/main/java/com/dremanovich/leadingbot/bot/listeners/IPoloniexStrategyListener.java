package com.dremanovich.leadingbot.bot.listeners;

import com.dremanovich.leadingbot.bot.AggregatorDto;
import com.dremanovich.leadingbot.types.CurrencyValue;
import com.dremanovich.leadingbot.types.RateValue;

public interface IPoloniexStrategyListener {
    void onStart(AggregatorDto information);
    void onOpenedOffer(boolean success, String currencyName, CurrencyValue lendingBalance, int daysLending, RateValue lendingRate, String infoMessage);
    void onClosedOffer(boolean success, int id, String infoMessage);
}
