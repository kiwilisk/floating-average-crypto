package org.kiwi.calculation;

import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.BUY;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.SELL;

import java.math.BigDecimal;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState;

class AverageAlert {

    AlertState evaluateStateWith(CalculationData calculationData) {
        BigDecimal deviation = calculateDeviation(calculationData);
        if (isThresholdExceeded(deviation, calculationData.deviationThreshold())) {
            return isValueHigherThanAverage(calculationData.currencyValue(),
                    calculationData.currentAverage()) ? BUY : SELL;
        }
        return calculationData.floatingAverage().getAlertState();
    }

    private BigDecimal calculateDeviation(CalculationData calculationData) {
        return new Deviation().calculate(calculationData.currentAverage(), calculationData.currencyValue());
    }

    private static boolean isThresholdExceeded(BigDecimal deviation, BigDecimal percentageThreshold) {
        return deviation.compareTo(percentageThreshold) >= 0;
    }

    private static boolean isValueHigherThanAverage(BigDecimal closingValue, BigDecimal lastAverage) {
        return closingValue.compareTo(lastAverage) == 1;
    }
}
