package com.dremanovich.lendingbot.api.entities;

import com.dremanovich.lendingbot.types.CurrencyValue;

import java.util.Map;

public class AvailableAccountBalancesEntity {
    private Map<String, CurrencyValue> exchange;
    private Map<String, CurrencyValue> margin;
    private Map<String, CurrencyValue> lending;

    public Map<String, CurrencyValue> getExchange() {
        return exchange;
    }

    public void setExchange(Map<String, CurrencyValue> exchange) {
        this.exchange = exchange;
    }

    public Map<String, CurrencyValue> getMargin() {
        return margin;
    }

    public void setMargin(Map<String, CurrencyValue> margin) {
        this.margin = margin;
    }

    public Map<String, CurrencyValue> getLending() {
        return lending;
    }

    public void setLending(Map<String, CurrencyValue> lending) {
        this.lending = lending;
    }
}
