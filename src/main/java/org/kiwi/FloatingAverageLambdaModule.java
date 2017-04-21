package org.kiwi;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.google.inject.name.Names.bindProperties;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.HashMap;
import java.util.Properties;
import org.kiwi.aws.s3.BinaryBucket;
import org.kiwi.aws.s3.S3Bucket;
import org.kiwi.config.Configuration;
import org.kiwi.config.ConfigurationLoader;
import org.kiwi.crypto.api.CoinMarketCap;
import org.kiwi.crypto.api.CoinMarketCapRepository;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.CoinMarketCapMapper;
import org.kiwi.crypto.currency.CurrencyMapper;
import org.kiwi.proto.FloatingAverageRepository;
import org.kiwi.proto.FloatingAverageS3Repository;
import org.kiwi.proto.HexKeyProvider;
import org.kiwi.proto.S3KeyProvider;
import org.kiwi.rest.RestClient;
import org.kiwi.rest.UnirestClient;

public class FloatingAverageLambdaModule extends AbstractModule {

    private static final String CONFIG_PROPERTIES = "/config.properties";
    private static final String CURRENCY_CONFIG = "/currency.json";

    @Override
    protected void configure() {
        bindProperties(binder(), loadProperties(CONFIG_PROPERTIES));
        bind(CurrencyRepository.class)
                .annotatedWith(CoinMarketCap.class)
                .to(CoinMarketCapRepository.class);
        bind(new TypeLiteral<CurrencyMapper<String>>() {
        })
                .annotatedWith(CoinMarketCap.class)
                .to(CoinMarketCapMapper.class);
        bind(RestClient.class)
                .to(UnirestClient.class);
        bind(S3Bucket.class)
                .to(BinaryBucket.class);
        bind(S3KeyProvider.class)
                .to(HexKeyProvider.class);
        bind(FloatingAverageRepository.class)
                .to(FloatingAverageS3Repository.class);
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
    public AmazonSNS amazonSNSClient() {
        return AmazonSNSClientBuilder.defaultClient();
    }

    @Provides
    @Singleton
    public AmazonS3 s3Client() {
        return AmazonS3ClientBuilder.defaultClient();
    }

    @Provides
    @Singleton
    public ConfigurationLoader configurationLoader(ObjectMapper objectMapper) {
        TypeReference<HashMap<String, Configuration>> stringToConfigTypeRef
                = new TypeReference<HashMap<String, Configuration>>() {
        };
        try (InputStream currencyConfigStream = getClass().getResourceAsStream(CURRENCY_CONFIG)) {
            HashMap<String, Configuration> currencyIdToConfig = objectMapper
                    .readValue(currencyConfigStream, stringToConfigTypeRef);
            return new ConfigurationLoader(currencyIdToConfig);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load currency configuration from file [" + CURRENCY_CONFIG + "]");
        }
    }

    private Properties loadProperties(String propertiesFile) {
        try {
            Properties properties = new Properties();
            try (InputStream inputStream = getClass().getResourceAsStream(propertiesFile)) {
                properties.load(inputStream);
                return properties;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load properties from file [" + propertiesFile + "]", e);
        }
    }
}
