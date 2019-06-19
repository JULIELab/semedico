package de.julielab.semedico.core.services;

import org.apache.tapestry5.ioc.services.SymbolProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * A symbol provider that takes an array where the even indexes hold symbol
 * names and the odd indexes hold the value to the previous symbol.
 * 
 * @author faessler
 *
 */
public class ArraySymbolProvider implements SymbolProvider {

	private Map<String, String> configuration;

	/**
	 * 
	 * @param symbolValuePairs
	 *            An array where the even indexes hold symbol names and the odd
	 *            indexes hold the value to the previous symbol.
	 */
	public ArraySymbolProvider(String... symbolValuePairs) {
		if (symbolValuePairs.length % 2 != 0)
			throw new IllegalArgumentException(
					"An array of symbol-value pairs is expected but the number of array elements is odd.");
		configuration = new HashMap<>();
		for (int i = 0; i < symbolValuePairs.length; i++) {
			if (i % 2 == 0) {
				String symbol = symbolValuePairs[i];
				String value = symbolValuePairs[i + 1];
				configuration.put(symbol, value);
			}
		}
	}

	@Override
	public String valueForSymbol(String symbolName) {
		return configuration.get(symbolName);
	}


}
