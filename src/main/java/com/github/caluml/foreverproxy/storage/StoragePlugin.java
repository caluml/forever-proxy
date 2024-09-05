package com.github.caluml.foreverproxy.storage;

import net.lightbody.bmp.proxy.jetty.util.URI;
import org.apache.http.Header;

import java.time.Instant;

public interface StoragePlugin {

	void storeResponse(Instant timestamp,
										 Header[] requestHeaders,
										 Header[] responseHeaders,
										 URI proxyRequestURI,
										 String mimeType,
										 byte[] bytes) throws StorageException;
}
