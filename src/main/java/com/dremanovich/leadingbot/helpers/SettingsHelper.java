package com.dremanovich.leadingbot.helpers;



import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by PavelDremanovich on 04.06.17.
 */
public class SettingsHelper {

    private static final Logger log = LogManager.getLogger(SettingsHelper.class);

    private JsonObject settingsAsJsonObject;

    private List<String> currenciesCache;
    private Map<String, BigDecimal> minimumThresholdsCache;

    public SettingsHelper(String fileName) {
        try (FileReader reader = new FileReader(fileName)){

            JsonParser parser = new JsonParser();
            JsonElement settings = parser.parse(reader);

            settingsAsJsonObject = settings.getAsJsonObject();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    //TODO: Create Entity object

    public List<String> getCurrencies() {

        if (currenciesCache != null){return currenciesCache;}

        if (settingsAsJsonObject != null){
            JsonArray arrayElement = settingsAsJsonObject.getAsJsonArray("currencies");

            if (arrayElement != null){
                List<String> currencies = new ArrayList<>();

                for (JsonElement currencyElement : arrayElement) {
                    currencies.add(currencyElement.getAsString());
                }

                currenciesCache = currencies;

                return  currencies;
            }
        }

        return new ArrayList<>();
    }

    public Map<String, BigDecimal> getMinimumThresholds() {
        if (minimumThresholdsCache != null){return minimumThresholdsCache;}

        if (settingsAsJsonObject != null){
            JsonObject strategyObject = settingsAsJsonObject.getAsJsonObject("")
        }

        return new HashMap<>();
    }
}
