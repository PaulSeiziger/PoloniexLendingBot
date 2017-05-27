
package com.dremanovich.leadingbot.api.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LoanOrdersEntity {

    private List<OfferEntity> offerEntities = null;
    private List<DemandEntity> demandEntities = null;

    public List<OfferEntity> getOfferEntities() {
        return offerEntities;
    }

    public void setOfferEntities(List<OfferEntity> offerEntities) {
        this.offerEntities = offerEntities;
    }

    public List<DemandEntity> getDemandEntities() {
        return demandEntities;
    }

    public void setDemandEntities(List<DemandEntity> demandEntities) {
        this.demandEntities = demandEntities;
    }

    public BigDecimal getAverageOfferRate(){
        double rate = 0d;
        if (offerEntities != null){
            for (OfferEntity offerEntity : offerEntities) {
                rate += offerEntity.getRate();
            }

            if (offerEntities.size() > 0){
                rate /= offerEntities.size();
            }
        }

        return new BigDecimal(rate).setScale(8, RoundingMode.HALF_EVEN);
    }

}
