package org.kiwi.proto;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.aws.s3.S3Content.newS3Content;
import static org.kiwi.proto.FloatingAverageTestData.createBitcoinTestData;
import static org.kiwi.proto.FloatingAverageTestData.createEthereumTestData;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.aws.s3.S3Bucket;
import org.kiwi.aws.s3.S3Content;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.mockito.ArgumentCaptor;

public class FloatingAverageS3RepositoryTest {

    private FloatingAverageS3Repository floatingAverageS3Repository;
    private FloatingAverage bitcoinAverage;
    private S3Bucket s3Bucket;
    private S3KeyProvider keyProvider;

    @Before
    public void setUp() throws Exception {
        s3Bucket = mock(S3Bucket.class);
        keyProvider = mock(S3KeyProvider.class);
        floatingAverageS3Repository = new FloatingAverageS3Repository(s3Bucket, keyProvider);
        bitcoinAverage = createBitcoinTestData();
    }

    @Test
    public void should_load_with_key_from_key_provider() throws Exception {
        when(keyProvider.createKeyFor("id")).thenReturn("valid_key");
        when(s3Bucket.retrieveContentFor("valid_key")).thenReturn(bitcoinAverage.toByteArray());

        FloatingAverage loadedFloatingAverage = floatingAverageS3Repository.load("id");

        verify(s3Bucket).retrieveContentFor("valid_key");
        assertThat(loadedFloatingAverage).isEqualTo(bitcoinAverage);
    }

    @Test
    public void should_store_binary_object_with_key_from_provider() throws Exception {
        String key = "this_is_a_valid_key";
        when(keyProvider.createKeyFor(bitcoinAverage.getId())).thenReturn(key);
        S3Content expectedContent = newS3Content(key, bitcoinAverage.toByteArray(), "binary/octet-stream");

        floatingAverageS3Repository.store(bitcoinAverage);

        verify(s3Bucket).storeContent(expectedContent);
    }

    @Test
    public void should_store_multiple_floating_averages() throws Exception {
        FloatingAverage ethereumAverageum = createEthereumTestData();
        when(keyProvider.createKeyFor(ethereumAverageum.getId())).thenReturn("ethereum_key");
        when(keyProvider.createKeyFor(bitcoinAverage.getId())).thenReturn("bitcoin_key");
        S3Content bitcoinContent = newS3Content("bitcoin_key", bitcoinAverage.toByteArray(), "binary/octet-stream");
        S3Content ethereumContent = newS3Content("ethereum_key", ethereumAverageum.toByteArray(),
                "binary/octet-stream");

        floatingAverageS3Repository.store(asList(ethereumAverageum, bitcoinAverage));

        ArgumentCaptor<S3Content> contentArgumentCaptor = ArgumentCaptor.forClass(S3Content.class);
        verify(s3Bucket, atLeastOnce()).storeContent(contentArgumentCaptor.capture());
        assertThat(contentArgumentCaptor.getAllValues()).containsExactlyInAnyOrder(bitcoinContent, ethereumContent);
    }

    @Test
    public void should_load_multiple_floating_averages() throws Exception {
        FloatingAverage etverageumAverage = createEthereumTestData();
        when(keyProvider.createKeyFor("ethereum")).thenReturn("ethereum_key");
        when(keyProvider.createKeyFor("bitcoin")).thenReturn("bitcoin_key");
        when(s3Bucket.retrieveContentFor("ethereum_key")).thenReturn(etverageumAverage.toByteArray());
        when(s3Bucket.retrieveContentFor("bitcoin_key")).thenReturn(bitcoinAverage.toByteArray());

        Collection<FloatingAverage> loadedAverages = floatingAverageS3Repository.load(asList("bitcoin", "ethereum"));

        assertThat(loadedAverages).containsExactlyInAnyOrder(etverageumAverage, bitcoinAverage);
    }
}