package org.kiwi.calculation;


import static java.util.stream.Collectors.toList;

import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.Quote;
import org.kiwi.proto.FloatingAverageRepository;

public class FloatingAverageCalculator implements Calculator {

    private final FloatingAverageRepository floatingAverageRepository;

    @Inject
    public FloatingAverageCalculator(FloatingAverageRepository floatingAverageRepository) {
        this.floatingAverageRepository = floatingAverageRepository;
    }

    @Override
    public FloatingAverage calculate(Currency currency) {
        try {
            return floatingAverageRepository.load(currency.id(), currency.symbol())
                    .map(toNewFloatingAverageWith(currency))
                    .orElseThrow(RuntimeException::new);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate FloatingAverage for " + currency, e);
        }
    }

    private Function<FloatingAverage, FloatingAverage> toNewFloatingAverageWith(Currency currency) {
        return currentAverage -> {
            BigDecimal priceInUsDollar = currency.priceInUsDollar();
            BigDecimal average = calculateNewAverage(priceInUsDollar, currentAverage.getQuotesList());
            long closingDateInEpochSeconds = currency.lastUpdated().getEpochSecond();

            Quote quote = Quote.newBuilder()
                    .setValue(priceInUsDollar.toPlainString())
                    .setAverage(average.toPlainString())
                    .setUpdatedAt(closingDateInEpochSeconds)
                    .build();
            return FloatingAverage.newBuilder(currentAverage)
                    .setClosingDate(closingDateInEpochSeconds)
                    .setCurrentAverage(average.toPlainString())
                    .addQuotes(quote).build();
        };
    }

    private BigDecimal calculateNewAverage(BigDecimal currentValue, List<Quote> quotes) {
        List<BigDecimal> quoteValues = quotes.stream()
                .map(quote -> new BigDecimal(quote.getValue()))
                .collect(toList());
        quoteValues.add(currentValue);
        return new Average().calculateFor(quoteValues);
    }
}
