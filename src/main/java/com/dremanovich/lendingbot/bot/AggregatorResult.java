package com.dremanovich.lendingbot.bot;

import com.dremanovich.lendingbot.api.entities.AvailableAccountBalancesEntity;
import com.dremanovich.lendingbot.api.entities.LoanOrdersEntity;
import com.dremanovich.lendingbot.api.entities.OpenedLoanOfferEntity;
import com.dremanovich.lendingbot.types.CurrencyValue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AggregatorResult {
    private Map<String, LoanOrdersEntity> loanOrdersList;
    private AvailableAccountBalancesEntity balances ;
    private Map<String, List<OpenedLoanOfferEntity>> openedOffer;

    public AggregatorResult(Map<String, LoanOrdersEntity> loanOrdersList, AvailableAccountBalancesEntity availableAccountBalancesEntity, Map<String, List<OpenedLoanOfferEntity>> openedOffer) {
        this.loanOrdersList = loanOrdersList;
        this.balances = availableAccountBalancesEntity;
        this.openedOffer = openedOffer;
    }

    public CurrencyInformationIterator getIterator()
    {
        return new CurrencyInformationIterator(
                loanOrdersList,
                balances,
                openedOffer
        );
    }
}
