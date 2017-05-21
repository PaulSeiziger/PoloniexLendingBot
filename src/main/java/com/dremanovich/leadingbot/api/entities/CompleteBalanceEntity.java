
package com.dremanovich.leadingbot.api.entities;


public class CompleteBalanceEntity {

    private double available;
    private double onOrders;
    private double btcValue;

    public double getAvailable() {
        return available;
    }

    public void setAvailable(double available) {
        this.available = available;
    }

    public double getOnOrders() {
        return onOrders;
    }

    public void setOnOrders(double onOrders) {
        this.onOrders = onOrders;
    }

    public double getBtcValue() {
        return btcValue;
    }

    public void setBtcValue(double btcValue) {
        this.btcValue = btcValue;
    }

}
