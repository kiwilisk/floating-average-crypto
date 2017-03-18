package org.kiwi.proto;

import static org.kiwi.aws.s3.S3Content.newS3Content;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import org.kiwi.aws.s3.S3Bucket;
import org.kiwi.aws.s3.S3Content;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class FloatingAverageS3Repository implements FloatingAverageRepository {

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
        byte[] bytes = s3Bucket.retrieveContentFor(key);
        return toFloatingAverage(bytes);
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
