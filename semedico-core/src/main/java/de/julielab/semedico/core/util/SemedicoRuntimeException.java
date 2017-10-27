package de.julielab.semedico.core.util;

public class SemedicoRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3937607079825114941L;

	public SemedicoRuntimeException() {
		super();
	}

	public SemedicoRuntimeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SemedicoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SemedicoRuntimeException(String message) {
		super(message);
	}

	public SemedicoRuntimeException(Throwable cause) {
		super(cause);
	}

}
