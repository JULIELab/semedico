/** 
 * ExternalLinkServiceTest.java
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
 * Creation date: 21.10.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.services.interfaces.IExternalLinkService;

public class ExternalLinkServiceTest {

	private IExternalLinkService externalLinkService;
	
	@Before
	public void setUp(){
		externalLinkService = new ExternalLinkService(LoggerFactory.getLogger(ExternalLinkService.class));
	}
	
	@Test
	public void testFetchExternalLinks() throws Exception{

		Collection<ExternalLink> result = externalLinkService.fetchExternalLinks(18789008);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = externalLinkService.fetchExternalLinks(18809684);
		assertNotNull(result);
		assertEquals(1, result.size());

		ExternalLink link = result.iterator().next();
		assertEquals("http://www.jbc.org/cgi/pmidlookup?view=long&pmid=18809684", link.getUrl());
		assertEquals("http://www.ncbi.nlm.nih.gov/entrez/query/egifs/http:--highwire.stanford.edu-icons-externalservices-pubmed-standard-jbc_full_free.gif", link.getIconUrl());
	}
	

}
