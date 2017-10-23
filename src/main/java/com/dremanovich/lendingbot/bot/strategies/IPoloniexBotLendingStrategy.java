package com.dremanovich.lendingbot.bot.strategies;


import com.dremanovich.lendingbot.bot.AggregatorResult;
import com.dremanovich.lendingbot.bot.listeners.IPoloniexStrategyListener;


public interface IPoloniexBotLendingStrategy {
    void start(AggregatorResult information);
    void addStrategyListener(IPoloniexStrategyListener listener);
}
