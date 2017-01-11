package de.julielab.semedico.core.services;

import org.apache.tapestry5.ioc.internal.services.ClasspathResourceSymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.slf4j.Logger;

public class ConfigurationSymbolProvider implements SymbolProvider {

	private static final String CONFIG_FILE_PROPERTY = "semedico.configuration";
	private SymbolProvider symbolProvider;

	public ConfigurationSymbolProvider(Logger log) {
		String configFileName = System.getProperty(CONFIG_FILE_PROPERTY);
		try {
			if (null == configFileName) {
				String username = System.getProperty("user.name");
				configFileName = "configuration.properties." + username;
				log.info(
						"System property {} for configuration location undefined. Looking for classpath resource {} for a configuration.",
						CONFIG_FILE_PROPERTY, configFileName);
			} else {
				log.info("Classpath resource {} was given as configuration location", configFileName);
			}
			this.symbolProvider = new ClasspathResourceSymbolProvider(configFileName);
		} catch (NullPointerException e) {
			log.error(
					"No configuration file found in the classpath. A configuration file as classpath resource must either be given by the {} system property or there must exist a file named {} where user.name is the system user name system property.",
					CONFIG_FILE_PROPERTY, "configuration.properties.user.name");
		}
		log.info("Found configuration file as classpath resource at {}", configFileName);
	}

	@Override
	public String valueForSymbol(String symbolName) {
		return symbolProvider.valueForSymbol(symbolName);
	}

}
