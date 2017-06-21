package com.dremanovich.lendingbot.api;

public enum Accounts {
    ALL("all"),
    EXCHANGE("exchange"),
    MARGIN("margin"),
    LENDING("lending");


    private String account;

    Accounts(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return account;
    }
}
