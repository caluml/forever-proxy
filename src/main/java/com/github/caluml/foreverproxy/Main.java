package com.github.caluml.foreverproxy;

import com.github.caluml.foreverproxy.storage.StoragePlugin;
import com.github.caluml.foreverproxy.storage.s3.S3StoragePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import website.magyar.mitm.proxy.ProxyServer;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashSet;


public class Main {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static void main(String[] args) throws Exception {
		ProxyServer proxyServer = new ProxyServer();
		proxyServer.setPort(8080);

		proxyServer.start(100000);
		proxyServer.setCaptureContent(true);
		proxyServer.setCaptureBinaryContent(true);
		proxyServer.setCaptureHeaders(true);


		Collection<StoragePlugin> storagePlugins = new HashSet<>();

		if (System.getProperty("storage.s3.region") != null &&
				System.getProperty("storage.s3.bucket") != null &&
				System.getProperty("storage.s3.aws-access-key-id") != null &&
				System.getProperty("storage.s3.aws-secret-access-key") != null) {
			logger.info("Enabling S3 storage plugin");
			storagePlugins.add(new S3StoragePlugin(
				System.getProperty("storage.s3.region"),
				System.getProperty("storage.s3.bucket"),
				System.getProperty("storage.s3.aws-access-key-id"),
				System.getProperty("storage.s3.aws-secret-access-key")));
		}

		proxyServer.addResponseInterceptor(new ResponseInterceptor(storagePlugins));
	}
}
