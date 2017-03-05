package org.kiwi.crypto.data;

import com.google.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Spliterator;
import java.util.function.Function;

import static java.time.Instant.ofEpochSecond;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

public class CoinMarketCapMapper implements CurrencyMapper<String> {

    private static final String NO_VALUE = "NO_VALUE";

    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Inject
    CoinMarketCapMapper(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public Collection<Currency> map(String value) {
        try {
            JsonNode jsonNode = objectMapper.readTree(value);
            return stream(spliteratorUnknownSize(jsonNode.iterator(), Spliterator.ORDERED), false)
                    .map(toCurrency())
                    .collect(toSet());
        } catch (IOException e) {
            throw new RuntimeException("Failed to map " + value + " to Currency", e);
        }
    }

    private Function<JsonNode, Currency> toCurrency() {
        return node -> {
            try {
                return ImmutableCurrency.builder()
                        .id(parseTextFrom(node, "id"))
                        .name(parseTextFrom(node, "name"))
                        .priceInUsDollar(parsePriceFrom(node))
                        .lastUpdated(parseInstantFrom(node))
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("could not parse " + node);
            }
        };
    }

    private String parseTextFrom(JsonNode node, String fieldName) {
        JsonNode jsonNode = node.get(fieldName);
        return jsonNode != null && !jsonNode.isNull() ? jsonNode.asText() : NO_VALUE;
    }

    private Instant parseInstantFrom(JsonNode node) {
        JsonNode jsonNode = node.get("last_updated");
        return jsonNode != null && !jsonNode.isNull() ? ofEpochSecond(jsonNode.asLong()) : clock.instant();
    }

    private BigDecimal parsePriceFrom(JsonNode node) {
        JsonNode jsonNode = node.get("price_usd");
        return jsonNode != null && !jsonNode.isNull() ? new BigDecimal(jsonNode.asText()) : BigDecimal.ZERO;
    }
}
