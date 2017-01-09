package de.julielab.semedico.core.services;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;

public class Neo4jHttpClientService extends HttpClientService implements INeo4jHttpClientService {

	private String authorizationToken;

	public Neo4jHttpClientService(Logger log, @Symbol(SemedicoSymbolConstants.NEO4J_USERNAME) String neo4jUsername,
			@Symbol(SemedicoSymbolConstants.NEO4J_PASSWORD) String neo4jPassword) {
		super(log);
		this.authorizationToken =
				"Basic " + Base64.encodeBase64URLSafeString((neo4jUsername + ":" + neo4jPassword).getBytes());
	}

	@Override
	public HttpEntity sendPostRequest(HttpPost reusablePost, String request, String[] headers) {
		String[] extendedHeaders = extendHeadersByAuthorization(headers);
		return super.sendPostRequest(reusablePost, request, extendedHeaders);
	}

	@Override
	public HttpEntity sendPostRequest(String address, String[] headers) {
		String[] extendedHeaders = extendHeadersByAuthorization(headers);
		return super.sendPostRequest(address, extendedHeaders);
	}

	@Override
	public HttpEntity sendRequest(HttpRequestBase request, String[] headers) {
		String[] extendedHeaders = extendHeadersByAuthorization(headers);
		return super.sendRequest(request, extendedHeaders);
	}

	@Override
	public HttpEntity sendPostRequest(String address, String request, String[] headers) {
		String[] extendedHeaders = extendHeadersByAuthorization(headers);
		return super.sendPostRequest(address, request, extendedHeaders);
	}

	@Override
	public HttpEntity sendPostRequest(HttpPost reusablePost, String request) {
		String[] extendedHeaders = extendHeadersByAuthorization(null);
		return super.sendPostRequest(reusablePost, request, extendedHeaders);
	}

	@Override
	public HttpEntity sendPostRequest(String address, String request) {
		String[] extendedHeaders = extendHeadersByAuthorization(null);
		return super.sendPostRequest(address, request, extendedHeaders);
	}

	@Override
	public HttpEntity sendPostRequest(String address) {
		String[] extendedHeaders = extendHeadersByAuthorization(null);
		return super.sendPostRequest(address, extendedHeaders);
	}

	@Override
	public HttpEntity sendRequest(HttpRequestBase request) {
		String[] extendedHeaders = extendHeadersByAuthorization(null);
		return super.sendRequest(request, extendedHeaders);
	}

	/**
	 * Extends the <tt>headers</tt> array by the Neo4j authorization header. The authorization values have to be defined
	 * in the Semedico configuration.
	 * 
	 * @param headers
	 *            Existing headers to be passed along.
	 * @return The input <tt>headers</tt> array extended by the authorization header.
	 */
	private String[] extendHeadersByAuthorization(String[] headers) {
		String[] extendedHeaders = copyHeadersForExtension(headers, 2);
		int headersLength = null != headers ? headers.length : 0;
		extendedHeaders[headersLength] = "Authorization";
		extendedHeaders[headersLength + 1] = authorizationToken;
		return extendedHeaders;
	}

	/**
	 * Returns an array of size
	 * <code>headers.length + extensionSize<code>, copying the headers into the first <code>headers.length</code> array
	 * cells, leaving the tail of the extended array free for further headers.
	 * 
	 * @param headers
	 *            existing headers
	 * @param extensionSize
	 *            number of new headers * 2 for header names and header values.
	 * @return The extended array, including <tt>headers</tt> and <tt>extensionSize</tt> more cells.
	 */
	private String[] copyHeadersForExtension(String[] headers, int extensionSize) {
		int arrayLength = null != headers ? headers.length + extensionSize : extensionSize;
		String[] extendedHeaders = new String[arrayLength];
		if (null != headers)
			System.arraycopy(headers, 0, extendedHeaders, 0, headers.length);
		return extendedHeaders;
	}

}
