package com.dremanovich.leadingbot.bot;

import com.dremanovich.leadingbot.api.entities.AvailableAccountBalancesEntity;
import com.dremanovich.leadingbot.api.entities.LoanOrdersEntity;
import com.dremanovich.leadingbot.api.entities.OpenedLoanOfferEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by PavelDremanovich on 22.05.17.
 */
public class AggregatorDto {
    private AvailableAccountBalancesEntity balances;
    private Map<String, LoanOrdersEntity> loanOrders;
    private Map<String, List<OpenedLoanOfferEntity>> openedLoanOffers;

    public AvailableAccountBalancesEntity getBalances() {
        return balances;
    }

    public void setBalances(AvailableAccountBalancesEntity balances) {
        this.balances = balances;
    }

    public Map<String, LoanOrdersEntity> getLoanOrders() {
        return loanOrders;
    }

    public void setLoanOrders(Map<String, LoanOrdersEntity> loanOrders) {
        this.loanOrders = loanOrders;
    }

    public Map<String, List<OpenedLoanOfferEntity>> getOpenedLoanOffers() {
        return openedLoanOffers;
    }

    public void setOpenedLoanOffers(Map<String, List<OpenedLoanOfferEntity>> openedLoanOffers) {
        this.openedLoanOffers = openedLoanOffers;
    }
}
