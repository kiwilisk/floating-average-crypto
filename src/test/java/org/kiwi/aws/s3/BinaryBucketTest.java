package org.kiwi.aws.s3;


import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.kiwi.aws.s3.S3Content.newS3Content;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.NONE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.Quote;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class BinaryBucketTest {

    private static final String BUCKET_NAME = "testBucket";
    private static String KEY;
    private static FloatingAverage FLOATING_AVERAGE_PROTO;

    private BinaryBucket binaryBucket;
    private AmazonS3Client s3Client;

    @BeforeClass
    public static void setUpData() {
        KEY = "e85ca376-8d17-493f-826b-1a5a20c88e76";
        Instant instant = LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC);
        Quote quote = Quote.newBuilder()
                .setAverage("123456.431")
                .setValue("119352.674")
                .setUpdatedAt(instant.getEpochSecond())
                .build();
        FLOATING_AVERAGE_PROTO = FloatingAverage.newBuilder()
                .setId("bitcoin")
                .setName("Bitcoin")
                .setSymbol("BTC")
                .setAlertState(NONE)
                .setClosingDate(instant.getEpochSecond())
                .setCurrentAverage("101010.123")
                .addQuotes(quote).build();
    }

    @Before
    public void setUp() throws Exception {
        s3Client = mock(AmazonS3Client.class);
        binaryBucket = new BinaryBucket(s3Client, BUCKET_NAME);
    }

    @Test
    public void should_store_content_with_meta_data() throws Exception {
        S3Content content = newS3Content(KEY, FLOATING_AVERAGE_PROTO.toByteArray(), "binary/octet-stream");
        ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutObjectRequest.class);

        binaryBucket.storeContent(content);

        verify(s3Client).putObject(putObjectRequestArgumentCaptor.capture());
        PutObjectRequest putObjectRequest = putObjectRequestArgumentCaptor.getValue();
        assertThat(putObjectRequest.getBucketName()).isEqualTo(BUCKET_NAME);
        ObjectMetadata metadata = putObjectRequest.getMetadata();
        assertThat(metadata.getContentType()).isEqualTo("binary/octet-stream");
        assertThat(metadata.getContentMD5()).isNotEmpty();
        assertThat(metadata.getContentLength()).isEqualTo(FLOATING_AVERAGE_PROTO.toByteArray().length);
    }

    @Test
    public void should_check_for_existing_key_before_retrieving_data() throws Exception {
        S3Object testS3Object = createTestS3Object();
        InOrder inOrder = Mockito.inOrder(s3Client);
        when(s3Client.doesObjectExist(BUCKET_NAME, KEY)).thenReturn(true);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(testS3Object);

        binaryBucket.retrieveContentFor(KEY);

        inOrder.verify(s3Client).doesObjectExist(BUCKET_NAME, KEY);
        inOrder.verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    public void should_propagate_exception_during_storage() throws Exception {
        S3Content content = newS3Content(KEY, new byte[]{1}, "binary/octet-stream");
        doThrow(new RuntimeException())
                .when(s3Client).putObject(any(PutObjectRequest.class));

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> binaryBucket.storeContent(content))
                .withMessage("Failed to store [1] with key "
                        + "[e85ca376-8d17-493f-826b-1a5a20c88e76] to bucket [testBucket]");
    }

    @Test
    public void should_propagate_exception_during_retrieval() throws Exception {
        doThrow(new RuntimeException())
                .when(s3Client).getObject(any(GetObjectRequest.class));

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> binaryBucket.retrieveContentFor(KEY))
                .withMessage("Failed to load object with key "
                        + "[e85ca376-8d17-493f-826b-1a5a20c88e76] from bucket [testBucket]");
    }

    @Test
    public void should_throw_illegal_argument_exception_if_key_or_byte_array_is_empty() throws Exception {
        S3Content content = newS3Content("", new byte[0], "");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> binaryBucket.storeContent(content));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> binaryBucket.retrieveContentFor(""));
    }

    @Test
    public void should_throw_illegal_argument_exception_if_content_is_null() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> binaryBucket.storeContent(null));
    }

    private S3Object createTestS3Object() throws IOException {
        S3Object s3Object = new S3Object();
        s3Object.setKey(KEY);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("binary/octet-stream");
        s3Object.setObjectMetadata(metadata);
        try (InputStream inputStream = new ByteArrayInputStream(FLOATING_AVERAGE_PROTO.toByteArray())) {
            s3Object.setObjectContent(inputStream);
        }
        return s3Object;
    }
}