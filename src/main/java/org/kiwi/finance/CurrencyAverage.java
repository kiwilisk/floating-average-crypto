package org.kiwi.finance;


import org.kiwi.crypto.data.Currency;

import java.math.BigDecimal;
import java.util.Collection;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.CEILING;

public class CurrencyAverage {

    public BigDecimal calculateFor(Collection<Currency> currencies) {
        if (currencies.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sumOfCurrencies = sum(currencies);
        BigDecimal amountOfCurrencies = new BigDecimal(currencies.size());
        return sumOfCurrencies.divide(amountOfCurrencies, CEILING);
    }

    private BigDecimal sum(Collection<Currency> currencies) {
        return currencies.stream()
                .map(Currency::priceInUsDollar)
                .reduce(ZERO, BigDecimal::add);
    }
}
