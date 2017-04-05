package org.kiwi.calculation;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.kiwi.crypto.currency.Currency.newCurrency;
import static org.kiwi.proto.FloatingAverageTestData.createBitcoinTestData;
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

public class FloatingAverageJobTest {

    private CurrencyRepository currencyRepository;
    private FloatingAverageJob job;
    private FloatingAverageRepository floatingAverageRepository;
    private FloatingAverageCalculator floatingAverageCalculator;

    @Before
    public void setUp() throws Exception {
        currencyRepository = mock(CurrencyRepository.class);
        floatingAverageRepository = mock(FloatingAverageRepository.class);
        floatingAverageCalculator = mock(FloatingAverageCalculator.class);
        job = new FloatingAverageJob(currencyRepository, floatingAverageRepository, floatingAverageCalculator);
    }

    @Test
    public void should_load_currencies_calculate_and_store_new_average() throws Exception {
        Currency bitcoin = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("1229.68"),
                LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC));
        when(currencyRepository.retrieveCurrencies()).thenReturn(singleton(bitcoin));
        FloatingAverage bitcoinAverage = createBitcoinTestData();
        when(floatingAverageRepository.load(singleton("bitcoin"))).thenReturn(singletonList(bitcoinAverage));
        Quote newBitcoinQuote = Quote.newBuilder()
                .setValue("1229.68")
                .setAverage("1213.82")
                .setUpdatedAt(1485129600).build();
        FloatingAverage newBitcoinAverage = FloatingAverage.newBuilder(bitcoinAverage)
                .setCurrentAverage("1213.82")
                .setClosingDate(1485129600)
                .addQuotes(newBitcoinQuote)
                .build();
        when(floatingAverageCalculator.calculate(bitcoin, bitcoinAverage)).thenReturn(newBitcoinAverage);

        job.execute();

        verify(floatingAverageRepository).store(singleton(newBitcoinAverage));
    }
}