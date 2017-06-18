package com.dremanovich.leadingbot.bot.strategies;

import com.dremanovich.leadingbot.bot.AggregatorDto;
import com.dremanovich.leadingbot.bot.listeners.IPoloniexStrategyListener;


public interface IPoloniexBotLendingStrategy {
    void start(AggregatorDto information);
    void addStrategyListener(IPoloniexStrategyListener listener);
}
