package com.dremanovich.lendingbot.providers;

import com.dremanovich.lendingbot.api.IPoloniexApi;
import org.mockito.Mockito;

public class ApiDataProvider {
    public IPoloniexApi getEmptyBalancesApi() {
        IPoloniexApi api = Mockito.mock(IPoloniexApi.class);

    }
}
