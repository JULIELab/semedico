package de.julielab.semedico.core.services;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import de.julielab.semedico.core.services.interfaces.IHttpClientService;

public class HttpClientService implements IHttpClientService {

	private HttpClient client;
	private Logger log;

	public HttpClientService(Logger log) {
		this.log = log;
		// Create a connection manager with custom configuration.
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		client = HttpClients.custom().setConnectionManager(connManager).build();
	}

	@Override
	public HttpEntity sendPostRequest(HttpPost reusablePost, String request, String[] headers) {
		if (!StringUtils.isBlank(request)) {
			reusablePost.setEntity(new StringEntity(request, Charset.forName("UTF-8")));
		}
		return sendRequest(reusablePost, headers);
	}

	@Override
	public HttpEntity sendPostRequest(String address, String request, String[] headers) {
		HttpPost post = new HttpPost(address);
		post.setHeader("Content-type", "application/json");
		return sendPostRequest(post, request, headers);
	}

	@Override
	public HttpEntity sendPostRequest(String address, String[] headers) {
		HttpPost post = new HttpPost(address);
		return sendPostRequest(post, null, headers);
	}

	@Override
	public HttpEntity sendRequest(HttpRequestBase request, String[] headers) {
		request.reset();
		for (int i = 0; null != headers && i < headers.length; i += 2) {
			String headerName = headers[i];
			if (i >= headers.length)
				throw new IllegalArgumentException("No value for header \"" + headerName + "\" was given.");
			String headerValue = headers[i+1];
			request.addHeader(headerName, headerValue);
		}
		try {
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			// We take all 200 values with us, because 204 is not really an
			// error. To get specific return codes, see HttpStatus
			// constants.
			if (response.getStatusLine().getStatusCode() < 300) {
				return entity;
			}
			String responseString = EntityUtils.toString(entity);
			log.error("Error when posting a request to the server: {}",
					null != entity && !StringUtils.isBlank(responseString) ? responseString : response.getStatusLine());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (HttpHostConnectException e) {
			log.error("Could not connect to HTTP host: {}", e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public HttpEntity sendPostRequest(HttpPost reusablePost, String request) {
		return sendPostRequest(reusablePost, request, null);
	}

	@Override
	public HttpEntity sendPostRequest(String address, String request) {
		return sendPostRequest(address, request, null);
	}

	@Override
	public HttpEntity sendPostRequest(String address) {
		return sendPostRequest(address, (String[])null);
	}

	@Override
	public HttpEntity sendRequest(HttpRequestBase request) {
		return sendRequest(request, null);
	}
}
