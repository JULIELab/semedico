/**
 * ConfigurationService.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 06.06.2011
 **/

package de.julielab.semedico.core.services;

import java.util.Properties;

import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.slf4j.Logger;

/**
 * This implementation of <code>SymbolProvider</code> tries to locate the file
 * {@link #CONFIG_FILE_NAME} on the class path. If found, it is loaded in a Java
 * <code>Properties</code> object and returns the property values through the
 * interface method {@link #valueForSymbol(String)}.
 * 
 * @author faessler
 */
public class SemedicoSymbolProvider implements SymbolProvider {

	/**
	 * The name of the file ({@value #CONFIG_FILE_NAME}) which should hold the configuration values. It
	 * should be in a format which can be loaded by a <code>Properties</code>
	 * object. The file must be located on the class path in order to be found.
	 */
	public static final String CONFIG_FILE_NAME = "/WEB-INF/configuration.properties";

	public static final String DATABASE_SERVER = "semedico.database.server";
	public static final String DATABASE_NAME = "semedico.database.name";
	public static final String DATABASE_USER = "semedico.database.user";
	public static final String DATABASE_PASSWORD = "semedico.database.password";
	public static final String DATABASE_MAX_CONN = "semedico.database.maxConnections";
	public static final String DATABASE_PORT = "semedico.database.port";
	public static final String DATABASE_INIT_CONN = "semedico.database.initialConnections";
	
	public static final String STOP_WORDS_FILE = "semedico.search.stopwords.file";
	public static final String DISAMBIGUATION_DICT_FILE = "semedico.query.dictionary.file";
	
	public static final String SOLR_URL = "semedico.solr.url";
	public static final String SEARCH_MAX_NUMBER_DOC_HITS = "semedico.core.search.maxNumberOfDocumentHits";
	public static final String SEARCH_MAX_FACETTED_DOCS = "semedico.core.search.maxFacettedDocuments";
	
	@Deprecated
	public static final String SPELLING_DICT = "semedico.spelling.dictionary.file";

	private Properties properties;

	public SemedicoSymbolProvider(Logger logger, Properties properties) {
		this.properties = properties;
		logger.info("User defined configuration properties:\n{}", properties.toString().replaceAll(",", "\n"));

		// In the case there are no properties we just create an empty
		// Properties object. The symbol source will return null for each
		// request, effectively delegating the request to the
		// ApplicationDefaults (see SemedicoCoreModule).
		if (properties == null) {
			logger.info("No configuration has been contributed to {}. ApplicationDefaults will be used.", getClass().getName());
			this.properties = new Properties();
		}
		// InputStream is = getClass().getResourceAsStream(CONFIG_FILE_NAME);
		//
		// if (is == null)
		// logger.error(
		// "The configuration file \"{}\" was not found on the classpath. Please provide a configuration file",
		// CONFIG_FILE_NAME);
		//
		// properties = new Properties();
		// try {
		// properties.load(is);
		// } catch (IOException e) {
		// logger.error(
		// "The configuration file \"{}\" could not be loaded by a Java Properties object: {}",
		// CONFIG_FILE_NAME, e);
		// }
	}

	// @Override
	// public String getDatabaseServer() {
	// String databaseServer = properties.getProperty(DATABASE_SERVER);
	// if (databaseServer == null) {
	// logger.error(
	// "The database server has not been set in the configuration file {}. Please set an appropriate value to the configuration parameter {}.",
	// CONFIG_FILE_NAME, DATABASE_SERVER);
	// }
	// return databaseServer;
	// }
	//
	// @Override
	// public String getDatabaseName() {
	// String databaseName = properties.getProperty(DATABASE_NAME);
	// return databaseName;
	// }
	//
	// @Override
	// public String getDatabaseUser() {
	// String databaseUser = properties.getProperty(DATABASE_USER);
	// return databaseUser;
	// }
	//
	// @Override
	// public String getDatabasePassword() {
	// String databasePassword = properties.getProperty(DATABASE_PASSWORD);
	// return databasePassword;
	// }
	//
	// @Override
	// public String getMaxDatabaseConnections() {
	// String databaseMaxConn = properties.getProperty(DATABASE_MAX_CONN);
	// return databaseMaxConn;
	// }
	//
	// @Override
	// public String getDatabasePort() {
	// String databasePort = properties.getProperty(DATABASE_PORT);
	// return databasePort;
	// }
	//
	// @Override
	// public String getInitialConnections() {
	// String databaseInitConn = properties.getProperty(DATABASE_INIT_CONN);
	// return databaseInitConn;
	// }
	//
	@Override
	public String valueForSymbol(String symbol) {
		return properties.getProperty(symbol);
	}

}
