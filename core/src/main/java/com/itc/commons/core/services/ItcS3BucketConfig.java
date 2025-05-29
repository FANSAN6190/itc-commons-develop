package com.itc.commons.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "ITC S3 Bucket Configs", description = "ITC S3 Bucket Configs")
public @interface ItcS3BucketConfig {

    @AttributeDefinition(name = "S3 Bucket Name", description = "S3 Bucket Name")
    String s3BucketName();
    @AttributeDefinition(name = "S3 Region", description = "S3 Region")
    String s3Region();
    @AttributeDefinition(name = "AWS Access Key", description = "AWS Access Key")
    String awsAccessKey();
    @AttributeDefinition(name = "AWS Secret Key", description = "AWS Secret Key")
    String awsSecretKey();
}
