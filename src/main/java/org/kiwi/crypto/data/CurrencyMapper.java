package org.kiwi.crypto.data;

import java.util.Collection;

public interface CurrencyMapper<T> {

    Collection<Currency> map(T value);
}
