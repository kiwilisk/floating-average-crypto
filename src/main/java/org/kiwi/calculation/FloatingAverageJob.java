package org.kiwi.calculation;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.kiwi.alert.Notification.NOTIFICATION_LIST;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import org.kiwi.alert.DeviationAlert;
import org.kiwi.aws.metrics.CloudWatchMetricsWriter;
import org.kiwi.crypto.api.CoinMarketCap;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.DepotRepository;
import org.kiwi.proto.FloatingAverageProtos.Depot;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class FloatingAverageJob {

    private final CurrencyRepository currencyRepository;
    private final DepotRepository depotRepository;
    private final FloatingAverageCalculator floatingAverageCalculator;
    private final DeviationAlert deviationAlert;
    private final CloudWatchMetricsWriter metricsWriter;
    private final String depotId;

    @Inject
    public FloatingAverageJob(@CoinMarketCap CurrencyRepository currencyRepository,
            DepotRepository depotRepository,
            FloatingAverageCalculator floatingAverageCalculator,
            DeviationAlert deviationAlert, CloudWatchMetricsWriter metricsWriter,
            @Named("crypto.depot.id") String depotId) {
        this.currencyRepository = currencyRepository;
        this.depotRepository = depotRepository;
        this.floatingAverageCalculator = floatingAverageCalculator;
        this.deviationAlert = deviationAlert;
        this.metricsWriter = metricsWriter;
        this.depotId = depotId;
    }

    public void execute() {
        Collection<Currency> currencies = metricsWriter.executeWithMetric(
                currencyRepository::retrieveCurrencies, "currencyAPILoad");

        Map<String, FloatingAverage> idToAverage = metricsWriter.executeWithMetric(
                this::loadAveragesAndGroupById, "s3AverageLoad");

        Collection<FloatingAverage> latestFloatingAverages =
                metricsWriter.executeWithMetric(() -> calculateLatestAveragesWith(currencies, idToAverage),
                        "averageCalculation");

        metricsWriter.executeWithMetric(() -> store(latestFloatingAverages), "s3AverageStore");

        alertForChangedState(idToAverage, latestFloatingAverages);
    }

    private Map<String, FloatingAverage> loadAveragesAndGroupById() {
        return depotRepository.load(depotId).getFloatingAveragesList().stream()
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
        Set<FloatingAverage> averagesWithChangedState = latestFloatingAverages.stream()
                .filter(isInNotifyList())
                .filter(alertStateHasChanged(idToAverage))
                .collect(toSet());
        if (!averagesWithChangedState.isEmpty()) {
            deviationAlert.alert(averagesWithChangedState);
        }
    }

    private Predicate<FloatingAverage> isInNotifyList() {
        return floatingAverage -> NOTIFICATION_LIST.contains(floatingAverage.getId());
    }

    private Predicate<FloatingAverage> alertStateHasChanged(Map<String, FloatingAverage> idToAverage) {
        return currentAverage -> {
            FloatingAverage previousAverage = idToAverage.get(currentAverage.getId());
            return previousAverage != null && previousAverage.getAlertState() != currentAverage.getAlertState();
        };
    }

    private void store(Collection<FloatingAverage> floatingAverages) {
        Depot depot = Depot.newBuilder()
                .setId(depotId)
                .addAllFloatingAverages(floatingAverages)
                .build();
        depotRepository.store(depot);
    }
}
