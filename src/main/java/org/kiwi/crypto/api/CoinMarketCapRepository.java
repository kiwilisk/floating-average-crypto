package org.kiwi.crypto.api;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.kiwi.crypto.data.Currency;
import org.kiwi.crypto.data.CurrencyMapper;
import org.kiwi.rest.RestClient;
import org.kiwi.rest.Unirest;

import java.util.Collection;

public class CoinMarketCapRepository implements CurrencyRepository {

    private final RestClient restClient;
    private final CurrencyMapper<String> currencyMapper;
    private final String endpoint;

    @Inject
    CoinMarketCapRepository(@Unirest RestClient restClient, @CoinMarketCap CurrencyMapper<String> currencyMapper, @Named(value = "crypto.coin.market.cap.endpoint") String endpoint) {
        this.restClient = restClient;
        this.currencyMapper = currencyMapper;
        this.endpoint = endpoint;
    }

    @Override
    public Collection<Currency> retrieveCurrencies() {
        try {
            String response = restClient.getGetResponseFrom(endpoint);
            return currencyMapper.map(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load crypto currencies with " + this.getClass().getSimpleName(), e);
        }
    }
}
