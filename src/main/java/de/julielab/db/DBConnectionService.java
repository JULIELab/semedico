/**
 * DBConnectionService.java
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

package de.julielab.db;

import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_INIT_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_MAX_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_NAME;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PASSWORD;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PORT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_SERVER;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_USER;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;

/**
 * Uses a connection pool to return connections to the STAG (semantic tagging of
 * concepts) database. It is configured using symbols set either by the
 * SemedicoSymbolProvider or, if not contributed by any module, the
 * ApplicationDefaults.
 * 
 * @author faessler
 */
public class DBConnectionService implements IDBConnectionService {

	private Logger logger;

	private PGPoolingDataSource dataSource;

	public DBConnectionService(Logger logger,
			@Symbol(DATABASE_SERVER) String serverName,
			@Symbol(DATABASE_NAME) String databaseName,
			@Symbol(DATABASE_USER) String user,
			@Symbol(DATABASE_PASSWORD) String password,
			@Symbol(DATABASE_MAX_CONN) int maxConnections,
			@Symbol(DATABASE_PORT) int portNumber,
			@Symbol(DATABASE_INIT_CONN) int initialConnections) {
		this.logger = logger;
		dataSource = new PGPoolingDataSource();
		dataSource.setServerName(serverName);
		dataSource.setDatabaseName(databaseName);
		dataSource.setUser(user);
		dataSource.setPassword(password);
		dataSource.setMaxConnections(maxConnections);
		dataSource.setPortNumber(portNumber);
		dataSource.setInitialConnections(initialConnections);
		
		logger.debug("Connecting to database server {}", serverName);
		logger.debug("Connecting to database {}", databaseName);
		logger.debug("Connecting with user name {}", user);
		logger.debug("Connection with password {}", password);
	}

	public Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			logger.error(
					"No SQL connection could be obtained to: {}\n Reason: {}",
					toString(), e);
		}
		return null;
	}

	@Override
	public String toString() {
		List<String> strs = new ArrayList<String>();
		strs.add("Server:" + dataSource.getServerName());
		strs.add("Database: " + dataSource.getDatabaseName());
		strs.add("User: " + dataSource.getUser());
		strs.add("Password: " + dataSource.getPassword());
		strs.add("Max connections: " + dataSource.getMaxConnections());
		strs.add("Port number: " + dataSource.getPortNumber());
		strs.add("Initial connections: " + dataSource.getInitialConnections());
		return StringUtils.join(strs, "\n");
	}
}
