package de.julielab.semedico.commons.services;

import org.apache.tapestry5.ioc.services.SymbolProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class FileSymbolProvider implements SymbolProvider {

	private Properties properties;

	public FileSymbolProvider(String path) throws FileNotFoundException, IOException {
		this.properties = new Properties();
		this.properties.load(new FileInputStream(path));
	}

	@Override
	public String valueForSymbol(String symbolName) {
		return properties.getProperty(symbolName);
	}

}
