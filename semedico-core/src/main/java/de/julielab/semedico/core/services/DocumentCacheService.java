/** 
 * DocumentCacheService.java
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
 * Creation date: 18.12.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.julielab.semedico.core.search.components.data.SemedicoDocument;
import de.julielab.semedico.core.services.interfaces.IDocumentCacheService;

public class DocumentCacheService implements IDocumentCacheService {
	private Map<String, SemedicoDocument> documentCache;

	public DocumentCacheService() {
		documentCache = new ConcurrentHashMap<>();
	}
	
	@Override
	public SemedicoDocument getCachedDocument(int docId) {
		return documentCache.get(docId);
	}

	@Override
	public void addDocument(SemedicoDocument document) {
		documentCache.put(document.getDocId(), document);
	}

}
