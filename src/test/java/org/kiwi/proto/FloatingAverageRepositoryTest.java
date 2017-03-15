package org.kiwi.proto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.aws.s3.S3Content.newS3Content;
import static org.kiwi.proto.FloatingAverageTestData.createFloatingAverage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.aws.s3.S3Bucket;
import org.kiwi.aws.s3.S3Content;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class FloatingAverageRepositoryTest {

    private FloatingAverageRepository floatingAverageRepository;
    private FloatingAverage floatingAverage;
    private S3Bucket s3Bucket;
    private S3KeyProvider keyProvider;

    @Before
    public void setUp() throws Exception {
        s3Bucket = mock(S3Bucket.class);
        keyProvider = mock(S3KeyProvider.class);
        floatingAverageRepository = new FloatingAverageRepository(s3Bucket, keyProvider);
        floatingAverage = createFloatingAverage();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void should_load_with_key_from_key_provider() throws Exception {
        when(keyProvider.createKeyFor("id", "symbol")).thenReturn(Optional.of("valid_key"));
        when(s3Bucket.retrieveContentFor("valid_key")).thenReturn(floatingAverage.toByteArray());

        Optional<FloatingAverage> loadedFloatingAverage = floatingAverageRepository.load("id", "symbol");

        verify(s3Bucket).retrieveContentFor("valid_key");
        assertThat(loadedFloatingAverage.get()).isEqualTo(floatingAverage);
    }

    @Test
    public void should_store_binary_object_with_key_from_provider() throws Exception {
        String key = "this_is_a_valid_key";
        when(keyProvider.createKeyFor(floatingAverage)).thenReturn(Optional.of(key));
        S3Content expectedContent = newS3Content(key, floatingAverage.toByteArray(), "binary/octet-stream");

        floatingAverageRepository.store(floatingAverage);

        verify(s3Bucket).storeContent(expectedContent);
    }
}