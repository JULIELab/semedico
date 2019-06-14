package de.julielab.semedico.core.util;

import de.julielab.semedico.commons.util.SemedicoException;

public class SearchException extends SemedicoException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6354625207540027527L;

	public SearchException() {
		super();
	}

	public SearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchException(String message) {
		super(message);
	}

	public SearchException(Throwable cause) {
		super(cause);
	}

}
