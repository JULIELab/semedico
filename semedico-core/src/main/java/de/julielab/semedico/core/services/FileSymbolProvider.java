package de.julielab.semedico.core.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.tapestry5.ioc.services.SymbolProvider;

public class FileSymbolProvider implements SymbolProvider {

	private Properties properties;

	public FileSymbolProvider(String path) throws IOException {
		this.properties = new Properties();
		try (FileInputStream fis = new FileInputStream(path)) {
			this.properties.load(fis);
		}
	}

	@Override
	public String valueForSymbol(String symbolName) {
		return properties.getProperty(symbolName);
	}

}
