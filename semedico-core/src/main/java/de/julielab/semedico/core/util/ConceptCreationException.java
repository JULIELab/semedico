package de.julielab.semedico.core.util;

import de.julielab.semedico.commons.util.SemedicoException;

public class ConceptCreationException extends SemedicoException {
    public ConceptCreationException() {
    }

    public ConceptCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConceptCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConceptCreationException(String message) {
        super(message);
    }

    public ConceptCreationException(Throwable cause) {
        super(cause);
    }
}
