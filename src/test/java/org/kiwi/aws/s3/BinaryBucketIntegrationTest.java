package org.kiwi.aws.s3;

import static com.amazonaws.regions.Regions.EU_CENTRAL_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.aws.s3.S3Content.newS3Content;
import static org.kiwi.proto.FloatingAverageTestData.createBitcoinTestData;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class BinaryBucketIntegrationTest {

    private static final String BUCKET_NAME = "floating-average-crypto";
    private static final String TEST_CONTENT_KEY = "test_content";
    private static final String BINARY_CONTENT_TYPE = "binary/octet-stream";

    private BinaryBucket binaryBucket;
    private AmazonS3Client s3Client;

    @Before
    public void setUp() throws Exception {
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        AWSCredentials credentials = credentialsProvider.getCredentials();
        s3Client = new AmazonS3Client(credentials).withRegion(EU_CENTRAL_1);
        binaryBucket = new BinaryBucket(s3Client, BUCKET_NAME);
    }

    @After
    public void tearDown() throws Exception {
        s3Client.deleteObject(new DeleteObjectRequest(BUCKET_NAME, TEST_CONTENT_KEY));
    }

    @Test
    public void should_store_and_retrieve_protobuf() throws Exception {
        FloatingAverage floatingAverage = createBitcoinTestData();
        S3Content s3Content = newS3Content(TEST_CONTENT_KEY, floatingAverage.toByteArray(), BINARY_CONTENT_TYPE);

        binaryBucket.storeContent(s3Content);
        S3Content loadedContent = binaryBucket.retrieveContentFor(TEST_CONTENT_KEY);

        FloatingAverage loadedFloatingAverage = FloatingAverage.parseFrom(loadedContent.bytes());
        assertThat(loadedFloatingAverage).isEqualTo(floatingAverage);
        assertThat(s3Content).isEqualTo(loadedContent);
    }
}