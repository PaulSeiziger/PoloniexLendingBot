package com.dremanovich.lendingbot.bot.strategies;

import com.dremanovich.lendingbot.api.IPoloniexApi;
import com.dremanovich.lendingbot.bot.CurrencyInformationItem;
import com.dremanovich.lendingbot.helpers.SettingsHelper;
import com.dremanovich.lendingbot.types.RateValue;
import org.apache.logging.log4j.Logger;

/**
 * Created by PavelDremanovich on 25.06.17.
 */
public class MaxLendingStrategy extends AverageLendingStrategy
{

    public MaxLendingStrategy(Logger log, IPoloniexApi api, SettingsHelper settings) {
        super(log, api, settings);
    }

    @Override
    protected RateValue calculateRate(CurrencyInformationItem item) {
        //Calculate the interest rate based on the max value based on the first n items,
        // don't apply a reduction factor
        return item.calculateMaxRateByOffers(
                settings.getCountOffersForAverageCalculating()
        );
    }
}
