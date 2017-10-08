package com.dremanovich.lendingbot.bot;

import com.dremanovich.lendingbot.api.IPoloniexApi;
import com.dremanovich.lendingbot.api.serializers.CurrencyValueSerializer;
import com.dremanovich.lendingbot.api.serializers.RateValueSerializer;
import com.dremanovich.lendingbot.bot.listeners.LoggerAverageStatisticListener;
import com.dremanovich.lendingbot.bot.listeners.LoggerStrategyListener;
import com.dremanovich.lendingbot.bot.strategies.IPoloniexBotLendingStrategy;
import com.dremanovich.lendingbot.helpers.SettingsHelper;
import com.dremanovich.lendingbot.retrofit.PostParameterCallAdapterFactory;
import com.dremanovich.lendingbot.retrofit.annotations.PostParameter;
import com.dremanovich.lendingbot.retrofit.interceptors.RequestPrinterInterceptor;
import com.dremanovich.lendingbot.retrofit.interceptors.PostParameterInterceptor;
import com.dremanovich.lendingbot.retrofit.interceptors.SignInterceptor;
import com.dremanovich.lendingbot.api.NonceReminder;
import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;

import org.apache.logging.log4j.Logger;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PoloniexBot {

    private NonceReminder reminder;

    private AggregatorPoloniexBot aggregator;

    private SettingsHelper settings;

    private IPoloniexBotLendingStrategy strategy;

    private Logger log;


    public PoloniexBot(Logger log, SettingsHelper settings,  NonceReminder reminder) throws IllegalArgumentException {
        this.reminder = reminder;
        this.settings = settings;
        this.log = log;


        Map<Integer, PostParameter> annotationRegistration = new HashMap<>();
        List<CallAdapter.Factory> factories = new ArrayList<>();

        factories.add(Retrofit2Platform.defaultCallAdapterFactory(null));

        PostParameterCallAdapterFactory callAdapterFactory =
                new PostParameterCallAdapterFactory(factories, annotationRegistration);

        SignInterceptor signInterceptor = new SignInterceptor(
                settings.getKey(),
                settings.getSecretKey(),
                reminder
        );

        PostParameterInterceptor postParameterInterceptor = new PostParameterInterceptor(annotationRegistration);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(settings.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(settings.getConnectTimeout(), TimeUnit.SECONDS)
                .addInterceptor(postParameterInterceptor)
                .addInterceptor(signInterceptor);

        if (settings.isPrintRequest()){
            RequestPrinterInterceptor requestPrinterInterceptor = new RequestPrinterInterceptor();
            clientBuilder.addInterceptor(requestPrinterInterceptor);
        }

        OkHttpClient client = clientBuilder.build();

        //Gson serializers
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RateValue.class, new RateValueSerializer())
                .registerTypeAdapter(CurrencyValue.class, new CurrencyValueSerializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(settings.getUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(callAdapterFactory)
                .client(client)
                .build();

        IPoloniexApi api = retrofit.create(IPoloniexApi.class);

        aggregator = new AggregatorPoloniexBot(log, api, settings.getRequestDelay());

        Class<IPoloniexBotLendingStrategy> loadedClass = settings.getStrategyClass();
        Constructor<?> ctor = null;
        try {

            ctor = loadedClass.getConstructor(Logger.class, IPoloniexApi.class, SettingsHelper.class);
            strategy = (IPoloniexBotLendingStrategy)ctor.newInstance(log, api, settings);
            strategy.addStrategyListener(new LoggerStrategyListener(log, settings));
            strategy.addStrategyListener(new LoggerAverageStatisticListener(log));

        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public void start() {

        aggregator.setChangeCallback((dto) -> {
            if (strategy != null){
                strategy.start(dto);
            }
        });

        aggregator.aggregate(settings.getCurrencies());

    }

    public void stop() {
        try {
            aggregator.close();
            reminder.close();
        } catch (Exception e) {
           log.error(e.getMessage(), e);
        }

    }

}