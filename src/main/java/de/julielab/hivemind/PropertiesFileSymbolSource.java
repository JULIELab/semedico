package de.julielab.hivemind;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.hivemind.SymbolSource;
import org.apache.log4j.Logger;

public class PropertiesFileSymbolSource implements SymbolSource {

	private static Logger LOG = Logger.getLogger(PropertiesFileSymbolSource.class);
	
	public static final String FILE_KEY = PropertiesFileSymbolSource.class.getCanonicalName() + ".file";
	private Properties properties;

	
	public PropertiesFileSymbolSource() {
		
		this.properties = new Properties();
		loadProperties();
	}
	
	void loadProperties() {
		String filePath =  System.getProperties().getProperty(FILE_KEY);
		if (filePath == null) {
			LOG.error("Java system property \"" + FILE_KEY + "\" not set. Please set this property to your configuration file, e.g. app.properties. Program will exit.");
			System.exit(1);
		}
		File propertiesFile = new File(filePath);
		if( propertiesFile.exists() ){
			FileInputStream fileInputStream;
			try {
				fileInputStream = new FileInputStream(propertiesFile);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
				properties.load(bufferedInputStream);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		else {
			LOG.error("Java system propery \"" + FILE_KEY + "\" points to \"" + filePath + "\" which does not exist. Please deliver an existing configuration file. Program will exit.");
			System.exit(1);
		}
	}

	@Override
	public String valueForSymbol(String symbol) {	
		return properties.getProperty(symbol);
	}

}
