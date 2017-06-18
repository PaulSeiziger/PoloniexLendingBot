package com.dremanovich.leadingbot.bot.strategies;

import com.dremanovich.leadingbot.api.IPoloniexApi;
import com.dremanovich.leadingbot.api.entities.*;
import com.dremanovich.leadingbot.bot.AggregatorDto;
import com.dremanovich.leadingbot.bot.calculators.Calculator;
import com.dremanovich.leadingbot.bot.calculators.ICalculator;
import com.dremanovich.leadingbot.bot.listeners.IPoloniexStrategyListener;
import com.dremanovich.leadingbot.helpers.SettingsHelper;
import com.dremanovich.leadingbot.types.CurrencyValue;
import com.dremanovich.leadingbot.types.RateValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class SimpleLendingStrategy implements IPoloniexBotLendingStrategy {
    private static final RateValue ABSOLUTE_MINIMUM_LENDING_RATE = new RateValue(0.0000001);

    //TODO: Refactor set logger via setter
    private static final Logger log = LogManager.getLogger(SimpleLendingStrategy.class);

    private IPoloniexApi api;
    private SettingsHelper settings;
    private ICalculator calculator;

    private HashSet<IPoloniexStrategyListener> listeners = new HashSet<>();

    public SimpleLendingStrategy(IPoloniexApi api, SettingsHelper settings, ICalculator calculator){
        this.api = api;
        this.settings = settings;
        this.calculator = calculator;
    }

    @Override
    public void start(AggregatorDto information) {

        for (IPoloniexStrategyListener listener : listeners) {
            try {
                listener.onStart(information);
            }catch (Exception ex){
                log.error(ex.getMessage(), ex);
            }
        }

        if (information.getBalances() == null || information.getBalances().getLending() == null){
            log.warn("No balances");
            return;
        }

        if (information.getLoanOrders() == null){
            log.warn("No loan orders");
            return;
        }

        Map<String, CurrencyValue> lendingBalances = information.getBalances().getLending();


        newCurrency: for (String currency : settings.getCurrencies()) {
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

                //Calculate the interest rate based on the average value based on the first n items,
                // don't apply a reduction factor
                RateValue lendingRate = calculator.calculateAverageRateByOffers(
                        loanOrders.getOfferEntities(),
                        settings.getCountOffersForAverageCalculating()
                );


                //If there is a currency on the balance sheet
                if (hasFreeBalance) {
                    //Check rate that more than ABSOLUTE_MINIMUM_LENDING_RATE
                    if (lendingRate.compareTo(ABSOLUTE_MINIMUM_LENDING_RATE) > 0){
                        //We expose all available balance and make an offer at the calculated rate
                        CurrencyValue lendingBalance = lendingBalances.get(currency);
                        openOffer(currency,lendingBalance, settings.getLendingDays(), lendingRate);
                    }
                }

                //TODO: REFACTOR THIS. CREATE MANY METHODS. CREATE BIGDECIMAL ROUNDING METHOD

                //If we find a still open offer, we check how much time has passed since the opening
                if (information.getOpenedLoanOffers() != null && information.getOpenedLoanOffers().size() > 0){
                    final Map<String, List<OpenedLoanOfferEntity>> openedLoanOffers = information.getOpenedLoanOffers();
                    if (openedLoanOffers != null && openedLoanOffers.containsKey(currency)){
                        final List<OpenedLoanOfferEntity> openedLoanOfferEntities = openedLoanOffers.get(currency);

                        for (OpenedLoanOfferEntity openedLoanOfferEntity : openedLoanOfferEntities) {
                            long currentTime = System.currentTimeMillis();
                            int waitingTime = settings.getWaitBeforeReopenOffer();

                            //If there is more than the established limit, then
                            if (currentTime > (openedLoanOfferEntity.getDateTimestamp() + waitingTime)){

                                //Apply a reduction factor to the rate
                                double minimizePercent = settings.getAverageOfferMinimizingPercent();
                                if (minimizePercent > 0.0){
                                    RateValue percentValue = lendingRate.divide(100).multiply(minimizePercent);
                                    lendingRate = lendingRate.substract(percentValue);
                                }

                                //Check rate that more than ABSOLUTE_MINIMUM_LENDING_RATE
                                if (lendingRate.compareTo(ABSOLUTE_MINIMUM_LENDING_RATE) < 0){
                                    continue newCurrency;
                                }

                                //Find minimum waiting rate
                                RateValue minimumLendingRate = settings.getAverageOfferMinimumThresholds().get(currency);

                                if (lendingRate.compareTo(minimumLendingRate) < 0){
                                    continue newCurrency;
                                }

                                //Close the offer
                                boolean successClosed = closeOffer(openedLoanOfferEntity.getId());
                                if (successClosed){
                                    //We open a new offer with the calculated rate and the amount indicated in the closed offer
                                    CurrencyValue lendingBalance = openedLoanOfferEntity.getAmount();
                                    openOffer(currency,lendingBalance, settings.getLendingDays(), lendingRate);
                                }
                            }
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


    private boolean openOffer(String currency, CurrencyValue lendingBalance, int daysLending, RateValue lendingRate) throws IOException {
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


    private boolean closeOffer(int id) throws IOException {
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
