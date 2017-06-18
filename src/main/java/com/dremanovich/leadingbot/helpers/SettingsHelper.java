package com.dremanovich.leadingbot.helpers;



import com.dremanovich.leadingbot.settings.SettingsEntity;
import com.dremanovich.leadingbot.types.CurrencyValue;
import com.dremanovich.leadingbot.types.RateValue;
import com.dremanovich.leadingbot.api.serializers.RateValueSerializer;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by PavelDremanovich on 04.06.17.
 */
public class SettingsHelper {

    private static final Logger log = LogManager.getLogger(SettingsHelper.class);

    private SettingsEntity settings;

    public SettingsHelper(String fileName) {
        try (FileReader reader = new FileReader(fileName)){

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(RateValue.class, new RateValueSerializer())
                    .create();

            settings = gson.fromJson(reader, SettingsEntity.class);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getUrl() {
        if (settings == null || settings.getBot() == null){
            return "https://poloniex.com/";
        }

        return settings.getBot().getUrl();
    }

    public int getRequestDelay() {
        if (settings == null || settings.getBot() == null){
            return 120;
        }

        return settings.getBot().getRequestDelay();
    }

    public boolean isPrintRequest() {
        if (settings == null || settings.getBot() == null){
            return false;
        }

        return settings.getBot().isPrintRequest();
    }


    public String getTimezone() {
        if (settings == null || settings.getBot() == null){
            return "UTC";
        }

        return settings.getBot().getTimezone();
    }

    public int getConnectTimeout(){
        if (settings == null || settings.getBot() == null){
            return 30;
        }

        return settings.getBot().getConnectTimeout();
    }

    public String getSecretKey() {
        String secret = System.getenv("POLONIEX_SECRET");

        return (secret == null) ? "" : secret;
    }

    public String getKey() {
        String key = System.getenv("POLONIEX_KEY");

        return (key == null) ? "" : key;
    }

    public List<String> getCurrencies() {
        if (settings == null){
            return new ArrayList<>();
        }

        return settings.getCurrencies();
    }

    public double getAverageOfferMinimizingPercent(){
        if (settings == null || settings.getStrategy() == null){
            return 0.0;
        }

        return settings.getStrategy().getAverageOfferMinimizingPercent();
    }

    public int getCountOffersForAverageCalculating() {
        if (settings == null || settings.getStrategy() == null){
            return 10;
        }

        return settings.getStrategy().getCountOffersForAverageCalculating();
    }

    public int getLendingDays() {
        if (settings == null || settings.getStrategy() == null){
            return 2;
        }

        return settings.getStrategy().getLendingDays();
    }

    public int getWaitBeforeReopenOffer() {
        if (settings == null || settings.getStrategy() == null){
            return 300;
        }

        return settings.getStrategy().getWaitBeforeReopenOffer();
    }


    public Map<String, RateValue> getAverageOfferMinimumThresholds() {
        if (settings == null || settings.getStrategy() == null){
            return new HashMap<>();
        }

        return settings.getStrategy().getAverageOfferMinimumThresholds();
    }
}
