package de.julielab.semedico.core.services.interfaces;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

public interface IHttpClientService {

	/**
	 * Marker annotation to determine the general HttpClientService in contrast to server specific versions, e.g. to
	 * communicate with Neo4j.
	 * 
	 * @author faessler
	 * 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface GeneralHttpClient {
		//
	}

	HttpEntity sendPostRequest(HttpPost reusablePost, String request, String[] headers);

	HttpEntity sendPostRequest(String address, String request, String[] headers);

	HttpEntity sendPostRequest(String address, String[] headers);

	HttpEntity sendRequest(HttpRequestBase request, String[] headers);

	HttpEntity sendPostRequest(HttpPost reusablePost, String request);

	HttpEntity sendPostRequest(String address, String request);

	HttpEntity sendPostRequest(String address);

	HttpEntity sendRequest(HttpRequestBase request);

}
