package com.dremanovich.leadingbot.bot.strategies;

import com.dremanovich.leadingbot.api.IPoloniexApi;
import com.dremanovich.leadingbot.api.entities.*;
import com.dremanovich.leadingbot.bot.AggregatorDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimpleLendingStrategy implements IPoloniexBotLendingStrategy {
    private static final double ABSOLUTE_MINIMUM_LENDING_RATE = 0.00000100;
    private static final String MINIMIZE_OFFER_PERCENT_PROPERTY_NAME = "poloniex.strategy.minimize_offer_percent";
    private static final String COUNT_FOR_AVERAGE_PROPERTY_NAME = "poloniex.strategy.count_offers_for_average_calculating";
    private static final String LENDING_DAYS_PROPERTY_NAME = "poloniex.strategy.lending_days";
    private static final String WAIT_BEFORE_REOPEN_OFFER_PROPERTY_NAME = "poloniex.strategy.wait_before_reopen_offer";
    private static final String MINIMUM_LENDING_THRESHOLD_PART = "poloniex.strategy.minimum_threshold_";

    private static final Logger log = LogManager.getLogger(SimpleLendingStrategy.class);
    private static final Marker financesMarker = new MarkerManager.Log4jMarker("FINANCE");
    private static final Marker statisticMarker = new MarkerManager.Log4jMarker("STATISTICS");

    private Properties currencies;
    private Properties strategyProperties;
    private IPoloniexApi api;

    public SimpleLendingStrategy(IPoloniexApi api, Properties currencies, Properties strategyProperties) throws IllegalArgumentException {
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
            log.warn("No balances");
            return;
        }

        if (information.getLoanOrders() == null){
            log.warn("No loan orders");
            return;
        }

        Map<String, BigDecimal> lendingBalances = information.getBalances().getLending();

        //Print Statistic
        try {
            printStatistic(information);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        newCurrency: for (Object currencyValue : currencies.values()) {
            try {
                String currency = (String)currencyValue;

                LoanOrdersEntity loanOrders = information.getLoanOrders().get(currency);

                if (loanOrders == null || loanOrders.getOfferEntities() == null){
                    log.warn("No loan orders!");
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

                boolean jobDone = false;

                //If there is a currency on the balance sheet
                if (hasFreeBalance) {
                    //Check rate that more than ABSOLUTE_MINIMUM_LENDING_RATE
                    if (lendingRate > ABSOLUTE_MINIMUM_LENDING_RATE){
                        //We expose all available balance and make an offer at the calculated rate
                        int daysLending = Integer.parseInt(strategyProperties.getProperty(LENDING_DAYS_PROPERTY_NAME));
                        double lendingBalance = lendingBalances.get(currency).setScale(8,BigDecimal.ROUND_HALF_EVEN).doubleValue();
                        jobDone = openOffer(currency,lendingBalance, daysLending, lendingRate);
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
                            int waitingTime = Integer.parseInt(strategyProperties.getProperty(WAIT_BEFORE_REOPEN_OFFER_PROPERTY_NAME));

                            //If there is more than the established limit, then
                            if (currentTime > (openedLoanOfferEntity.getDateTimestamp() + waitingTime)){

                                //Apply a reduction factor to the rate
                                double minimizePercent = Double.parseDouble(strategyProperties.getProperty(MINIMIZE_OFFER_PERCENT_PROPERTY_NAME));
                                if (minimizePercent > 0.0){
                                    double percentValue = (lendingRate / 100) * minimizePercent;
                                    lendingRate -= percentValue;
                                    lendingRate = new BigDecimal(lendingRate).setScale(8,BigDecimal.ROUND_HALF_EVEN).doubleValue();
                                }

                                //Check rate that more than ABSOLUTE_MINIMUM_LENDING_RATE
                                if (lendingRate <= ABSOLUTE_MINIMUM_LENDING_RATE){
                                    continue newCurrency;
                                }

                                //Find minimum waiting rate
                                String minimumLendingRateString = strategyProperties.getProperty(MINIMUM_LENDING_THRESHOLD_PART + currency);
                                if (minimumLendingRateString != null){
                                    Double minimumThresholdLendingRate = Double.parseDouble(minimumLendingRateString);

                                    if (lendingRate <= minimumThresholdLendingRate){
                                        continue newCurrency;
                                    }
                                }

                                //Close the offer
                                boolean successClosed = closeOffer(openedLoanOfferEntity.getId());
                                if (successClosed){
                                    //We open a new offer with the calculated rate and the amount indicated in the closed offer
                                    int daysLending = Integer.parseInt(strategyProperties.getProperty(LENDING_DAYS_PROPERTY_NAME));
                                    double lendingBalance = openedLoanOfferEntity.getAmount();
                                    jobDone = openOffer(currency,lendingBalance, daysLending, lendingRate);
                                }
                            }
                        }
                    }
                }

                if (jobDone){
                    log.trace(financesMarker, "---------------------------------------------------------------\n");
                }

            } catch (Exception e){
                log.debug(e.getMessage(), e);
            }
        }
    }


    private double calculateAverageRateByOffers(List<OfferEntity> offers, int count){
        double average = 0d;

        if (offers != null){
            int length = (offers.size() > count) ? count : offers.size();

            for (int i = 0; i < length; i++){
                OfferEntity offer = offers.get(i);
                if (offer != null){
                    average += offer.getRate();
                }
            }

            average /= length;
        }


        return average;
    }

    private boolean openOffer(String currency, double lendingBalance, int daysLending, double lendingRate) throws IOException {
        final Response<CreatedLoanOfferResponseEntity> response = api.createLoanOffer(currency, lendingBalance, daysLending, 0, lendingRate).execute();

        BigDecimal printedLendingRate = new BigDecimal(lendingRate * 100).setScale(8,BigDecimal.ROUND_HALF_EVEN);

        if (response.isSuccessful() && response.body().getSuccess() == 1){
            log.trace(
                    financesMarker,
                    "Opened offer: \n" +
                            "Currency:" + currency +
                            "; Lending Balance: " + lendingBalance +
                            "; Days: " + daysLending + "; " +
                            "Lending rate: " + printedLendingRate
            );

            return true;
        } else {
            String message = "Can't open offer by rate " + printedLendingRate + " with amount " + lendingBalance ;
            if (response.body() != null){
                message += "\nReason: " + response.body().getMessage();
            }

            log.trace(financesMarker, message);
            log.warn(message);

            return false;
        }

    }


    private boolean closeOffer(int id) throws IOException {
        Response<CanceledLoanOfferResponseEntity> response = api.cancelLoanOffer(id).execute();
        if (response.isSuccessful() && response.body().getSuccess() == 1){
            log.trace(financesMarker, "Close offer with id: " + id);
            return true;
        } else {
            String message = "Can't close offer with id: " + id;
            if (response.body() != null){
                message += "\nReason: " + response.body().getMessage();
            }

            log.trace(financesMarker, message);
            log.warn(message);

            return false;
        }
    }


    private void printStatistic(AggregatorDto information)
    {
        if (information.getBalances().getLending() != null){
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date date = new Date();

            log.trace(statisticMarker, "Date: " + dateFormat.format(date));
            log.trace(statisticMarker, "Balances:\r\n");

            Map<String, BigDecimal> lendingBalances = information.getBalances().getLending();

            for (Map.Entry<Object, Object> currencyProperty : currencies.entrySet()) {
                String currency = (String)currencyProperty.getValue();
                log.trace(statisticMarker, "Currency: " + currency);
                if (lendingBalances.containsKey(currency)){
                    log.trace(statisticMarker, "Balance: " + lendingBalances.get(currency).setScale(8, RoundingMode.HALF_DOWN));
                } else {
                    log.trace(statisticMarker, "Balance: 0.0");
                }

                if (
                        information.getLoanOrders() != null &&
                        information.getLoanOrders().get(currency) != null
                ){
                    int countOffersForAverage = Integer.parseInt(strategyProperties.getProperty(COUNT_FOR_AVERAGE_PROPERTY_NAME));
                    double average = calculateAverageRateByOffers(information.getLoanOrders().get(currency).getOfferEntities(), countOffersForAverage) * 100;

                    log.trace(statisticMarker, "Average offer: " + (new BigDecimal(average)).setScale(8, RoundingMode.HALF_DOWN));
                } else {
                    log.trace(statisticMarker, "No offers!");
                }

                log.trace(statisticMarker, "\r\n******************************************************************");


                List<OpenedLoanOfferEntity> openedLoanOffers = information.getOpenedLoanOffers().get(currency);
                if (openedLoanOffers != null){
                    for (OpenedLoanOfferEntity offer : openedLoanOffers){
                        BigDecimal printedRate = new BigDecimal(offer.getRate() * 100).setScale(8, RoundingMode.HALF_DOWN);
                        BigDecimal printedAmount = new BigDecimal(offer.getAmount()).setScale(8, RoundingMode.HALF_DOWN);
                        log.trace(statisticMarker, "Rate: " + printedRate + "; Amount: " + printedAmount + "; Duration: " + offer.getDuration() + "; Date" + offer.getDate() );
                    }
                } else {
                    log.trace(statisticMarker, "No opened orders!");
                }
                log.trace(statisticMarker, "-----------------------------------------------------------------\r\n");
            }

            log.trace(statisticMarker, "=============================================================================\r\n\r\n");
        }
    }
}
