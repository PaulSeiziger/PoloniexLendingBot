package com.dremanovich.lendingbot.api.entities;

import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by PavelDremanovich on 22.05.17.
 */
public class OpenedLoanOfferEntity {

    private int id;
    private RateValue rate;
    private CurrencyValue amount;
    private int duration;
    private int autoRenew;
    private String date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RateValue getRate() {
        return rate;
    }

    public void setRate(RateValue rate) {
        this.rate = rate;
    }

    public CurrencyValue getAmount() {
        return amount;
    }

    public void setAmount(CurrencyValue amount) {
        this.amount = amount;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(int autoRenew) {
        this.autoRenew = autoRenew;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDateTimestamp()
    {
        long time = 0;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(this.date);
            time = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return time;
    }
}
