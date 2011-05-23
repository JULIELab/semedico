/** 
 * ConnectionService.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: Rico Landefeld
 * 
 * Current version: 0.1  	
 * Since version:   0.1 
 *
 * Creation date: 30.09.2007 
 * 
 * Connection factory service factory to create database connections.
 **/

package de.julielab.db;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.hivemind.ServiceImplementationFactory;
import org.apache.hivemind.ServiceImplementationFactoryParameters;


/*
 * Connection service factory to create database connections.
 * */
public class ConnectionService implements ServiceImplementationFactory {

	public ConnectionService(){}
	public Object	createCoreServiceImplementation(
			ServiceImplementationFactoryParameters factoryParams)
	{
		Log logger = factoryParams.getLog();
		Class serviceInterface = factoryParams.getServiceInterface();

//		Make sure that serviceInterface == Connection
//		(this factory is dedicated exclusively to JDBC Connections)
		if (!Connection.class.equals(serviceInterface))
		{
//			throw error
			logger.error("ConnectionFactory can only build Connection services.");
			throw new ApplicationRuntimeException("Bad interface");
		}

//		Read parameter
		Object service = factoryParams.getFirstParameter();
		if (service == null || !(service instanceof DataSource))
		{
//			throw error
			logger.error("ConnectionFactory needs one DataSource service as parameter.");
			throw new ApplicationRuntimeException("Bad DataSource interface");
		}
		DataSource ds = (DataSource) service;
		Connection _con = null;
		try {
			_con = ds.getConnection();
		} catch (Exception e) {
			throw new ApplicationRuntimeException(e);
		}		
		return _con;
	}


}

