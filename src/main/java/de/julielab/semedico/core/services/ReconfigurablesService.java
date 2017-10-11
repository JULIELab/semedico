package de.julielab.semedico.core.services;

import org.apache.tapestry5.ioc.services.SymbolSource;

public interface ReconfigurablesService {
	void configure(SymbolSource symbolSource);
	void configure(ServiceConfiguration configuration);
	void configure(ServiceConfiguration configuration, boolean recursive);
}
