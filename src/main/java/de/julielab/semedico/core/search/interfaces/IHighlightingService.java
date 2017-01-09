/** 
 * IKwicService.java
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
 * Creation date: 04.03.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.search.interfaces;

import java.util.List;
import java.util.Map;

import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.semedico.core.HighlightedSemedicoDocument.AuthorHighlight;
import de.julielab.semedico.core.HighlightedSemedicoDocument.Highlight;

public interface IHighlightingService {

	@Deprecated
	String getHighlightedTitle(Map<String, List<String>> docHighlights);

	Highlight getHighlightedAbstract(ISearchServerDocument serverDoc);

	@Deprecated
	String[] getAbstractHighlights(Map<String, List<String>> docHighlights);
	
	List<Highlight> getEventHighlights(ISearchServerDocument serverDoc);
	
	List<Highlight> getSentenceHighlights(ISearchServerDocument serverDoc);

	Highlight getTitleHighlight(ISearchServerDocument serverDoc);

	List<Highlight> getBestTextContentHighlights(ISearchServerDocument serverDoc, int num, String... excludedText);

	List<Highlight> getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued);

	List<Highlight> getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
			boolean replaceMissingWithFieldValue, boolean merge);

	List<Highlight> getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
			boolean replaceMissingWithFieldValue, boolean merge, boolean replaceConceptIds);

	List<Highlight> getFieldHighlights(ISearchServerDocument serverDoc, String journalvolume, boolean multivalued, boolean replaceMissingWithFieldValue);

	List<AuthorHighlight> getAuthorHighlights(ISearchServerDocument serverDoc);
}
