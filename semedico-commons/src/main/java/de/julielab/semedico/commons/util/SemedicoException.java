package de.julielab.semedico.commons.util;

/**
 * Top exception to be thrown from Semedico-specific code. Subclasses should have more helpful names.
 * @author faessler
 *
 */
public class SemedicoException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1100928417784984465L;

	public SemedicoException() {
		super();
	}

	public SemedicoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SemedicoException(String message, Throwable cause) {
		super(message, cause);
	}

	public SemedicoException(String message) {
		super(message);
	}

	public SemedicoException(Throwable cause) {
		super(cause);
	}

}
