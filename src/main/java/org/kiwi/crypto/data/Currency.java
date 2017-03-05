package org.kiwi.crypto.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value.Immutable
@Value.Style(jdkOnly = true, allParameters = true, of = "new*")
@JsonSerialize(as = ImmutableCurrency.class)
@JsonDeserialize(as = ImmutableCurrency.class)
public abstract class Currency {

    public abstract String id();

    public abstract String name();

    public abstract BigDecimal priceInUsd();

    public abstract Instant lastUpdated();
}
