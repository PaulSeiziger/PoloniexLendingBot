package com.dremanovich.lendingbot.bot.strategies;


import com.dremanovich.lendingbot.api.IPoloniexApi;
import com.dremanovich.lendingbot.api.entities.*;
import com.dremanovich.lendingbot.bot.AggregatorResult;
import com.dremanovich.lendingbot.bot.CurrencyInformationItem;
import com.dremanovich.lendingbot.bot.CurrencyInformationIterator;
import com.dremanovich.lendingbot.bot.listeners.IPoloniexStrategyListener;
import com.dremanovich.lendingbot.helpers.SettingsHelper;
import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;
import org.apache.logging.log4j.Logger;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public abstract class AbstractStrategy implements IPoloniexBotLendingStrategy {
    protected static final RateValue ABSOLUTE_MINIMUM_LENDING_RATE = new RateValue(0.0000001);

    protected IPoloniexApi api;
    protected SettingsHelper settings;

    protected HashSet<IPoloniexStrategyListener> listeners = new HashSet<>();
    protected Logger log;

    public AbstractStrategy(Logger log, IPoloniexApi api, SettingsHelper settings){
        this.api = api;
        this.settings = settings;
        this.log = log;
    }

    protected abstract void hasFreeBalance(CurrencyInformationItem information);

    protected abstract void hasOpenedOffers(CurrencyInformationItem information);

    @Override
    public void start(AggregatorResult result) {

        //Notify listeners about starting analyze
        for (IPoloniexStrategyListener listener : listeners) {
            try {
                listener.onStart(result);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        CurrencyInformationIterator information = result.getIterator();

        while (information.hasNext()) {
            CurrencyInformationItem item = information.next();

            List<OfferEntity> offers = item.getOffers();

            if (offers.size() > 0) {
                CurrencyValue balance = item.getAvailableBalance();
                boolean hasFreeBalance = balance.compareTo(CurrencyValue.ZERO) > 0;

                if (hasFreeBalance) {
                    hasFreeBalance(item);
                }

                //If exists opened loan offers
                if (item.getOpenedLoanOffers().size() > 0) {
                    hasOpenedOffers(item);
                }
            }
        }
    }

    @Override
    public void addStrategyListener(IPoloniexStrategyListener listener) {
        if (listener != null){
            listeners.add(listener);
        }
    }


    protected boolean openOffer(String currency, CurrencyValue lendingBalance, int daysLending, RateValue lendingRate) throws IOException {
        boolean result = false;
        String message = null;

        Response<CreatedLoanOfferResponseEntity> response = api.createLoanOffer(currency, lendingBalance, daysLending, 0, lendingRate).execute();

        result = (response.isSuccessful()) && (response.body().getSuccess() == 1);
        if (!result){
            if (response.body() != null){
                message = response.body().getMessage();
            }
        }

        for (IPoloniexStrategyListener listener : listeners) {
            try {
                listener.onOpenedOffer(result, currency,lendingBalance, daysLending, lendingRate, message);
            }catch (Exception ex){
                log.error(ex.getMessage(), ex);
            }
        }

        return result;
    }


    protected boolean closeOffer(int id) throws IOException {
        boolean result = false;
        String message = null;

        Response<CanceledLoanOfferResponseEntity> response = api.cancelLoanOffer(id).execute();

        result = (response.isSuccessful()) && (response.body().getSuccess() == 1);
        if (!result){
            if (response.body() != null){
                message = response.body().getMessage();
            }
        }

        for (IPoloniexStrategyListener listener : listeners) {
            try {
                listener.onClosedOffer(result, id, message);
            }catch (Exception ex){
                log.error(ex.getMessage(), ex);
            }
        }

        return result;
    }
}
