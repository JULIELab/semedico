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

package de.julielab.semedico.core.db;

import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_INIT_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_MAX_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_NAME;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PASSWORD;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PORT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_SERVER;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_USER;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;

import de.julielab.semedico.core.util.ClosableIterator;

/**
 * Uses a connection pool to return connections to the STAG (semantic tagging of
 * concepts) database. It is configured using symbols set either by the
 * SemedicoSymbolProvider or, if not contributed by any module, the
 * ApplicationDefaults.
 * 
 * @author faessler
 */
public class DBConnectionService implements IDBConnectionService {

	private final static int QUERY_BATCH_SIZE = 1000;

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
			logger.debug("Requesting database connection...");
			Connection connection = dataSource.getConnection();
			logger.debug("Database connection retrieved.");
			return connection;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.db.IDBConnectionService#tableExists(java.lang.String)
	 */
	@Override
	public boolean tableExists(Connection conn, String table) {
		try {
			conn.createStatement().executeQuery(
					"SELECT * FROM " + table + " LIMIT 1"); // Provoking
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.db.IDBConnectionService#selectRowsFromTable(java.sql.Connection
	 * , java.lang.String)
	 */
	@Override
	public Iterator<byte[][]> selectRowsFromTable(final String[] columns,
			String tableName, String whereStatement) {
		final Connection connection = getConnection();
		if (!tableExists(connection, tableName))
			throw new IllegalArgumentException("Table \"" + tableName
					+ "\" does not exist.");

		// Build the correct query.
		String query = null;
		String selectedColumns = StringUtils.join(columns, ",");
		if (whereStatement != null
				&& !whereStatement.trim().toUpperCase().startsWith("WHERE"))
			query = String.format("SELECT %s FROM %s WHERE %s",
					selectedColumns, tableName, whereStatement);
		else if (whereStatement != null)
			query = String.format("SELECT %s FROM %s %s", selectedColumns,
					tableName, whereStatement);
		else
			query = String.format("SELECT %s FROM %s", selectedColumns,
					tableName);
		final String finalQuery = query;

		try {

			ClosableIterator<byte[][]> it = new ClosableIterator<byte[][]>() {

				private Connection conn = connection;
				private ResultSet rs = doQuery(conn);
				private boolean hasNext = rs.next();

				private ResultSet doQuery(Connection conn) throws SQLException {
					// Get a statement which is set to cursor mode. The data
					// table could
					// be really large and we don't have the two fold process
					// here where
					// first we get IDs from a subset and then only the actual
					// documents
					// for these IDs.
					conn.setAutoCommit(false);
					Statement stmt = conn.createStatement();
					stmt.setFetchSize(QUERY_BATCH_SIZE);
					return stmt.executeQuery(finalQuery);
				}

				@Override
				public boolean hasNext() {
					if (!hasNext)
						close();
					return hasNext;
				}

				@Override
				public byte[][] next() {
					if (hasNext) {
						try {
							byte[][] retrievedData = new byte[columns.length][];
							for (int i = 0; i < retrievedData.length; i++) {
								retrievedData[i] = rs.getBytes(i + 1);
							}
							hasNext = rs.next();
							if (!hasNext)
								close();
							return retrievedData;
						} catch (SQLException e) {
							hasNext = false;
							e.printStackTrace();
						}
					}
					return null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public void close() {
					try {
						if (!conn.isClosed()) {
							conn.close();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			};

			return it;
		} catch (SQLException e) {
			logger.error("Error while executing SQL statement \"" + finalQuery
					+ "\"");
			e.printStackTrace();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.db.IDBConnectionService#createSchema(java.lang.String)
	 */
	@Override
	public void createSchema(String pgSchemaName) {
		if (!schemaExists(pgSchemaName)) {
			Connection connection = getConnection();
			try {
				Statement stmt = connection.createStatement();
				stmt.execute("CREATE SCHEMA " + pgSchemaName);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.db.IDBConnectionService#schemaExists(java.lang.String)
	 */
	@Override
	public boolean schemaExists(String pgSchemaName) {
		Connection connection = getConnection();
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT " + COL_NAMESPACE_NAME
					+ " FROM " + TABLE_PG_NAMESPACE + " WHERE "
					+ COL_NAMESPACE_NAME + "='" + pgSchemaName.toLowerCase() + "'");
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
