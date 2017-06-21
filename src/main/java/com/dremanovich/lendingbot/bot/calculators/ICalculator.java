package com.dremanovich.lendingbot.bot.calculators;

import com.dremanovich.lendingbot.api.entities.OfferEntity;
import com.dremanovich.lendingbot.types.RateValue;

import java.util.List;

/**
 * Created by PavelDremanovich on 18.06.17.
 */
public interface ICalculator {
    RateValue calculateMinRateByOffers(List<OfferEntity> offers, int count);
    RateValue calculateMaxRateByOffers(List<OfferEntity> offers, int count);
    RateValue calculateAverageRateByOffers(List<OfferEntity> offers, int count);
}
