package org.kiwi.calculation;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.crypto.currency.Currency.newCurrency;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.NONE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.Quote;
import org.kiwi.proto.FloatingAverageRepository;

public class FloatingAverageCalculatorTest {

    private FloatingAverageRepository floatingAverageRepository;
    private FloatingAverageCalculator calculator;

    @Before
    public void setUp() throws Exception {
        Quote etherumQuote = Quote.newBuilder()
                .setAverage("17.1701")
                .setValue("18.2812")
                .setUpdatedAt(1485129600)
                .build();
        FloatingAverage etherumFloatingAverage = FloatingAverage.newBuilder()
                .setId("ethereum")
                .setName("Ethereum")
                .setSymbol("ETH")
                .setAlertState(NONE)
                .setClosingDate(1485129600)
                .setCurrentAverage("16.0690")
                .addQuotes(etherumQuote)
                .build();
        floatingAverageRepository = mock(FloatingAverageRepository.class);
        when(floatingAverageRepository.load("ethereum", "ETH")).thenReturn(Optional.of(etherumFloatingAverage));
        calculator = new FloatingAverageCalculator(floatingAverageRepository);
    }

    @Test
    public void should_retrieve_stored_average_and_calculate_new_result() throws Exception {
        Quote etherumQuote1 = Quote.newBuilder()
                .setAverage("17.1701")
                .setValue("18.2812")
                .setUpdatedAt(1485129600)
                .build();
        Quote etherumQuote2 = Quote.newBuilder()
                .setAverage("18.2257")
                .setValue("18.1701")
                .setUpdatedAt(1485216000)
                .build();
        FloatingAverage expectedFloatingAverage = FloatingAverage.newBuilder()
                .setId("ethereum")
                .setName("Ethereum")
                .setSymbol("ETH")
                .setAlertState(NONE)
                .setClosingDate(1485216000)
                .setCurrentAverage("18.2257")
                .addAllQuotes(asList(etherumQuote1, etherumQuote2))
                .build();

        Currency etherum = newCurrency("ethereum", "Ethereum", "ETH", new BigDecimal("18.1701"),
                LocalDate.of(2017, 1, 24).atStartOfDay().toInstant(UTC));

        FloatingAverage ethereumAverage = calculator.calculate(etherum);

        assertThat(ethereumAverage).isEqualTo(expectedFloatingAverage);
    }
}