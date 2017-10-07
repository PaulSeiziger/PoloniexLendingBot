package com.dremanovich.lendingbot.bot;

import com.dremanovich.lendingbot.api.Accounts;
import com.dremanovich.lendingbot.api.IPoloniexApi;
import com.dremanovich.lendingbot.api.entities.AvailableAccountBalancesEntity;
import com.dremanovich.lendingbot.api.entities.LoanOrdersEntity;
import com.dremanovich.lendingbot.api.entities.OpenedLoanOfferEntity;
import org.apache.logging.log4j.Logger;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;


public class AggregatorPoloniexBot implements AutoCloseable {
    private static final int API_WAIT_MILLIS = 170; //No more 6 requests per second

    private Logger log;

    private int delaySeconds;

    private IPoloniexApi api;

    private ScheduledExecutorService scannerService;

    private Consumer<CurrencyInformationIterator> callback;

     AggregatorPoloniexBot(Logger log, IPoloniexApi api, int delaySeconds) {
        this.api = api;
        this.log = log;

        if (delaySeconds < 1){
            this.delaySeconds = 1;
        }else {
            this.delaySeconds = delaySeconds;
        }

        scannerService = Executors.newSingleThreadScheduledExecutor();
    }

     public void aggregate(List<String> currencies) {
         scannerService.scheduleWithFixedDelay(
                 ()->{
                     try {
                         //Create DTO object
                         CurrencyInformationIterator iterator = new CurrencyInformationIterator(
                                 getLoanOrders(currencies),
                                 getBalances(),
                                 getOpenedOffers()
                         );

                         //Send DTO to listener
                         if (callback != null){
                             callback.accept(iterator);
                         }

                     } catch (Exception e) {
                         log.debug(e.getMessage(), e);
                     }
                 },
            0,
            delaySeconds,
            TimeUnit.SECONDS
        );
    }

    public void setChangeCallback(Consumer<CurrencyInformationIterator> callback) {
        this.callback = callback;
    }

    @Override
    public void close() throws Exception {
        scannerService.shutdownNow();
    }


    protected Map<String, LoanOrdersEntity> getLoanOrders(List<String> currencies){
        Map<String, LoanOrdersEntity> loanOrders = new HashMap<>();

        //Get loan orders
        for (String currency : currencies) {
            Response<LoanOrdersEntity> loanOrdersEntityResponse = null;
            try {
                loanOrdersEntityResponse = api.getLoanOrders(currency).execute();

                if ((loanOrdersEntityResponse != null) && (loanOrdersEntityResponse.isSuccessful())){
                    loanOrders.put(currency, loanOrdersEntityResponse.body());
                }

                Thread.sleep(API_WAIT_MILLIS);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return loanOrders;
    }

    protected AvailableAccountBalancesEntity getBalances() throws IOException {
        AvailableAccountBalancesEntity availableAccountBalancesEntity = null;
            Response<AvailableAccountBalancesEntity> accountBalancesResponse = api.getAvailableAccountBalance(Accounts.LENDING).execute();

            if ((accountBalancesResponse != null) && (accountBalancesResponse.isSuccessful())){
                availableAccountBalancesEntity = accountBalancesResponse.body();
            }
        return availableAccountBalancesEntity;
    }

    protected Map<String, List<OpenedLoanOfferEntity>> getOpenedOffers() throws IOException {
        Map<String, List<OpenedLoanOfferEntity>> openedLoanOffers = new HashMap<>();
            Response<Map<String, List<OpenedLoanOfferEntity>>> openedLoanOffersResponse = api.getOpenedLoanOffers(1).execute();

            if ((openedLoanOffersResponse != null) && (openedLoanOffersResponse.isSuccessful())){
                openedLoanOffers = openedLoanOffersResponse.body();
            }

        return openedLoanOffers;
    }
}
