package org.kiwi.proto;

import java.util.Optional;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public interface FloatingAverageRepository {

    Optional<FloatingAverage> load(String id, String symbol);

    void store(FloatingAverage floatingAverage);

}
