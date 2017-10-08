package com.dremanovich.lendingbot.bot.listeners;

import com.dremanovich.lendingbot.bot.CurrencyInformationItem;
import com.dremanovich.lendingbot.bot.CurrencyInformationIterator;
import com.dremanovich.lendingbot.helpers.SettingsHelper;
import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;

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

    public LoggerAverageStatisticListener(Logger log) {
        this.log = log;

        log.trace(averagesMarker, "День;Дата и время;Валюта;Среднее;Минимальное;Максимальное");
    }

    @Override
    public void onStart(CurrencyInformationIterator information) {
        if (information == null){
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        DateFormat dateDayInWeekFormat = new SimpleDateFormat("E");
        Date date = new Date();

        String averageString = dateDayInWeekFormat.format(date) + ';';

        averageString += dateFormat.format(date) + ';';

        while (information.hasNext()) {
            CurrencyInformationItem item = information.next();
            averageString = item.calculateAverageRateByOffers(averageItemCount).toPercentString() + ";" +
                    item.calculateMinRateByOffers(averageItemCount).toPercentString() + ";" +
                    item.calculateMaxRateByOffers(averageItemCount).toPercentString();
        }

        log.trace(averagesMarker, averageString);
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
        //Not implemented
    }

    @Override
    public void onClosedOffer(boolean success, int id, String infoMessage) {
        //Not implemented
    }

}
