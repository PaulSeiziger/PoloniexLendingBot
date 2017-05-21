package com.dremanovich.leadingbot.api;

import com.dremanovich.leadingbot.api.entities.AvailableAccountBalances;
import com.dremanovich.leadingbot.api.entities.CompleteBalanceEntity;
import com.dremanovich.leadingbot.api.entities.CreatedLoanOfferResponseEntity;
import com.dremanovich.leadingbot.api.entities.LoanOrdersEntity;
import com.dremanovich.leadingbot.retrofit.annotations.PostParameter;
import retrofit2.Call;
import retrofit2.http.*;

import java.math.BigDecimal;
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
    Call<AvailableAccountBalances> getAvailableAccountBalance(@Field("account") Accounts account);

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
}
