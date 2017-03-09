package org.kiwi.crypto.currency;


import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.CEILING;

import java.math.BigDecimal;
import java.util.Collection;

public class PriceAverage {

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
