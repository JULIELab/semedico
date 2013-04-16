/** 
 * RelatedArticlesService.java
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.services.interfaces.IRelatedArticlesService;
import de.julielab.semedico.core.services.interfaces.ISearchService;

public class RelatedArticlesService implements IRelatedArticlesService {

	private DocumentBuilder documentBuilder;
	private static final String EUTILS_URL = "http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&datetype=pdat&id=";
	private static final String LINK_TAG = "Link";
	private static final Object ID_TAG = "Id";
	
	
	private final Logger logger;
	private final ISearchService searchService;
	
	public RelatedArticlesService(Logger logger, ISearchService searchService) {
	    this.logger = logger;
		this.searchService = searchService;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			documentBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage(), e);
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public Collection<SemedicoDocument> fetchRelatedArticles(Integer pmid) throws IOException {
		org.w3c.dom.Document document = null;
		try {
			String urlString = EUTILS_URL+pmid;
			logger.debug("Fetching related articles for \"{}\". URL: {}", pmid, urlString);
			document = executeGet(urlString);
		} catch (SAXException e) {
			throw new IOException(e);
		}
		return readRelatedArticles(pmid, document);
	}

	/**
	 * @param pmid
	 * @param document
	 * @return
	 */
	protected Collection<SemedicoDocument> readRelatedArticles(Integer pmid,
			org.w3c.dom.Document document) {
		Collection<SemedicoDocument> relatedArticles = new ArrayList<SemedicoDocument>();
		NodeList links = document.getElementsByTagName(LINK_TAG);
		logger.debug("Retrieved {} related articles from NLM.", links.getLength());
		for( int i = 0; i < links.getLength(); i++ ){
			String relatedPmid = "";

			Node link = links.item(i);
			NodeList linkChilds = link.getChildNodes();
			
			for( int j = 0; j < linkChilds.getLength(); j++ ){
				Node linkChild = linkChilds.item(j);
				if( linkChild.getNodeName() != null && linkChild.getNodeName().equals(ID_TAG) )
					relatedPmid = linkChild.getTextContent();
			}
			Integer relatedPmidInt = new Integer(relatedPmid);
			SemedicoDocument hit = searchService.doRelatedArticleSearch(relatedPmidInt).semedicoDoc;
			if( hit != null && !relatedPmidInt.equals(pmid) )
				relatedArticles.add(hit);
			
			if( relatedArticles.size() == MAX_RELATED_ARTICLES )
				break;
		}
		logger.debug("Read {} related articles (cut off at {}).", relatedArticles.size(), MAX_RELATED_ARTICLES);
		return relatedArticles;
	}

	private org.w3c.dom.Document executeGet(String urlString) throws IOException, SAXException{
		// Create a URL for the desired page
		URL url = new URL(urlString);

		// Read all the text returned by the server
		long time = System.currentTimeMillis();
		org.w3c.dom.Document document = documentBuilder.parse(url.openStream());
		time = System.currentTimeMillis() - time;
		logger.debug("Retrieving data from URL took {}ms.", time);
		return document;
	}

//	public IDocumentService getHitService() {
//		return documentService;
//	}
//
//	public void setDocumentService(IDocumentService documentService) {
//		this.documentService = documentService;
//	}
}
