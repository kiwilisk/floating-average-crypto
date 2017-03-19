package org.kiwi.proto;

import java.util.Collection;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public interface FloatingAverageRepository {

    FloatingAverage load(String id);

    Collection<FloatingAverage> load(Collection<String> ids);

    void store(FloatingAverage floatingAverage);

    void store(Collection<FloatingAverage> floatingAverages);

}
