package de.julielab.semedico.core.services.interfaces;

import java.util.Optional;

import org.apache.tapestry5.ioc.services.SymbolProvider;

public interface ReconfigurableService {
	void configure(SymbolProvider symbolProvider);

	default Optional<Boolean> getBoolean(String symbol, SymbolProvider symbolProvider) {
		if (null == symbolProvider || symbolProvider.valueForSymbol(symbol) == null)
			return Optional.empty();
		return Optional.of(Boolean.parseBoolean(symbolProvider.valueForSymbol(symbol)));
	}

	default <T extends Enum<T>> Optional<T> getEnum(String symbol, SymbolProvider symbolProvider, Class<T> enumCls) {
		String symbolValue = symbolProvider.valueForSymbol(symbol);
		if (symbolValue == null)
			return Optional.empty();
		T enumValue = null;
		// We now try the exact given symbol, and, if this is not found, the
		// upper case and lower case variants.
		try {
			enumValue = Enum.valueOf(enumCls, symbolValue);
		} catch (IllegalArgumentException e) {
			try {
				enumValue = Enum.valueOf(enumCls, symbolValue.toUpperCase());
			} catch (IllegalArgumentException e1) {
				enumValue = Enum.valueOf(enumCls, symbolValue.toLowerCase());
			}
		}
		return Optional.ofNullable(enumValue);
	}

	default Optional<Integer> getInteger(String symbol, SymbolProvider symbolProvider) {
		if (null == symbolProvider || symbolProvider.valueForSymbol(symbol) == null)
			return Optional.empty();
		return Optional.of(Integer.parseInt(symbolProvider.valueForSymbol(symbol)));
	}

	default Optional<String> getString(String symbol, SymbolProvider symbolProvider) {
		if (null == symbolProvider || symbolProvider.valueForSymbol(symbol) == null)
			return Optional.empty();
		return Optional.of(symbolProvider.valueForSymbol(symbol));
	}

}
