package com.github.caluml.foreverproxy;

import com.github.caluml.foreverproxy.storage.StorageException;
import com.github.caluml.foreverproxy.storage.StoragePlugin;
import net.lightbody.bmp.core.har.HarContent;
import net.lightbody.bmp.core.har.HarResponse;
import net.lightbody.bmp.proxy.jetty.util.URI;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import website.magyar.mitm.proxy.http.MitmJavaProxyHttpResponse;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Collection;

public class ResponseInterceptor implements website.magyar.mitm.proxy.ResponseInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


	private final Collection<StoragePlugin> storagePlugins;

	public ResponseInterceptor(Collection<StoragePlugin> storagePlugins) {
		this.storagePlugins = storagePlugins;
	}


	@Override
	public void process(MitmJavaProxyHttpResponse response) {
		byte[] bytes = response.getBodyBytes();
		if (bytes == null) return;

		HarResponse harResponse = response.getEntry().getResponse();
		HarContent content = harResponse.getContent();

		Instant timestamp = response.getEntry().getStartedDateTime().toInstant();
		Header[] requestHeaders = response.getRequestHeaders();
		Header[] responseHeaders = response.getHeaders();

		URI proxyRequestURI = response.getProxyRequestURI();
		String mimeType = content.getMimeType();

		for (StoragePlugin storagePlugin : storagePlugins) {
			try {
				storagePlugin.storeResponse(timestamp, requestHeaders, responseHeaders, proxyRequestURI, mimeType, bytes);
			} catch (StorageException e) {
				logger.error("Error storing response", e);
			}
		}
	}
}
