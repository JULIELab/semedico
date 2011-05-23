/** 
 * IndexSearcherFactory.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 31.07.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.lucene;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.hivemind.ServiceImplementationFactory;
import org.apache.hivemind.ServiceImplementationFactoryParameters;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

public class IndexSearcherFactory implements ServiceImplementationFactory {

	@Override
	public Object createCoreServiceImplementation(
			ServiceImplementationFactoryParameters factoryParams) {

		Log logger = factoryParams.getLog();
		Class serviceInterface = factoryParams.getServiceInterface();

//		Make sure that serviceInterface == Connection
//		(this factory is dedicated exclusively to JDBC Connections)
		if (!IIndexSearcherWrapper.class.equals(serviceInterface))
		{
//			throw error
			logger.error("IndexSearcherFactory can only build IndexSearchWrapper services.");
			throw new ApplicationRuntimeException("Bad interface");
		}

//		Read parameter
		Object directoryPath = factoryParams.getFirstParameter();
		if (directoryPath == null || !(directoryPath instanceof String))
		{
//			throw error
			logger.error("IndexSearcherFactory needs one directoryPath as parameter.");
			throw new ApplicationRuntimeException("bad directory path parameter");
		}
		IndexSearcher searcher = null;
		try {
			searcher = new IndexSearcher(FSDirectory.open(new File((String)directoryPath)));
		} catch (Exception e) {
			throw new ApplicationRuntimeException(e);
		}		
		return new IndexSearcherWrapper(searcher);		
	}

}
