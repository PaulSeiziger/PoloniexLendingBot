package com.dremanovich.leadingbot.bot.strategies;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;


public interface IPoloniexBotLendingStrategy {
    void start(
            ConcurrentHashMap<String, BigDecimal> currentAverageOfferRate,
            ConcurrentHashMap<String, BigDecimal> currentAvailableBalance
    );
}
