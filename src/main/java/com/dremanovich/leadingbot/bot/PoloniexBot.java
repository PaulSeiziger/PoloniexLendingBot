package com.dremanovich.leadingbot.bot;

import com.dremanovich.leadingbot.api.IPoloniexApi;
import com.dremanovich.leadingbot.bot.strategies.IPoloniexBotLendingStrategy;
import com.dremanovich.leadingbot.bot.strategies.SimpleLendingStrategy;
import com.dremanovich.leadingbot.retrofit.PostParameterCallAdapterFactory;
import com.dremanovich.leadingbot.retrofit.annotations.PostParameter;
import com.dremanovich.leadingbot.retrofit.interceptors.RequestPrinterInterceptor;
import com.dremanovich.leadingbot.retrofit.interceptors.PostParameterInterceptor;
import com.dremanovich.leadingbot.retrofit.interceptors.SignInterceptor;
import com.dremanovich.leadingbot.api.NonceReminder;
import okhttp3.OkHttpClient;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.*;

public class PoloniexBot {
    private static final String baseUrlPropertyName = "poloniex.bot.base_url";
    private static final String keyPropertyName = "poloniex.bot.key";
    private static final String secretPropertyName = "poloniex.bot.secret";
    private static final String requestDelaySecondsPropertyName = "poloniex.bot.request_delay_seconds";
    private static final String printRequestPropertyName = "poloniex.bot.print_queries";
    private static final String minimizeOfferPercentPropertyName = "poloniex.bot.minimize_offer_percent";

    private NonceReminder reminder;

    private AggregatorPoloniexBot aggregator;

    private Properties currencies;

    private IPoloniexBotLendingStrategy strategy;

    public PoloniexBot(Properties properties, Properties currencies,  NonceReminder reminder) {

        if (!properties.containsKey(baseUrlPropertyName)) {
            throw new IllegalArgumentException("Not found \"" + baseUrlPropertyName + "\" property!");
        }

        if (!properties.containsKey(keyPropertyName)) {
            throw new IllegalArgumentException("Not found \"" + keyPropertyName + "\" property!");
        }

        if (!properties.containsKey(secretPropertyName)) {
            throw new IllegalArgumentException("Not found \"" + secretPropertyName + "\" property!");
        }

        if (!properties.containsKey(requestDelaySecondsPropertyName)) {
            throw new IllegalArgumentException("Not found \"" + requestDelaySecondsPropertyName + "\" property!");
        }

        if (!properties.containsKey(printRequestPropertyName)) {
            throw new IllegalArgumentException("Not found \"" + printRequestPropertyName + "\" property!");
        }

        if (!properties.containsKey(minimizeOfferPercentPropertyName)) {
            throw new IllegalArgumentException("Not found \"" + minimizeOfferPercentPropertyName + "\" property!");
        }

        if (currencies == null || currencies.size() == 0){
            throw  new IllegalArgumentException("Not found currencies!");
        }

        this.reminder = reminder;
        this.currencies = currencies;


        Map<Integer, PostParameter> annotationRegistration = new HashMap<>();
        List<CallAdapter.Factory> factories = new ArrayList<>();

        factories.add(Retrofit2Platform.defaultCallAdapterFactory(null));

        PostParameterCallAdapterFactory callAdapterFactory =
                new PostParameterCallAdapterFactory(factories, annotationRegistration);

        SignInterceptor signInterceptor = new SignInterceptor(
                properties.getProperty(keyPropertyName),
                properties.getProperty(secretPropertyName),
                reminder
        );


        PostParameterInterceptor postParameterInterceptor = new PostParameterInterceptor(annotationRegistration);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(postParameterInterceptor)
                .addInterceptor(signInterceptor);

        if (properties.getProperty(printRequestPropertyName).equalsIgnoreCase("true")){
            RequestPrinterInterceptor requestPrinterInterceptor = new RequestPrinterInterceptor();
            clientBuilder.addInterceptor(requestPrinterInterceptor);
        }

        OkHttpClient client = clientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(properties.getProperty(baseUrlPropertyName))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(callAdapterFactory)
                .client(client)
                .build();



        IPoloniexApi api = retrofit.create(IPoloniexApi.class);

        int delay = Integer.parseInt(properties.getProperty(requestDelaySecondsPropertyName));
        aggregator = new AggregatorPoloniexBot(api, delay);

        //TODO: replace hardcode strategy
        int minimizePercent = Integer.parseInt(properties.getProperty(minimizeOfferPercentPropertyName));
        strategy = new SimpleLendingStrategy(api,currencies,2, minimizePercent);
    }

    public void start() {

        aggregator.setChangeCallback(() -> {
            strategy.start(
                    aggregator.getCurrentAverageOfferRate(),
                    aggregator.getCurrentAvailableBalance()
            );
            return null;
        });

        aggregator.aggregate(currencies);

    }

    public void stop() {
        try {
            aggregator.close();
            reminder.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}