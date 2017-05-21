package com.dremanovich.leadingbot.bot.strategies;

import com.dremanovich.leadingbot.api.IPoloniexApi;
import com.dremanovich.leadingbot.api.entities.CreatedLoanOfferResponseEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleLendingStrategy implements IPoloniexBotLendingStrategy {

    private Properties currencies;
    private int daysLending;
    private double minimizePercent = 0;
    private IPoloniexApi api;

    public SimpleLendingStrategy(IPoloniexApi api, Properties currencies, int daysLending, double percent) {
        this(api, currencies,daysLending);
        this.minimizePercent = percent;
    }

    public SimpleLendingStrategy(IPoloniexApi api, Properties currencies, int daysLending) {
        this.api = api;
        this.currencies = currencies;
        this.daysLending = daysLending;
    }

    @Override
    public void start(
            ConcurrentHashMap<String, BigDecimal> currentAverageOfferRate,
            ConcurrentHashMap<String, BigDecimal> currentAvailableBalance
    ) {
        for (Object currencyValue : currencies.values()) {
            try {
                String currency = (String)currencyValue;

                if (
                        currentAvailableBalance.containsKey(currency) &&
                        currentAverageOfferRate.containsKey(currency) &&
                        currentAvailableBalance.get(currency).compareTo(BigDecimal.ZERO) > 0
                    ){
                        double lendingRate = currentAverageOfferRate.get(currency).doubleValue();

                        System.out.println("Average offer rate:" + currentAverageOfferRate.get(currency));

                        if (minimizePercent > 0.0){
                            double percentValue = (lendingRate / 100) * minimizePercent;
                            lendingRate -= percentValue;
                            lendingRate = new BigDecimal(lendingRate).setScale(8, RoundingMode.HALF_EVEN).doubleValue();
                        }

                        System.out.println("Recalculated offer rate:" + new BigDecimal(lendingRate).setScale(8, RoundingMode.HALF_EVEN));

                        //TODO: logger

                        api.createLoanOffer(
                                currency,
                                currentAvailableBalance.get(currency).doubleValue(),
                                daysLending,
                                0,
                                lendingRate
                        ).enqueue(new CreatedLoanOfferListener());
                }

            } catch (ClassCastException e){
                e.printStackTrace();
            }
        }

        System.out.println("Strategy done!");
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
