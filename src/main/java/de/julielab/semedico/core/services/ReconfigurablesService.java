package de.julielab.semedico.core.services;

import java.util.Optional;

import org.apache.tapestry5.ioc.services.SymbolSource;

public interface ReconfigurablesService {
	void configure(SymbolSource symbolSource);
	void configure(SymbolSource symbolSource, boolean recursive);
	
	default Optional<Boolean> getBoolean(String symbol, SymbolSource symbolSource) {
		if (null == symbolSource || symbolSource.valueForSymbol(symbol) == null)
			return Optional.empty();
		return Optional.of(Boolean.parseBoolean(symbolSource.valueForSymbol(symbol)));
	}
	
	
	default Optional<Integer> getInteger(String symbol, SymbolSource symbolSource) {
		if (null == symbolSource || symbolSource.valueForSymbol(symbol) == null)
			return Optional.empty();
		return Optional.of(Integer.parseInt(symbolSource.valueForSymbol(symbol)));
	}
	
	
	default Optional<String> getString(String symbol, SymbolSource symbolSource) {
		if (null == symbolSource || symbolSource.valueForSymbol(symbol) == null)
			return Optional.empty();
		return Optional.of(symbolSource.valueForSymbol(symbol));
	}
	
}
