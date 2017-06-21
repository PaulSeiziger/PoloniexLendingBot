package com.dremanovich.lendingbot.bot.strategies;

import com.dremanovich.lendingbot.bot.AggregatorDto;
import com.dremanovich.lendingbot.bot.listeners.IPoloniexStrategyListener;


public interface IPoloniexBotLendingStrategy {
    void start(AggregatorDto information);
    void addStrategyListener(IPoloniexStrategyListener listener);
}
