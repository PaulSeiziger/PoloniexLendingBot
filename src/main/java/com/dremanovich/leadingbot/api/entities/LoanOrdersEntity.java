
package com.dremanovich.leadingbot.api.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LoanOrdersEntity {

    private List<Offer> offers = null;
    private List<Demand> demands = null;

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    public List<Demand> getDemands() {
        return demands;
    }

    public void setDemands(List<Demand> demands) {
        this.demands = demands;
    }

    public BigDecimal getAverageOfferRate(){
        double rate = 0d;
        if (offers != null){
            for (Offer offer : offers) {
                rate += offer.getRate();
            }

            if (offers.size() > 0){
                rate /= offers.size();
            }
        }

        return new BigDecimal(rate).setScale(8, RoundingMode.HALF_EVEN);
    }

}
