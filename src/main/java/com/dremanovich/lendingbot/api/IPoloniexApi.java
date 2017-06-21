package com.dremanovich.lendingbot.api;

import com.dremanovich.lendingbot.api.entities.*;
import com.dremanovich.lendingbot.retrofit.annotations.PostParameter;
import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;
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
            @Field("amount") CurrencyValue amount,
            @Field("duration") int duration,
            @Field("autoRenew") int autoRenew,
            @Field("lendingRate") RateValue lendingRate
    );

    @FormUrlEncoded
    @POST("/tradingApi")
    @PostParameter(key = "command", value = "returnOpenLoanOffers")
    Call<Map<String, List<OpenedLoanOfferEntity>>> getOpenedLoanOffers(@Field("dumb") int dumb);

    @FormUrlEncoded
    @POST("/tradingApi")
    @PostParameter(key = "command", value = "cancelLoanOffer")
    Call<CanceledLoanOfferResponseEntity> cancelLoanOffer(@Field("orderNumber") int orderNumber);
}
