package org.kiwi.proto;

import org.kiwi.proto.FloatingAverageProtos.Depot;

public interface DepotRepository {

    Depot load(String id);

    void store(Depot depot);
}
