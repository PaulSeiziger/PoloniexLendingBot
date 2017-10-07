package com.dremanovich.lendingbot.bot.strategies;

import com.dremanovich.lendingbot.bot.CurrencyInformationIterator;
import com.dremanovich.lendingbot.bot.listeners.IPoloniexStrategyListener;


public interface IPoloniexBotLendingStrategy {
    void start(CurrencyInformationIterator information);
    void addStrategyListener(IPoloniexStrategyListener listener);
}
