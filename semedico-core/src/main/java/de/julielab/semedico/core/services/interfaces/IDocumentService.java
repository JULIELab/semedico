package de.julielab.semedico.core.services.interfaces;

import java.util.List;

import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.HighlightedStatement;
import de.julielab.semedico.core.search.components.data.SemedicoDocument;

public interface IDocumentService {

	/**
	 * Only used by related article service to get an unhighlighted document
	 * from which only the title, publication title and review status a
	 * currrently used
	 * 
	 * @param pmid
	 * @return
	 */
	@Deprecated
	 SemedicoDocument getSemedicoDocument(int pmid);

	HighlightedSemedicoDocument getHitListDocument(ISearchServerDocument serverDoc);

	/**
	 * Only used by Article to get a fully highlighted abstract.
	 * 
	 * @param pubMedId
	 * @param originalQueryString
	 * @return
	 */
	 HighlightedSemedicoDocument getHighlightedSemedicoDocument(
			ISearchServerDocument solrDoc);
	
	 List<HighlightedStatement> getHighlightedStatements(ISearchServerDocument serverDoc);

}