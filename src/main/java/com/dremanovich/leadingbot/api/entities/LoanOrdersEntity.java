
package com.dremanovich.leadingbot.api.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LoanOrdersEntity {

    private List<OfferEntity> offers = null;
    private List<DemandEntity> demands = null;

    public List<OfferEntity> getOfferEntities() {
        return offers;
    }

    public void setOfferEntities(List<OfferEntity> offerEntities) {
        this.offers = offerEntities;
    }

    public List<DemandEntity> getDemandEntities() {
        return demands;
    }

    public void setDemandEntities(List<DemandEntity> demandEntities) {
        this.demands = demandEntities;
    }

}
