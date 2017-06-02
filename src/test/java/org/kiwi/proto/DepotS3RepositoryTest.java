package org.kiwi.proto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.aws.s3.S3Content.newS3Content;
import static org.kiwi.proto.FloatingAverageTestData.createBitcoinTestData;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.kiwi.aws.s3.S3Bucket;
import org.kiwi.aws.s3.S3Content;
import org.kiwi.aws.s3.S3KeyProvider;
import org.kiwi.proto.FloatingAverageProtos.Depot;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class DepotS3RepositoryTest {

    private DepotS3Repository depotS3Repository;
    private FloatingAverage bitcoinAverage;
    private S3Bucket s3Bucket;
    private S3KeyProvider keyProvider;
    private Depot depot;

    @Before
    public void setUp() throws Exception {
        s3Bucket = mock(S3Bucket.class);
        keyProvider = mock(S3KeyProvider.class);
        depotS3Repository = new DepotS3Repository(s3Bucket, keyProvider);
        bitcoinAverage = createBitcoinTestData();
        depot = Depot.newBuilder()
                .setId("id")
                .addFloatingAverages(bitcoinAverage)
                .build();
    }

    @Test
    public void should_load_with_key_from_key_provider() throws Exception {
        when(keyProvider.createKeyFor("id")).thenReturn("valid_key");
        S3Content content = newS3Content("valid_key", depot.toByteArray(), "binary/octet-stream");
        when(s3Bucket.retrieveContentFor("valid_key")).thenReturn(content);

        Depot loadedFloatingAverage = depotS3Repository.load("id");

        verify(s3Bucket).retrieveContentFor("valid_key");
        assertThat(loadedFloatingAverage).isEqualTo(depot);
    }

    @Test
    public void should_store_binary_object_with_key_from_provider() throws Exception {
        String key = "this_is_a_valid_key";
        when(keyProvider.createKeyFor(depot.getId())).thenReturn(key);
        S3Content expectedContent = newS3Content(key, depot.toByteArray(), "binary/octet-stream");

        depotS3Repository.store(depot);

        verify(s3Bucket).storeContent(expectedContent);
    }
}