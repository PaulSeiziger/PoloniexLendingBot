package com.dremanovich.leadingbot.bot;

import com.dremanovich.leadingbot.api.Accounts;
import com.dremanovich.leadingbot.api.IPoloniexApi;
import com.dremanovich.leadingbot.api.entities.AvailableAccountBalancesEntity;
import com.dremanovich.leadingbot.api.entities.LoanOrdersEntity;
import com.dremanovich.leadingbot.api.entities.OpenedLoanOfferEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Consumer;


public class AggregatorPoloniexBot implements AutoCloseable {
    private static final int API_WAIT_MILLIS = 170; //No more 6 requests per second

    private static final Logger log = LogManager.getLogger(AggregatorPoloniexBot.class);

    private int delaySeconds;

    private IPoloniexApi api;

    private ScheduledExecutorService scannerService;

    private Consumer<AggregatorDto> callback;

     AggregatorPoloniexBot(IPoloniexApi api, int delaySeconds) {
        this.api = api;

        if (delaySeconds < 1){
            this.delaySeconds = 1;
        }else {
            this.delaySeconds = delaySeconds;
        }

        scannerService = Executors.newSingleThreadScheduledExecutor();
    }

     void aggregate(List<String> currencies) {
         scannerService.scheduleWithFixedDelay(
                 ()->{
                     Map<String, LoanOrdersEntity> loanOrders = new HashMap<>();
                     Map<String, List<OpenedLoanOfferEntity>> openedLoanOffers = new HashMap<>();
                     AvailableAccountBalancesEntity availableAccountBalancesEntity = null;

                     try {
                         //Get loan orders
                         for (String currency : currencies) {


                                 Response<LoanOrdersEntity> loanOrdersEntityResponse = api.getLoanOrders(currency).execute();

                                 if ((loanOrdersEntityResponse != null) && (loanOrdersEntityResponse.isSuccessful())){
                                     loanOrders.put(currency, loanOrdersEntityResponse.body());
                                 }

                                 Thread.sleep(API_WAIT_MILLIS);
                         }

                        //Get balances by accounts
                        Response<AvailableAccountBalancesEntity> accountBalancesResponse = api.getAvailableAccountBalance(Accounts.LENDING).execute();

                         if ((accountBalancesResponse != null) && (accountBalancesResponse.isSuccessful())){
                             availableAccountBalancesEntity = accountBalancesResponse.body();
                         }

                         //Get opened offers
                         Response<Map<String, List<OpenedLoanOfferEntity>>> openedLoanOffersResponse = api.getOpenedLoanOffers(1).execute();

                         if ((openedLoanOffersResponse != null) && (openedLoanOffersResponse.isSuccessful())){
                             openedLoanOffers = openedLoanOffersResponse.body();
                         }


                         //Create DTO object
                         AggregatorDto dto = new AggregatorDto();
                         dto.setBalances(availableAccountBalancesEntity);
                         dto.setLoanOrders(loanOrders);
                         dto.setOpenedLoanOffers(openedLoanOffers);

                         //Send DTO to listener
                         if (callback != null){
                             callback.accept(dto);
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

    void setChangeCallback(Consumer<AggregatorDto> callback) {
        this.callback = callback;
    }

    @Override
    public void close() throws Exception {
        scannerService.shutdownNow();
    }
}
