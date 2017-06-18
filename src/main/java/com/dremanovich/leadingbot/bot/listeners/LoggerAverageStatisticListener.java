package com.dremanovich.leadingbot.bot.listeners;

import com.dremanovich.leadingbot.bot.AggregatorDto;
import com.dremanovich.leadingbot.bot.calculators.ICalculator;
import com.dremanovich.leadingbot.helpers.SettingsHelper;
import com.dremanovich.leadingbot.types.CurrencyValue;
import com.dremanovich.leadingbot.types.RateValue;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LoggerAverageStatisticListener implements IPoloniexStrategyListener {
    private static final Marker averagesMarker = new MarkerManager.Log4jMarker("AVERAGES");
    private static final int averageItemCount = 20;


    private Logger log;
    private SettingsHelper settingsHelper;
    private ICalculator calculator;

    public LoggerAverageStatisticListener(Logger log, SettingsHelper settingsHelper, ICalculator calculator) {
        this.log = log;
        this.settingsHelper = settingsHelper;
        this.calculator = calculator;
    }

    @Override
    public void onStart(AggregatorDto information) {
        if (information == null){
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        DateFormat dateDayInWeekFormat = new SimpleDateFormat("E");
        Date date = new Date();

        String averageString = dateDayInWeekFormat.format(date) + ';';

        averageString += dateFormat.format(date) + ';';

        for (String currency : settingsHelper.getCurrencies()) {
            averageString += currency + ';';

            if ((information.getLoanOrders() != null) && (information.getLoanOrders().get(currency) != null)){
                RateValue average = calculator.calculateAverageRateByOffers(
                        information.getLoanOrders().get(currency).getOfferEntities(),
                        averageItemCount
                );

                RateValue min = calculator.calculateMinRateByOffers(
                        information.getLoanOrders().get(currency).getOfferEntities(),
                        averageItemCount
                );

                RateValue max = calculator.calculateMaxRateByOffers(
                        information.getLoanOrders().get(currency).getOfferEntities(),
                        averageItemCount
                );

                averageString += average + ";" + min + ";" + max;
            }

            log.trace(averagesMarker, averageString);

        }
    }

    @Override
    public void onOpenedOffer(
            boolean success,
            String currencyName,
            CurrencyValue lendingBalance,
            int daysLending,
            RateValue lendingRate,
            String infoMessage
    ) {

    }

    @Override
    public void onClosedOffer(boolean success, int id, String infoMessage) {

    }

}
