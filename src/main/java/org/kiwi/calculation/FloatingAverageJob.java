package org.kiwi.calculation;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.google.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageRepository;

public class FloatingAverageJob {

    private final CurrencyRepository currencyRepository;
    private final FloatingAverageRepository floatingAverageRepository;
    private final FloatingAverageCalculator floatingAverageCalculator;

    @Inject
    public FloatingAverageJob(CurrencyRepository currencyRepository,
            FloatingAverageRepository floatingAverageRepository,
            FloatingAverageCalculator floatingAverageCalculator) {
        this.currencyRepository = currencyRepository;
        this.floatingAverageRepository = floatingAverageRepository;
        this.floatingAverageCalculator = floatingAverageCalculator;
    }

    public void execute() {
        Collection<Currency> currencies = currencyRepository.retrieveCurrencies();
        Map<String, FloatingAverage> idToAverage = loadAveragesAndGroupById(currencies);
        Collection<FloatingAverage> latestFloatingAverages = calculateLatestAveragesWith(currencies, idToAverage);
        floatingAverageRepository.store(latestFloatingAverages);
    }

    private Map<String, FloatingAverage> loadAveragesAndGroupById(Collection<Currency> currencies) {
        Set<String> idSet = currencies.stream()
                .map(Currency::id)
                .collect(toSet());
        return floatingAverageRepository.load(idSet).stream()
                .collect(toMap(FloatingAverage::getId, floatingAverage -> floatingAverage));
    }

    private Collection<FloatingAverage> calculateLatestAveragesWith(Collection<Currency> currencies,
            Map<String, FloatingAverage> idToAverage) {
        return currencies.parallelStream()
                .map(toLatestFloatingAverage(idToAverage))
                .collect(toSet());
    }

    private Function<Currency, FloatingAverage> toLatestFloatingAverage(
            Map<String, FloatingAverage> idToAverage) {
        return currency -> {
            FloatingAverage floatingAverage = idToAverage.get(currency.id());
            return floatingAverageCalculator.calculate(currency, floatingAverage);
        };
    }
}
