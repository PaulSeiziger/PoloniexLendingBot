package com.dremanovich.leadingbot.api;

import com.dremanovich.leadingbot.api.entities.AvailableAccountBalances;
import com.dremanovich.leadingbot.api.entities.CompleteBalanceEntity;
import com.dremanovich.leadingbot.api.entities.LoanOrdersEntity;
import com.dremanovich.leadingbot.retrofit.annotations.PostParameter;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface IPoloniexApi {
    @GET("/public?command=returnLoanOrders")
    Call<LoanOrdersEntity> getLoanOrders(@Query("currency") String currency);

    @FormUrlEncoded
    @POST("/tradingApi")
    @PostParameter(key = "command", value = "returnBalances")
    Call<Map<String, String>> getBalances(@Field("nonce") long nonce);

    @FormUrlEncoded
    @POST("/tradingApi")
    @PostParameter(key = "command", value = "returnCompleteBalances")
    Call<Map<String, CompleteBalanceEntity>> getCompleteBalance(@Field("account") Accounts account, @Field("nonce") long nonce);

    @FormUrlEncoded
    @POST("/tradingApi")
    @PostParameter(key = "command", value = "returnAvailableAccountBalances")
    Call<AvailableAccountBalances> getAvailableAccountBalance(@Field("account") Accounts account, @Field("nonce") long nonce);
}
