package de.julielab.semedico.core.util;

import de.julielab.semedico.commons.util.SemedicoException;

public class ConceptLoadingException extends SemedicoException {
    public ConceptLoadingException() {
    }

    public ConceptLoadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConceptLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConceptLoadingException(String message) {
        super(message);
    }

    public ConceptLoadingException(Throwable cause) {
        super(cause);
    }
}
