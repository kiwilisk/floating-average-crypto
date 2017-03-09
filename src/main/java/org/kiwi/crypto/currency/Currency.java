package org.kiwi.crypto.currency;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.math.BigDecimal;
import java.time.Instant;

@AutoValue
public abstract class Currency {

    @JsonCreator
    static Currency newCurrency(@JsonProperty String id, @JsonProperty String name,
            @JsonProperty BigDecimal priceInUsDollar, @JsonProperty Instant lastUpdated) {
        return new AutoValue_Currency(id, name, priceInUsDollar, lastUpdated);
    }

    public abstract String id();

    public abstract String name();

    public abstract BigDecimal priceInUsDollar();

    public abstract Instant lastUpdated();
}
