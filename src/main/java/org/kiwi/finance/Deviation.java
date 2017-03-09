package org.kiwi.finance;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.CEILING;

import java.math.BigDecimal;

class Deviation {

    private final static BigDecimal HUNDRED = new BigDecimal("100");

    public BigDecimal calculate(BigDecimal currentAverage, BigDecimal currentClosingQuote) {
        if (currentClosingQuote == null || ZERO.equals(currentClosingQuote)) {
            return ZERO;
        }
        return calculateDeviationWith(currentAverage, currentClosingQuote);
    }

    private BigDecimal calculateDeviationWith(BigDecimal currentAverage, BigDecimal currentClosingQuote) {
        return currentAverage
                .multiply(HUNDRED)
                .divide(currentClosingQuote, CEILING)
                .subtract(HUNDRED)
                .abs();
    }

    private boolean isThresholdExceeded(BigDecimal deviation, BigDecimal percentageThreshold) {
        return deviation.compareTo(percentageThreshold) >= 0;
    }
}