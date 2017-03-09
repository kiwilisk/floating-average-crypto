package org.kiwi;

import static com.amazonaws.regions.Regions.EU_CENTRAL_1;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.google.inject.name.Names.bindProperties;

import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.io.InputStream;
import java.time.Clock;
import java.util.Properties;
import org.kiwi.crypto.api.CoinMarketCap;
import org.kiwi.crypto.api.CoinMarketCapRepository;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.CoinMarketCapMapper;
import org.kiwi.crypto.currency.CurrencyMapper;
import org.kiwi.rest.RestClient;
import org.kiwi.rest.Unirest;
import org.kiwi.rest.UnirestClient;

public class FloatingAverageLambdaModule extends AbstractModule {

    private static final String CONFIG_PROPERTIES = "/config.properties";

    @Override
    protected void configure() {
        bindProperties(binder(), loadProperties());
        bind(CurrencyRepository.class)
                .annotatedWith(CoinMarketCap.class)
                .to(CoinMarketCapRepository.class);
        bind(new TypeLiteral<CurrencyMapper<String>>() {
        })
                .annotatedWith(CoinMarketCap.class)
                .to(CoinMarketCapMapper.class);
        bind(RestClient.class)
                .annotatedWith(Unirest.class)
                .to(UnirestClient.class);
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .disable(WRITE_DATES_AS_TIMESTAMPS);
    }

    @Provides
    @Singleton
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Provides
    @Singleton
    public AmazonSNSClient amazonSNSClient() {
        return new AmazonSNSAsyncClient()
                .<AmazonSNSAsyncClient>withRegion(Region.getRegion(EU_CENTRAL_1));
    }

    @Provides
    @Singleton
    public AmazonS3Client s3Client() {
        return new AmazonS3Client()
                .withRegion(Region.getRegion(EU_CENTRAL_1));
    }

    private Properties loadProperties() {
        try {
            Properties properties = new Properties();
            try (InputStream inputStream = getClass().getResourceAsStream(CONFIG_PROPERTIES)) {
                properties.load(inputStream);
                return properties;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load properties from file [" + CONFIG_PROPERTIES + "]", e);
        }
    }
}
