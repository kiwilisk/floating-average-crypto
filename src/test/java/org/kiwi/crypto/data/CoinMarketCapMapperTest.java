package org.kiwi.crypto.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;

import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.crypto.data.ImmutableCurrency.newCurrency;

public class CoinMarketCapMapperTest {

    private CoinMarketCapMapper mapper;
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        mapper = new CoinMarketCapMapper(objectMapper, clock);
    }

    @Test
    public void should_map_json_to_currency() throws Exception {
        String json = "[{\n" +
                "        \"id\": \"bitcoin\", \n" +
                "        \"name\": \"Bitcoin\", \n" +
                "        \"symbol\": \"BTC\", \n" +
                "        \"price_usd\": \"1253.84\", \n" +
                "        \"last_updated\": \"1488719045\"\n" +
                "    }]";
        Currency expected = newCurrency("bitcoin", "Bitcoin", new BigDecimal("1253.84"), ofEpochSecond(1488719045));

        Collection<Currency> currency = mapper.map(json);

        assertThat(currency).isEqualTo(singleton(expected));
    }

    @Test
    public void should_replace_missing_text_values_with_NO_VALUE() throws Exception {
        String json = "[{\n" +
                "        \"price_usd\": \"1253.84\", \n" +
                "        \"last_updated\": \"1488719045\"\n" +
                "    }]";
        Currency expected = newCurrency("NO_VALUE", "NO_VALUE", new BigDecimal("1253.84"), ofEpochSecond(1488719045));

        Collection<Currency> currency = mapper.map(json);

        assertThat(currency).isEqualTo(singleton(expected));
    }

    @Test
    public void should_replace_missing_updated_value_with_current_time() throws Exception {
        String json = "[{\n" +
                "        \"id\": \"bitcoin\", \n" +
                "        \"name\": \"Bitcoin\", \n" +
                "        \"symbol\": \"BTC\", \n" +
                "        \"price_usd\": \"1253.84\"\n" +
                "    }]";
        Currency expected = newCurrency("bitcoin", "Bitcoin", new BigDecimal("1253.84"), clock.instant());

        Collection<Currency> currency = mapper.map(json);

        assertThat(currency).isEqualTo(singleton(expected));
    }

    @Test
    public void should_replace_missing_currency_value_with_zero() throws Exception {
        String json = "[{\n" +
                "        \"id\": \"bitcoin\", \n" +
                "        \"name\": \"Bitcoin\", \n" +
                "        \"symbol\": \"BTC\", \n" +
                "        \"last_updated\": \"1488719045\"\n" +
                "    }]";
        Currency expected = newCurrency("bitcoin", "Bitcoin", BigDecimal.ZERO, ofEpochSecond(1488719045));

        Collection<Currency> currency = mapper.map(json);

        assertThat(currency).isEqualTo(singleton(expected));
    }

    @Test
    public void should_parse_goddamn_vcoin() throws Exception {
        String json = "{\"id\":\"vcoin\",\"name\":\"Vcoin\",\"symbol\":\"VCN\",\"rank\":\"737\",\"price_usd\":null,\"price_btc\":null,\"24h_volume_usd\":null,\"market_cap_usd\":null,\"available_supply\":null,\"total_supply\":null,\"percent_change_1h\":null,\"percent_change_24h\":null,\"percent_change_7d\":null,\"last_updated\":null}";

        Collection<Currency> currency = mapper.map(json);

        assertThat(currency).isNotNull();
    }
}