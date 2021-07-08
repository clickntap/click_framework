package com.clickntap.platform;

import java.io.File;
import java.io.InputStream;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

public class S3 {

	private String accessKey;
	private String secretKey;
	private String bucketName;
	private String region;
	private AmazonS3 client = null;

	public void init() throws Exception {
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withRegion(getRegion());
		if (accessKey != null && secretKey != null) {
			builder = builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(getAccessKey(), getSecretKey())));
		}
		client = builder.build();
	}

	public String getUrl() {
		return getUrl(null);
	}

	public String getUrl(String key) {
		try {
			return client.getUrl(getBucketName(), key).toString();
		} catch (Exception e) {
			return null;
		}
	}

	public PutObjectResult upload(File file, String path) throws Exception {
		return client.putObject(new PutObjectRequest(getBucketName(), path, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}

	public PutObjectResult upload(InputStream in, String path, String contentType, long contentLength) throws Exception {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(contentType);
		metadata.setContentLength(contentLength);
		return client.putObject(new PutObjectRequest(getBucketName(), path, in, metadata).withCannedAcl(CannedAccessControlList.PublicRead));
	}

	public void delete(String path) throws Exception {
		client.deleteObject(getBucketName(), path);
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getRegion() {
		if (region == null) {
			return Regions.EU_WEST_1.getName();
		}
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

}
