package org.kiwi.proto;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

import java.util.Optional;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class HexKeyProvider implements S3KeyProvider {

    @Override
    public Optional<String> createKeyFor(FloatingAverage floatingAverage) {
        if (floatingAverage == null) {
            throw new IllegalArgumentException("Given FloatingAverage was empty");
        }
        try {
            return createKeyFor(floatingAverage.getId(), floatingAverage.getSymbol());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create key from " + floatingAverage);
        }
    }

    @Override
    public Optional<String> createKeyFor(String id, String symbol) {
        if (isNullOrEmpty(id) || isNullOrEmpty(symbol)) {
            throw new IllegalArgumentException("Id or symbol must not be empty");
        }
        return create(id, symbol);
    }

    private Optional<String> create(String id, String symbol) {
        String compositeKey = id + "_" + symbol;
        String encodeHexString = encodeHexString(compositeKey.getBytes(UTF_8));
        return Optional.of(encodeHexString);
    }
}
