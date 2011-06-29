/** 
 * RelatedArticlesServiceTest.java
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
 * Creation date: 22.10.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import de.julielab.semedico.core.SemedicoDocument;

public class RelatedArticlesServiceTest {

 
	@Test
	public void testFetchRelatedArticles() throws Exception{
		RelatedArticlesService relatedArticlesService = new RelatedArticlesService();
		relatedArticlesService.setDocumentService(new DocumentService());
		Collection<SemedicoDocument> relatedArticles = relatedArticlesService.fetchRelatedArticles(18387510);
		assertTrue(relatedArticles.size() > 0);
	}

}
