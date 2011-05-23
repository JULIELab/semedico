/** 
 * ExternalLinkService.java
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

package de.julielab.stemnet.core.services;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.julielab.stemnet.core.ExternalLink;

public class ExternalLinkService implements IExternalLinkService{

	private static final String EUTILS_LLINKS_URL = "http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&cmd=llinks"; 
	private static final String EUTILS_PRLINKS_URL = "http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&cmd=prlinks";
	
	private DocumentBuilder documentBuilder;
	private static final Logger LOGGER = Logger.getLogger(ExternalLinkService.class);
	
	private static final String OBJ_URL_TAG = "ObjUrl";
	private static final String URL_TAG = "Url";
	private static final String ICON_URL_TAG = "IconUrl";
	private static final String ATTRIBUTE_TAG = "Attribute";
	private static final String ID_URL_SET_TAG = "IdUrlSet";
	private static final String ID_TAG = "Id";
	
	public ExternalLinkService() {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			documentBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
			throw new IllegalStateException(e);
		}
	}
	private Document executeGet(String urlString) throws IOException, SAXException{
		// Create a URL for the desired page
		URL url = new URL(urlString);

		// Read all the text returned by the server
		Document document = documentBuilder.parse(url.openStream());
		return document;
	}
	
	public void markFullTexts(Collection<de.julielab.stemnet.core.SemedicoDocument> documents) throws IOException{
		if( documents.size() == 0 )
			return;
		
		String ids = "";
		Map<String, de.julielab.stemnet.core.SemedicoDocument> hitsById = new HashMap<String, de.julielab.stemnet.core.SemedicoDocument>();
		
		for( de.julielab.stemnet.core.SemedicoDocument document: documents ){
			String pmid = document.getPubmedId().toString();
			ids += pmid + ",";
			hitsById.put(pmid, document);
		}
		
		ids = ids.substring(0, ids.length()-1);
		String url = EUTILS_PRLINKS_URL + "&id=" + ids;
		Document document = null;
		
		try {
			document = executeGet(url);
		} catch (SAXException e) {
			throw new IOException(e);
		}
		
		NodeList sets = document.getElementsByTagName(ID_URL_SET_TAG);
		for( int i = 0; i < sets.getLength(); i++ ){
			Node urlSet = sets.item(i);
			NodeList setChilds = urlSet.getChildNodes();
			String pmid = null;
			boolean hasLink = false;
			
			for( int j= 0; j < setChilds.getLength(); j++ ){
				Node setChild = setChilds.item(j);
				String setChildName = setChild.getNodeName();

				
				if( setChildName != null && setChildName.equals(ID_TAG) )
					pmid = setChild.getTextContent();
				if( setChildName != null && setChildName.equals(OBJ_URL_TAG) )
					hasLink = true;
			}
			if( hasLink )
				hitsById.get(pmid).setType(de.julielab.stemnet.core.SemedicoDocument.TYPE_FULL_TEXT);
		}
	}
	
	@Override
	public Collection<ExternalLink> fetchExternalLinks(Integer pmid) throws IOException {
		String url = EUTILS_LLINKS_URL + "&id=" + pmid;
		Document document = null;
		Collection<ExternalLink> externalLinks = new ArrayList<ExternalLink>();
		
		try {
			document = executeGet(url);
		} catch (SAXException e) {
			throw new IOException(e);
		}
		NodeList links = document.getElementsByTagName(OBJ_URL_TAG);
		for( int i = 0; i < links.getLength(); i++ ){
			String linkUrl = "";
			String iconUrl = "";
			Node link = links.item(i);
			NodeList linkChilds = link.getChildNodes();
			boolean fullText = false;
			for( int j = 0; j < linkChilds.getLength(); j++ ){
				Node linkChild = linkChilds.item(j);
				if( linkChild.getNodeName() != null && linkChild.getNodeName().equals(URL_TAG) )
					linkUrl = linkChild.getTextContent();
				else if( linkChild.getNodeName() != null && linkChild.getNodeName().equals(ICON_URL_TAG) )
					iconUrl = linkChild.getTextContent();
				else if( linkChild.getNodeName() != null && linkChild.getNodeName().equals(ATTRIBUTE_TAG) )
					if( linkChild.getTextContent().contains("full-text") )
						fullText = true;
			}
			
			if( fullText )
				externalLinks.add(new ExternalLink(linkUrl, iconUrl));
		}
		
		return externalLinks;
	}
}
