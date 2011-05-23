/**
 * SolrServerFactory.java
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
 * Creation date: 27.04.2011
 **/

package de.julielab.solr;

import org.apache.commons.logging.Log;
import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.hivemind.ServiceImplementationFactory;
import org.apache.hivemind.ServiceImplementationFactoryParameters;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;

/**
 * Factory which simply creates a SolrServer wrapped in a SolrServerWrapper for use with hivemind.
 * @author faessler
 */
public class SolrServerFactory implements ServiceImplementationFactory {

	@Override
	public Object createCoreServiceImplementation(
			ServiceImplementationFactoryParameters factoryParams) {
		Log logger = factoryParams.getLog();
		Class<?> serviceInterface = factoryParams.getServiceInterface();

//		Make sure that serviceInterface == Connection
//		(this factory is dedicated exclusively to JDBC Connections)
		if (!ISolrServerWrapper.class.equals(serviceInterface))
		{
//			throw error
			logger.error("SolrServerFactory can only build SolrServerWrapper services.");
			throw new ApplicationRuntimeException("Bad interface");
		}

//		Read parameter
		Object serverUrl = factoryParams.getFirstParameter();
		if (serverUrl == null || !(serverUrl instanceof String))
		{
//			throw error
			logger.error("IndexSearcherFactory needs one directoryPath as parameter.");
			throw new ApplicationRuntimeException("bad directory path parameter");
		}
		CommonsHttpSolrServer solr = null;
		try {
			solr = new CommonsHttpSolrServer((String)serverUrl);
		} catch (Exception e) {
			throw new ApplicationRuntimeException(e);
		}
		solr.setRequestWriter(new BinaryRequestWriter());
		return new SolrServerWrapper(solr);		
	}
}
