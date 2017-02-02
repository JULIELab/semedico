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
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.concurrent.Future;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.IRelatedArticlesService;
import de.julielab.semedico.core.services.interfaces.ISearchService;

public class RelatedArticlesServiceTest {

	private Logger log = LoggerFactory.getLogger(RelatedArticlesServiceTest.class);

	/**
	 * PMID of the document we want to get related articles for.
	 */
	public static String testPmid = "10782842";
	/**
	 * Path to the XML holding related articles. This XML has been fetched by NLM E-Utils and stored to file for this
	 * test.
	 */
	public static String relatedArticlesXMLPath = "src/test/resources/relatedArticles.xml";

	@SuppressWarnings("unchecked")
	@Test
	public void testReadRelatedArticles() throws Exception {
		ISearchService documentService = createMock(ISearchService.class);
		LegacySemedicoSearchResult ssr1 = new LegacySemedicoSearchResult(null);
		LegacySemedicoSearchResult ssr2 = new LegacySemedicoSearchResult(null);
		LegacySemedicoSearchResult ssr3 = new LegacySemedicoSearchResult(null);
		LegacySemedicoSearchResult ssr4 = new LegacySemedicoSearchResult(null);
		LegacySemedicoSearchResult ssr5 = new LegacySemedicoSearchResult(null);
		LegacySemedicoSearchResult ssr6 = new LegacySemedicoSearchResult(null);
		ssr1.semedicoDoc = new HighlightedSemedicoDocument(new SemedicoDocument(log, "10782842", IIndexInformationService.Indexes.DocumentTypes.medline));
		ssr2.semedicoDoc = new HighlightedSemedicoDocument(new SemedicoDocument(log, "10953975", IIndexInformationService.Indexes.DocumentTypes.medline));
		ssr3.semedicoDoc = new HighlightedSemedicoDocument(new SemedicoDocument(log, "11493382", IIndexInformationService.Indexes.DocumentTypes.medline));
		ssr4.semedicoDoc = new HighlightedSemedicoDocument(new SemedicoDocument(log, "12729371", IIndexInformationService.Indexes.DocumentTypes.medline));
		ssr5.semedicoDoc = new HighlightedSemedicoDocument(new SemedicoDocument(log, "8432509", IIndexInformationService.Indexes.DocumentTypes.medline));
		ssr6.semedicoDoc = new HighlightedSemedicoDocument(new SemedicoDocument(log, "16371804", IIndexInformationService.Indexes.DocumentTypes.medline));
		// Set up the futures for the search results; this is nothing more than to wrap the SemedicoSearchResults
		// created above into future mock objects.
		Future<SemedicoSearchResult> ssr1f = createMock(Future.class);
		Future<SemedicoSearchResult> ssr2f = createMock(Future.class);
		Future<SemedicoSearchResult> ssr3f = createMock(Future.class);
		Future<SemedicoSearchResult> ssr4f = createMock(Future.class);
		Future<SemedicoSearchResult> ssr5f = createMock(Future.class);
		Future<SemedicoSearchResult> ssr6f = createMock(Future.class);
		expect(ssr1f.get()).andReturn(ssr1);
		expect(ssr2f.get()).andReturn(ssr2);
		expect(ssr3f.get()).andReturn(ssr3);
		expect(ssr4f.get()).andReturn(ssr4);
		expect(ssr5f.get()).andReturn(ssr5);
		expect(ssr6f.get()).andReturn(ssr6);
		replay(ssr1f);
		replay(ssr2f);
		replay(ssr3f);
		replay(ssr4f);
		replay(ssr5f);
		replay(ssr6f);
		// -------- END FUTURE CREATION ---------
		expect(documentService.doRelatedArticleSearch("10782842")).andReturn(ssr1f);
		expect(documentService.doRelatedArticleSearch("10953975")).andReturn(ssr2f);
		expect(documentService.doRelatedArticleSearch("11493382")).andReturn(ssr3f);
		expect(documentService.doRelatedArticleSearch("12729371")).andReturn(ssr4f);
		expect(documentService.doRelatedArticleSearch("8432509")).andReturn(ssr5f);
		expect(documentService.doRelatedArticleSearch("16371804")).andReturn(ssr6f);
		replay(documentService);
		RelatedArticlesService relatedArticlesService =
				new RelatedArticlesService(LoggerFactory.getLogger(RelatedArticlesService.class), documentService);
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(new FileInputStream(relatedArticlesXMLPath));
		Collection<SemedicoDocument> relatedArticles = relatedArticlesService.readRelatedArticles(testPmid, document);
		verify(documentService);
		assertTrue(relatedArticles.size() == IRelatedArticlesService.MAX_RELATED_ARTICLES);
	}
}
