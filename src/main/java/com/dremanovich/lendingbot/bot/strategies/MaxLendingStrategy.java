package com.dremanovich.lendingbot.bot.strategies;

import com.dremanovich.lendingbot.api.IPoloniexApi;
import com.dremanovich.lendingbot.api.entities.LoanOrdersEntity;
import com.dremanovich.lendingbot.api.entities.OpenedLoanOfferEntity;
import com.dremanovich.lendingbot.bot.AggregatorDto;
import com.dremanovich.lendingbot.bot.calculators.ICalculator;
import com.dremanovich.lendingbot.helpers.SettingsHelper;
import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;
import org.apache.logging.log4j.Logger;

/**
 * Created by PavelDremanovich on 25.06.17.
 */
public class MaxLendingStrategy extends AverageLendingStrategy
{

    public MaxLendingStrategy(Logger log, IPoloniexApi api, SettingsHelper settings, ICalculator calculator) {
        super(log, api, settings, calculator);
    }

    @Override
    protected RateValue calculateRate(AggregatorDto information, String currencyName) {
        LoanOrdersEntity loanOrders = information.getLoanOrders().get(currencyName);

        //Calculate the interest rate based on the average value based on the first n items,
        // don't apply a reduction factor
        return calculator.calculateMaxRateByOffers(
                loanOrders.getOfferEntities(),
                settings.getCountOffersForAverageCalculating()
        );
    }
}