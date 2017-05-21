package com.dremanovich.leadingbot.bot;

import com.dremanovich.leadingbot.api.Accounts;
import com.dremanovich.leadingbot.api.IPoloniexApi;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by PavelDremanovich on 21.05.17.
 */
public class AggregatorPoloniexBot implements AutoCloseable {
    private IPoloniexApi api;

    private ConcurrentHashMap<String, BigDecimal> currentAverageOfferRate = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BigDecimal> currentAvailableBalance = new ConcurrentHashMap<>();

    private ScheduledExecutorService offerScannerService;

    private long nonce = 1;

    private Callable<Void> callback;

     AggregatorPoloniexBot(long nonce, IPoloniexApi api) {
        this.nonce = nonce;
        this.api = api;

        offerScannerService = Executors.newSingleThreadScheduledExecutor();
    }

     void aggregate(Properties currencies) {
        AverageOfferRateConsumer createAverageOfferTable = new AverageOfferRateConsumer();
        BalanceListener balanceListener = new BalanceListener();

        offerScannerService.scheduleWithFixedDelay(()->{
            createAverageOfferTable.accept(currencies);
            api.getCompleteBalance(Accounts.ALL, nonce).enqueue(balanceListener);

            nonce++;
        },
        0,
        10,
        TimeUnit.SECONDS);
    }

    long getNonce() {
        return nonce;
    }

    ConcurrentHashMap<String, BigDecimal> getCurrentAverageOfferRate() {
        return currentAverageOfferRate;
    }

    ConcurrentHashMap<String, BigDecimal> getCurrentAvailableBalance() {
        return currentAvailableBalance;
    }

    void setChangeallback(Callable<Void> callback) {
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


    private class BalanceListener implements Callback<Map<String, CompleteBalanceEntity>>{
        @Override
        public void onResponse(Call<Map<String, CompleteBalanceEntity>> call, Response<Map<String, CompleteBalanceEntity>> response) {
            if (response.isSuccessful()) {
                Map<String, CompleteBalanceEntity> balances = response.body();
                if (balances != null) {
                    for (Map.Entry<String, CompleteBalanceEntity> balance : balances.entrySet()) {

                        try {
                            CompleteBalanceEntity balanceEntity = balance.getValue();
                            if (balanceEntity != null){
                                //TODO: Подумай куда запихнуть политику округления
                                BigDecimal balanceValue = new BigDecimal(balanceEntity.getAvailable()).setScale(8, RoundingMode.HALF_EVEN);
                                currentAvailableBalance.put(balance.getKey(), balanceValue);
                            }
                        }catch (NumberFormatException ex){
                            ex.printStackTrace();
                        }

                    }

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
        public void onFailure(Call<Map<String, CompleteBalanceEntity>> call, Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
