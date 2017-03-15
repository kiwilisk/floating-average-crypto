package org.kiwi.proto;

import java.util.Optional;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public interface S3KeyProvider {

    Optional<String> createKeyFor(FloatingAverage floatingAverage);

    Optional<String> createKeyFor(String id, String symbol);

}
