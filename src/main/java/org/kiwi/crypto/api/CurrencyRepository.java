package org.kiwi.crypto.api;

import org.kiwi.crypto.data.Currency;

import java.util.Collection;

public interface CurrencyRepository {

    Collection<Currency> retrieveCurrencies();
}
