package com.dremanovich.lendingbot.bot;

import com.dremanovich.lendingbot.api.entities.OfferEntity;
import com.dremanovich.lendingbot.api.entities.OpenedLoanOfferEntity;
import com.dremanovich.lendingbot.types.CurrencyValue;
import com.dremanovich.lendingbot.types.RateValue;

import java.util.List;

public class CurrencyInformationItem {
    private String curencyName;
    private List<OpenedLoanOfferEntity> openedOffers;
    private CurrencyValue availableBalance;
    private List<OfferEntity> offers;

    public CurrencyInformationItem(String curencyName, List<OpenedLoanOfferEntity> openedOffers, CurrencyValue availableBalance, List<OfferEntity> offers) {
        this.curencyName = curencyName;
        this.openedOffers = openedOffers;
        this.availableBalance = availableBalance;
        this.offers = offers;
    }

    public String getCurrencyName(){return curencyName;}
    public List<OpenedLoanOfferEntity> getOpenedLoanOffers(){return openedOffers;}
    public CurrencyValue getAvailableBalance(){return availableBalance;}
    public List<OfferEntity> getOffers(){return offers;}

    public RateValue calculateMinRateByOffers(int count){
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

    public RateValue calculateMaxRateByOffers(int count){
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

    public RateValue calculateAverageRateByOffers(int count){
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
