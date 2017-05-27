package com.dremanovich.leadingbot.api.entities;

import java.math.BigDecimal;
import java.util.Map;

public class AvailableAccountBalancesEntity {
    private Map<String, BigDecimal> exchange;
    private Map<String, BigDecimal> margin;
    private Map<String, BigDecimal> lending;

    public Map<String, BigDecimal> getExchange() {
        return exchange;
    }

    public void setExchange(Map<String, BigDecimal> exchange) {
        this.exchange = exchange;
    }

    public Map<String, BigDecimal> getMargin() {
        return margin;
    }

    public void setMargin(Map<String, BigDecimal> margin) {
        this.margin = margin;
    }

    public Map<String, BigDecimal> getLending() {
        return lending;
    }

    public void setLending(Map<String, BigDecimal> lending) {
        this.lending = lending;
    }
}
