package com.dremanovich.leadingbot.api;

import com.dremanovich.leadingbot.api.entities.*;
import com.dremanovich.leadingbot.retrofit.annotations.PostParameter;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface IPoloniexApi {
    @GET("/public?command=returnLoanOrders")
    Call<LoanOrdersEntity> getLoanOrders(@Query("currency") String currency);

    @FormUrlEncoded
    @POST("/tradingApi")
    @PostParameter(key = "command", value = "returnBalances")
    Call<Map<String, String>> getBalances();

    @FormUrlEncoded
    @POST("/tradingApi")
    @PostParameter(key = "command", value = "returnCompleteBalances")
    Call<Map<String, CompleteBalanceEntity>> getCompleteBalance(@Field("account") Accounts account);

    @FormUrlEncoded
    @POST("/tradingApi")
    @PostParameter(key = "command", value = "returnAvailableAccountBalances")
    Call<AvailableAccountBalancesEntity> getAvailableAccountBalance(@Field("account") Accounts account);

    @FormUrlEncoded
    @POST("/tradingApi")
    @PostParameter(key = "command", value = "createLoanOffer")
    Call<CreatedLoanOfferResponseEntity> createLoanOffer(
            @Field("currency") String currency,
            @Field("amount") double amount,
            @Field("duration") int duration,
            @Field("autoRenew") int autoRenew,
            @Field("lendingRate") double lendingRate
    );

    @FormUrlEncoded
    @POST("/tradingApi")
    @PostParameter(key = "command", value = "returnOpenLoanOffers")
    Call<Map<String, List<OpenedLoanOfferEntity>>> getOpenedLoanOffers(@Field("dumb") int dumb);
}
