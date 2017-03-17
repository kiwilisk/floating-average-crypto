package org.kiwi.calculation;


import java.util.function.Function;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState;
import org.kiwi.proto.FloatingAverageProtos.Quote;

class FloatingAverageCalculation implements Function<CalculationData, FloatingAverage> {

    @Override
    public FloatingAverage apply(CalculationData calculationData) {
        AlertState state = new AverageAlert().evaluateStateWith(calculationData);
        Quote quote = createLatestQuoteOf(calculationData);
        return createFloatingAverageWith(calculationData, state, quote);
    }

    private Quote createLatestQuoteOf(CalculationData calculationData) {
        return Quote.newBuilder()
                .setValue(calculationData.currencyValue().toPlainString())
                .setAverage(calculationData.newAverage().toPlainString())
                .setUpdatedAt(calculationData.closingDateInEpochSeconds())
                .build();
    }

    private FloatingAverage createFloatingAverageWith(CalculationData calculationData, AlertState state, Quote quote) {
        return FloatingAverage.newBuilder(calculationData.floatingAverage())
                .setClosingDate(calculationData.closingDateInEpochSeconds())
                .setCurrentAverage(calculationData.newAverage().toPlainString())
                .addQuotes(quote)
                .setAlertState(state)
                .build();
    }
}
