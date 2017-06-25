package com.dremanovich.lendingbot.bot.strategies;


import com.dremanovich.lendingbot.api.IPoloniexApi;
import com.dremanovich.lendingbot.api.entities.CanceledLoanOfferResponseEntity;
import com.dremanovich.lendingbot.api.entities.CreatedLoanOfferResponseEntity;
import com.dremanovich.lendingbot.api.entities.LoanOrdersEntity;
import com.dremanovich.lendingbot.api.entities.OpenedLoanOfferEntity;
import com.dremanovich.lendingbot.bot.AggregatorDto;
import com.dremanovich.lendingbot.bot.calculators.ICalculator;
import com.dremanovich.lendingbot.bot.listeners.IPoloniexStrategyListener;
import com.dremanovich.lendingbot.helpers.SettingsHelper;
import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;
import org.apache.logging.log4j.Logger;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class AbstractStrategy implements IPoloniexBotLendingStrategy {
    protected static final RateValue ABSOLUTE_MINIMUM_LENDING_RATE = new RateValue(0.0000001);

    protected IPoloniexApi api;
    protected SettingsHelper settings;
    protected ICalculator calculator;

    protected HashSet<IPoloniexStrategyListener> listeners = new HashSet<>();
    protected Logger log;

    public AbstractStrategy(Logger log, IPoloniexApi api, SettingsHelper settings, ICalculator calculator){
        this.api = api;
        this.settings = settings;
        this.calculator = calculator;
        this.log = log;
    }

    abstract void hasFreeBalance(AggregatorDto information, CurrencyValue balance, String currencyName);

    abstract void hasOpenedOffers(AggregatorDto information, OpenedLoanOfferEntity openedLoanOfferEntity, String currencyName);

    @Override
    public void start(AggregatorDto information) {

        //Notify listeners about starting analyze
        for (IPoloniexStrategyListener listener : listeners) {
            try {
                listener.onStart(information);
            }catch (Exception ex){
                log.error(ex.getMessage(), ex);
            }
        }

        if (!validate(information)){
            return;
        }

        Map<String, CurrencyValue> lendingBalances = information.getBalances().getLending();


        for (String currency : settings.getCurrencies()) {
            try {
                LoanOrdersEntity loanOrders = information.getLoanOrders().get(currency);

                if (loanOrders == null || loanOrders.getOfferEntities() == null){
                    log.warn("No loan orders!");
                    break;
                }

                boolean hasFreeBalance = (
                        lendingBalances.containsKey(currency) &&
                                lendingBalances.get(currency).compareTo(CurrencyValue.ZERO) > 0
                );

                //If there is a currency on the balance sheet
                if (hasFreeBalance) {
                    CurrencyValue lendingBalance = lendingBalances.get(currency);
                    hasFreeBalance(information, lendingBalance, currency);
                }

                //If exists opened loan offers
                if (information.getOpenedLoanOffers() != null && information.getOpenedLoanOffers().size() > 0){
                    final Map<String, List<OpenedLoanOfferEntity>> openedLoanOffers = information.getOpenedLoanOffers();
                    if (openedLoanOffers != null && openedLoanOffers.containsKey(currency)){
                        final List<OpenedLoanOfferEntity> openedLoanOfferEntities = openedLoanOffers.get(currency);

                        for (OpenedLoanOfferEntity openedLoanOfferEntity : openedLoanOfferEntities) {
                            hasOpenedOffers(information, openedLoanOfferEntity, currency);
                        }
                    }
                }

            } catch (Exception e){
                log.debug(e.getMessage(), e);
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

    private boolean validate(AggregatorDto information) {
        if (information.getBalances() == null || information.getBalances().getLending() == null){
            log.warn("No balances");
            return false;
        }

        if (information.getLoanOrders() == null){
            log.warn("No loan orders");
            return false;
        }

        return true;
    }
}
