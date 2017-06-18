package com.dremanovich.leadingbot.bot.calculators;

import com.dremanovich.leadingbot.api.entities.OfferEntity;
import com.dremanovich.leadingbot.types.RateValue;

import java.util.List;

/**
 * Created by PavelDremanovich on 18.06.17.
 */
public class Calculator implements ICalculator{

    public RateValue calculateMinRateByOffers(List<OfferEntity> offers, int count){
        RateValue min = new RateValue("0");

        if (offers != null){
            int length = (offers.size() > count) ? count : offers.size();

            for (int i = 0; i < length; i++){
                OfferEntity offer = offers.get(i);
                if (offer != null){
                    if (i == 0){min = offer.getRate();}

                    if (min.compareTo(offer.getRate()) > 0){
                        min = offer.getRate();
                    }
                }
            }
        }

        return min;
    }

    public RateValue calculateMaxRateByOffers(List<OfferEntity> offers, int count){
        RateValue max = new RateValue("0");

        if (offers != null){
            int length = (offers.size() > count) ? count : offers.size();

            for (int i = 0; i < length; i++) {
                OfferEntity offer = offers.get(i);
                if (offer != null && max.compareTo(offer.getRate()) < 0) {
                    max = offer.getRate();
                }
            }
        }

        return max;
    }

    public RateValue calculateAverageRateByOffers(List<OfferEntity> offers, int count){
        RateValue average = new RateValue("0");

        if (offers != null){
            int length = (offers.size() > count) ? count : offers.size();

            for (int i = 0; i < length; i++){
                OfferEntity offer = offers.get(i);
                if (offer != null){
                    average = average.add(offer.getRate());
                }
            }

            average = average.divide(count);
        }


        return average;
    }
}
