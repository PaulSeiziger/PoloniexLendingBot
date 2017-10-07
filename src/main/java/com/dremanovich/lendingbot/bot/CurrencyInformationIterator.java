package com.dremanovich.lendingbot.bot;

import com.dremanovich.lendingbot.api.entities.AvailableAccountBalancesEntity;
import com.dremanovich.lendingbot.api.entities.LoanOrdersEntity;
import com.dremanovich.lendingbot.api.entities.OfferEntity;
import com.dremanovich.lendingbot.api.entities.OpenedLoanOfferEntity;
import com.dremanovich.lendingbot.types.CurrencyValue;

import java.util.*;

public class CurrencyInformationIterator implements Iterator<CurrencyInformationItem>{
    private Map<String, LoanOrdersEntity> loanOrdersList;
    private Map<String, CurrencyValue> balances ;
    private Map<String, List<OpenedLoanOfferEntity>> openedOffer;
    private Iterator<String> iterator;

    public CurrencyInformationIterator(Map<String, LoanOrdersEntity> loanOrdersList, AvailableAccountBalancesEntity availableAccountBalancesEntity, Map<String, List<OpenedLoanOfferEntity>> openedOffer) {
        this.loanOrdersList = loanOrdersList;
        this.balances = availableAccountBalancesEntity.getLending();
        this.openedOffer = openedOffer;
        this.iterator = loanOrdersList.keySet().iterator();
    }


    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public CurrencyInformationItem next() {
        CurrencyInformationItem item = null;
        if (iterator.hasNext()){
            String currency = iterator.next();
            List<OfferEntity> offers = loanOrdersList.get(currency).getOfferEntities();
            List<OpenedLoanOfferEntity> openedOffers = openedOffer.get(currency);
            CurrencyValue balance = balances.get(currency);

            if (offers == null){
                offers = new ArrayList<>();
            }

            if (openedOffers == null){
                openedOffers = new ArrayList<>();
            }

            if (balance == null){
                balance = CurrencyValue.ZERO;
            }

            item = new CurrencyInformationItem(currency, openedOffers, balance, offers);
        }

        return item;
    }
}
