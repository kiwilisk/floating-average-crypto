package org.kiwi.calculation;

import static java.util.stream.Collectors.toList;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.newBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage.Builder;
import org.kiwi.proto.FloatingAverageProtos.Quote;

class HistoricalQuotesAverage {

    private final Currency currency;
    private final FloatingAverage floatingAverage;

    HistoricalQuotesAverage(Currency currency, FloatingAverage floatingAverage) {
        this.currency = currency;
        this.floatingAverage = floatingAverage;
    }

    FloatingAverage calculate() {
        BigDecimal latestValue = currency.priceInUsDollar();
        long dateInEpochSeconds = currency.lastUpdated().getEpochSecond();
        BigDecimal latestAverage = calculateLatestAverageWith(latestValue);
        Quote latestQuote = createQuoteWith(latestValue, latestAverage, dateInEpochSeconds);
        AlertState state = new AverageAlert().evaluateStateWith(latestAverage, latestValue, new BigDecimal("4.0"));

        return getBuilder()
                .setClosingDate(dateInEpochSeconds)
                .setCurrentAverage(latestAverage.toPlainString())
                .addQuotes(latestQuote)
                .setAlertState(state)
                .build();
    }

    private BigDecimal calculateLatestAverageWith(BigDecimal latestValue) {
        Collection<Quote> historicalQuotes = getHistoricalQuotes();
        List<BigDecimal> quoteValues = historicalQuotes.stream()
                .map(quote -> new BigDecimal(quote.getValue()))
                .collect(toList());
        quoteValues.add(latestValue);
        return new Average().calculateFor(quoteValues);
    }

    private Collection<Quote> getHistoricalQuotes() {
        return floatingAverage != null ? floatingAverage.getQuotesList() : new ArrayList<>();
    }

    private Quote createQuoteWith(BigDecimal latestValue, BigDecimal latestAverage, long dateInEpochSeconds) {
        return Quote.newBuilder()
                .setAverage(latestAverage.toPlainString())
                .setValue(latestValue.toPlainString())
                .setUpdatedAt(dateInEpochSeconds)
                .build();
    }

    private Builder getBuilder() {
        return floatingAverage != null
                ? newBuilder(floatingAverage)
                : newBuilder()
                        .setId(currency.id())
                        .setSymbol(currency.symbol())
                        .setName(currency.name());
    }
}
