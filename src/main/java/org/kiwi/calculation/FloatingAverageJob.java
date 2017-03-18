package org.kiwi.calculation;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.apache.log4j.Logger;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState;
import org.kiwi.proto.FloatingAverageProtos.Quote;
import org.kiwi.proto.FloatingAverageRepository;

public class FloatingAverageJob {

    private final static Logger LOGGER = Logger.getLogger(FloatingAverageJob.class);

    private final CurrencyRepository currencyRepository;
    private final FloatingAverageRepository floatingAverageRepository;

    @Inject
    public FloatingAverageJob(CurrencyRepository currencyRepository,
            FloatingAverageRepository floatingAverageRepository) {
        this.currencyRepository = currencyRepository;
        this.floatingAverageRepository = floatingAverageRepository;
    }

    public void execute() {
        Collection<Currency> currencies = currencyRepository.retrieveCurrencies();
        Map<String, FloatingAverage> idToAverage = loadAveragesFor(currencies);
        List<FloatingAverage> latestFloatingAverages = calculateNewAveragesWith(currencies, idToAverage);
        store(latestFloatingAverages);
    }

    private List<FloatingAverage> calculateNewAveragesWith(Collection<Currency> currencies,
            Map<String, FloatingAverage> idToAverage) {
        return currencies.parallelStream()
                .map(toLatestFloatingAverageWith(idToAverage))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private Map<String, FloatingAverage> loadAveragesFor(Collection<Currency> currencies) {
        List<CompletableFuture<FloatingAverage>> loadAverageFutures = currencies.stream()
                .map(toLoadAverageFuture())
                .collect(toList());
        return loadAverageFutures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(toMap(FloatingAverage::getId, floatingAverage -> floatingAverage));
    }

    private Function<Currency, CompletableFuture<FloatingAverage>> toLoadAverageFuture() {
        return currency -> supplyAsync(() -> floatingAverageRepository.load(currency.id()))
                .exceptionally(throwable -> {
                    LOGGER.error("Failed to load FloatingAverage with id [" + currency.id() + "]");
                    return null;
                });
    }

    private Function<Currency, FloatingAverage> toLatestFloatingAverageWith(Map<String, FloatingAverage> idToAverage) {
        return currency -> {
            FloatingAverage floatingAverage = idToAverage.get(currency.id());
            if (floatingAverage != null) {
                BigDecimal latestValue = currency.priceInUsDollar();
                long dateInEpochSeconds = currency.lastUpdated().getEpochSecond();
                BigDecimal latestAverage = calculateAverageWith(latestValue, floatingAverage.getQuotesList());
                AlertState state = new AverageAlert()
                        .evaluateStateWith(latestAverage, latestValue, new BigDecimal("4.0"));
                Quote latestQuote = createQuoteWith(latestValue, latestAverage, dateInEpochSeconds);

                return FloatingAverage.newBuilder(floatingAverage)
                        .setClosingDate(dateInEpochSeconds)
                        .setCurrentAverage(latestAverage.toPlainString())
                        .addQuotes(latestQuote)
                        .setAlertState(state)
                        .build();
            } else {
                LOGGER.warn("Failed to retrieve FloatingAverage from data structure with id [" + currency.id()
                        + "]. Creating new.");
                BigDecimal latestValue = currency.priceInUsDollar();
                long dateInEpochSeconds = currency.lastUpdated().getEpochSecond();
                Quote latestQuote = createQuoteWith(latestValue, latestValue, dateInEpochSeconds);
                return FloatingAverage.newBuilder()
                        .setId(currency.id())
                        .setSymbol(currency.symbol())
                        .setName(currency.name())
                        .setClosingDate(dateInEpochSeconds)
                        .setCurrentAverage(latestValue.toPlainString())
                        .addQuotes(latestQuote)
                        .setAlertState(AlertState.NONE)
                        .build();
            }
        };
    }

    private static BigDecimal calculateAverageWith(BigDecimal latestValue, Collection<Quote> quotes) {
        List<BigDecimal> quoteValues = quotes.stream()
                .map(quote -> new BigDecimal(quote.getValue()))
                .collect(toList());
        quoteValues.add(latestValue);
        return new Average().calculateFor(quoteValues);
    }

    private Quote createQuoteWith(BigDecimal latestValue, BigDecimal latestAverage, long dateInEpochSeconds) {
        return Quote.newBuilder()
                .setAverage(latestAverage.toPlainString())
                .setValue(latestValue.toPlainString())
                .setUpdatedAt(dateInEpochSeconds)
                .build();
    }

    private void store(List<FloatingAverage> latestFloatingAverages) {
        List<CompletableFuture<Void>> storeAverageFutures = latestFloatingAverages.stream()
                .map(floatingAverage -> runAsync(() -> floatingAverageRepository.store(floatingAverage)))
                .collect(toList());
        storeAverageFutures.forEach(CompletableFuture::join);
    }
}
