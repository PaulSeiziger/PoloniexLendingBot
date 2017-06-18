
package com.dremanovich.leadingbot.api.entities;


import com.dremanovich.leadingbot.types.CurrencyValue;

public class CompleteBalanceEntity {

    private CurrencyValue available;
    private CurrencyValue onOrders;
    private CurrencyValue btcValue;

    public CurrencyValue getAvailable() {
        return available;
    }

    public void setAvailable(CurrencyValue available) {
        this.available = available;
    }

    public CurrencyValue getOnOrders() {
        return onOrders;
    }

    public void setOnOrders(CurrencyValue onOrders) {
        this.onOrders = onOrders;
    }

    public CurrencyValue getBtcValue() {
        return btcValue;
    }

    public void setBtcValue(CurrencyValue btcValue) {
        this.btcValue = btcValue;
    }

}
