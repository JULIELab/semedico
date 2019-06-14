package de.julielab.semedico.core.util;

import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;

public class ReconfigurationSymbolProvider implements SymbolProvider {

	private SymbolSource symbolSource;


	public ReconfigurationSymbolProvider(SymbolSource symbolSource) {
		this.symbolSource = symbolSource;
	}
	
	
	@Override
	public String valueForSymbol(String symbolName) {
		return symbolSource.valueForSymbol(symbolName);
	}

}
