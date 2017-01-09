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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.services.interfaces.IExternalLinkService;

public class ExternalLinkServiceTest {

	private final static Logger log = LoggerFactory
			.getLogger(ExternalLinkServiceTest.class);

	private IExternalLinkService externalLinkService;

	@Before
	public void beforeMethod() {
		// We assume that we have a Neo4j server up and running with the correct
		// data for this test. If this is not the case, skip the tests.
		org.junit.Assume.assumeTrue(isNeo4jServerReachable());
		externalLinkService = new ExternalLinkService(
				LoggerFactory.getLogger(ExternalLinkService.class));
	}

	private boolean isNeo4jServerReachable() {
		boolean reachable = false;
		try {
			URLConnection connection = new URL(
					ExternalLinkService.EUTILS_LLINKS_URL).openConnection();
			connection.connect();
			// If we've come this far without an exception, the connection is
			// available.
			reachable = true;
		} catch (ConnectException | UnknownHostException e) {
			// don't do anything, the warning will be logged below.
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!reachable)
			log.warn(
					"EXTERNAL LINKS SERVICE TESTS ARE NOT PERFORMED BECAUSE THE ADDRESS {} COULD NOT BE REACHED.",
					ExternalLinkService.EUTILS_LLINKS_URL);
		return reachable;
	}

	@Test
	public void testFetchExternalLinks() throws Exception {

		Collection<ExternalLink> result = externalLinkService
				.fetchExternalLinks("18789008");
		assertNotNull(result);
		assertEquals(0, result.size());

		result = externalLinkService.fetchExternalLinks("18809684");
		assertNotNull(result);
		// There were three external links as of 2013-07-08.
		assertTrue(result.size() >= 3);

		ExternalLink link = result.iterator().next();
		assertEquals(
				"http://www.jbc.org/cgi/pmidlookup?view=long&pmid=18809684",
				link.getUrl());
		// The exact URL changes, so let's just say we want 'something'. If we
		// have it, it's hopefully the correct URL.
		assertTrue(!StringUtils.isBlank(link.getIconUrl()));
	}

}
