package org.kiwi.calculation;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.newBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState;
import org.kiwi.proto.FloatingAverageProtos.Quote;

class FloatingAverageCalculator {

    FloatingAverage calculate(Currency currency, FloatingAverage floatingAverage) {
        BigDecimal latestValue = currency.priceInUsDollar();
        long dateInEpochSeconds = currency.lastUpdated().getEpochSecond();
        Collection<Quote> historicalQuotes = getHistoricalQuotes(floatingAverage);
        BigDecimal latestAverage = calculateLatestAverageWith(latestValue, historicalQuotes);
        Quote latestQuote = createQuoteWith(latestValue, latestAverage, dateInEpochSeconds);
        historicalQuotes.add(latestQuote);
        AlertState state = new AverageAlertState().evaluateStateWith(latestAverage, latestValue, new BigDecimal("4.0"));

        return newBuilder()
                .setId(currency.id())
                .setSymbol(currency.symbol())
                .setName(currency.name())
                .setClosingDate(dateInEpochSeconds)
                .setCurrentAverage(latestAverage.toPlainString())
                .addAllQuotes(historicalQuotes)
                .setAlertState(state)
                .build();
    }

    private BigDecimal calculateLatestAverageWith(BigDecimal latestValue, Collection<Quote> historicalQuotes) {
        List<BigDecimal> quoteValues = historicalQuotes.stream()
                .map(quote -> new BigDecimal(quote.getValue()))
                .collect(toList());
        quoteValues.add(latestValue);
        return new Average().calculateFor(quoteValues);
    }

    private Collection<Quote> getHistoricalQuotes(FloatingAverage floatingAverage) {
        if (floatingAverage != null) {
            List<Quote> quotesList = new ArrayList<>(floatingAverage.getQuotesList());
            boolean isMaxDaysCapReached =
                    quotesList.size() != 0 && quotesList.size() == floatingAverage.getMaxDaysCap();
            if (isMaxDaysCapReached) {
                removeOldestQuoteFrom(quotesList);
            }
            return quotesList;
        }
        return new ArrayList<>();
    }

    private Quote createQuoteWith(BigDecimal latestValue, BigDecimal latestAverage, long dateInEpochSeconds) {
        return Quote.newBuilder()
                .setAverage(latestAverage.toPlainString())
                .setValue(latestValue.toPlainString())
                .setUpdatedAt(dateInEpochSeconds)
                .build();
    }

    private void removeOldestQuoteFrom(List<Quote> quotesList) {
        quotesList.sort(comparingLong(Quote::getUpdatedAt));
        quotesList.remove(0);
    }
}