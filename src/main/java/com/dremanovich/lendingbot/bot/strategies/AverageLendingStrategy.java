package com.dremanovich.lendingbot.bot.strategies;

import com.dremanovich.lendingbot.api.IPoloniexApi;
import com.dremanovich.lendingbot.api.entities.*;
import com.dremanovich.lendingbot.bot.CurrencyInformationItem;
import com.dremanovich.lendingbot.helpers.SettingsHelper;
import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AverageLendingStrategy extends AbstractStrategy{

    public AverageLendingStrategy(Logger log, IPoloniexApi api, SettingsHelper settings){
        super(log, api, settings);
    }

    @Override
    void hasFreeBalance(CurrencyInformationItem item) {

        RateValue lendingRate = calculateRate(item);

        //Check rate that more than ABSOLUTE_MINIMUM_LENDING_RATE
        if (lendingRate.compareTo(ABSOLUTE_MINIMUM_LENDING_RATE) > 0){
            //We expose all available balance and make an offer at the calculated rate
            try {
                openOffer(item.getCurrencyName(), item.getAvailableBalance(), settings.getLendingDays(), lendingRate);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    void hasOpenedOffers(CurrencyInformationItem item) {
        //If we find a still open offer, we check how much time has passed since the opening
        long currentTime = System.currentTimeMillis();
        int waitingTime = settings.getWaitBeforeReopenOffer();

        //If there is more than the established limit, then
        for (OpenedLoanOfferEntity openedLoanOfferEntity : item.getOpenedLoanOffers()){
            if (currentTime > (openedLoanOfferEntity.getDateTimestamp() + waitingTime)){

                RateValue lendingRate = calculateRate(item);

                //Apply a reduction factor to the rate
                double minimizePercent = settings.getAverageOfferMinimizingPercent();
                if (minimizePercent > 0.0){
                    RateValue percentValue = lendingRate.divide(100).multiply(minimizePercent);
                    lendingRate = lendingRate.substract(percentValue);
                }

                //Check rate that more than ABSOLUTE_MINIMUM_LENDING_RATE
                if (lendingRate.compareTo(ABSOLUTE_MINIMUM_LENDING_RATE) < 0){
                    return;
                }

                //Find minimum waiting rate
                RateValue minimumLendingRate = settings
                        .getAverageOfferMinimumThresholds()
                        .get(
                            item.getCurrencyName()
                        );

                if (lendingRate.compareTo(minimumLendingRate) < 0){
                    return;
                }

                //Close the offer
                try {
                    boolean successClosed = closeOffer(openedLoanOfferEntity.getId());

                    if (successClosed){
                        //We open a new offer with the calculated rate
                        CurrencyValue lendingBalance = openedLoanOfferEntity.getAmount();
                        openOffer(item.getCurrencyName(),lendingBalance, settings.getLendingDays(), lendingRate);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    protected RateValue calculateRate(CurrencyInformationItem item) {
        //Calculate the interest rate based on the average value based on the first n items,
        // don't apply a reduction factor
        return item.calculateAverageRateByOffers(
            settings.getCountOffersForAverageCalculating()
        );
    }
}
