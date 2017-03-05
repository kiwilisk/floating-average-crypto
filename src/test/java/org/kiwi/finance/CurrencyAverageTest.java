package org.kiwi.finance;

import org.junit.Before;
import org.junit.Test;
import org.kiwi.crypto.data.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.crypto.data.ImmutableCurrency.newCurrency;

public class CurrencyAverageTest {

    private CurrencyAverage currencyAverage;

    @Before
    public void setUp() throws Exception {
        currencyAverage = new CurrencyAverage();
    }

    @Test
    public void should_return_zero_for_empty_currencies() throws Exception {
        BigDecimal average = this.currencyAverage.calculateFor(Collections.emptyList());

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

        BigDecimal average = this.currencyAverage.calculateFor(currencies);

        assertThat(average).isEqualTo(new BigDecimal("9569.073171"));
    }
}