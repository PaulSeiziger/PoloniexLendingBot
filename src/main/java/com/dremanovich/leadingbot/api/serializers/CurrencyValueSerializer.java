package com.dremanovich.leadingbot.api.serializers;

import com.dremanovich.leadingbot.types.CurrencyValue;
import com.google.gson.*;

import java.lang.reflect.Type;


public class CurrencyValueSerializer implements JsonDeserializer<CurrencyValue>, JsonSerializer<CurrencyValue> {
    @Override
    public CurrencyValue deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new CurrencyValue(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(CurrencyValue currencyValue, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(currencyValue.toString());
    }
}
