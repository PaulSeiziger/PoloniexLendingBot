package com.dremanovich.lendingbot.bot.listeners;

import com.dremanovich.lendingbot.bot.AggregatorResult;
import com.dremanovich.lendingbot.bot.CurrencyInformationIterator;
import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;

public interface IPoloniexStrategyListener {
    void onStart(AggregatorResult information);
    void onOpenedOffer(boolean success, String currencyName, CurrencyValue lendingBalance, int daysLending, RateValue lendingRate, String infoMessage);
    void onClosedOffer(boolean success, int id, String infoMessage);
}
