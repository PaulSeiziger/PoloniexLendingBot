package com.dremanovich.leadingbot.bot;

import com.dremanovich.leadingbot.api.IPoloniexApi;
import com.dremanovich.leadingbot.retrofit.PostParameterCallAdapterFactory;
import com.dremanovich.leadingbot.retrofit.annotations.PostParameter;
import com.dremanovich.leadingbot.retrofit.interceptors.RequestPrinterInterceptor;
import com.dremanovich.leadingbot.retrofit.interceptors.PostParameterInterceptor;
import com.dremanovich.leadingbot.retrofit.interceptors.SignInterceptor;
import com.dremanovich.leadingbot.helpers.NonceReminder;
import okhttp3.OkHttpClient;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.*;

public class PoloniexBot {
    private static final String baseUrlPropertyName = "poloniex.bot.base_url";
    private static final String keyPropertyName = "poloniex.bot.key";
    private static final String secretPropertyName = "poloniex.bot.secret";

    private NonceReminder reminder;

    private Properties currencies;

    private AggregatorPoloniexBot aggregator;

    public PoloniexBot(Properties properties, NonceReminder reminder) {

        if (!properties.containsKey(baseUrlPropertyName)) {
            throw new IllegalArgumentException("Not found \"" + baseUrlPropertyName + "\" property!");
        }

        if (!properties.containsKey(keyPropertyName)) {
            throw new IllegalArgumentException("Not found \"" + keyPropertyName + "\" property!");
        }

        if (!properties.containsKey(secretPropertyName)) {
            throw new IllegalArgumentException("Not found \"" + secretPropertyName + "\" property!");
        }

        Map<Integer, PostParameter> annotationRegistration = new HashMap<>();
        List<CallAdapter.Factory> factories = new ArrayList<>();

        factories.add(Retrofit2Platform.defaultCallAdapterFactory(null));

        PostParameterCallAdapterFactory callAdapterFactory =
                new PostParameterCallAdapterFactory(factories, annotationRegistration);

        SignInterceptor signInterceptor = new SignInterceptor(
                properties.getProperty(keyPropertyName),
                properties.getProperty(secretPropertyName)
        );

        RequestPrinterInterceptor requestPrinterInterceptor = new RequestPrinterInterceptor();
        PostParameterInterceptor postParameterInterceptor = new PostParameterInterceptor(annotationRegistration);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(postParameterInterceptor)
                .addInterceptor(signInterceptor)
//                .addInterceptor(requestPrinterInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(properties.getProperty(baseUrlPropertyName))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(callAdapterFactory)
                .client(client)
                .build();

        this.reminder = reminder;
        long nonce = reminder.get();

        IPoloniexApi api = retrofit.create(IPoloniexApi.class);

        aggregator = new AggregatorPoloniexBot(nonce, api);
    }

    public void start(Properties currencies) {

        if (currencies == null || currencies.size() == 0){
            throw  new IllegalArgumentException("Not found currencies!");
        }

        this.currencies = currencies;

        aggregator.setChangeCallback(() -> {
            System.out.println(aggregator.getCurrentAvailableBalance());
            System.out.println(aggregator.getCurrentAverageOfferRate());
            return null;
        });

        aggregator.aggregate(currencies);

    }

    public void stop() {
        try {
            aggregator.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        reminder.save(aggregator.getNonce());
    }

}