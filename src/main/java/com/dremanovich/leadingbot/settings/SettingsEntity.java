package com.dremanovich.leadingbot.settings;

import java.util.List;

/**
 * Created by PavelDremanovich on 04.06.17.
 */
public class SettingsEntity {
    private BotSettingsEntity bot;
    private List<String> currencies;
    private StrategySettingsEntity strategy;

    public BotSettingsEntity getBot() {
        return bot;
    }

    public void setBot(BotSettingsEntity bot) {
        this.bot = bot;
    }

    public List<String> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<String> currencies) {
        this.currencies = currencies;
    }

    public StrategySettingsEntity getStrategy() {
        return strategy;
    }

    public void setStrategy(StrategySettingsEntity strategy) {
        this.strategy = strategy;
    }
}
