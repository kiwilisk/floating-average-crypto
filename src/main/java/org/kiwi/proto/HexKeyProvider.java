package org.kiwi.proto;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

public class HexKeyProvider implements S3KeyProvider {

    @Override
    public String createKeyFor(String id) {
        if (isNullOrEmpty(id)) {
            throw new IllegalArgumentException("Given FloatingAverage was empty");
        }
        try {
            return encodeHexString(id.getBytes(UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create key for [" + id + "]");
        }
    }
}
