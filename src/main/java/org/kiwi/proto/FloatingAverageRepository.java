package org.kiwi.proto;

import static org.kiwi.aws.s3.S3Content.newS3Content;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import org.kiwi.aws.s3.S3Bucket;
import org.kiwi.aws.s3.S3Content;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class FloatingAverageRepository {

    private static final String CONTENT_TYPE = "binary/octet-stream";

    private final S3Bucket s3Bucket;
    private final S3KeyProvider keyProvider;

    @Inject
    public FloatingAverageRepository(S3Bucket s3Bucket, S3KeyProvider keyProvider) {
        this.s3Bucket = s3Bucket;
        this.keyProvider = keyProvider;
    }

    public Optional<FloatingAverage> load(String id, String symbol) {
        return keyProvider.createKeyFor(id, symbol)
                .map(s3Bucket::retrieveContentFor)
                .map(this::toFloatingAverage);
    }

    public void store(FloatingAverage floatingAverage) {
        byte[] bytes = floatingAverage.toByteArray();
        keyProvider.createKeyFor(floatingAverage)
                .map(toS3Content(bytes))
                .ifPresent(s3Bucket::storeContent);
    }

    private FloatingAverage toFloatingAverage(byte[] bytes) {
        try {
            return FloatingAverage.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to parse to FloatingAverage from " + Arrays.toString(bytes));
        }
    }

    private Function<String, S3Content> toS3Content(byte[] bytes) {
        return key -> newS3Content(key, bytes, CONTENT_TYPE);
    }
}
