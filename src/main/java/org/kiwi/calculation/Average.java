package org.kiwi.calculation;


import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.CEILING;

import java.math.BigDecimal;
import java.util.Collection;

public class Average {

    public BigDecimal calculateFor(Collection<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sumOfCurrencies = sum(values);
        BigDecimal amountOfCurrencies = new BigDecimal(values.size());
        return sumOfCurrencies.divide(amountOfCurrencies, CEILING);
    }

    private BigDecimal sum(Collection<BigDecimal> values) {
        return values.stream()
                .reduce(ZERO, BigDecimal::add);
    }
}
