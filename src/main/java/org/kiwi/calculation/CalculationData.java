package org.kiwi.calculation;

import com.google.auto.value.AutoValue;
import java.math.BigDecimal;

@AutoValue
abstract class CalculationData {

    static CalculationData newCalculationData(BigDecimal currencyValue,
            long closingDateInEpochSeconds, BigDecimal currentAverage, BigDecimal newAverage,
            BigDecimal deviationThreshold) {
        return new AutoValue_CalculationData(currencyValue, closingDateInEpochSeconds, currentAverage,
                newAverage, deviationThreshold);
    }

    abstract BigDecimal currencyValue();

    abstract long closingDateInEpochSeconds();

    abstract BigDecimal currentAverage();

    abstract BigDecimal newAverage();

    abstract BigDecimal deviationThreshold();
}
