package de.julielab.semedico.core.services.interfaces;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerDocument;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;

public interface IDocumentService {

	/**
	 * Just a conversion function from a document search response with separate
	 * elasticDocs and corresponding Highlighting to a DocumentHit object used in
	 * the ResultList page.
	 * 
	 * @param elasticDoc
	 * @return
	 */
	HighlightedSemedicoDocument getHitListDocument(
		ISearchServerDocument elasticDoc);

	/**
	 * Only used by Article to get a fully highlighted abstract.
	 * 
	 * @param elasticDoc
	 * @return
	 */
	public HighlightedSemedicoDocument getHighlightedSemedicoDocument(
			ISearchServerDocument elasticDoc);
	
}