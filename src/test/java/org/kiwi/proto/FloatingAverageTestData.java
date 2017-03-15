package org.kiwi.proto;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.NONE;

import java.time.LocalDate;
import java.util.Collection;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.Quote;

public class FloatingAverageTestData {

    private static final long DATE_AS_EPOCH_SECOND = LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC)
            .getEpochSecond();

    public static FloatingAverage createFloatingAverage() {
        return FloatingAverage.newBuilder()
                .setId("bitcoin")
                .setName("Bitcoin")
                .setSymbol("BTC")
                .setAlertState(NONE)
                .setClosingDate(DATE_AS_EPOCH_SECOND)
                .setCurrentAverage("101010.123")
                .addAllQuotes(createQuotes())
                .build();
    }

    private static Collection<Quote> createQuotes() {
        Quote quote1 = Quote.newBuilder()
                .setAverage("123456.431")
                .setValue("119352.674")
                .setUpdatedAt(DATE_AS_EPOCH_SECOND)
                .build();
        Quote quote2 = Quote.newBuilder()
                .setAverage("132145.320")
                .setValue("108241.563")
                .setUpdatedAt(DATE_AS_EPOCH_SECOND)
                .build();
        return asList(quote1, quote2);
    }
}
