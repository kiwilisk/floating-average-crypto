package org.kiwi.proto;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static org.kiwi.aws.s3.S3Content.newS3Content;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.apache.log4j.Logger;
import org.kiwi.aws.s3.S3Bucket;
import org.kiwi.aws.s3.S3Content;
import org.kiwi.aws.s3.S3KeyProvider;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class FloatingAverageS3Repository implements FloatingAverageRepository {

    private static final Logger LOGGER = Logger.getLogger(FloatingAverageS3Repository.class);
    private static final String CONTENT_TYPE = "binary/octet-stream";

    private final S3Bucket s3Bucket;
    private final S3KeyProvider keyProvider;

    @Inject
    FloatingAverageS3Repository(S3Bucket s3Bucket, S3KeyProvider keyProvider) {
        this.s3Bucket = s3Bucket;
        this.keyProvider = keyProvider;
    }

    @Override
    public FloatingAverage load(String id) {
        String key = keyProvider.createKeyFor(id);
        byte[] bytes = s3Bucket.retrieveContentFor(key).bytes();
        return toFloatingAverage(bytes);
    }

    @Override
    public Collection<FloatingAverage> load(Collection<String> ids) {
        List<CompletableFuture<FloatingAverage>> loadAverageFutures = ids.stream()
                .map(toLoadAverageFuture())
                .collect(toList());
        return loadAverageFutures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    @Override
    public void store(FloatingAverage floatingAverage) {
        if (floatingAverage == null) {
            throw new IllegalArgumentException("FloatingAverage must not be null");
        }
        String key = keyProvider.createKeyFor(floatingAverage.getId());
        byte[] bytes = floatingAverage.toByteArray();
        S3Content content = createS3ContentWith(key, bytes);
        s3Bucket.storeContent(content);
    }

    @Override
    public void store(Collection<FloatingAverage> floatingAverages) {
        List<CompletableFuture<Void>> storeAverageFutures = floatingAverages.stream()
                .map(toStoreAverageFuture())
                .collect(toList());
        storeAverageFutures.forEach(CompletableFuture::join);
    }

    private Function<String, CompletableFuture<FloatingAverage>> toLoadAverageFuture() {
        return id -> supplyAsync(() -> load(id))
                .exceptionally(throwable -> {
                    LOGGER.error("Failed to load FloatingAverage with id [" + id + "]", throwable);
                    return null;
                });
    }

    private Function<FloatingAverage, CompletableFuture<Void>> toStoreAverageFuture() {
        return floatingAverage -> runAsync(() -> store(floatingAverage))
                .exceptionally(throwable -> {
                    LOGGER.error("Failed to store [" + floatingAverage + "]", throwable);
                    return null;
                });
    }

    private FloatingAverage toFloatingAverage(byte[] bytes) {
        try {
            return FloatingAverage.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to parse to FloatingAverage from " + Arrays.toString(bytes));
        }
    }

    private S3Content createS3ContentWith(String key, byte[] bytes) {
        return newS3Content(key, bytes, CONTENT_TYPE);
    }
}
