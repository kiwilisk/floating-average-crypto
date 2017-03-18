package org.kiwi.calculation;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singleton;
import static org.kiwi.crypto.currency.Currency.newCurrency;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.NONE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.Quote;
import org.kiwi.proto.FloatingAverageRepository;
import org.kiwi.proto.FloatingAverageTestData;

public class FloatingAverageJobTest {

    private CurrencyRepository currencyRepository;
    private FloatingAverageJob job;
    private FloatingAverageRepository floatingAverageRepository;

    @Before
    public void setUp() throws Exception {
        currencyRepository = mock(CurrencyRepository.class);
        floatingAverageRepository = mock(FloatingAverageRepository.class);
        job = new FloatingAverageJob(currencyRepository, floatingAverageRepository);
    }

    @Test
    public void should_create_new_average_from_currency_if_no_historic_values_exist() throws Exception {
        Currency etherum = newCurrency("ethereum", "Ethereum", "ETH", new BigDecimal("18.2812"),
                LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC));
        when(currencyRepository.retrieveCurrencies()).thenReturn(singleton(etherum));
        Quote etherumQuote = Quote.newBuilder()
                .setAverage("18.2812")
                .setValue("18.2812")
                .setUpdatedAt(1485129600)
                .build();
        FloatingAverage etherumFloatingAverage = FloatingAverage.newBuilder()
                .setId("ethereum")
                .setName("Ethereum")
                .setSymbol("ETH")
                .setAlertState(NONE)
                .setClosingDate(1485129600)
                .setCurrentAverage("18.2812")
                .addQuotes(etherumQuote)
                .build();

        job.execute();

        verify(floatingAverageRepository).store(etherumFloatingAverage);
    }

    @Test
    public void should_calculate_and_store_new_average_from_currency_and_historic_values() throws Exception {
        FloatingAverage bitcoinAverage = FloatingAverageTestData.createFloatingAverage();
        when(floatingAverageRepository.load("bitcoin")).thenReturn(bitcoinAverage);
        Currency bitcoin = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("1229.68"),
                LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC));
        when(currencyRepository.retrieveCurrencies()).thenReturn(singleton(bitcoin));
        Quote expectedNewQuote = Quote.newBuilder()
                .setValue("1229.68")
                .setAverage("1213.82")
                .setUpdatedAt(1485129600).build();
        FloatingAverage expectedBitcoinAverage = FloatingAverage.newBuilder(bitcoinAverage)
                .setCurrentAverage("1213.82")
                .setClosingDate(1485129600)
                .addQuotes(expectedNewQuote)
                .build();

        job.execute();

        verify(floatingAverageRepository).store(expectedBitcoinAverage);
    }
}