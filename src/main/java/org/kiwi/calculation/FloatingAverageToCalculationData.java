package org.kiwi.calculation;

import static java.util.stream.Collectors.toList;
import static org.kiwi.calculation.CalculationData.newCalculationData;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.Quote;

class FloatingAverageToCalculationData implements Function<FloatingAverage, CalculationData> {

    private final Currency currency;
    private final BigDecimal deviationThreshold;

    FloatingAverageToCalculationData(Currency currency, BigDecimal deviationThreshold) {
        this.currency = currency;
        this.deviationThreshold = deviationThreshold;
    }

    @Override
    public CalculationData apply(FloatingAverage floatingAverage) {
        BigDecimal currencyValue = currency.priceInUsDollar();
        long closingDateInEpochSeconds = currency.lastUpdated().getEpochSecond();
        BigDecimal newAverage = calculateNewAverageWith(currencyValue, floatingAverage.getQuotesList());
        return newCalculationData(floatingAverage, currencyValue, closingDateInEpochSeconds, newAverage,
                deviationThreshold);
    }

    private static BigDecimal calculateNewAverageWith(BigDecimal currentValue, List<Quote> quotes) {
        List<BigDecimal> quoteValues = quotes.stream()
                .map(quote -> new BigDecimal(quote.getValue()))
                .collect(toList());
        quoteValues.add(currentValue);
        return new Average().calculateFor(quoteValues);
    }
}
