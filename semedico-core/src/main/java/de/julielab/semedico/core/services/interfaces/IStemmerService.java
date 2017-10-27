package de.julielab.semedico.core.services.interfaces;

public interface IStemmerService {
	/**
	 * Returns the stemmed form of <tt>token</tt>. The actual used stemming algorithm is up to the implementation.
	 * 
	 * @param token
	 * @return
	 */
	String stem(String token);
}
