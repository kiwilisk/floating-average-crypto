package org.kiwi.proto;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class HexKeyProviderTest {

    private HexKeyProvider hexKeyProvider;
    private FloatingAverage floatingAverage;

    @Before
    public void setUp() throws Exception {
        hexKeyProvider = new HexKeyProvider();
        floatingAverage = FloatingAverageTestData.createFloatingAverage();
    }

    @Test
    public void should_create_hex_key_from_id_and_symbol() throws Exception {
        Optional<String> key = hexKeyProvider.createKeyFor(floatingAverage.getId(), floatingAverage.getSymbol());

        assertThat(key.get()).isEqualTo("626974636f696e5f425443");
    }

    @Test
    public void should_create_hex_key_from_floating_average() throws Exception {
        Optional<String> key = hexKeyProvider.createKeyFor(floatingAverage);

        assertThat(key.get()).isEqualTo("626974636f696e5f425443");
    }

    @Test
    public void should_throw_illegal_argument_exception_if_floating_average_is_empty() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> hexKeyProvider.createKeyFor(null));
    }

    @Test
    public void should_throw_illegal_argument_exception_if_id_or_symbol_is_null_or_empty() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> hexKeyProvider.createKeyFor("", null));
    }
}