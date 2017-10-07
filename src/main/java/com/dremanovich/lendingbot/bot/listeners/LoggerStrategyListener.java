package com.dremanovich.lendingbot.bot.listeners;

import com.dremanovich.lendingbot.api.entities.OpenedLoanOfferEntity;
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


public class LoggerStrategyListener implements IPoloniexStrategyListener {

    private static final Marker financesMarker = new MarkerManager.Log4jMarker("FINANCE");
    private static final Marker statisticMarker = new MarkerManager.Log4jMarker("STATISTICS");


    private Logger log;
    private SettingsHelper settingsHelper;

    public LoggerStrategyListener(Logger log, SettingsHelper settingsHelper) {
        this.log = log;
        this.settingsHelper = settingsHelper;
    }

    @Override
    public void onStart(CurrencyInformationIterator information) {

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();

        log.trace(statisticMarker, "\r\nDate: " + dateFormat.format(date));
        log.trace(statisticMarker, "Balances:\r\n");

        while (information.hasNext()) {
            CurrencyInformationItem item = information.next();

            log.trace(statisticMarker, "Currency: " + item.getCurrencyName());
            if (item.getAvailableBalance() != null){
                log.trace(statisticMarker, "Balance: " + item.getAvailableBalance().toString());
            } else {
                log.trace(statisticMarker, "Balance: 0.0");
            }

            int countOffersForAverage = settingsHelper.getCountOffersForAverageCalculating();
            log.trace(
                statisticMarker,
                "Average offer: " + item.calculateAverageRateByOffers(
                        countOffersForAverage
                ).toPercentString()
            );


            log.trace(statisticMarker, "\r\n******************************************************************");


            for (OpenedLoanOfferEntity offer : item.getOpenedLoanOffers()){
                log.trace(statisticMarker, "Rate: " + offer.getRate().toPercentString() + "; Amount: " + offer.getAmount() + "; Duration: " + offer.getDuration() + "; Date" + offer.getDate() );
            }

            log.trace(statisticMarker, "=============================================================================\r\n");
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
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();

        log.trace(financesMarker, "Date: " + dateFormat.format(date));

        if (success){
            log.trace(
                    financesMarker,
                    "Opened offer: \n" +
                            "Currency:" + currencyName +
                            "; Lending Balance: " + lendingBalance +
                            "; Days: " + daysLending + "; " +
                            "Lending rate: " + lendingRate.toPercentString()
            );
        } else {
            String message = "Can't open offer by rate " + lendingRate.toPercentString() + " with amount " + lendingBalance ;
            if (infoMessage != null){
                message += "\nReason: " + infoMessage;
            }

            log.trace(financesMarker, message);
            log.trace(financesMarker, "______________________________________________________________________________\r\n");
            log.warn(message);
        }

    }

    @Override
    public void onClosedOffer(boolean success, int id, String infoMessage) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();

        log.trace(financesMarker, "Date: " + dateFormat.format(date));

        if (success){
            log.trace(financesMarker, "Close offer with id: " + id);
        } else {
            String message = "Can't close offer with id: " + id;
            if (infoMessage != null){
                message += "\nReason: " + infoMessage;
            }

            log.trace(financesMarker, message);
            log.trace(financesMarker, "______________________________________________________________________________\r\n");
            log.warn(message);
        }
    }

}
