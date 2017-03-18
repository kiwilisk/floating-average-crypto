package org.kiwi.proto;

import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public interface FloatingAverageRepository {

    FloatingAverage load(String id);

    void store(FloatingAverage floatingAverage);

}
