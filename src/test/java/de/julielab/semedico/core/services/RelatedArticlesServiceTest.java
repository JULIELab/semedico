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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import de.julielab.semedico.core.SemedicoDocument;

public class RelatedArticlesServiceTest {

	/**
	 * PMID of the document we want to get related articles for.
	 */
	public static Integer testPmid = 10782842;
	/**
	 * Path to the XML holding related articles. This XML has been fetched by
	 * NLM E-Utils and stored to file for this test.
	 */
	public static String relatedArticlesXMLPath = "src/test/resources/relatedArticles.xml";

	@Test
	public void testReadRelatedArticles() throws Exception{
		IDocumentService documentService = createMock(IDocumentService.class);
		expect(documentService.getSemedicoDocument(10782842)).andReturn(new SemedicoDocument(10782842));
		expect(documentService.getSemedicoDocument(10953975)).andReturn(new SemedicoDocument(10953975));
		expect(documentService.getSemedicoDocument(11493382)).andReturn(new SemedicoDocument(11493382));
		expect(documentService.getSemedicoDocument(12729371)).andReturn(new SemedicoDocument(12729371));
		expect(documentService.getSemedicoDocument(8432509)).andReturn(new SemedicoDocument(8432509));
		expect(documentService.getSemedicoDocument(16371804)).andReturn(new SemedicoDocument(16371804));
		replay(documentService);
		RelatedArticlesService relatedArticlesService = new RelatedArticlesService(LoggerFactory.getLogger(RelatedArticlesService.class), documentService);
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(new FileInputStream(relatedArticlesXMLPath));
		Collection<SemedicoDocument> relatedArticles = relatedArticlesService.readRelatedArticles(testPmid, document);
		verify(documentService);
		assertTrue(relatedArticles.size() == IRelatedArticlesService.MAX_RELATED_ARTICLES);
	}
}
