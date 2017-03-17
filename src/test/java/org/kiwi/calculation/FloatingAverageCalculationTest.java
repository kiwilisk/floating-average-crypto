package org.kiwi.calculation;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.crypto.currency.Currency.newCurrency;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.BUY;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.SELL;
import static org.kiwi.proto.FloatingAverageTestData.createFloatingAverage;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.Quote;

public class FloatingAverageCalculationTest {

    private FloatingAverage floatingAverage;
    private FloatingAverageCalculation floatingAverageCalculation;

    @Before
    public void setUp() throws Exception {
        floatingAverage = createFloatingAverage();
        floatingAverageCalculation = new FloatingAverageCalculation();
    }

    @Test
    public void should_calculate_new_average_from_calculation_data() throws Exception {
        Quote latestBitcoinQuote = Quote.newBuilder()
                .setAverage("109905.157")
                .setValue("102121.234")
                .setUpdatedAt(1485216000)
                .build();
        FloatingAverage expectedFloatingAverage = FloatingAverage.newBuilder(floatingAverage)
                .setCurrentAverage("109905.157")
                .setClosingDate(1485216000)
                .addQuotes(latestBitcoinQuote)
                .build();
        Currency bitcoin = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("102121.234"),
                LocalDate.of(2017, 1, 24).atStartOfDay().toInstant(UTC));
        CalculationData calculationData = new FloatingAverageToCalculationData(bitcoin, new BigDecimal("4.0"))
                .apply(floatingAverage);

        FloatingAverage bitcoinAverage = floatingAverageCalculation.apply(calculationData);

        assertThat(bitcoinAverage).isEqualTo(expectedFloatingAverage);
    }

    @Test
    public void should_change_alert_to_buy_if_current_value_deviates_more_than_threshold_to_last_average()
            throws Exception {
        Quote latestBitcoinQuote = Quote.newBuilder()
                .setAverage("118952.674")
                .setValue("129263.785")
                .setUpdatedAt(1485216000)
                .build();
        FloatingAverage expectedFloatingAverage = FloatingAverage.newBuilder(floatingAverage)
                .setCurrentAverage("118952.674")
                .setClosingDate(1485216000)
                .addQuotes(latestBitcoinQuote)
                .setAlertState(BUY)
                .build();
        Currency bitcoin = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("129263.785"),
                LocalDate.of(2017, 1, 24).atStartOfDay().toInstant(UTC));
        CalculationData calculationData = new FloatingAverageToCalculationData(bitcoin, new BigDecimal("4.0"))
                .apply(floatingAverage);

        FloatingAverage bitcoinAverage = floatingAverageCalculation.apply(calculationData);

        assertThat(bitcoinAverage).isEqualTo(expectedFloatingAverage);
    }

    @Test
    public void should_change_alert_to_sell_if_current_value_deviates_less_than_threshold_to_last_average()
            throws Exception {
        Quote latestBitcoinQuote = Quote.newBuilder()
                .setAverage("106571.824")
                .setValue("92121.234")
                .setUpdatedAt(1485216000)
                .build();
        FloatingAverage expectedFloatingAverage = FloatingAverage.newBuilder(floatingAverage)
                .setCurrentAverage("106571.824")
                .setClosingDate(1485216000)
                .addQuotes(latestBitcoinQuote)
                .setAlertState(SELL)
                .build();
        Currency bitcoin = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("92121.234"),
                LocalDate.of(2017, 1, 24).atStartOfDay().toInstant(UTC));
        CalculationData calculationData = new FloatingAverageToCalculationData(bitcoin, new BigDecimal("4.0"))
                .apply(floatingAverage);

        FloatingAverage bitcoinAverage = floatingAverageCalculation.apply(calculationData);

        assertThat(bitcoinAverage).isEqualTo(expectedFloatingAverage);
    }
}