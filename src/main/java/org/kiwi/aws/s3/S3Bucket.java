package org.kiwi.aws.s3;

public interface S3Bucket {

    void storeContent(S3Content content);

    byte[] retrieveContentFor(String key);
}
