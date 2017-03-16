package org.kiwi.calculation;

import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public interface Calculator {

    FloatingAverage calculate(Currency currency);

}
