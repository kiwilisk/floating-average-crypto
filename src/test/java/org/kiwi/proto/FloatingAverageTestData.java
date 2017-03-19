package org.kiwi.proto;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.NONE;

import java.time.LocalDate;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.Quote;

public class FloatingAverageTestData {

    private static final long DATE_AS_EPOCH_SECOND = LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC)
            .getEpochSecond();

    public static FloatingAverage createBitcoinTestData() {
        Quote quote1 = Quote.newBuilder()
                .setAverage("1199.67")
                .setValue("1201.75")
                .setUpdatedAt(DATE_AS_EPOCH_SECOND)
                .build();
        Quote quote2 = Quote.newBuilder()
                .setAverage("1189.92")
                .setValue("1210.02")
                .setUpdatedAt(DATE_AS_EPOCH_SECOND)
                .build();
        return FloatingAverage.newBuilder()
                .setId("bitcoin")
                .setName("Bitcoin")
                .setSymbol("BTC")
                .setAlertState(NONE)
                .setClosingDate(DATE_AS_EPOCH_SECOND)
                .setCurrentAverage("1229.68")
                .addAllQuotes(asList(quote1, quote2))
                .build();
    }

    public static FloatingAverage createEthereumTestData() {
        Quote etherumQuote = Quote.newBuilder()
                .setAverage("18.2812")
                .setValue("18.2812")
                .setUpdatedAt(1485129600)
                .build();
        return FloatingAverage.newBuilder()
                .setId("ethereum")
                .setName("Ethereum")
                .setSymbol("ETH")
                .setAlertState(NONE)
                .setClosingDate(1485129600)
                .setCurrentAverage("18.2812")
                .addQuotes(etherumQuote)
                .build();
    }
}
