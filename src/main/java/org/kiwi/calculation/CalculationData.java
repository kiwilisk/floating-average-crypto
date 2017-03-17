package org.kiwi.calculation;

import com.google.auto.value.AutoValue;
import java.math.BigDecimal;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

@AutoValue
abstract class CalculationData {

    static CalculationData newCalculationData(FloatingAverage floatingAverage, BigDecimal currencyValue,
            long closingDateInEpochSeconds, BigDecimal newAverage, BigDecimal deviationThreshold) {
        return new AutoValue_CalculationData(floatingAverage, currencyValue, closingDateInEpochSeconds, newAverage,
                new BigDecimal(floatingAverage.getCurrentAverage()), deviationThreshold);
    }

    abstract FloatingAverage floatingAverage();

    abstract BigDecimal currencyValue();

    abstract long closingDateInEpochSeconds();

    abstract BigDecimal newAverage();

    abstract BigDecimal currentAverage();

    abstract BigDecimal deviationThreshold();
}
