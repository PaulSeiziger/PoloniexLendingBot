package com.dremanovich.leadingbot.bot;

import com.dremanovich.leadingbot.api.Accounts;
import com.dremanovich.leadingbot.api.IPoloniexApi;
import com.dremanovich.leadingbot.api.NonceReminder;
import com.dremanovich.leadingbot.api.entities.AvailableAccountBalances;
import com.dremanovich.leadingbot.api.entities.CompleteBalanceEntity;
import com.dremanovich.leadingbot.api.entities.LoanOrdersEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Consumer;


public class AggregatorPoloniexBot implements AutoCloseable {
    private IPoloniexApi api;

    private ConcurrentHashMap<String, BigDecimal> currentAverageOfferRate = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BigDecimal> currentAvailableBalance = new ConcurrentHashMap<>();

    private ScheduledExecutorService offerScannerService;

    private Callable<Void> callback;

     AggregatorPoloniexBot(IPoloniexApi api) {
        this.api = api;

        offerScannerService = Executors.newSingleThreadScheduledExecutor();
    }

     void aggregate(Properties currencies) {
        AverageOfferRateConsumer createAverageOfferTable = new AverageOfferRateConsumer();
        AvailableBalanceListener balanceListener = new AvailableBalanceListener();

        offerScannerService.scheduleWithFixedDelay(()->{
            createAverageOfferTable.accept(currencies);
            api.getAvailableAccountBalance(Accounts.LENDING).enqueue(balanceListener);
        },
        0,
        10,
        TimeUnit.SECONDS);
    }

    ConcurrentHashMap<String, BigDecimal> getCurrentAverageOfferRate() {
        return currentAverageOfferRate;
    }

    ConcurrentHashMap<String, BigDecimal> getCurrentAvailableBalance() {
        return currentAvailableBalance;
    }

    void setChangeCallback(Callable<Void> callback) {
        this.callback = callback;
    }

    @Override
    public void close() throws Exception {
        offerScannerService.shutdownNow();
    }


    private class AverageOfferRateConsumer implements Consumer<Properties>{

        @Override
        public void accept(Properties currencies) {
            for (Object currencyObject : currencies.values()) {
                try {
                    String currency = (String)currencyObject;
                    Response response = api.getLoanOrders(currency).execute();

                    LoanOrdersEntity loanOrdersEntity = (LoanOrdersEntity) response.body();
                    if (loanOrdersEntity != null){
                            currentAverageOfferRate.put(currency, loanOrdersEntity.getAverageOfferRate());
                    }

                } catch (ClassCastException| IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class AvailableBalanceListener implements Callback<AvailableAccountBalances>{
        @Override
        public void onResponse(Call<AvailableAccountBalances> call, Response<AvailableAccountBalances> response) {
            if (response.isSuccessful()) {
                AvailableAccountBalances balances = response.body();
                if (balances != null && balances.getLending() != null) {

                    Map<String, BigDecimal> lendingBalance = balances.getLending();
                    currentAvailableBalance.putAll(lendingBalance);

                    //Notify about changes
                    if (callback != null){
                        try {
                            callback.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                System.out.println(response.raw().code());
            }
        }

        @Override
        public void onFailure(Call<AvailableAccountBalances> call, Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
