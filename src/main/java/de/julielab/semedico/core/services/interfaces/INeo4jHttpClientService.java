package de.julielab.semedico.core.services.interfaces;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This interface does nothing else than to extend {@link IHttpClientService}, so the exact same methods are offered.
 * The actual difference is the implementation of this interface that adds the Neo4j authentication header to each HTTP
 * request for transparent REST access to the Neo4j server.
 * 
 * @author faessler
 * 
 */
public interface INeo4jHttpClientService extends IHttpClientService {

	/**
	 * Marker annotation that this version of the HttpClientService is dedicated for the communication with Neo4j.
	 * 
	 * @author faessler
	 * 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Neo4jHttpClient {
		//
	}

	// same methods
}
