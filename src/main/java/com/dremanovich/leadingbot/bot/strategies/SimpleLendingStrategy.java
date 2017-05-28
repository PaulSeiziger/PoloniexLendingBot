package com.dremanovich.leadingbot.bot.strategies;

import com.dremanovich.leadingbot.api.IPoloniexApi;
import com.dremanovich.leadingbot.api.entities.*;
import com.dremanovich.leadingbot.bot.AggregatorDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public class SimpleLendingStrategy implements IPoloniexBotLendingStrategy {
    private static final double ABSOLUTE_MINIMUM_LENDING_RATE = 0.00000100;
    private static final String MINIMIZE_OFFER_PERCENT_PROPERTY_NAME = "poloniex.strategy.minimize_offer_percent";
    private static final String COUNT_FOR_AVERAGE_PROPERTY_NAME = "poloniex.strategy.count_offers_for_average_calculating";
    private static final String LENDING_DAYS_PROPERTY_NAME = "poloniex.strategy.lending_days";
    private static final String WAIT_BEFORE_REOPEN_OFFER_PROPERTY_NAME = "poloniex.strategy.wait_before_reopen_offer";

    private Properties currencies;
    private Properties strategyProperties;
    private IPoloniexApi api;

    public SimpleLendingStrategy(IPoloniexApi api, Properties currencies, Properties strategyProperties) {
        this.api = api;
        this.currencies = currencies;
        this.strategyProperties = strategyProperties;

        if (!strategyProperties.containsKey(MINIMIZE_OFFER_PERCENT_PROPERTY_NAME)) {
            throw new IllegalArgumentException("Not found \"" + MINIMIZE_OFFER_PERCENT_PROPERTY_NAME + "\" property!");
        }

        if (!strategyProperties.containsKey(COUNT_FOR_AVERAGE_PROPERTY_NAME)) {
            throw new IllegalArgumentException("Not found \"" + COUNT_FOR_AVERAGE_PROPERTY_NAME + "\" property!");
        }

        if (!strategyProperties.containsKey(LENDING_DAYS_PROPERTY_NAME)) {
            throw new IllegalArgumentException("Not found \"" + LENDING_DAYS_PROPERTY_NAME + "\" property!");
        }

        if (!strategyProperties.containsKey(WAIT_BEFORE_REOPEN_OFFER_PROPERTY_NAME)) {
            throw new IllegalArgumentException("Not found \"" + WAIT_BEFORE_REOPEN_OFFER_PROPERTY_NAME + "\" property!");
        }
    }

    @Override
    public void start(AggregatorDto information) {

        if (information.getBalances() == null || information.getBalances().getLending() == null){
            System.out.println("No balances");
            return;
        }

        if (information.getLoanOrders() == null){
            System.out.println("No loan orders");
            return;
        }

        Map<String, BigDecimal> lendingBalances = information.getBalances().getLending();

        for (Object currencyValue : currencies.values()) {
            try {
                String currency = (String)currencyValue;

                LoanOrdersEntity loanOrders = information.getLoanOrders().get(currency);

                if (loanOrders == null || loanOrders.getOfferEntities() == null){
                    System.out.println("No loan orders!");
                    break;
                }

                boolean hasFreeBalance = (
                        lendingBalances.containsKey(currency) &&
                        lendingBalances.get(currency).compareTo(BigDecimal.ZERO) > 0
                );

                int countOffersForAverage = Integer.parseInt(strategyProperties.getProperty(COUNT_FOR_AVERAGE_PROPERTY_NAME));
                //Calculate the interest rate based on the average value based on the first n items,
                // don't apply a reduction factor
                double lendingRate = calculateAverageRateByOffers(loanOrders.getOfferEntities(), countOffersForAverage);

                //If there is a currency on the balance sheet
                if (hasFreeBalance) {
                    //Check rate that more than ABSOLUTE_MINIMUM_LENDING_RATE
                    if (lendingRate > ABSOLUTE_MINIMUM_LENDING_RATE){
                        //We expose all available balance and make an offer at the calculated rate
                        int daysLending = Integer.parseInt(strategyProperties.getProperty(LENDING_DAYS_PROPERTY_NAME));
                        double lendingBalance = lendingBalances.get(currency).doubleValue();
//                        api.createLoanOffer(currency, lendingBalance, daysLending, 0, lendingRate);
                        System.out.println("Opened offer (by Balance): " + currency + "; " + lendingBalance + "; " + daysLending + "; " + lendingRate);
                    }
                }

                //If we find a still open offer, we check how much time has passed since the opening
                if (information.getOpenedLoanOffers() != null && information.getOpenedLoanOffers().size() > 0){
                    final Map<String, List<OpenedLoanOfferEntity>> openedLoanOffers = information.getOpenedLoanOffers();
                    if (openedLoanOffers != null && openedLoanOffers.containsKey(currency)){
                        final List<OpenedLoanOfferEntity> openedLoanOfferEntities = openedLoanOffers.get(currency);

                        for (OpenedLoanOfferEntity openedLoanOfferEntity : openedLoanOfferEntities) {
                            long currentTime = System.currentTimeMillis();
                            int waitingTime = Integer.parseInt(strategyProperties.getProperty(WAIT_BEFORE_REOPEN_OFFER_PROPERTY_NAME));

                            //If there is more than the established limit, then
                            if (currentTime > (openedLoanOfferEntity.getDateTimestamp() + waitingTime)){
                                //Close the offer
//                                Response<CanceledLoanOfferResponseEntity> response = api.cancelLoanOffer(openedLoanOfferEntity.getId()).execute();
                                System.out.println("Close offer with id: " + openedLoanOfferEntity.getId());
//                                if (response.isSuccessful()){
                                    //Apply a reduction factor to the rate
                                    double minimizePercent = Double.parseDouble(strategyProperties.getProperty(MINIMIZE_OFFER_PERCENT_PROPERTY_NAME));
                                    if (minimizePercent > 0.0){
                                        double percentValue = (lendingRate / 100) * minimizePercent;
                                        lendingRate -= percentValue;
                                        lendingRate = new BigDecimal(lendingRate).doubleValue();
                                    }

                                    //Check rate that more than ABSOLUTE_MINIMUM_LENDING_RATE
                                    if (lendingRate > ABSOLUTE_MINIMUM_LENDING_RATE){
                                        //We open a new offer with the calculated rate and the amount indicated in the closed offer
                                        int daysLending = Integer.parseInt(strategyProperties.getProperty(LENDING_DAYS_PROPERTY_NAME));
                                        double lendingBalance = openedLoanOfferEntity.getAmount();
//                                        api.createLoanOffer(currency, lendingBalance, daysLending, 0, lendingRate);
                                        System.out.println("Opened offer: " + currency + "; " + lendingBalance + "; " + daysLending + "; " + lendingRate);
                                    }

//                                }

                            }
                        }
                    }
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }

        System.out.println("Strategy done!");
    }

    private double calculateAverageRateByOffers(List<OfferEntity> offers, int count){
        double average = 0d;

        int length = (offers.size() > count) ? count : offers.size();

        for (int i = 0; i < length; i++){
            OfferEntity offer = offers.get(i);
            if (offer != null){
                average += offer.getRate();
            }
        }

        average /= length;

        return average;
    }


    private class CreatedLoanOfferListener implements Callback<CreatedLoanOfferResponseEntity> {
        @Override
        public void onResponse(Call<CreatedLoanOfferResponseEntity> call, Response<CreatedLoanOfferResponseEntity> response) {
            if (response.isSuccessful()) {
                CreatedLoanOfferResponseEntity loanOfferResponseEntity = response.body();

                System.out.println(loanOfferResponseEntity.getSuccess());
                System.out.println(loanOfferResponseEntity.getMessage());
                System.out.println(loanOfferResponseEntity.getOrderID());

            } else {
                System.out.println(response.raw().code());
            }
        }

        @Override
        public void onFailure(Call<CreatedLoanOfferResponseEntity> call, Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
