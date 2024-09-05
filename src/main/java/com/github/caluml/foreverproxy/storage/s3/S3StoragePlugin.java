package com.github.caluml.foreverproxy.storage.s3;

import com.github.caluml.foreverproxy.storage.StorageException;
import com.github.caluml.foreverproxy.storage.StoragePlugin;
import net.lightbody.bmp.proxy.jetty.util.URI;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class S3StoragePlugin implements StoragePlugin {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	final String bucket;

	final S3AsyncClient s3AsyncClient;

	public S3StoragePlugin(String region,
												 String bucket,
												 String awsAccessKeyId,
												 String awsSecretAccessKey) {
		this.bucket = bucket;

		if (awsAccessKeyId == null) throw new RuntimeException("AWS Access Key ID not set");
		if (awsSecretAccessKey == null) throw new RuntimeException("AWS Secret Access Key not set");

		AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
			AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey));

		s3AsyncClient = S3AsyncClient.builder()
			.region(Region.of(region))
			.credentialsProvider(credentialsProvider)
			.build();

		logger.info("Instantiated {}", this);
	}

	@Override
	public void storeResponse(Instant timestamp,
														Header[] requestHeaders,
														Header[] responseHeaders,
														URI proxyRequestURI,
														String mimeType,
														byte[] bytes) throws StorageException {
		try {
			Map<String, String> metadata = new HashMap<>();
			metadata.put("timestamp", timestamp.toString());
			metadata.put("request-headers", Arrays.toString(requestHeaders));
			metadata.put("response-headers", Arrays.toString(responseHeaders));

			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucket)
				.key(uriKey(proxyRequestURI))
				.contentType(mimeType)
				.metadata(metadata)
				.contentMD5(Base64.encodeBase64String(DigestUtils.md5(bytes)))
				.build();

			s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes(bytes))
				.thenAccept(putObjectResponse -> {
					logger.debug("Saved {} bytes to s3://{}/{}", bytes.length, bucket, putObjectRequest.key());
				});
		} catch (Exception e) {
			throw new StorageException(e);
		}
	}

	private String uriKey(URI uri) {
		return uri.getScheme() + "/" + uri.getHost() + uri.getPath();
	}
}
