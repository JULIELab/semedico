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

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.SemedicoDocument;

public class RelatedArticlesService implements IRelatedArticlesService {

	private DocumentBuilder documentBuilder;
	private static final Logger LOGGER = Logger.getLogger(RelatedArticlesService.class);
	private static final String EUTILS_URL = "http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&datetype=pdat&id=";
	private static final String LINK_TAG = "Link";
	private static final Object ID_TAG = "Id";
	private static final int MAX_RELATED_ARTICLES = 5;
	
	private IDocumentService documentService;
	
	public RelatedArticlesService() {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			documentBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public Collection<SemedicoDocument> fetchRelatedArticles(Integer pmid) throws IOException {
		org.w3c.dom.Document document = null;
		try {
			document = executeGet(EUTILS_URL+pmid);
		} catch (SAXException e) {
			throw new IOException(e);
		}
		Collection<SemedicoDocument> relatedArticles = new ArrayList<SemedicoDocument>();
		NodeList links = document.getElementsByTagName(LINK_TAG);
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
			SemedicoDocument hit = documentService.readDocumentStubWithPubMedId(relatedPmidInt);
			if( hit != null && !relatedPmidInt.equals(pmid) )
				relatedArticles.add(hit);
			
			if( relatedArticles.size() == MAX_RELATED_ARTICLES )
				break;
		}
		return relatedArticles;
	}

	private org.w3c.dom.Document executeGet(String urlString) throws IOException, SAXException{
		// Create a URL for the desired page
		URL url = new URL(urlString);

		// Read all the text returned by the server
		org.w3c.dom.Document document = documentBuilder.parse(url.openStream());
		return document;
	}

	public IDocumentService getHitService() {
		return documentService;
	}

	public void setDocumentService(IDocumentService documentService) {
		this.documentService = documentService;
	}
}
