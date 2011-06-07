/**
 * IConfigurationService.java
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

/**
 * Defines methods to retrieve all values of configuration parameters which can
 * be set outside of the application.
 * 
 * @author faessler
 */
public interface IConfigurationService {

	public String getDatabaseServer();

	public String getDatabaseName();

	public String getDatabaseUser();

	public String getDatabasePassword();

	public String getMaxDatabaseConnections();

	public String getDatabasePort();

	public String getInitialConnections();

}
