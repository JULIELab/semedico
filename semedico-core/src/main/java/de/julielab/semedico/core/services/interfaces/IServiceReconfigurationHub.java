package de.julielab.semedico.core.services.interfaces;

import org.apache.tapestry5.ioc.services.SymbolProvider;

public interface IServiceReconfigurationHub {
	void registerService(ReconfigurableService service);
	void reconfigureServices(SymbolProvider symbolProvider);
}
