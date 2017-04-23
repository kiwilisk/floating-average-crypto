package org.kiwi.calculation;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.kiwi.crypto.currency.Currency.newCurrency;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.newBuilder;
import static org.kiwi.proto.FloatingAverageTestData.createBitcoinTestData;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.alert.DeviationAlert;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState;
import org.kiwi.proto.FloatingAverageProtos.Quote;
import org.kiwi.proto.FloatingAverageRepository;

public class FloatingAverageJobTest {

    private static final Currency BITCOIN = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("1229.68"),
            LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC));
    private static final FloatingAverage BITCOIN_AVERAGE = createBitcoinTestData();

    private FloatingAverageJob job;
    private FloatingAverageRepository floatingAverageRepository;
    private FloatingAverageCalculator floatingAverageCalculator;
    private DeviationAlert deviationAlert;

    @Before
    public void setUp() throws Exception {
        CurrencyRepository currencyRepository = mock(CurrencyRepository.class);
        when(currencyRepository.retrieveCurrencies()).thenReturn(singleton(BITCOIN));
        floatingAverageRepository = mock(FloatingAverageRepository.class);
        when(floatingAverageRepository.load(singleton("bitcoin"))).thenReturn(singletonList(BITCOIN_AVERAGE));
        floatingAverageCalculator = mock(FloatingAverageCalculator.class);
        deviationAlert = mock(DeviationAlert.class);
        job = new FloatingAverageJob(currencyRepository, floatingAverageRepository, floatingAverageCalculator,
                deviationAlert);
    }

    @Test
    public void should_calculate_and_store_latest_average() throws Exception {
        Quote newBitcoinQuote = Quote.newBuilder()
                .setValue("1229.68")
                .setAverage("1213.82")
                .setUpdatedAt(1485129600).build();
        FloatingAverage newBitcoinAverage = newBuilder(BITCOIN_AVERAGE)
                .setLatestAverage("1213.82")
                .setClosingDate(1485129600)
                .addQuotes(newBitcoinQuote)
                .build();
        when(floatingAverageCalculator.calculate(BITCOIN, BITCOIN_AVERAGE)).thenReturn(newBitcoinAverage);

        job.execute();

        verify(floatingAverageRepository).store(singleton(newBitcoinAverage));
    }

    @Test
    public void should_notify_with_alert_when_state_has_changed() throws Exception {
        Quote newBitcoinQuote = Quote.newBuilder()
                .setValue("1229.68")
                .setAverage("1213.82")
                .setUpdatedAt(1485129600).build();
        FloatingAverage newBitcoinAverage = newBuilder(BITCOIN_AVERAGE)
                .setLatestAverage("1213.82")
                .setClosingDate(1485129600)
                .addQuotes(newBitcoinQuote)
                .setAlertState(AlertState.SELL)
                .build();
        when(floatingAverageCalculator.calculate(BITCOIN, BITCOIN_AVERAGE)).thenReturn(newBitcoinAverage);

        job.execute();

        verify(floatingAverageRepository).store(singleton(newBitcoinAverage));
        verify(deviationAlert).alert(newBitcoinAverage);
    }

    @Test
    public void should_store_latest_average_if_no_historical_data_is_available() throws Exception {
        when(floatingAverageRepository.load(singleton("bitcoin"))).thenReturn(emptyList());
        when(floatingAverageCalculator.calculate(BITCOIN, null)).thenReturn(BITCOIN_AVERAGE);

        job.execute();

        verify(floatingAverageRepository).store(singleton(BITCOIN_AVERAGE));
        verifyNoMoreInteractions(deviationAlert);
    }
}