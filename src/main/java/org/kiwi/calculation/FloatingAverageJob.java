package org.kiwi.calculation;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.google.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import org.kiwi.alert.DeviationAlert;
import org.kiwi.crypto.api.CoinMarketCap;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageRepository;

public class FloatingAverageJob {

    private final CurrencyRepository currencyRepository;
    private final FloatingAverageRepository floatingAverageRepository;
    private final FloatingAverageCalculator floatingAverageCalculator;
    private final DeviationAlert deviationAlert;

    @Inject
    public FloatingAverageJob(@CoinMarketCap CurrencyRepository currencyRepository,
            FloatingAverageRepository floatingAverageRepository,
            FloatingAverageCalculator floatingAverageCalculator,
            DeviationAlert deviationAlert) {
        this.currencyRepository = currencyRepository;
        this.floatingAverageRepository = floatingAverageRepository;
        this.floatingAverageCalculator = floatingAverageCalculator;
        this.deviationAlert = deviationAlert;
    }

    public void execute() {
        Collection<Currency> currencies = currencyRepository.retrieveCurrencies();
        Map<String, FloatingAverage> idToAverage = loadAveragesAndGroupById(currencies);
        Collection<FloatingAverage> latestFloatingAverages = calculateLatestAveragesWith(currencies, idToAverage);
        floatingAverageRepository.store(latestFloatingAverages);
        alertForChangedState(idToAverage, latestFloatingAverages);
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

    private void alertForChangedState(Map<String, FloatingAverage> idToAverage,
            Collection<FloatingAverage> latestFloatingAverages) {
        latestFloatingAverages.stream()
                .filter(alertStateHasChanged(idToAverage))
                .forEach(deviationAlert::alert);
    }

    private Predicate<FloatingAverage> alertStateHasChanged(Map<String, FloatingAverage> idToAverage) {
        return currentAverage -> {
            FloatingAverage previousAverage = idToAverage.get(currentAverage.getId());
            return previousAverage != null && previousAverage.getAlertState() != currentAverage.getAlertState();
        };
    }
}
