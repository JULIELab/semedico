package de.julielab.semedico.core.util;

import de.julielab.semedico.commons.util.SemedicoException;

public class DocumentModelAccessException extends SemedicoException{
    public DocumentModelAccessException() {
    }

    public DocumentModelAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DocumentModelAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentModelAccessException(String message) {
        super(message);
    }

    public DocumentModelAccessException(Throwable cause) {
        super(cause);
    }
}
