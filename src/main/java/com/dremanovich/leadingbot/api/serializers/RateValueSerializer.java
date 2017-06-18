package com.dremanovich.leadingbot.api.serializers;

import com.dremanovich.leadingbot.types.RateValue;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by PavelDremanovich on 18.06.17.
 */
public class RateValueSerializer implements JsonDeserializer<RateValue>, JsonSerializer<RateValue> {
    @Override
    public RateValue deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new RateValue(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(RateValue rateValue, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(rateValue.toString());
    }
}
