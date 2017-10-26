package de.julielab.semedico.state;

import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.services.ApplicationStateCreator;

import de.julielab.semedico.core.services.interfaces.IDocumentRetrievalSearchStateCreator;
import de.julielab.semedico.core.services.interfaces.IDocumentRetrievalUserInterfaceCreator;

public class SemedicoSessionStateCreator implements ApplicationStateCreator<SemedicoSessionState> {

	private IDocumentRetrievalSearchStateCreator docRetrievalSSCreator;
	private IDocumentRetrievalUserInterfaceCreator docRetrievalUiStateCreator;
	private LoggerSource loggerSource;

	public SemedicoSessionStateCreator(LoggerSource loggerSource,
			IDocumentRetrievalSearchStateCreator docRetrievalSSCreator,
			IDocumentRetrievalUserInterfaceCreator docRetrievalUiStateCreator) {
		this.loggerSource = loggerSource;
		this.docRetrievalSSCreator = docRetrievalSSCreator;
		this.docRetrievalUiStateCreator = docRetrievalUiStateCreator;
	}

	@Override
	public SemedicoSessionState create() {
		return new SemedicoSessionState(loggerSource.getLogger(SemedicoSessionState.class), docRetrievalSSCreator,
				docRetrievalUiStateCreator);
	}

}
