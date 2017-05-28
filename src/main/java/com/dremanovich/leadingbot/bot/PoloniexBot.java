package com.dremanovich.leadingbot.bot;

import com.dremanovich.leadingbot.Main;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PoloniexBot {
    private static final String BASE_URL_PROPERTY_NAME = "poloniex.bot.base_url";
    private static final String KEY_PROPERTY_NAME = "poloniex.bot.key";
    private static final String SECRET_PROPERTY_NAME = "poloniex.bot.secret";
    private static final String REQUEST_DELAY_SECONDS_PROPERTY_NAME = "poloniex.bot.request_delay_seconds";
    private static final String PRINT_REQUEST_PROPERTY_NAME = "poloniex.bot.print_queries";


    static final Logger log = LogManager.getLogger(PoloniexBot.class);

    private static final int CONNECT_TIMEOUT = 20;

    private NonceReminder reminder;

    private AggregatorPoloniexBot aggregator;

    private Properties currencies;

    private IPoloniexBotLendingStrategy strategy;


    public PoloniexBot(Properties properties, Properties currencies, Properties strategyProperties,  NonceReminder reminder) throws IllegalArgumentException {

        if (!properties.containsKey(BASE_URL_PROPERTY_NAME)) {
            throw new IllegalArgumentException("Not found \"" + BASE_URL_PROPERTY_NAME + "\" property!");
        }

        if (!properties.containsKey(KEY_PROPERTY_NAME)) {
            throw new IllegalArgumentException("Not found \"" + KEY_PROPERTY_NAME + "\" property!");
        }

        if (!properties.containsKey(SECRET_PROPERTY_NAME)) {
            throw new IllegalArgumentException("Not found \"" + SECRET_PROPERTY_NAME + "\" property!");
        }

        if (!properties.containsKey(REQUEST_DELAY_SECONDS_PROPERTY_NAME)) {
            throw new IllegalArgumentException("Not found \"" + REQUEST_DELAY_SECONDS_PROPERTY_NAME + "\" property!");
        }

        if (!properties.containsKey(PRINT_REQUEST_PROPERTY_NAME)) {
            throw new IllegalArgumentException("Not found \"" + PRINT_REQUEST_PROPERTY_NAME + "\" property!");
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
                properties.getProperty(KEY_PROPERTY_NAME),
                properties.getProperty(SECRET_PROPERTY_NAME),
                reminder
        );


        PostParameterInterceptor postParameterInterceptor = new PostParameterInterceptor(annotationRegistration);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(postParameterInterceptor)
                .addInterceptor(signInterceptor);

        if (properties.getProperty(PRINT_REQUEST_PROPERTY_NAME).equalsIgnoreCase("true")){
            RequestPrinterInterceptor requestPrinterInterceptor = new RequestPrinterInterceptor();
            clientBuilder.addInterceptor(requestPrinterInterceptor);
        }

        OkHttpClient client = clientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(properties.getProperty(BASE_URL_PROPERTY_NAME))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(callAdapterFactory)
                .client(client)
                .build();



        IPoloniexApi api = retrofit.create(IPoloniexApi.class);

        int delay = Integer.parseInt(properties.getProperty(REQUEST_DELAY_SECONDS_PROPERTY_NAME));
        aggregator = new AggregatorPoloniexBot(api, delay);

        //TODO: Dependency injection
        strategy = new SimpleLendingStrategy(api,currencies, strategyProperties);

    }

    public void start() {

        aggregator.setChangeCallback((dto) -> {
            if (strategy != null){
                strategy.start(dto);
            }
        });

        aggregator.aggregate(currencies);

    }

    public void stop() {
        try {
            aggregator.close();
            reminder.close();
        } catch (Exception e) {
           log.error(e);
        }

    }

}