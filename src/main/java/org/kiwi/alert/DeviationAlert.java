package org.kiwi.alert;

import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public interface DeviationAlert {

    void alert(FloatingAverage floatingAverage);
}
