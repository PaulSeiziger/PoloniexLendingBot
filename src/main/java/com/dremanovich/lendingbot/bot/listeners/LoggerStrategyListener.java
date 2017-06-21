package com.dremanovich.lendingbot.bot.listeners;

import com.dremanovich.lendingbot.api.entities.OpenedLoanOfferEntity;
import com.dremanovich.lendingbot.bot.AggregatorDto;
import com.dremanovich.lendingbot.bot.calculators.ICalculator;
import com.dremanovich.lendingbot.helpers.SettingsHelper;
import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class LoggerStrategyListener implements IPoloniexStrategyListener {

    private static final Marker financesMarker = new MarkerManager.Log4jMarker("FINANCE");
    private static final Marker statisticMarker = new MarkerManager.Log4jMarker("STATISTICS");


    private Logger log;
    private SettingsHelper settingsHelper;
    private ICalculator calculator;

    public LoggerStrategyListener(Logger log, SettingsHelper settingsHelper, ICalculator calculator) {
        this.log = log;
        this.settingsHelper = settingsHelper;
        this.calculator = calculator;
    }

    @Override
    public void onStart(AggregatorDto information) {

        if(information == null){
            return;
        }

        if (information.getBalances().getLending() != null){
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date date = new Date();

            log.trace(statisticMarker, "\r\nDate: " + dateFormat.format(date));
            log.trace(statisticMarker, "Balances:\r\n");

            Map<String, CurrencyValue> lendingBalances = information.getBalances().getLending();

            for (String currency : settingsHelper.getCurrencies()) {

                log.trace(statisticMarker, "Currency: " + currency);
                if (lendingBalances.containsKey(currency)){
                    log.trace(statisticMarker, "Balance: " + lendingBalances.get(currency));
                } else {
                    log.trace(statisticMarker, "Balance: 0.0");
                }

                if (
                        information.getLoanOrders() != null &&
                                information.getLoanOrders().get(currency) != null
                    ){
                    int countOffersForAverage = settingsHelper.getCountOffersForAverageCalculating();
                    RateValue average = calculator.calculateAverageRateByOffers(
                            information.getLoanOrders().get(currency).getOfferEntities(),
                            countOffersForAverage
                    );

                    log.trace(statisticMarker, "Average offer: " + average.toPercentString());
                } else {
                    log.trace(statisticMarker, "No offers!");
                }

                log.trace(statisticMarker, "\r\n******************************************************************");


                List<OpenedLoanOfferEntity> openedLoanOffers = information.getOpenedLoanOffers().get(currency);
                if (openedLoanOffers != null){
                    for (OpenedLoanOfferEntity offer : openedLoanOffers){
                        log.trace(statisticMarker, "Rate: " + offer.getRate().toPercentString() + "; Amount: " + offer.getAmount() + "; Duration: " + offer.getDuration() + "; Date" + offer.getDate() );
                    }
                } else {
                    log.trace(statisticMarker, "No opened orders!");
                }
                log.trace(statisticMarker, "-----------------------------------------------------------------\r\n");
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
