package org.kiwi.crypto.currency;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.crypto.currency.Currency.newCurrency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class PriceAverageTest {

    private PriceAverage priceAverage;

    @Before
    public void setUp() throws Exception {
        priceAverage = new PriceAverage();
    }

    @Test
    public void should_return_zero_for_empty_currencies() throws Exception {
        BigDecimal average = this.priceAverage.calculateFor(Collections.emptyList());

        assertThat(average).isEqualTo("0");
    }

    @Test
    public void should_calculate_average_from_list_of_currencies() throws Exception {
        Collection<Currency> currencies = asList(
                newCurrency("someId", "someName", new BigDecimal("10019.997793"), Instant.now()),
                newCurrency("someId", "someName", new BigDecimal("9845.234556"), Instant.now()),
                newCurrency("someId", "someName", new BigDecimal("7856.567886"), Instant.now()),
                newCurrency("someId", "someName", new BigDecimal("10119.567823"), Instant.now()),
                newCurrency("someId", "someName", new BigDecimal("10003.997793"), Instant.now())
        );

        BigDecimal average = this.priceAverage.calculateFor(currencies);

        assertThat(average).isEqualTo(new BigDecimal("9569.073171"));
    }
}